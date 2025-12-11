import { useState, useEffect, useCallback } from 'react';
import { cn } from '../../utils/cn';
import { srsService, handleApiError } from '../../services/srsService';
import type { SrsCardWithProgress, SrsQuality } from '../../types';
import { FlashcardViewer } from './FlashcardViewer';
import { SrsProgressBar } from './SrsProgressBar';
import { Button } from '../common/Button';

interface SrsReviewSessionProps {
  languageCode: string;
  onComplete?: (stats: { reviewed: number; correct: number }) => void;
  onCancel?: () => void;
  className?: string;
}

const qualityButtons: { quality: SrsQuality; label: string; sublabel: string; color: string }[] = [
  { quality: 0, label: 'Não lembrei', sublabel: 'Esqueci completamente', color: 'bg-red-500 hover:bg-red-600' },
  { quality: 1, label: 'Errei', sublabel: 'Lembrei ao ver resposta', color: 'bg-orange-500 hover:bg-orange-600' },
  { quality: 2, label: 'Difícil', sublabel: 'Lembrei com dificuldade', color: 'bg-yellow-500 hover:bg-yellow-600' },
  { quality: 3, label: 'Ok', sublabel: 'Lembrei com esforço', color: 'bg-lime-500 hover:bg-lime-600' },
  { quality: 4, label: 'Fácil', sublabel: 'Lembrei bem', color: 'bg-green-500 hover:bg-green-600' },
  { quality: 5, label: 'Perfeito', sublabel: 'Resposta instantânea', color: 'bg-emerald-500 hover:bg-emerald-600' },
];

export const SrsReviewSession = ({
  languageCode,
  onComplete,
  onCancel,
  className,
}: SrsReviewSessionProps) => {
  const [cards, setCards] = useState<SrsCardWithProgress[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isFlipped, setIsFlipped] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [stats, setStats] = useState({ reviewed: 0, correct: 0 });

  useEffect(() => {
    const loadCards = async () => {
      try {
        setIsLoading(true);
        const response = await srsService.getDueCards(languageCode);
        setCards(response.cards);
      } catch (err) {
        const apiError = handleApiError(err);
        setError(apiError.message);
      } finally {
        setIsLoading(false);
      }
    };

    loadCards();
  }, [languageCode]);

  const currentCard = cards[currentIndex];
  const isComplete = currentIndex >= cards.length;

  const handleFlip = useCallback(() => {
    setIsFlipped((prev) => !prev);
  }, []);

  const handleAnswer = async (quality: SrsQuality) => {
    if (!currentCard || isSubmitting) return;

    try {
      setIsSubmitting(true);
      await srsService.reviewCard({
        cardId: currentCard.id,
        quality,
      });

      setStats((prev) => ({
        reviewed: prev.reviewed + 1,
        correct: quality >= 3 ? prev.correct + 1 : prev.correct,
      }));

      setIsFlipped(false);
      setCurrentIndex((prev) => prev + 1);
    } catch (err) {
      const apiError = handleApiError(err);
      setError(apiError.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Keyboard shortcuts
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (isComplete || isLoading) return;

      if (e.key === ' ' && !isFlipped) {
        e.preventDefault();
        handleFlip();
      } else if (isFlipped && e.key >= '1' && e.key <= '6') {
        e.preventDefault();
        const quality = (parseInt(e.key) - 1) as SrsQuality;
        handleAnswer(quality);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isFlipped, isComplete, isLoading, handleFlip]);

  if (isLoading) {
    return (
      <div className={cn('flex flex-col items-center justify-center min-h-[400px]', className)}>
        <div className="text-4xl mb-4 animate-pulse">📚</div>
        <p className="text-text-light">Carregando cards...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className={cn('flex flex-col items-center justify-center min-h-[400px]', className)}>
        <div className="text-4xl mb-4">😕</div>
        <p className="text-red-600 mb-4">{error}</p>
        <Button variant="outline" onClick={onCancel}>
          Voltar
        </Button>
      </div>
    );
  }

  if (cards.length === 0) {
    return (
      <div className={cn('flex flex-col items-center justify-center min-h-[400px]', className)}>
        <div className="text-6xl mb-4">🎉</div>
        <h2 className="text-2xl font-bold text-text mb-2">Parabéns!</h2>
        <p className="text-text-light mb-6">Não há cards para revisar agora.</p>
        <Button variant="primary" onClick={onCancel}>
          Voltar ao Dashboard
        </Button>
      </div>
    );
  }

  if (isComplete) {
    const accuracy = stats.reviewed > 0 ? Math.round((stats.correct / stats.reviewed) * 100) : 0;

    return (
      <div className={cn('flex flex-col items-center justify-center min-h-[400px]', className)}>
        <div className="text-6xl mb-4">🏆</div>
        <h2 className="text-2xl font-bold text-text mb-2">Sessão Completa!</h2>
        <div className="grid grid-cols-3 gap-6 my-6">
          <div className="text-center">
            <p className="text-3xl font-bold text-primary">{stats.reviewed}</p>
            <p className="text-sm text-text-light">Revisados</p>
          </div>
          <div className="text-center">
            <p className="text-3xl font-bold text-success">{stats.correct}</p>
            <p className="text-sm text-text-light">Acertos</p>
          </div>
          <div className="text-center">
            <p className="text-3xl font-bold text-streak">{accuracy}%</p>
            <p className="text-sm text-text-light">Precisão</p>
          </div>
        </div>
        <div className="flex gap-4">
          <Button variant="outline" onClick={onCancel}>
            Voltar
          </Button>
          <Button
            variant="primary"
            onClick={() => onComplete?.(stats)}
          >
            Continuar
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className={cn('flex flex-col', className)}>
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <Button variant="ghost" size="sm" onClick={onCancel}>
          ← Sair
        </Button>
        <SrsProgressBar current={currentIndex} total={cards.length} className="flex-1 mx-4 max-w-md" />
        <div className="text-sm text-text-light">
          {currentIndex + 1}/{cards.length}
        </div>
      </div>

      {/* Card */}
      <div className="flex-1 flex items-center justify-center mb-6">
        <FlashcardViewer
          card={currentCard}
          isFlipped={isFlipped}
          onFlip={handleFlip}
          className="w-full max-w-lg"
        />
      </div>

      {/* Answer Buttons */}
      {isFlipped ? (
        <div className="space-y-4">
          <p className="text-center text-sm text-text-light mb-2">
            Como foi sua lembrança? (ou use teclas 1-6)
          </p>
          <div className="grid grid-cols-3 md:grid-cols-6 gap-2">
            {qualityButtons.map((btn) => (
              <button
                key={btn.quality}
                onClick={() => handleAnswer(btn.quality)}
                disabled={isSubmitting}
                className={cn(
                  'p-3 rounded-xl text-white transition-all duration-200',
                  'flex flex-col items-center justify-center min-h-[80px]',
                  'disabled:opacity-50 disabled:cursor-not-allowed',
                  btn.color
                )}
              >
                <span className="font-medium text-sm">{btn.label}</span>
                <span className="text-xs opacity-80 hidden md:block">{btn.sublabel}</span>
              </button>
            ))}
          </div>
        </div>
      ) : (
        <div className="text-center">
          <p className="text-text-light mb-4">
            Pense na resposta e toque no card para revelar
          </p>
          <Button variant="primary" size="lg" onClick={handleFlip}>
            Mostrar Resposta (Espaço)
          </Button>
        </div>
      )}
    </div>
  );
};
