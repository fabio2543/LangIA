package com.langia.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langia.backend.model.PromptTemplate;
import com.langia.backend.model.StudentSkillAssessment;
import com.langia.backend.repository.PromptTemplateRepository;
import com.langia.backend.repository.StudentLanguageEnrollmentRepository;
import com.langia.backend.repository.StudentLearningPreferencesRepository;
import com.langia.backend.repository.StudentSkillAssessmentRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para geração de trilhas de aprendizado usando IA (LLM).
 * Utiliza as preferências e assessment do estudante para personalizar a trilha.
 * Usa GeminiApiClient para chamadas diretas à API do Gemini.
 */
@Service
@Slf4j
public class TrailGenerationAIService {

    private final GeminiApiClient geminiApiClient;
    private final StudentLearningPreferencesRepository preferencesRepository;
    private final StudentLanguageEnrollmentRepository enrollmentRepository;
    private final StudentSkillAssessmentRepository assessmentRepository;
    private final PromptTemplateRepository promptTemplateRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public TrailGenerationAIService(
            GeminiApiClient geminiApiClient,
            StudentLearningPreferencesRepository preferencesRepository,
            StudentLanguageEnrollmentRepository enrollmentRepository,
            StudentSkillAssessmentRepository assessmentRepository,
            PromptTemplateRepository promptTemplateRepository,
            ObjectMapper objectMapper) {
        this.geminiApiClient = geminiApiClient;
        this.preferencesRepository = preferencesRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.assessmentRepository = assessmentRepository;
        this.promptTemplateRepository = promptTemplateRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Verifica se o LLM está disponível.
     */
    private boolean isLlmAvailable() {
        return geminiApiClient.isConfigured();
    }

    /**
     * Chama o LLM (Gemini API direta).
     */
    private String callLlm(String prompt) {
        if (!geminiApiClient.isConfigured()) {
            throw new RuntimeException("Gemini API não configurada");
        }

        log.debug("Chamando Gemini API...");
        return geminiApiClient.generateContent(prompt);
    }

    /**
     * Gera uma estrutura de trilha personalizada baseada no perfil do estudante.
     *
     * @param studentId ID do estudante
     * @param languageCode Código do idioma
     * @param levelCode Código do nível CEFR
     * @return JSON com a estrutura da trilha gerada
     */
    public String generatePersonalizedTrailStructure(UUID studentId, String languageCode, String levelCode) {
        log.info("Gerando trilha personalizada para estudante: {}, idioma: {}, nível: {}",
                studentId, languageCode, levelCode);

        if (!isLlmAvailable()) {
            log.warn("Gemini API não configurada. Retornando estrutura padrão.");
            return getDefaultTrailStructure(levelCode);
        }

        // Coletar dados do estudante
        StudentContext context = collectStudentContext(studentId, languageCode);

        // Montar prompt
        String prompt = buildTrailGenerationPrompt(context, languageCode, levelCode);

        try {
            String response = callLlm(prompt);

            log.info("Trilha gerada com sucesso via IA para estudante: {}", studentId);
            return extractJsonFromResponse(response);
        } catch (Exception e) {
            log.error("Erro ao gerar trilha via IA: {}", e.getMessage(), e);
            return getDefaultTrailStructure(levelCode);
        }
    }

    /**
     * Gera conteúdo de uma lição específica.
     *
     * @param lessonTitle Título da lição
     * @param lessonType Tipo da lição
     * @param studentId ID do estudante
     * @param languageCode Idioma
     * @param levelCode Nível
     * @return JSON com o conteúdo da lição
     */
    public String generateLessonContent(String lessonTitle, String lessonType,
            UUID studentId, String languageCode, String levelCode) {
        log.info("Gerando conteúdo da lição: {} para estudante: {}", lessonTitle, studentId);

        if (!isLlmAvailable()) {
            log.warn("Gemini API não configurada. Retornando conteúdo padrão.");
            return getDefaultLessonContent(lessonType);
        }

        StudentContext context = collectStudentContext(studentId, languageCode);
        String prompt = buildLessonContentPrompt(lessonTitle, lessonType, context, languageCode, levelCode);

        try {
            String response = callLlm(prompt);

            log.info("Conteúdo da lição gerado com sucesso");
            return extractJsonFromResponse(response);
        } catch (Exception e) {
            log.error("Erro ao gerar conteúdo da lição: {}", e.getMessage(), e);
            return getDefaultLessonContent(lessonType);
        }
    }

    /**
     * Coleta o contexto completo do estudante para personalização.
     */
    private StudentContext collectStudentContext(UUID studentId, String languageCode) {
        StudentContext context = new StudentContext();

        // Preferências de aprendizado
        preferencesRepository.findByUserId(studentId).ifPresent(prefs -> {
            context.dailyTime = prefs.getDailyTimeAvailable();
            context.preferredFormats = prefs.getPreferredFormats();
            context.primaryObjective = prefs.getPrimaryObjective() != null
                    ? prefs.getPrimaryObjective().name() : null;
            context.secondaryObjective = prefs.getSecondaryObjective() != null
                    ? prefs.getSecondaryObjective().name() : null;
            context.objectiveDescription = prefs.getObjectiveDescription();
            context.topicsOfInterest = prefs.getTopicsOfInterest();
        });

        // Enrollment do idioma
        enrollmentRepository.findByUserIdAndLanguageCode(studentId, languageCode).ifPresent(enrollment -> {
            context.cefrLevel = enrollment.getCefrLevel() != null
                    ? enrollment.getCefrLevel() : "A1";
        });

        // Assessment de habilidades (pegar o mais recente)
        List<StudentSkillAssessment> assessments = assessmentRepository
                .findByUserIdAndLanguageOrderByAssessedAtDesc(studentId, languageCode);
        if (!assessments.isEmpty()) {
            StudentSkillAssessment assessment = assessments.get(0);
            context.listeningDifficulty = assessment.getListeningDifficulty() != null
                    ? assessment.getListeningDifficulty().name() : null;
            context.speakingDifficulty = assessment.getSpeakingDifficulty() != null
                    ? assessment.getSpeakingDifficulty().name() : null;
            context.readingDifficulty = assessment.getReadingDifficulty() != null
                    ? assessment.getReadingDifficulty().name() : null;
            context.writingDifficulty = assessment.getWritingDifficulty() != null
                    ? assessment.getWritingDifficulty().name() : null;
        }

        return context;
    }

    /**
     * Monta o prompt para geração da estrutura da trilha.
     */
    private String buildTrailGenerationPrompt(StudentContext context, String languageCode, String levelCode) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Você é um especialista em ensino de idiomas e design instrucional. ");
        prompt.append("Crie uma trilha de aprendizado personalizada para um estudante de ")
              .append(getLanguageName(languageCode)).append(" no nível ").append(levelCode).append(".\n\n");

        prompt.append("## Perfil do Estudante:\n");

        if (context.primaryObjective != null) {
            prompt.append("- Objetivo principal: ").append(translateObjective(context.primaryObjective)).append("\n");
        }
        if (context.secondaryObjective != null) {
            prompt.append("- Objetivo secundário: ").append(translateObjective(context.secondaryObjective)).append("\n");
        }
        if (context.objectiveDescription != null && !context.objectiveDescription.isBlank()) {
            prompt.append("- Detalhes do objetivo: \"").append(context.objectiveDescription).append("\"\n");
        }
        if (context.dailyTime != null) {
            prompt.append("- Tempo disponível por dia: ").append(translateDailyTime(context.dailyTime)).append("\n");
        }
        if (context.preferredFormats != null && !context.preferredFormats.isEmpty()) {
            prompt.append("- Formatos preferidos: ").append(translateFormats(context.preferredFormats)).append("\n");
        }
        if (context.topicsOfInterest != null && !context.topicsOfInterest.isEmpty()) {
            prompt.append("- Tópicos de interesse: ").append(String.join(", ", context.topicsOfInterest)).append("\n");
        }

        prompt.append("\n## Dificuldades identificadas:\n");
        if (context.listeningDifficulty != null) {
            prompt.append("- Compreensão auditiva: ").append(translateDifficulty(context.listeningDifficulty)).append("\n");
        }
        if (context.speakingDifficulty != null) {
            prompt.append("- Fala: ").append(translateDifficulty(context.speakingDifficulty)).append("\n");
        }
        if (context.readingDifficulty != null) {
            prompt.append("- Leitura: ").append(translateDifficulty(context.readingDifficulty)).append("\n");
        }
        if (context.writingDifficulty != null) {
            prompt.append("- Escrita: ").append(translateDifficulty(context.writingDifficulty)).append("\n");
        }

        prompt.append("\n## Instruções:\n");
        prompt.append("1. Crie módulos focados nas necessidades específicas do estudante\n");
        prompt.append("2. Priorize as habilidades com maior dificuldade\n");
        prompt.append("3. Use os formatos preferidos do estudante\n");
        prompt.append("4. Inclua conteúdo relacionado aos objetivos declarados\n");
        prompt.append("5. Adapte a duração das lições ao tempo disponível\n\n");

        prompt.append("## Formato de resposta (JSON):\n");
        prompt.append("```json\n");
        prompt.append("{\n");
        prompt.append("  \"trailTitle\": \"Título personalizado da trilha\",\n");
        prompt.append("  \"trailDescription\": \"Descrição da trilha explicando como ela atende os objetivos\",\n");
        prompt.append("  \"modules\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"title\": \"Nome do módulo\",\n");
        prompt.append("      \"description\": \"Descrição do módulo\",\n");
        prompt.append("      \"competency\": \"listening|speaking|reading|writing|vocabulary|grammar\",\n");
        prompt.append("      \"lessons\": [\n");
        prompt.append("        {\n");
        prompt.append("          \"title\": \"Título da lição\",\n");
        prompt.append("          \"type\": \"video|reading|exercise|conversation|flashcard|interactive\",\n");
        prompt.append("          \"durationMinutes\": 15,\n");
        prompt.append("          \"description\": \"Breve descrição do conteúdo\"\n");
        prompt.append("        }\n");
        prompt.append("      ]\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        prompt.append("```\n\n");
        prompt.append("Crie 4-6 módulos com 3-5 lições cada, totalizando aproximadamente 20-30 lições.\n");
        prompt.append("Responda APENAS com o JSON, sem texto adicional.");

        return prompt.toString();
    }

    /**
     * Monta o prompt para geração do conteúdo de uma lição.
     * Busca template da base de dados baseado na competência.
     */
    private String buildLessonContentPrompt(String lessonTitle, String lessonType,
            StudentContext context, String languageCode, String levelCode) {

        // Mapear tipo de lição para código de competência
        String competencyCode = mapLessonTypeToCompetency(lessonType);

        // Buscar template da base de dados
        List<PromptTemplate> templates = promptTemplateRepository.findLatestActiveByCompetencyCode(competencyCode);

        if (!templates.isEmpty()) {
            PromptTemplate template = templates.get(0);
            log.info("Usando template '{}' para competência '{}'", template.getName(), competencyCode);

            // Preparar valores para os slots do template
            Map<String, String> slotValues = new HashMap<>();
            slotValues.put("level", levelCode);
            slotValues.put("topic", lessonTitle);
            slotValues.put("topic_details", buildTopicDetails(context));
            slotValues.put("language", getLanguageName(languageCode));
            slotValues.put("objective", context.objectiveDescription != null ? context.objectiveDescription : "");
            slotValues.put("daily_time", context.dailyTime != null ? translateDailyTime(context.dailyTime) : "30 minutos");

            // Preencher template com os valores
            String filledTemplate = template.fillTemplate(slotValues);

            // Adicionar instruções de formato JSON usando o outputSchema do template
            StringBuilder prompt = new StringBuilder(filledTemplate);
            prompt.append("\n\n## IMPORTANTE - Formato de Resposta:\n");
            prompt.append("Você DEVE responder APENAS com um objeto JSON válido, sem nenhum texto adicional.\n");
            prompt.append("NÃO use markdown, NÃO adicione explicações antes ou depois do JSON.\n");

            if (template.getOutputSchema() != null && !template.getOutputSchema().isBlank()) {
                prompt.append("\nEsquema JSON esperado:\n```json\n");
                prompt.append(template.getOutputSchema());
                prompt.append("\n```\n");
            } else {
                // Schema padrão quando não há outputSchema definido
                prompt.append("\nEstrutura JSON mínima esperada:\n```json\n");
                prompt.append("{\n");
                prompt.append("  \"title\": \"Título da lição\",\n");
                prompt.append("  \"type\": \"").append(lessonType).append("\",\n");
                prompt.append("  \"content\": { ... conteúdo específico do tipo ... },\n");
                prompt.append("  \"exercises\": [ ... exercícios práticos ... ]\n");
                prompt.append("}\n```\n");
            }

            prompt.append("\nResponda SOMENTE com o JSON, começando com { e terminando com }.");
            return prompt.toString();
        }

