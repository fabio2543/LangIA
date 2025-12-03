-- ============================================================================
-- Migration V017: Tabelas de Geração de Conteúdo para Módulo de Trilhas
-- ============================================================================
-- Esta camada armazena templates de prompts e blueprints reutilizáveis
-- para geração de trilhas personalizadas via LLM.
--
-- Hierarquia: PromptTemplate -> Blueprint -> ContentBlock
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Tabela: prompt_templates (Templates de prompts para LLM)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS prompt_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    competency_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    template TEXT NOT NULL,
    slots JSONB NOT NULL DEFAULT '[]',
    output_schema JSONB,
    version INTEGER NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_by UUID,
    approved_by UUID,
    approved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_prompt_templates_competency FOREIGN KEY (competency_id)
        REFERENCES competencies(id) ON DELETE RESTRICT,
    CONSTRAINT uq_prompt_templates UNIQUE (competency_id, name, version)
);

COMMENT ON TABLE prompt_templates IS 'Templates de prompts slotados para geração de conteúdo via LLM. Imutáveis após aprovação.';
COMMENT ON COLUMN prompt_templates.id IS 'Identificador único do template';
COMMENT ON COLUMN prompt_templates.competency_id IS 'Competência linguística associada';
COMMENT ON COLUMN prompt_templates.name IS 'Nome identificador do template';
COMMENT ON COLUMN prompt_templates.description IS 'Descrição do propósito do template';
COMMENT ON COLUMN prompt_templates.template IS 'Texto do prompt com slots ({{level}}, {{language}}, {{topic}}, etc.)';
COMMENT ON COLUMN prompt_templates.slots IS 'Array de slots disponíveis [{name, type, required, default}]';
COMMENT ON COLUMN prompt_templates.output_schema IS 'Schema esperado do output (JSON Schema)';
COMMENT ON COLUMN prompt_templates.version IS 'Versão do template (imutável após aprovação)';
COMMENT ON COLUMN prompt_templates.is_active IS 'Indica se o template está ativo para uso';
COMMENT ON COLUMN prompt_templates.created_by IS 'Usuário que criou o template';
COMMENT ON COLUMN prompt_templates.approved_by IS 'Usuário que aprovou o template';
COMMENT ON COLUMN prompt_templates.approved_at IS 'Data/hora da aprovação';

-- Índices para prompt_templates
CREATE INDEX IF NOT EXISTS idx_prompt_templates_competency ON prompt_templates(competency_id);
CREATE INDEX IF NOT EXISTS idx_prompt_templates_active ON prompt_templates(is_active) WHERE is_active = true;

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_prompt_templates_updated_at ON prompt_templates;
CREATE TRIGGER trg_prompt_templates_updated_at
    BEFORE UPDATE ON prompt_templates
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- 2. Tabela: blueprints (Esqueletos de trilhas reutilizáveis)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS blueprints (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    language_code VARCHAR(10) NOT NULL,
    level_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    preferences_pattern JSONB NOT NULL,
    structure JSONB NOT NULL,
    estimated_duration_hours DECIMAL(5,1),
    version INTEGER NOT NULL DEFAULT 1,
    is_approved BOOLEAN NOT NULL DEFAULT false,
    usage_count INTEGER NOT NULL DEFAULT 0,
    avg_completion_rate DECIMAL(5,2),
    created_by UUID,
    approved_by UUID,
    approved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_blueprints_language FOREIGN KEY (language_code)
        REFERENCES languages(code) ON DELETE RESTRICT,
    CONSTRAINT fk_blueprints_level FOREIGN KEY (level_id)
        REFERENCES levels(id) ON DELETE RESTRICT,
    CONSTRAINT uq_blueprints UNIQUE (language_code, level_id, name, version),
    CONSTRAINT chk_blueprints_completion_rate CHECK (avg_completion_rate IS NULL OR (avg_completion_rate >= 0 AND avg_completion_rate <= 100))
);

