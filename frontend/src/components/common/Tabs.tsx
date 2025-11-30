import { type ReactNode } from 'react';
import { cn } from '../../utils/cn';

interface Tab {
  id: string;
  label: string;
  icon?: ReactNode;
  disabled?: boolean;
}

interface TabsProps {
  tabs: Tab[];
  activeTab: string;
  onChange: (tabId: string) => void;
  variant?: 'default' | 'pills' | 'underline';
  fullWidth?: boolean;
  className?: string;
}

export const Tabs = ({
  tabs,
  activeTab,
  onChange,
  variant = 'default',
  fullWidth = false,
  className,
}: TabsProps) => {
  const variantStyles = {
    default: {
      container: 'bg-gray-100 rounded-full p-1',
      tab: 'rounded-full px-4 py-2',
      active: 'bg-white shadow-sm text-primary',
      inactive: 'text-text-light hover:text-text',
    },
    pills: {
      container: 'gap-2',
      tab: 'rounded-full px-5 py-2.5 border',
      active: 'bg-primary text-white border-primary',
      inactive: 'bg-white text-text border-gray-200 hover:border-primary/50',
    },
    underline: {
      container: 'border-b border-gray-200 gap-0',
      tab: 'px-4 py-3 border-b-2 -mb-px',
      active: 'text-primary border-primary',
      inactive: 'text-text-light border-transparent hover:text-text hover:border-gray-300',
    },
  };

  const styles = variantStyles[variant];

  return (
    <div
      className={cn(
        'flex',
        styles.container,
        fullWidth && 'w-full',
        className
      )}
      role="tablist"
    >
      {tabs.map((tab) => (
        <button
          key={tab.id}
          type="button"
          role="tab"
          aria-selected={activeTab === tab.id}
          aria-controls={`tabpanel-${tab.id}`}
          disabled={tab.disabled}
          onClick={() => !tab.disabled && onChange(tab.id)}
          className={cn(
            'flex items-center justify-center gap-2 font-medium transition-all duration-200',
            styles.tab,
            activeTab === tab.id ? styles.active : styles.inactive,
            fullWidth && 'flex-1',
            tab.disabled && 'opacity-50 cursor-not-allowed'
          )}
        >
          {tab.icon && <span className="flex-shrink-0">{tab.icon}</span>}
          <span>{tab.label}</span>
        </button>
      ))}
    </div>
  );
};

interface TabPanelProps {
  id: string;
  activeTab: string;
  children: ReactNode;
  className?: string;
}

export const TabPanel = ({
  id,
  activeTab,
  children,
  className,
}: TabPanelProps) => {
  if (activeTab !== id) return null;

  return (
    <div
      id={`tabpanel-${id}`}
      role="tabpanel"
      aria-labelledby={id}
      className={className}
    >
      {children}
    </div>
  );
};
