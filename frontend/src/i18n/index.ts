import { createContext, useContext } from 'react';
import { pt } from './locales/pt';
import { en } from './locales/en';
import { es } from './locales/es';
import type { Locale } from '../types';

export type Translations = typeof pt;

const locales: Record<Locale, Translations> = {
  pt,
  en,
  es,
};

export interface I18nContextType {
  locale: Locale;
  setLocale: (locale: Locale) => void;
  t: Translations;
}

export const I18nContext = createContext<I18nContextType | null>(null);

export const useTranslation = (): I18nContextType => {
  const context = useContext(I18nContext);
  if (!context) {
    throw new Error('useTranslation must be used within I18nProvider');
  }
  return context;
};

export const getTranslations = (locale: Locale): Translations => locales[locale];

export const LOCALE_FLAGS: Record<Locale, string> = {
  pt: '🇧🇷',
  en: '🇺🇸',
  es: '🇪🇸',
};

export const LOCALE_LABELS: Record<Locale, string> = {
  pt: 'PT',
  en: 'EN',
  es: 'ES',
};
