-- Migration: Adicionar verificacao de e-mail
-- Descricao: Adiciona campos para verificacao de e-mail na tabela users
--            e cria tabela para tokens de verificacao

-- Adicionar colunas na tabela users
ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN email_verified_at TIMESTAMP;

-- Criar tabela email_verification_tokens
CREATE TABLE email_verification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indices para performance
CREATE INDEX idx_evt_token_hash ON email_verification_tokens(token_hash);
CREATE INDEX idx_evt_user_id ON email_verification_tokens(user_id);

-- Marcar usuarios existentes como verificados para nao quebrar fluxo atual
UPDATE users SET email_verified = TRUE, email_verified_at = created_at;
