import eslint from '@eslint/js';
import eslintConfigPrettier from 'eslint-config-prettier';
import tseslint from 'typescript-eslint';
import react from 'eslint-plugin-react';
import importPlugin from 'eslint-plugin-import';
// TODO: Does not work with eslint 9
// import reactHooks from 'eslint-plugin-react-hooks';

export default tseslint.config(eslint.configs.recommended, eslintConfigPrettier, tseslint.configs.recommended, react.configs.flat.recommended, {
  settings: {
    react: {
      version: 'detect',
    },
  },
  files: ['**/*.{ts,tsx}'],
  extends: [importPlugin.flatConfigs.recommended, importPlugin.flatConfigs.typescript],
  rules: {
    'react/react-in-jsx-scope': 'off',
    'react/jsx-uses-react': 'off',
    'react/prop-types': 'off',
  },
});
