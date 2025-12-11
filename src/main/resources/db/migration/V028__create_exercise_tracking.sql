-- ============================================================================
-- Migration V028: Sistema de Tracking de Exercícios e Erros
-- ============================================================================
-- Rastreia todas as respostas dos exercícios e identifica padrões de erros
-- Essencial para a métrica "Top 5 erros recorrentes"
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Tabela: exercise_responses (Respostas de exercícios)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS exercise_responses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    lesson_id UUID,
    exercise_id UUID,
    exercise_type VARCHAR(50) NOT NULL,
    skill_type VARCHAR(20) NOT NULL,
    user_response TEXT,
    correct_response TEXT,
    is_correct BOOLEAN NOT NULL,
    partial_score DECIMAL(5,2),
    error_type VARCHAR(100),
    error_details JSONB,
    response_time_ms INTEGER,
    hints_used INTEGER DEFAULT 0,
    attempt_number INTEGER DEFAULT 1,
    session_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_responses_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_responses_language FOREIGN KEY (language_code)
        REFERENCES languages(code) ON DELETE CASCADE,
    CONSTRAINT chk_responses_exercise_type CHECK (exercise_type IN (
        'listen', 'select', 'speak', 'write', 'fill_blank',
        'matching', 'ordering', 'translation', 'dictation',
        'multiple_choice', 'true_false', 'role_play'
    )),
    CONSTRAINT chk_responses_skill_type CHECK (skill_type IN (
        'listening', 'speaking', 'reading', 'writing',
        'grammar', 'vocabulary', 'pronunciation'
    )),
    CONSTRAINT chk_responses_score CHECK (partial_score IS NULL OR (partial_score >= 0 AND partial_score <= 100))
);

COMMENT ON TABLE exercise_responses IS 'Registro de todas as respostas de exercícios para análise de progresso';
COMMENT ON COLUMN exercise_responses.id IS 'Identificador único da resposta';
COMMENT ON COLUMN exercise_responses.user_id IS 'Usuário que respondeu';
COMMENT ON COLUMN exercise_responses.language_code IS 'Idioma do exercício';
COMMENT ON COLUMN exercise_responses.lesson_id IS 'Lição à qual o exercício pertence';
COMMENT ON COLUMN exercise_responses.exercise_id IS 'Identificador do exercício';
COMMENT ON COLUMN exercise_responses.exercise_type IS 'Tipo do exercício';
COMMENT ON COLUMN exercise_responses.skill_type IS 'Habilidade exercitada';
COMMENT ON COLUMN exercise_responses.user_response IS 'Resposta do usuário';
COMMENT ON COLUMN exercise_responses.correct_response IS 'Resposta correta esperada';
COMMENT ON COLUMN exercise_responses.is_correct IS 'Se a resposta está correta';
COMMENT ON COLUMN exercise_responses.partial_score IS 'Pontuação parcial (0-100) para respostas parcialmente corretas';
COMMENT ON COLUMN exercise_responses.error_type IS 'Categoria do erro (se incorreto)';
COMMENT ON COLUMN exercise_responses.error_details IS 'Detalhes do erro em JSON';
COMMENT ON COLUMN exercise_responses.response_time_ms IS 'Tempo de resposta em milissegundos';
COMMENT ON COLUMN exercise_responses.hints_used IS 'Número de dicas utilizadas';
COMMENT ON COLUMN exercise_responses.attempt_number IS 'Número da tentativa (para exercícios com múltiplas tentativas)';
COMMENT ON COLUMN exercise_responses.session_id IS 'ID da sessão de estudo';

-- Índices para exercise_responses
CREATE INDEX IF NOT EXISTS idx_responses_user ON exercise_responses(user_id);
CREATE INDEX IF NOT EXISTS idx_responses_user_date ON exercise_responses(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_responses_lesson ON exercise_responses(lesson_id);
CREATE INDEX IF NOT EXISTS idx_responses_skill ON exercise_responses(user_id, skill_type);
CREATE INDEX IF NOT EXISTS idx_responses_incorrect ON exercise_responses(user_id, is_correct)
    WHERE is_correct = false;
CREATE INDEX IF NOT EXISTS idx_responses_error_type ON exercise_responses(user_id, error_type)
    WHERE error_type IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_responses_date ON exercise_responses(created_at);

-- ----------------------------------------------------------------------------
-- 2. Tabela: error_patterns (Padrões de erros recorrentes)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS error_patterns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    skill_type VARCHAR(20) NOT NULL,
    error_category VARCHAR(100) NOT NULL,
    error_description TEXT NOT NULL,
    example_errors JSONB DEFAULT '[]',
    occurrence_count INTEGER DEFAULT 1,
    first_occurred_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_occurred_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_resolved BOOLEAN DEFAULT false,
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolution_notes TEXT,
    priority INTEGER DEFAULT 5,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_errors_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_errors_language FOREIGN KEY (language_code)
        REFERENCES languages(code) ON DELETE CASCADE,
    CONSTRAINT uq_error_pattern UNIQUE (user_id, language_code, error_category),
    CONSTRAINT chk_errors_skill_type CHECK (skill_type IN (
        'listening', 'speaking', 'reading', 'writing',
        'grammar', 'vocabulary', 'pronunciation'
    )),
    CONSTRAINT chk_errors_priority CHECK (priority BETWEEN 1 AND 10)
);

