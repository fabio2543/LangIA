-- Script de carga inicial das funcionalidades do sistema LangIA
-- Este script deve ser executado após a criação da tabela functionalities

-- Limpar dados existentes (use com cuidado em produção!)
-- DELETE FROM functionalities;

-- ============================================================================
-- MÓDULO: PERFIL PRÓPRIO
-- Funcionalidades relacionadas ao gerenciamento de informações pessoais
-- ============================================================================

INSERT INTO functionalities (id, code, description, module, active, created_at, updated_at) VALUES
(gen_random_uuid(),
 'visualizar_proprio_perfil',
 'Permite que o usuário visualize todas as suas informações pessoais cadastradas no sistema, incluindo nome completo, endereço de e-mail, número de telefone cadastrado, foto de perfil se houver, data de cadastro na plataforma, perfil atribuído no sistema (estudante, professor ou administrador), estatísticas gerais como número de aulas em que está matriculado ou que criou, e preferências de configuração pessoal.',
 'OWN_PROFILE',
 true,
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP);

INSERT INTO functionalities (id, code, description, module, active, created_at, updated_at) VALUES
(gen_random_uuid(),
 'editar_proprio_perfil',
 'Concede ao usuário a capacidade de modificar suas próprias informações pessoais, como atualizar o nome de exibição, alterar a foto do perfil fazendo upload de uma nova imagem, modificar o número de telefone para contato, ajustar preferências de notificação escolhendo quais tipos de alertas deseja receber por e-mail ou dentro da plataforma, configurar preferências de idioma da interface, e ajustar outras configurações pessoais relacionadas à experiência de uso.',
 'OWN_PROFILE',
 true,
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP);

INSERT INTO functionalities (id, code, description, module, active, created_at, updated_at) VALUES
(gen_random_uuid(),
 'ver_perfis_de_outros',
 'Permite visualizar informações públicas do perfil de outros usuários da plataforma. Para estudantes, esta funcionalidade geralmente não está disponível para proteger a privacidade. Para professores, está limitada apenas aos estudantes matriculados em suas próprias aulas, permitindo que conheçam melhor seus alunos e personalizem o ensino. Para administradores, permite visualizar o perfil completo de qualquer usuário do sistema, incluindo informações detalhadas, histórico de atividades e estatísticas de uso.',
 'OWN_PROFILE',
 true,
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP);

INSERT INTO functionalities (id, code, description, module, active, created_at, updated_at) VALUES
(gen_random_uuid(),
 'editar_perfis_de_outros',
 'Concede a capacidade de modificar informações no perfil de outros usuários da plataforma. Esta é uma funcionalidade altamente privilegiada disponível apenas para administradores, permitindo que eles corrijam informações incorretas quando um usuário não consegue fazer isso sozinho, atualizem dados desatualizados, ajustem configurações em nome do usuário quando solicitado via suporte, alterem o perfil de acesso do usuário, e façam quaisquer outras modificações necessárias para a gestão adequada da base de usuários.',
 'OWN_PROFILE',
 true,
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP);

-- ============================================================================
-- MÓDULO: AULAS
-- Funcionalidades relacionadas à criação e gestão de aulas e cursos
-- ============================================================================

INSERT INTO functionalities (id, code, description, module, active, created_at, updated_at) VALUES
(gen_random_uuid(),
 'visualizar_aulas_disponiveis',
 'Permite que o usuário navegue pelo catálogo completo de aulas públicas disponíveis na plataforma LangIA, visualizando informações como título da aula, descrição do conteúdo que será ensinado, nome do professor responsável, nível de dificuldade (iniciante, intermediário, avançado), idioma que será ensinado, número de estudantes já matriculados, avaliações e comentários de alunos anteriores, e qualquer outro metadado relevante que ajude o usuário a decidir se quer se matricular.',
 'LESSONS',
 true,
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP);

INSERT INTO functionalities (id, code, description, module, active, created_at, updated_at) VALUES
(gen_random_uuid(),
 'matricular_se',
 'Concede ao usuário a capacidade de se inscrever em aulas disponíveis no catálogo da plataforma, tornando-se oficialmente um estudante daquela aula. Após a matrícula, o usuário ganha acesso a todo o material didático da aula, pode participar de atividades e exercícios, interagir nos fóruns de discussão da turma, enviar mensagens ao professor, e ter seu progresso acompanhado. Em algumas aulas onde o professor configurou aprovação manual, a matrícula pode ficar pendente até que o professor revise e aprove a inscrição.',
 'LESSONS',
 true,
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP);

