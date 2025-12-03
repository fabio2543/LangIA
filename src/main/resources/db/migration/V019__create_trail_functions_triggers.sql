-- ============================================================================
-- Migration V019: Funções e Triggers para Módulo de Trilhas
-- ============================================================================
-- Este arquivo contém funções utilitárias e triggers para automação
-- de processos no módulo de trilhas.
--
-- Funções: Hash, Signature, Versioning, Limits, Progress, Usage
-- Triggers: Validação, Atualização automática, Contadores
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Função: calculate_trail_hash
-- ----------------------------------------------------------------------------
-- Calcula hash único para identificação de trilha baseado em parâmetros
-- Usado para cache e deduplicação de trilhas similares

CREATE OR REPLACE FUNCTION calculate_trail_hash(
    p_language_code VARCHAR,
    p_level_code VARCHAR,
    p_preferences JSONB,
    p_curriculum_version VARCHAR
)
RETURNS VARCHAR(40)
LANGUAGE plpgsql
IMMUTABLE
AS $$
DECLARE
    v_sorted_prefs TEXT;
    v_concat_string TEXT;
    v_hash VARCHAR(40);
BEGIN
    -- Ordena as chaves do JSON para consistência
    SELECT jsonb_agg(elem ORDER BY elem->>'key')::TEXT
    INTO v_sorted_prefs
    FROM (
        SELECT jsonb_build_object('key', key, 'value', value) AS elem
        FROM jsonb_each(p_preferences)
    ) AS sorted;

    -- Se preferences for vazio ou null, usa string vazia
    IF v_sorted_prefs IS NULL THEN
        v_sorted_prefs := '';
    END IF;

    -- Concatena todos os parâmetros com separador
    v_concat_string := p_language_code || '|' ||
                       p_level_code || '|' ||
                       encode(digest(v_sorted_prefs, 'sha1'), 'hex') || '|' ||
                       p_curriculum_version;

    -- Calcula SHA-1 final
    v_hash := encode(digest(v_concat_string, 'sha1'), 'hex');

    RETURN v_hash;
END;
$$;

COMMENT ON FUNCTION calculate_trail_hash(VARCHAR, VARCHAR, JSONB, VARCHAR) IS
'Calcula hash SHA-1 único para identificação de trilha. IMMUTABLE para otimização de queries.';

-- ----------------------------------------------------------------------------
-- 2. Função: calculate_preferences_signature
-- ----------------------------------------------------------------------------
-- Gera assinatura das preferências para matching de blueprints

CREATE OR REPLACE FUNCTION calculate_preferences_signature(
    p_preferences JSONB
)
RETURNS VARCHAR(64)
LANGUAGE plpgsql
IMMUTABLE
AS $$
DECLARE
    v_sorted_json TEXT;
    v_signature VARCHAR(64);
BEGIN
    -- Serializa JSON com chaves ordenadas recursivamente
    SELECT jsonb_agg(
        jsonb_build_object(
            'key', key,
            'value', CASE
                WHEN jsonb_typeof(value) = 'object' THEN
                    (SELECT jsonb_agg(elem ORDER BY elem->>'k')
                     FROM jsonb_each(value) AS x(k, v),
                     LATERAL (SELECT jsonb_build_object('k', k, 'v', v) AS elem) AS y)
                WHEN jsonb_typeof(value) = 'array' THEN
                    (SELECT jsonb_agg(elem ORDER BY elem) FROM jsonb_array_elements(value) AS elem)
                ELSE value
            END
        ) ORDER BY key
    )::TEXT
    INTO v_sorted_json
    FROM jsonb_each(p_preferences);

    IF v_sorted_json IS NULL THEN
        v_sorted_json := '{}';
    END IF;

    -- Retorna SHA-256 truncado para 64 caracteres
    v_signature := encode(digest(v_sorted_json, 'sha256'), 'hex');

    RETURN v_signature;
END;
$$;

COMMENT ON FUNCTION calculate_preferences_signature(JSONB) IS
'Gera assinatura SHA-256 das preferências para matching de blueprints. IMMUTABLE.';

-- ----------------------------------------------------------------------------
-- 3. Função: get_current_curriculum_version
-- ----------------------------------------------------------------------------
-- Retorna a versão atual do currículo

