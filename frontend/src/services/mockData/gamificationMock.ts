export interface UserProgress {
  streak: number;
  maxStreak: number;
  totalXp: number;
  currentLevel: string;
  nextLevel: string;
  progressPercent: number;
  lessonsCompleted: number;
  hoursStudied: number;
}

export interface Achievement {
  id: string;
  name: string;
  description: string;
  icon: string;
  unlockedAt: string | null;
  progress?: number;
  maxProgress?: number;
}

export const mockUserProgress: UserProgress = {
  streak: 12,
  maxStreak: 15,
  totalXp: 1450,
  currentLevel: 'A2',
  nextLevel: 'B1',
  progressPercent: 23,
  lessonsCompleted: 24,
  hoursStudied: 18.5,
};

export const mockAchievements: Achievement[] = [
  {
    id: 'first-lesson',
    name: 'First Steps',
    description: 'Complete your first lesson',
    icon: '🎯',
    unlockedAt: '2024-01-15T10:30:00Z',
  },
  {
    id: 'week-streak',
    name: 'Week Warrior',
    description: 'Study 7 days in a row',
    icon: '🔥',
    unlockedAt: '2024-01-20T14:00:00Z',
  },
  {
    id: 'vocabulary-master',
    name: 'Word Collector',
    description: 'Learn 100 new words',
    icon: '📚',
    unlockedAt: null,
    progress: 78,
    maxProgress: 100,
  },
  {
    id: 'speaking-star',
    name: 'Speaking Star',
    description: 'Complete 10 speaking exercises',
    icon: '🎤',
    unlockedAt: null,
    progress: 6,
    maxProgress: 10,
  },
  {
    id: 'perfect-score',
    name: 'Perfectionist',
    description: 'Get 100% on 5 quizzes',
    icon: '⭐',
    unlockedAt: '2024-01-25T09:15:00Z',
  },
];

export const getXpForLevel = (level: string): number => {
  const xpMap: Record<string, number> = {
    A1: 0,
    A2: 500,
    B1: 1500,
    B2: 3500,
    C1: 6500,
    C2: 10000,
  };
  return xpMap[level] || 0;
};

export const calculateProgress = (currentXp: number, currentLevel: string, nextLevel: string): number => {
  const currentLevelXp = getXpForLevel(currentLevel);
  const nextLevelXp = getXpForLevel(nextLevel);
  const xpInLevel = currentXp - currentLevelXp;
  const xpNeeded = nextLevelXp - currentLevelXp;
  return Math.round((xpInLevel / xpNeeded) * 100);
};
