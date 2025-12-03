-- ============================================================================
-- Migration V021: Tabelas Core para Trilhas de Aprendizado
-- ============================================================================
-- Esta migration cria as tabelas principais do módulo de trilhas:
-- trails, modules, lessons e trail_progress
--
-- Depende de: V015 (ENUMs), V016 (estrutura curricular), V017 (content_blocks)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Tabela: trails (Trilhas de aprendizado do estudante)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS trails (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    level_id UUID NOT NULL,
    blueprint_id UUID,
    status trail_status NOT NULL DEFAULT 'GENERATING',
    content_hash VARCHAR(40) NOT NULL,
    curriculum_version VARCHAR(20) NOT NULL,
    estimated_duration_hours DECIMAL(5,1),
    previous_trail_id UUID,
    refresh_reason refresh_reason,
    archived_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_trails_student FOREIGN KEY (student_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_trails_language FOREIGN KEY (language_code)
        REFERENCES languages(code) ON DELETE RESTRICT,
    CONSTRAINT fk_trails_level FOREIGN KEY (level_id)
        REFERENCES levels(id) ON DELETE RESTRICT,
    CONSTRAINT fk_trails_blueprint FOREIGN KEY (blueprint_id)
        REFERENCES blueprints(id) ON DELETE SET NULL,
    CONSTRAINT fk_trails_previous FOREIGN KEY (previous_trail_id)
        REFERENCES trails(id) ON DELETE SET NULL
);

-- Índice único parcial: apenas uma trilha ativa por estudante/idioma
CREATE UNIQUE INDEX IF NOT EXISTS idx_trails_student_language_active
    ON trails(student_id, language_code)
    WHERE status != 'ARCHIVED';

-- Outros índices
CREATE INDEX IF NOT EXISTS idx_trails_student ON trails(student_id);
CREATE INDEX IF NOT EXISTS idx_trails_language ON trails(language_code);
CREATE INDEX IF NOT EXISTS idx_trails_level ON trails(level_id);
CREATE INDEX IF NOT EXISTS idx_trails_status ON trails(status);
CREATE INDEX IF NOT EXISTS idx_trails_hash ON trails(content_hash);
CREATE INDEX IF NOT EXISTS idx_trails_blueprint ON trails(blueprint_id);

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_trails_updated_at ON trails;
CREATE TRIGGER trg_trails_updated_at
    BEFORE UPDATE ON trails
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE trails IS 'Trilhas de aprendizado personalizadas por estudante/idioma. Geradas on-demand via IA.';
COMMENT ON COLUMN trails.id IS 'Identificador único da trilha';
COMMENT ON COLUMN trails.student_id IS 'Estudante dono da trilha';
COMMENT ON COLUMN trails.language_code IS 'Código do idioma da trilha';
COMMENT ON COLUMN trails.level_id IS 'Nível CEFR da trilha';
COMMENT ON COLUMN trails.blueprint_id IS 'Blueprint usado como base (NULL se gerada do zero)';
COMMENT ON COLUMN trails.status IS 'Status: GENERATING, PARTIAL, READY, ARCHIVED';
COMMENT ON COLUMN trails.content_hash IS 'Hash SHA-1 para cache: sha1(lang|level|prefs|version)';
COMMENT ON COLUMN trails.curriculum_version IS 'Versão do currículo usada na geração';
COMMENT ON COLUMN trails.estimated_duration_hours IS 'Duração total estimada em horas';
COMMENT ON COLUMN trails.previous_trail_id IS 'Trilha anterior (em caso de refresh)';
COMMENT ON COLUMN trails.refresh_reason IS 'Motivo do refresh, se aplicável';
COMMENT ON COLUMN trails.archived_at IS 'Data de arquivamento';

-- ----------------------------------------------------------------------------
-- 2. Tabela: modules (Módulos de uma trilha)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS modules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trail_id UUID NOT NULL,
    competency_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    order_index INTEGER NOT NULL,
    status module_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_modules_trail FOREIGN KEY (trail_id)
        REFERENCES trails(id) ON DELETE CASCADE,
    CONSTRAINT fk_modules_competency FOREIGN KEY (competency_id)
        REFERENCES competencies(id) ON DELETE RESTRICT,
    CONSTRAINT uq_modules_order UNIQUE (trail_id, order_index),
    CONSTRAINT chk_modules_order CHECK (order_index >= 0)
);

