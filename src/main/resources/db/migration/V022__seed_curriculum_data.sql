-- ============================================================================
-- Migration V022: Seed Data para Estrutura Curricular
-- ============================================================================
-- Dados iniciais baseados no Common European Framework of Reference (CEFR)
-- Inclui: níveis, competências, level_competencies e descritores base
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Níveis CEFR (A1-C2)
-- ----------------------------------------------------------------------------

INSERT INTO levels (id, code, name, description, order_index) VALUES
    (uuid_generate_v4(), 'A1', 'Iniciante',
     'Consegue compreender e usar expressões familiares e quotidianas, bem como frases básicas destinadas à satisfação de necessidades concretas. Consegue apresentar-se e apresentar outros e é capaz de fazer perguntas e dar respostas sobre aspectos pessoais como, por exemplo, o local onde vive, as pessoas que conhece e as coisas que tem.',
     1),
    (uuid_generate_v4(), 'A2', 'Básico',
     'Consegue compreender frases e expressões frequentes relacionadas com áreas de importância imediata (informações pessoais e familiares, compras, geografia local, emprego). Consegue comunicar em tarefas simples e em rotinas que exigem apenas uma troca de informação simples sobre assuntos que lhe são familiares.',
     2),
    (uuid_generate_v4(), 'B1', 'Intermediário',
     'Consegue compreender questões principais em textos claros sobre assuntos que lhe são familiares. Consegue lidar com a maioria das situações encontradas na região onde se fala a língua-alvo. Consegue produzir um discurso simples e coerente sobre assuntos que lhe são familiares ou de interesse pessoal.',
     3),
    (uuid_generate_v4(), 'B2', 'Intermediário Superior',
     'Consegue compreender ideias principais em textos complexos sobre assuntos concretos e abstratos. Consegue interagir com um grau de fluência e espontaneidade que torna possível a interação regular com falantes nativos. Consegue produzir textos claros e detalhados sobre uma ampla variedade de assuntos.',
     4),
    (uuid_generate_v4(), 'C1', 'Avançado',
     'Consegue compreender uma ampla variedade de textos longos e exigentes, bem como reconhecer significados implícitos. Consegue expressar-se de forma fluente e espontânea sem procurar muito as palavras. Consegue usar a língua de modo flexível e eficaz para fins sociais, acadêmicos e profissionais.',
     5),
    (uuid_generate_v4(), 'C2', 'Proficiente',
     'Consegue compreender, sem esforço, praticamente tudo o que ouve ou lê. Consegue resumir as informações recolhidas em diversas fontes orais e escritas, reconstruindo argumentos e factos de um modo coerente. Consegue expressar-se espontaneamente, de modo fluente e com exatidão.',
     6)
ON CONFLICT (code) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 2. Competências Linguísticas
-- ----------------------------------------------------------------------------

INSERT INTO competencies (id, code, name, description, icon, order_index) VALUES
    (uuid_generate_v4(), 'listening', 'Compreensão Auditiva',
     'Capacidade de entender a língua falada em diferentes contextos e velocidades.',
     'ear', 1),
    (uuid_generate_v4(), 'reading', 'Leitura',
     'Capacidade de compreender textos escritos de diferentes gêneros e complexidades.',
     'book-open', 2),
    (uuid_generate_v4(), 'speaking', 'Expressão Oral',
     'Capacidade de produzir língua falada de forma clara e apropriada para diferentes situações.',
     'mic', 3),
    (uuid_generate_v4(), 'writing', 'Expressão Escrita',
     'Capacidade de produzir textos escritos claros, coerentes e adequados a diferentes propósitos.',
     'pencil', 4),
    (uuid_generate_v4(), 'grammar', 'Gramática',
     'Conhecimento e uso correto das estruturas gramaticais da língua.',
     'book', 5),
    (uuid_generate_v4(), 'vocabulary', 'Vocabulário',
     'Conhecimento e uso adequado de palavras e expressões da língua.',
     'text', 6),
    (uuid_generate_v4(), 'pronunciation', 'Pronúncia',
     'Produção correta dos sons, ritmo e entonação da língua.',
     'volume-2', 7)
ON CONFLICT (code) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 3. Associação Level-Competencies com pesos
-- ----------------------------------------------------------------------------