        // Fallback: prompt genérico se não houver template
        log.warn("Template não encontrado para competência '{}'. Usando prompt genérico.", competencyCode);
        return buildFallbackPrompt(lessonTitle, lessonType, context, languageCode, levelCode);
    }

    /**
     * Mapeia tipo de lição para código de competência.
     */
    private String mapLessonTypeToCompetency(String lessonType) {
        return switch (lessonType.toLowerCase()) {
            case "video", "listening" -> "listening";
            case "conversation", "speaking" -> "speaking";
            case "reading" -> "reading";
            case "exercise", "writing" -> "writing";
            case "flashcard", "vocabulary" -> "vocabulary";
            case "interactive", "grammar" -> "grammar";
            default -> "grammar";
        };
    }

    /**
     * Constrói detalhes do tema baseado no contexto do estudante.
     */
    private String buildTopicDetails(StudentContext context) {
        StringBuilder details = new StringBuilder();

        if (context.objectiveDescription != null && !context.objectiveDescription.isBlank()) {
            details.append(context.objectiveDescription);
        }

        if (context.primaryObjective != null) {
            if (details.length() > 0) details.append(". ");
            details.append("Objetivo: ").append(translateObjective(context.primaryObjective));
        }

        if (context.topicsOfInterest != null && !context.topicsOfInterest.isEmpty()) {
            if (details.length() > 0) details.append(". ");
            details.append("Interesses: ").append(String.join(", ", context.topicsOfInterest));
        }

        return details.length() > 0 ? details.toString() : "Aprendizado geral do idioma";
    }

    /**
     * Prompt de fallback quando não há template na base.
     */
    private String buildFallbackPrompt(String lessonTitle, String lessonType,
            StudentContext context, String languageCode, String levelCode) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Você é um especialista em ensino de ").append(getLanguageName(languageCode)).append(". ");
        prompt.append("Crie o conteúdo completo para a seguinte lição:\n\n");
        prompt.append("- Título: ").append(lessonTitle).append("\n");
        prompt.append("- Tipo: ").append(lessonType).append("\n");
        prompt.append("- Nível: ").append(levelCode).append("\n");

        if (context.objectiveDescription != null && !context.objectiveDescription.isBlank()) {
            prompt.append("- Contexto do estudante: ").append(context.objectiveDescription).append("\n");
        }

        prompt.append("\n## IMPORTANTE - Formato de Resposta:\n");
        prompt.append("Você DEVE responder APENAS com um objeto JSON válido, sem nenhum texto adicional.\n");
        prompt.append("NÃO use markdown, NÃO adicione explicações antes ou depois do JSON.\n\n");

        prompt.append("Estrutura JSON esperada:\n```json\n");
        prompt.append("{\n");
        prompt.append("  \"title\": \"").append(lessonTitle).append("\",\n");
        prompt.append("  \"type\": \"").append(lessonType).append("\",\n");
        prompt.append("  \"level\": \"").append(levelCode).append("\",\n");
        prompt.append("  \"content\": {\n");
        prompt.append("    \"introduction\": \"Texto introdutório\",\n");
        prompt.append("    \"mainContent\": \"Conteúdo principal da lição\",\n");
        prompt.append("    \"examples\": [\"exemplo1\", \"exemplo2\"]\n");
        prompt.append("  },\n");
        prompt.append("  \"exercises\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"question\": \"Pergunta do exercício\",\n");
        prompt.append("      \"options\": [\"A\", \"B\", \"C\", \"D\"],\n");
        prompt.append("      \"correctAnswer\": \"A\",\n");
        prompt.append("      \"explanation\": \"Explicação da resposta\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n```\n");
        prompt.append("\nResponda SOMENTE com o JSON, começando com { e terminando com }.");

        return prompt.toString();
    }

    /**
     * Extrai JSON de uma resposta que pode conter markdown ou texto adicional.
     * Sanitiza o JSON para garantir compatibilidade com PostgreSQL JSONB.
     */
    private String extractJsonFromResponse(String response) {
        if (response == null || response.isBlank()) {
            return "{}";
        }

        // Tentar encontrar JSON no response
        int jsonStart = response.indexOf("{");
        int jsonEnd = response.lastIndexOf("}");

        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            String json = response.substring(jsonStart, jsonEnd + 1);
            try {
                // Validar e sanitizar o JSON:
                // 1. Parse para validar estrutura
                // 2. Serializa de volta para garantir encoding correto
                Object parsed = objectMapper.readValue(json, Object.class);
                String sanitized = objectMapper.writeValueAsString(parsed);
                return sanitized;
            } catch (Exception e) {
                log.warn("JSON extraído não é válido: {}", e.getMessage());
            }
        }

        return "{}";
    }

    private String getLanguageName(String code) {
        return switch (code.toLowerCase()) {
            case "en" -> "Inglês";
            case "es" -> "Espanhol";
            case "fr" -> "Francês";
            case "de" -> "Alemão";
            case "it" -> "Italiano";
            case "pt" -> "Português";
            case "ja" -> "Japonês";
            case "ko" -> "Coreano";
            case "zh" -> "Chinês";
            default -> code;
        };
    }

    private String translateObjective(String objective) {
        return switch (objective) {
            case "CAREER" -> "Carreira profissional";
            case "UNIVERSITY" -> "Estudos acadêmicos";
            case "EXAMS" -> "Preparação para exames";
            case "TRAVEL" -> "Viagens";
            case "HOBBY" -> "Hobby/Interesse pessoal";
            case "IMMIGRATION" -> "Imigração";
            case "OTHER" -> "Outro";
            default -> objective;
        };
    }

    private String translateDailyTime(String time) {
        return switch (time) {
            case "MIN_15" -> "15 minutos";
            case "MIN_30" -> "30 minutos";
            case "MIN_45" -> "45 minutos";
            case "H_1" -> "1 hora";
            case "H_1_30" -> "1 hora e 30 minutos";
            case "H_2_PLUS" -> "2+ horas";
            default -> time;
        };
    }

    private String translateFormats(List<String> formats) {
        return formats.stream()
                .map(f -> switch (f) {
                    case "VIDEO_LESSONS" -> "Vídeo aulas";
                    case "WRITTEN_EXERCISES" -> "Exercícios escritos";
                    case "CONVERSATION" -> "Conversação";
                    case "GAMES" -> "Jogos";
                    case "READING" -> "Leitura";
                    case "AUDIO_PODCAST" -> "Áudio/Podcast";
                    case "FLASHCARDS" -> "Flashcards";
                    default -> f;
                })
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    private String translateDifficulty(String difficulty) {
        return switch (difficulty) {
            case "NONE" -> "Sem dificuldade";
            case "LOW" -> "Pouca dificuldade";
            case "MODERATE" -> "Dificuldade moderada";
            case "HIGH" -> "Alta dificuldade";
            default -> difficulty;
        };
    }

    private String getDefaultTrailStructure(String levelCode) {
        return """
            {
                "trailTitle": "Trilha de Aprendizado - %s",
                "trailDescription": "Trilha padrão para o nível %s",
                "modules": []
            }
            """.formatted(levelCode, levelCode);
    }

    private String getDefaultLessonContent(String lessonType) {
        return """
            {
                "type": "%s",
                "generated": false,
                "content": "Conteúdo padrão"
            }
            """.formatted(lessonType);
    }

    /**
     * Classe interna para armazenar o contexto do estudante.
     */
    private static class StudentContext {
        String dailyTime;
        List<String> preferredFormats;
        String primaryObjective;
        String secondaryObjective;
        String objectiveDescription;
        List<String> topicsOfInterest;
        String cefrLevel;
        String listeningDifficulty;
        String speakingDifficulty;
        String readingDifficulty;
        String writingDifficulty;
    }
}
