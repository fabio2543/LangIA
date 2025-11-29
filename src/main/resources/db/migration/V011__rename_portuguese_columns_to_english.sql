-- =============================================================================
-- Migration V011: Rename Portuguese Column Names to English
-- Refactoring database schema to use English naming conventions
-- =============================================================================

-- =============================================================================
-- 1. USER_PROFILE_DETAILS
-- =============================================================================

ALTER TABLE user_profile_details RENAME COLUMN idioma_nativo TO native_language;
ALTER TABLE user_profile_details RENAME COLUMN fuso_horario TO timezone;
ALTER TABLE user_profile_details RENAME COLUMN data_nascimento TO birth_date;

-- Update constraint names
ALTER TABLE user_profile_details DROP CONSTRAINT IF EXISTS chk_idade_minima;
ALTER TABLE user_profile_details ADD CONSTRAINT chk_minimum_age CHECK (
    birth_date IS NULL OR birth_date <= CURRENT_DATE - INTERVAL '13 years'
);
ALTER TABLE user_profile_details DROP CONSTRAINT IF EXISTS chk_fuso_horario_formato;
ALTER TABLE user_profile_details ADD CONSTRAINT chk_timezone_format CHECK (
    timezone IS NULL OR timezone ~ '^[A-Za-z]+/[A-Za-z_]+$'
);

-- =============================================================================
-- 2. STUDENT_LEARNING_PREFERENCES
-- =============================================================================

ALTER TABLE student_learning_preferences RENAME COLUMN tempo_diario_disponivel TO daily_time_available;
ALTER TABLE student_learning_preferences RENAME COLUMN dias_semana_preferidos TO preferred_days;
ALTER TABLE student_learning_preferences RENAME COLUMN horarios_preferidos TO preferred_times;
ALTER TABLE student_learning_preferences RENAME COLUMN meta_horas_semana TO weekly_hours_goal;
ALTER TABLE student_learning_preferences RENAME COLUMN topicos_interesse TO topics_of_interest;
ALTER TABLE student_learning_preferences RENAME COLUMN topicos_customizados TO custom_topics;
ALTER TABLE student_learning_preferences RENAME COLUMN formatos_preferidos TO preferred_formats;
ALTER TABLE student_learning_preferences RENAME COLUMN ranking_formatos TO format_ranking;
ALTER TABLE student_learning_preferences RENAME COLUMN objetivo_principal TO primary_objective;
ALTER TABLE student_learning_preferences RENAME COLUMN objetivo_descricao TO objective_description;
ALTER TABLE student_learning_preferences RENAME COLUMN prazo_objetivo TO objective_deadline;

-- Update constraints
ALTER TABLE student_learning_preferences DROP CONSTRAINT IF EXISTS chk_topicos_customizados_limite;
ALTER TABLE student_learning_preferences ADD CONSTRAINT chk_custom_topics_limit CHECK (
    custom_topics IS NULL OR jsonb_array_length(custom_topics) <= 5
);

-- =============================================================================
-- 3. STUDENT_SKILL_ASSESSMENTS
-- =============================================================================

ALTER TABLE student_skill_assessments RENAME COLUMN idioma TO language;
ALTER TABLE student_skill_assessments RENAME COLUMN dificuldade_escuta TO listening_difficulty;
ALTER TABLE student_skill_assessments RENAME COLUMN dificuldade_fala TO speaking_difficulty;
ALTER TABLE student_skill_assessments RENAME COLUMN dificuldade_leitura TO reading_difficulty;
ALTER TABLE student_skill_assessments RENAME COLUMN dificuldade_escrita TO writing_difficulty;
ALTER TABLE student_skill_assessments RENAME COLUMN detalhes_escuta TO listening_details;
ALTER TABLE student_skill_assessments RENAME COLUMN detalhes_fala TO speaking_details;
ALTER TABLE student_skill_assessments RENAME COLUMN detalhes_leitura TO reading_details;
ALTER TABLE student_skill_assessments RENAME COLUMN detalhes_escrita TO writing_details;
ALTER TABLE student_skill_assessments RENAME COLUMN nivel_cefr_auto TO self_cefr_level;