COMMENT ON TABLE blueprints IS 'Blueprints são esqueletos de trilhas reutilizáveis. Imutáveis após aprovação. Selecionados por similaridade de perfil.';
COMMENT ON COLUMN blueprints.id IS 'Identificador único do blueprint';
COMMENT ON COLUMN blueprints.language_code IS 'Código do idioma alvo da trilha';
COMMENT ON COLUMN blueprints.level_id IS 'Nível CEFR da trilha';
COMMENT ON COLUMN blueprints.name IS 'Nome identificador do blueprint';
COMMENT ON COLUMN blueprints.description IS 'Descrição do blueprint';
COMMENT ON COLUMN blueprints.preferences_pattern IS 'Padrão de preferências coberto: {learningStyles, topics, goals, studyTimeRange}';
COMMENT ON COLUMN blueprints.structure IS 'Estrutura da trilha: {modules: [{competency_code, title_template, lessons: [...]}]}';
COMMENT ON COLUMN blueprints.estimated_duration_hours IS 'Duração total estimada em horas';
COMMENT ON COLUMN blueprints.version IS 'Versão do blueprint (imutável após aprovação)';
COMMENT ON COLUMN blueprints.is_approved IS 'Indica se o blueprint foi aprovado para uso';
COMMENT ON COLUMN blueprints.usage_count IS 'Contador de quantas vezes o blueprint foi utilizado';
COMMENT ON COLUMN blueprints.avg_completion_rate IS 'Taxa média de conclusão das trilhas geradas (0-100%)';
COMMENT ON COLUMN blueprints.created_by IS 'Usuário que criou o blueprint';
COMMENT ON COLUMN blueprints.approved_by IS 'Usuário que aprovou o blueprint';
COMMENT ON COLUMN blueprints.approved_at IS 'Data/hora da aprovação';

-- Índices para blueprints
CREATE INDEX IF NOT EXISTS idx_blueprints_language_level ON blueprints(language_code, level_id);
CREATE INDEX IF NOT EXISTS idx_blueprints_approved ON blueprints(is_approved) WHERE is_approved = true;
CREATE INDEX IF NOT EXISTS idx_blueprints_preferences ON blueprints USING GIN(preferences_pattern);

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_blueprints_updated_at ON blueprints;
CREATE TRIGGER trg_blueprints_updated_at
    BEFORE UPDATE ON blueprints
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- 3. Tabela: content_blocks (Blocos de conteúdo reutilizáveis / Catálogo)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS content_blocks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    descriptor_id UUID NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    content_hash VARCHAR(40) NOT NULL,
    title VARCHAR(255) NOT NULL,
    type lesson_type NOT NULL,
    content JSONB NOT NULL,
    duration_minutes INTEGER NOT NULL,
    difficulty_score DECIMAL(3,2),
    quality_score DECIMAL(3,2),
    usage_count INTEGER NOT NULL DEFAULT 0,
    generation_source VARCHAR(20) NOT NULL DEFAULT 'llm',
    llm_model VARCHAR(50),
    tokens_used INTEGER,
    is_approved BOOLEAN NOT NULL DEFAULT false,
    approved_by UUID,
    approved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_content_blocks_descriptor FOREIGN KEY (descriptor_id)
        REFERENCES descriptors(id) ON DELETE RESTRICT,
    CONSTRAINT fk_content_blocks_language FOREIGN KEY (language_code)
        REFERENCES languages(code) ON DELETE RESTRICT,
    CONSTRAINT uq_content_blocks_hash UNIQUE (content_hash),
    CONSTRAINT chk_content_blocks_difficulty CHECK (difficulty_score IS NULL OR (difficulty_score >= 0.00 AND difficulty_score <= 1.00)),
    CONSTRAINT chk_content_blocks_quality CHECK (quality_score IS NULL OR (quality_score >= 0.00 AND quality_score <= 1.00)),
    CONSTRAINT chk_content_blocks_source CHECK (generation_source IN ('llm', 'human', 'imported'))
);

COMMENT ON TABLE content_blocks IS 'Catálogo de blocos de conteúdo reutilizáveis. Endereçados por hash SHA-1 para deduplicação.';
COMMENT ON COLUMN content_blocks.id IS 'Identificador único do bloco';
COMMENT ON COLUMN content_blocks.descriptor_id IS 'Descritor de aprendizagem que este bloco cobre';
COMMENT ON COLUMN content_blocks.language_code IS 'Código do idioma do conteúdo';
COMMENT ON COLUMN content_blocks.content_hash IS 'Hash SHA-1 do conteúdo para deduplicação';
COMMENT ON COLUMN content_blocks.title IS 'Título do bloco de conteúdo';
COMMENT ON COLUMN content_blocks.type IS 'Tipo da lição (interactive, video, reading, exercise, conversation, flashcard, game)';
COMMENT ON COLUMN content_blocks.content IS 'Conteúdo estruturado em JSONB (estrutura varia por tipo)';
COMMENT ON COLUMN content_blocks.duration_minutes IS 'Duração estimada em minutos';
COMMENT ON COLUMN content_blocks.difficulty_score IS 'Pontuação de dificuldade (0.00 a 1.00)';
COMMENT ON COLUMN content_blocks.quality_score IS 'Pontuação de qualidade baseada em avaliações (0.00 a 1.00)';
COMMENT ON COLUMN content_blocks.usage_count IS 'Contador de quantas vezes o bloco foi utilizado';
COMMENT ON COLUMN content_blocks.generation_source IS 'Fonte de geração: llm, human, imported';
COMMENT ON COLUMN content_blocks.llm_model IS 'Modelo LLM usado na geração (ex: gpt-4, claude-3)';
COMMENT ON COLUMN content_blocks.tokens_used IS 'Quantidade de tokens consumidos na geração';
COMMENT ON COLUMN content_blocks.is_approved IS 'Indica se o conteúdo foi aprovado para uso';
COMMENT ON COLUMN content_blocks.approved_by IS 'Usuário que aprovou o conteúdo';
COMMENT ON COLUMN content_blocks.approved_at IS 'Data/hora da aprovação';

