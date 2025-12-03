-- V023: Adiciona campos para tracking de onboarding
-- Permite rastrear se o usuário completou o processo de onboarding inicial

ALTER TABLE users ADD COLUMN IF NOT EXISTS onboarding_completed BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS onboarding_completed_at TIMESTAMP WITH TIME ZONE;

-- Índice para queries de usuários que ainda não completaram onboarding
CREATE INDEX IF NOT EXISTS idx_users_onboarding_pending ON users(id) WHERE onboarding_completed = FALSE;

-- Comentários
COMMENT ON COLUMN users.onboarding_completed IS 'Indica se o usuário completou o processo de onboarding inicial';
COMMENT ON COLUMN users.onboarding_completed_at IS 'Data/hora em que o onboarding foi completado';
