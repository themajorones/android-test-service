import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

export default defineConfig({
  root: 'src/main/webapp',
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/auth': 'http://localhost:7000',
      '/health': 'http://localhost:7000',
      '/oauth2': 'http://localhost:7000',
      '/login': 'http://localhost:7000',
    },
  },
  build: {
    outDir: '../../../target/classes/static',
    emptyOutDir: true,
  },
});
