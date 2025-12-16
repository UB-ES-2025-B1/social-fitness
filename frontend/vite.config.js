import { defineConfig } from 'vite'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],
  test: {
    environment: 'happy-dom',
    setupFiles: './src/setupTests.js',
    globals: true,
    css: true, // permite importar CSS en tests
    coverage: {
      reporter: ['text', 'html', 'json-summary', 'lcov'],
      include: ['src/**/*.{js,jsx}'],
      exclude: [
        'src/setupTests.js',
        'src/**/__tests__/**',
        'src/**/*.test.{js,jsx}',
      ],
    },
  },
})
