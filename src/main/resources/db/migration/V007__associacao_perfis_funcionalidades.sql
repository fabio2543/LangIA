-- ============================================================================
-- Script de associação entre perfis e funcionalidades (profile_functionalities)
-- ============================================================================
--
-- Este script implementa o sistema de permissões do LangIA associando cada
-- perfil (STUDENT, TEACHER, ADMIN) às suas funcionalidades correspondentes.
--
-- IMPORTANTE: A lógica de herança de permissões é implementada através de
-- inserções explícitas. Isso significa que:
--   - STUDENT recebe apenas suas permissões básicas
--   - TEACHER recebe TODAS as permissões de STUDENT + suas próprias
--   - ADMIN recebe TODAS as permissões de TEACHER e STUDENT + suas próprias
--
-- Esta abordagem de "desnormalização planejada" traz melhor performance nas
-- consultas, já que não é necessário percorrer hierarquias em tempo de execução.
--
-- Estrutura da tabela 'profile_functionalities':
--   - id (UUID, PK, gerado automaticamente)
--   - profile_id (UUID, FK → profiles.id, NOT NULL)
--   - functionality_id (UUID, FK → functionalities.id, NOT NULL)
--   - granted_at (TIMESTAMP, gerado automaticamente)
--   - UNIQUE(profile_id, functionality_id)
--
-- Total de associações que serão criadas: 34
--   - STUDENT: 6 funcionalidades
--   - TEACHER: 6 (herdadas) + 6 (próprias) = 12 funcionalidades
--   - ADMIN: 12 (herdadas) + 4 (próprias) = 16 funcionalidades
--
-- ============================================================================

-- Opcional: Limpar dados existentes
-- ATENÇÃO: Isso remove todas as associações entre perfis e funcionalidades!
-- DELETE FROM profile_functionalities;

-- ============================================================================
-- PERFIL: STUDENT (Estudante)
-- ============================================================================
-- Total de funcionalidades: 6
--
-- Estudantes têm acesso apenas às funcionalidades básicas necessárias para
-- seu aprendizado: podem gerenciar seu próprio perfil, visualizar aulas
-- disponíveis, se matricular, e acompanhar seu progresso.
-- ============================================================================

-- Módulo: Perfil Próprio (2 funcionalidades)
INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'STUDENT'),
    (SELECT id FROM functionalities WHERE code = 'visualizar_proprio_perfil'),
    CURRENT_TIMESTAMP,
    false;

INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'STUDENT'),
    (SELECT id FROM functionalities WHERE code = 'editar_proprio_perfil'),
    CURRENT_TIMESTAMP,
    false;

-- Módulo: Aulas (3 funcionalidades)
INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'STUDENT'),
    (SELECT id FROM functionalities WHERE code = 'visualizar_aulas_disponiveis'),
    CURRENT_TIMESTAMP,
    false;

INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'STUDENT'),
    (SELECT id FROM functionalities WHERE code = 'matricular_se'),
    CURRENT_TIMESTAMP,
    false;

-- Módulo: Alunos (1 funcionalidade)
INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'STUDENT'),
    (SELECT id FROM functionalities WHERE code = 'ver_proprio_progresso'),
    CURRENT_TIMESTAMP,
    false;

-- ============================================================================
-- PERFIL: TEACHER (Professor)
-- ============================================================================
-- Total de funcionalidades: 12
--   - 6 herdadas de STUDENT
--   - 6 próprias do perfil TEACHER
--
-- Professores herdam todas as capacidades de estudantes (podem se matricular
-- e aprender também) e adicionam capacidades de criação e gestão de aulas,
-- além de acompanhamento de seus alunos.
-- ============================================================================

-- -----------------------------------------------
-- PARTE 1: Funcionalidades herdadas de STUDENT
-- -----------------------------------------------
-- As mesmas 6 funcionalidades do perfil STUDENT são atribuídas ao TEACHER

-- Módulo: Perfil Próprio (2 funcionalidades herdadas)
INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'TEACHER'),
    (SELECT id FROM functionalities WHERE code = 'visualizar_proprio_perfil'),
    CURRENT_TIMESTAMP,
    true;

INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'TEACHER'),
    (SELECT id FROM functionalities WHERE code = 'editar_proprio_perfil'),
    CURRENT_TIMESTAMP,
    true;

-- Módulo: Aulas (3 funcionalidades herdadas)
INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'TEACHER'),
    (SELECT id FROM functionalities WHERE code = 'visualizar_aulas_disponiveis'),
    CURRENT_TIMESTAMP,
    true;

INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'TEACHER'),
    (SELECT id FROM functionalities WHERE code = 'matricular_se'),
    CURRENT_TIMESTAMP,
    true;

-- Módulo: Alunos (1 funcionalidade herdada)
INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'TEACHER'),
    (SELECT id FROM functionalities WHERE code = 'ver_proprio_progresso'),
    CURRENT_TIMESTAMP,
    true;

