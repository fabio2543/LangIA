import { useTranslation } from '../../i18n';
import type { AuthUser } from '../../types';

interface ProfileHeaderProps {
  user: AuthUser;
}

export const ProfileHeader = ({ user }: ProfileHeaderProps) => {
  const { t } = useTranslation();

  return (
    <div className="bg-gradient-to-r from-primary to-primary-dark">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center gap-6">
          {/* Avatar */}
          <div className="w-20 h-20 sm:w-24 sm:h-24 rounded-full bg-white/20 flex items-center justify-center text-4xl sm:text-5xl shadow-lg">
            {user.profile === 'TEACHER' ? '👨‍🏫' : '🎓'}
          </div>

          {/* User Info */}
          <div className="text-white">
            <h1 className="text-2xl sm:text-3xl font-serif italic mb-1">
              {user.name}
            </h1>
            <p className="text-white/80 text-sm sm:text-base">
              {user.email}
            </p>
            <span className="inline-flex items-center gap-1 mt-2 px-3 py-1 bg-white/20 rounded-full text-sm">
              {user.profile === 'TEACHER' ? t.dashboard.teacher : t.dashboard.student}
            </span>
          </div>
        </div>

        {/* Page Title */}
        <div className="mt-6 pt-6 border-t border-white/20">
          <h2 className="text-xl sm:text-2xl font-semibold text-white">
            {t.profile.title}
          </h2>
          <p className="text-white/70 mt-1">
            {t.profile.subtitle}
          </p>
        </div>
      </div>
    </div>
  );
};
