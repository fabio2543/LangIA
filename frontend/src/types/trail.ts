// ============================================
// Trail Types
// ============================================

export type TrailStatus = 'GENERATING' | 'PARTIAL' | 'READY' | 'ARCHIVED';

export type ModuleStatus = 'PENDING' | 'READY';

export type LessonType =
  | 'interactive'
  | 'video'
  | 'reading'
  | 'exercise'
  | 'conversation'
  | 'flashcard'
  | 'game';

export type GenerationJobStatus =
  | 'QUEUED'
  | 'PROCESSING'
  | 'COMPLETED'
  | 'FAILED'
  | 'CANCELLED';

export type RefreshReason =
  | 'level_change'
  | 'preferences_update'
  | 'curriculum_update'
  | 'manual_request';

// ============================================
// Trail DTOs
// ============================================

export interface Lesson {
  id: string;
  moduleId: string;
  title: string;
  type: LessonType;
  orderIndex: number;
  durationMinutes: number;
  content: Record<string, unknown>;
  isPlaceholder: boolean;
  completedAt: string | null;
  score: number | null;
  timeSpentSeconds: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface TrailModule {
  id: string;
  trailId: string;
  title: string;
  description: string | null;
  orderIndex: number;
  status: ModuleStatus;
  competencyCode: string;
  competencyName: string;
  lessons: Lesson[];
  createdAt: string;
  updatedAt: string;
}

export interface TrailProgress {
  id: string;
  trailId: string;
  totalLessons: number;
  lessonsCompleted: number;
  progressPercentage: number;
  averageScore: number | null;
  timeSpentMinutes: number;
  lastActivityAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface Trail {
  id: string;
  studentId: string;
  languageCode: string;
  languageName: string;
  languageFlag: string | null;
  levelCode: string;
  levelName: string;
  status: TrailStatus;
  contentHash: string;
  curriculumVersion: string;
  estimatedDurationHours: number | null;
  blueprintId: string | null;
  previousTrailId: string | null;
  refreshReason: RefreshReason | null;
  modules: TrailModule[];
  progress: TrailProgress | null;
  archivedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface TrailSummary {
  id: string;
  languageCode: string;
  languageName: string;
  languageFlag: string | null;
  levelCode: string;
  levelName: string;
  status: TrailStatus;
  progressPercentage: number | null;
  lessonsCompleted: number;
  totalLessons: number;
  averageScore: number | null;
  timeSpentMinutes: number;
  lastActivityAt: string | null;
  createdAt: string;
}

// ============================================
// Generation Status
// ============================================

export interface TrailGenerationStatus {
  trailId: string;
  jobId: string | null;
  trailStatus: TrailStatus;
  jobStatus: GenerationJobStatus;
  progressPercentage: number;
  currentStep: string;
  message: string;
  modulesGenerated: number;
  totalModules: number;
  lessonsGenerated: number;
  totalLessons: number;
  startedAt: string | null;
  estimatedCompletionAt: string | null;
  errorMessage: string | null;
  attemptNumber: number;
  maxAttempts: number;
}

// ============================================
// Request DTOs
// ============================================

export interface GenerateTrailRequest {
  languageCode: string;
  forceRegenerate?: boolean;
}

export interface RefreshTrailRequest {
  reason: RefreshReason;
  preserveProgress?: boolean;
  newLevelCode?: string;
  notes?: string;
}

export interface UpdateLessonProgressRequest {
  completed?: boolean;
  score?: number;
  timeSpentSeconds?: number;
  userResponses?: Record<string, unknown>;
}

// ============================================
// Curriculum Types
// ============================================

export interface Level {
  id: string;
  code: string;
  name: string;
  description: string | null;
  orderIndex: number;
}

export interface Competency {
  id: string;
  code: string;
  name: string;
  description: string | null;
  category: string | null;
  icon: string | null;
  orderIndex: number;
}

export interface Descriptor {
  id: string;
  code: string;
  description: string;
  descriptionEn: string | null;
  levelCode: string;
  competencyCode: string;
  orderIndex: number;
  isCore: boolean;
  estimatedHours: number | null;
}

// ============================================
// Language Enrollment Types
// ============================================

export interface LanguageEnrollment {
  id: string;
  languageCode: string;
  languageNamePt: string;
  languageNameEn: string;
  languageNameEs: string;
  cefrLevel: string;
  isPrimary: boolean;
  enrolledAt: string;
  lastStudiedAt: string | null;
}