COMMENT ON TABLE error_patterns IS 'Padrões de erros recorrentes identificados por usuário';
COMMENT ON COLUMN error_patterns.id IS 'Identificador único do padrão';
COMMENT ON COLUMN error_patterns.user_id IS 'Usuário com o padrão de erro';
COMMENT ON COLUMN error_patterns.language_code IS 'Idioma onde o erro ocorre';
COMMENT ON COLUMN error_patterns.skill_type IS 'Habilidade afetada';
COMMENT ON COLUMN error_patterns.error_category IS 'Categoria do erro (ex: verb_conjugation, article_usage)';
COMMENT ON COLUMN error_patterns.error_description IS 'Descrição legível do erro';
COMMENT ON COLUMN error_patterns.example_errors IS 'Exemplos de erros em JSON';
COMMENT ON COLUMN error_patterns.occurrence_count IS 'Quantas vezes o erro ocorreu';
COMMENT ON COLUMN error_patterns.first_occurred_at IS 'Primeira ocorrência';
COMMENT ON COLUMN error_patterns.last_occurred_at IS 'Última ocorrência';
COMMENT ON COLUMN error_patterns.is_resolved IS 'Se o padrão foi resolvido (5+ acertos consecutivos)';
COMMENT ON COLUMN error_patterns.resolved_at IS 'Quando foi resolvido';
COMMENT ON COLUMN error_patterns.resolution_notes IS 'Notas sobre como foi resolvido';
COMMENT ON COLUMN error_patterns.priority IS 'Prioridade de correção (1=alta, 10=baixa)';

-- Índices para error_patterns
CREATE INDEX IF NOT EXISTS idx_errors_user_lang ON error_patterns(user_id, language_code);
CREATE INDEX IF NOT EXISTS idx_errors_count ON error_patterns(user_id, occurrence_count DESC);
CREATE INDEX IF NOT EXISTS idx_errors_unresolved ON error_patterns(user_id, is_resolved)
    WHERE is_resolved = false;
CREATE INDEX IF NOT EXISTS idx_errors_priority ON error_patterns(user_id, priority, occurrence_count DESC);

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_errors_updated_at ON error_patterns;
CREATE TRIGGER trg_errors_updated_at
    BEFORE UPDATE ON error_patterns
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- 3. Função: Atualizar padrão de erro após resposta incorreta
-- ----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION update_error_pattern_on_response()
RETURNS TRIGGER AS $$
BEGIN
    -- Apenas processar respostas incorretas com tipo de erro
    IF NEW.is_correct = false AND NEW.error_type IS NOT NULL THEN
        INSERT INTO error_patterns (
            user_id,
            language_code,
            skill_type,
            error_category,
            error_description,
            example_errors,
            occurrence_count,
            last_occurred_at
        )
        VALUES (
            NEW.user_id,
            NEW.language_code,
            NEW.skill_type,
            NEW.error_type,
            COALESCE(NEW.error_details->>'description', NEW.error_type),
            jsonb_build_array(jsonb_build_object(
                'response', NEW.user_response,
                'correct', NEW.correct_response,
                'date', NEW.created_at
            )),
            1,
            NEW.created_at
        )
        ON CONFLICT (user_id, language_code, error_category)
        DO UPDATE SET
            occurrence_count = error_patterns.occurrence_count + 1,
            last_occurred_at = NEW.created_at,
            example_errors = (
                SELECT jsonb_agg(elem)
                FROM (
                    SELECT elem
                    FROM jsonb_array_elements(error_patterns.example_errors) AS elem
                    UNION ALL
                    SELECT jsonb_build_object(
                        'response', NEW.user_response,
                        'correct', NEW.correct_response,
                        'date', NEW.created_at
                    )
                    LIMIT 10  -- Manter apenas últimos 10 exemplos
                ) AS subq
            ),
            is_resolved = false,
            resolved_at = NULL,
            updated_at = NOW();
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_update_error_pattern ON exercise_responses;
CREATE TRIGGER trg_update_error_pattern
    AFTER INSERT ON exercise_responses
    FOR EACH ROW
    WHEN (NEW.is_correct = false AND NEW.error_type IS NOT NULL)
    EXECUTE FUNCTION update_error_pattern_on_response();

-- ----------------------------------------------------------------------------
-- 4. View: Top 5 erros por usuário/idioma
-- ----------------------------------------------------------------------------

CREATE OR REPLACE VIEW top_errors_by_user AS
SELECT
    user_id,
    language_code,
    skill_type,
    error_category,
    error_description,
    occurrence_count,
    first_occurred_at,
    last_occurred_at,
    ROW_NUMBER() OVER (
        PARTITION BY user_id, language_code
        ORDER BY occurrence_count DESC, last_occurred_at DESC
    ) AS rank
FROM error_patterns
WHERE is_resolved = false;

COMMENT ON VIEW top_errors_by_user IS 'Ranking de erros mais frequentes por usuário e idioma';

-- ============================================================================
-- ROLLBACK SCRIPT
-- ============================================================================
--
-- Para reverter esta migration:
--
-- DROP VIEW IF EXISTS top_errors_by_user;
-- DROP TRIGGER IF EXISTS trg_update_error_pattern ON exercise_responses;
-- DROP FUNCTION IF EXISTS update_error_pattern_on_response;
-- DROP TRIGGER IF EXISTS trg_errors_updated_at ON error_patterns;
-- DROP TABLE IF EXISTS error_patterns CASCADE;
-- DROP TABLE IF EXISTS exercise_responses CASCADE;
--
-- ============================================================================