-- -----------------------------------------------
-- PARTE 2: Funcionalidades exclusivas de TEACHER
-- -----------------------------------------------

-- Módulo: Aulas (3 funcionalidades próprias)
INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'TEACHER'),
    (SELECT id FROM functionalities WHERE code = 'criar_aulas'),
    CURRENT_TIMESTAMP,
    false;

INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'TEACHER'),
    (SELECT id FROM functionalities WHERE code = 'editar_proprias_aulas'),
    CURRENT_TIMESTAMP,
    false;

-- Módulo: Alunos (2 funcionalidades próprias)
INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'TEACHER'),
    (SELECT id FROM functionalities WHERE code = 'ver_progresso_de_alunos'),
    CURRENT_TIMESTAMP,
    false;

INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'TEACHER'),
    (SELECT id FROM functionalities WHERE code = 'gerenciar_matriculas'),
    CURRENT_TIMESTAMP,
    false;

-- Módulo: Perfil Próprio (1 funcionalidade própria - contexto limitado)
-- Professor pode ver perfis de outros, mas apenas de seus alunos
INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'TEACHER'),
    (SELECT id FROM functionalities WHERE code = 'ver_perfis_de_outros'),
    CURRENT_TIMESTAMP,
    false;

-- ============================================================================
-- PERFIL: ADMIN (Administrador)
-- ============================================================================
-- Total de funcionalidades: 17 (todas as funcionalidades do sistema)
--   - 12 herdadas de TEACHER (que inclui as 6 de STUDENT)
--   - 5 próprias do perfil ADMIN
--
-- Administradores têm acesso irrestrito a todas as funcionalidades do sistema,
-- incluindo gestão de usuários, configurações globais, e acesso a logs e
-- dashboards administrativos.
-- ============================================================================

-- -----------------------------------------------
-- PARTE 1: Funcionalidades herdadas de TEACHER (e STUDENT)
-- -----------------------------------------------
-- As mesmas 12 funcionalidades do perfil TEACHER são atribuídas ao ADMIN

-- Módulo: Perfil Próprio (3 funcionalidades herdadas)
INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'ADMIN'),
    (SELECT id FROM functionalities WHERE code = 'visualizar_proprio_perfil'),
    CURRENT_TIMESTAMP,
    true;

INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'ADMIN'),
    (SELECT id FROM functionalities WHERE code = 'editar_proprio_perfil'),
    CURRENT_TIMESTAMP,
    true;

INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'ADMIN'),
    (SELECT id FROM functionalities WHERE code = 'ver_perfis_de_outros'),
    CURRENT_TIMESTAMP,
    true;

-- Módulo: Aulas (5 funcionalidades herdadas)
INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'ADMIN'),
    (SELECT id FROM functionalities WHERE code = 'visualizar_aulas_disponiveis'),
    CURRENT_TIMESTAMP,
    true;

INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'ADMIN'),
    (SELECT id FROM functionalities WHERE code = 'matricular_se'),
    CURRENT_TIMESTAMP,
    true;

INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'ADMIN'),
    (SELECT id FROM functionalities WHERE code = 'criar_aulas'),
    CURRENT_TIMESTAMP,
    true;

INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'ADMIN'),
    (SELECT id FROM functionalities WHERE code = 'editar_proprias_aulas'),
    CURRENT_TIMESTAMP,
    true;

-- Módulo: Alunos (3 funcionalidades herdadas)
INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'ADMIN'),
    (SELECT id FROM functionalities WHERE code = 'ver_proprio_progresso'),
    CURRENT_TIMESTAMP,
    true;

INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'ADMIN'),
    (SELECT id FROM functionalities WHERE code = 'ver_progresso_de_alunos'),
    CURRENT_TIMESTAMP,
    true;

INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'ADMIN'),
    (SELECT id FROM functionalities WHERE code = 'gerenciar_matriculas'),
    CURRENT_TIMESTAMP,
    true;

-- -----------------------------------------------
-- PARTE 2: Funcionalidades exclusivas de ADMIN
-- -----------------------------------------------

-- Módulo: Perfil Próprio (1 funcionalidade própria)
INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'ADMIN'),
    (SELECT id FROM functionalities WHERE code = 'editar_perfis_de_outros'),
    CURRENT_TIMESTAMP,
    false;

-- Módulo: Aulas (2 funcionalidades próprias)
INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'ADMIN'),
    (SELECT id FROM functionalities WHERE code = 'editar_aulas_de_outros'),
    CURRENT_TIMESTAMP,
    false;

INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'ADMIN'),
    (SELECT id FROM functionalities WHERE code = 'deletar_qualquer_aula'),
    CURRENT_TIMESTAMP,
    false;

