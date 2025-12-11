-- ============================================================================
-- Migration V026: Tabelas de Chunks Linguísticos
-- ============================================================================
-- Chunks são padrões linguísticos reutilizáveis como "I'd like...", "Can I have...?"
-- Essenciais para o método de aprendizado baseado em padrões (A1)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Tabela: linguistic_chunks (Padrões linguísticos)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS linguistic_chunks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    language_code VARCHAR(10) NOT NULL,
    cefr_level VARCHAR(2) NOT NULL,
    chunk_text TEXT NOT NULL,
    translation TEXT,
    category VARCHAR(50) NOT NULL,
    usage_context TEXT,
    variations JSONB DEFAULT '[]',
    audio_url TEXT,
    difficulty_score DECIMAL(3,2) DEFAULT 0.50,
    is_core BOOLEAN DEFAULT true,
    order_index INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_chunks_language FOREIGN KEY (language_code)
        REFERENCES languages(code) ON DELETE CASCADE,
    CONSTRAINT chk_chunks_level CHECK (cefr_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    CONSTRAINT chk_chunks_category CHECK (category IN (
        'greeting', 'farewell', 'request', 'question', 'direction',
        'shopping', 'restaurant', 'travel', 'introduction', 'time',
        'weather', 'opinion', 'agreement', 'disagreement', 'apology',
        'gratitude', 'emergency', 'phone', 'email', 'other'
    )),
    CONSTRAINT chk_chunks_difficulty CHECK (difficulty_score >= 0.00 AND difficulty_score <= 1.00)
);

COMMENT ON TABLE linguistic_chunks IS 'Padrões linguísticos reutilizáveis (chunks) organizados por nível CEFR';
COMMENT ON COLUMN linguistic_chunks.id IS 'Identificador único do chunk';
COMMENT ON COLUMN linguistic_chunks.language_code IS 'Código do idioma (en, es, fr, etc.)';
COMMENT ON COLUMN linguistic_chunks.cefr_level IS 'Nível CEFR do chunk (A1, A2, B1, B2, C1, C2)';
COMMENT ON COLUMN linguistic_chunks.chunk_text IS 'Texto do padrão linguístico (ex: "I would like...")';
COMMENT ON COLUMN linguistic_chunks.translation IS 'Tradução para português';
COMMENT ON COLUMN linguistic_chunks.category IS 'Categoria funcional do chunk';
COMMENT ON COLUMN linguistic_chunks.usage_context IS 'Contexto de uso (ex: "restaurantes, lojas")';
COMMENT ON COLUMN linguistic_chunks.variations IS 'Variações do chunk em JSON (ex: ["I''d like...", "I want..."])';
COMMENT ON COLUMN linguistic_chunks.audio_url IS 'URL do áudio de pronúncia';
COMMENT ON COLUMN linguistic_chunks.difficulty_score IS 'Pontuação de dificuldade (0.00 a 1.00)';
COMMENT ON COLUMN linguistic_chunks.is_core IS 'Se é um chunk essencial (obrigatório) para o nível';
COMMENT ON COLUMN linguistic_chunks.order_index IS 'Ordem de apresentação no nível';

-- Índices para linguistic_chunks
CREATE INDEX IF NOT EXISTS idx_chunks_language_level ON linguistic_chunks(language_code, cefr_level);
CREATE INDEX IF NOT EXISTS idx_chunks_category ON linguistic_chunks(category);
CREATE INDEX IF NOT EXISTS idx_chunks_core ON linguistic_chunks(is_core) WHERE is_core = true;
CREATE INDEX IF NOT EXISTS idx_chunks_order ON linguistic_chunks(language_code, cefr_level, order_index);

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_chunks_updated_at ON linguistic_chunks;
CREATE TRIGGER trg_chunks_updated_at
    BEFORE UPDATE ON linguistic_chunks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- 2. Tabela: chunk_mastery (Domínio de chunks por usuário)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS chunk_mastery (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    chunk_id UUID NOT NULL,
    mastery_level INTEGER DEFAULT 0,
    times_practiced INTEGER DEFAULT 0,
    times_correct INTEGER DEFAULT 0,
    last_practiced_at TIMESTAMP WITH TIME ZONE,
    contexts_used JSONB DEFAULT '[]',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_chunk_mastery_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_chunk_mastery_chunk FOREIGN KEY (chunk_id)
        REFERENCES linguistic_chunks(id) ON DELETE CASCADE,
    CONSTRAINT uq_chunk_mastery UNIQUE (user_id, chunk_id),
    CONSTRAINT chk_mastery_level CHECK (mastery_level BETWEEN 0 AND 5)
);

