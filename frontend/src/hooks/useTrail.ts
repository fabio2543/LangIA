import { useContext } from 'react';
import TrailContext, { type TrailContextValue } from '../context/TrailContext';

export const useTrail = (): TrailContextValue => {
  const context = useContext(TrailContext);
  if (!context) {
    throw new Error('useTrail must be used within a TrailProvider');
  }
  return context;
};
