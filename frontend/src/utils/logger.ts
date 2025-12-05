/**
 * Logger utilitário que só exibe logs em ambiente de desenvolvimento.
 * Em produção, todos os logs são silenciados para evitar exposição de informações.
 *
 * Uso:
 * import { logger } from '../utils/logger';
 * logger.log('mensagem');
 * logger.error('erro', error);
 */

const isDev = import.meta.env.DEV;

type LogArgs = Parameters<typeof console.log>;

export const logger = {
  log: (...args: LogArgs): void => {
    if (isDev) {
      console.log(...args);
    }
  },

  info: (...args: LogArgs): void => {
    if (isDev) {
      console.info(...args);
    }
  },

  warn: (...args: LogArgs): void => {
    if (isDev) {
      console.warn(...args);
    }
  },

  error: (...args: LogArgs): void => {
    if (isDev) {
      console.error(...args);
    }
  },

  debug: (...args: LogArgs): void => {
    if (isDev) {
      console.debug(...args);
    }
  },
};

export default logger;
