-- ============================================================================
-- Migration V030: Sistema de Streaks e Atividade Diária
-- ============================================================================
-- Rastreia consistência diária de estudo (streaks) e log de atividades
-- Essencial para "Consistência diária" e gamificação
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Tabela: daily_streaks (Sequência de dias de estudo)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS daily_streaks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    current_streak INTEGER DEFAULT 0,
    longest_streak INTEGER DEFAULT 0,
    last_study_date DATE,
    streak_started_at DATE,
    streak_frozen_until DATE,
    freeze_count_used INTEGER DEFAULT 0,
    total_study_days INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_streaks_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_streaks_language FOREIGN KEY (language_code)
        REFERENCES languages(code) ON DELETE CASCADE,
    CONSTRAINT uq_streaks UNIQUE (user_id, language_code),
    CONSTRAINT chk_streaks_positive CHECK (current_streak >= 0 AND longest_streak >= 0)
);

COMMENT ON TABLE daily_streaks IS 'Rastreia sequência de dias consecutivos de estudo (streaks)';
COMMENT ON COLUMN daily_streaks.id IS 'Identificador único';
COMMENT ON COLUMN daily_streaks.user_id IS 'Usuário';
COMMENT ON COLUMN daily_streaks.language_code IS 'Idioma sendo estudado';
COMMENT ON COLUMN daily_streaks.current_streak IS 'Streak atual (dias consecutivos)';
COMMENT ON COLUMN daily_streaks.longest_streak IS 'Maior streak já alcançado';
COMMENT ON COLUMN daily_streaks.last_study_date IS 'Último dia de estudo';
COMMENT ON COLUMN daily_streaks.streak_started_at IS 'Quando o streak atual começou';
COMMENT ON COLUMN daily_streaks.streak_frozen_until IS 'Data até quando o streak está congelado';
COMMENT ON COLUMN daily_streaks.freeze_count_used IS 'Quantos freezes já usou este mês';
COMMENT ON COLUMN daily_streaks.total_study_days IS 'Total de dias estudados (acumulado)';

-- Índices para daily_streaks
CREATE INDEX IF NOT EXISTS idx_streaks_user ON daily_streaks(user_id);
CREATE INDEX IF NOT EXISTS idx_streaks_user_lang ON daily_streaks(user_id, language_code);
CREATE INDEX IF NOT EXISTS idx_streaks_current ON daily_streaks(current_streak DESC);
CREATE INDEX IF NOT EXISTS idx_streaks_last_study ON daily_streaks(last_study_date);

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_streaks_updated_at ON daily_streaks;
CREATE TRIGGER trg_streaks_updated_at
    BEFORE UPDATE ON daily_streaks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- 2. Tabela: daily_activity_log (Log diário de atividades)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS daily_activity_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    activity_date DATE NOT NULL,
    lessons_started INTEGER DEFAULT 0,
    lessons_completed INTEGER DEFAULT 0,
    exercises_completed INTEGER DEFAULT 0,
    cards_reviewed INTEGER DEFAULT 0,
    chunks_practiced INTEGER DEFAULT 0,
    minutes_studied INTEGER DEFAULT 0,
    xp_earned INTEGER DEFAULT 0,
    skills_practiced JSONB DEFAULT '[]',
    achievements_unlocked JSONB DEFAULT '[]',
    session_count INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_activity_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_activity_language FOREIGN KEY (language_code)
        REFERENCES languages(code) ON DELETE CASCADE,
    CONSTRAINT uq_activity UNIQUE (user_id, language_code, activity_date)
);

