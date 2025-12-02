-- Habilita extensão pgvector
CREATE EXTENSION IF NOT EXISTS vector;

-- Tabela para armazenar documentos e embeddings
CREATE TABLE document_embeddings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content TEXT NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    metadata JSONB,
    embedding vector(768),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Índice para busca por similaridade (IVFFlat - bom até ~1M registros)
CREATE INDEX idx_document_embeddings_vector
ON document_embeddings
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);

-- Índice para filtrar por tipo de conteúdo
CREATE INDEX idx_document_embeddings_type ON document_embeddings(content_type);

-- Índice para busca em metadados
CREATE INDEX idx_document_embeddings_metadata ON document_embeddings USING gin(metadata);
