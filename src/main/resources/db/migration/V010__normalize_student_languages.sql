-- =============================================================================
-- Migration V010: Normalizar Idiomas de Estudantes
-- Cria tabela de referência de idiomas e tabela de enrollment
-- Corrige tipos de colunas em student_learning_preferences
-- =============================================================================

-- =============================================================================
-- 1. TABELA DE REFERÊNCIA DE IDIOMAS
-- =============================================================================

CREATE TABLE IF NOT EXISTS languages (
    code VARCHAR(10) PRIMARY KEY,
    name_pt VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    name_es VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Seed de idiomas suportados
INSERT INTO languages (code, name_pt, name_en, name_es) VALUES
    ('en', 'Inglês', 'English', 'Inglés'),
    ('es', 'Espanhol', 'Spanish', 'Español'),
    ('pt', 'Português', 'Portuguese', 'Portugués'),
    ('fr', 'Francês', 'French', 'Francés'),
    ('de', 'Alemão', 'German', 'Alemán'),
    ('it', 'Italiano', 'Italian', 'Italiano'),
    ('zh', 'Chinês', 'Chinese', 'Chino'),
    ('ja', 'Japonês', 'Japanese', 'Japonés'),
    ('ko', 'Coreano', 'Korean', 'Coreano'),
    ('ru', 'Russo', 'Russian', 'Ruso')
ON CONFLICT (code) DO NOTHING;

-- =============================================================================
-- 2. TABELA DE ENROLLMENT DE IDIOMAS
-- =============================================================================

CREATE TABLE IF NOT EXISTS student_language_enrollments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    language_code VARCHAR(10) NOT NULL REFERENCES languages(code),
    cefr_level VARCHAR(2) CHECK (cefr_level IS NULL OR cefr_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    is_primary BOOLEAN NOT NULL DEFAULT false,
    enrolled_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_studied_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_user_language UNIQUE (user_id, language_code)
);

-- Índices (IF NOT EXISTS)
CREATE INDEX IF NOT EXISTS idx_sle_user_id ON student_language_enrollments(user_id);
CREATE INDEX IF NOT EXISTS idx_sle_language_code ON student_language_enrollments(language_code);
CREATE INDEX IF NOT EXISTS idx_sle_is_primary ON student_language_enrollments(user_id, is_primary) WHERE is_primary = true;

-- Trigger para updated_at (drop if exists, then create)
DROP TRIGGER IF EXISTS update_student_language_enrollments_updated_at ON student_language_enrollments;
CREATE TRIGGER update_student_language_enrollments_updated_at
    BEFORE UPDATE ON student_language_enrollments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Função para garantir máximo de 3 idiomas por aluno
CREATE OR REPLACE FUNCTION check_max_languages()
RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT COUNT(*) FROM student_language_enrollments WHERE user_id = NEW.user_id) >= 3 THEN
        RAISE EXCEPTION 'Maximum of 3 languages per student allowed';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_check_max_languages ON student_language_enrollments;
CREATE TRIGGER trg_check_max_languages
    BEFORE INSERT ON student_language_enrollments
    FOR EACH ROW EXECUTE FUNCTION check_max_languages();

-- Função para garantir apenas um idioma primário por usuário
CREATE OR REPLACE FUNCTION ensure_single_primary_language()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_primary = true THEN
        UPDATE student_language_enrollments
        SET is_primary = false
        WHERE user_id = NEW.user_id AND id != NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_ensure_single_primary ON student_language_enrollments;
CREATE TRIGGER trg_ensure_single_primary
    BEFORE INSERT OR UPDATE ON student_language_enrollments
    FOR EACH ROW EXECUTE FUNCTION ensure_single_primary_language();

-- =============================================================================
-- 3. CORRIGIR TIPOS EM STUDENT_LEARNING_PREFERENCES
-- =============================================================================

-- Remover constraints que dependem de arrays nativos
ALTER TABLE student_learning_preferences
    DROP CONSTRAINT IF EXISTS chk_idiomas_limite,
    DROP CONSTRAINT IF EXISTS chk_dias_semana_validos,
    DROP CONSTRAINT IF EXISTS chk_topicos_customizados_limite;

-- Remover colunas de idiomas (agora em tabela separada)
ALTER TABLE student_learning_preferences
    DROP COLUMN IF EXISTS idiomas_estudo,
    DROP COLUMN IF EXISTS idioma_principal,
    DROP COLUMN IF EXISTS nivel_auto_por_idioma;

-- Remover defaults existentes (necessário antes de alterar tipo)
ALTER TABLE student_learning_preferences
    ALTER COLUMN dias_semana_preferidos DROP DEFAULT,
    ALTER COLUMN horarios_preferidos DROP DEFAULT,
    ALTER COLUMN topicos_interesse DROP DEFAULT,
    ALTER COLUMN topicos_customizados DROP DEFAULT,
    ALTER COLUMN formatos_preferidos DROP DEFAULT,
    ALTER COLUMN ranking_formatos DROP DEFAULT,
    ALTER COLUMN tempo_diario_disponivel DROP DEFAULT;

-- Converter colunas TEXT[] para JSONB
ALTER TABLE student_learning_preferences
    ALTER COLUMN dias_semana_preferidos TYPE JSONB USING COALESCE(to_jsonb(dias_semana_preferidos), '[]'::jsonb),
    ALTER COLUMN horarios_preferidos TYPE JSONB USING COALESCE(to_jsonb(horarios_preferidos), '[]'::jsonb),
    ALTER COLUMN topicos_interesse TYPE JSONB USING COALESCE(to_jsonb(topicos_interesse), '[]'::jsonb),
    ALTER COLUMN topicos_customizados TYPE JSONB USING COALESCE(to_jsonb(topicos_customizados), '[]'::jsonb);

-- Converter colunas de enum array para JSONB
ALTER TABLE student_learning_preferences
    ALTER COLUMN formatos_preferidos TYPE JSONB USING COALESCE(to_jsonb(formatos_preferidos::text[]), '[]'::jsonb),
    ALTER COLUMN ranking_formatos TYPE JSONB USING COALESCE(to_jsonb(ranking_formatos::text[]), '[]'::jsonb);

-- Converter enum para VARCHAR
ALTER TABLE student_learning_preferences
    ALTER COLUMN tempo_diario_disponivel TYPE VARCHAR(20) USING tempo_diario_disponivel::text;

-- Definir novos defaults JSONB
ALTER TABLE student_learning_preferences
    ALTER COLUMN dias_semana_preferidos SET DEFAULT '[]'::jsonb,
    ALTER COLUMN horarios_preferidos SET DEFAULT '[]'::jsonb,
    ALTER COLUMN topicos_interesse SET DEFAULT '[]'::jsonb,
    ALTER COLUMN topicos_customizados SET DEFAULT '[]'::jsonb,
    ALTER COLUMN formatos_preferidos SET DEFAULT '[]'::jsonb,
    ALTER COLUMN ranking_formatos SET DEFAULT '[]'::jsonb;

-- Recriar constraints usando JSONB
ALTER TABLE student_learning_preferences
    ADD CONSTRAINT chk_topicos_customizados_limite CHECK (
        topicos_customizados IS NULL OR jsonb_array_length(topicos_customizados) <= 5
    );

-- =============================================================================
-- 4. LIMPAR TIPOS ENUM NÃO UTILIZADOS
-- =============================================================================

DROP TYPE IF EXISTS time_available CASCADE;
DROP TYPE IF EXISTS learning_format CASCADE;
