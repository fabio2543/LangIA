import { useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Button } from '../components/common/Button';
import { useAuth } from '../hooks/useAuth';
import { useTranslation } from '../i18n';

export const DashboardPage = () => {
  const { t, locale, setLocale } = useTranslation();
  const { user, logout, isLoading } = useAuth();
  const navigate = useNavigate();

  // Redireciona para login se não estiver autenticado
  useEffect(() => {
    if (!user && !isLoading) {
      navigate('/login');
    }
  }, [user, isLoading, navigate]);

  // Redireciona para onboarding se não completou
  useEffect(() => {
    if (user && !user.onboardingCompleted && !isLoading) {
      navigate('/onboarding');
    }
  }, [user, isLoading, navigate]);

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  // Mostra loading enquanto verifica autenticação
  if (isLoading || !user) {
    return (
      <div className="min-h-screen bg-bg-warm flex items-center justify-center">
        <div className="text-center">
          <div className="text-4xl mb-4">⏳</div>
          <p className="text-text-light">Carregando...</p>
        </div>
      </div>
    );
  }

  const profileLabel = user.profile === 'TEACHER' ? t.dashboard.teacher : t.dashboard.student;

  return (
    <div className="min-h-screen bg-bg-warm">
      {/* Header */}
      <header className="bg-text shadow-card">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            {/* Logo */}
            <a href="/" className="flex items-center gap-1 text-2xl font-bold text-white">
              Lang<span className="text-accent">IA</span>
            </a>

            <div className="flex items-center gap-4">
              {/* Seletor de Idioma */}
              <div className="flex items-center gap-1 bg-white/10 rounded-full px-2 py-1">
                {(['pt', 'en', 'es'] as const).map((lang) => (
                  <button
                    key={lang}
                    onClick={() => setLocale(lang)}
                    className={`px-2 py-1 text-xs font-medium rounded-full transition-colors ${
                      locale === lang
                        ? 'bg-white text-text'
                        : 'text-white/70 hover:text-white'
                    }`}
                  >
                    {lang.toUpperCase()}
                  </button>
                ))}
              </div>

              {/* Botão Perfil */}
              <Link to="/profile">
                <Button variant="secondary" size="sm">
                  {t.dashboard.myProfile}
                </Button>
              </Link>

              {/* Botão Logout */}
              <Button
                variant="outline"
                size="sm"
                onClick={handleLogout}
                disabled={isLoading}
              >
                {isLoading ? t.dashboard.loggingOut : t.dashboard.logout}
              </Button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Welcome Card */}
        <div className="bg-white rounded-3xl shadow-card p-8 mb-8">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
            <div>
              <h1 className="text-3xl font-serif italic text-text mb-2">
                {t.dashboard.welcome}, {user.name.split(' ')[0]}! 👋
              </h1>
              <p className="text-text-light">{t.dashboard.subtitle}</p>
            </div>
            <Link to="/profile" className="flex items-center gap-3 hover:opacity-80 transition-opacity cursor-pointer">
              <div className="w-16 h-16 rounded-full bg-primary-light flex items-center justify-center text-3xl">
                {user.profile === 'TEACHER' ? '👨‍🏫' : '🎓'}
              </div>
              <div>
                <p className="font-semibold text-text">{user.name}</p>
                <p className="text-sm text-text-light">{profileLabel}</p>
              </div>
            </Link>
          </div>
        </div>

        {/* Profile Info Card */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
          <div className="bg-white rounded-2xl shadow-card p-6">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 rounded-full bg-bg flex items-center justify-center">
                📧
              </div>
              <span className="text-sm text-text-light">Email</span>
            </div>
            <p className="font-medium text-text">{user.email}</p>
          </div>

          <div className="bg-white rounded-2xl shadow-card p-6">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 rounded-full bg-bg flex items-center justify-center">
                👤
              </div>
              <span className="text-sm text-text-light">{t.dashboard.profile}</span>
            </div>
            <p className="font-medium text-text">{profileLabel}</p>
          </div>

          <div className="bg-white rounded-2xl shadow-card p-6">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 rounded-full bg-bg flex items-center justify-center">
                🆔
              </div>
              <span className="text-sm text-text-light">ID</span>
            </div>
            <p className="font-medium text-text text-sm truncate">{user.id}</p>
          </div>
        </div>

        {/* Learning Trails Section */}
        <div className="bg-gradient-to-br from-primary to-primary-dark rounded-3xl shadow-card p-8 text-white text-center">
          <div className="text-6xl mb-4">🎯</div>
          <h2 className="text-2xl font-serif italic mb-2">Trilhas de Aprendizado</h2>
          <p className="text-white/80 max-w-md mx-auto mb-6">
            Acesse suas trilhas personalizadas e continue sua jornada de aprendizado de idiomas.
          </p>
          <Link to="/trails">
            <Button variant="secondary" size="lg">
              Ver Minhas Trilhas
            </Button>
          </Link>
        </div>
      </main>
    </div>
  );
};
