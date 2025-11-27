// .eslintrc.js
module.exports = {
    parser: '@typescript-eslint/parser',
    extends: [
        'eslint:recommended',
        'plugin:react/recommended',
        'plugin:react-hooks/recommended',
        'plugin:@typescript-eslint/recommended',
        'plugin:tailwindcss/recommended',
        'prettier',
    ],
    plugins: ['react', 'react-hooks', '@typescript-eslint', 'tailwindcss'],
    rules: {
        'react/react-in-jsx-scope': 'off',
        'react/prop-types': 'off',
        '@typescript-eslint/no-unused-vars': 'error',
        '@typescript-eslint/no-explicit-any': 'error',
        'tailwindcss/classnames-order': 'warn',
        'tailwindcss/no-custom-classname': 'off',
    },
    settings: {
        react: { version: 'detect' },
    },
};
