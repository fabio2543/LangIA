-- ============================================================================
-- Migration V027: Sistema SRS (Spaced Repetition System)
-- ============================================================================
-- Implementa o algoritmo SM-2 (SuperMemo 2) para repetição espaçada
-- Inclui vocabulary_cards e srs_progress
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Tabela: vocabulary_cards (Flashcards de vocabulário)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS vocabulary_cards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    language_code VARCHAR(10) NOT NULL,
    cefr_level VARCHAR(2) NOT NULL,
    card_type VARCHAR(20) NOT NULL DEFAULT 'word',
    front TEXT NOT NULL,
    back TEXT NOT NULL,
    context TEXT,
    example_sentence TEXT,
    audio_url TEXT,
    image_url TEXT,
    tags JSONB DEFAULT '[]',
    source_lesson_id UUID,
    source_chunk_id UUID,
    is_system_card BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_vocab_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_vocab_language FOREIGN KEY (language_code)
        REFERENCES languages(code) ON DELETE CASCADE,
    CONSTRAINT fk_vocab_chunk FOREIGN KEY (source_chunk_id)
        REFERENCES linguistic_chunks(id) ON DELETE SET NULL,
    CONSTRAINT chk_vocab_level CHECK (cefr_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    CONSTRAINT chk_vocab_type CHECK (card_type IN ('word', 'chunk', 'phrase', 'grammar', 'expression'))
);

COMMENT ON TABLE vocabulary_cards IS 'Flashcards de vocabulário para revisão SRS. user_id NULL indica card do sistema.';
COMMENT ON COLUMN vocabulary_cards.id IS 'Identificador único do card';
COMMENT ON COLUMN vocabulary_cards.user_id IS 'Usuário dono do card (NULL = card do sistema)';
COMMENT ON COLUMN vocabulary_cards.language_code IS 'Código do idioma';
COMMENT ON COLUMN vocabulary_cards.cefr_level IS 'Nível CEFR do vocabulário';
COMMENT ON COLUMN vocabulary_cards.card_type IS 'Tipo: word, chunk, phrase, grammar, expression';
COMMENT ON COLUMN vocabulary_cards.front IS 'Frente do card (pergunta/palavra no idioma alvo)';
COMMENT ON COLUMN vocabulary_cards.back IS 'Verso do card (resposta/tradução)';
COMMENT ON COLUMN vocabulary_cards.context IS 'Contexto de uso';
COMMENT ON COLUMN vocabulary_cards.example_sentence IS 'Frase de exemplo';
COMMENT ON COLUMN vocabulary_cards.audio_url IS 'URL do áudio de pronúncia';
COMMENT ON COLUMN vocabulary_cards.image_url IS 'URL de imagem ilustrativa';
COMMENT ON COLUMN vocabulary_cards.tags IS 'Tags categorizando o card (JSON array)';
COMMENT ON COLUMN vocabulary_cards.source_lesson_id IS 'Lição de origem do card';
COMMENT ON COLUMN vocabulary_cards.source_chunk_id IS 'Chunk de origem (se aplicável)';
COMMENT ON COLUMN vocabulary_cards.is_system_card IS 'Se é um card do sistema (não editável pelo usuário)';
COMMENT ON COLUMN vocabulary_cards.is_active IS 'Se o card está ativo para revisão';

-- Índices para vocabulary_cards
CREATE INDEX IF NOT EXISTS idx_vocab_user_language ON vocabulary_cards(user_id, language_code);
CREATE INDEX IF NOT EXISTS idx_vocab_level ON vocabulary_cards(cefr_level);
CREATE INDEX IF NOT EXISTS idx_vocab_type ON vocabulary_cards(card_type);
CREATE INDEX IF NOT EXISTS idx_vocab_system ON vocabulary_cards(is_system_card) WHERE is_system_card = true;
CREATE INDEX IF NOT EXISTS idx_vocab_active ON vocabulary_cards(is_active) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_vocab_chunk ON vocabulary_cards(source_chunk_id);

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_vocab_updated_at ON vocabulary_cards;
CREATE TRIGGER trg_vocab_updated_at
    BEFORE UPDATE ON vocabulary_cards
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- 2. Tabela: srs_progress (Progresso de repetição espaçada)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS srs_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    card_id UUID NOT NULL,
    easiness_factor DECIMAL(4,2) DEFAULT 2.50,
    interval_days INTEGER DEFAULT 1,
    repetitions INTEGER DEFAULT 0,
    next_review_date DATE NOT NULL,
    last_reviewed_at TIMESTAMP WITH TIME ZONE,
    last_quality INTEGER,
    total_reviews INTEGER DEFAULT 0,
    correct_reviews INTEGER DEFAULT 0,
    streak INTEGER DEFAULT 0,
    lapses INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'new',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_srs_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_srs_card FOREIGN KEY (card_id)
        REFERENCES vocabulary_cards(id) ON DELETE CASCADE,
    CONSTRAINT uq_srs_user_card UNIQUE (user_id, card_id),
    CONSTRAINT chk_srs_ef CHECK (easiness_factor >= 1.30),
    CONSTRAINT chk_srs_quality CHECK (last_quality IS NULL OR (last_quality >= 0 AND last_quality <= 5)),
    CONSTRAINT chk_srs_status CHECK (status IN ('new', 'learning', 'review', 'relearning', 'suspended'))
);

