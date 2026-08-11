import { defineConfig } from 'vite'

export default defineConfig({
  root: 'client',
  envDir: '..',
  server: {
    port: 8081,
    // Browser talks to Vite :8081; /ws is forwarded to Node signaling on :8080.
    proxy: {
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true
      }
    }
  },
  preview: {
    port: 8081
  },
  build: {
    outDir: '../build',
    emptyOutDir: true
  }
})
