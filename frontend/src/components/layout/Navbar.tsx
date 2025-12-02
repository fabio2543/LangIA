import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Button } from '../common/Button';
import { useTranslation, LOCALE_FLAGS, LOCALE_LABELS } from '../../i18n';
import { useAuth } from '../../hooks/useAuth';
import type { Locale } from '../../types';

const LOCALES: Locale[] = ['pt', 'en', 'es'];

export const Navbar = () => {
  const { t, locale, setLocale } = useTranslation();
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const [isLangOpen, setIsLangOpen] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);

  const navLinks = [
    { label: t.navbar.method, href: '#method' },
    { label: t.navbar.tutors, href: '#tutors' },
    { label: t.navbar.courses, href: '#courses' },
    { label: t.navbar.pricing, href: '#pricing' },
    { label: t.navbar.business, href: '#business' },
  ];

  const handleLogout = async () => {
    await logout();
    setIsUserMenuOpen(false);
    navigate('/');
  };

  return (
    <nav className="flex justify-between items-center px-6 lg:px-15 py-4 bg-text">
      {/* Logo */}
      <Link to="/" className="text-2xl font-bold text-white">
        Lang<span className="text-accent">IA</span>
      </Link>

      {/* Desktop Menu */}
      <div className="hidden lg:flex items-center gap-7">
        {navLinks.map((link) => (
          <a
            key={link.href}
            href={link.href}
            className="text-gray-200 text-sm font-medium hover:text-white transition-colors"
          >
            {link.label}
          </a>
        ))}
      </div>

      {/* Actions */}
      <div className="flex items-center gap-3">
        {/* Language Selector */}
        <div className="relative">
          <button
            onClick={() => setIsLangOpen(!isLangOpen)}
            className="flex items-center gap-2 px-3 py-2 rounded-full bg-white/10 text-white text-sm hover:bg-white/20 transition-colors"
            aria-label="Select language"
          >
            <span>{LOCALE_FLAGS[locale]}</span>
            <span>{LOCALE_LABELS[locale]}</span>
          </button>

          {isLangOpen && (
            <div className="absolute top-full right-0 mt-2 bg-white rounded-xl shadow-lg overflow-hidden z-50">
              {LOCALES.map((loc) => (
                <button
                  key={loc}
                  onClick={() => {
                    setLocale(loc);
                    setIsLangOpen(false);
                  }}
                  className={`flex items-center gap-2 w-full px-4 py-2.5 text-sm hover:bg-bg transition-colors ${
                    locale === loc ? 'bg-primary-light text-primary' : 'text-text'
                  }`}
                >
                  <span>{LOCALE_FLAGS[loc]}</span>
                  <span>{LOCALE_LABELS[loc]}</span>
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Auth Buttons - Desktop */}
        <div className="hidden lg:flex items-center gap-3">
          {isAuthenticated && user ? (
            // Usuário logado - Menu dropdown
            <div className="relative">
              <button
                onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                className="flex items-center gap-2 px-3 py-2 rounded-full bg-white/10 text-white text-sm hover:bg-white/20 transition-colors"
              >
                <span className="w-7 h-7 rounded-full bg-primary-light flex items-center justify-center text-sm">
                  {user.profile === 'TEACHER' ? '👨‍🏫' : '🎓'}
                </span>
                <span className="max-w-24 truncate">{user.name.split(' ')[0]}</span>
              </button>

              {isUserMenuOpen && (
                <div className="absolute top-full right-0 mt-2 bg-white rounded-xl shadow-lg overflow-hidden z-50 min-w-40">
                  <Link
                    to="/dashboard"
                    onClick={() => setIsUserMenuOpen(false)}
                    className="flex items-center gap-2 w-full px-4 py-3 text-sm text-text hover:bg-bg transition-colors"
                  >
                    📊 {t.dashboard.title}
                  </Link>
                  <button
                    onClick={handleLogout}
                    className="flex items-center gap-2 w-full px-4 py-3 text-sm text-red-600 hover:bg-red-50 transition-colors"
                  >
                    🚪 {t.dashboard.logout}
                  </button>
                </div>
              )}
            </div>
          ) : (
            // Usuário não logado - Botões de Login/Signup
            <>
              <Link to="/login">
                <Button variant="outline" size="md">
                  {t.navbar.login}
                </Button>
              </Link>
              <Link to="/signup">
                <Button variant="primary" size="md">
                  {t.navbar.signup}
                </Button>
              </Link>
            </>
          )}
        </div>

        {/* Mobile Menu Button */}
        <button
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          className="lg:hidden p-2 text-white"
          aria-label="Toggle menu"
        >
          {isMobileMenuOpen ? '✕' : '☰'}
        </button>
      </div>

      {/* Mobile Menu */}
      {isMobileMenuOpen && (
        <div className="absolute top-full left-0 right-0 bg-text p-6 lg:hidden z-40">
          <div className="flex flex-col gap-4">
            {navLinks.map((link) => (
              <a
                key={link.href}
                href={link.href}
                className="text-gray-200 text-base font-medium hover:text-white"
                onClick={() => setIsMobileMenuOpen(false)}
              >
                {link.label}
              </a>
            ))}
            <hr className="border-white/20 my-2" />

            {isAuthenticated && user ? (
              // Usuário logado - Mobile
              <>
                <div className="flex items-center gap-3 text-white mb-2">
                  <span className="w-10 h-10 rounded-full bg-primary-light flex items-center justify-center text-xl">
                    {user.profile === 'TEACHER' ? '👨‍🏫' : '🎓'}
                  </span>
                  <div>
                    <p className="font-medium">{user.name}</p>
                    <p className="text-sm text-gray-300">{user.email}</p>
                  </div>
                </div>
                <Link to="/dashboard" onClick={() => setIsMobileMenuOpen(false)}>
                  <Button variant="outline" fullWidth>
                    📊 {t.dashboard.title}
                  </Button>
                </Link>
                <Button variant="primary" fullWidth onClick={handleLogout}>
                  🚪 {t.dashboard.logout}
                </Button>
              </>
            ) : (
              // Usuário não logado - Mobile
              <>
                <Link to="/login" onClick={() => setIsMobileMenuOpen(false)}>
                  <Button variant="outline" fullWidth>
                    {t.navbar.login}
                  </Button>
                </Link>
                <Link to="/signup" onClick={() => setIsMobileMenuOpen(false)}>
                  <Button variant="primary" fullWidth>
                    {t.navbar.signup}
                  </Button>
                </Link>
              </>
            )}
          </div>
        </div>
      )}
    </nav>
  );
};
