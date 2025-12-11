-- ============================================================================
-- Migration V029: Métricas de Habilidades por Dia
-- ============================================================================
-- Rastreia métricas diárias de desempenho por habilidade (listening, speaking, etc.)
-- Essencial para "Tempo médio de resposta" e "Precisão no listening"
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Tabela: skill_metrics (Métricas diárias por habilidade)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS skill_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    skill_type VARCHAR(20) NOT NULL,
    metric_date DATE NOT NULL,
    exercises_completed INTEGER DEFAULT 0,
    correct_answers INTEGER DEFAULT 0,
    accuracy_percentage DECIMAL(5,2),
    avg_response_time_ms INTEGER,
    min_response_time_ms INTEGER,
    max_response_time_ms INTEGER,
    total_practice_time_minutes INTEGER DEFAULT 0,
    xp_earned INTEGER DEFAULT 0,
    difficulty_avg DECIMAL(3,2),
    improvement_score DECIMAL(5,2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_metrics_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_metrics_language FOREIGN KEY (language_code)
        REFERENCES languages(code) ON DELETE CASCADE,
    CONSTRAINT uq_skill_metrics UNIQUE (user_id, language_code, skill_type, metric_date),
    CONSTRAINT chk_metrics_skill_type CHECK (skill_type IN (
        'listening', 'speaking', 'reading', 'writing',
        'grammar', 'vocabulary', 'pronunciation'
    )),
    CONSTRAINT chk_metrics_accuracy CHECK (accuracy_percentage IS NULL OR (accuracy_percentage >= 0 AND accuracy_percentage <= 100))
);

COMMENT ON TABLE skill_metrics IS 'Métricas consolidadas de desempenho por habilidade por dia';
COMMENT ON COLUMN skill_metrics.id IS 'Identificador único';
COMMENT ON COLUMN skill_metrics.user_id IS 'Usuário';
COMMENT ON COLUMN skill_metrics.language_code IS 'Idioma';
COMMENT ON COLUMN skill_metrics.skill_type IS 'Habilidade: listening, speaking, reading, writing, grammar, vocabulary, pronunciation';
COMMENT ON COLUMN skill_metrics.metric_date IS 'Data das métricas';
COMMENT ON COLUMN skill_metrics.exercises_completed IS 'Exercícios completados';
COMMENT ON COLUMN skill_metrics.correct_answers IS 'Respostas corretas';
COMMENT ON COLUMN skill_metrics.accuracy_percentage IS 'Percentual de acerto (0-100)';
COMMENT ON COLUMN skill_metrics.avg_response_time_ms IS 'Tempo médio de resposta em ms';
COMMENT ON COLUMN skill_metrics.min_response_time_ms IS 'Menor tempo de resposta';
COMMENT ON COLUMN skill_metrics.max_response_time_ms IS 'Maior tempo de resposta';
COMMENT ON COLUMN skill_metrics.total_practice_time_minutes IS 'Tempo total praticando em minutos';
COMMENT ON COLUMN skill_metrics.xp_earned IS 'XP ganho nesta habilidade no dia';
COMMENT ON COLUMN skill_metrics.difficulty_avg IS 'Dificuldade média dos exercícios (0-1)';
COMMENT ON COLUMN skill_metrics.improvement_score IS 'Score de melhoria comparado ao dia anterior';

-- Índices para skill_metrics
CREATE INDEX IF NOT EXISTS idx_metrics_user_date ON skill_metrics(user_id, metric_date DESC);
CREATE INDEX IF NOT EXISTS idx_metrics_user_skill ON skill_metrics(user_id, skill_type);
CREATE INDEX IF NOT EXISTS idx_metrics_user_lang_date ON skill_metrics(user_id, language_code, metric_date DESC);
CREATE INDEX IF NOT EXISTS idx_metrics_accuracy ON skill_metrics(user_id, skill_type, accuracy_percentage);

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_metrics_updated_at ON skill_metrics;
CREATE TRIGGER trg_metrics_updated_at
    BEFORE UPDATE ON skill_metrics
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- 2. Função: Atualizar métricas após resposta de exercício
-- ----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION update_skill_metrics_on_response()
RETURNS TRIGGER AS $$
DECLARE
    v_date DATE;
    v_accuracy DECIMAL(5,2);
    v_exercises INTEGER;
    v_correct INTEGER;
