import { useState, useEffect, type ReactNode } from 'react';
import { I18nContext, getTranslations } from '../i18n';
import type { Locale } from '../types';

const STORAGE_KEY = 'langia-locale';

const getInitialLocale = (): Locale => {
  if (typeof window !== 'undefined') {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored && ['pt', 'en', 'es'].includes(stored)) {
      return stored as Locale;
    }
    const browserLang = navigator.language.slice(0, 2);
    if (browserLang === 'pt') return 'pt';
    if (browserLang === 'es') return 'es';
  }
  return 'pt';
};

interface I18nProviderProps {
  children: ReactNode;
}

export const I18nProvider = ({ children }: I18nProviderProps) => {
  const [locale, setLocaleState] = useState<Locale>(getInitialLocale);

  const setLocale = (newLocale: Locale) => {
    setLocaleState(newLocale);
    localStorage.setItem(STORAGE_KEY, newLocale);
  };

  useEffect(() => {
    document.documentElement.lang = locale;
  }, [locale]);

  const t = getTranslations(locale);

  return (
    <I18nContext.Provider value={{ locale, setLocale, t }}>
      {children}
    </I18nContext.Provider>
  );
};