-- Índices para modules
CREATE INDEX IF NOT EXISTS idx_modules_trail ON modules(trail_id);
CREATE INDEX IF NOT EXISTS idx_modules_competency ON modules(competency_id);
CREATE INDEX IF NOT EXISTS idx_modules_status ON modules(status);

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_modules_updated_at ON modules;
CREATE TRIGGER trg_modules_updated_at
    BEFORE UPDATE ON modules
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE modules IS 'Módulos de uma trilha, cada um focado em uma competência linguística.';
COMMENT ON COLUMN modules.id IS 'Identificador único do módulo';
COMMENT ON COLUMN modules.trail_id IS 'Trilha à qual o módulo pertence';
COMMENT ON COLUMN modules.competency_id IS 'Competência linguística trabalhada no módulo';
COMMENT ON COLUMN modules.title IS 'Título do módulo';
COMMENT ON COLUMN modules.description IS 'Descrição do módulo';
COMMENT ON COLUMN modules.order_index IS 'Ordem de exibição na trilha (0-based)';
COMMENT ON COLUMN modules.status IS 'Status: PENDING (aguardando geração), READY (pronto)';

-- ----------------------------------------------------------------------------
-- 3. Tabela: lessons (Lições de um módulo)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS lessons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    module_id UUID NOT NULL,
    content_block_id UUID,
    descriptor_id UUID,
    title VARCHAR(255) NOT NULL,
    type lesson_type NOT NULL,
    order_index INTEGER NOT NULL,
    duration_minutes INTEGER NOT NULL DEFAULT 15,
    content JSONB NOT NULL DEFAULT '{}',
    is_placeholder BOOLEAN NOT NULL DEFAULT false,
    completed_at TIMESTAMP WITH TIME ZONE,
    score DECIMAL(5,2),
    time_spent_seconds INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_lessons_module FOREIGN KEY (module_id)
        REFERENCES modules(id) ON DELETE CASCADE,
    CONSTRAINT fk_lessons_content_block FOREIGN KEY (content_block_id)
        REFERENCES content_blocks(id) ON DELETE SET NULL,
    CONSTRAINT fk_lessons_descriptor FOREIGN KEY (descriptor_id)
        REFERENCES descriptors(id) ON DELETE SET NULL,
    CONSTRAINT uq_lessons_order UNIQUE (module_id, order_index),
    CONSTRAINT chk_lessons_order CHECK (order_index >= 0),
    CONSTRAINT chk_lessons_score CHECK (score IS NULL OR (score >= 0 AND score <= 100)),
    CONSTRAINT chk_lessons_duration CHECK (duration_minutes > 0)
);

-- Índices para lessons
CREATE INDEX IF NOT EXISTS idx_lessons_module ON lessons(module_id);
CREATE INDEX IF NOT EXISTS idx_lessons_content_block ON lessons(content_block_id);
CREATE INDEX IF NOT EXISTS idx_lessons_descriptor ON lessons(descriptor_id);
CREATE INDEX IF NOT EXISTS idx_lessons_type ON lessons(type);
CREATE INDEX IF NOT EXISTS idx_lessons_completed ON lessons(completed_at) WHERE completed_at IS NOT NULL;

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_lessons_updated_at ON lessons;
CREATE TRIGGER trg_lessons_updated_at
    BEFORE UPDATE ON lessons
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE lessons IS 'Lições dentro de um módulo. Podem referenciar content_blocks ou ter conteúdo inline.';
COMMENT ON COLUMN lessons.id IS 'Identificador único da lição';
COMMENT ON COLUMN lessons.module_id IS 'Módulo ao qual a lição pertence';
COMMENT ON COLUMN lessons.content_block_id IS 'Bloco de conteúdo reutilizável (se aplicável)';
COMMENT ON COLUMN lessons.descriptor_id IS 'Descritor de aprendizagem associado';
COMMENT ON COLUMN lessons.title IS 'Título da lição';
COMMENT ON COLUMN lessons.type IS 'Tipo: interactive, video, reading, exercise, conversation, flashcard, game';
COMMENT ON COLUMN lessons.order_index IS 'Ordem de exibição no módulo (0-based)';
COMMENT ON COLUMN lessons.duration_minutes IS 'Duração estimada em minutos';
COMMENT ON COLUMN lessons.content IS 'Conteúdo da lição em JSONB (estrutura varia por tipo)';
COMMENT ON COLUMN lessons.is_placeholder IS 'Se true, conteúdo ainda precisa ser gerado';
COMMENT ON COLUMN lessons.completed_at IS 'Data/hora de conclusão pelo estudante';
COMMENT ON COLUMN lessons.score IS 'Pontuação obtida (0-100)';
COMMENT ON COLUMN lessons.time_spent_seconds IS 'Tempo gasto na lição em segundos';

