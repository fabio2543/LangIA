import { useTranslation } from '../../i18n';

export const TestimonialSection = () => {
  const { t } = useTranslation();

  return (
    <section className="px-6 lg:px-15 py-16 lg:py-20 bg-white">
      <p className="text-text-light text-xs font-semibold uppercase tracking-wider mb-2">
        {t.testimonial.sectionLabel}
      </p>
      <h2 className="text-3xl lg:text-4xl font-bold text-text font-serif mb-12">
        {t.testimonial.title}
      </h2>

      <div className="flex flex-col lg:flex-row gap-10 lg:gap-15 items-center">
        {/* Photo */}
        <div className="w-full max-w-[300px] lg:max-w-[350px] h-[350px] lg:h-[400px] bg-gradient-to-br from-primary-light to-white rounded-3xl flex items-center justify-center text-8xl lg:text-[100px] flex-shrink-0">
          🧑‍💼
        </div>

        {/* Content */}
        <div>
          <h3 className="text-2xl lg:text-[28px] font-semibold text-text mb-2">
            Martin Watson
          </h3>
          <div className="flex items-center gap-2 mb-6">
            <span>🇪🇸</span>
            <span className="text-text-light text-sm">
              B2 {t.testimonial.course}
            </span>
          </div>
          <p className="text-lg lg:text-[22px] text-gray-700 leading-relaxed max-w-lg font-serif italic">
            {t.testimonial.quote}
          </p>

          {/* Navigation Dots */}
          <div className="flex gap-2 mt-8">
            {[0, 1, 2, 3].map((i) => (
              <div
                key={i}
                className={`w-2.5 h-2.5 rounded-full ${
                  i === 1 ? 'bg-primary' : 'bg-gray-200'
                }`}
              />
            ))}
          </div>
        </div>
      </div>
    </section>
  );
};
