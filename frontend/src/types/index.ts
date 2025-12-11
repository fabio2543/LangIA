export type Locale = 'pt' | 'en' | 'es';

export interface Teacher {
  id: string;
  name: string;
  flag: string;
  role: string;
  accent: string;
  reviews: string;
  tags: string[];
  superTutor: boolean;
  bgColor: string;
  image?: string;
}

export interface FAQ {
  id: string;
  question: string;
  answer: string;
}

export interface Goal {
  id: string;
  emoji: string;
  label: string;
}

export interface Benefit {
  icon: string;
  text: string;
}

export interface Value {
  icon: string;
  title: string;
  description: string;
}

export interface ProgressTrack {
  icon: string;
  name: string;
  progress: number;
}

export interface Testimonial {
  id: string;
  name: string;
  flag: string;
  course: string;
  quote: string;
  image?: string;
}

export interface NavLink {
  label: string;
  href: string;
}

export interface FooterLinks {
  company: string[];
  product: string[];
  legal: string[];
}

// ============================================
// Auth Types
// ============================================

export type UserProfile = 'STUDENT' | 'TEACHER' | 'ADMIN';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: string;
  name: string;
  email: string;
  profile: UserProfile;
  permissions: string[];
  expiresIn: number;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  cpf: string;
  phone: string;
  profile: UserProfile;
}

export interface UserResponse {
  id: string;
  name: string;
  email: string;
  cpf: string;
  phone: string;
  profile: UserProfile;
}

export interface AuthUser {
  id: string;
  name: string;
  email: string;
  profile: UserProfile;
  permissions: string[];
}

