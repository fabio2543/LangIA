import { cn } from '../../utils/cn';

interface LearningMomentCardProps {
  learningMoment: string;
  selfCorrectionAchieved: boolean;
  finalCorrection?: string;
  onRate?: (rating: number) => void;
  className?: string;
}

export const LearningMomentCard = ({
  learningMoment,
  selfCorrectionAchieved,
  finalCorrection,
  onRate,
  className,
}: LearningMomentCardProps) => {
  return (
    <div
      className={cn(
        'rounded-2xl overflow-hidden',
        selfCorrectionAchieved
          ? 'bg-gradient-to-br from-green-50 to-emerald-50 border border-green-200'
          : 'bg-gradient-to-br from-blue-50 to-indigo-50 border border-blue-200',
        className
      )}
    >
      {/* Header */}
      <div
        className={cn(
          'px-6 py-4 flex items-center gap-3',
          selfCorrectionAchieved ? 'bg-green-100/50' : 'bg-blue-100/50'
        )}
      >
        <span className="text-2xl">
          {selfCorrectionAchieved ? '🎉' : '💡'}
        </span>
        <div>
          <h3
            className={cn(
              'font-bold',
              selfCorrectionAchieved ? 'text-green-800' : 'text-blue-800'
            )}
          >
            {selfCorrectionAchieved ? 'Você se autocorrigiu!' : 'Momento de Aprendizado'}
          </h3>
          <p
            className={cn(
              'text-sm',
              selfCorrectionAchieved ? 'text-green-600' : 'text-blue-600'
            )}
          >
            {selfCorrectionAchieved
              ? 'Excelente reflexão!'
              : 'Veja o que aprendemos'}
          </p>
        </div>
      </div>

      {/* Content */}
      <div className="px-6 py-5 space-y-4">
        {/* Learning Moment */}
        <div>
          <p className="text-sm text-text-light mb-2 uppercase tracking-wide">
            O que aprendemos
          </p>
          <p className="text-text font-medium">{learningMoment}</p>
        </div>

        {/* Final Correction (if not self-corrected) */}
        {!selfCorrectionAchieved && finalCorrection && (
          <div className="pt-4 border-t border-gray-200">
            <p className="text-sm text-text-light mb-2 uppercase tracking-wide">
              Forma correta
            </p>
            <p className="text-primary font-bold text-lg">{finalCorrection}</p>
          </div>
        )}
      </div>

      {/* Rating */}
      {onRate && (
        <div className="px-6 py-4 bg-white/50 border-t border-gray-100">
          <p className="text-sm text-text-light mb-3 text-center">
            Essa explicação foi útil?
          </p>
          <div className="flex justify-center gap-2">
            {[1, 2, 3, 4, 5].map((rating) => (
              <button
                key={rating}
                onClick={() => onRate(rating)}
                className="w-10 h-10 rounded-full bg-gray-100 hover:bg-primary-light hover:text-primary transition-colors flex items-center justify-center"
                aria-label={`Avaliar com ${rating} estrelas`}
              >
                ⭐
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
