-- ============================================================================
-- Migration V018: Tabelas de Suporte para Módulo de Trilhas
-- ============================================================================
-- Esta camada contém tabelas de suporte para processamento assíncrono,
-- cache, versionamento e auditoria do módulo de trilhas.
--
-- Tabelas: Jobs, Cache, Versões, Auditoria, RAG Embeddings
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Tabela: trail_generation_jobs (Jobs de geração de trilha)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS trail_generation_jobs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trail_id UUID NOT NULL,
    student_id UUID NOT NULL,
    status generation_job_status NOT NULL DEFAULT 'QUEUED',
    priority INTEGER NOT NULL DEFAULT 5,
    job_type VARCHAR(30) NOT NULL DEFAULT 'full_generation',
    gaps JSONB,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 5,
    last_error TEXT,
    error_details JSONB,
    tokens_used INTEGER NOT NULL DEFAULT 0,
    processing_time_ms INTEGER,
    worker_id VARCHAR(100),
    queued_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    next_retry_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_jobs_priority CHECK (priority BETWEEN 1 AND 10),
    CONSTRAINT chk_jobs_type CHECK (job_type IN ('full_generation', 'gap_fill', 'refresh')),
    CONSTRAINT chk_jobs_attempts CHECK (attempt_count >= 0 AND attempt_count <= max_attempts)
);

-- Nota: FKs para trails e students serão adicionadas quando essas tabelas forem criadas
-- ALTER TABLE trail_generation_jobs ADD CONSTRAINT fk_jobs_trail FOREIGN KEY (trail_id) REFERENCES trails(id) ON DELETE CASCADE;
-- ALTER TABLE trail_generation_jobs ADD CONSTRAINT fk_jobs_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE;

COMMENT ON TABLE trail_generation_jobs IS 'Fila de jobs para geração de trilhas. Processados por LLM Workers.';
COMMENT ON COLUMN trail_generation_jobs.id IS 'Identificador único do job';
COMMENT ON COLUMN trail_generation_jobs.trail_id IS 'Trilha a ser gerada/atualizada';
COMMENT ON COLUMN trail_generation_jobs.student_id IS 'Estudante solicitante';
COMMENT ON COLUMN trail_generation_jobs.status IS 'Status do job: QUEUED, PROCESSING, COMPLETED, FAILED, CANCELLED';
COMMENT ON COLUMN trail_generation_jobs.priority IS 'Prioridade de 1 (mais alta) a 10 (mais baixa)';
COMMENT ON COLUMN trail_generation_jobs.job_type IS 'Tipo: full_generation, gap_fill, refresh';
COMMENT ON COLUMN trail_generation_jobs.gaps IS 'Array de gaps a preencher: [{module_index, lesson_index, descriptor_code}]';
COMMENT ON COLUMN trail_generation_jobs.attempt_count IS 'Número de tentativas realizadas';
COMMENT ON COLUMN trail_generation_jobs.max_attempts IS 'Número máximo de tentativas permitidas';
COMMENT ON COLUMN trail_generation_jobs.last_error IS 'Mensagem do último erro';
COMMENT ON COLUMN trail_generation_jobs.error_details IS 'Detalhes técnicos do erro em JSON';
COMMENT ON COLUMN trail_generation_jobs.tokens_used IS 'Total de tokens LLM consumidos';
COMMENT ON COLUMN trail_generation_jobs.processing_time_ms IS 'Tempo de processamento em milissegundos';
COMMENT ON COLUMN trail_generation_jobs.worker_id IS 'Identificador do worker que processou';
COMMENT ON COLUMN trail_generation_jobs.queued_at IS 'Timestamp de entrada na fila';
COMMENT ON COLUMN trail_generation_jobs.started_at IS 'Timestamp de início do processamento';
COMMENT ON COLUMN trail_generation_jobs.completed_at IS 'Timestamp de conclusão com sucesso';
COMMENT ON COLUMN trail_generation_jobs.failed_at IS 'Timestamp da última falha';
COMMENT ON COLUMN trail_generation_jobs.next_retry_at IS 'Timestamp para próxima tentativa';