CREATE OR REPLACE FUNCTION get_current_curriculum_version()
RETURNS VARCHAR(20)
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    v_version VARCHAR(20);
BEGIN
    SELECT version INTO v_version
    FROM curriculum_versions
    WHERE is_current = true
    LIMIT 1;

    -- Se não houver versão current, retorna '1.0.0' como fallback
    IF v_version IS NULL THEN
        v_version := '1.0.0';
    END IF;

    RETURN v_version;
END;
$$;

COMMENT ON FUNCTION get_current_curriculum_version() IS
'Retorna a versão atual do currículo. Fallback para 1.0.0 se não configurado.';

-- ----------------------------------------------------------------------------
-- 4. Função: check_student_trail_limit
-- ----------------------------------------------------------------------------
-- Trigger function para verificar limite de 3 trilhas ativas por estudante

CREATE OR REPLACE FUNCTION check_student_trail_limit()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_active_count INTEGER;
    v_max_trails INTEGER := 3;
BEGIN
    -- Conta trilhas ativas do estudante (não arquivadas)
    SELECT COUNT(*) INTO v_active_count
    FROM trails
    WHERE student_id = NEW.student_id
      AND status != 'ARCHIVED';

    -- Verifica limite
    IF v_active_count >= v_max_trails THEN
        RAISE EXCEPTION 'Limite de trilhas ativas atingido. Máximo: % trilhas por estudante.', v_max_trails
            USING ERRCODE = 'P0001',
                  HINT = 'Arquive uma trilha existente antes de criar uma nova.';
    END IF;

    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION check_student_trail_limit() IS
'Trigger function que impede criação de mais de 3 trilhas ativas por estudante.';

-- ----------------------------------------------------------------------------
-- 5. Função: update_trail_progress
-- ----------------------------------------------------------------------------
-- Trigger function para atualizar progresso quando lição é completada

CREATE OR REPLACE FUNCTION update_trail_progress()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_module_id UUID;
    v_trail_id UUID;
    v_module_total INTEGER;
    v_module_completed INTEGER;
    v_trail_total INTEGER;
    v_trail_completed INTEGER;
    v_avg_score DECIMAL(5,2);
BEGIN
    -- Obtém IDs do módulo e trilha
    v_module_id := NEW.module_id;

    SELECT m.trail_id INTO v_trail_id
    FROM modules m
    WHERE m.id = v_module_id;

    -- Recalcula progresso do módulo
    SELECT
        COUNT(*),
        COUNT(*) FILTER (WHERE completed_at IS NOT NULL)
    INTO v_module_total, v_module_completed
    FROM lessons
    WHERE module_id = v_module_id;

    -- Atualiza progresso do módulo (se tabela module_progress existir)
    -- Esta parte será habilitada quando as tabelas de progresso forem criadas
    /*
    UPDATE module_progress
    SET
        lessons_completed = v_module_completed,
        progress_percentage = (v_module_completed::DECIMAL / NULLIF(v_module_total, 0)) * 100,
        updated_at = NOW()
    WHERE module_id = v_module_id;
    */

    -- Recalcula progresso da trilha
    SELECT
        COUNT(*),
        COUNT(*) FILTER (WHERE completed_at IS NOT NULL),
        AVG(score) FILTER (WHERE score IS NOT NULL)
    INTO v_trail_total, v_trail_completed, v_avg_score
    FROM lessons l
    JOIN modules m ON l.module_id = m.id
    WHERE m.trail_id = v_trail_id;

    -- Atualiza progresso da trilha (se tabela trail_progress existir)
    /*
    UPDATE trail_progress
    SET
        lessons_completed = v_trail_completed,
        total_lessons = v_trail_total,
        progress_percentage = (v_trail_completed::DECIMAL / NULLIF(v_trail_total, 0)) * 100,
        average_score = v_avg_score,
        last_activity_at = NOW(),
        updated_at = NOW()
    WHERE trail_id = v_trail_id;
    */

    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION update_trail_progress() IS
'Trigger function que recalcula progresso do módulo e trilha quando lição é completada.';

-- ----------------------------------------------------------------------------
-- 6. Função: archive_old_trail
-- ----------------------------------------------------------------------------
-- Trigger function para arquivar trilha anterior em refresh

