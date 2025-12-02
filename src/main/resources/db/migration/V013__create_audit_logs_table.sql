-- Migration: Criar tabela de auditoria
-- Descrição: Tabela para registro de todas as operações de criação, atualização e exclusão

-- Criar tabela audit_logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(20) NOT NULL,
    old_value JSONB,
    new_value JSONB,
    user_id UUID NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,

    -- Constraint para validar ações permitidas
    CONSTRAINT chk_audit_action CHECK (action IN ('CREATE', 'UPDATE', 'DELETE'))
);

-- Índice para busca por entidade específica
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity
    ON audit_logs(entity_type, entity_id);

-- Índice para busca por usuário
CREATE INDEX IF NOT EXISTS idx_audit_logs_user
    ON audit_logs(user_id);

-- Índice para busca por data (mais recentes primeiro)
CREATE INDEX IF NOT EXISTS idx_audit_logs_created
    ON audit_logs(created_at DESC);

-- Índice para busca por tipo de ação
CREATE INDEX IF NOT EXISTS idx_audit_logs_action
    ON audit_logs(action);

-- Índice composto para consultas frequentes
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_entity
    ON audit_logs(user_id, entity_type, created_at DESC);

-- Comentários na tabela e colunas
COMMENT ON TABLE audit_logs IS 'Registro de auditoria de todas as operações do sistema';
COMMENT ON COLUMN audit_logs.entity_type IS 'Tipo da entidade alterada (ex: USER, PREFERENCES)';
COMMENT ON COLUMN audit_logs.entity_id IS 'ID da entidade alterada';
COMMENT ON COLUMN audit_logs.action IS 'Tipo de operação: CREATE, UPDATE ou DELETE';
COMMENT ON COLUMN audit_logs.old_value IS 'Estado anterior da entidade (JSON) - null para CREATE';
COMMENT ON COLUMN audit_logs.new_value IS 'Novo estado da entidade (JSON) - null para DELETE';
COMMENT ON COLUMN audit_logs.user_id IS 'ID do usuário que realizou a operação';
COMMENT ON COLUMN audit_logs.ip_address IS 'Endereço IP de origem da requisição';
COMMENT ON COLUMN audit_logs.user_agent IS 'User-Agent do cliente';
COMMENT ON COLUMN audit_logs.created_at IS 'Timestamp da operação';