-- Inserir todas as combinações de level-competency
INSERT INTO level_competencies (id, level_id, competency_id, weight)
SELECT
    uuid_generate_v4(),
    l.id,
    c.id,
    CASE
        -- Em níveis iniciantes, foco maior em listening/speaking
        WHEN l.order_index <= 2 AND c.code IN ('listening', 'speaking') THEN 1.00
        WHEN l.order_index <= 2 AND c.code IN ('reading', 'vocabulary') THEN 0.80
        WHEN l.order_index <= 2 AND c.code IN ('writing', 'grammar') THEN 0.60
        WHEN l.order_index <= 2 AND c.code = 'pronunciation' THEN 0.90
        -- Em níveis intermediários, equilíbrio
        WHEN l.order_index IN (3, 4) THEN 0.85
        -- Em níveis avançados, foco em produção e nuances
        WHEN l.order_index >= 5 AND c.code IN ('writing', 'speaking') THEN 1.00
        WHEN l.order_index >= 5 THEN 0.90
        ELSE 0.80
    END as weight
FROM levels l
CROSS JOIN competencies c
ON CONFLICT (level_id, competency_id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 4. Descritores (Can-Do Statements) por nível e competência
-- ----------------------------------------------------------------------------

-- A1 - Listening
INSERT INTO descriptors (id, level_competency_id, language_code, code, description, description_en, order_index, is_core, estimated_hours)
SELECT
    uuid_generate_v4(),
    lc.id,
    NULL,
    'A1-LIS-' || LPAD(ROW_NUMBER() OVER ()::TEXT, 3, '0'),
    unnest(ARRAY[
        'Consegue reconhecer palavras e expressões básicas de uso corrente relativas a si próprio, à sua família e aos contextos em que está inserido.',
        'Consegue entender instruções simples e curtas.',
        'Consegue identificar o assunto geral de uma conversa lenta e clara.',
        'Consegue compreender números, preços, datas e horários quando falados claramente.',
        'Consegue entender saudações e despedidas básicas.'
    ]),
    unnest(ARRAY[
        'Can recognize familiar words and very basic phrases concerning self, family, and immediate concrete surroundings.',
        'Can understand simple, short instructions.',
        'Can identify the general topic of slow, clear conversation.',
        'Can understand numbers, prices, dates and times when spoken clearly.',
        'Can understand basic greetings and farewells.'
    ]),
    ROW_NUMBER() OVER ()::INTEGER,
    true,
    2.0
FROM level_competencies lc
JOIN levels l ON lc.level_id = l.id
JOIN competencies c ON lc.competency_id = c.id
WHERE l.code = 'A1' AND c.code = 'listening'
LIMIT 1
ON CONFLICT (level_competency_id, language_code, code) DO NOTHING;

-- A1 - Speaking
INSERT INTO descriptors (id, level_competency_id, language_code, code, description, description_en, order_index, is_core, estimated_hours)
SELECT
    uuid_generate_v4(),
    lc.id,
    NULL,
    'A1-SPK-' || LPAD(ROW_NUMBER() OVER ()::TEXT, 3, '0'),
    unnest(ARRAY[
        'Consegue apresentar-se e apresentar outros de forma simples.',
        'Consegue fazer perguntas básicas sobre informações pessoais.',
        'Consegue pedir e dar informações simples como direções.',
        'Consegue usar expressões quotidianas para satisfazer necessidades básicas.',
        'Consegue falar sobre gostos e preferências de forma simples.'
    ]),
    unnest(ARRAY[
        'Can introduce themselves and others in a simple way.',
        'Can ask basic questions about personal information.',
        'Can ask for and give simple information like directions.',
        'Can use everyday expressions to meet basic needs.',
        'Can talk about likes and preferences in a simple way.'
    ]),
    ROW_NUMBER() OVER ()::INTEGER,
    true,
    3.0
FROM level_competencies lc
JOIN levels l ON lc.level_id = l.id
JOIN competencies c ON lc.competency_id = c.id
WHERE l.code = 'A1' AND c.code = 'speaking'
LIMIT 1
ON CONFLICT (level_competency_id, language_code, code) DO NOTHING;

-- A1 - Reading
INSERT INTO descriptors (id, level_competency_id, language_code, code, description, description_en, order_index, is_core, estimated_hours)
SELECT
    uuid_generate_v4(),
    lc.id,
    NULL,
    'A1-REA-' || LPAD(ROW_NUMBER() OVER ()::TEXT, 3, '0'),
    unnest(ARRAY[
        'Consegue compreender nomes familiares, palavras e frases muito simples.',
        'Consegue ler avisos simples, cartazes e catálogos.',
        'Consegue entender formulários simples.',
        'Consegue identificar informações específicas em textos informativos simples.',
        'Consegue compreender mensagens curtas e simples em cartões postais.'
    ]),
    unnest(ARRAY[
        'Can understand familiar names, words and very simple sentences.',
        'Can read simple notices, posters and catalogues.',
        'Can understand simple forms.',
        'Can locate specific information in simple informative texts.',
        'Can understand short, simple messages on postcards.'
    ]),
    ROW_NUMBER() OVER ()::INTEGER,
    true,
    2.0
FROM level_competencies lc
JOIN levels l ON lc.level_id = l.id
JOIN competencies c ON lc.competency_id = c.id
WHERE l.code = 'A1' AND c.code = 'reading'
LIMIT 1
ON CONFLICT (level_competency_id, language_code, code) DO NOTHING;

-- A1 - Writing
INSERT INTO descriptors (id, level_competency_id, language_code, code, description, description_en, order_index, is_core, estimated_hours)
SELECT
    uuid_generate_v4(),
    lc.id,
    NULL,
    'A1-WRI-' || LPAD(ROW_NUMBER() OVER ()::TEXT, 3, '0'),
    unnest(ARRAY[
        'Consegue escrever um postal curto e simples.',
        'Consegue preencher formulários com dados pessoais.',
        'Consegue escrever frases e expressões simples sobre si mesmo.',
        'Consegue escrever uma lista de compras.',
        'Consegue deixar uma nota ou mensagem simples.'
    ]),
    unnest(ARRAY[
        'Can write a short, simple postcard.',
        'Can fill in forms with personal details.',
        'Can write simple phrases and sentences about themselves.',
        'Can write a shopping list.',
        'Can leave a simple note or message.'
    ]),
    ROW_NUMBER() OVER ()::INTEGER,
    true,
    2.5
FROM level_competencies lc
JOIN levels l ON lc.level_id = l.id
JOIN competencies c ON lc.competency_id = c.id
WHERE l.code = 'A1' AND c.code = 'writing'
LIMIT 1
ON CONFLICT (level_competency_id, language_code, code) DO NOTHING;

-- A2 - Listening
INSERT INTO descriptors (id, level_competency_id, language_code, code, description, description_en, order_index, is_core, estimated_hours)
SELECT
    uuid_generate_v4(),
    lc.id,
    NULL,
    'A2-LIS-' || LPAD(ROW_NUMBER() OVER ()::TEXT, 3, '0'),
    unnest(ARRAY[
        'Consegue compreender o essencial de anúncios e mensagens breves.',
        'Consegue entender conversas sobre família, compras e trabalho.',
        'Consegue identificar o tema de discussões quando as pessoas falam devagar.',
        'Consegue compreender instruções simples sobre como ir de um lugar a outro.',
        'Consegue entender informações principais em programas de rádio ou TV sobre assuntos conhecidos.'
    ]),
    unnest(ARRAY[
        'Can understand the main points of short, clear announcements and messages.',
        'Can understand conversations about family, shopping and work.',
        'Can identify the topic of discussion when people speak slowly.',
        'Can understand simple directions on how to get from one place to another.',
        'Can understand main information in radio/TV programs about familiar topics.'
    ]),
    ROW_NUMBER() OVER ()::INTEGER,
    true,
    3.0
FROM level_competencies lc
JOIN levels l ON lc.level_id = l.id
JOIN competencies c ON lc.competency_id = c.id
WHERE l.code = 'A2' AND c.code = 'listening'
LIMIT 1
ON CONFLICT (level_competency_id, language_code, code) DO NOTHING;

-- A2 - Speaking
INSERT INTO descriptors (id, level_competency_id, language_code, code, description, description_en, order_index, is_core, estimated_hours)
SELECT
    uuid_generate_v4(),
    lc.id,
    NULL,
    'A2-SPK-' || LPAD(ROW_NUMBER() OVER ()::TEXT, 3, '0'),
    unnest(ARRAY[
        'Consegue comunicar-se em tarefas simples e rotineiras.',
        'Consegue descrever a família, condições de vida e formação.',
        'Consegue usar frases e expressões para descrever experiências passadas.',
        'Consegue participar de conversas curtas em contextos conhecidos.',
        'Consegue fazer compras em lojas, restaurantes e serviços.'
    ]),
    unnest(ARRAY[
        'Can communicate in simple and routine tasks.',
        'Can describe family, living conditions and educational background.',
        'Can use phrases and sentences to describe past experiences.',
        'Can take part in short conversations in familiar contexts.',
        'Can handle shopping in stores, restaurants and services.'
    ]),
    ROW_NUMBER() OVER ()::INTEGER,
    true,
    4.0
FROM level_competencies lc
JOIN levels l ON lc.level_id = l.id
JOIN competencies c ON lc.competency_id = c.id
WHERE l.code = 'A2' AND c.code = 'speaking'
LIMIT 1
ON CONFLICT (level_competency_id, language_code, code) DO NOTHING;

-- B1 - Listening
INSERT INTO descriptors (id, level_competency_id, language_code, code, description, description_en, order_index, is_core, estimated_hours)
SELECT
    uuid_generate_v4(),
    lc.id,
    NULL,
    'B1-LIS-' || LPAD(ROW_NUMBER() OVER ()::TEXT, 3, '0'),
    unnest(ARRAY[
        'Consegue compreender as questões principais quando a linguagem é clara e padrão.',
        'Consegue seguir muitos filmes em que a história se apoia fortemente na ação e na imagem.',
        'Consegue compreender a maioria dos noticiários de TV sobre assuntos familiares.',
        'Consegue seguir palestras e apresentações dentro de sua área de interesse.',
        'Consegue entender detalhes de conversas sobre assuntos do quotidiano.'
    ]),
    unnest(ARRAY[
        'Can understand main points when clear standard language is used.',
        'Can follow many films in which the story relies heavily on action and image.',
        'Can understand most TV news about familiar topics.',
        'Can follow lectures and talks within their field of interest.',
        'Can understand details in conversations about everyday topics.'
    ]),
    ROW_NUMBER() OVER ()::INTEGER,
    true,
    4.0
FROM level_competencies lc
JOIN levels l ON lc.level_id = l.id
JOIN competencies c ON lc.competency_id = c.id
WHERE l.code = 'B1' AND c.code = 'listening'
LIMIT 1
ON CONFLICT (level_competency_id, language_code, code) DO NOTHING;

-- B1 - Speaking
INSERT INTO descriptors (id, level_competency_id, language_code, code, description, description_en, order_index, is_core, estimated_hours)
SELECT
    uuid_generate_v4(),
    lc.id,
    NULL,
    'B1-SPK-' || LPAD(ROW_NUMBER() OVER ()::TEXT, 3, '0'),
    unnest(ARRAY[
        'Consegue lidar com a maioria das situações que surgem durante viagens.',
        'Consegue participar de conversas sobre assuntos familiares e de interesse pessoal.',
        'Consegue descrever experiências e eventos, sonhos, esperanças e ambições.',
        'Consegue explicar brevemente opiniões e planos.',
        'Consegue contar uma história ou descrever algo como um filme ou livro.'
    ]),
    unnest(ARRAY[
        'Can deal with most situations arising during travel.',
        'Can enter unprepared into conversation on familiar topics and personal interest.',
        'Can describe experiences and events, dreams, hopes and ambitions.',
        'Can briefly give reasons and explanations for opinions and plans.',
        'Can narrate a story or describe something like a film or book.'
    ]),
    ROW_NUMBER() OVER ()::INTEGER,
    true,
    5.0
FROM level_competencies lc
JOIN levels l ON lc.level_id = l.id
JOIN competencies c ON lc.competency_id = c.id
WHERE l.code = 'B1' AND c.code = 'speaking'
LIMIT 1
ON CONFLICT (level_competency_id, language_code, code) DO NOTHING;

-- B2 - Listening
INSERT INTO descriptors (id, level_competency_id, language_code, code, description, description_en, order_index, is_core, estimated_hours)
SELECT
    uuid_generate_v4(),
    lc.id,
    NULL,
    'B2-LIS-' || LPAD(ROW_NUMBER() OVER ()::TEXT, 3, '0'),
    unnest(ARRAY[
        'Consegue compreender discursos longos e argumentação complexa sobre assuntos familiares.',
        'Consegue entender a maioria dos noticiários de TV e programas sobre assuntos atuais.',
        'Consegue compreender a maioria dos filmes em linguagem padrão.',
        'Consegue seguir discussões técnicas na sua área de especialização.',
        'Consegue entender palestras e apresentações complexas.'
    ]),
    unnest(ARRAY[
        'Can understand extended speech and complex argumentation on familiar topics.',
        'Can understand most TV news and current affairs programs.',
        'Can understand the majority of films in standard dialect.',
        'Can follow technical discussions in their field of specialization.',
        'Can understand complex lectures and presentations.'
    ]),
    ROW_NUMBER() OVER ()::INTEGER,
    true,
    5.0
FROM level_competencies lc
JOIN levels l ON lc.level_id = l.id
JOIN competencies c ON lc.competency_id = c.id
WHERE l.code = 'B2' AND c.code = 'listening'
LIMIT 1
ON CONFLICT (level_competency_id, language_code, code) DO NOTHING;

-- B2 - Speaking
INSERT INTO descriptors (id, level_competency_id, language_code, code, description, description_en, order_index, is_core, estimated_hours)
SELECT
    uuid_generate_v4(),
    lc.id,
    NULL,
    'B2-SPK-' || LPAD(ROW_NUMBER() OVER ()::TEXT, 3, '0'),
    unnest(ARRAY[
        'Consegue interagir com fluência e espontaneidade com falantes nativos.',
        'Consegue participar ativamente de discussões em contextos familiares.',
        'Consegue apresentar descrições claras e detalhadas de assuntos complexos.',
        'Consegue explicar um ponto de vista sobre um assunto atual com vantagens e desvantagens.',
        'Consegue desenvolver argumentos sistemáticos com pontos secundários relevantes.'
    ]),
    unnest(ARRAY[
        'Can interact with fluency and spontaneity with native speakers.',
        'Can take active part in discussions in familiar contexts.',
        'Can present clear, detailed descriptions of complex subjects.',
        'Can explain a viewpoint on a topical issue giving advantages and disadvantages.',
        'Can develop arguments systematically with relevant supporting points.'
    ]),
    ROW_NUMBER() OVER ()::INTEGER,
    true,
    6.0
FROM level_competencies lc
JOIN levels l ON lc.level_id = l.id
JOIN competencies c ON lc.competency_id = c.id
WHERE l.code = 'B2' AND c.code = 'speaking'
LIMIT 1
ON CONFLICT (level_competency_id, language_code, code) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 5. Tabela de versões do currículo (se não existir)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS curriculum_versions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    release_notes TEXT,
    is_current BOOLEAN NOT NULL DEFAULT false,
    published_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE curriculum_versions IS 'Versões do currículo base (níveis, competências, descritores)';

-- Garantir que apenas uma versão seja current
CREATE UNIQUE INDEX IF NOT EXISTS idx_curriculum_versions_current
ON curriculum_versions (is_current)
WHERE is_current = true;

-- ----------------------------------------------------------------------------
-- 6. Versão inicial do currículo
-- ----------------------------------------------------------------------------

INSERT INTO curriculum_versions (id, version, description, release_notes, is_current, published_at, created_by)
VALUES (
    uuid_generate_v4(),
    '1.0.0',
    'Versão inicial do currículo baseado em CEFR',
    'Primeira versão do currículo com níveis A1-C2, 7 competências e descritores base.',
    true,
    NOW(),
    NULL
)
ON CONFLICT (version) DO NOTHING;

-- ============================================================================
-- ROLLBACK SCRIPT
-- ============================================================================
--
-- Para reverter esta migration:
--
-- DELETE FROM curriculum_versions WHERE version = '1.0.0';
-- DELETE FROM descriptors WHERE code LIKE 'A1-%' OR code LIKE 'A2-%' OR code LIKE 'B1-%' OR code LIKE 'B2-%';
-- DELETE FROM level_competencies;
-- DELETE FROM competencies;
-- DELETE FROM levels;
--
-- ============================================================================
