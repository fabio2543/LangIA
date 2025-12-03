import { cn } from '../../utils/cn';
import type { TrailGenerationStatus } from '../../types/trail';

interface TrailGeneratingProps {
  status: TrailGenerationStatus | null;
  className?: string;
}

/**
 * Componente de UI para trilha em geração.
 */
export const TrailGenerating = ({ status, className }: TrailGeneratingProps) => {
  const progressPercentage = status?.progressPercentage ?? 0;
  const currentStep = status?.currentStep ?? 'Iniciando geração...';
  const message = status?.message ?? 'Sua trilha personalizada está sendo criada com IA';

  return (
    <div
      className={cn(
        'bg-gradient-to-br from-primary-light to-bg rounded-2xl p-8 text-center',
        className
      )}
    >
      {/* Animated Icon */}
      <div className="mb-6">
        <div className="relative w-24 h-24 mx-auto">
          {/* Outer ring */}
          <div className="absolute inset-0 border-4 border-primary/20 rounded-full" />
          {/* Spinning ring */}
          <div className="absolute inset-0 border-4 border-transparent border-t-primary rounded-full animate-spin" />
          {/* Center icon */}
          <div className="absolute inset-0 flex items-center justify-center">
            <span className="text-4xl">🎯</span>
          </div>
        </div>
      </div>

      {/* Title */}
      <h3 className="text-xl font-semibold text-text mb-2">
        Gerando sua trilha personalizada
      </h3>

      {/* Message */}
      <p className="text-textLight mb-6 max-w-md mx-auto">
        {message}
      </p>

      {/* Progress Bar */}
      <div className="max-w-xs mx-auto mb-4">
        <div className="flex justify-between text-sm mb-2">
          <span className="text-textLight">{currentStep}</span>
          <span className="font-medium text-primary">{progressPercentage}%</span>
        </div>
        <div className="h-2 bg-white/50 rounded-full overflow-hidden">
          <div
            className="h-full bg-primary rounded-full transition-all duration-500"
            style={{ width: `${progressPercentage}%` }}
          />
        </div>
      </div>

      {/* Modules/Lessons Progress */}
      {status && status.totalModules > 0 && (
        <div className="flex justify-center gap-6 text-sm text-textLight">
          <span>
            📚 {status.modulesGenerated}/{status.totalModules} módulos
          </span>
          {status.totalLessons > 0 && (
            <span>
              📝 {status.lessonsGenerated}/{status.totalLessons} lições
            </span>
          )}
        </div>
      )}

      {/* Error Message */}
      {status?.errorMessage && (
        <div className="mt-4 p-3 bg-red-50 rounded-lg text-red-600 text-sm">
          {status.errorMessage}
        </div>
      )}

      {/* Tips */}
      <div className="mt-8 p-4 bg-white/50 rounded-xl">
        <p className="text-sm text-textLight">
          💡 <strong>Dica:</strong> Enquanto sua trilha é gerada, você pode explorar
          outros idiomas ou configurar suas preferências de estudo.
        </p>
      </div>
    </div>
  );
};

export default TrailGenerating;
