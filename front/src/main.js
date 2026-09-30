import { createApp } from 'vue'
import { provideGlobalConfig } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn.mjs'
import './styles/global.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(router)
provideGlobalConfig({ locale: zhCn }, app, true)
app.mount('#app')