INSERT INTO functionalities (id, code, description, module, active, created_at, updated_at) VALUES
(gen_random_uuid(),
 'criar_aulas',
 'Permite que o usuário crie novas aulas do zero na plataforma, definindo informações básicas como título da aula, descrição detalhada do conteúdo que será ensinado, idioma alvo, nível de dificuldade, se a aula será pública ou privada, se as matrículas serão automáticas ou requerem aprovação manual, e outros parâmetros iniciais. Após criar a aula, o usuário se torna o professor responsável e pode começar a adicionar conteúdo didático, estruturar módulos de aprendizado, e gerenciar todos os aspectos daquela aula.',
 'LESSONS',
 true,
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP);

INSERT INTO functionalities (id, code, description, module, active, created_at, updated_at) VALUES
(gen_random_uuid(),
 'editar_proprias_aulas',
 'Concede ao usuário a capacidade de modificar informações e configurações das aulas que ele próprio criou. Isso inclui alterar título, descrição, nível de dificuldade, adicionar ou remover material didático, reorganizar a estrutura de módulos e lições, ajustar configurações de privacidade e matrícula, modificar datas e prazos se houver, atualizar exercícios e atividades, e fazer qualquer outra mudança necessária para manter a aula atualizada e relevante.',
 'LESSONS',
 true,
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP);

INSERT INTO functionalities (id, code, description, module, active, created_at, updated_at) VALUES
(gen_random_uuid(),
 'editar_aulas_de_outros',
 'Permite que o usuário modifique aulas criadas por outros professores. Esta é uma funcionalidade exclusiva de administradores, utilizada em situações específicas como quando um professor sai da plataforma e deixa aulas ativas que precisam de manutenção, quando há necessidade de correção emergencial de conteúdo incorreto ou inadequado, quando um professor solicita ajuda administrativa para fazer alterações técnicas que ele não consegue realizar sozinho, ou quando há necessidade de padronização de conteúdo em toda a plataforma.',
 'LESSONS',
 true,
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP);

INSERT INTO functionalities (id, code, description, module, active, created_at, updated_at) VALUES
(gen_random_uuid(),
 'deletar_qualquer_aula',
 'Concede ao administrador a capacidade de remover permanentemente qualquer aula do sistema, independentemente de quem a criou. Esta é uma funcionalidade crítica utilizada apenas em circunstâncias especiais, como quando uma aula contém conteúdo inadequado ou viola termos de uso, quando uma aula ficou obsoleta e o professor original não está mais ativo para removê-la, quando há duplicação acidental de conteúdo, ou quando necessário por questões legais ou de conformidade.',
 'LESSONS',
 true,
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP);

-- ============================================================================
-- MÓDULO: ALUNOS
-- Funcionalidades de acompanhamento e gestão de estudantes
-- ============================================================================

INSERT INTO functionalities (id, code, description, module, active, created_at, updated_at) VALUES
(gen_random_uuid(),
 'ver_proprio_progresso',
 'Permite que o usuário visualize estatísticas detalhadas sobre seu próprio progresso de aprendizado em todas as aulas em que está matriculado. Isso inclui ver quais módulos e lições já foram concluídos, percentual de conclusão de cada aula, exercícios já realizados e suas respectivas pontuações, tempo total dedicado ao estudo, sequências de dias consecutivos estudando, conquistas e badges desbloqueadas, comparação do progresso ao longo do tempo através de gráficos, e identificação de áreas onde pode estar tendo mais dificuldade.',
 'STUDENTS',
 true,
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP);

INSERT INTO functionalities (id, code, description, module, active, created_at, updated_at) VALUES
(gen_random_uuid(),
 'ver_progresso_de_alunos',
 'Permite que professores visualizem informações detalhadas sobre o progresso de estudantes matriculados em suas próprias aulas, e que administradores visualizem o progresso de qualquer estudante no sistema. Para professores, isso inclui ver quais estudantes estão mais engajados, identificar alunos que podem estar tendo dificuldades e precisam de atenção especial, verificar quem completou quais módulos e exercícios, analisar padrões de estudo da turma, e usar essas informações para adaptar o conteúdo e o ritmo das aulas conforme necessário.',
 'STUDENTS',
 true,
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP);

