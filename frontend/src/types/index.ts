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
