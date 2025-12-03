-- ============================================================================
-- Migration V020: Views e Views Materializadas para Módulo de Trilhas
-- ============================================================================
-- Este arquivo contém views simples e materializadas para consultas
-- frequentes e dashboards do módulo de trilhas.
--
-- Views Simples: Consultas em tempo real
-- Views Materializadas: Estatísticas agregadas com refresh periódico
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. View: v_student_active_trails
-- ----------------------------------------------------------------------------
-- Trilhas ativas por aluno com informações consolidadas
-- Uso: Listagem de trilhas na dashboard do estudante

CREATE OR REPLACE VIEW v_student_active_trails AS
SELECT
    t.student_id,
    t.id AS trail_id,
    l.code AS language_code,
    l.name_pt AS language_name,
    l.name_native AS language_name_native,
    lv.code AS level_code,
    lv.name AS level_name,
    t.status,
    COALESCE(tp.progress_percentage, 0) AS progress_percentage,
    COALESCE(tp.total_lessons, 0) AS total_lessons,
    COALESCE(tp.lessons_completed, 0) AS completed_lessons,
    COALESCE(tp.time_spent_minutes / 60.0, 0) AS time_spent_hours,
    tp.last_activity_at AS last_accessed_at,
    t.created_at,
    t.curriculum_version
FROM trails t
JOIN languages l ON t.language_code = l.code
JOIN levels lv ON t.level_id = lv.id
LEFT JOIN trail_progress tp ON t.id = tp.trail_id
WHERE t.status != 'ARCHIVED';

COMMENT ON VIEW v_student_active_trails IS
'Trilhas ativas por aluno com informações consolidadas. Exclui trilhas arquivadas.';

-- ----------------------------------------------------------------------------
-- 2. View: v_trail_details
-- ----------------------------------------------------------------------------
-- Detalhes completos de uma trilha para API
-- Uso: Endpoint GET /api/trails/{id}

CREATE OR REPLACE VIEW v_trail_details AS
SELECT
    t.id AS trail_id,
    t.student_id,
    l.code AS language_code,
    l.name_pt AS language_name,
    lv.code AS level_code,
    lv.name AS level_name,
    t.status,
    t.curriculum_version,
    t.estimated_duration_hours,
    t.created_at,
    t.updated_at,
    -- Contadores
    (SELECT COUNT(*) FROM modules WHERE trail_id = t.id) AS modules_count,
    (SELECT COUNT(*) FROM lessons ls
     JOIN modules m ON ls.module_id = m.id
     WHERE m.trail_id = t.id) AS lessons_count,
    -- Módulos como JSON array
    (SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', m.id,
            'title', m.title,
            'competency_code', c.code,
            'competency_name', c.name,
            'order_index', m.order_index,
            'status', m.status,
            'lessons_count', (SELECT COUNT(*) FROM lessons WHERE module_id = m.id)
        ) ORDER BY m.order_index
    ), '[]'::jsonb)
    FROM modules m
    JOIN competencies c ON m.competency_id = c.id
    WHERE m.trail_id = t.id) AS modules
FROM trails t
JOIN languages l ON t.language_code = l.code
JOIN levels lv ON t.level_id = lv.id;

COMMENT ON VIEW v_trail_details IS
'Detalhes completos de uma trilha incluindo módulos agregados em JSON.';

-- ----------------------------------------------------------------------------
-- 3. View: v_module_with_lessons
-- ----------------------------------------------------------------------------
-- Módulo com suas lições e progresso
-- Uso: Endpoint GET /api/modules/{id}

CREATE OR REPLACE VIEW v_module_with_lessons AS
SELECT
    m.id AS module_id,
    m.trail_id,
    m.title,
    c.code AS competency_code,
    c.name AS competency_name,
    m.order_index,
    m.status,
    -- Progresso do módulo
    (SELECT COUNT(*) FROM lessons WHERE module_id = m.id) AS total_lessons,
    (SELECT COUNT(*) FROM lessons WHERE module_id = m.id AND completed_at IS NOT NULL) AS completed_lessons,
    -- Lições como JSON array
    (SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', ls.id,
            'title', ls.title,
            'type', ls.type,
            'order_index', ls.order_index,
            'duration_minutes', ls.duration_minutes,
            'is_completed', ls.completed_at IS NOT NULL,
            'completed_at', ls.completed_at,
            'score', ls.score
        ) ORDER BY ls.order_index
    ), '[]'::jsonb)
    FROM lessons ls
    WHERE ls.module_id = m.id) AS lessons
FROM modules m
JOIN competencies c ON m.competency_id = c.id;

COMMENT ON VIEW v_module_with_lessons IS
'Módulo com suas lições agregadas em JSON e informações de progresso.';