COMMENT ON TABLE daily_activity_log IS 'Log consolidado de atividades diárias por usuário/idioma';
COMMENT ON COLUMN daily_activity_log.id IS 'Identificador único';
COMMENT ON COLUMN daily_activity_log.user_id IS 'Usuário';
COMMENT ON COLUMN daily_activity_log.language_code IS 'Idioma';
COMMENT ON COLUMN daily_activity_log.activity_date IS 'Data da atividade';
COMMENT ON COLUMN daily_activity_log.lessons_started IS 'Lições iniciadas';
COMMENT ON COLUMN daily_activity_log.lessons_completed IS 'Lições completadas';
COMMENT ON COLUMN daily_activity_log.exercises_completed IS 'Exercícios completados';
COMMENT ON COLUMN daily_activity_log.cards_reviewed IS 'Cards SRS revisados';
COMMENT ON COLUMN daily_activity_log.chunks_practiced IS 'Chunks praticados';
COMMENT ON COLUMN daily_activity_log.minutes_studied IS 'Minutos estudados';
COMMENT ON COLUMN daily_activity_log.xp_earned IS 'XP ganho no dia';
COMMENT ON COLUMN daily_activity_log.skills_practiced IS 'Habilidades praticadas (JSON array)';
COMMENT ON COLUMN daily_activity_log.achievements_unlocked IS 'Conquistas desbloqueadas (JSON array)';
COMMENT ON COLUMN daily_activity_log.session_count IS 'Número de sessões de estudo';

-- Índices para daily_activity_log
CREATE INDEX IF NOT EXISTS idx_activity_user_date ON daily_activity_log(user_id, activity_date DESC);
CREATE INDEX IF NOT EXISTS idx_activity_user_lang ON daily_activity_log(user_id, language_code);
CREATE INDEX IF NOT EXISTS idx_activity_date ON daily_activity_log(activity_date DESC);

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_activity_updated_at ON daily_activity_log;
CREATE TRIGGER trg_activity_updated_at
    BEFORE UPDATE ON daily_activity_log
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- 3. Função: Atualizar streak após atividade
-- ----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION update_streak_on_activity()
RETURNS TRIGGER AS $$
DECLARE
    v_last_date DATE;
    v_current_streak INTEGER;
    v_longest_streak INTEGER;
    v_streak_start DATE;
    v_frozen_until DATE;
BEGIN
    -- Buscar estado atual do streak
    SELECT
        last_study_date,
        current_streak,
        longest_streak,
        streak_started_at,
        streak_frozen_until
    INTO v_last_date, v_current_streak, v_longest_streak, v_streak_start, v_frozen_until
    FROM daily_streaks
    WHERE user_id = NEW.user_id AND language_code = NEW.language_code;

    -- Se não existe registro, criar
    IF NOT FOUND THEN
        INSERT INTO daily_streaks (
            user_id,
            language_code,
            current_streak,
            longest_streak,
            last_study_date,
            streak_started_at,
            total_study_days
        )
        VALUES (
            NEW.user_id,
            NEW.language_code,
            1,
            1,
            NEW.activity_date,
            NEW.activity_date,
            1
        );
        RETURN NEW;
    END IF;

    -- Se já estudou hoje, apenas atualizar
    IF v_last_date = NEW.activity_date THEN
        RETURN NEW;
    END IF;

    -- Verificar se streak está congelado
    IF v_frozen_until IS NOT NULL AND NEW.activity_date <= v_frozen_until THEN
        -- Streak protegido, apenas atualizar última data
        UPDATE daily_streaks
        SET
            last_study_date = NEW.activity_date,
            total_study_days = total_study_days + 1,
            streak_frozen_until = NULL  -- Remover freeze após usar
        WHERE user_id = NEW.user_id AND language_code = NEW.language_code;
        RETURN NEW;
    END IF;

    -- Verificar continuidade do streak
    IF v_last_date = NEW.activity_date - INTERVAL '1 day' THEN
        -- Dia consecutivo: incrementar streak
        v_current_streak := v_current_streak + 1;
        IF v_current_streak > v_longest_streak THEN
            v_longest_streak := v_current_streak;
        END IF;

        UPDATE daily_streaks
        SET
            current_streak = v_current_streak,
            longest_streak = v_longest_streak,
            last_study_date = NEW.activity_date,
            total_study_days = total_study_days + 1
        WHERE user_id = NEW.user_id AND language_code = NEW.language_code;
    ELSE
        -- Streak quebrado: reiniciar
        UPDATE daily_streaks
        SET
            current_streak = 1,
            last_study_date = NEW.activity_date,
            streak_started_at = NEW.activity_date,
            total_study_days = total_study_days + 1
        WHERE user_id = NEW.user_id AND language_code = NEW.language_code;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_update_streak ON daily_activity_log;
