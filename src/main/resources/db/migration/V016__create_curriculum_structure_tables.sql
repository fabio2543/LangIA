-- ============================================================================
-- Migration V016: Tabelas de Estrutura Curricular para Módulo de Trilhas
-- ============================================================================
-- Esta camada define a base curricular seguindo o padrão CEFR (A1-C2)
-- com competências linguísticas e descritores de aprendizagem (Can-Do Statements).
--
-- Hierarquia: Language -> Level -> Competency -> Descriptor
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Função auxiliar para atualização automática de updated_at
-- ----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION update_updated_at_column() IS 'Função trigger para atualização automática do campo updated_at';

-- ----------------------------------------------------------------------------
-- 2. Tabela: languages (Usar tabela existente de V010)
-- ----------------------------------------------------------------------------
-- A tabela languages já foi criada na V010 com estrutura:
-- code VARCHAR(10) PRIMARY KEY, name_pt, name_en, name_es, active, created_at
--
-- Adicionamos apenas a coluna name_native se não existir

ALTER TABLE languages ADD COLUMN IF NOT EXISTS name_native VARCHAR(100);

-- Atualizar name_native com valores baseados nos nomes existentes
UPDATE languages SET name_native =
    CASE code
        WHEN 'en' THEN 'English'
        WHEN 'es' THEN 'Español'
        WHEN 'pt' THEN 'Português'
        WHEN 'fr' THEN 'Français'
        WHEN 'de' THEN 'Deutsch'
        WHEN 'it' THEN 'Italiano'
        WHEN 'zh' THEN '中文'
        WHEN 'ja' THEN '日本語'
        WHEN 'ko' THEN '한국어'
        WHEN 'ru' THEN 'Русский'
        ELSE name_en
    END
WHERE name_native IS NULL;

COMMENT ON TABLE languages IS 'Idiomas disponíveis para estudo na plataforma. Máximo 3 simultâneos por aluno.';
COMMENT ON COLUMN languages.name_native IS 'Nome do idioma no idioma nativo';

-- ----------------------------------------------------------------------------
-- 3. Tabela: levels (Níveis CEFR)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS levels (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(2) NOT NULL,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    order_index INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_levels_code UNIQUE (code),
    CONSTRAINT uq_levels_order_index UNIQUE (order_index),
    CONSTRAINT chk_levels_order_index CHECK (order_index BETWEEN 1 AND 6)
);

COMMENT ON TABLE levels IS 'Níveis do Common European Framework of Reference (CEFR): A1, A2, B1, B2, C1, C2';
COMMENT ON COLUMN levels.id IS 'Identificador único do nível';
COMMENT ON COLUMN levels.code IS 'Código CEFR do nível (A1, A2, B1, B2, C1, C2)';
COMMENT ON COLUMN levels.name IS 'Nome descritivo do nível (Iniciante, Básico, Intermediário, etc.)';
COMMENT ON COLUMN levels.description IS 'Descrição detalhada das capacidades esperadas neste nível';
COMMENT ON COLUMN levels.order_index IS 'Ordem sequencial do nível (1=A1 até 6=C2)';

-- Índices para levels
CREATE INDEX IF NOT EXISTS idx_levels_code ON levels(code);
CREATE INDEX IF NOT EXISTS idx_levels_order ON levels(order_index);

-- ----------------------------------------------------------------------------
-- 4. Tabela: competencies (Competências linguísticas)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS competencies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon VARCHAR(50),
    category VARCHAR(50),
    order_index INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_competencies_code UNIQUE (code)
);

COMMENT ON TABLE competencies IS 'Competências linguísticas avaliadas e desenvolvidas nas trilhas';
COMMENT ON COLUMN competencies.id IS 'Identificador único da competência';
COMMENT ON COLUMN competencies.code IS 'Código da competência (speaking, listening, reading, writing, grammar, vocabulary, pronunciation)';
COMMENT ON COLUMN competencies.name IS 'Nome da competência em português';
COMMENT ON COLUMN competencies.description IS 'Descrição detalhada da competência';
COMMENT ON COLUMN competencies.icon IS 'Nome do ícone para exibição na interface';
COMMENT ON COLUMN competencies.order_index IS 'Ordem de exibição na interface';

-- Índices para competencies
CREATE INDEX IF NOT EXISTS idx_competencies_code ON competencies(code);

-- ----------------------------------------------------------------------------
-- 5. Tabela: level_competencies (Associação Nível-Competência)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS level_competencies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    level_id UUID NOT NULL,
    competency_id UUID NOT NULL,
    weight DECIMAL(3,2) DEFAULT 1.00,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_level_competencies_level FOREIGN KEY (level_id)
        REFERENCES levels(id) ON DELETE CASCADE,
    CONSTRAINT fk_level_competencies_competency FOREIGN KEY (competency_id)
        REFERENCES competencies(id) ON DELETE CASCADE,
    CONSTRAINT uq_level_competencies UNIQUE (level_id, competency_id),
    CONSTRAINT chk_level_competencies_weight CHECK (weight >= 0.00 AND weight <= 1.00)
);