export interface AuthState {
  user: AuthUser | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

// ============================================
// Email Verification Types
// ============================================

export interface RegisterResponse {
  userId: string;
  name: string;
  maskedEmail: string;
  emailVerificationRequired: boolean;
  message: string;
}

export interface ResendVerificationRequest {
  userId: string;
}

export interface ResendVerificationResponse {
  success: boolean;
  maskedEmail?: string;
  message: string;
  remainingResends?: number;
  retryAfterSeconds?: number;
}

export interface EmailVerificationResponse {
  success: boolean;
  error?: 'TOKEN_INVALID' | 'TOKEN_EXPIRED' | 'TOKEN_USED';
  message?: string;
}

export interface PendingVerificationResponse {
  userId: string;
  maskedEmail: string;
  emailVerificationRequired: boolean;
  message: string;
}

// ============================================
// Student Profile - Union Types (Enums)
// ============================================

export type CefrLevel = 'A1' | 'A2' | 'B1' | 'B2' | 'C1' | 'C2';

export type DifficultyLevel = 'NONE' | 'LOW' | 'MODERATE' | 'HIGH';

export type TimeAvailable = 'MIN_15' | 'MIN_30' | 'MIN_45' | 'H_1' | 'H_1_30' | 'H_2_PLUS';

export type ReminderFrequency = 'DAILY' | 'ALTERNATE_DAYS' | 'WEEKLY' | 'CUSTOM';

export type LearningObjective =
  | 'CAREER'
  | 'UNIVERSITY'
  | 'EXAMS'
  | 'TRAVEL'
  | 'HOBBY'
  | 'IMMIGRATION'
  | 'OTHER';

export type LearningFormat =
  | 'VIDEO_LESSONS'
  | 'WRITTEN_EXERCISES'
  | 'CONVERSATION'
  | 'GAMES'
  | 'READING'
  | 'AUDIO_PODCAST'
  | 'FLASHCARDS';

export type StudyDayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

export type TimeOfDay = 'MORNING' | 'AFTERNOON' | 'EVENING' | 'NIGHT';

export type NotificationChannel = 'PUSH' | 'EMAIL' | 'WHATSAPP';

export type NotificationCategory =
  | 'SECURITY'
  | 'STUDY_REMINDERS'
  | 'PROGRESS'
  | 'CONTENT'
  | 'CLASS'
  | 'MARKETING';

// ============================================
// Student Profile - Language Types
// ============================================

export interface Language {
  code: string;
  namePt: string;
  nameEn: string;
  nameEs: string;
  active: boolean;
}

export interface LanguageEnrollment {
  id: string;
  languageCode: string;
  languageNamePt: string;
  languageNameEn: string;
  languageNameEs: string;
  cefrLevel: CefrLevel | null;
  isPrimary: boolean;
  enrolledAt: string;
  lastStudiedAt: string | null;
}

export interface EnrollLanguageRequest {
  languageCode: string;
  cefrLevel?: CefrLevel;
  isPrimary?: boolean;
}

export interface UpdateLanguageEnrollmentRequest {
  cefrLevel?: CefrLevel;
  isPrimary?: boolean;
}

// ============================================
// Student Profile - Interfaces
// ============================================

// Personal Data
export interface UserProfileDetails {
  id: string;
  fullName: string;
  email: string;
  whatsappPhone: string;
  nativeLanguage: string;
  timezone: string;
  birthDate?: string;
  bio?: string;
}

export interface UpdatePersonalDataRequest {
  fullName?: string;
  whatsappPhone?: string;
  nativeLanguage?: string;
  timezone?: string;
  birthDate?: string;
  bio?: string;
}

// Email Change
export interface RequestEmailChangeRequest {
  newEmail: string;
}

export interface VerifyEmailChangeRequest {
  code: string;
}

// Learning Preferences (idiomas agora são gerenciados separadamente via LanguageEnrollment)
export interface LearningPreferences {
  dailyTimeAvailable: TimeAvailable | null;
  preferredDays: StudyDayOfWeek[];
  preferredTimes?: TimeOfDay[];
  weeklyHoursGoal?: number;
  topicsOfInterest: string[];
  customTopics?: string[];
  preferredFormats: LearningFormat[];
  formatRanking?: LearningFormat[];
  primaryObjective?: LearningObjective;
  objectiveDescription?: string;
  objectiveDeadline?: string;
}

// Skill Assessment
export interface SkillAssessment {
  language: string;
  listeningDifficulty: DifficultyLevel;
  speakingDifficulty: DifficultyLevel;
  readingDifficulty: DifficultyLevel;
  writingDifficulty: DifficultyLevel;
  listeningDetails?: string[];
  speakingDetails?: string[];
  readingDetails?: string[];
  writingDetails?: string[];
  selfCefrLevel?: CefrLevel;
}

export interface SkillAssessmentResponse extends SkillAssessment {
  id: string;
  assessedAt: string;
}

// Notification Settings
export interface CategoryPreference {
  active: boolean;
  channels: NotificationChannel[];
}

export interface NotificationSettings {
  activeChannels: Record<NotificationChannel, boolean>;
  categoryPreferences: Record<NotificationCategory, CategoryPreference>;
  reminderFrequency: ReminderFrequency;
  preferredTimeStart?: string;
  preferredTimeEnd?: string;
  quietModeStart?: string;
  quietModeEnd?: string;
}

// ============================================
// Linguistic Chunks
// ============================================

export type ChunkCategory =
  | 'greeting'
  | 'request'
  | 'question'
  | 'direction'
  | 'emergency'
  | 'social'
  | 'shopping'
  | 'travel';

export interface LinguisticChunk {
  id: string;
  languageCode: string;
  cefrLevel: CefrLevel;
  chunkText: string;
  translation: string;
  category: ChunkCategory;
  usageContext?: string;
  variations: string[];
  audioUrl?: string;
  difficultyScore: number;
  isCore: boolean;
}

export interface ChunkMastery {
  id: string;
  chunkId: string;
  chunk?: LinguisticChunk;
  masteryLevel: number;
  timesPracticed: number;
  lastPracticedAt?: string;
  contextsUsed: string[];
}

// ============================================
// Vocabulary & SRS (Spaced Repetition System)
// ============================================

export type CardType = 'word' | 'chunk' | 'phrase' | 'grammar';
export type SrsQuality = 0 | 1 | 2 | 3 | 4 | 5;

export interface VocabularyCard {
  id: string;
  languageCode: string;
  cefrLevel: CefrLevel;
  cardType: CardType;
  front: string;
  back: string;
  context?: string;
  exampleSentence?: string;
  audioUrl?: string;
  imageUrl?: string;
  tags: string[];
  isSystemCard: boolean;
}

export interface SrsProgress {
  id: string;
  cardId: string;
  card?: VocabularyCard;
  easinessFactor: number;
  intervalDays: number;
  repetitions: number;
  nextReviewDate: string;
  lastReviewedAt?: string;
  lastQuality?: SrsQuality;
  totalReviews: number;
  correctReviews: number;
}

export interface SrsReviewRequest {
  cardId: string;
  quality: SrsQuality;
}

export interface SrsReviewResponse {
  nextReviewDate: string;
  intervalDays: number;
  easinessFactor: number;
}

export interface SrsCardWithProgress extends VocabularyCard {
  progress: SrsProgress;
}

export interface SrsDueCardsResponse {
  cards: SrsCardWithProgress[];
  totalDue: number;
  reviewedToday: number;
}

export interface SrsStats {
  totalCards: number;
  mastered: number;
  learning: number;
  newCards: number;
  dueToday: number;
  reviewedToday: number;
}

// ============================================
// Exercise Tracking & Error Patterns
// ============================================

export type ExerciseType =
  | 'listen'
  | 'select'
  | 'speak'
  | 'write'
  | 'fill_blank'
  | 'match'
  | 'order';

export type SkillType =
  | 'listening'
  | 'speaking'
  | 'reading'
  | 'writing'
  | 'grammar'
  | 'vocabulary'
  | 'pronunciation';

export type ErrorCategory =
  | 'verb_conjugation'
  | 'article_usage'
  | 'pronunciation'
  | 'word_order'
  | 'vocabulary_choice'
  | 'spelling'
  | 'preposition';

export interface ExerciseResponse {
  id: string;
  lessonId?: string;
  exerciseId?: string;
  exerciseType: ExerciseType;
  skillType: SkillType;
  languageCode: string;
  userResponse?: string;
  correctResponse?: string;
  isCorrect: boolean;
  partialScore?: number;
  errorType?: string;
  errorDetails?: Record<string, unknown>;
  responseTimeMs?: number;
  hintsUsed: number;
  createdAt: string;
}

export interface SubmitExerciseRequest {
  lessonId?: string;
  exerciseId?: string;
  exerciseType: ExerciseType;
  skillType: SkillType;
  languageCode: string;
  userResponse: string;
  correctResponse: string;
  responseTimeMs?: number;
  hintsUsed?: number;
}

export interface ErrorPattern {
  id: string;
  languageCode: string;
  skillType: SkillType;
  errorCategory: ErrorCategory;
  errorDescription: string;
  exampleErrors: string[];
  occurrenceCount: number;
  firstOccurredAt: string;
  lastOccurredAt: string;
  isResolved: boolean;
}

// ============================================
// Skill Metrics
// ============================================

export interface SkillMetric {
  id: string;
  languageCode: string;
  skillType: SkillType;
  metricDate: string;
  exercisesCompleted: number;
  correctAnswers: number;
  accuracyPercentage: number;
  avgResponseTimeMs?: number;
  totalPracticeTimeMinutes: number;
  xpEarned: number;
}

export type MetricTrend = 'improving' | 'stable' | 'declining';

export interface SkillMetricsSummary {
  skillType: SkillType;
  totalExercises: number;
  avgAccuracy: number;
  avgResponseTimeMs: number;
  trend: MetricTrend;
}

export interface DailyProgressPoint {
  date: string;
  exercisesCompleted: number;
  accuracyPercentage: number;
}

// ============================================
// Streaks & Daily Activity
// ============================================

export interface DailyStreak {
  id: string;
  languageCode: string;
  currentStreak: number;
  longestStreak: number;
  lastStudyDate?: string;
  streakStartedAt?: string;
  streakFrozenUntil?: string;
  totalStudyDays: number;
}

export interface DailyActivityLog {
  id: string;
  languageCode: string;
  activityDate: string;
  lessonsStarted: number;
  lessonsCompleted: number;
  exercisesCompleted: number;
  cardsReviewed: number;
  minutesStudied: number;
  xpEarned: number;
  skillsPracticed: SkillType[];
}

export interface ActivitySummary {
  totalLessons: number;
  totalExercises: number;
  totalCardsReviewed: number;
  totalMinutes: number;
  totalXp: number;
  activeDays: number;
  avgMinutesPerDay: number;
}

// ============================================
// Socratic Interactions (AI Feedback)
// ============================================

export interface SocraticInteraction {
  id: string;
  lessonId?: string;
  exerciseId?: string;
  languageCode: string;
  skillType?: SkillType;
  userInput: string;
  aiQuestion: string;
  userReflection?: string;
  aiFollowUp?: string;
  finalCorrection?: string;
  learningMoment?: string;
  selfCorrectionAchieved: boolean;
  interactionRounds: number;
  userRating?: number;
  tokensUsed?: number;
  createdAt: string;
}

export interface SocraticFeedbackRequest {
  lessonId?: string;
  exerciseId?: string;
  languageCode: string;
  skillType: SkillType;
  userInput: string;
  expectedOutput?: string;
  errorContext?: string;
}

export interface SocraticFeedbackResponse {
  interactionId: string;
  aiQuestion: string;
  hints?: string[];
}

export interface SocraticReflectionRequest {
  interactionId: string;
  userReflection: string;
}

export interface SocraticReflectionResponse {
  needsFollowUp: boolean;
  aiFollowUp?: string;
  finalCorrection?: string;
  learningMoment: string;
  selfCorrectionAchieved: boolean;
}
