import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import 'element-plus/dist/index.css'
import './styles/global.css'
import App from './App.vue'
import router from './router'

gsap.registerPlugin(ScrollTrigger)

createApp(App).use(router).use(ElementPlus).mount('#app')
