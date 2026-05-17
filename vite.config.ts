import { defineConfig } from 'vite';

export default defineConfig({
  root: 'src/main/webapp',
  server: {
    port: 5173,
    proxy: {
      '/auth': 'http://localhost:8200',
      '/health': 'http://localhost:8200',
      '/oauth2': 'http://localhost:8200',
      '/login': 'http://localhost:8200',
      '/api': 'http://localhost:8200',
    },
  },
  build: {
    outDir: '../../../target/classes/static',
    emptyOutDir: true,
  },
});
