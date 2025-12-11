-- ============================================================================
-- Migration V031: Sistema de Interações Socráticas
-- ============================================================================
-- Registra interações com IA usando método socrático (correção por perguntas)
-- Essencial para "IA deve corrigir com perguntas, não com respostas diretas"
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Tabela: socratic_interactions (Interações pedagógicas socráticas)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS socratic_interactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    lesson_id UUID,
    exercise_id UUID,
    skill_type VARCHAR(20),
    interaction_type VARCHAR(50) NOT NULL DEFAULT 'correction',

    -- Fluxo da interação
    user_input TEXT NOT NULL,
    ai_question TEXT NOT NULL,
    user_reflection TEXT,
    ai_follow_up TEXT,
    user_second_attempt TEXT,
    final_correction TEXT,

    -- Resultado pedagógico
    learning_moment TEXT,
    concepts_addressed JSONB DEFAULT '[]',
    user_understood BOOLEAN,
    self_correction_achieved BOOLEAN,

    -- Métricas
    interaction_rounds INTEGER DEFAULT 1,
    total_time_seconds INTEGER,
    tokens_used INTEGER,
    model_used VARCHAR(50),

    -- Avaliação
    user_rating INTEGER,
    was_helpful BOOLEAN,
    feedback_notes TEXT,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_socratic_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_socratic_language FOREIGN KEY (language_code)
        REFERENCES languages(code) ON DELETE CASCADE,
    CONSTRAINT chk_socratic_skill CHECK (skill_type IS NULL OR skill_type IN (
        'listening', 'speaking', 'reading', 'writing',
        'grammar', 'vocabulary', 'pronunciation'
    )),
    CONSTRAINT chk_socratic_type CHECK (interaction_type IN (
        'correction', 'explanation', 'practice', 'role_play',
        'conversation', 'feedback', 'guidance'
    )),
    CONSTRAINT chk_socratic_rating CHECK (user_rating IS NULL OR (user_rating >= 1 AND user_rating <= 5))
);

COMMENT ON TABLE socratic_interactions IS 'Registro de interações pedagógicas usando método socrático';
COMMENT ON COLUMN socratic_interactions.id IS 'Identificador único';
COMMENT ON COLUMN socratic_interactions.user_id IS 'Usuário';
COMMENT ON COLUMN socratic_interactions.language_code IS 'Idioma da interação';
COMMENT ON COLUMN socratic_interactions.lesson_id IS 'Lição relacionada (se aplicável)';
COMMENT ON COLUMN socratic_interactions.exercise_id IS 'Exercício relacionado (se aplicável)';
COMMENT ON COLUMN socratic_interactions.skill_type IS 'Habilidade sendo trabalhada';
COMMENT ON COLUMN socratic_interactions.interaction_type IS 'Tipo de interação: correction, explanation, practice, role_play, conversation, feedback, guidance';
COMMENT ON COLUMN socratic_interactions.user_input IS 'Input inicial do usuário (resposta ou pergunta)';
COMMENT ON COLUMN socratic_interactions.ai_question IS 'Pergunta guiada da IA (ao invés de correção direta)';
COMMENT ON COLUMN socratic_interactions.user_reflection IS 'Reflexão/resposta do usuário à pergunta';
COMMENT ON COLUMN socratic_interactions.ai_follow_up IS 'Segunda pergunta da IA (se necessário)';
COMMENT ON COLUMN socratic_interactions.user_second_attempt IS 'Segunda tentativa do usuário';
COMMENT ON COLUMN socratic_interactions.final_correction IS 'Correção final (se o aluno não chegou sozinho)';
COMMENT ON COLUMN socratic_interactions.learning_moment IS 'O que o aluno deveria ter aprendido';
COMMENT ON COLUMN socratic_interactions.concepts_addressed IS 'Conceitos abordados (JSON array)';
COMMENT ON COLUMN socratic_interactions.user_understood IS 'Se o usuário demonstrou compreensão';
COMMENT ON COLUMN socratic_interactions.self_correction_achieved IS 'Se o aluno conseguiu se auto-corrigir';
COMMENT ON COLUMN socratic_interactions.interaction_rounds IS 'Número de rodadas de perguntas';
COMMENT ON COLUMN socratic_interactions.total_time_seconds IS 'Tempo total da interação';
COMMENT ON COLUMN socratic_interactions.tokens_used IS 'Tokens LLM consumidos';
COMMENT ON COLUMN socratic_interactions.model_used IS 'Modelo de IA usado';
COMMENT ON COLUMN socratic_interactions.user_rating IS 'Avaliação do usuário (1-5 estrelas)';
COMMENT ON COLUMN socratic_interactions.was_helpful IS 'Se o usuário achou útil';
COMMENT ON COLUMN socratic_interactions.feedback_notes IS 'Feedback textual do usuário';

