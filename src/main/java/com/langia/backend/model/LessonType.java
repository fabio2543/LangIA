package com.langia.backend.model;

/**
 * Tipos de lição disponíveis na plataforma.
 * Corresponde ao enum lesson_type no banco de dados.
 */
public enum LessonType {
    /**
     * Lição interativa com exercícios dinâmicos
     */
    interactive,

    /**
     * Conteúdo em vídeo (aulas gravadas ou geradas)
     */
    video,

    /**
     * Material de leitura (textos, artigos)
     */
    reading,

    /**
     * Exercícios práticos de gramática/vocabulário
     */
    exercise,

    /**
     * Prática de conversação (com IA ou pares)
     */
    conversation,

    /**
     * Cartões de memorização (vocabulário, frases)
     */
    flashcard,

    /**
     * Jogos educativos e gamificação
     */
    game
}