CREATE OR REPLACE FUNCTION archive_old_trail()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    -- Arquiva a trilha anterior
    UPDATE trails
    SET
        status = 'ARCHIVED',
        archived_at = NOW(),
        updated_at = NOW()
    WHERE id = NEW.previous_trail_id
      AND status != 'ARCHIVED';

    -- Registra no audit log
    INSERT INTO trail_audit_logs (
        trail_id,
        student_id,
        action,
        actor_type,
        actor_id,
        details,
        previous_state,
        new_state
    ) VALUES (
        NEW.previous_trail_id,
        NEW.student_id,
        'archived',
        'system',
        NULL,
        jsonb_build_object(
            'reason', 'replaced_by_new_trail',
            'new_trail_id', NEW.id
        ),
        jsonb_build_object('status', 'READY'),
        jsonb_build_object('status', 'ARCHIVED')
    );

    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION archive_old_trail() IS
'Trigger function que arquiva trilha anterior quando nova trilha é criada com previous_trail_id.';

-- ----------------------------------------------------------------------------
-- 7. Função: update_content_block_usage
-- ----------------------------------------------------------------------------
-- Trigger function para incrementar contador de uso de content_blocks

CREATE OR REPLACE FUNCTION update_content_block_usage()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE content_blocks
    SET
        usage_count = usage_count + 1,
        updated_at = NOW()
    WHERE id = NEW.content_block_id;

    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION update_content_block_usage() IS
'Trigger function que incrementa usage_count de content_blocks quando usado em lição.';

-- ----------------------------------------------------------------------------
-- 8. Função: update_blueprint_usage
-- ----------------------------------------------------------------------------
-- Trigger function para incrementar contador de uso de blueprints

CREATE OR REPLACE FUNCTION update_blueprint_usage()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE blueprints
    SET
        usage_count = usage_count + 1,
        updated_at = NOW()
    WHERE id = NEW.blueprint_id;

    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION update_blueprint_usage() IS
'Trigger function que incrementa usage_count de blueprints quando usado em trilha.';

-- ----------------------------------------------------------------------------
-- 9. Função: cleanup_expired_cache
-- ----------------------------------------------------------------------------
-- Função para limpeza de cache expirado (chamada por job agendado)

CREATE OR REPLACE FUNCTION cleanup_expired_cache()
RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_deleted_count INTEGER;
BEGIN
    DELETE FROM trail_cache
    WHERE expires_at < NOW();

    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;

    -- Log opcional
    IF v_deleted_count > 0 THEN
        RAISE NOTICE 'Cache cleanup: % registros removidos', v_deleted_count;
    END IF;

    RETURN v_deleted_count;
END;
$$;

COMMENT ON FUNCTION cleanup_expired_cache() IS
'Remove registros expirados do trail_cache. Chamar via pg_cron ou application scheduler.';

-- ----------------------------------------------------------------------------
-- 10. Função: find_matching_blueprint
-- ----------------------------------------------------------------------------
-- Encontra blueprint mais adequado baseado em preferências

CREATE OR REPLACE FUNCTION find_matching_blueprint(
    p_language_id UUID,
    p_level_id UUID,
    p_preferences JSONB
)
RETURNS UUID
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    v_blueprint_id UUID;
BEGIN
    -- Busca blueprint aprovado que melhor corresponde às preferências
    SELECT b.id INTO v_blueprint_id
    FROM blueprints b
    WHERE b.language_id = p_language_id
      AND b.level_id = p_level_id
      AND b.is_approved = true
      -- Verifica se o padrão de preferências do blueprint é compatível
      AND (
          -- Learning styles match
          (b.preferences_pattern->'learningStyles' IS NULL
           OR b.preferences_pattern->'learningStyles' @> p_preferences->'learningStyles')
          -- Goals match
          AND (b.preferences_pattern->'goals' IS NULL
               OR b.preferences_pattern->'goals' @> p_preferences->'goals')
      )
    ORDER BY
        -- Prioriza blueprints mais utilizados e com maior taxa de conclusão
        b.usage_count DESC,
        COALESCE(b.avg_completion_rate, 0) DESC
    LIMIT 1;

    RETURN v_blueprint_id;
