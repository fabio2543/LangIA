import { useState } from 'react';
import { cn } from '../../utils/cn';
import { ChunkMasteryIndicator } from './ChunkMasteryIndicator';
import type { LinguisticChunk, ChunkMastery } from '../../types';

interface ChunkCardProps {
  chunk: LinguisticChunk;
  mastery?: ChunkMastery;
  onPractice?: (chunk: LinguisticChunk) => void;
  onPlayAudio?: (audioUrl: string) => void;
  showVariations?: boolean;
  className?: string;
}

const CATEGORY_ICONS: Record<string, string> = {
  greeting: '👋',
  request: '🙏',
  question: '❓',
  direction: '🧭',
  emergency: '🚨',
  social: '🤝',
  shopping: '🛒',
  travel: '✈️',
};

const CATEGORY_LABELS: Record<string, string> = {
  greeting: 'Saudação',
  request: 'Pedido',
  question: 'Pergunta',
  direction: 'Direção',
  emergency: 'Emergência',
  social: 'Social',
  shopping: 'Compras',
  travel: 'Viagem',
};

export const ChunkCard = ({
  chunk,
  mastery,
  onPractice,
  onPlayAudio,
  showVariations = false,
  className,
}: ChunkCardProps) => {
  const [isExpanded, setIsExpanded] = useState(false);

  const handlePlayAudio = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (chunk.audioUrl) {
      if (onPlayAudio) {
        onPlayAudio(chunk.audioUrl);
      } else {
        const audio = new Audio(chunk.audioUrl);
        audio.play();
      }
    }
  };

  const handlePractice = (e: React.MouseEvent) => {
    e.stopPropagation();
    onPractice?.(chunk);
  };

  return (
    <article
      className={cn(
        'rounded-2xl bg-white shadow-sm border border-gray-100 overflow-hidden',
        'transition-all hover:shadow-md',
        className
      )}
    >
      {/* Header */}
      <div className="p-4 md:p-6">
        <div className="flex items-start justify-between gap-4 mb-3">
          <div className="flex items-center gap-2">
            <span
              className="w-8 h-8 rounded-lg bg-primary-light flex items-center justify-center text-lg"
              aria-hidden="true"
            >
              {CATEGORY_ICONS[chunk.category] || '📝'}
            </span>
            <div>
              <span className="text-xs text-text-light">
                {CATEGORY_LABELS[chunk.category] || chunk.category}
              </span>
              <div className="flex items-center gap-2">
                <span className="text-xs bg-primary-light text-primary px-2 py-0.5 rounded-full">
                  {chunk.cefrLevel}
                </span>
                {chunk.isCore && (
                  <span className="text-xs bg-yellow-100 text-yellow-700 px-2 py-0.5 rounded-full">
                    Essencial
                  </span>
                )}
              </div>
            </div>
          </div>

          {chunk.audioUrl && (
            <button
              onClick={handlePlayAudio}
              className="w-10 h-10 rounded-full bg-primary-light text-primary flex items-center justify-center hover:bg-primary hover:text-white transition-colors"
              aria-label="Ouvir pronúncia"
            >
              🔊
            </button>
          )}
        </div>

        {/* Chunk Text */}
        <p className="text-xl md:text-2xl font-bold text-text mb-2">
          {chunk.chunkText}
        </p>

        {/* Translation */}
        <p className="text-text-light mb-3">{chunk.translation}</p>

        {/* Context */}
        {chunk.usageContext && (
          <p className="text-sm text-text-light italic bg-gray-50 p-3 rounded-lg mb-3">
            {chunk.usageContext}
          </p>
        )}

        {/* Mastery Indicator */}
        {mastery && (
          <div className="flex items-center justify-between mb-3">
            <ChunkMasteryIndicator
              level={mastery.masteryLevel}
              size="md"
              showLabel
            />
            <span className="text-xs text-text-light">
              Praticado {mastery.timesPracticed}x
            </span>
          </div>
        )}

        {/* Variations Toggle */}
        {showVariations && chunk.variations.length > 0 && (
          <button
            onClick={() => setIsExpanded(!isExpanded)}
            className="text-sm text-primary hover:text-primary-dark flex items-center gap-1"
          >
            <span>{isExpanded ? '▼' : '▶'}</span>
            {chunk.variations.length} variações
          </button>
        )}
      </div>

      {/* Variations (expanded) */}
      {isExpanded && chunk.variations.length > 0 && (
        <div className="px-4 md:px-6 pb-4 md:pb-6 pt-0">
          <div className="border-t border-gray-100 pt-4">
            <p className="text-xs text-text-light mb-2 uppercase tracking-wide">
              Variações
            </p>
            <ul className="space-y-2">
              {chunk.variations.map((variation, index) => (
                <li
                  key={index}
                  className="text-sm text-text bg-gray-50 px-3 py-2 rounded-lg"
                >
                  {variation}
                </li>
              ))}
            </ul>
          </div>
        </div>
      )}

      {/* Actions */}
      {onPractice && (
        <div className="px-4 md:px-6 pb-4 md:pb-6 pt-0">
          <button
            onClick={handlePractice}
            className="w-full py-3 bg-primary text-white rounded-xl font-medium hover:bg-primary-dark transition-colors"
          >
            Praticar
          </button>
        </div>
      )}
    </article>
  );
};
