import {defineConfig, loadEnv} from 'vite';
import react from '@vitejs/plugin-react';
import viteTsconfigPaths from 'vite-tsconfig-paths';
import eslintPlugin from '@nabla/vite-plugin-eslint';

export default defineConfig((configEnv) => {
  const env = {...process.env, ...loadEnv(configEnv.mode, process.cwd(), '')};
  return {
    base: env.APP_BASE_URL,
    plugins: [react(), viteTsconfigPaths(), eslintPlugin()],
    resolve: {
      mainFields: ['browser'],
    },
    server: {
      open: true,
      port: 3000,
      proxy: {
        '/lighter/api': {
          target: env.LIGHTER_DEV_PROXY_URL || 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
          ws: true,
        },
      },
    },
  };
});
