import { useTranslation } from '../../i18n';

export const ValuesSection = () => {
  const { t } = useTranslation();

  const values = [
    {
      icon: '🤖',
      title: t.values.aiTitle,
      description: t.values.aiDesc,
    },
    {
      icon: '📱',
      title: t.values.multiplatformTitle,
      description: t.values.multiplatformDesc,
    },
    {
      icon: '📊',
      title: t.values.progressTitle,
      description: t.values.progressDesc,
    },
  ];

  return (
    <section className="px-6 lg:px-15 py-12 lg:py-15 text-center bg-white">
      <p className="text-primary text-xs font-semibold uppercase tracking-wider mb-2">
        {t.values.sectionLabel}
      </p>
      <h2 className="text-3xl lg:text-4xl font-bold text-text font-serif mb-12">
        {t.values.title}
      </h2>

      <div className="flex flex-col md:flex-row justify-center gap-8 lg:gap-12">
        {values.map((value) => (
          <div key={value.title} className="max-w-[280px] mx-auto md:mx-0 p-6 rounded-3xl card-hover">
            <div className="w-20 lg:w-[90px] h-20 lg:h-[90px] bg-bg rounded-3xl mx-auto mb-5 flex items-center justify-center text-3xl lg:text-4xl transition-transform group-hover:scale-110">
              {value.icon}
            </div>
            <h3 className="text-lg font-semibold text-text mb-2.5">
              {value.title}
            </h3>
            <p className="text-text-light text-sm leading-relaxed">
              {value.description}
            </p>
          </div>
        ))}
      </div>
    </section>
  );
};
