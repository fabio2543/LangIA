import { useState } from 'react';
import { cn } from '../../utils/cn';
import type { VocabularyCard } from '../../types';

interface FlashcardViewerProps {
  card: VocabularyCard;
  isFlipped?: boolean;
  onFlip?: () => void;
  className?: string;
}

export const FlashcardViewer = ({
  card,
  isFlipped = false,
  onFlip,
  className,
}: FlashcardViewerProps) => {
  const [internalFlipped, setInternalFlipped] = useState(false);
  const flipped = onFlip ? isFlipped : internalFlipped;

  const handleFlip = () => {
    if (onFlip) {
      onFlip();
    } else {
      setInternalFlipped(!internalFlipped);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === ' ' || e.key === 'Enter') {
      e.preventDefault();
      handleFlip();
    }
  };

  return (
    <div
      className={cn('perspective-1000', className)}
      style={{ perspective: '1000px' }}
    >
      <div
        role="button"
        tabIndex={0}
        onClick={handleFlip}
        onKeyDown={handleKeyDown}
        className={cn(
          'relative w-full h-64 md:h-80 cursor-pointer transition-transform duration-500',
          'transform-style-preserve-3d'
        )}
        style={{
          transformStyle: 'preserve-3d',
          transform: flipped ? 'rotateY(180deg)' : 'rotateY(0deg)',
        }}
        aria-label={flipped ? 'Mostrar frente do card' : 'Mostrar verso do card'}
      >
        {/* Front */}
        <div
          className={cn(
            'absolute inset-0 w-full h-full rounded-3xl shadow-lg p-6 md:p-8',
            'flex flex-col items-center justify-center text-center',
            'bg-gradient-to-br from-primary to-primary-dark text-white',
            'backface-hidden'
          )}
          style={{ backfaceVisibility: 'hidden' }}
        >
          <div className="absolute top-4 left-4 flex items-center gap-2">
            <span className="text-xs bg-white/20 px-2 py-1 rounded-full">
              {card.cardType === 'word' && '📝 Palavra'}
              {card.cardType === 'chunk' && '🧩 Chunk'}
              {card.cardType === 'phrase' && '💬 Frase'}
              {card.cardType === 'grammar' && '📚 Gramática'}
            </span>
            <span className="text-xs bg-white/20 px-2 py-1 rounded-full">
              {card.cefrLevel}
            </span>
          </div>

          <p className="text-3xl md:text-4xl font-bold mb-4">{card.front}</p>

          {card.context && (
            <p className="text-sm text-white/70 mb-2">"{card.context}"</p>
          )}

          <div className="absolute bottom-4 flex items-center gap-2 text-white/60 text-sm">
            <span>👆 Toque para virar</span>
          </div>

          {card.audioUrl && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                const audio = new Audio(card.audioUrl);
                audio.play();
              }}
              className="absolute top-4 right-4 w-10 h-10 rounded-full bg-white/20 flex items-center justify-center hover:bg-white/30 transition-colors"
              aria-label="Ouvir pronúncia"
            >
              🔊
            </button>
          )}
        </div>

        {/* Back */}
        <div
          className={cn(
            'absolute inset-0 w-full h-full rounded-3xl shadow-lg p-6 md:p-8',
            'flex flex-col items-center justify-center text-center',
            'bg-white text-text',
            'backface-hidden'
          )}
          style={{
            backfaceVisibility: 'hidden',
            transform: 'rotateY(180deg)',
          }}
        >
          <div className="absolute top-4 left-4">
            <span className="text-xs bg-success-light text-success px-2 py-1 rounded-full">
              Resposta
            </span>
          </div>

          <p className="text-3xl md:text-4xl font-bold text-primary mb-4">
            {card.back}
          </p>

          {card.exampleSentence && (
            <div className="mt-4 p-4 bg-gray-50 rounded-xl w-full max-w-md">
              <p className="text-sm text-text-light mb-1">Exemplo:</p>
              <p className="text-text italic">"{card.exampleSentence}"</p>
            </div>
          )}

          {card.tags.length > 0 && (
            <div className="absolute bottom-4 flex gap-2 flex-wrap justify-center">
              {card.tags.map((tag) => (
                <span
                  key={tag}
                  className="text-xs bg-gray-100 text-text-light px-2 py-1 rounded-full"
                >
                  #{tag}
                </span>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
