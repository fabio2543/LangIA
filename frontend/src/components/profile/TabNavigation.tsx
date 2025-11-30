import { type ReactNode } from 'react';
import { cn } from '../../utils/cn';

interface Tab {
  id: string;
  label: string;
  icon?: ReactNode;
}

interface TabNavigationProps {
  tabs: Tab[];
  activeTab: string;
  onChange: (tabId: string) => void;
}

export const TabNavigation = ({ tabs, activeTab, onChange }: TabNavigationProps) => {
  return (
    <div className="overflow-x-auto">
      <nav className="flex gap-2 min-w-max" role="tablist">
        {tabs.map((tab) => {
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              type="button"
              role="tab"
              aria-selected={isActive}
              onClick={() => onChange(tab.id)}
              className={cn(
                'flex items-center gap-2 px-4 sm:px-6 py-3 rounded-full font-medium transition-all duration-200',
                'whitespace-nowrap text-sm sm:text-base',
                isActive
                  ? 'bg-primary text-white shadow-primary'
                  : 'bg-white text-text-light hover:text-text hover:bg-gray-50 shadow-card'
              )}
            >
              {tab.icon && <span className="text-lg">{tab.icon}</span>}
              <span className="hidden sm:inline">{tab.label}</span>
            </button>
          );
        })}
      </nav>
    </div>
  );
};
