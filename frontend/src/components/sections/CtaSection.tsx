import { Link } from 'react-router-dom';
import { Button } from '../common/Button';
import { useTranslation } from '../../i18n';

export const CtaSection = () => {
  const { t } = useTranslation();

  return (
    <section className="px-6 lg:px-15 py-16 lg:py-20 text-center bg-white">
      <h2 className="text-3xl lg:text-[40px] font-bold text-text font-serif mb-4">
        {t.cta.title}
      </h2>
      <p className="text-text-light text-base mb-10">
        {t.cta.subtitle}
      </p>

      <div className="flex justify-center">
        <Link to="/signup">
          <Button size="lg">{t.cta.button}</Button>
        </Link>
      </div>
    </section>
  );
};
