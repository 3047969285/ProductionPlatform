import { createApp } from 'vue'
import { provideGlobalConfig } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn.mjs'
import './styles/global.css'
import App from './App.vue'
import router from './router'
import { vReveal } from './motion'
import { vCount, vInteract } from './interactions'

const app = createApp(App)

app.use(router)
app.directive('reveal', vReveal)
app.directive('count', vCount)
app.directive('interact', vInteract)
provideGlobalConfig({ locale: zhCn }, app, true)
app.mount('#app')
