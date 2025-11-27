import { useState } from 'react';
import { Button } from '../common/Button';
import { useTranslation } from '../../i18n';

export const CtaSection = () => {
  const { t } = useTranslation();
  const [email, setEmail] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // TODO: Handle email submission
    console.log('Email submitted:', email);
    setEmail('');
  };

  return (
    <section className="px-6 lg:px-15 py-16 lg:py-20 text-center bg-white">
      <h2 className="text-3xl lg:text-[40px] font-bold text-text font-serif mb-4">
        {t.cta.title}
      </h2>
      <p className="text-text-light text-base mb-10">
        {t.cta.subtitle}
      </p>

      <form
        onSubmit={handleSubmit}
        className="flex flex-col sm:flex-row justify-center gap-3 max-w-lg mx-auto"
      >
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder={t.cta.placeholder}
          required
          className="flex-1 px-6 py-4 rounded-full border border-gray-200 text-base outline-none focus:border-primary transition-colors"
        />
        <Button type="submit" size="lg">
          {t.cta.button}
        </Button>
      </form>
    </section>
  );
};