COMMENT ON TABLE level_competencies IS 'Define quais competências são trabalhadas em cada nível CEFR e com qual peso';
COMMENT ON COLUMN level_competencies.id IS 'Identificador único da associação';
COMMENT ON COLUMN level_competencies.level_id IS 'Referência ao nível CEFR';
COMMENT ON COLUMN level_competencies.competency_id IS 'Referência à competência linguística';
COMMENT ON COLUMN level_competencies.weight IS 'Peso/importância da competência neste nível (0.00 a 1.00)';

-- Índices para level_competencies
CREATE INDEX IF NOT EXISTS idx_level_competencies_level ON level_competencies(level_id);
CREATE INDEX IF NOT EXISTS idx_level_competencies_competency ON level_competencies(competency_id);

-- ----------------------------------------------------------------------------
-- 6. Tabela: descriptors (Descritores de aprendizagem / Can-Do Statements)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS descriptors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    level_competency_id UUID NOT NULL,
    language_code VARCHAR(10),
    code VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    description_en TEXT,
    order_index INTEGER NOT NULL,
    is_core BOOLEAN NOT NULL DEFAULT true,
    estimated_hours DECIMAL(4,1),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_descriptors_level_competency FOREIGN KEY (level_competency_id)
        REFERENCES level_competencies(id) ON DELETE CASCADE,
    CONSTRAINT fk_descriptors_language FOREIGN KEY (language_code)
        REFERENCES languages(code) ON DELETE SET NULL,
    CONSTRAINT uq_descriptors_code UNIQUE (level_competency_id, language_code, code)
);

COMMENT ON TABLE descriptors IS 'Descritores de aprendizagem baseados em CEFR (Can-Do Statements). language_code NULL indica descritor universal aplicável a todos os idiomas.';
COMMENT ON COLUMN descriptors.id IS 'Identificador único do descritor';
COMMENT ON COLUMN descriptors.level_competency_id IS 'Referência à combinação nível-competência';
COMMENT ON COLUMN descriptors.language_code IS 'Código do idioma específico (NULL = descritor universal para todos os idiomas)';
COMMENT ON COLUMN descriptors.code IS 'Código único do descritor (ex: A1-SP-001)';
COMMENT ON COLUMN descriptors.description IS 'Descrição do objetivo em português (ex: "Consegue se apresentar")';
COMMENT ON COLUMN descriptors.description_en IS 'Descrição em inglês para referência internacional';
COMMENT ON COLUMN descriptors.order_index IS 'Ordem de apresentação/progressão';
COMMENT ON COLUMN descriptors.is_core IS 'Indica se é descritor obrigatório (core) ou opcional';
COMMENT ON COLUMN descriptors.estimated_hours IS 'Horas estimadas para domínio deste descritor';

-- Índices para descriptors
CREATE INDEX IF NOT EXISTS idx_descriptors_level_competency ON descriptors(level_competency_id);
CREATE INDEX IF NOT EXISTS idx_descriptors_language ON descriptors(language_code);
CREATE INDEX IF NOT EXISTS idx_descriptors_code ON descriptors(code);
CREATE INDEX IF NOT EXISTS idx_descriptors_core ON descriptors(is_core) WHERE is_core = true;

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_descriptors_updated_at ON descriptors;
CREATE TRIGGER trg_descriptors_updated_at
    BEFORE UPDATE ON descriptors
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- ROLLBACK SCRIPT (executar na ordem inversa em caso de rollback)
-- ============================================================================
--
-- Para reverter esta migration, execute os comandos abaixo na ordem:
--
-- -- Remover triggers
-- DROP TRIGGER IF EXISTS trg_descriptors_updated_at ON descriptors;
--
-- -- Remover tabelas (ordem inversa de dependência)
-- DROP TABLE IF EXISTS descriptors CASCADE;
-- DROP TABLE IF EXISTS level_competencies CASCADE;
-- DROP TABLE IF EXISTS competencies CASCADE;
-- DROP TABLE IF EXISTS levels CASCADE;
--
-- -- Remover coluna adicionada em languages (não remover tabela pois é de V010)
-- ALTER TABLE languages DROP COLUMN IF EXISTS name_native;
--
-- -- Remover função auxiliar (apenas se não usada por outras tabelas)
-- DROP FUNCTION IF EXISTS update_updated_at_column();
--
-- ============================================================================
