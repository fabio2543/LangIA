package com.langia.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "socratic_interactions", indexes = {
    @Index(name = "idx_socratic_user", columnList = "user_id"),
    @Index(name = "idx_socratic_user_date", columnList = "user_id, created_at DESC"),
    @Index(name = "idx_socratic_lesson", columnList = "lesson_id"),
    @Index(name = "idx_socratic_skill", columnList = "user_id, skill_type")
})
public class SocraticInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Column(name = "lesson_id")
    private UUID lessonId;

    @Column(name = "exercise_id")
    private UUID exerciseId;

    @Column(name = "skill_type", length = 20)
    private String skillType;

    @Column(name = "interaction_type", nullable = false, length = 50)
    private String interactionType = "correction";

    // Interaction flow
    @Column(name = "user_input", nullable = false, columnDefinition = "TEXT")
    private String userInput;

    @Column(name = "ai_question", nullable = false, columnDefinition = "TEXT")
    private String aiQuestion;

    @Column(name = "user_reflection", columnDefinition = "TEXT")
    private String userReflection;

    @Column(name = "ai_follow_up", columnDefinition = "TEXT")
    private String aiFollowUp;

    @Column(name = "user_second_attempt", columnDefinition = "TEXT")
    private String userSecondAttempt;

    @Column(name = "final_correction", columnDefinition = "TEXT")
    private String finalCorrection;

    // Pedagogical result
    @Column(name = "learning_moment", columnDefinition = "TEXT")
    private String learningMoment;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "concepts_addressed", columnDefinition = "jsonb")
    private List<String> conceptsAddressed;

    @Column(name = "user_understood")
    private Boolean userUnderstood;

    @Column(name = "self_correction_achieved")
    private Boolean selfCorrectionAchieved;

    // Metrics
    @Column(name = "interaction_rounds")
    private Integer interactionRounds = 1;

    @Column(name = "total_time_seconds")
    private Integer totalTimeSeconds;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "model_used", length = 50)
    private String modelUsed;

    // Evaluation
    @Column(name = "user_rating")
    private Integer userRating;

    @Column(name = "was_helpful")
    private Boolean wasHelpful;

    @Column(name = "feedback_notes", columnDefinition = "TEXT")
    private String feedbackNotes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public UUID getLessonId() {
        return lessonId;
    }

    public void setLessonId(UUID lessonId) {
        this.lessonId = lessonId;
    }

    public UUID getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(UUID exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getSkillType() {
        return skillType;
    }

    public void setSkillType(String skillType) {
        this.skillType = skillType;
    }

    public String getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(String interactionType) {
        this.interactionType = interactionType;
    }

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    public String getAiQuestion() {
        return aiQuestion;
    }

    public void setAiQuestion(String aiQuestion) {
        this.aiQuestion = aiQuestion;
    }

    public String getUserReflection() {
        return userReflection;
    }

    public void setUserReflection(String userReflection) {
        this.userReflection = userReflection;
    }

    public String getAiFollowUp() {
        return aiFollowUp;
    }

    public void setAiFollowUp(String aiFollowUp) {
        this.aiFollowUp = aiFollowUp;
    }

    public String getUserSecondAttempt() {
        return userSecondAttempt;
    }

    public void setUserSecondAttempt(String userSecondAttempt) {
        this.userSecondAttempt = userSecondAttempt;
    }

    public String getFinalCorrection() {
        return finalCorrection;
    }

    public void setFinalCorrection(String finalCorrection) {
        this.finalCorrection = finalCorrection;
    }

    public String getLearningMoment() {
        return learningMoment;
    }

    public void setLearningMoment(String learningMoment) {
        this.learningMoment = learningMoment;
    }

    public List<String> getConceptsAddressed() {
        return conceptsAddressed;
    }

    public void setConceptsAddressed(List<String> conceptsAddressed) {
        this.conceptsAddressed = conceptsAddressed;
    }

    public Boolean getUserUnderstood() {
        return userUnderstood;
    }

    public void setUserUnderstood(Boolean userUnderstood) {
        this.userUnderstood = userUnderstood;
    }

    public Boolean getSelfCorrectionAchieved() {
        return selfCorrectionAchieved;
    }

    public void setSelfCorrectionAchieved(Boolean selfCorrectionAchieved) {
        this.selfCorrectionAchieved = selfCorrectionAchieved;
    }

    public Integer getInteractionRounds() {
        return interactionRounds;
    }

    public void setInteractionRounds(Integer interactionRounds) {
        this.interactionRounds = interactionRounds;
    }

    public Integer getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    public void setTotalTimeSeconds(Integer totalTimeSeconds) {
        this.totalTimeSeconds = totalTimeSeconds;
    }

    public Integer getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public Integer getUserRating() {
        return userRating;
    }

    public void setUserRating(Integer userRating) {
        this.userRating = userRating;
    }

    public Boolean getWasHelpful() {
        return wasHelpful;
    }

    public void setWasHelpful(Boolean wasHelpful) {
        this.wasHelpful = wasHelpful;
    }

    public String getFeedbackNotes() {
        return feedbackNotes;
    }

    public void setFeedbackNotes(String feedbackNotes) {
        this.feedbackNotes = feedbackNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public void addFollowUpRound(String followUp, String userAttempt) {
        this.aiFollowUp = followUp;
        this.userSecondAttempt = userAttempt;
        this.interactionRounds++;
    }

    public void completeInteraction(boolean understood, boolean selfCorrected, String correction, String learning) {
        this.userUnderstood = understood;
        this.selfCorrectionAchieved = selfCorrected;
        this.finalCorrection = correction;
        this.learningMoment = learning;
    }

    public void rate(int rating, boolean helpful, String feedback) {
        this.userRating = rating;
        this.wasHelpful = helpful;
        this.feedbackNotes = feedback;
    }
}