-- ----------------------------------------------------------------------------
-- 4. Tabela: trail_progress (Progresso consolidado da trilha)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS trail_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trail_id UUID NOT NULL UNIQUE,
    total_lessons INTEGER NOT NULL DEFAULT 0,
    lessons_completed INTEGER NOT NULL DEFAULT 0,
    progress_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    average_score DECIMAL(5,2),
    time_spent_minutes INTEGER NOT NULL DEFAULT 0,
    last_activity_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_trail_progress_trail FOREIGN KEY (trail_id)
        REFERENCES trails(id) ON DELETE CASCADE,
    CONSTRAINT chk_progress_percentage CHECK (progress_percentage >= 0 AND progress_percentage <= 100),
    CONSTRAINT chk_progress_score CHECK (average_score IS NULL OR (average_score >= 0 AND average_score <= 100)),
    CONSTRAINT chk_progress_lessons CHECK (lessons_completed <= total_lessons)
);

-- Índices para trail_progress
CREATE INDEX IF NOT EXISTS idx_trail_progress_trail ON trail_progress(trail_id);
CREATE INDEX IF NOT EXISTS idx_trail_progress_activity ON trail_progress(last_activity_at);

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_trail_progress_updated_at ON trail_progress;
CREATE TRIGGER trg_trail_progress_updated_at
    BEFORE UPDATE ON trail_progress
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE trail_progress IS 'Progresso consolidado do estudante em cada trilha.';
COMMENT ON COLUMN trail_progress.id IS 'Identificador único do registro de progresso';
COMMENT ON COLUMN trail_progress.trail_id IS 'Trilha associada';
COMMENT ON COLUMN trail_progress.total_lessons IS 'Total de lições na trilha';
COMMENT ON COLUMN trail_progress.lessons_completed IS 'Lições completadas';
COMMENT ON COLUMN trail_progress.progress_percentage IS 'Percentual de conclusão (0-100)';
COMMENT ON COLUMN trail_progress.average_score IS 'Média de pontuação nas lições (0-100)';
COMMENT ON COLUMN trail_progress.time_spent_minutes IS 'Tempo total gasto em minutos';
COMMENT ON COLUMN trail_progress.last_activity_at IS 'Data/hora da última atividade';

-- ----------------------------------------------------------------------------
-- 5. Função: Verificar limite de trilhas ativas (máximo 3)
-- ----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION check_trail_limit()
RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT COUNT(*) FROM trails
        WHERE student_id = NEW.student_id
        AND status != 'ARCHIVED') >= 3 THEN
        RAISE EXCEPTION 'Limite de 3 trilhas ativas por estudante atingido';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_check_trail_limit ON trails;
CREATE TRIGGER trg_check_trail_limit
    BEFORE INSERT ON trails
    FOR EACH ROW
    EXECUTE FUNCTION check_trail_limit();

