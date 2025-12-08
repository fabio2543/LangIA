import { useState } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useTranslation, type Translations } from '../i18n';
import { AuthNavbar } from '../components/layout/AuthNavbar';
import { BottomNav } from '../components/layout/BottomNav';
import { Button } from '../components/common/Button';
import { mockTutors, mockUserProgress } from '../services/mockData';
import type { Tutor } from '../services/mockData/tutorsMock';

export const TutorsPage = () => {
  const { user, isAuthenticated, isLoading } = useAuth();
  const { t } = useTranslation();
  const [searchQuery, setSearchQuery] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState({
    onlineOnly: false,
    superTutorOnly: false,
  });

  if (isLoading) {
    return (
      <div className="min-h-screen bg-bg-warm flex items-center justify-center animate-fade-in">
        <div className="text-center">
          <div className="text-4xl animate-pulse-slow">📚</div>
          <p className="text-text-light mt-2">{t.profile.common.loading}</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  const filteredTutors = mockTutors.filter((tutor) => {
    const matchesSearch =
      searchQuery === '' ||
      tutor.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      tutor.languages.some((l) => l.toLowerCase().includes(searchQuery.toLowerCase())) ||
      tutor.specialties.some((s) => s.toLowerCase().includes(searchQuery.toLowerCase()));

    const matchesOnline = !filters.onlineOnly || tutor.isOnline;
    const matchesSuperTutor = !filters.superTutorOnly || tutor.isSuperTutor;

    return matchesSearch && matchesOnline && matchesSuperTutor;
  });

  return (
    <div className="min-h-screen bg-bg-warm">
      <AuthNavbar showStreak streak={mockUserProgress.streak} />

      <main className="max-w-7xl mx-auto px-4 py-6 has-bottom-nav">
        <div className="space-y-8 animate-in fade-in duration-500">
          {/* Header */}
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
              <h1 className="font-serif text-3xl md:text-4xl font-bold text-indigo-950">
                {t.tutors.title}
              </h1>
              <p className="text-muted-foreground mt-2">{t.tutors.subtitle}</p>
            </div>

          <div className="flex gap-2 w-full md:w-auto">
            <div className="relative flex-1 md:w-64">
              <span className="absolute left-3 top-1/2 -translate-y-1/2 text-text-light">🔍</span>
              <input
                type="text"
                placeholder={t.tutors.searchPlaceholder}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 rounded-full bg-white border border-gray-200 focus:border-primary focus:ring-2 focus:ring-primary-light outline-none transition-all"
              />
            </div>
            <button
              onClick={() => setShowFilters(!showFilters)}
              className={`p-2.5 rounded-full border transition-all ${
                showFilters ? 'bg-primary text-white border-primary' : 'bg-white border-gray-200 hover:border-primary'
              }`}
              aria-label={t.tutors.filters}
            >
              <span className="text-lg">⚙️</span>
            </button>
          </div>
        </div>

        {/* Filters Panel */}
        {showFilters && (
          <div className="bg-white rounded-2xl p-4 mb-6 shadow-sm animate-fade-in">
            <div className="flex flex-wrap gap-4">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={filters.onlineOnly}
                  onChange={(e) => setFilters({ ...filters, onlineOnly: e.target.checked })}
                  className="w-4 h-4 rounded text-primary focus:ring-primary"
                />
                <span className="text-sm text-text">{t.tutors.online}</span>
              </label>
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={filters.superTutorOnly}
                  onChange={(e) => setFilters({ ...filters, superTutorOnly: e.target.checked })}
                  className="w-4 h-4 rounded text-primary focus:ring-primary"
                />
                <span className="text-sm text-text">{t.tutors.superTutor}</span>
              </label>
            </div>
          </div>
        )}

          {/* Speaking Clubs Banner */}
          <div className="bg-gradient-to-r from-orange-100 to-indigo-100 rounded-3xl p-6 flex flex-col md:flex-row items-center justify-between gap-4 shadow-sm">
            <div className="space-y-2 text-center md:text-left">
              <div className="flex items-center justify-center md:justify-start gap-2 text-orange-600 font-bold uppercase text-xs tracking-wider">
                <span>👥</span> {t.tutors.speakingClubs}
              </div>
              <h3 className="text-2xl font-serif font-bold text-indigo-950">
                {t.tutors.speakingClubsDesc}
              </h3>
              <p className="text-muted-foreground max-w-md">
                Practice with 3-5 other learners led by a moderator.
              </p>
            </div>
            <Button variant="primary" size="lg" className="rounded-full shadow-lg">
              {t.tutors.joinClub} <span className="ml-2">🎤</span>
            </Button>
          </div>

          {/* Tutors Grid */}
          <div className="grid md:grid-cols-2 xl:grid-cols-3 gap-6">
            {filteredTutors.map((tutor) => (
              <TutorCard key={tutor.id} tutor={tutor} t={t} />
            ))}
          </div>

          {filteredTutors.length === 0 && (
            <div className="text-center py-12">
              <div className="text-4xl mb-4">🔍</div>
              <p className="text-muted-foreground">Nenhum tutor encontrado com esses filtros.</p>
            </div>
          )}
        </div>
      </main>

      <BottomNav />
    </div>
  );
};

interface TutorCardProps {
  tutor: Tutor;
  t: Translations;
}

const TutorCard = ({ tutor, t }: TutorCardProps) => {
  return (
    <div className="overflow-hidden border-0 shadow-sm hover:shadow-lg transition-all duration-300 rounded-2xl group bg-white">
      <div className="p-6">
        <div className="flex gap-4">
          {/* Avatar */}
          <div className="relative">
            <div
              className="h-20 w-20 rounded-2xl flex items-center justify-center text-4xl shadow-sm"
              style={{ backgroundColor: tutor.bgColor }}
            >
              {tutor.flag}
            </div>
            {tutor.isOnline && (
              <span className="absolute -bottom-1 -right-1 flex h-4 w-4 items-center justify-center rounded-full bg-white">
                <span className="h-2.5 w-2.5 rounded-full bg-green-500"></span>
              </span>
            )}
          </div>

          {/* Info */}
          <div className="flex-1 min-w-0">
            <h3 className="font-bold text-xl text-indigo-950">{tutor.name}</h3>
            <p className="text-muted-foreground text-sm mb-2">
              {tutor.languages.join(' ')} Native
            </p>
            <div className="flex items-center gap-1">
              <span className="text-yellow-400">⭐</span>
              <span className="font-bold text-sm">{tutor.rating}</span>
              <span className="text-xs text-muted-foreground">
                ({tutor.reviewCount})
              </span>
            </div>
          </div>
        </div>

        {/* Specialties */}
        <div className="mt-4 flex flex-wrap gap-2">
          {tutor.specialties.slice(0, 3).map((specialty) => (
            <span
              key={specialty}
              className="px-3 py-1 bg-indigo-50 text-primary text-xs font-medium rounded-md hover:bg-indigo-100 transition-colors"
            >
              {specialty}
            </span>
          ))}
        </div>

        {/* Footer */}
        <div className="mt-6 pt-4 border-t flex items-center justify-between">
          <div>
            <span className="text-lg font-bold text-indigo-950">
              ${tutor.hourlyRate}
            </span>
            <span className="text-xs text-muted-foreground">/hour</span>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" size="sm" className="rounded-full h-9 px-4">
              <span className="mr-1">🎬</span> {t.tutors.intro}
            </Button>
            <Button variant="primary" size="sm" className="rounded-full h-9 px-4 shadow-lg">
              {t.tutors.bookNow}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};
