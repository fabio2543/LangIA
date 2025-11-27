import { useTranslation } from '../../i18n';

export const BenefitsBar = () => {
  const { t } = useTranslation();

  const benefits = [
    { icon: '📊', text: t.benefits.allLevels },
    { icon: '🤖', text: t.benefits.aiFeedback },
    { icon: '🌍', text: t.benefits.available247 },
  ];

  return (
    <section className="flex flex-col md:flex-row justify-center items-center gap-6 md:gap-20 px-6 lg:px-15 py-6 bg-primary">
      {benefits.map((benefit) => (
        <div
          key={benefit.text}
          className="flex items-center gap-3"
        >
          <span className="text-2xl">{benefit.icon}</span>
          <span className="text-white font-medium text-sm lg:text-base">
            {benefit.text}
          </span>
        </div>
      ))}
    </section>
  );
};
