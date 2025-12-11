import { useState, useCallback } from 'react';
import { cn } from '../../utils/cn';
import { ChunkMasteryIndicator } from './ChunkMasteryIndicator';
import type { LinguisticChunk, ChunkMastery } from '../../types';

type PracticeMode = 'flashcard' | 'typing' | 'multiple_choice';
type PracticeStep = 'question' | 'answer' | 'rating';

interface ChunkPracticeProps {
  chunk: LinguisticChunk;
  mastery?: ChunkMastery;
  mode?: PracticeMode;
  onComplete: (quality: number, context?: string) => void;
  onSkip?: () => void;
  className?: string;
}

const QUALITY_OPTIONS = [
  { value: 0, label: 'Esqueci', emoji: '😰', color: 'bg-red-100 text-red-700 hover:bg-red-200' },
  { value: 2, label: 'Difícil', emoji: '😓', color: 'bg-orange-100 text-orange-700 hover:bg-orange-200' },
  { value: 3, label: 'OK', emoji: '🤔', color: 'bg-yellow-100 text-yellow-700 hover:bg-yellow-200' },
  { value: 4, label: 'Fácil', emoji: '😊', color: 'bg-lime-100 text-lime-700 hover:bg-lime-200' },
  { value: 5, label: 'Muito Fácil', emoji: '🎉', color: 'bg-green-100 text-green-700 hover:bg-green-200' },
];

export const ChunkPractice = ({
  chunk,
  mastery,
  mode = 'flashcard',
  onComplete,
  onSkip,
  className,
}: ChunkPracticeProps) => {
  const [step, setStep] = useState<PracticeStep>('question');
  const [userInput, setUserInput] = useState('');
  const [isCorrect, setIsCorrect] = useState<boolean | null>(null);

  const handleReveal = useCallback(() => {
    if (mode === 'typing') {
      const normalized = userInput.trim().toLowerCase();
      const expected = chunk.chunkText.toLowerCase();
      setIsCorrect(normalized === expected);
    }
    setStep('answer');
  }, [mode, userInput, chunk.chunkText]);

  const handleRate = useCallback(
    (quality: number) => {
      onComplete(quality, mode === 'typing' ? userInput : undefined);
      setStep('question');
      setUserInput('');
      setIsCorrect(null);
    },
    [onComplete, mode, userInput]
  );

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && step === 'question') {
      handleReveal();
    }
  };

  const playAudio = () => {
    if (chunk.audioUrl) {
      const audio = new Audio(chunk.audioUrl);
      audio.play();
    }
  };

  return (
    <div className={cn('max-w-lg mx-auto', className)}>
      {/* Progress Header */}
      {mastery && (
        <div className="flex items-center justify-between mb-6">
          <ChunkMasteryIndicator level={mastery.masteryLevel} size="md" showLabel />
          {onSkip && (
            <button
              onClick={onSkip}
              className="text-sm text-text-light hover:text-text"
            >
              Pular →
            </button>
          )}
        </div>
      )}

      {/* Question Step */}
      {step === 'question' && (
        <div className="space-y-6">
          {/* Translation (what user needs to remember) */}
          <div className="bg-gradient-to-br from-primary to-primary-dark rounded-3xl p-8 text-center text-white shadow-lg">
            <p className="text-sm opacity-70 mb-2">Como se diz...</p>
            <p className="text-2xl md:text-3xl font-bold">{chunk.translation}</p>
            {chunk.usageContext && (
              <p className="text-sm opacity-70 mt-4 italic">
                Contexto: {chunk.usageContext}
              </p>
            )}
          </div>

          {/* Input Mode */}
          {mode === 'typing' && (
            <div className="space-y-3">
              <input
                type="text"
                value={userInput}
                onChange={(e) => setUserInput(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Digite em inglês..."
                className="w-full px-4 py-4 rounded-xl border border-gray-200 text-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                autoFocus
              />
              <p className="text-xs text-text-light text-center">
                Pressione Enter para verificar
              </p>
            </div>
          )}

          {/* Reveal Button */}
          <button
            onClick={handleReveal}
            className="w-full py-4 bg-primary text-white rounded-xl font-medium hover:bg-primary-dark transition-colors"
          >
            {mode === 'typing' ? 'Verificar' : 'Mostrar Resposta'}
          </button>
        </div>
      )}

      {/* Answer Step */}
      {step === 'answer' && (
        <div className="space-y-6">
          {/* Result (for typing mode) */}
          {mode === 'typing' && isCorrect !== null && (
            <div
              className={cn(
                'p-4 rounded-xl text-center',
                isCorrect ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
              )}
            >
              <p className="text-lg font-bold">
                {isCorrect ? '🎉 Correto!' : '😅 Quase lá!'}
              </p>
              {!isCorrect && (
                <p className="text-sm mt-1">
                  Sua resposta: <span className="font-medium">{userInput}</span>
                </p>
              )}
            </div>
          )}

          {/* Answer Card */}
          <div className="bg-white rounded-3xl p-8 text-center shadow-lg border border-gray-100">
            <p className="text-sm text-text-light mb-2">Resposta</p>
            <p className="text-2xl md:text-3xl font-bold text-primary mb-4">
              {chunk.chunkText}
            </p>
            <p className="text-text-light">{chunk.translation}</p>

            {chunk.audioUrl && (
              <button
                onClick={playAudio}
                className="mt-4 px-6 py-2 bg-primary-light text-primary rounded-full hover:bg-primary hover:text-white transition-colors"
              >
                🔊 Ouvir
              </button>
            )}

            {/* Variations */}
            {chunk.variations.length > 0 && (
              <div className="mt-6 pt-4 border-t border-gray-100">
                <p className="text-xs text-text-light mb-2">Outras formas:</p>
                <div className="flex flex-wrap justify-center gap-2">
                  {chunk.variations.map((v, i) => (
                    <span
                      key={i}
                      className="text-sm bg-gray-100 px-3 py-1 rounded-full"
                    >
                      {v}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Rating Buttons */}
          <div className="space-y-3">
            <p className="text-center text-text-light text-sm">
              Como foi lembrar?
            </p>
            <div className="grid grid-cols-5 gap-2">
              {QUALITY_OPTIONS.map((option) => (
                <button
                  key={option.value}
                  onClick={() => handleRate(option.value)}
                  className={cn(
                    'flex flex-col items-center py-3 px-2 rounded-xl transition-colors',
                    option.color
                  )}
                >
                  <span className="text-xl mb-1">{option.emoji}</span>
                  <span className="text-xs font-medium">{option.label}</span>
                </button>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
