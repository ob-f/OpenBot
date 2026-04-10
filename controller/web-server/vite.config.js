import { defineConfig } from 'vite'

export default defineConfig({
  root: 'client',
  envDir: '..',
  server: {
    port: 8081
  },
  preview: {
    port: 8081
  },
  build: {
    outDir: '../build',
    emptyOutDir: true
  }
})
