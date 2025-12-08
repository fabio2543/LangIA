import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation, LOCALE_FLAGS, LOCALE_LABELS } from '../../i18n';
import { useAuth } from '../../hooks/useAuth';
import type { Locale } from '../../types';

const LOCALES: Locale[] = ['pt', 'en', 'es'];

interface AuthNavbarProps {
  showStreak?: boolean;
  streak?: number;
}

export const AuthNavbar = ({ showStreak = true, streak = 0 }: AuthNavbarProps) => {
  const { t, locale, setLocale } = useTranslation();
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [isLangOpen, setIsLangOpen] = useState(false);
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    setIsUserMenuOpen(false);
    navigate('/');
  };

  return (
    <header className="sticky top-0 z-40 w-full border-b bg-white/80 backdrop-blur-md">
      <div className="container mx-auto flex h-16 items-center justify-between px-4">
        {/* Logo */}
        <div className="flex items-center gap-2">
          <Link to="/dashboard" className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-white font-serif font-bold italic">
              L
            </div>
            <span className="font-serif text-xl font-bold italic tracking-tight text-primary">
              LangIA
            </span>
          </Link>
        </div>

        {/* Desktop Navigation - Hidden on mobile (BottomNav handles it) */}
        <nav className="hidden md:flex items-center gap-6">
          <Link
            to="/dashboard"
            className="flex items-center gap-2 text-sm font-medium text-muted-foreground hover:text-primary transition-colors"
          >
            {t.bottomNav?.home || 'Home'}
          </Link>
          <Link
            to="/lessons"
            className="flex items-center gap-2 text-sm font-medium text-muted-foreground hover:text-primary transition-colors"
          >
            {t.bottomNav?.learn || 'Learn'}
          </Link>
          <Link
            to="/tutors"
            className="flex items-center gap-2 text-sm font-medium text-muted-foreground hover:text-primary transition-colors"
          >
            {t.bottomNav?.tutors || 'Tutors'}
          </Link>
          <Link
            to="/profile"
            className="flex items-center gap-2 text-sm font-medium text-muted-foreground hover:text-primary transition-colors"
          >
            {t.bottomNav?.profile || 'Profile'}
          </Link>
        </nav>

        {/* Right Side Actions */}
        <div className="hidden md:flex items-center gap-4">
          {/* Streak Badge */}
          {showStreak && (
            <div className="flex items-center gap-2 rounded-full bg-orange-100 px-3 py-1 text-sm font-medium text-orange-600">
              🔥 {streak}
            </div>
          )}

          {/* Language Selector */}
          <div className="relative">
            <button
              onClick={() => setIsLangOpen(!isLangOpen)}
              className="flex items-center gap-1.5 px-2.5 py-1.5 rounded-full bg-gray-100 text-sm hover:bg-gray-200 transition-colors"
              aria-label="Select language"
            >
              <span>{LOCALE_FLAGS[locale]}</span>
            </button>

            {isLangOpen && (
              <div className="absolute top-full right-0 mt-2 bg-white rounded-xl shadow-lg overflow-hidden z-50 min-w-32">
                {LOCALES.map((loc) => (
                  <button
                    key={loc}
                    onClick={() => {
                      setLocale(loc);
                      setIsLangOpen(false);
                    }}
                    className={`flex items-center gap-2 w-full px-4 py-2.5 text-sm hover:bg-indigo-50 transition-colors ${
                      locale === loc ? 'bg-indigo-100 text-primary' : ''
                    }`}
                  >
                    <span>{LOCALE_FLAGS[loc]}</span>
                    <span>{LOCALE_LABELS[loc]}</span>
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* User Menu */}
          {user && (
            <div className="relative">
              <button
                onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                className="h-8 w-8 rounded-full bg-primary/10 overflow-hidden border border-primary/20"
              >
                <div className="h-full w-full flex items-center justify-center text-sm">
                  {user.profile === 'TEACHER' ? '👨‍🏫' : '🎓'}
                </div>
              </button>

              {isUserMenuOpen && (
                <div className="absolute top-full right-0 mt-2 bg-white rounded-xl shadow-lg overflow-hidden z-50 min-w-44">
                  <div className="px-4 py-3 border-b border-gray-100">
                    <p className="font-medium truncate">{user.name}</p>
                    <p className="text-xs text-muted-foreground truncate">{user.email}</p>
                  </div>
                  <Link
                    to="/profile"
                    onClick={() => setIsUserMenuOpen(false)}
                    className="flex items-center gap-2 w-full px-4 py-3 text-sm hover:bg-indigo-50 transition-colors"
                  >
                    ⚙️ {t.profile?.title || 'Settings'}
                  </Link>
                  <button
                    onClick={handleLogout}
                    className="flex items-center gap-2 w-full px-4 py-3 text-sm text-red-600 hover:bg-red-50 transition-colors"
                  >
                    🚪 {t.dashboard?.logout || 'Logout'}
                  </button>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Mobile: Streak badge only */}
        <div className="flex md:hidden items-center gap-3">
          {showStreak && (
            <div className="flex items-center gap-2 rounded-full bg-orange-100 px-3 py-1 text-sm font-medium text-orange-600">
              🔥 {streak}
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