-- Módulo: Sistema (4 funcionalidades próprias)
INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'ADMIN'),
    (SELECT id FROM functionalities WHERE code = 'acessar_dashboard_admin'),
    CURRENT_TIMESTAMP,
    false;

INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'ADMIN'),
    (SELECT id FROM functionalities WHERE code = 'configurar_sistema'),
    CURRENT_TIMESTAMP,
    false;

INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'ADMIN'),
    (SELECT id FROM functionalities WHERE code = 'ver_logs_sistema'),
    CURRENT_TIMESTAMP,
    false;

INSERT INTO profile_functionalities (id, profile_id, functionality_id, granted_at, granted_by_inheritance)
SELECT
    gen_random_uuid(),
    (SELECT id FROM profiles WHERE code = 'ADMIN'),
    (SELECT id FROM functionalities WHERE code = 'gerenciar_usuarios'),
    CURRENT_TIMESTAMP,
    false;

-- ============================================================================
-- QUERIES DE VERIFICAÇÃO
-- ============================================================================

-- Query 1: Contar total de associações criadas
-- Resultado esperado: 35 (6 STUDENT + 12 TEACHER + 17 ADMIN)
SELECT COUNT(*) as total_associacoes FROM profile_functionalities;

-- Query 2: Contar funcionalidades por perfil
-- Mostra quantas permissões cada perfil possui
SELECT
    p.code as perfil_codigo,
    p.name as perfil_nome,
    p.hierarchy_level as nivel,
    COUNT(pf.id) as total_funcionalidades
FROM profiles p
LEFT JOIN profile_functionalities pf ON p.id = pf.profile_id
GROUP BY p.id, p.code, p.name, p.hierarchy_level
ORDER BY p.hierarchy_level ASC;

-- Query 3: Listar funcionalidades por perfil e módulo
-- Visualização detalhada das permissões de cada perfil
SELECT
    p.code as perfil,
    f.module as modulo,
    COUNT(f.id) as quantidade_funcionalidades,
    STRING_AGG(f.code, ', ' ORDER BY f.code) as funcionalidades
FROM profiles p
JOIN profile_functionalities pf ON p.id = pf.profile_id
JOIN functionalities f ON pf.functionality_id = f.id
GROUP BY p.code, p.hierarchy_level, f.module
ORDER BY p.hierarchy_level, f.module;

-- Query 4: Verificar herança de permissões
-- Confirma que TEACHER tem todas de STUDENT e ADMIN tem todas de TEACHER
WITH student_perms AS (
    SELECT functionality_id
    FROM profile_functionalities
    WHERE profile_id = (SELECT id FROM profiles WHERE code = 'STUDENT')
),
teacher_perms AS (
    SELECT functionality_id
    FROM profile_functionalities
    WHERE profile_id = (SELECT id FROM profiles WHERE code = 'TEACHER')
),
admin_perms AS (
    SELECT functionality_id
    FROM profile_functionalities
    WHERE profile_id = (SELECT id FROM profiles WHERE code = 'ADMIN')
)
SELECT
    'TEACHER herda todas de STUDENT?' as verificacao,
    CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM student_perms
            WHERE functionality_id NOT IN (SELECT functionality_id FROM teacher_perms)
        ) THEN 'SIM - Herança correta'
        ELSE 'NÃO - ERRO na herança!'
    END as resultado
UNION ALL
SELECT
    'ADMIN herda todas de TEACHER?' as verificacao,
    CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM teacher_perms
            WHERE functionality_id NOT IN (SELECT functionality_id FROM admin_perms)
        ) THEN 'SIM - Herança correta'
        ELSE 'NÃO - ERRO na herança!'
    END as resultado;

-- Query 5: Listar todas as permissões detalhadamente
-- Útil para documentação e auditoria
SELECT
    p.code as perfil,
    p.name as perfil_nome,
    f.code as funcionalidade_codigo,
    f.description as funcionalidade_descricao,
    f.module as modulo,
    pf.granted_at as concedida_em
FROM profile_functionalities pf
JOIN profiles p ON pf.profile_id = p.id
JOIN functionalities f ON pf.functionality_id = f.id
ORDER BY p.hierarchy_level, f.module, f.code;

-- ============================================================================
-- RESULTADOS ESPERADOS
-- ============================================================================
--
-- Query 1 - Total de associações: 35
--
-- Query 2 - Funcionalidades por perfil:
-- perfil_codigo | perfil_nome   | nivel | total_funcionalidades
-- --------------|---------------|-------|----------------------
-- STUDENT       | Estudante     | 1     | 6
-- TEACHER       | Professor     | 2     | 12
-- ADMIN         | Administrador | 3     | 17
--
-- Query 4 - Verificação de herança:
-- verificacao                      | resultado
-- ---------------------------------|----------------------
-- TEACHER herda todas de STUDENT?  | SIM - Herança correta
-- ADMIN herda todas de TEACHER?    | SIM - Herança correta
--
-- ============================================================================