COMMENT ON TABLE chunk_mastery IS 'Rastreia o domínio de cada chunk por usuário';
COMMENT ON COLUMN chunk_mastery.id IS 'Identificador único do registro';
COMMENT ON COLUMN chunk_mastery.user_id IS 'Usuário que está aprendendo o chunk';
COMMENT ON COLUMN chunk_mastery.chunk_id IS 'Chunk sendo aprendido';
COMMENT ON COLUMN chunk_mastery.mastery_level IS 'Nível de domínio (0=não visto, 1=iniciando, 2=praticando, 3=familiar, 4=confiante, 5=dominado)';
COMMENT ON COLUMN chunk_mastery.times_practiced IS 'Quantas vezes o chunk foi praticado';
COMMENT ON COLUMN chunk_mastery.times_correct IS 'Quantas vezes acertou ao usar o chunk';
COMMENT ON COLUMN chunk_mastery.last_practiced_at IS 'Última vez que praticou o chunk';
COMMENT ON COLUMN chunk_mastery.contexts_used IS 'Contextos onde o chunk foi usado (JSON array)';
COMMENT ON COLUMN chunk_mastery.notes IS 'Notas pessoais do aluno sobre o chunk';

-- Índices para chunk_mastery
CREATE INDEX IF NOT EXISTS idx_chunk_mastery_user ON chunk_mastery(user_id);
CREATE INDEX IF NOT EXISTS idx_chunk_mastery_chunk ON chunk_mastery(chunk_id);
CREATE INDEX IF NOT EXISTS idx_chunk_mastery_level ON chunk_mastery(user_id, mastery_level);
CREATE INDEX IF NOT EXISTS idx_chunk_mastery_practiced ON chunk_mastery(last_practiced_at DESC);

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_chunk_mastery_updated_at ON chunk_mastery;
CREATE TRIGGER trg_chunk_mastery_updated_at
    BEFORE UPDATE ON chunk_mastery
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- SEED DATA: Chunks A1 de Inglês (Essenciais)
-- ============================================================================