BEGIN
    v_date := DATE(NEW.created_at);

    -- Inserir ou atualizar métricas do dia
    INSERT INTO skill_metrics (
        user_id,
        language_code,
        skill_type,
        metric_date,
        exercises_completed,
        correct_answers,
        avg_response_time_ms,
        min_response_time_ms,
        max_response_time_ms
    )
    VALUES (
        NEW.user_id,
        NEW.language_code,
        NEW.skill_type,
        v_date,
        1,
        CASE WHEN NEW.is_correct THEN 1 ELSE 0 END,
        NEW.response_time_ms,
        NEW.response_time_ms,
        NEW.response_time_ms
    )
    ON CONFLICT (user_id, language_code, skill_type, metric_date)
    DO UPDATE SET
        exercises_completed = skill_metrics.exercises_completed + 1,
        correct_answers = skill_metrics.correct_answers + CASE WHEN NEW.is_correct THEN 1 ELSE 0 END,
        avg_response_time_ms = (
            (skill_metrics.avg_response_time_ms * skill_metrics.exercises_completed + COALESCE(NEW.response_time_ms, 0))
            / (skill_metrics.exercises_completed + 1)
        ),
        min_response_time_ms = LEAST(skill_metrics.min_response_time_ms, NEW.response_time_ms),
        max_response_time_ms = GREATEST(skill_metrics.max_response_time_ms, NEW.response_time_ms),
        updated_at = NOW();

    -- Atualizar accuracy_percentage
    SELECT exercises_completed, correct_answers
    INTO v_exercises, v_correct
    FROM skill_metrics
    WHERE user_id = NEW.user_id
      AND language_code = NEW.language_code
      AND skill_type = NEW.skill_type
      AND metric_date = v_date;

    IF v_exercises > 0 THEN
        v_accuracy := (v_correct::DECIMAL / v_exercises) * 100;

        UPDATE skill_metrics
        SET accuracy_percentage = v_accuracy
        WHERE user_id = NEW.user_id
          AND language_code = NEW.language_code
          AND skill_type = NEW.skill_type
          AND metric_date = v_date;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_update_skill_metrics ON exercise_responses;
CREATE TRIGGER trg_update_skill_metrics
    AFTER INSERT ON exercise_responses
    FOR EACH ROW
    EXECUTE FUNCTION update_skill_metrics_on_response();

-- ----------------------------------------------------------------------------
-- 3. View: Resumo semanal de métricas por habilidade
-- ----------------------------------------------------------------------------

CREATE OR REPLACE VIEW weekly_skill_summary AS
SELECT
    user_id,
    language_code,
    skill_type,
    DATE_TRUNC('week', metric_date) AS week_start,
    SUM(exercises_completed) AS total_exercises,
    SUM(correct_answers) AS total_correct,
    ROUND(AVG(accuracy_percentage), 2) AS avg_accuracy,
    ROUND(AVG(avg_response_time_ms), 0) AS avg_response_time,
    SUM(total_practice_time_minutes) AS total_practice_minutes,
    SUM(xp_earned) AS total_xp
FROM skill_metrics
WHERE metric_date >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY user_id, language_code, skill_type, DATE_TRUNC('week', metric_date)
ORDER BY week_start DESC;

COMMENT ON VIEW weekly_skill_summary IS 'Resumo semanal de métricas por habilidade';

-- ----------------------------------------------------------------------------
-- 4. View: Progresso de precisão por habilidade (últimos 7 dias)
-- ----------------------------------------------------------------------------

CREATE OR REPLACE VIEW skill_accuracy_trend AS
SELECT
    user_id,
    language_code,
    skill_type,
    metric_date,
    accuracy_percentage,
    LAG(accuracy_percentage) OVER (
        PARTITION BY user_id, language_code, skill_type
        ORDER BY metric_date
    ) AS previous_accuracy,
    accuracy_percentage - LAG(accuracy_percentage) OVER (
        PARTITION BY user_id, language_code, skill_type
        ORDER BY metric_date
    ) AS accuracy_change
FROM skill_metrics
WHERE metric_date >= CURRENT_DATE - INTERVAL '7 days'
ORDER BY user_id, language_code, skill_type, metric_date;

COMMENT ON VIEW skill_accuracy_trend IS 'Tendência de precisão por habilidade nos últimos 7 dias';

-- ============================================================================
-- ROLLBACK SCRIPT
-- ============================================================================
--
-- Para reverter esta migration:
--
-- DROP VIEW IF EXISTS skill_accuracy_trend;
-- DROP VIEW IF EXISTS weekly_skill_summary;
-- DROP TRIGGER IF EXISTS trg_update_skill_metrics ON exercise_responses;
-- DROP FUNCTION IF EXISTS update_skill_metrics_on_response;
-- DROP TRIGGER IF EXISTS trg_metrics_updated_at ON skill_metrics;
-- DROP TABLE IF EXISTS skill_metrics CASCADE;
--
-- ============================================================================
