import { useTranslation } from '../../i18n';

interface ProgressCardProps {
  currentLevel: string;
  nextLevel: string;
  progressPercent: number;
}

export const ProgressCard = ({
  currentLevel,
  nextLevel,
  progressPercent,
}: ProgressCardProps) => {
  const { t } = useTranslation();

  return (
    <div className="p-6 bg-indigo-600 text-white rounded-3xl shadow-xl relative overflow-hidden">
      {/* Blur effect decoration */}
      <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full -mr-16 -mt-16 blur-2xl" />

      <div className="relative z-10">
        <div className="flex justify-between items-end mb-4">
          <div>
            <p className="text-indigo-200 font-medium mb-1">
              {t.dashboard?.currentLevel || 'Current Level'}: {currentLevel}
            </p>
            <h2 className="text-2xl font-bold">
              {progressPercent}% to {nextLevel}
            </h2>
          </div>
          <span className="bg-white/20 px-3 py-1 rounded-full text-sm font-medium cursor-pointer hover:bg-white/30 transition-colors">
            See details
          </span>
        </div>

        {/* Progress bar */}
        <div className="h-3 bg-indigo-900/50 rounded-full overflow-hidden">
          <div
            className="h-full bg-white rounded-full transition-all duration-500"
            style={{ width: `${progressPercent}%` }}
          />
        </div>
      </div>
    </div>
  );
};
