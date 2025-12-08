import { useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Button } from '../components/common/Button';
import { useAuth } from '../hooks/useAuth';
import { useTranslation } from '../i18n';
import { AuthNavbar } from '../components/layout/AuthNavbar';
import { BottomNav } from '../components/layout/BottomNav';
import { cn } from '../utils/cn';
import {
  mockLessons,
  mockUserProgress,
} from '../services/mockData';
import type { Lesson } from '../services/mockData/lessonsMock';

interface LessonCardProps {
  lesson: Lesson;
}

const LessonCard = ({ lesson }: LessonCardProps) => {
  const { t } = useTranslation();
  const typeKey = lesson.type as keyof typeof t.lessons.lessonTypes;

  const getButtonText = () => {
    switch (lesson.status) {
      case 'completed':
        return t.lessons?.reviewLesson || 'Review';
      case 'in_progress':
        return t.lessons?.continueLesson || 'Continue';
      default:
        return t.lessons?.startLesson || 'Start';
    }
  };

  return (
    <div className="relative md:pl-24 group">
      {/* Timeline Dot - Desktop only */}
      <div
        className={cn(
          'absolute left-6 top-1/2 -mt-4 w-4 h-4 rounded-full border-2 hidden md:block z-10 bg-bg-warm',
          lesson.status === 'completed'
            ? 'border-green-500 bg-green-500'
            : lesson.status === 'in_progress'
            ? 'border-primary bg-primary animate-pulse'
            : 'border-slate-300'
        )}
      />

      {/* Card */}
      <div
        className={cn(
          'p-0 overflow-hidden border-0 shadow-sm transition-all duration-300 rounded-2xl mb-4',
          lesson.status === 'completed'
            ? 'opacity-75 hover:opacity-100 bg-slate-50'
            : lesson.status === 'locked'
            ? 'bg-slate-50 opacity-60'
            : 'bg-white hover:shadow-md hover:scale-[1.01]'
        )}
      >
        <div className="flex items-center p-4 sm:p-6 gap-4 sm:gap-6">
          {/* Icon */}
          <div
            className={cn(
              'flex-shrink-0 h-14 w-14 rounded-2xl flex items-center justify-center',
              lesson.status === 'completed'
                ? 'bg-green-100 text-green-600'
                : lesson.status === 'in_progress'
                ? 'bg-indigo-50 text-primary'
                : 'bg-slate-100 text-slate-400'
            )}
          >
            {lesson.status === 'completed' ? (
              <span className="text-2xl">✓</span>
            ) : lesson.status === 'in_progress' ? (
              <span className="text-2xl">▶</span>
            ) : (
              <span className="text-xl">🔒</span>
            )}
          </div>

          {/* Content */}
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 mb-1">
              <span className="text-xs font-bold uppercase tracking-wider text-slate-500">
                {t.lessons?.lessonTypes?.[typeKey] || lesson.type}
              </span>
              <span className="text-xs text-slate-400">
                • {lesson.duration} {t.lessons?.duration || 'min'}
              </span>
            </div>
            <h3 className="font-bold text-lg text-indigo-950 truncate">{lesson.title}</h3>
            <p className="text-sm text-muted-foreground truncate hidden sm:block">
              {lesson.description}
            </p>
          </div>

          {/* Action */}
          <div className="flex-shrink-0">
            {lesson.status === 'completed' && lesson.score ? (
              <div className="text-right">
                <span className="block font-bold text-green-600">{lesson.score}%</span>
                <span className="text-xs text-muted-foreground">Score</span>
              </div>
            ) : lesson.status === 'locked' ? (
              <Button
                variant="secondary"
                size="sm"
                disabled
                className="rounded-full px-6 opacity-50"
              >
                {t.lessons?.locked || 'Locked'}
              </Button>
            ) : (
              <Link to={`/lesson/${lesson.id}`}>
                <Button variant="primary" size="sm" className="rounded-full px-6">
                  {getButtonText()}
                </Button>
              </Link>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export const LessonsPage = () => {
  const { t } = useTranslation();
  const { user, isLoading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!user && !isLoading) {
      navigate('/login');
    }
  }, [user, isLoading, navigate]);

  if (isLoading || !user) {
    return (
      <div className="min-h-screen bg-bg-warm flex items-center justify-center">
        <div className="text-center animate-in fade-in duration-500">
          <div className="text-4xl mb-4 animate-pulse-slow">⏳</div>
          <p className="text-muted-foreground">Loading...</p>
        </div>
      </div>
    );
  }

  const completedCount = mockLessons.filter((l) => l.status === 'completed').length;
  const totalCount = mockLessons.length;
  const progressPercent = Math.round((completedCount / totalCount) * 100);

  return (
    <div className="min-h-screen bg-bg-warm">
      <AuthNavbar showStreak streak={mockUserProgress.streak} />

      <main className="max-w-3xl mx-auto px-4 sm:px-6 py-6 md:py-10 has-bottom-nav md:pb-10">
        <div className="space-y-6 animate-in fade-in duration-500">
          {/* Header */}
          <div className="flex items-end justify-between">
            <div>
              <h1 className="font-serif text-3xl md:text-4xl font-bold text-indigo-950">
                {t.lessons?.title || 'Learning Path'}
              </h1>
              <p className="text-muted-foreground mt-2">
                {t.lessons?.subtitle || 'Level A2: Elementary'}
              </p>
            </div>
            <div className="hidden md:block">
              <span className="bg-indigo-100 text-primary px-4 py-2 rounded-full font-medium text-sm">
                {progressPercent}% {t.lessons?.completed || 'Complete'}
              </span>
            </div>
          </div>

          {/* Timeline Container */}
          <div className="space-y-0 relative">
            {/* Timeline Line - Desktop only */}
            <div className="absolute left-8 top-8 bottom-8 w-0.5 bg-indigo-100 hidden md:block" />

            {mockLessons.map((lesson) => (
              <LessonCard
                key={lesson.id}
                lesson={lesson}
              />
            ))}
          </div>
        </div>
      </main>

      <BottomNav />
    </div>
  );
};
