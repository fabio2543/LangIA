export type ExerciseType = 'listen' | 'select' | 'speak';

export interface Exercise {
  id: string;
  lessonId: string;
  type: ExerciseType;
  prompt: string;
  content?: string;
  options?: string[];
  correctAnswer?: number;
  xpReward: number;
}

// Exercises for each lesson
export const mockExercises: Exercise[] = [
  // Lesson 1: Basic Greetings (completed)
  {
    id: 'ex-1-1',
    lessonId: 'lesson-1',
    type: 'listen',
    prompt: 'listenPrompt',
    content: 'Hello, how are you today?',
    xpReward: 5,
  },
  {
    id: 'ex-1-2',
    lessonId: 'lesson-1',
    type: 'select',
    prompt: 'selectPrompt',
    content: 'Good morning',
    options: ['Buenos días', 'Buenas noches', 'Hola', 'Adiós'],
    correctAnswer: 0,
    xpReward: 5,
  },
  {
    id: 'ex-1-3',
    lessonId: 'lesson-1',
    type: 'speak',
    prompt: 'speakPrompt',
    content: 'Nice to meet you!',
    xpReward: 10,
  },

  // Lesson 2: Numbers 1-10 (completed)
  {
    id: 'ex-2-1',
    lessonId: 'lesson-2',
    type: 'listen',
    prompt: 'listenPrompt',
    content: 'One, two, three, four, five',
    xpReward: 5,
  },
  {
    id: 'ex-2-2',
    lessonId: 'lesson-2',
    type: 'select',
    prompt: 'selectPrompt',
    content: 'Seven',
    options: ['Cinco', 'Siete', 'Nueve', 'Tres'],
    correctAnswer: 1,
    xpReward: 5,
  },
  {
    id: 'ex-2-3',
    lessonId: 'lesson-2',
    type: 'speak',
    prompt: 'speakPrompt',
    content: 'Six, seven, eight, nine, ten',
    xpReward: 10,
  },

  // Lesson 3: Common Phrases (in_progress)
  {
    id: 'ex-3-1',
    lessonId: 'lesson-3',
    type: 'listen',
    prompt: 'listenPrompt',
    content: 'Thank you very much!',
    xpReward: 5,
  },
  {
    id: 'ex-3-2',
    lessonId: 'lesson-3',
    type: 'select',
    prompt: 'selectPrompt',
    content: 'Please',
    options: ['Gracias', 'Por favor', 'De nada', 'Lo siento'],
    correctAnswer: 1,
    xpReward: 5,
  },
  {
    id: 'ex-3-3',
    lessonId: 'lesson-3',
    type: 'speak',
    prompt: 'speakPrompt',
    content: "You're welcome!",
    xpReward: 10,
  },

  // Lesson 4: Asking Questions (locked)
  {
    id: 'ex-4-1',
    lessonId: 'lesson-4',
    type: 'listen',
    prompt: 'listenPrompt',
    content: 'Where is the bathroom?',
    xpReward: 5,
  },
  {
    id: 'ex-4-2',
    lessonId: 'lesson-4',
    type: 'select',
    prompt: 'selectPrompt',
    content: 'What time is it?',
    options: ['¿Cómo estás?', '¿Qué hora es?', '¿Cuánto cuesta?', '¿Dónde está?'],
    correctAnswer: 1,
    xpReward: 5,
  },
  {
    id: 'ex-4-3',
    lessonId: 'lesson-4',
    type: 'speak',
    prompt: 'speakPrompt',
    content: 'How much does it cost?',
    xpReward: 10,
  },

  // Lesson 5: Daily Routine (locked)
  {
    id: 'ex-5-1',
    lessonId: 'lesson-5',
    type: 'listen',
    prompt: 'listenPrompt',
    content: 'I wake up at seven in the morning.',
    xpReward: 5,
  },
  {
    id: 'ex-5-2',
    lessonId: 'lesson-5',
    type: 'select',
    prompt: 'selectPrompt',
    content: 'I have breakfast',
    options: ['Me ducho', 'Desayuno', 'Almuerzo', 'Ceno'],
    correctAnswer: 1,
    xpReward: 5,
  },
  {
    id: 'ex-5-3',
    lessonId: 'lesson-5',
    type: 'speak',
    prompt: 'speakPrompt',
    content: 'I go to work at eight.',
    xpReward: 10,
  },

  // Lesson 6-8: More exercises for remaining lessons
  {
    id: 'ex-6-1',
    lessonId: 'lesson-6',
    type: 'listen',
    prompt: 'listenPrompt',
    content: 'The weather is nice today.',
    xpReward: 5,
  },
  {
    id: 'ex-6-2',
    lessonId: 'lesson-6',
    type: 'select',
    prompt: 'selectPrompt',
    content: 'It is raining',
    options: ['Hace sol', 'Está lloviendo', 'Hace frío', 'Está nevando'],
    correctAnswer: 1,
    xpReward: 5,
  },
  {
    id: 'ex-6-3',
    lessonId: 'lesson-6',
    type: 'speak',
    prompt: 'speakPrompt',
    content: 'It is very hot today!',
    xpReward: 10,
  },

  {
    id: 'ex-7-1',
    lessonId: 'lesson-7',
    type: 'listen',
    prompt: 'listenPrompt',
    content: 'I like to read books.',
    xpReward: 5,
  },
  {
    id: 'ex-7-2',
    lessonId: 'lesson-7',
    type: 'select',
    prompt: 'selectPrompt',
    content: 'I am reading',
    options: ['Estoy escribiendo', 'Estoy leyendo', 'Estoy escuchando', 'Estoy hablando'],
    correctAnswer: 1,
    xpReward: 5,
  },
  {
    id: 'ex-7-3',
    lessonId: 'lesson-7',
    type: 'speak',
    prompt: 'speakPrompt',
    content: 'This is a very interesting story.',
    xpReward: 10,
  },

  {
    id: 'ex-8-1',
    lessonId: 'lesson-8',
    type: 'listen',
    prompt: 'listenPrompt',
    content: 'I want to write a letter.',
    xpReward: 5,
  },
  {
    id: 'ex-8-2',
    lessonId: 'lesson-8',
    type: 'select',
    prompt: 'selectPrompt',
    content: 'The essay',
    options: ['La carta', 'El ensayo', 'El libro', 'El periódico'],
    correctAnswer: 1,
    xpReward: 5,
  },
  {
    id: 'ex-8-3',
    lessonId: 'lesson-8',
    type: 'speak',
    prompt: 'speakPrompt',
    content: 'Can you check my writing, please?',
    xpReward: 10,
  },
];

// Helper functions
export const getExercisesByLessonId = (lessonId: string): Exercise[] => {
  return mockExercises.filter((ex) => ex.lessonId === lessonId);
};

export const getExerciseTypeIcon = (type: ExerciseType): string => {
  const icons: Record<ExerciseType, string> = {
    listen: '🎧',
    select: '✅',
    speak: '🎤',
  };
  return icons[type];
};

export const calculateLessonXp = (lessonId: string): number => {
  return getExercisesByLessonId(lessonId).reduce((sum, ex) => sum + ex.xpReward, 0);
};