INSERT INTO linguistic_chunks (language_code, cefr_level, chunk_text, translation, category, usage_context, variations, is_core, order_index)
VALUES
    -- Greetings (Saudações)
    ('en', 'A1', 'Hello!', 'Olá!', 'greeting', 'Saudação informal', '["Hi!", "Hey!"]', true, 1),
    ('en', 'A1', 'Good morning!', 'Bom dia!', 'greeting', 'Saudação matinal', '["Morning!"]', true, 2),
    ('en', 'A1', 'Good afternoon!', 'Boa tarde!', 'greeting', 'Saudação vespertina', '[]', true, 3),
    ('en', 'A1', 'Good evening!', 'Boa noite!', 'greeting', 'Saudação noturna', '[]', true, 4),
    ('en', 'A1', 'How are you?', 'Como você está?', 'greeting', 'Pergunta sobre bem-estar', '["How are you doing?", "How''s it going?"]', true, 5),
    ('en', 'A1', 'Nice to meet you!', 'Prazer em conhecê-lo!', 'introduction', 'Apresentação inicial', '["Pleased to meet you!"]', true, 6),

    -- Introductions (Apresentações)
    ('en', 'A1', 'My name is...', 'Meu nome é...', 'introduction', 'Apresentação pessoal', '["I''m...", "I am..."]', true, 7),
    ('en', 'A1', 'I am from...', 'Eu sou de...', 'introduction', 'Dizer origem', '["I come from..."]', true, 8),
    ('en', 'A1', 'I live in...', 'Eu moro em...', 'introduction', 'Dizer onde mora', '[]', true, 9),
    ('en', 'A1', 'I work as a...', 'Eu trabalho como...', 'introduction', 'Dizer profissão', '["I''m a..."]', true, 10),

    -- Requests (Pedidos)
    ('en', 'A1', 'I would like...', 'Eu gostaria de...', 'request', 'Fazer pedidos educados', '["I''d like..."]', true, 11),
    ('en', 'A1', 'Can I have...?', 'Posso ter...?', 'request', 'Pedir algo', '["Could I have...?", "May I have...?"]', true, 12),
    ('en', 'A1', 'Please...', 'Por favor...', 'request', 'Adicionar educação ao pedido', '[]', true, 13),
    ('en', 'A1', 'Thank you!', 'Obrigado(a)!', 'gratitude', 'Agradecer', '["Thanks!", "Thank you very much!"]', true, 14),
    ('en', 'A1', 'You''re welcome!', 'De nada!', 'gratitude', 'Responder agradecimento', '["No problem!", "My pleasure!"]', true, 15),

    -- Questions (Perguntas)
    ('en', 'A1', 'Where is...?', 'Onde fica...?', 'direction', 'Perguntar localização', '["Where''s...?"]', true, 16),
    ('en', 'A1', 'How much is...?', 'Quanto custa...?', 'shopping', 'Perguntar preço', '["How much does... cost?"]', true, 17),
    ('en', 'A1', 'What time is it?', 'Que horas são?', 'time', 'Perguntar hora', '["Do you have the time?"]', true, 18),
    ('en', 'A1', 'What is this?', 'O que é isto?', 'question', 'Perguntar sobre objeto', '["What''s this?"]', true, 19),
    ('en', 'A1', 'Do you speak English?', 'Você fala inglês?', 'question', 'Verificar idioma', '[]', true, 20),

    -- Directions (Direções)
    ('en', 'A1', 'Turn left', 'Vire à esquerda', 'direction', 'Dar direção', '["Go left"]', true, 21),
    ('en', 'A1', 'Turn right', 'Vire à direita', 'direction', 'Dar direção', '["Go right"]', true, 22),
    ('en', 'A1', 'Go straight', 'Siga em frente', 'direction', 'Dar direção', '["Go straight ahead"]', true, 23),
    ('en', 'A1', 'It''s next to...', 'Fica ao lado de...', 'direction', 'Indicar posição', '["It''s beside..."]', true, 24),

    -- Restaurant/Shopping
    ('en', 'A1', 'The check, please.', 'A conta, por favor.', 'restaurant', 'Pedir conta no restaurante', '["The bill, please."]', true, 25),
    ('en', 'A1', 'I''ll take this one.', 'Vou levar este.', 'shopping', 'Decidir compra', '["I''ll have this one."]', true, 26),
    ('en', 'A1', 'Do you accept credit cards?', 'Vocês aceitam cartão?', 'shopping', 'Perguntar forma de pagamento', '["Can I pay by card?"]', true, 27),

    -- Apologies/Emergencies
    ('en', 'A1', 'I''m sorry.', 'Desculpe.', 'apology', 'Pedir desculpas', '["Sorry!", "Excuse me."]', true, 28),
    ('en', 'A1', 'I don''t understand.', 'Eu não entendo.', 'question', 'Expressar incompreensão', '["I don''t get it."]', true, 29),
    ('en', 'A1', 'Could you repeat, please?', 'Poderia repetir, por favor?', 'request', 'Pedir repetição', '["Can you say that again?"]', true, 30),
    ('en', 'A1', 'Help!', 'Socorro!', 'emergency', 'Pedir ajuda urgente', '["I need help!"]', true, 31),

    -- Farewells (Despedidas)
    ('en', 'A1', 'Goodbye!', 'Tchau!', 'farewell', 'Despedida formal', '["Bye!", "Bye-bye!"]', true, 32),
    ('en', 'A1', 'See you later!', 'Até mais!', 'farewell', 'Despedida informal', '["See you!", "See ya!"]', true, 33),
    ('en', 'A1', 'Have a nice day!', 'Tenha um bom dia!', 'farewell', 'Despedida amigável', '["Have a good one!"]', true, 34),
    ('en', 'A1', 'Good night!', 'Boa noite!', 'farewell', 'Despedida noturna', '["Night!"]', true, 35)

ON CONFLICT DO NOTHING;

-- ============================================================================
-- ROLLBACK SCRIPT
-- ============================================================================
--
-- Para reverter esta migration:
--
-- DROP TRIGGER IF EXISTS trg_chunk_mastery_updated_at ON chunk_mastery;
-- DROP TRIGGER IF EXISTS trg_chunks_updated_at ON linguistic_chunks;
-- DROP TABLE IF EXISTS chunk_mastery CASCADE;
-- DROP TABLE IF EXISTS linguistic_chunks CASCADE;
--
-- ============================================================================