-- Índices para socratic_interactions
CREATE INDEX IF NOT EXISTS idx_socratic_user ON socratic_interactions(user_id);
CREATE INDEX IF NOT EXISTS idx_socratic_user_date ON socratic_interactions(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_socratic_lesson ON socratic_interactions(lesson_id);
CREATE INDEX IF NOT EXISTS idx_socratic_skill ON socratic_interactions(user_id, skill_type);
CREATE INDEX IF NOT EXISTS idx_socratic_type ON socratic_interactions(interaction_type);
CREATE INDEX IF NOT EXISTS idx_socratic_self_correction ON socratic_interactions(self_correction_achieved)
    WHERE self_correction_achieved = true;

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_socratic_updated_at ON socratic_interactions;
CREATE TRIGGER trg_socratic_updated_at
    BEFORE UPDATE ON socratic_interactions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- 2. View: Taxa de auto-correção por usuário
-- ----------------------------------------------------------------------------

CREATE OR REPLACE VIEW self_correction_rate AS
SELECT
    user_id,
    language_code,
    skill_type,
    COUNT(*) AS total_interactions,
    COUNT(*) FILTER (WHERE self_correction_achieved = true) AS self_corrections,
    ROUND(
        (COUNT(*) FILTER (WHERE self_correction_achieved = true)::DECIMAL / COUNT(*)) * 100,
        2
    ) AS self_correction_rate,
    ROUND(AVG(interaction_rounds), 2) AS avg_rounds_needed,
    ROUND(AVG(user_rating), 2) AS avg_rating
FROM socratic_interactions
WHERE created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY user_id, language_code, skill_type
ORDER BY self_correction_rate DESC;

COMMENT ON VIEW self_correction_rate IS 'Taxa de auto-correção por usuário, idioma e habilidade (últimos 30 dias)';

-- ----------------------------------------------------------------------------
-- 3. View: Conceitos mais trabalhados (para personalização)
-- ----------------------------------------------------------------------------

CREATE OR REPLACE VIEW most_addressed_concepts AS
SELECT
    user_id,
    language_code,
    concept,
    COUNT(*) AS times_addressed,
    COUNT(*) FILTER (WHERE user_understood = true) AS times_understood,
    ROUND(
        (COUNT(*) FILTER (WHERE user_understood = true)::DECIMAL / COUNT(*)) * 100,
        2
    ) AS understanding_rate
FROM socratic_interactions,
     LATERAL jsonb_array_elements_text(concepts_addressed) AS concept
WHERE created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY user_id, language_code, concept
HAVING COUNT(*) >= 2
ORDER BY times_addressed DESC;

COMMENT ON VIEW most_addressed_concepts IS 'Conceitos mais trabalhados nas interações socráticas';

-- ----------------------------------------------------------------------------
-- 4. Tabela: socratic_templates (Templates de perguntas socráticas)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS socratic_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    language_code VARCHAR(10) NOT NULL,
    skill_type VARCHAR(20) NOT NULL,
    error_category VARCHAR(100) NOT NULL,
    template_type VARCHAR(50) NOT NULL DEFAULT 'question',
    template_text TEXT NOT NULL,
    follow_up_text TEXT,
    hint_text TEXT,
    explanation_text TEXT,
    difficulty_level INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT true,
    usage_count INTEGER DEFAULT 0,
    success_rate DECIMAL(5,2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_templates_language FOREIGN KEY (language_code)
        REFERENCES languages(code) ON DELETE CASCADE,
    CONSTRAINT chk_templates_skill CHECK (skill_type IN (
        'listening', 'speaking', 'reading', 'writing',
        'grammar', 'vocabulary', 'pronunciation', 'general'
    )),
    CONSTRAINT chk_templates_type CHECK (template_type IN (
        'question', 'hint', 'scaffold', 'example', 'counter_example'
    )),
    CONSTRAINT chk_templates_difficulty CHECK (difficulty_level BETWEEN 1 AND 5)
);

COMMENT ON TABLE socratic_templates IS 'Templates de perguntas socráticas por categoria de erro';
COMMENT ON COLUMN socratic_templates.id IS 'Identificador único';
COMMENT ON COLUMN socratic_templates.language_code IS 'Idioma do template';
COMMENT ON COLUMN socratic_templates.skill_type IS 'Habilidade alvo';
COMMENT ON COLUMN socratic_templates.error_category IS 'Categoria de erro que o template aborda';
COMMENT ON COLUMN socratic_templates.template_type IS 'Tipo: question, hint, scaffold, example, counter_example';
COMMENT ON COLUMN socratic_templates.template_text IS 'Texto do template (pode ter placeholders como {word}, {error})';
COMMENT ON COLUMN socratic_templates.follow_up_text IS 'Pergunta de acompanhamento';
COMMENT ON COLUMN socratic_templates.hint_text IS 'Dica a dar se o aluno não entender';
COMMENT ON COLUMN socratic_templates.explanation_text IS 'Explicação final se necessário';
COMMENT ON COLUMN socratic_templates.difficulty_level IS 'Nível de dificuldade do conceito (1-5)';
COMMENT ON COLUMN socratic_templates.usage_count IS 'Vezes que o template foi usado';
COMMENT ON COLUMN socratic_templates.success_rate IS 'Taxa de sucesso (% de auto-correções)';

-- Índices para socratic_templates
CREATE INDEX IF NOT EXISTS idx_templates_lang_skill ON socratic_templates(language_code, skill_type);
CREATE INDEX IF NOT EXISTS idx_templates_error ON socratic_templates(error_category);
CREATE INDEX IF NOT EXISTS idx_templates_active ON socratic_templates(is_active) WHERE is_active = true;

-- Trigger para updated_at
DROP TRIGGER IF EXISTS trg_templates_updated_at ON socratic_templates;
CREATE TRIGGER trg_templates_updated_at
    BEFORE UPDATE ON socratic_templates
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- SEED DATA: Templates Socráticos para Inglês A1
-- ============================================================================

INSERT INTO socratic_templates (language_code, skill_type, error_category, template_type, template_text, follow_up_text, hint_text, difficulty_level)
VALUES
    -- Grammar - Verb To Be
    ('en', 'grammar', 'verb_to_be', 'question',
     'I notice you wrote "{error}". Can you think about when we use "am", "is", and "are"? Which pronoun goes with each?',
     'Good! Now, which one should go with "{subject}"?',
     'Remember: I AM, You/We/They ARE, He/She/It IS',
     1),

    -- Grammar - Articles
    ('en', 'grammar', 'article_usage', 'question',
     'You wrote "{error}". Think about this: do we use "a" or "an" before this word? What sound does the word start with?',
     'Is it a vowel sound or a consonant sound?',
     'We use "an" before vowel SOUNDS (a, e, i, o, u), and "a" before consonant sounds',
     1),

    -- Grammar - Present Simple
    ('en', 'grammar', 'present_simple_third_person', 'question',
     'Look at your sentence: "{error}". The subject is he/she/it. What happens to verbs in present simple with he/she/it?',
     'What letter do we usually add?',
     'With he/she/it, we add -s or -es to the verb',
     2),

    -- Vocabulary - Word Choice
    ('en', 'vocabulary', 'word_confusion', 'question',
     'You used "{error}" here. These two words look similar but have different meanings. Can you think of when we use each one?',
     'Try to use each word in a different sentence.',
     'Think about the context where you''ve seen each word before',
     2),

    -- Speaking - Pronunciation
    ('en', 'pronunciation', 'vowel_sound', 'question',
     'Listen to how you said "{word}". Can you hear the difference between your pronunciation and the model? What vowel sound should it have?',
     'Try saying it again, focusing on the vowel.',
     'This vowel is similar to the sound in "{example_word}"',
     2),

    -- Listening - Comprehension
    ('en', 'listening', 'missed_detail', 'question',
     'You mentioned "{user_answer}", but the audio said something slightly different. Can you listen again and focus on the {focus_area}?',
     'What exact words did you hear?',
     'Pay attention to the {key_word} in the sentence',
     2),

    -- Writing - Structure
    ('en', 'writing', 'sentence_structure', 'question',
     'Look at your sentence: "{error}". In English, what usually comes first in a sentence - the subject or the verb?',
     'Can you reorder the words to follow Subject + Verb + Object?',
     'Basic English order: Subject (who?) + Verb (does what?) + Object (to what?)',
     1),

    -- General - Politeness
    ('en', 'general', 'politeness_missing', 'question',
     'Your sentence is grammatically correct! But how could you make it sound more polite? What word could you add?',
     'Where would you put "please" in this sentence?',
     'Adding "please" or "could you" makes requests more polite',
     1)

ON CONFLICT DO NOTHING;

-- ============================================================================
-- ROLLBACK SCRIPT
-- ============================================================================
--
-- Para reverter esta migration:
--
-- DROP VIEW IF EXISTS most_addressed_concepts;
-- DROP VIEW IF EXISTS self_correction_rate;
-- DROP TRIGGER IF EXISTS trg_templates_updated_at ON socratic_templates;
-- DROP TRIGGER IF EXISTS trg_socratic_updated_at ON socratic_interactions;
-- DROP TABLE IF EXISTS socratic_templates CASCADE;
-- DROP TABLE IF EXISTS socratic_interactions CASCADE;
--
-- ============================================================================
