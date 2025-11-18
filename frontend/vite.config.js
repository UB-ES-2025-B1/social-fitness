import { defineConfig } from 'vite'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],
  test: {
    environment: 'jsdom',
    setupFiles: './src/setupTests.js',
    globals: true,
    css: true, // permite importar CSS en tests
    fileParallelism: false, // Run test files sequentially
    maxConcurrency: 1, // Run tests sequentially to avoid jsdom worker thread issues
  },
})
