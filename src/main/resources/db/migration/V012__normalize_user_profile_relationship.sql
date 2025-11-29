-- V012__normalize_user_profile_relationship.sql
-- Normaliza o relacionamento entre users e profiles adicionando FK

-- 1. Adiciona a coluna profile_id
ALTER TABLE users ADD COLUMN profile_id UUID;

-- 2. Popula profile_id baseado no valor atual do enum profile
UPDATE users u
SET profile_id = p.id
FROM profiles p
WHERE p.code = u.profile;

-- 3. Adiciona constraint NOT NULL após popular os dados
ALTER TABLE users ALTER COLUMN profile_id SET NOT NULL;

-- 4. Adiciona a FK constraint
ALTER TABLE users
ADD CONSTRAINT fk_users_profile
FOREIGN KEY (profile_id)
REFERENCES profiles(id);

-- 5. Cria índice para performance em queries por perfil
CREATE INDEX idx_users_profile_id ON users(profile_id);

-- 6. Remove a coluna profile (enum) que agora é redundante
ALTER TABLE users DROP COLUMN profile;