CREATE TRIGGER trg_update_streak
    AFTER INSERT ON daily_activity_log
    FOR EACH ROW
    EXECUTE FUNCTION update_streak_on_activity();

-- ----------------------------------------------------------------------------
-- 4. Função: Verificar e quebrar streaks inativos (rodar diariamente)
-- ----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION check_inactive_streaks()
RETURNS INTEGER AS $$
DECLARE
    v_count INTEGER;
BEGIN
    -- Quebrar streaks de quem não estudou ontem (e não tem freeze)
    UPDATE daily_streaks
    SET
        current_streak = 0,
        streak_started_at = NULL
    WHERE last_study_date < CURRENT_DATE - INTERVAL '1 day'
      AND current_streak > 0
      AND (streak_frozen_until IS NULL OR streak_frozen_until < CURRENT_DATE);

    GET DIAGNOSTICS v_count = ROW_COUNT;
    RETURN v_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION check_inactive_streaks IS 'Função para rodar diariamente via cron job para quebrar streaks inativos';

-- ----------------------------------------------------------------------------
-- 5. View: Leaderboard de streaks
-- ----------------------------------------------------------------------------

CREATE OR REPLACE VIEW streak_leaderboard AS
SELECT
    ds.user_id,
    u.name AS user_name,
    ds.language_code,
    l.name_pt AS language_name,
    ds.current_streak,
    ds.longest_streak,
    ds.total_study_days,
    ds.last_study_date,
    ROW_NUMBER() OVER (
        PARTITION BY ds.language_code
        ORDER BY ds.current_streak DESC, ds.total_study_days DESC
    ) AS rank
FROM daily_streaks ds
JOIN users u ON ds.user_id = u.id
JOIN languages l ON ds.language_code = l.code
WHERE ds.current_streak > 0
ORDER BY ds.language_code, ds.current_streak DESC;

COMMENT ON VIEW streak_leaderboard IS 'Ranking de streaks por idioma';

-- ----------------------------------------------------------------------------
-- 6. View: Resumo semanal de atividade
-- ----------------------------------------------------------------------------

CREATE OR REPLACE VIEW weekly_activity_summary AS
SELECT
    user_id,
    language_code,
    DATE_TRUNC('week', activity_date) AS week_start,
    COUNT(DISTINCT activity_date) AS days_active,
    SUM(lessons_completed) AS total_lessons,
    SUM(exercises_completed) AS total_exercises,
    SUM(cards_reviewed) AS total_cards_reviewed,
    SUM(minutes_studied) AS total_minutes,
    SUM(xp_earned) AS total_xp
FROM daily_activity_log
WHERE activity_date >= CURRENT_DATE - INTERVAL '4 weeks'
GROUP BY user_id, language_code, DATE_TRUNC('week', activity_date)
ORDER BY week_start DESC;

COMMENT ON VIEW weekly_activity_summary IS 'Resumo semanal de atividade por usuário/idioma';

-- ============================================================================
-- ROLLBACK SCRIPT
-- ============================================================================
--
-- Para reverter esta migration:
--
-- DROP VIEW IF EXISTS weekly_activity_summary;
-- DROP VIEW IF EXISTS streak_leaderboard;
-- DROP FUNCTION IF EXISTS check_inactive_streaks;
-- DROP TRIGGER IF EXISTS trg_update_streak ON daily_activity_log;
-- DROP FUNCTION IF EXISTS update_streak_on_activity;
-- DROP TRIGGER IF EXISTS trg_activity_updated_at ON daily_activity_log;
-- DROP TRIGGER IF EXISTS trg_streaks_updated_at ON daily_streaks;
-- DROP TABLE IF EXISTS daily_activity_log CASCADE;
-- DROP TABLE IF EXISTS daily_streaks CASCADE;
--
-- ============================================================================
