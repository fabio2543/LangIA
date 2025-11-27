import { Badge } from '../common/Badge';
import { Button } from '../common/Button';
import { useTranslation } from '../../i18n';

const TEACHERS = [
  {
    id: 'teacher-1',
    name: 'Christian Howard',
    flag: '🇮🇹',
    role: 'Italian teacher',
    accent: 'Native Italian',
    reviews: '100%',
    tags: ['Business', 'Culture'],
    superTutor: true,
    bgColor: '#DBEAFE',
  },
  {
    id: 'teacher-2',
    name: 'Sandra Wilson',
    flag: '🇪🇸',
    role: 'Spanish teacher',
    accent: 'Latin American',
    reviews: '99%',
    tags: ['Travel', 'Conversation'],
    superTutor: true,
    bgColor: '#FCE7F3',
  },
  {
    id: 'teacher-3',
    name: 'Jimmy Cooper',
    flag: '🇬🇧',
    role: 'English teacher',
    accent: 'British accent',
    reviews: '98%',
    tags: ['IELTS', 'Academic'],
    superTutor: false,
    bgColor: '#E0E7FF',
  },
];

export const TeachersSection = () => {
  const { t } = useTranslation();

  return (
    <section id="tutors" className="px-6 lg:px-15 py-12 lg:py-15 bg-bg-warm">
      {/* Header */}
      <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center mb-10 gap-4">
        <div>
          <p className="text-text-light text-xs font-semibold uppercase tracking-wider mb-2">
            {t.teachers.sectionLabel}
          </p>
          <h2 className="text-3xl lg:text-4xl font-bold text-text font-serif">
            {t.teachers.title}
          </h2>
        </div>
        <div className="flex gap-2.5">
          <button
            className="w-12 h-12 rounded-full border border-gray-200 bg-white hover:bg-gray-50 text-lg transition-colors"
            aria-label="Previous"
          >
            ←
          </button>
          <button
            className="w-12 h-12 rounded-full bg-primary text-white hover:bg-primary-dark text-lg transition-colors"
            aria-label="Next"
          >
            →
          </button>
        </div>
      </div>

      {/* Teachers Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {TEACHERS.map((teacher) => (
          <div
            key={teacher.id}
            className="bg-white rounded-2xl overflow-hidden shadow-card"
          >
            {/* Image Area */}
            <div
              className="h-[180px] lg:h-[200px] flex items-center justify-center text-6xl lg:text-7xl relative"
              style={{ backgroundColor: teacher.bgColor }}
            >
              👨‍🏫
              {/* Tags */}
              {teacher.tags.map((tag, i) => (
                <Badge
                  key={tag}
                  variant="primary"
                  className={`absolute ${
                    i === 0
                      ? 'top-5 right-5'
                      : 'bottom-5 left-5'
                  }`}
                >
                  {tag}
                </Badge>
              ))}
            </div>

            {/* Info */}
            <div className="p-5">
              <div className="flex items-center gap-2 mb-1">
                <h3 className="text-lg font-semibold text-text">
                  {teacher.name}
                </h3>
                <span className="text-lg">{teacher.flag}</span>
              </div>

              {teacher.superTutor && (
                <Badge variant="accent" className="mb-2.5">
                  ⭐ {t.teachers.superTutor}
                </Badge>
              )}

              <p className="text-text-light text-sm mb-1">
                🗣️ {teacher.accent}
              </p>
              <p className="text-text-light text-sm mb-3">
                👍 {teacher.reviews} {t.teachers.positiveReviews}
              </p>

              <Button
                variant="ghost"
                fullWidth
                className="border border-primary"
              >
                {t.teachers.seeProfile}
              </Button>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
};
