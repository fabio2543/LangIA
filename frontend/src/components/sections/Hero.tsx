import { Button } from '../common/Button';
import { Avatar } from '../common/Avatar';
import { useTranslation } from '../../i18n';

export const Hero = () => {
  const { t } = useTranslation();

  return (
    <section className="flex flex-col lg:flex-row items-center gap-8 lg:gap-0 px-6 lg:px-15 py-12 lg:py-15 bg-gradient-to-br from-bg to-bg-warm relative overflow-hidden">
      {/* Left - Text */}
      <div className="flex-1 max-w-xl">
        <h1 className="text-4xl lg:text-[52px] font-bold leading-tight text-text mb-5 font-serif italic">
          {t.hero.title}{' '}
          <span className="text-primary">{t.hero.titleHighlight}</span>{' '}
          {t.hero.titleEnd}
        </h1>
        <p className="text-text-light text-base lg:text-lg leading-relaxed mb-8">
          {t.hero.subtitle}
        </p>
        <Button size="lg">{t.hero.cta}</Button>
      </div>

      {/* Right - Visual */}
      <div className="flex-1 relative flex justify-center min-h-[400px]">
        {/* Decorative Blob */}
        <div
          className="absolute w-[320px] lg:w-[380px] h-[320px] lg:h-[380px] bg-gradient-to-br from-primary-light to-accent/30 z-0"
          style={{ borderRadius: '60% 40% 30% 70% / 60% 30% 70% 40%' }}
        />

        {/* Main Image */}
        <Avatar
          fallback="👩‍🎓"
          alt="Student learning languages"
          size="xl"
          className="z-10"
        />

        {/* Badge: Courses */}
        <div className="absolute top-6 lg:top-8 right-4 lg:right-15 bg-white p-3.5 lg:p-4 rounded-xl shadow-badge z-20">
          <div className="text-primary font-bold text-base lg:text-lg">
            {t.hero.coursesCount}
          </div>
          <div className="text-text-light text-xs">{t.hero.coursesLabel}</div>
        </div>

        {/* Badge: Students */}
        <div className="absolute bottom-10 lg:bottom-12 right-2 lg:right-10 bg-white p-3.5 lg:p-4 rounded-xl shadow-badge z-20 flex items-center gap-2.5">
          <div className="flex">
            {['😊', '😄', '🙂', '😃'].map((emoji, i) => (
              <div
                key={emoji}
                className="w-7 lg:w-8 h-7 lg:h-8 bg-primary-light rounded-full flex items-center justify-center border-2 border-white text-sm"
                style={{ marginLeft: i > 0 ? '-8px' : '0' }}
              >
                {emoji}
              </div>
            ))}
          </div>
          <div>
            <div className="font-semibold text-text text-sm">
              {t.hero.studentsCount}
            </div>
            <div className="text-[10px] text-text-light">
              {t.hero.studentsLabel}
            </div>
          </div>
        </div>

        {/* Badge: Testimonial Mini */}
        <div className="absolute top-16 lg:top-20 left-0 lg:left-5 bg-white p-3 rounded-xl shadow-badge z-20 flex items-center gap-2.5">
          <div className="w-9 lg:w-10 h-9 lg:h-10 bg-primary-light rounded-full flex items-center justify-center text-lg">
            👩
          </div>
          <div>
            <div className="font-semibold text-xs lg:text-sm text-text">
              Jane Cooper
            </div>
            <div className="text-[10px] lg:text-xs text-text-light">
              I loved the Italian course!
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
