export interface Tutor {
  id: string;
  name: string;
  avatar?: string;
  flag: string;
  country: string;
  languages: string[];
  specialties: string[];
  rating: number;
  reviewCount: number;
  hourlyRate: number;
  currency: string;
  isSuperTutor: boolean;
  isOnline: boolean;
  bio: string;
  yearsExperience: number;
  lessonsCompleted: number;
  responseTime: string; // e.g., "< 1 hour"
  bgColor: string;
}

export const mockTutors: Tutor[] = [
  {
    id: 'tutor-1',
    name: 'Sarah Mitchell',
    flag: '🇬🇧',
    country: 'United Kingdom',
    languages: ['English', 'Spanish'],
    specialties: ['Business English', 'IELTS', 'Conversation'],
    rating: 4.9,
    reviewCount: 234,
    hourlyRate: 35,
    currency: 'USD',
    isSuperTutor: true,
    isOnline: true,
    bio: 'Native British English teacher with 8 years of experience. I specialize in Business English and exam preparation.',
    yearsExperience: 8,
    lessonsCompleted: 1250,
    responseTime: '< 1 hour',
    bgColor: '#DBEAFE',
  },
  {
    id: 'tutor-2',
    name: 'Carlos García',
    flag: '🇪🇸',
    country: 'Spain',
    languages: ['Spanish', 'English', 'Portuguese'],
    specialties: ['Conversación', 'DELE', 'Grammar'],
    rating: 4.8,
    reviewCount: 189,
    hourlyRate: 28,
    currency: 'USD',
    isSuperTutor: true,
    isOnline: false,
    bio: 'Certified Spanish teacher from Madrid. Fun and interactive lessons for all levels!',
    yearsExperience: 6,
    lessonsCompleted: 890,
    responseTime: '< 2 hours',
    bgColor: '#FCE7F3',
  },
  {
    id: 'tutor-3',
    name: 'Marie Dubois',
    flag: '🇫🇷',
    country: 'France',
    languages: ['French', 'English'],
    specialties: ['French Culture', 'Pronunciation', 'Business French'],
    rating: 4.7,
    reviewCount: 156,
    hourlyRate: 32,
    currency: 'USD',
    isSuperTutor: false,
    isOnline: true,
    bio: 'Parisian French teacher. Learn French the natural way with conversation-based lessons.',
    yearsExperience: 5,
    lessonsCompleted: 720,
    responseTime: '< 3 hours',
    bgColor: '#E0E7FF',
  },
  {
    id: 'tutor-4',
    name: 'Hans Weber',
    flag: '🇩🇪',
    country: 'Germany',
    languages: ['German', 'English'],
    specialties: ['German Grammar', 'TestDaF', 'Business German'],
    rating: 4.9,
    reviewCount: 98,
    hourlyRate: 38,
    currency: 'USD',
    isSuperTutor: true,
    isOnline: true,
    bio: 'Professional German teacher from Berlin. I make German grammar easy and fun!',
    yearsExperience: 10,
    lessonsCompleted: 1100,
    responseTime: '< 1 hour',
    bgColor: '#FEF3C7',
  },
  {
    id: 'tutor-5',
    name: 'Yuki Tanaka',
    flag: '🇯🇵',
    country: 'Japan',
    languages: ['Japanese', 'English'],
    specialties: ['JLPT Preparation', 'Conversation', 'Kanji'],
    rating: 4.8,
    reviewCount: 145,
    hourlyRate: 30,
    currency: 'USD',
    isSuperTutor: false,
    isOnline: false,
    bio: 'Learn Japanese with a native speaker from Tokyo. From hiragana to advanced conversation!',
    yearsExperience: 4,
    lessonsCompleted: 580,
    responseTime: '< 4 hours',
    bgColor: '#F3E8FF',
  },
  {
    id: 'tutor-6',
    name: 'Isabella Romano',
    flag: '🇮🇹',
    country: 'Italy',
    languages: ['Italian', 'English', 'Spanish'],
    specialties: ['Italian Culture', 'Cooking Vocabulary', 'Travel Italian'],
    rating: 4.9,
    reviewCount: 167,
    hourlyRate: 26,
    currency: 'USD',
    isSuperTutor: true,
    isOnline: true,
    bio: 'Ciao! Italian teacher from Florence. Learn Italian through culture, food, and fun conversations!',
    yearsExperience: 7,
    lessonsCompleted: 950,
    responseTime: '< 2 hours',
    bgColor: '#D1FAE5',
  },
];

export const getOnlineTutors = (): Tutor[] => {
  return mockTutors.filter((t) => t.isOnline);
};

export const getSuperTutors = (): Tutor[] => {
  return mockTutors.filter((t) => t.isSuperTutor);
};

export const getRecommendedTutors = (limit = 3): Tutor[] => {
  // Return top-rated tutors, prioritizing online ones
  return [...mockTutors]
    .sort((a, b) => {
      if (a.isOnline && !b.isOnline) return -1;
      if (!a.isOnline && b.isOnline) return 1;
      return b.rating - a.rating;
    })
    .slice(0, limit);
};

export const searchTutors = (query: string): Tutor[] => {
  const lowerQuery = query.toLowerCase();
  return mockTutors.filter(
    (t) =>
      t.name.toLowerCase().includes(lowerQuery) ||
      t.languages.some((l) => l.toLowerCase().includes(lowerQuery)) ||
      t.specialties.some((s) => s.toLowerCase().includes(lowerQuery))
  );
};

export const filterTutors = (filters: {
  online?: boolean;
  superTutor?: boolean;
  language?: string;
  maxPrice?: number;
}): Tutor[] => {
  return mockTutors.filter((t) => {
    if (filters.online !== undefined && t.isOnline !== filters.online) return false;
    if (filters.superTutor !== undefined && t.isSuperTutor !== filters.superTutor) return false;
    if (filters.language && !t.languages.includes(filters.language)) return false;
    if (filters.maxPrice && t.hourlyRate > filters.maxPrice) return false;
    return true;
  });
};