-- ----------------------------------------------------------------------------
-- 4. View: v_pending_generations
-- ----------------------------------------------------------------------------
-- Jobs de geração pendentes para dashboard de administração
-- Uso: Dashboard de monitoramento de fila

CREATE OR REPLACE VIEW v_pending_generations AS
SELECT
    j.id AS job_id,
    j.trail_id,
    j.student_id,
    j.status,
    j.priority,
    j.job_type,
    j.queued_at,
    j.attempt_count,
    j.max_attempts,
    j.last_error,
    j.next_retry_at,
    -- Tempo estimado de espera baseado em posição na fila e média histórica
    (SELECT COUNT(*) FROM trail_generation_jobs
     WHERE status = 'QUEUED'
       AND (priority < j.priority OR (priority = j.priority AND queued_at < j.queued_at))
    ) AS queue_position,
    -- Estimativa baseada em média de 30 segundos por job na fila
    CEIL((SELECT COUNT(*) FROM trail_generation_jobs
          WHERE status = 'QUEUED'
            AND (priority < j.priority OR (priority = j.priority AND queued_at < j.queued_at))
         ) * 0.5) AS estimated_wait_minutes
FROM trail_generation_jobs j
WHERE j.status IN ('QUEUED', 'PROCESSING', 'FAILED')
  AND (j.status != 'FAILED' OR j.attempt_count < j.max_attempts);

COMMENT ON VIEW v_pending_generations IS
'Jobs de geração pendentes ou em processamento com estimativa de tempo de espera.';

-- ----------------------------------------------------------------------------
-- 5. View Materializada: mv_blueprint_usage_stats
-- ----------------------------------------------------------------------------
-- Estatísticas de uso de blueprints
-- Refresh: Diariamente

CREATE MATERIALIZED VIEW IF NOT EXISTS mv_blueprint_usage_stats AS
SELECT
    b.id AS blueprint_id,
    l.code AS language_code,
    lv.code AS level_code,
    b.name AS blueprint_name,
    b.usage_count,
    b.avg_completion_rate,
    -- Tempo médio de geração das trilhas que usaram este blueprint
    (SELECT AVG(j.processing_time_ms)
     FROM trail_generation_jobs j
     JOIN trails t ON j.trail_id = t.id
     WHERE t.blueprint_id = b.id
       AND j.status = 'COMPLETED'
    ) AS avg_generation_time_ms,
    -- Última vez que foi usado
    (SELECT MAX(t.created_at)
     FROM trails t
     WHERE t.blueprint_id = b.id
    ) AS last_used_at,
    b.is_approved,
    b.created_at
FROM blueprints b
JOIN languages l ON b.language_code = l.code
JOIN levels lv ON b.level_id = lv.id
WITH DATA;

COMMENT ON MATERIALIZED VIEW mv_blueprint_usage_stats IS
'Estatísticas agregadas de uso de blueprints. Atualizar diariamente.';

-- Índices para mv_blueprint_usage_stats
CREATE INDEX IF NOT EXISTS idx_mv_blueprint_usage_language_level
ON mv_blueprint_usage_stats(language_code, level_code);

CREATE INDEX IF NOT EXISTS idx_mv_blueprint_usage_count
ON mv_blueprint_usage_stats(usage_count DESC);

-- ----------------------------------------------------------------------------
-- 6. View Materializada: mv_content_block_stats
-- ----------------------------------------------------------------------------
-- Estatísticas de uso de blocos de conteúdo
-- Refresh: Diariamente

CREATE MATERIALIZED VIEW IF NOT EXISTS mv_content_block_stats AS
SELECT
    cb.id AS content_block_id,
    d.code AS descriptor_code,
    cb.title,
    cb.type,
    cb.usage_count,
    cb.quality_score,
    -- Score médio obtido pelos estudantes neste conteúdo
    (SELECT AVG(ls.score)
     FROM lessons ls
     WHERE ls.content_block_id = cb.id
       AND ls.score IS NOT NULL
    ) AS avg_student_score,
    -- Tempo médio gasto pelos estudantes
    (SELECT AVG(ls.time_spent_seconds)
     FROM lessons ls
     WHERE ls.content_block_id = cb.id
       AND ls.time_spent_seconds IS NOT NULL
    ) AS avg_time_spent_seconds,
    -- Última vez que foi usado
    (SELECT MAX(ls.created_at)
     FROM lessons ls
     WHERE ls.content_block_id = cb.id
    ) AS last_used_at,
    cb.is_approved,
    cb.generation_source,
    cb.created_at
FROM content_blocks cb
JOIN descriptors d ON cb.descriptor_id = d.id
WITH DATA;

