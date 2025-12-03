-- ============================================================================
-- Migration V015: Schema e extensões para Módulo de Geração de Trilha On-Demand
-- ============================================================================
-- Este módulo permite a geração dinâmica de trilhas de aprendizado personalizadas
-- para cada estudante, baseadas em seu nível, preferências e objetivos.
--
-- Organização: Todas as tabelas do módulo de trilhas usam o schema 'public'
-- com prefixo 'trail_' para fácil identificação e agrupamento.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Habilitar extensões necessárias
-- ----------------------------------------------------------------------------

-- uuid-ossp: Permite geração de UUIDs v4 para chaves primárias
-- Utilizado para identificadores únicos de trilhas, módulos e lições
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- pgcrypto: Funções criptográficas para hash e geração segura de dados
-- Utilizado para checksums de conteúdo e tokens de cache
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ----------------------------------------------------------------------------
-- 2. Criar ENUM types para o módulo de Trilhas
-- ----------------------------------------------------------------------------

-- Trail Status: Estados possíveis de uma trilha de aprendizado
-- GENERATING: Trilha está sendo gerada pela IA (processo em andamento)
-- PARTIAL: Alguns módulos prontos, outros ainda em geração (permite acesso parcial)
-- READY: Trilha completamente gerada e disponível para o estudante
-- ARCHIVED: Trilha arquivada (substituída por versão mais recente ou inativa)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'trail_status') THEN
        CREATE TYPE trail_status AS ENUM (
            'GENERATING',
            'PARTIAL',
            'READY',
            'ARCHIVED'
        );
        COMMENT ON TYPE trail_status IS 'Estados possíveis de uma trilha de aprendizado: GENERATING (em geração), PARTIAL (parcialmente pronta), READY (completa), ARCHIVED (arquivada)';
    END IF;
END$$;

-- Module Status: Estados de um módulo dentro da trilha
-- PENDING: Módulo aguardando geração de conteúdo
-- READY: Módulo com todas as lições geradas e disponíveis
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'module_status') THEN
        CREATE TYPE module_status AS ENUM (
            'PENDING',
            'READY'
        );
        COMMENT ON TYPE module_status IS 'Estados de um módulo: PENDING (aguardando geração), READY (conteúdo disponível)';
    END IF;
END$$;

-- Lesson Type: Tipos de lição disponíveis na plataforma
-- interactive: Lição interativa com exercícios dinâmicos
-- video: Conteúdo em vídeo (aulas gravadas ou geradas)
-- reading: Material de leitura (textos, artigos)
-- exercise: Exercícios práticos de gramática/vocabulário
-- conversation: Prática de conversação (com IA ou pares)
-- flashcard: Cartões de memorização (vocabulário, frases)
-- game: Jogos educativos e gamificação
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'lesson_type') THEN
        CREATE TYPE lesson_type AS ENUM (
            'interactive',
            'video',
            'reading',
            'exercise',
            'conversation',
            'flashcard',
            'game'
        );
        COMMENT ON TYPE lesson_type IS 'Tipos de lição: interactive, video, reading, exercise, conversation, flashcard, game';
    END IF;
END$$;

-- Generation Job Status: Estados do job de geração assíncrona
-- QUEUED: Job na fila aguardando processamento
-- PROCESSING: Job sendo processado (IA gerando conteúdo)
-- COMPLETED: Job finalizado com sucesso
-- FAILED: Job falhou (erro na geração, timeout, etc.)
-- CANCELLED: Job cancelado pelo usuário ou sistema
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'generation_job_status') THEN
        CREATE TYPE generation_job_status AS ENUM (
            'QUEUED',
            'PROCESSING',
            'COMPLETED',
            'FAILED',
            'CANCELLED'
        );
        COMMENT ON TYPE generation_job_status IS 'Estados do job de geração: QUEUED, PROCESSING, COMPLETED, FAILED, CANCELLED';
    END IF;
END$$;

-- Refresh Reason: Motivos para regeneração de trilha
-- level_change: Nível do estudante mudou (subiu/desceu)
-- preferences_update: Preferências de aprendizado alteradas
-- curriculum_update: Currículo base foi atualizado pela plataforma
-- manual_request: Estudante solicitou nova trilha manualmente
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'refresh_reason') THEN
        CREATE TYPE refresh_reason AS ENUM (
            'level_change',
            'preferences_update',
            'curriculum_update',
            'manual_request'
        );
        COMMENT ON TYPE refresh_reason IS 'Motivos para refresh de trilha: level_change, preferences_update, curriculum_update, manual_request';
    END IF;
END$$;

-- ============================================================================
-- ROLLBACK SCRIPT (executar na ordem inversa em caso de rollback)
-- ============================================================================
--
-- Para reverter esta migration, execute os comandos abaixo na ordem:
--
-- DROP TYPE IF EXISTS refresh_reason CASCADE;
-- DROP TYPE IF EXISTS generation_job_status CASCADE;
-- DROP TYPE IF EXISTS lesson_type CASCADE;
-- DROP TYPE IF EXISTS module_status CASCADE;
-- DROP TYPE IF EXISTS trail_status CASCADE;
--
-- Nota: As extensões uuid-ossp e pgcrypto são compartilhadas e não devem
-- ser removidas pois podem estar em uso por outras partes do sistema.
-- ============================================================================
