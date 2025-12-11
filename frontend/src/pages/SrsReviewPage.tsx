import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { SrsReviewSession } from '../components/srs';

export const SrsReviewPage = () => {
  const { user, isLoading } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const languageCode = searchParams.get('language') || 'en';

  useEffect(() => {
    if (!user && !isLoading) {
      navigate('/login');
    }
  }, [user, isLoading, navigate]);

  if (isLoading || !user) {
    return (
      <div className="min-h-screen bg-bg-warm flex items-center justify-center">
        <div className="text-center animate-in fade-in duration-500">
          <div className="text-4xl mb-4 animate-pulse">📚</div>
          <p className="text-text-light">Carregando...</p>
        </div>
      </div>
    );
  }

  const handleComplete = (_stats: { reviewed: number; correct: number }) => {
    // Poderia mostrar um modal ou redirecionar com os stats
    navigate('/dashboard');
  };

  const handleCancel = () => {
    navigate('/dashboard');
  };

  return (
    <div className="min-h-screen bg-bg-warm">
      <div className="max-w-4xl mx-auto px-4 py-6 md:py-10">
        <SrsReviewSession
          languageCode={languageCode}
          onComplete={handleComplete}
          onCancel={handleCancel}
          className="min-h-[calc(100vh-120px)]"
        />
      </div>
    </div>
  );
};
