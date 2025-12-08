export type LessonStatus = 'completed' | 'in_progress' | 'locked';
export type LessonType = 'video' | 'quiz' | 'speaking' | 'reading' | 'writing';

export interface Lesson {
  id: string;
  title: string;
  description: string;
  type: LessonType;
  duration: number; // in minutes
  xpReward: number;
  status: LessonStatus;
  score?: number; // 0-100, only for completed lessons
  completedAt?: string;
  order: number;
  module: string;
}

export interface LessonModule {
  id: string;
  title: string;
  description: string;
  level: string;
  lessons: Lesson[];
  progress: number; // 0-100
}

export const mockLessons: Lesson[] = [
  // Module 1: Basics
  {
    id: 'lesson-1',
    title: 'Greetings & Introductions',
    description: 'Learn basic greetings and how to introduce yourself',
    type: 'video',
    duration: 15,
    xpReward: 50,
    status: 'completed',
    score: 95,
    completedAt: '2024-01-10T10:30:00Z',
    order: 1,
    module: 'basics',
  },
  {
    id: 'lesson-2',
    title: 'Greetings Quiz',
    description: 'Test your knowledge of basic greetings',
    type: 'quiz',
    duration: 10,
    xpReward: 30,
    status: 'completed',
    score: 88,
    completedAt: '2024-01-11T14:00:00Z',
    order: 2,
    module: 'basics',
  },
  {
    id: 'lesson-3',
    title: 'Practice: Introduce Yourself',
    description: 'Record yourself introducing yourself in the target language',
    type: 'speaking',
    duration: 8,
    xpReward: 40,
    status: 'completed',
    score: 92,
    completedAt: '2024-01-12T09:15:00Z',
    order: 3,
    module: 'basics',
  },
  {
    id: 'lesson-4',
    title: 'Numbers 1-20',
    description: 'Learn to count from 1 to 20',
    type: 'video',
    duration: 12,
    xpReward: 45,
    status: 'in_progress',
    order: 4,
    module: 'basics',
  },
  {
    id: 'lesson-5',
    title: 'Numbers Quiz',
    description: 'Practice your numbers',
    type: 'quiz',
    duration: 8,
    xpReward: 25,
    status: 'locked',
    order: 5,
    module: 'basics',
  },
  // Module 2: Daily Life
  {
    id: 'lesson-6',
    title: 'Daily Routine',
    description: 'Learn vocabulary for your daily activities',
    type: 'video',
    duration: 18,
    xpReward: 55,
    status: 'locked',
    order: 6,
    module: 'daily-life',
  },
  {
    id: 'lesson-7',
    title: 'Reading: A Day in the Life',
    description: 'Read a short story about a typical day',
    type: 'reading',
    duration: 12,
    xpReward: 35,
    status: 'locked',
    order: 7,
    module: 'daily-life',
  },
  {
    id: 'lesson-8',
    title: 'Writing: My Day',
    description: 'Write about your typical day',
    type: 'writing',
    duration: 15,
    xpReward: 45,
    status: 'locked',
    order: 8,
    module: 'daily-life',
  },
];

export const mockModules: LessonModule[] = [
  {
    id: 'basics',
    title: 'Basics',
    description: 'Fundamental vocabulary and expressions',
    level: 'A1',
    lessons: mockLessons.filter((l) => l.module === 'basics'),
    progress: 60,
  },
  {
    id: 'daily-life',
    title: 'Daily Life',
    description: 'Talk about your everyday activities',
    level: 'A1',
    lessons: mockLessons.filter((l) => l.module === 'daily-life'),
    progress: 0,
  },
];

export const getNextLesson = (): Lesson | null => {
  const inProgress = mockLessons.find((l) => l.status === 'in_progress');
  if (inProgress) return inProgress;

  const firstLocked = mockLessons.find((l) => l.status === 'locked');
  return firstLocked || null;
};

export const getLessonTypeIcon = (type: LessonType): string => {
  const icons: Record<LessonType, string> = {
    video: '🎬',
    quiz: '📝',
    speaking: '🎤',
    reading: '📖',
    writing: '✍️',
  };
  return icons[type];
};

export const getStatusColor = (status: LessonStatus): string => {
  const colors: Record<LessonStatus, string> = {
    completed: 'text-success',
    in_progress: 'text-primary',
    locked: 'text-text-light',
  };
  return colors[status];
};