INSERT INTO functionalities (id, code, description, module, active, created_at, updated_at) VALUES
(gen_random_uuid(),
 'gerenciar_matriculas',
 'Concede a capacidade de administrar as matrículas de estudantes em aulas. Para professores, isso significa poder remover um estudante de sua aula se necessário, aprovar ou rejeitar solicitações de matrícula quando a aula está configurada para aprovação manual, e gerenciar listas de espera se houver limite de vagas. Para administradores, essa funcionalidade é ainda mais ampla, permitindo gerenciar matrículas em qualquer aula da plataforma, realizar matrículas em massa quando necessário, e resolver questões administrativas relacionadas a inscrições e cancelamentos.',
 'STUDENTS',
 true,
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP);

-- ============================================================================
-- MÓDULO: SISTEMA
-- Funcionalidades administrativas que afetam toda a plataforma
-- ============================================================================

INSERT INTO functionalities (id, code, description, module, active, created_at, updated_at) VALUES
(gen_random_uuid(),
 'acessar_dashboard_admin',
 'Concede acesso ao painel administrativo completo da plataforma LangIA, onde administradores podem visualizar métricas agregadas sobre uso do sistema, incluindo número total de usuários ativos, quantidade de aulas criadas e em andamento, estatísticas de engajamento e retenção, gráficos de crescimento ao longo do tempo, identificação de aulas mais populares, professores mais ativos, uso de recursos do sistema como processamento de IA e integração com WhatsApp, alertas sobre problemas técnicos ou de performance, e qualquer outra informação relevante para a gestão e tomada de decisões estratégicas.',
 'SYSTEM',
 true,
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP);

INSERT INTO functionalities (id, code, description, module, active, created_at, updated_at) VALUES
(gen_random_uuid(),
 'configurar_sistema',
 'Permite que administradores modifiquem configurações globais que afetam o funcionamento de toda a plataforma. Isso inclui ajustar parâmetros técnicos como limites de upload de arquivo, configurar integrações com serviços externos como APIs de inteligência artificial para conversação e processamento de linguagem natural, gerenciar conexões com WhatsApp Business API, definir templates de e-mail e notificação, configurar políticas de cache e performance, ajustar configurações de segurança como complexidade mínima de senhas, definir períodos de timeout de sessão, e qualquer outro parâmetro que influencie o comportamento global do sistema.',
 'SYSTEM',
 true,
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP);

INSERT INTO functionalities (id, code, description, module, active, created_at, updated_at) VALUES
(gen_random_uuid(),
 'ver_logs_sistema',
 'Concede acesso aos registros de log do sistema, que contêm informações detalhadas sobre tudo que acontece na plataforma. Administradores podem consultar logs de erro para investigar problemas técnicos e identificar causas de falhas, logs de auditoria para ver quem fez o quê e quando (essencial para segurança e conformidade), logs de acesso para identificar padrões de uso suspeitos ou ataques, logs de performance para identificar gargalos e oportunidades de otimização, e qualquer outro tipo de registro que ajude na manutenção, troubleshooting e segurança da plataforma.',
 'SYSTEM',
 true,
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP);

INSERT INTO functionalities (id, code, description, module, active, created_at, updated_at) VALUES
(gen_random_uuid(),
 'gerenciar_usuarios',
 'Concede aos administradores controle total sobre a base de usuários da plataforma. Isso inclui a capacidade de criar novos usuários manualmente quando necessário, editar informações de qualquer usuário para corrigir dados incorretos ou atualizar informações, desativar temporariamente contas de usuários que violaram termos de uso, reativar contas previamente suspensas, deletar permanentemente usuários quando solicitado ou por necessidade legal, alterar o perfil de acesso de usuários, resetar senhas quando usuários não conseguem recuperar acesso, visualizar histórico completo de atividades de qualquer usuário, e realizar qualquer outra operação necessária para a gestão adequada da comunidade.',
 'SYSTEM',
 true,
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP);

-- Verificar total de funcionalidades inseridas
SELECT COUNT(*) as total_funcionalidades FROM functionalities;

-- Verificar funcionalidades por módulo
SELECT module, COUNT(*) as quantidade
FROM functionalities
GROUP BY module
ORDER BY module;