COMMENT ON MATERIALIZED VIEW mv_content_block_stats IS
'Estatísticas agregadas de uso de blocos de conteúdo. Atualizar diariamente.';

-- Índices para mv_content_block_stats
CREATE INDEX IF NOT EXISTS idx_mv_content_stats_usage
ON mv_content_block_stats(usage_count DESC);

CREATE INDEX IF NOT EXISTS idx_mv_content_stats_type
ON mv_content_block_stats(type);

CREATE INDEX IF NOT EXISTS idx_mv_content_stats_quality
ON mv_content_block_stats(quality_score DESC NULLS LAST);

-- ----------------------------------------------------------------------------
-- 7. View Materializada: mv_generation_metrics
-- ----------------------------------------------------------------------------
-- Métricas de geração agregadas por dia para monitoramento
-- Refresh: A cada hora

CREATE MATERIALIZED VIEW IF NOT EXISTS mv_generation_metrics AS
SELECT
    DATE(j.created_at) AS date,
    COUNT(*) AS total_jobs,
    COUNT(*) FILTER (WHERE j.status = 'COMPLETED') AS completed_jobs,
    COUNT(*) FILTER (WHERE j.status = 'FAILED') AS failed_jobs,
    COUNT(*) FILTER (WHERE j.status = 'CANCELLED') AS cancelled_jobs,
    COUNT(*) FILTER (WHERE j.status IN ('QUEUED', 'PROCESSING')) AS pending_jobs,
    -- Taxa de sucesso
    ROUND(
        COUNT(*) FILTER (WHERE j.status = 'COMPLETED')::DECIMAL /
        NULLIF(COUNT(*) FILTER (WHERE j.status IN ('COMPLETED', 'FAILED')), 0) * 100,
        2
    ) AS success_rate,
    -- Tempos médios
    AVG(j.processing_time_ms) FILTER (WHERE j.status = 'COMPLETED') AS avg_processing_time_ms,
    PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY j.processing_time_ms)
        FILTER (WHERE j.status = 'COMPLETED') AS median_processing_time_ms,
    PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY j.processing_time_ms)
        FILTER (WHERE j.status = 'COMPLETED') AS p95_processing_time_ms,
    -- Tokens
    SUM(j.tokens_used) AS total_tokens_used,
    AVG(j.tokens_used) FILTER (WHERE j.status = 'COMPLETED') AS avg_tokens_per_job,
    -- Cache (estimativa baseada em trails que usaram blueprint existente)
    ROUND(
        (SELECT COUNT(*)::DECIMAL FROM trails t
         WHERE DATE(t.created_at) = DATE(j.created_at)
           AND t.blueprint_id IS NOT NULL) /
        NULLIF((SELECT COUNT(*) FROM trails t
                WHERE DATE(t.created_at) = DATE(j.created_at)), 0) * 100,
        2
    ) AS blueprint_reuse_rate,
    -- Retries
    AVG(j.attempt_count) FILTER (WHERE j.status = 'COMPLETED') AS avg_attempts,
    COUNT(*) FILTER (WHERE j.attempt_count > 1 AND j.status = 'COMPLETED') AS jobs_with_retries
FROM trail_generation_jobs j
GROUP BY DATE(j.created_at)
ORDER BY DATE(j.created_at) DESC
WITH DATA;

COMMENT ON MATERIALIZED VIEW mv_generation_metrics IS
'Métricas de geração agregadas por dia. Atualizar a cada hora.';

-- Índices para mv_generation_metrics
CREATE INDEX IF NOT EXISTS idx_mv_generation_date
ON mv_generation_metrics(date DESC);

-- ----------------------------------------------------------------------------
-- 8. View Materializada: mv_student_learning_summary
-- ----------------------------------------------------------------------------
-- Resumo de aprendizado por estudante
-- Refresh: Diariamente

CREATE MATERIALIZED VIEW IF NOT EXISTS mv_student_learning_summary AS
SELECT
    t.student_id,
    COUNT(DISTINCT t.id) AS total_trails,
    COUNT(DISTINCT t.id) FILTER (WHERE t.status = 'READY') AS active_trails,
    COUNT(DISTINCT t.id) FILTER (WHERE t.status = 'ARCHIVED') AS completed_trails,
    COUNT(DISTINCT t.language_code) AS languages_studied,
    -- Progresso geral
    AVG(tp.progress_percentage) AS avg_progress,
    SUM(tp.lessons_completed) AS total_lessons_completed,
    SUM(tp.time_spent_minutes) AS total_time_spent_minutes,
    AVG(tp.average_score) AS overall_avg_score,
    -- Atividade
    MAX(tp.last_activity_at) AS last_activity_at,
    MIN(t.created_at) AS first_trail_created_at