-- Índices para content_blocks
CREATE INDEX IF NOT EXISTS idx_content_blocks_descriptor ON content_blocks(descriptor_id);
CREATE INDEX IF NOT EXISTS idx_content_blocks_language ON content_blocks(language_code);
CREATE INDEX IF NOT EXISTS idx_content_blocks_hash ON content_blocks(content_hash);
CREATE INDEX IF NOT EXISTS idx_content_blocks_type ON content_blocks(type);
CREATE INDEX IF NOT EXISTS idx_content_blocks_approved ON content_blocks(is_approved) WHERE is_approved = true;

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_content_blocks_updated_at ON content_blocks;
CREATE TRIGGER trg_content_blocks_updated_at
    BEFORE UPDATE ON content_blocks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- 4. Tabela: blueprint_content_blocks (Associação Blueprint-Bloco)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS blueprint_content_blocks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    blueprint_id UUID NOT NULL,
    content_block_id UUID NOT NULL,
    module_index INTEGER NOT NULL,
    lesson_index INTEGER NOT NULL,
    is_placeholder BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_blueprint_blocks_blueprint FOREIGN KEY (blueprint_id)
        REFERENCES blueprints(id) ON DELETE CASCADE,
    CONSTRAINT fk_blueprint_blocks_content FOREIGN KEY (content_block_id)
        REFERENCES content_blocks(id) ON DELETE RESTRICT,
    CONSTRAINT uq_blueprint_blocks UNIQUE (blueprint_id, module_index, lesson_index),
    CONSTRAINT chk_blueprint_blocks_indexes CHECK (module_index >= 0 AND lesson_index >= 0)
);

COMMENT ON TABLE blueprint_content_blocks IS 'Vincula blocos de conteúdo existentes aos blueprints. is_placeholder=true indica que precisa ser gerado.';
COMMENT ON COLUMN blueprint_content_blocks.id IS 'Identificador único da associação';
COMMENT ON COLUMN blueprint_content_blocks.blueprint_id IS 'Referência ao blueprint';
COMMENT ON COLUMN blueprint_content_blocks.content_block_id IS 'Referência ao bloco de conteúdo';
COMMENT ON COLUMN blueprint_content_blocks.module_index IS 'Índice do módulo no blueprint (0-based)';
COMMENT ON COLUMN blueprint_content_blocks.lesson_index IS 'Índice da lição no módulo (0-based)';
COMMENT ON COLUMN blueprint_content_blocks.is_placeholder IS 'Indica se é placeholder para geração futura';

-- Índices para blueprint_content_blocks
CREATE INDEX IF NOT EXISTS idx_blueprint_blocks_blueprint ON blueprint_content_blocks(blueprint_id);
CREATE INDEX IF NOT EXISTS idx_blueprint_blocks_content ON blueprint_content_blocks(content_block_id);

-- ============================================================================
-- ROLLBACK SCRIPT (executar na ordem inversa em caso de rollback)
-- ============================================================================
--
-- Para reverter esta migration, execute os comandos abaixo na ordem:
--
-- -- Remover triggers
-- DROP TRIGGER IF EXISTS trg_content_blocks_updated_at ON content_blocks;
-- DROP TRIGGER IF EXISTS trg_blueprints_updated_at ON blueprints;
-- DROP TRIGGER IF EXISTS trg_prompt_templates_updated_at ON prompt_templates;
--
-- -- Remover tabelas (ordem inversa de dependência)
-- DROP TABLE IF EXISTS blueprint_content_blocks CASCADE;
-- DROP TABLE IF EXISTS content_blocks CASCADE;
-- DROP TABLE IF EXISTS blueprints CASCADE;
-- DROP TABLE IF EXISTS prompt_templates CASCADE;
--
-- ============================================================================