-- Índices para trail_generation_jobs
CREATE INDEX IF NOT EXISTS idx_generation_jobs_trail ON trail_generation_jobs(trail_id);
CREATE INDEX IF NOT EXISTS idx_generation_jobs_student ON trail_generation_jobs(student_id);
CREATE INDEX IF NOT EXISTS idx_generation_jobs_status ON trail_generation_jobs(status);
CREATE INDEX IF NOT EXISTS idx_generation_jobs_priority ON trail_generation_jobs(priority, queued_at) WHERE status = 'QUEUED';
CREATE INDEX IF NOT EXISTS idx_generation_jobs_retry ON trail_generation_jobs(next_retry_at) WHERE status = 'FAILED' AND attempt_count < max_attempts;

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_generation_jobs_updated_at ON trail_generation_jobs;
CREATE TRIGGER trg_generation_jobs_updated_at
    BEFORE UPDATE ON trail_generation_jobs
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- 2. Tabela: trail_cache (Cache de trilhas geradas)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS trail_cache (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cache_key VARCHAR(40) NOT NULL,
    cache_type VARCHAR(20) NOT NULL,
    data JSONB NOT NULL,
    hit_count INTEGER NOT NULL DEFAULT 0,
    last_hit_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_trail_cache_key UNIQUE (cache_key),
    CONSTRAINT chk_cache_type CHECK (cache_type IN ('trail', 'blueprint', 'content_block', 'preferences', 'progress'))
);

COMMENT ON TABLE trail_cache IS 'Cache de leitura para evitar hits no banco principal. TTL configurável por tipo.';
COMMENT ON COLUMN trail_cache.id IS 'Identificador único do registro de cache';
COMMENT ON COLUMN trail_cache.cache_key IS 'Chave única de cache (hash SHA-1)';
COMMENT ON COLUMN trail_cache.cache_type IS 'Tipo de dado cacheado: trail, blueprint, content_block, preferences, progress';
COMMENT ON COLUMN trail_cache.data IS 'Dados cacheados em JSONB';
COMMENT ON COLUMN trail_cache.hit_count IS 'Contador de hits no cache';
COMMENT ON COLUMN trail_cache.last_hit_at IS 'Timestamp do último hit';
COMMENT ON COLUMN trail_cache.expires_at IS 'Timestamp de expiração do cache';

-- Índices para trail_cache
CREATE INDEX IF NOT EXISTS idx_trail_cache_key ON trail_cache(cache_key);
CREATE INDEX IF NOT EXISTS idx_trail_cache_expires ON trail_cache(expires_at);
CREATE INDEX IF NOT EXISTS idx_trail_cache_type ON trail_cache(cache_type);

-- ----------------------------------------------------------------------------
-- 3. Tabela: curriculum_versions (Versões do currículo)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS curriculum_versions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version VARCHAR(20) NOT NULL,
    description TEXT,
    release_notes TEXT,
    is_current BOOLEAN NOT NULL DEFAULT false,
    published_at TIMESTAMP WITH TIME ZONE,
    deprecated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_curriculum_version UNIQUE (version)
);

-- Índice único parcial para garantir apenas uma versão current
CREATE UNIQUE INDEX IF NOT EXISTS idx_curriculum_current ON curriculum_versions(is_current) WHERE is_current = true;

COMMENT ON TABLE curriculum_versions IS 'Controle de versões do currículo. Trilhas armazenam a versão usada na geração.';
COMMENT ON COLUMN curriculum_versions.id IS 'Identificador único da versão';
COMMENT ON COLUMN curriculum_versions.version IS 'Número da versão em formato semver (1.0.0, 1.1.0, etc.)';
COMMENT ON COLUMN curriculum_versions.description IS 'Descrição da versão';
COMMENT ON COLUMN curriculum_versions.release_notes IS 'Notas de release detalhadas';
COMMENT ON COLUMN curriculum_versions.is_current IS 'Indica se é a versão atual (apenas uma pode ser true)';
COMMENT ON COLUMN curriculum_versions.published_at IS 'Data de publicação da versão';
COMMENT ON COLUMN curriculum_versions.deprecated_at IS 'Data de depreciação (se aplicável)';
COMMENT ON COLUMN curriculum_versions.created_by IS 'Usuário que criou a versão';

-- Índices para curriculum_versions
CREATE INDEX IF NOT EXISTS idx_curriculum_version ON curriculum_versions(version);

-- ----------------------------------------------------------------------------
-- 4. Tabela: trail_audit_logs (Logs de auditoria de trilhas)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS trail_audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trail_id UUID,
    student_id UUID,
    action VARCHAR(50) NOT NULL,
    actor_type VARCHAR(20) NOT NULL,
    actor_id UUID,
    details JSONB,
    previous_state JSONB,
    new_state JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_audit_action CHECK (action IN (
        'created', 'generation_started', 'generation_completed', 'generation_failed',
        'refreshed', 'archived', 'deleted', 'progress_updated', 'module_completed',
        'lesson_completed', 'preferences_changed', 'level_changed'
    )),
    CONSTRAINT chk_audit_actor_type CHECK (actor_type IN ('student', 'teacher', 'system', 'admin', 'worker'))
);

-- Nota: FKs com ON DELETE SET NULL para manter histórico mesmo após exclusão
-- ALTER TABLE trail_audit_logs ADD CONSTRAINT fk_audit_trail FOREIGN KEY (trail_id) REFERENCES trails(id) ON DELETE SET NULL;
-- ALTER TABLE trail_audit_logs ADD CONSTRAINT fk_audit_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE SET NULL;