FROM trails t
LEFT JOIN trail_progress tp ON t.id = tp.trail_id
GROUP BY t.student_id
WITH DATA;

COMMENT ON MATERIALIZED VIEW mv_student_learning_summary IS
'Resumo de aprendizado agregado por estudante. Atualizar diariamente.';

-- Índices para mv_student_learning_summary
CREATE INDEX IF NOT EXISTS idx_mv_student_summary_student
ON mv_student_learning_summary(student_id);

CREATE INDEX IF NOT EXISTS idx_mv_student_summary_activity
ON mv_student_learning_summary(last_activity_at DESC NULLS LAST);

-- ----------------------------------------------------------------------------
-- 9. Função: refresh_trail_materialized_views
-- ----------------------------------------------------------------------------
-- Função para refresh de todas as views materializadas

CREATE OR REPLACE FUNCTION refresh_trail_materialized_views(
    p_concurrent BOOLEAN DEFAULT false
)
RETURNS TABLE(view_name TEXT, refresh_time_ms BIGINT)
LANGUAGE plpgsql
AS $$
DECLARE
    v_start_time TIMESTAMP;
    v_end_time TIMESTAMP;
    v_view_name TEXT;
    v_views TEXT[] := ARRAY[
        'mv_blueprint_usage_stats',
        'mv_content_block_stats',
        'mv_generation_metrics',
        'mv_student_learning_summary'
    ];
BEGIN
    FOREACH v_view_name IN ARRAY v_views
    LOOP
        v_start_time := clock_timestamp();

        IF p_concurrent THEN
            EXECUTE format('REFRESH MATERIALIZED VIEW CONCURRENTLY %I', v_view_name);
        ELSE
            EXECUTE format('REFRESH MATERIALIZED VIEW %I', v_view_name);
        END IF;

        v_end_time := clock_timestamp();

        view_name := v_view_name;
        refresh_time_ms := EXTRACT(MILLISECONDS FROM (v_end_time - v_start_time))::BIGINT;

        RETURN NEXT;
    END LOOP;

    RETURN;
END;
$$;

COMMENT ON FUNCTION refresh_trail_materialized_views(BOOLEAN) IS
'Atualiza todas as views materializadas do módulo de trilhas. Use p_concurrent=true para refresh sem lock.';

-- ----------------------------------------------------------------------------
-- 10. Função: refresh_single_materialized_view
-- ----------------------------------------------------------------------------
-- Função para refresh de uma view específica

CREATE OR REPLACE FUNCTION refresh_single_materialized_view(
    p_view_name TEXT,
    p_concurrent BOOLEAN DEFAULT false
)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    v_start_time TIMESTAMP;
    v_end_time TIMESTAMP;
BEGIN
    v_start_time := clock_timestamp();

    IF p_concurrent THEN
        EXECUTE format('REFRESH MATERIALIZED VIEW CONCURRENTLY %I', p_view_name);
    ELSE
        EXECUTE format('REFRESH MATERIALIZED VIEW %I', p_view_name);
    END IF;

    v_end_time := clock_timestamp();

    RETURN EXTRACT(MILLISECONDS FROM (v_end_time - v_start_time))::BIGINT;
END;
$$;

COMMENT ON FUNCTION refresh_single_materialized_view(TEXT, BOOLEAN) IS
'Atualiza uma view materializada específica. Retorna tempo de execução em ms.';

-- ============================================================================
-- ROLLBACK SCRIPT (executar na ordem inversa em caso de rollback)
-- ============================================================================
--
-- Para reverter esta migration, execute os comandos abaixo na ordem:
--
-- -- Remover funções
-- DROP FUNCTION IF EXISTS refresh_single_materialized_view(TEXT, BOOLEAN);
-- DROP FUNCTION IF EXISTS refresh_trail_materialized_views(BOOLEAN);
--
-- -- Remover views materializadas
-- DROP MATERIALIZED VIEW IF EXISTS mv_student_learning_summary CASCADE;
-- DROP MATERIALIZED VIEW IF EXISTS mv_generation_metrics CASCADE;
-- DROP MATERIALIZED VIEW IF EXISTS mv_content_block_stats CASCADE;
-- DROP MATERIALIZED VIEW IF EXISTS mv_blueprint_usage_stats CASCADE;
--
-- -- Remover views simples
-- DROP VIEW IF EXISTS v_pending_generations CASCADE;
-- DROP VIEW IF EXISTS v_module_with_lessons CASCADE;
-- DROP VIEW IF EXISTS v_trail_details CASCADE;
-- DROP VIEW IF EXISTS v_student_active_trails CASCADE;
--
-- ============================================================================
