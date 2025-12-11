import { useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useTranslation } from '../i18n';
import { AuthNavbar } from '../components/layout/AuthNavbar';
import { BottomNav } from '../components/layout/BottomNav';
import {
  StatCard,
  ProgressCard,
  NextLessonCard,
  RecommendedTutors,
  StreakCard,
  SrsReviewCard,
  TopErrorsCard,
  DailyActivityCard,
} from '../components/dashboard';
import {
  mockUserProgress,
  getNextLesson,
  getRecommendedTutors,
} from '../services/mockData';

export const DashboardPage = () => {
  const { t } = useTranslation();
  const { user, isLoading } = useAuth();
  const navigate = useNavigate();

  // Redireciona para login se não estiver autenticado
  useEffect(() => {
    if (!user && !isLoading) {
      navigate('/login');
    }
  }, [user, isLoading, navigate]);

  // Mostra loading enquanto verifica autenticação
  if (isLoading || !user) {
    return (
      <div className="min-h-screen bg-bg-warm flex items-center justify-center">
        <div className="text-center animate-in fade-in duration-500">
          <div className="text-4xl mb-4 animate-pulse-slow">⏳</div>
          <p className="text-muted-foreground">Carregando...</p>
        </div>
      </div>
    );
  }

  const nextLesson = getNextLesson();
  const recommendedTutors = getRecommendedTutors(3);

  return (
    <div className="min-h-screen bg-bg-warm">
      {/* Auth Navbar */}
      <AuthNavbar showStreak streak={mockUserProgress.streak} />

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 md:py-10 has-bottom-nav md:pb-10">
        <div className="space-y-8 animate-in fade-in duration-500">
          {/* Welcome & Stats */}
          <div className="grid md:grid-cols-3 gap-6">
            <div className="md:col-span-2 space-y-2">
              <h1 className="font-serif text-3xl md:text-4xl font-bold text-indigo-950">
                {t.dashboard?.welcome || 'Welcome back'}, {user.name.split(' ')[0]}! 👋
              </h1>
              <p className="text-muted-foreground text-lg">
                {t.dashboard?.subtitle || "You're making great progress. Ready for the next level?"}
              </p>
            </div>
            <div className="p-4 flex items-center justify-between bg-white shadow-sm border-0 rounded-2xl">
              <div className="flex items-center gap-3">
                <div className="h-10 w-10 rounded-full bg-orange-100 flex items-center justify-center text-orange-600">
                  <span className="text-xl">⚡</span>
                </div>
                <div>
                  <p className="font-bold text-lg">{mockUserProgress.streak} {t.dashboard?.streakDays || 'Days'}</p>
                  <p className="text-sm text-muted-foreground">{t.dashboard?.streak || 'Current Streak'}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 border-l pl-4">
                <div className="h-10 w-10 rounded-full bg-yellow-100 flex items-center justify-center text-yellow-600">
                  <span className="text-xl">🏆</span>
                </div>
                <div>
                  <p className="font-bold text-lg">{mockUserProgress.totalXp.toLocaleString()}</p>
                  <p className="text-sm text-muted-foreground">{t.dashboard?.totalXp || 'Total XP'}</p>
                </div>
              </div>
            </div>
          </div>

          {/* Learning Progress Widgets */}
          <section>
            <h3 className="font-serif text-2xl font-bold text-indigo-950 mb-4">
              {t.dashboard?.progress || 'Seu Progresso'}
            </h3>
            <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-4">
              <StreakCard languageCode="en" />
              <SrsReviewCard languageCode="en" />
              <DailyActivityCard languageCode="en" />
              <TopErrorsCard languageCode="en" />
            </div>
          </section>

          {/* Progress Card */}
          <ProgressCard
            currentLevel={mockUserProgress.currentLevel}
            nextLevel={mockUserProgress.nextLevel}
            progressPercent={mockUserProgress.progressPercent}
          />

          {/* Next Lesson Section */}
          <section>
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-serif text-2xl font-bold text-indigo-950">
                {t.dashboard?.nextLesson || 'Next Lesson'}
              </h3>
              <Link to="/lessons" className="text-primary font-medium hover:underline">
                {t.dashboard?.viewAll || 'View all'}
              </Link>
            </div>
            <NextLessonCard lesson={nextLesson} />
          </section>

          {/* Recommended Tutors Section */}
          <section>
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-serif text-2xl font-bold text-indigo-950">
                {t.dashboard?.recommendedTutors || 'Recommended Tutors'}
              </h3>
              <Link to="/tutors" className="text-primary font-medium hover:underline">
                Find more
              </Link>
            </div>
            <RecommendedTutors tutors={recommendedTutors} />
          </section>

          {/* Quick Stats */}
          <section>
            <h3 className="font-serif text-2xl font-bold text-indigo-950 mb-4">
              Your Stats
            </h3>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <StatCard
                icon="📚"
                value={mockUserProgress.lessonsCompleted}
                label="Lessons Completed"
                variant="default"
              />
              <StatCard
                icon="⏱️"
                value={mockUserProgress.hoursStudied}
                label="Hours Studied"
                sublabel="hrs"
                variant="default"
              />
              <StatCard
                icon="🏆"
                value={mockUserProgress.maxStreak}
                label="Best Streak"
                sublabel={t.dashboard?.streakDays || 'days'}
                variant="default"
              />
              <StatCard
                icon="🎯"
                value={`${mockUserProgress.progressPercent}%`}
                label="Level Progress"
                variant="progress"
              />
            </div>
          </section>
        </div>
      </main>

      {/* Bottom Navigation - Mobile only */}
      <BottomNav />
    </div>
  );
};