COMMENT ON TABLE trail_audit_logs IS 'Audit log para compliance e debugging. Retido por 5 anos.';
COMMENT ON COLUMN trail_audit_logs.id IS 'Identificador único do log';
COMMENT ON COLUMN trail_audit_logs.trail_id IS 'Trilha relacionada (NULL se excluída)';
COMMENT ON COLUMN trail_audit_logs.student_id IS 'Estudante relacionado (NULL se excluído)';
COMMENT ON COLUMN trail_audit_logs.action IS 'Ação realizada (created, generation_started, etc.)';
COMMENT ON COLUMN trail_audit_logs.actor_type IS 'Tipo do ator: student, teacher, system, admin, worker';
COMMENT ON COLUMN trail_audit_logs.actor_id IS 'ID do ator que realizou a ação';
COMMENT ON COLUMN trail_audit_logs.details IS 'Detalhes da ação em JSON (parâmetros, motivo, etc.)';
COMMENT ON COLUMN trail_audit_logs.previous_state IS 'Estado anterior para ações de update';
COMMENT ON COLUMN trail_audit_logs.new_state IS 'Novo estado após a ação';
COMMENT ON COLUMN trail_audit_logs.ip_address IS 'Endereço IP do cliente';
COMMENT ON COLUMN trail_audit_logs.user_agent IS 'User-Agent do cliente';

-- Índices para trail_audit_logs
CREATE INDEX IF NOT EXISTS idx_audit_trail ON trail_audit_logs(trail_id);
CREATE INDEX IF NOT EXISTS idx_audit_student ON trail_audit_logs(student_id);
CREATE INDEX IF NOT EXISTS idx_audit_action ON trail_audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_created ON trail_audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_actor ON trail_audit_logs(actor_type, actor_id);

-- ----------------------------------------------------------------------------
-- 5. Tabela: rag_embeddings (Embeddings para RAG)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS rag_embeddings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    source_type VARCHAR(30) NOT NULL,
    source_id UUID NOT NULL,
    chunk_index INTEGER NOT NULL DEFAULT 0,
    content_text TEXT NOT NULL,
    summary TEXT,
    embedding vector(768),
    model VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_rag_source_chunk UNIQUE (source_type, source_id, chunk_index),
    CONSTRAINT chk_rag_source_type CHECK (source_type IN ('content_block', 'descriptor', 'lesson', 'module', 'trail')),
    CONSTRAINT chk_rag_chunk_index CHECK (chunk_index >= 0)
);

COMMENT ON TABLE rag_embeddings IS 'Embeddings para busca semântica (RAG). Usa pgvector para armazenamento local.';
COMMENT ON COLUMN rag_embeddings.id IS 'Identificador único do embedding';
COMMENT ON COLUMN rag_embeddings.source_type IS 'Tipo da fonte: content_block, descriptor, lesson, module, trail';
COMMENT ON COLUMN rag_embeddings.source_id IS 'ID da entidade fonte';
COMMENT ON COLUMN rag_embeddings.chunk_index IS 'Índice do chunk (para conteúdos divididos)';
COMMENT ON COLUMN rag_embeddings.content_text IS 'Texto original que foi embedado';
COMMENT ON COLUMN rag_embeddings.summary IS 'Sumário curto do conteúdo (128-256 tokens)';
COMMENT ON COLUMN rag_embeddings.embedding IS 'Vetor de embedding (768 dimensões para Gemini)';
COMMENT ON COLUMN rag_embeddings.model IS 'Modelo usado para gerar o embedding';

-- Índices para rag_embeddings
CREATE INDEX IF NOT EXISTS idx_rag_source ON rag_embeddings(source_type, source_id);
CREATE INDEX IF NOT EXISTS idx_rag_model ON rag_embeddings(model);

-- Índice vetorial para busca por similaridade (IVFFlat)
CREATE INDEX IF NOT EXISTS idx_rag_embedding_vector ON rag_embeddings
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

-- ============================================================================
-- ROLLBACK SCRIPT (executar na ordem inversa em caso de rollback)
-- ============================================================================
--
-- Para reverter esta migration, execute os comandos abaixo na ordem:
--
-- -- Remover triggers
-- DROP TRIGGER IF EXISTS trg_generation_jobs_updated_at ON trail_generation_jobs;
--
-- -- Remover tabelas (ordem inversa)
-- DROP TABLE IF EXISTS rag_embeddings CASCADE;
-- DROP TABLE IF EXISTS trail_audit_logs CASCADE;
-- DROP TABLE IF EXISTS curriculum_versions CASCADE;
-- DROP TABLE IF EXISTS trail_cache CASCADE;
-- DROP TABLE IF EXISTS trail_generation_jobs CASCADE;
--
-- ============================================================================