COMMENT ON TABLE srs_progress IS 'Progresso de repetição espaçada usando algoritmo SM-2';
COMMENT ON COLUMN srs_progress.id IS 'Identificador único';
COMMENT ON COLUMN srs_progress.user_id IS 'Usuário';
COMMENT ON COLUMN srs_progress.card_id IS 'Card de vocabulário';
COMMENT ON COLUMN srs_progress.easiness_factor IS 'Fator de facilidade SM-2 (mín 1.3, padrão 2.5)';
COMMENT ON COLUMN srs_progress.interval_days IS 'Intervalo até próxima revisão em dias';
COMMENT ON COLUMN srs_progress.repetitions IS 'Número de repetições bem-sucedidas consecutivas';
COMMENT ON COLUMN srs_progress.next_review_date IS 'Data da próxima revisão';
COMMENT ON COLUMN srs_progress.last_reviewed_at IS 'Timestamp da última revisão';
COMMENT ON COLUMN srs_progress.last_quality IS 'Qualidade da última resposta (0-5): 0=blackout, 3=difícil, 5=perfeito';
COMMENT ON COLUMN srs_progress.total_reviews IS 'Total de revisões feitas';
COMMENT ON COLUMN srs_progress.correct_reviews IS 'Revisões corretas (quality >= 3)';
COMMENT ON COLUMN srs_progress.streak IS 'Sequência de acertos consecutivos';
COMMENT ON COLUMN srs_progress.lapses IS 'Número de vezes que esqueceu (quality < 3 após aprender)';
COMMENT ON COLUMN srs_progress.status IS 'Status: new, learning, review, relearning, suspended';

-- Índices para srs_progress
CREATE INDEX IF NOT EXISTS idx_srs_user ON srs_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_srs_next_review ON srs_progress(user_id, next_review_date);
CREATE INDEX IF NOT EXISTS idx_srs_status ON srs_progress(user_id, status);
CREATE INDEX IF NOT EXISTS idx_srs_due_today ON srs_progress(user_id, next_review_date)
    WHERE next_review_date <= CURRENT_DATE;

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_srs_updated_at ON srs_progress;
CREATE TRIGGER trg_srs_updated_at
    BEFORE UPDATE ON srs_progress
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- 3. Função: Calcular próxima revisão (Algoritmo SM-2)
-- ----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION calculate_srs_next_review(
    p_quality INTEGER,
    p_easiness_factor DECIMAL,
    p_interval INTEGER,
    p_repetitions INTEGER
)
RETURNS TABLE (
    new_ef DECIMAL,
    new_interval INTEGER,
    new_repetitions INTEGER
) AS $$
DECLARE
    v_ef DECIMAL;
    v_interval INTEGER;
    v_repetitions INTEGER;
BEGIN
    -- Algoritmo SM-2:
    -- EF' = EF + (0.1 - (5-q) * (0.08 + (5-q) * 0.02))
    -- onde q é a qualidade (0-5)

    v_ef := p_easiness_factor + (0.1 - (5 - p_quality) * (0.08 + (5 - p_quality) * 0.02));

    -- EF mínimo é 1.3
    IF v_ef < 1.3 THEN
        v_ef := 1.3;
    END IF;

    -- Se qualidade >= 3 (resposta correta)
    IF p_quality >= 3 THEN
        IF p_repetitions = 0 THEN
            v_interval := 1;
        ELSIF p_repetitions = 1 THEN
            v_interval := 6;
        ELSE
            v_interval := ROUND(p_interval * v_ef);
        END IF;
        v_repetitions := p_repetitions + 1;
    ELSE
        -- Resposta incorreta: reiniciar
        v_interval := 1;
        v_repetitions := 0;
    END IF;

    RETURN QUERY SELECT v_ef, v_interval, v_repetitions;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION calculate_srs_next_review IS 'Calcula próxima revisão usando algoritmo SM-2';

-- ----------------------------------------------------------------------------
-- 4. View: Cards para revisão hoje
-- ----------------------------------------------------------------------------

CREATE OR REPLACE VIEW srs_cards_due_today AS
SELECT
    sp.id AS srs_id,
    sp.user_id,
    vc.id AS card_id,
    vc.language_code,
    vc.cefr_level,
    vc.card_type,
    vc.front,
    vc.back,
    vc.context,
    vc.example_sentence,
    vc.audio_url,
    sp.easiness_factor,
    sp.interval_days,
    sp.repetitions,
    sp.next_review_date,
    sp.total_reviews,
    sp.streak,
    sp.status
FROM srs_progress sp
JOIN vocabulary_cards vc ON sp.card_id = vc.id
WHERE sp.next_review_date <= CURRENT_DATE
  AND vc.is_active = true
  AND sp.status != 'suspended'
ORDER BY sp.next_review_date, sp.easiness_factor;

COMMENT ON VIEW srs_cards_due_today IS 'Cards de vocabulário pendentes de revisão para hoje';

-- ============================================================================
-- SEED DATA: Vocabulary Cards A1 de Inglês (Sistema)
-- ============================================================================

-- Criar cards do sistema baseados nos chunks
INSERT INTO vocabulary_cards (user_id, language_code, cefr_level, card_type, front, back, context, is_system_card, source_chunk_id)
SELECT
    NULL,
    lc.language_code,
    lc.cefr_level,
    'chunk',
    lc.chunk_text,
    lc.translation,
    lc.usage_context,
    true,
    lc.id
FROM linguistic_chunks lc
WHERE lc.is_core = true
ON CONFLICT DO NOTHING;

-- ============================================================================
-- ROLLBACK SCRIPT
-- ============================================================================
--
-- Para reverter esta migration:
--
-- DROP VIEW IF EXISTS srs_cards_due_today;
-- DROP FUNCTION IF EXISTS calculate_srs_next_review;
-- DROP TRIGGER IF EXISTS trg_srs_updated_at ON srs_progress;
-- DROP TRIGGER IF EXISTS trg_vocab_updated_at ON vocabulary_cards;
-- DROP TABLE IF EXISTS srs_progress CASCADE;
-- DROP TABLE IF EXISTS vocabulary_cards CASCADE;
--
-- ============================================================================
