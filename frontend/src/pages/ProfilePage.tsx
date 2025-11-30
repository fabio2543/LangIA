import { useEffect, useMemo } from 'react';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../i18n';
import { ProfileHeader } from '../components/profile/ProfileHeader';
import { TabNavigation } from '../components/profile/TabNavigation';

export const ProfilePage = () => {
  const { t, locale, setLocale } = useTranslation();
  const { user, isLoading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  // Determina a tab ativa baseado na URL (derivado, sem estado)
  const activeTab = useMemo(() => {
    const path = location.pathname;
    if (path.includes('/learning')) return 'learning';
    if (path.includes('/assessment')) return 'assessment';
    if (path.includes('/notifications')) return 'notifications';
    return 'personal';
  }, [location.pathname]);

  // Redireciona para login se não estiver autenticado
  useEffect(() => {
    if (!user && !isLoading) {
      navigate('/login');
    }
  }, [user, isLoading, navigate]);

  const handleTabChange = (tabId: string) => {
    const routes: Record<string, string> = {
      personal: '/profile',
      learning: '/profile/learning',
      assessment: '/profile/assessment',
      notifications: '/profile/notifications',
    };
    navigate(routes[tabId]);
  };

  // Mostra loading enquanto verifica autenticação
  if (isLoading || !user) {
    return (
      <div className="min-h-screen bg-bg-warm flex items-center justify-center">
        <div className="text-center">
          <div className="text-4xl mb-4 animate-pulse">⏳</div>
          <p className="text-text-light">{t.profile.common.loading}</p>
        </div>
      </div>
    );
  }

  const tabs = [
    { id: 'personal', label: t.profile.tabs.personalData, icon: <span>👤</span> },
    { id: 'learning', label: t.profile.tabs.learningPreferences, icon: <span>📚</span> },
    { id: 'assessment', label: t.profile.tabs.skillAssessment, icon: <span>📊</span> },
    { id: 'notifications', label: t.profile.tabs.notifications, icon: <span>🔔</span> },
  ];

  return (
    <div className="min-h-screen bg-bg-warm">
      {/* Header */}
      <header className="bg-text shadow-card">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            {/* Logo */}
            <a href="/dashboard" className="flex items-center gap-1 text-2xl font-bold text-white">
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

              {/* Voltar para Dashboard */}
              <button
                onClick={() => navigate('/dashboard')}
                className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-white/80 hover:text-white transition-colors"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                </svg>
                Dashboard
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Profile Header */}
      <ProfileHeader user={user} />

      {/* Main Content */}
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Tab Navigation */}
        <TabNavigation
          tabs={tabs}
          activeTab={activeTab}
          onChange={handleTabChange}
        />

        {/* Tab Content */}
        <div className="bg-white rounded-3xl shadow-card p-6 sm:p-8 mt-6">
          <Outlet />
        </div>
      </main>
    </div>
  );
};