-- ----------------------------------------------------------------------------
-- 6. Função: Criar registro de progresso ao inserir trilha
-- ----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION create_trail_progress()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO trail_progress (trail_id)
    VALUES (NEW.id)
    ON CONFLICT (trail_id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_create_trail_progress ON trails;
CREATE TRIGGER trg_create_trail_progress
    AFTER INSERT ON trails
    FOR EACH ROW
    EXECUTE FUNCTION create_trail_progress();

-- ----------------------------------------------------------------------------
-- 7. Função: Atualizar progresso ao completar lição
-- ----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION update_trail_progress_on_lesson()
RETURNS TRIGGER AS $$
DECLARE
    v_trail_id UUID;
    v_total INTEGER;
    v_completed INTEGER;
    v_avg_score DECIMAL;
    v_time_spent INTEGER;
BEGIN
    -- Obter trail_id do módulo
    SELECT m.trail_id INTO v_trail_id
    FROM modules m
    WHERE m.id = NEW.module_id;

    -- Calcular estatísticas
    SELECT
        COUNT(*),
        COUNT(*) FILTER (WHERE completed_at IS NOT NULL),
        AVG(score) FILTER (WHERE score IS NOT NULL),
        COALESCE(SUM(time_spent_seconds), 0) / 60
    INTO v_total, v_completed, v_avg_score, v_time_spent
    FROM lessons l
    JOIN modules m ON l.module_id = m.id
    WHERE m.trail_id = v_trail_id;

    -- Atualizar progresso
    UPDATE trail_progress
    SET
        total_lessons = v_total,
        lessons_completed = v_completed,
        progress_percentage = CASE
            WHEN v_total > 0 THEN (v_completed::DECIMAL / v_total) * 100
            ELSE 0
        END,
        average_score = v_avg_score,
        time_spent_minutes = v_time_spent,
        last_activity_at = NOW(),
        updated_at = NOW()
    WHERE trail_id = v_trail_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_update_trail_progress ON lessons;
CREATE TRIGGER trg_update_trail_progress
    AFTER UPDATE OF completed_at, score, time_spent_seconds ON lessons
    FOR EACH ROW
    WHEN (OLD IS DISTINCT FROM NEW)
    EXECUTE FUNCTION update_trail_progress_on_lesson();

-- ----------------------------------------------------------------------------
-- 8. Adicionar FKs pendentes em trail_generation_jobs (V018)
-- ----------------------------------------------------------------------------

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_jobs_trail') THEN
        ALTER TABLE trail_generation_jobs
            ADD CONSTRAINT fk_jobs_trail
            FOREIGN KEY (trail_id) REFERENCES trails(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_jobs_student') THEN
        ALTER TABLE trail_generation_jobs
            ADD CONSTRAINT fk_jobs_student
            FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END$$;

-- ============================================================================
-- ROLLBACK SCRIPT
-- ============================================================================
--
-- Para reverter esta migration:
--
-- -- Remover FKs adicionadas
-- ALTER TABLE trail_generation_jobs DROP CONSTRAINT IF EXISTS fk_jobs_trail;
-- ALTER TABLE trail_generation_jobs DROP CONSTRAINT IF EXISTS fk_jobs_student;
--
-- -- Remover triggers
-- DROP TRIGGER IF EXISTS trg_update_trail_progress ON lessons;
-- DROP TRIGGER IF EXISTS trg_create_trail_progress ON trails;
-- DROP TRIGGER IF EXISTS trg_check_trail_limit ON trails;
-- DROP TRIGGER IF EXISTS trg_trail_progress_updated_at ON trail_progress;
-- DROP TRIGGER IF EXISTS trg_lessons_updated_at ON lessons;
-- DROP TRIGGER IF EXISTS trg_modules_updated_at ON modules;
-- DROP TRIGGER IF EXISTS trg_trails_updated_at ON trails;
--
-- -- Remover funções
-- DROP FUNCTION IF EXISTS update_trail_progress_on_lesson();
-- DROP FUNCTION IF EXISTS create_trail_progress();
-- DROP FUNCTION IF EXISTS check_trail_limit();
--
-- -- Remover tabelas (ordem inversa de dependência)
-- DROP TABLE IF EXISTS trail_progress CASCADE;
-- DROP TABLE IF EXISTS lessons CASCADE;
-- DROP TABLE IF EXISTS modules CASCADE;
-- DROP TABLE IF EXISTS trails CASCADE;
--
-- ============================================================================
