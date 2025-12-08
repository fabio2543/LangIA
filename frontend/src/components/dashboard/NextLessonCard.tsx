import { Link } from 'react-router-dom';
import { Button } from '../common/Button';
import { useTranslation } from '../../i18n';
import type { Lesson, LessonType } from '../../services/mockData/lessonsMock';
import { getLessonTypeIcon } from '../../services/mockData/lessonsMock';

interface NextLessonCardProps {
  lesson: Lesson | null;
}

const typeColors: Record<LessonType, string> = {
  video: 'bg-blue-100 text-blue-700',
  quiz: 'bg-purple-100 text-purple-700',
  speaking: 'bg-orange-100 text-orange-700',
  reading: 'bg-green-100 text-green-700',
  writing: 'bg-pink-100 text-pink-700',
};

export const NextLessonCard = ({ lesson }: NextLessonCardProps) => {
  const { t } = useTranslation();

  if (!lesson) {
    return (
      <div className="bg-white rounded-2xl shadow-md p-6">
        <p className="text-lg font-medium text-muted-foreground">
          {t.dashboard?.comingSoonDesc || 'No lessons available'}
        </p>
      </div>
    );
  }

  const typeKey = lesson.type as keyof typeof t.lessons.lessonTypes;

  return (
    <div className="relative group">
      {/* Gradient glow effect */}
      <div className="absolute -inset-0.5 bg-gradient-to-r from-primary to-accent rounded-3xl opacity-20 group-hover:opacity-40 transition duration-500 blur" />

      <div className="relative overflow-hidden border-0 rounded-2xl shadow-md bg-white">
        <div className="flex flex-col md:flex-row">
          {/* Left side - indigo background */}
          <div className="bg-indigo-50 p-6 md:w-1/3 flex flex-col justify-center items-start">
            <span
              className={`inline-block px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wide mb-3 ${typeColors[lesson.type]}`}
            >
              {getLessonTypeIcon(lesson.type)} {t.lessons?.lessonTypes?.[typeKey] || lesson.type}
            </span>
            <h4 className="font-serif text-2xl font-bold text-indigo-950 mb-2">
              {lesson.title}
            </h4>
            <p className="text-muted-foreground mb-4 text-sm">
              {t.dashboard?.nextLesson || 'Next Lesson'}
            </p>
            <Link to={`/lesson/${lesson.id}`}>
              <Button
                variant="primary"
                size="md"
                className="rounded-full w-full md:w-auto shadow-lg"
              >
                {lesson.status === 'in_progress'
                  ? t.lessons?.continueLesson || 'Continue'
                  : t.dashboard?.startLesson || 'Start Lesson'}
              </Button>
            </Link>
          </div>

          {/* Right side - white */}
          <div className="p-6 md:w-2/3 flex flex-col justify-center">
            <p className="text-muted-foreground text-lg mb-4 line-clamp-3">
              {lesson.description}
            </p>
            <div className="flex items-center gap-6 text-sm font-medium text-slate-500">
              <div className="flex items-center gap-2">
                <div className="w-1.5 h-1.5 rounded-full bg-slate-400" />
                {lesson.duration} {t.lessons?.duration || 'min'}
              </div>
              <div className="flex items-center gap-2">
                <div className="w-1.5 h-1.5 rounded-full bg-slate-400" />
                +{lesson.xpReward} XP
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