END;
$$;

COMMENT ON FUNCTION find_matching_blueprint(UUID, UUID, JSONB) IS
'Encontra o blueprint mais adequado baseado em idioma, nível e preferências do estudante.';

-- ============================================================================
-- TRIGGERS
-- ============================================================================

-- Nota: Os triggers abaixo serão ativados quando as tabelas correspondentes
-- forem criadas. Por enquanto, ficam como referência e serão habilitados
-- na migration que cria as tabelas trails, modules e lessons.

-- ----------------------------------------------------------------------------
-- Trigger: trg_check_trail_limit
-- ----------------------------------------------------------------------------
-- Será criado na migration de trails:
-- CREATE TRIGGER trg_check_trail_limit
--     BEFORE INSERT ON trails
--     FOR EACH ROW
--     EXECUTE FUNCTION check_student_trail_limit();

-- ----------------------------------------------------------------------------
-- Trigger: trg_update_progress
-- ----------------------------------------------------------------------------
-- Será criado na migration de lessons:
-- CREATE TRIGGER trg_update_progress
--     AFTER UPDATE ON lessons
--     FOR EACH ROW
--     WHEN (OLD.completed_at IS NULL AND NEW.completed_at IS NOT NULL)
--     EXECUTE FUNCTION update_trail_progress();

-- ----------------------------------------------------------------------------
-- Trigger: trg_archive_previous_trail
-- ----------------------------------------------------------------------------
-- Será criado na migration de trails:
-- CREATE TRIGGER trg_archive_previous_trail
--     AFTER INSERT ON trails
--     FOR EACH ROW
--     WHEN (NEW.previous_trail_id IS NOT NULL)
--     EXECUTE FUNCTION archive_old_trail();

-- ----------------------------------------------------------------------------
-- Trigger: trg_increment_content_usage
-- ----------------------------------------------------------------------------
-- Será criado na migration de lessons:
-- CREATE TRIGGER trg_increment_content_usage
--     AFTER INSERT ON lessons
--     FOR EACH ROW
--     WHEN (NEW.content_block_id IS NOT NULL)
--     EXECUTE FUNCTION update_content_block_usage();

-- ----------------------------------------------------------------------------
-- Trigger: trg_increment_blueprint_usage
-- ----------------------------------------------------------------------------
-- Será criado na migration de trails:
-- CREATE TRIGGER trg_increment_blueprint_usage
--     AFTER INSERT ON trails
--     FOR EACH ROW
--     WHEN (NEW.blueprint_id IS NOT NULL)
--     EXECUTE FUNCTION update_blueprint_usage();

-- ============================================================================
-- ROLLBACK SCRIPT (executar na ordem inversa em caso de rollback)
-- ============================================================================
--
-- Para reverter esta migration, execute os comandos abaixo na ordem:
--
-- -- Remover triggers (quando as tabelas existirem)
-- DROP TRIGGER IF EXISTS trg_increment_blueprint_usage ON trails;
-- DROP TRIGGER IF EXISTS trg_increment_content_usage ON lessons;
-- DROP TRIGGER IF EXISTS trg_archive_previous_trail ON trails;
-- DROP TRIGGER IF EXISTS trg_update_progress ON lessons;
-- DROP TRIGGER IF EXISTS trg_check_trail_limit ON trails;
--
-- -- Remover funções
-- DROP FUNCTION IF EXISTS find_matching_blueprint(UUID, UUID, JSONB);
-- DROP FUNCTION IF EXISTS cleanup_expired_cache();
-- DROP FUNCTION IF EXISTS update_blueprint_usage();
-- DROP FUNCTION IF EXISTS update_content_block_usage();
-- DROP FUNCTION IF EXISTS archive_old_trail();
-- DROP FUNCTION IF EXISTS update_trail_progress();
-- DROP FUNCTION IF EXISTS check_student_trail_limit();
-- DROP FUNCTION IF EXISTS get_current_curriculum_version();
-- DROP FUNCTION IF EXISTS calculate_preferences_signature(JSONB);
-- DROP FUNCTION IF EXISTS calculate_trail_hash(VARCHAR, VARCHAR, JSONB, VARCHAR);
--
-- ============================================================================
