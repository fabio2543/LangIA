import { useState, useEffect } from 'react';
import { useParams, useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useTranslation } from '../i18n';
import { Button } from '../components/common/Button';
import { mockLessons, getExercisesByLessonId } from '../services/mockData';

type FeedbackType = 'none' | 'success' | 'error';

export const LessonViewPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();
  const { t } = useTranslation();

  const [step, setStep] = useState(0);
  const [feedback, setFeedback] = useState<FeedbackType>('none');
  const [selectedOption, setSelectedOption] = useState<number | null>(null);
  const [isComplete, setIsComplete] = useState(false);
  const [totalXpEarned, setTotalXpEarned] = useState(0);

  // Get lesson and exercises
  const lesson = mockLessons.find((l) => l.id === id);
  const exercises = id ? getExercisesByLessonId(id) : [];
  const currentExercise = exercises[step];
  const progress = exercises.length > 0 ? ((step + 1) / exercises.length) * 100 : 0;

  // Reset state when lesson changes
  useEffect(() => {
    setStep(0);
    setFeedback('none');
    setSelectedOption(null);
    setIsComplete(false);
    setTotalXpEarned(0);
  }, [id]);

  if (authLoading) {
    return (
      <div className="min-h-screen bg-bg-warm flex items-center justify-center animate-fade-in">
        <div className="text-center">
          <div className="text-4xl animate-pulse-slow">📚</div>
          <p className="text-text-light mt-2">{t.profile.common.loading}</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  if (!lesson || exercises.length === 0) {
    return (
      <div className="min-h-screen bg-bg-warm flex items-center justify-center">
        <div className="text-center">
          <div className="text-6xl mb-4">😕</div>
          <h2 className="text-2xl font-bold text-text mb-2">Lesson not found</h2>
          <p className="text-text-light mb-6">The lesson you're looking for doesn't exist.</p>
          <Button variant="primary" onClick={() => navigate('/lessons')}>
            {t.lessons?.title || 'Back to Lessons'}
          </Button>
        </div>
      </div>
    );
  }

  const handleCheck = (optionIndex?: number) => {
    if (currentExercise.type === 'select' && optionIndex !== undefined) {
      const isCorrect = optionIndex === currentExercise.correctAnswer;
      setSelectedOption(optionIndex);
      setFeedback(isCorrect ? 'success' : 'error');

      if (isCorrect) {
        setTotalXpEarned((prev) => prev + currentExercise.xpReward);
        setTimeout(() => {
          advanceToNext();
        }, 1500);
      } else {
        setTimeout(() => {
          setFeedback('none');
          setSelectedOption(null);
        }, 1500);
      }
    } else {
      // For listen and speak, always mark as success (mock)
      setFeedback('success');
      setTotalXpEarned((prev) => prev + currentExercise.xpReward);
      setTimeout(() => {
        advanceToNext();
      }, 1500);
    }
  };

  const advanceToNext = () => {
    if (step < exercises.length - 1) {
      setStep(step + 1);
      setFeedback('none');
      setSelectedOption(null);
    } else {
      setIsComplete(true);
    }
  };

  const handleContinue = () => {
    if (feedback === 'success') {
      advanceToNext();
    }
  };

  // Completion screen
  if (isComplete) {
    return (
      <div className="min-h-screen bg-bg-warm flex flex-col">
        <div className="flex-1 flex items-center justify-center p-6">
          <div className="text-center max-w-md animate-fade-in">
            <div className="text-8xl mb-6">🎉</div>
            <h1 className="text-3xl font-bold text-text mb-2">
              {t.exercises?.lessonComplete || 'Lesson Complete!'}
            </h1>
            <p className="text-text-light mb-8">{lesson.title}</p>

            <div className="bg-white rounded-3xl p-6 shadow-sm mb-8">
              <div className="flex items-center justify-center gap-2 text-2xl font-bold text-primary">
                <span>⭐</span>
                <span>+{totalXpEarned} XP</span>
              </div>
              <p className="text-text-light text-sm mt-2">
                {t.exercises?.xpEarned || 'XP Earned'}
              </p>
            </div>

            <Button
              variant="primary"
              size="lg"
              onClick={() => navigate('/lessons')}
              className="w-full"
            >
              {t.exercises?.backToLessons || 'Back to Lessons'}
            </Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-bg-warm flex flex-col">
      {/* Header */}
      <header className="sticky top-0 z-40 bg-white border-b border-gray-100 px-4 py-4">
        <div className="max-w-2xl mx-auto flex items-center gap-4">
          <button
            onClick={() => navigate('/lessons')}
            className="p-2 rounded-full hover:bg-gray-100 transition-colors"
            aria-label="Back"
          >
            <span className="text-xl">←</span>
          </button>

          {/* Progress bar */}
          <div className="flex-1 h-3 bg-gray-200 rounded-full overflow-hidden">
            <div
              className="h-full bg-gradient-to-r from-primary to-accent rounded-full transition-all duration-500"
              style={{ width: `${progress}%` }}
            />
          </div>

          <span className="text-sm text-text-light font-medium">
            {step + 1}/{exercises.length}
          </span>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 flex flex-col justify-center p-6">
        <div className="max-w-2xl mx-auto w-full animate-fade-in" key={step}>
          <div className="text-center space-y-8">
            {/* Prompt */}
            <h2 className="text-2xl font-bold text-text">
              {t.exercises?.[currentExercise.prompt as keyof typeof t.exercises] ||
                currentExercise.prompt}
            </h2>

            {/* Listen Exercise */}
            {currentExercise.type === 'listen' && (
              <div className="flex flex-col items-center gap-6">
                <button
                  className="h-24 w-24 rounded-full bg-primary hover:bg-primary-dark text-white shadow-lg shadow-primary/30 flex items-center justify-center transition-all active:scale-95"
                  onClick={() => {
                    // Mock audio play - in real app, would play audio
                    const utterance = new SpeechSynthesisUtterance(currentExercise.content);
                    utterance.lang = 'en-US';
                    speechSynthesis.speak(utterance);
                  }}
                  aria-label="Play audio"
                >
                  <span className="text-4xl">🔊</span>
                </button>
                <p className="text-3xl font-serif italic text-text">{currentExercise.content}</p>
              </div>
            )}

            {/* Select Exercise */}
            {currentExercise.type === 'select' && currentExercise.options && (
              <div className="space-y-4">
                <p className="text-xl text-text-light mb-6">"{currentExercise.content}"</p>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {currentExercise.options.map((option, i) => {
                    const isSelected = selectedOption === i;
                    const isCorrect = i === currentExercise.correctAnswer;
                    const showResult = feedback !== 'none' && isSelected;

                    return (
                      <button
                        key={i}
                        className={`p-6 rounded-xl border-2 transition-all font-medium text-lg text-left ${
                          showResult
                            ? isCorrect
                              ? 'border-success bg-success-light'
                              : 'border-red-500 bg-red-50'
                            : isSelected
                              ? 'border-primary bg-primary-light'
                              : 'border-gray-200 hover:border-primary hover:bg-primary-light/50'
                        }`}
                        onClick={() => feedback === 'none' && handleCheck(i)}
                        disabled={feedback !== 'none'}
                      >
                        {option}
                      </button>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Speak Exercise */}
            {currentExercise.type === 'speak' && (
              <div className="flex flex-col items-center gap-6">
                <p className="text-3xl font-serif italic text-text mb-4">
                  {currentExercise.content}
                </p>
                <button
                  className="h-20 w-20 rounded-full bg-streak hover:opacity-90 text-white shadow-lg shadow-streak/30 flex items-center justify-center transition-all active:scale-95"
                  onClick={() => handleCheck()}
                  aria-label="Record"
                >
                  <span className="text-3xl">🎤</span>
                </button>
                <p className="text-sm text-text-light">
                  {t.exercises?.tapToSpeak || 'Tap to speak'}
                </p>
              </div>
            )}
          </div>
        </div>
      </main>

      {/* Footer / Feedback */}
      <footer
        className={`p-6 border-t transition-colors duration-300 ${
          feedback === 'success'
            ? 'bg-success-light border-success'
            : feedback === 'error'
              ? 'bg-red-50 border-red-200'
              : 'bg-white'
        }`}
      >
        <div className="max-w-2xl mx-auto flex items-center justify-between">
          {feedback === 'success' ? (
            <div className="flex items-center gap-3 text-success font-bold text-xl animate-fade-in">
              <div className="h-10 w-10 bg-white rounded-full flex items-center justify-center">
                <span className="text-success text-xl">✓</span>
              </div>
              {t.exercises?.excellent || 'Excellent!'}
            </div>
          ) : feedback === 'error' ? (
            <div className="flex items-center gap-3 text-red-600 font-bold text-xl animate-fade-in">
              <div className="h-10 w-10 bg-white rounded-full flex items-center justify-center">
                <span className="text-red-500 text-xl">✗</span>
              </div>
              {t.exercises?.tryAgain || 'Try again!'}
            </div>
          ) : (
            <div className="text-text-light hidden md:block">
              {currentExercise.type === 'listen' && '🎧'}
              {currentExercise.type === 'select' && '✅'}
              {currentExercise.type === 'speak' && '🎤'}
              <span className="ml-2 capitalize">{currentExercise.type}</span>
            </div>
          )}

          {currentExercise.type !== 'select' && (
            <Button
              variant={feedback === 'success' ? 'success' : 'primary'}
              size="lg"
              onClick={feedback === 'success' ? handleContinue : () => handleCheck()}
              className="min-w-[150px]"
            >
              {feedback === 'success'
                ? t.exercises?.continue || 'Continue'
                : t.exercises?.check || 'Check'}
            </Button>
          )}
        </div>
      </footer>
    </div>
  );
};
