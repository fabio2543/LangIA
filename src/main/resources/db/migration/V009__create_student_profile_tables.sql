-- =============================================================================
-- Migration V009: Student Profile Tables
-- Tabelas complementares para gestão de perfil do estudante
-- =============================================================================

-- =============================================================================
-- ENUM TYPES
-- =============================================================================

CREATE TYPE cefr_level AS ENUM ('A1', 'A2', 'B1', 'B2', 'C1', 'C2');
CREATE TYPE difficulty_level AS ENUM ('nenhuma', 'pouca', 'moderada', 'muita');
CREATE TYPE time_available AS ENUM ('15min', '30min', '45min', '1h', '1h30', '2h_plus');
CREATE TYPE reminder_frequency AS ENUM ('diario', 'dias_alternados', 'semanal', 'personalizado');
CREATE TYPE learning_objective AS ENUM (
    'carreira', 'universidade', 'exames', 'viagem', 'hobby', 'imigracao', 'outro'
);
CREATE TYPE learning_format AS ENUM (
    'video_aulas', 'exercicios_escritos', 'conversacao', 'jogos',
    'leitura', 'audio_podcast', 'flashcards'
);

-- =============================================================================
-- 1. USER_PROFILE_DETAILS (Complemento para users - STUDENT e TEACHER)
-- =============================================================================

CREATE TABLE user_profile_details (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,

    -- Dados pessoais adicionais
    idioma_nativo VARCHAR(50),
    fuso_horario VARCHAR(50) DEFAULT 'America/Sao_Paulo',
    data_nascimento DATE,

    -- Bio/Sobre
    bio TEXT,

    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_idade_minima CHECK (
        data_nascimento IS NULL OR
        data_nascimento <= CURRENT_DATE - INTERVAL '13 years'
    ),
    CONSTRAINT chk_fuso_horario_formato CHECK (
        fuso_horario IS NULL OR fuso_horario ~ '^[A-Za-z]+/[A-Za-z_]+$'
    )
);

-- =============================================================================
-- 2. STUDENT_LEARNING_PREFERENCES
-- =============================================================================

CREATE TABLE student_learning_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,

    -- Idiomas de estudo
    idiomas_estudo TEXT[] NOT NULL DEFAULT '{}',
    idioma_principal VARCHAR(50),
    nivel_auto_por_idioma JSONB DEFAULT '{}',

    -- Disponibilidade
    tempo_diario_disponivel time_available DEFAULT '30min',
    dias_semana_preferidos TEXT[] NOT NULL DEFAULT '{}',
    horarios_preferidos TEXT[],
    meta_horas_semana INTEGER,

    -- Interesses e formatos
    topicos_interesse TEXT[] NOT NULL DEFAULT '{}',
    topicos_customizados TEXT[],
    formatos_preferidos learning_format[] NOT NULL DEFAULT '{}',
    ranking_formatos learning_format[],

    -- Objetivos
    objetivo_principal learning_objective,
    objetivo_descricao VARCHAR(500),
    prazo_objetivo VARCHAR(30),

    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_idiomas_limite CHECK (array_length(idiomas_estudo, 1) <= 3),
    CONSTRAINT chk_dias_semana_validos CHECK (
        dias_semana_preferidos <@ ARRAY['seg', 'ter', 'qua', 'qui', 'sex', 'sab', 'dom']
    ),
    CONSTRAINT chk_topicos_customizados_limite CHECK (
        topicos_customizados IS NULL OR array_length(topicos_customizados, 1) <= 5
    ),
    CONSTRAINT chk_meta_horas_range CHECK (
        meta_horas_semana IS NULL OR (meta_horas_semana >= 1 AND meta_horas_semana <= 40)
    )
);

-- =============================================================================
-- 3. STUDENT_SKILL_ASSESSMENTS (Histórico de autoavaliações)
-- =============================================================================

CREATE TABLE student_skill_assessments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- Idioma avaliado
    idioma VARCHAR(50) NOT NULL,

    -- Nível de dificuldade por habilidade
    dificuldade_escuta difficulty_level NOT NULL DEFAULT 'moderada',
    dificuldade_fala difficulty_level NOT NULL DEFAULT 'moderada',
    dificuldade_leitura difficulty_level NOT NULL DEFAULT 'moderada',
    dificuldade_escrita difficulty_level NOT NULL DEFAULT 'moderada',

    -- Detalhes específicos (arrays de tags)
    detalhes_escuta TEXT[],
    detalhes_fala TEXT[],
    detalhes_leitura TEXT[],
    detalhes_escrita TEXT[],

    -- Nível CEFR autoavaliado
    nivel_cefr_auto cefr_level,

    -- Quando foi avaliado
    assessed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraint: um assessment por idioma por momento
    CONSTRAINT uq_assessment_idioma_momento UNIQUE (user_id, idioma, assessed_at)
);

-- =============================================================================
-- 4. NOTIFICATION_SETTINGS (Genérica para todos os perfis)
-- =============================================================================

CREATE TABLE notification_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,

    -- Canais ativos (ex: {"push": true, "email": true, "whatsapp": false})
    canais_ativos JSONB NOT NULL DEFAULT '{"push": true, "email": true, "whatsapp": false}',

    -- Preferências por categoria (ex: {"lembretes": {"ativo": true, "canais": ["push", "email"]}})
    preferencias_por_categoria JSONB DEFAULT '{}',

    -- Frequência de lembretes
    frequencia_lembretes reminder_frequency DEFAULT 'diario',

    -- Horários preferidos para notificações
    horario_preferido_inicio TIME,
    horario_preferido_fim TIME,

    -- Modo silencioso (não enviar notificações neste período)
    modo_silencioso_inicio TIME,
    modo_silencioso_fim TIME,

    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 5. EMAIL_CHANGE_REQUESTS (Para alteração de e-mail)
-- =============================================================================

CREATE TABLE email_change_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- Novo e-mail solicitado
    novo_email VARCHAR(255) NOT NULL,

    -- Token de verificação (hash bcrypt)
    token_hash VARCHAR(255) NOT NULL,

    -- Validade e uso
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,

    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraint: novo email deve ser único
    CONSTRAINT chk_novo_email_formato CHECK (novo_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- =============================================================================
-- ÍNDICES
-- =============================================================================

-- user_profile_details
CREATE INDEX idx_upd_user_id ON user_profile_details(user_id);

-- student_learning_preferences
CREATE INDEX idx_slp_user_id ON student_learning_preferences(user_id);
CREATE INDEX idx_slp_idioma_principal ON student_learning_preferences(idioma_principal);
CREATE INDEX idx_slp_objetivo ON student_learning_preferences(objetivo_principal);

-- student_skill_assessments
CREATE INDEX idx_ssa_user_id ON student_skill_assessments(user_id);
CREATE INDEX idx_ssa_idioma ON student_skill_assessments(idioma);
CREATE INDEX idx_ssa_assessed_at ON student_skill_assessments(assessed_at DESC);

-- notification_settings
CREATE INDEX idx_ns_user_id ON notification_settings(user_id);

-- email_change_requests
CREATE INDEX idx_ecr_user_id ON email_change_requests(user_id);
CREATE INDEX idx_ecr_token_hash ON email_change_requests(token_hash);
CREATE INDEX idx_ecr_expires_at ON email_change_requests(expires_at);

-- =============================================================================
-- TRIGGER PARA updated_at
-- =============================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_user_profile_details_updated_at
    BEFORE UPDATE ON user_profile_details
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_student_learning_preferences_updated_at
    BEFORE UPDATE ON student_learning_preferences
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_settings_updated_at
    BEFORE UPDATE ON notification_settings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