-- Update constraint
ALTER TABLE student_skill_assessments DROP CONSTRAINT IF EXISTS uq_assessment_idioma_momento;
ALTER TABLE student_skill_assessments ADD CONSTRAINT uq_assessment_language_time UNIQUE (user_id, language, assessed_at);

-- Update index
DROP INDEX IF EXISTS idx_ssa_idioma;
CREATE INDEX idx_ssa_language ON student_skill_assessments(language);

-- =============================================================================
-- 4. NOTIFICATION_SETTINGS
-- =============================================================================

ALTER TABLE notification_settings RENAME COLUMN canais_ativos TO active_channels;
ALTER TABLE notification_settings RENAME COLUMN preferencias_por_categoria TO category_preferences;
ALTER TABLE notification_settings RENAME COLUMN frequencia_lembretes TO reminder_frequency;
ALTER TABLE notification_settings RENAME COLUMN horario_preferido_inicio TO preferred_time_start;
ALTER TABLE notification_settings RENAME COLUMN horario_preferido_fim TO preferred_time_end;
ALTER TABLE notification_settings RENAME COLUMN modo_silencioso_inicio TO quiet_mode_start;
ALTER TABLE notification_settings RENAME COLUMN modo_silencioso_fim TO quiet_mode_end;

-- =============================================================================
-- 5. EMAIL_CHANGE_REQUESTS
-- =============================================================================

ALTER TABLE email_change_requests RENAME COLUMN novo_email TO new_email;

-- Update constraint
ALTER TABLE email_change_requests DROP CONSTRAINT IF EXISTS chk_novo_email_formato;
ALTER TABLE email_change_requests ADD CONSTRAINT chk_new_email_format CHECK (
    new_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'
);

-- =============================================================================
-- 6. UPDATE INDEXES
-- =============================================================================

DROP INDEX IF EXISTS idx_slp_idioma_principal;
DROP INDEX IF EXISTS idx_slp_objetivo;
CREATE INDEX idx_slp_primary_objective ON student_learning_preferences(primary_objective);

-- =============================================================================
-- 7. UPDATE FUNCTIONALITY CODES
-- =============================================================================

UPDATE functionalities SET code = 'view_own_profile' WHERE code = 'visualizar_proprio_perfil';
UPDATE functionalities SET code = 'edit_own_profile' WHERE code = 'editar_proprio_perfil';
UPDATE functionalities SET code = 'view_other_profiles' WHERE code = 'ver_perfis_de_outros';
UPDATE functionalities SET code = 'edit_other_profiles' WHERE code = 'editar_perfis_de_outros';
UPDATE functionalities SET code = 'view_available_lessons' WHERE code = 'visualizar_aulas_disponiveis';
UPDATE functionalities SET code = 'enroll' WHERE code = 'matricular_se';
UPDATE functionalities SET code = 'create_lessons' WHERE code = 'criar_aulas';
UPDATE functionalities SET code = 'edit_own_lessons' WHERE code = 'editar_proprias_aulas';
UPDATE functionalities SET code = 'edit_other_lessons' WHERE code = 'editar_aulas_de_outros';
UPDATE functionalities SET code = 'delete_any_lesson' WHERE code = 'deletar_qualquer_aula';
UPDATE functionalities SET code = 'view_own_progress' WHERE code = 'ver_proprio_progresso';
UPDATE functionalities SET code = 'view_student_progress' WHERE code = 'ver_progresso_de_alunos';
UPDATE functionalities SET code = 'manage_enrollments' WHERE code = 'gerenciar_matriculas';
UPDATE functionalities SET code = 'access_admin_dashboard' WHERE code = 'acessar_dashboard_admin';
UPDATE functionalities SET code = 'configure_system' WHERE code = 'configurar_sistema';
UPDATE functionalities SET code = 'view_system_logs' WHERE code = 'ver_logs_sistema';
UPDATE functionalities SET code = 'manage_users' WHERE code = 'gerenciar_usuarios';
