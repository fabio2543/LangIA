import { Link } from 'react-router-dom';
import { Button } from '../common/Button';
import { Badge } from '../common/Badge';
import { useTranslation } from '../../i18n';
import type { Tutor } from '../../services/mockData/tutorsMock';

interface RecommendedTutorsProps {
  tutors: Tutor[];
}

export const RecommendedTutors = ({ tutors }: RecommendedTutorsProps) => {
  const { t } = useTranslation();

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-xl font-bold font-serif text-text">
          {t.dashboard?.recommendedTutors || 'Recommended Tutors'}
        </h3>
        <Link
          to="/tutors"
          className="text-sm font-medium text-primary hover:underline"
        >
          {t.dashboard?.viewAll || 'View all'}
        </Link>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {tutors.map((tutor) => (
          <div
            key={tutor.id}
            className="bg-white rounded-2xl p-4 shadow-card card-hover"
          >
            <div className="flex items-start gap-3 mb-3">
              {/* Avatar */}
              <div
                className="w-14 h-14 rounded-xl flex items-center justify-center text-2xl flex-shrink-0"
                style={{ backgroundColor: tutor.bgColor }}
              >
                {tutor.flag}
              </div>

              {/* Info */}
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2">
                  <h4 className="font-semibold text-text truncate">{tutor.name}</h4>
                  {tutor.isOnline && (
                    <span className="w-2 h-2 rounded-full bg-success flex-shrink-0" />
                  )}
                </div>
                <p className="text-sm text-text-light">{tutor.languages[0]}</p>
                <div className="flex items-center gap-1 mt-1">
                  <span className="text-sm">⭐</span>
                  <span className="text-sm font-medium text-text">
                    {tutor.rating}
                  </span>
                  <span className="text-xs text-text-light">
                    ({tutor.reviewCount})
                  </span>
                </div>
              </div>
            </div>

            {/* Tags */}
            <div className="flex flex-wrap gap-1.5 mb-3">
              {tutor.isSuperTutor && (
                <Badge variant="accent" className="text-xs">
                  {t.tutors?.superTutor || 'Super Tutor'}
                </Badge>
              )}
              {tutor.specialties.slice(0, 2).map((spec) => (
                <Badge key={spec} variant="light" className="text-xs">
                  {spec}
                </Badge>
              ))}
            </div>

            {/* Actions */}
            <div className="flex items-center justify-between">
              <span className="text-sm font-semibold text-text">
                ${tutor.hourlyRate}
                <span className="font-normal text-text-light">
                  {t.tutors?.perHour || '/hour'}
                </span>
              </span>
              <Button variant="ghost" size="sm" className="border border-primary">
                {t.tutors?.seeProfile || 'Profile'}
              </Button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
