import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    Components({
      dts: false,
      resolvers: [ElementPlusResolver({ directives: true })],
    }),
  ],
  base: process.env.VITE_BASE_PATH || '/',
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
  server: {
    port: 5173,
    proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } },
  },
  build: {
    // 依赖包（@vueuse/core 等）里的 #__PURE__ 注释位置警告不影响产物，忽略避免 CI 失败
    rolldownOptions: {
      onLog(level, log, handler) {
        if (log.code === 'INVALID_ANNOTATION') return
        handler(level, log)
      },
    },
  },
})
