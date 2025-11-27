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
