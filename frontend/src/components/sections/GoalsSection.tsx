import { useState } from 'react';
import { useTranslation } from '../../i18n';

export const GoalsSection = () => {
  const { t } = useTranslation();
  const [selectedGoal, setSelectedGoal] = useState('career');

  const goals = [
    { id: 'career', emoji: '💼', label: t.goals.career },
    { id: 'university', emoji: '🎓', label: t.goals.university },
    { id: 'test', emoji: '📝', label: t.goals.test },
    { id: 'travel', emoji: '🌍', label: t.goals.travel },
    { id: 'fun', emoji: '🎉', label: t.goals.fun },
  ];

  const tracks = [
    { icon: '🎓', name: t.goals.grammar, progress: 75 },
    { icon: '💬', name: t.goals.coherence, progress: 60 },
    { icon: '📚', name: t.goals.vocabulary, progress: 85 },
  ];

  return (
    <section className="px-6 lg:px-15 py-16 lg:py-20">
      {/* Title */}
      <div className="text-center mb-12">
        <h2 className="text-3xl lg:text-[40px] font-bold text-text font-serif">
          {t.goals.title}
        </h2>
        <p className="text-text-light text-base mt-3 max-w-xl mx-auto">
          {t.goals.subtitle}
        </p>
      </div>

      {/* Cards */}
      <div className="flex flex-col lg:flex-row gap-6">
        {/* Card 1: Goals */}
        <div className="flex-1 bg-primary-light rounded-3xl p-6 lg:p-8 min-h-[420px] card-hover">
          <h3 className="text-xl lg:text-[22px] font-semibold text-text mb-6 lg:mb-8">
            {t.goals.focusTitle}
          </h3>

          {/* Phone Mockup */}
          <div className="w-[200px] lg:w-[220px] bg-white rounded-[30px] p-3 shadow-xl mx-auto">
            {/* Status Bar */}
            <div className="flex justify-between px-3 py-2 text-xs text-text-light">
              <span>9:41</span>
              <span>📶 🔋</span>
            </div>

            {/* Content */}
            <div className="p-4 text-center">
              <p className="text-base lg:text-lg font-semibold text-text mb-5">
                {t.goals.question}
              </p>

              {goals.map((goal) => (
                <button
                  key={goal.id}
                  onClick={() => setSelectedGoal(goal.id)}
                  className={`flex items-center gap-2.5 w-full px-4 py-2.5 rounded-full mb-2 text-xs lg:text-sm font-medium justify-center transition-colors ${
                    selectedGoal === goal.id
                      ? 'bg-primary text-white'
                      : 'bg-gray-100 text-text hover:bg-gray-200'
                  }`}
                >
                  <span>{goal.emoji}</span>
                  <span>{goal.label}</span>
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Card 2: Track Progress */}
        <div className="flex-1 bg-bg rounded-3xl p-6 lg:p-8 min-h-[420px] card-hover">
          <h3 className="text-xl lg:text-[22px] font-semibold text-text mb-5">
            {t.goals.trackTitle}
          </h3>

          {/* Image Placeholder */}
          <div className="w-full h-[180px] lg:h-[200px] bg-gradient-to-br from-primary-light to-white rounded-2xl flex items-center justify-center text-6xl lg:text-[80px] mb-6">
            🎯
          </div>

          {/* Progress Circles */}
          <div className="flex justify-around">
            {tracks.map((track) => (
              <div key={track.name} className="text-center">
                <div className="w-16 lg:w-[70px] h-16 lg:h-[70px] rounded-full border-4 border-primary flex items-center justify-center text-2xl lg:text-[28px] bg-white mx-auto mb-2.5">
                  {track.icon}
                </div>
                <span className="text-xs lg:text-sm font-semibold text-text">
                  {track.name}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
};
