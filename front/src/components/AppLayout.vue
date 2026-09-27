<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter, RouterLink, RouterView } from 'vue-router'
import { gsap } from 'gsap'
import { clearAuth, getUser } from '../auth'
import api from '../api'

const route = useRoute()
const router = useRouter()
const user = computed(() => getUser())
const root = ref(null)
let animationContext
let mediaContext

const links = [
  { to: '/', label: '总览' },
  { to: '/projects', label: '项目', match: '/projects' },
  { to: '/teams', label: '团队' },
  { to: '/reports', label: '报表' },
  { to: '/users', label: '用户', admin: true },
]

const visibleLinks = computed(() => links.filter((l) => !l.admin || user.value?.role === 'admin'))

function isActive(link) {
  if (link.match) return route.path.startsWith(link.match)
  return route.path === link.to
}

async function logout() {
  try { await api.post('/auth/logout') } catch { /* ignore */ }
  clearAuth()
  router.push('/login')
}

onMounted(() => {
  if (!root.value) return
  animationContext = gsap.context(() => {
    mediaContext = gsap.matchMedia()
    mediaContext.add({ reduced: '(prefers-reduced-motion: reduce)' }, ({ conditions }) => {
      const reduced = conditions?.reduced
      gsap.from('.nav', {
        autoAlpha: 0,
        y: reduced ? 0 : -10,
        duration: reduced ? 0 : .55,
        ease: 'power3.out',
        clearProps: 'all',
      })
      gsap.from('.nav-item, .system-status, .user', {
        autoAlpha: 0,
        y: reduced ? 0 : 6,
        duration: reduced ? 0 : .4,
        stagger: reduced ? 0 : .045,
        delay: reduced ? 0 : .12,
        ease: 'power3.out',
        clearProps: 'all',
      })
    })
  }, root.value)
})

onUnmounted(() => {
  mediaContext?.revert()
  animationContext?.revert()
})
</script>

<template>
  <div ref="root" class="layout">
    <header class="nav">
      <RouterLink to="/" class="brand" aria-label="DevFlow 总览">
        <span class="brand-mark">DF</span>
        <span class="brand-copy"><strong>DEVFLOW</strong><small>PDE / DELIVERY</small></span>
      </RouterLink>
      <nav class="nav-list" aria-label="主导航">
        <RouterLink v-for="(l, index) in visibleLinks" :key="l.to" :to="l.to" class="nav-item" :class="{ active: isActive(l) }">
          <span class="nav-index">{{ String(index + 1).padStart(2, '0') }}</span>
          <span>{{ l.label }}</span>
        </RouterLink>
      </nav>
      <div class="nav-end">
        <span class="system-status"><i /> LIVE</span>
        <div class="user">
          <span class="user-name">{{ user?.nickname || user?.username }}</span>
          <button @click="logout">退出</button>
        </div>
      </div>
    </header>
    <main>
      <RouterView v-slot="{ Component }">
        <Transition name="page" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </Transition>
      </RouterView>
    </main>
  </div>
</template>

<style scoped>
.layout { min-height: 100vh; }
.nav {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  height: var(--nav-h); display: flex; align-items: center; gap: clamp(18px, 3vw, 44px);
  padding: 0 clamp(18px, 4vw, 64px);
  background: rgba(14, 17, 16, .82);
  backdrop-filter: blur(22px) saturate(1.2);
  -webkit-backdrop-filter: blur(22px) saturate(1.2);
  border-bottom: 1px solid var(--border);
}
.nav::after {
  position: absolute; right: clamp(18px, 4vw, 64px); bottom: -1px; left: clamp(18px, 4vw, 64px);
  height: 1px; content: ""; opacity: .5;
  background: linear-gradient(90deg, transparent, var(--accent), transparent);
}
.brand {
  display: inline-flex; align-items: center; gap: 10px; flex: 0 0 auto;
  color: var(--text); text-decoration: none;
}
.brand-mark {
  display: grid; place-items: center; width: 31px; height: 31px;
  border: 1px solid var(--accent); color: var(--accent); font-size: 10px; letter-spacing: .08em;
}
.brand-copy { display: flex; flex-direction: column; gap: 1px; line-height: 1; }
.brand-copy strong { font-size: 13px; letter-spacing: .18em; }
.brand-copy small { color: var(--muted-light); font-size: 8px; letter-spacing: .14em; }
.nav-list {
  display: flex; align-items: stretch; align-self: stretch; gap: clamp(12px, 2vw, 30px); flex: 1; overflow-x: auto;
}
.nav-item {
  position: relative; display: inline-flex; align-items: center; gap: 7px;
  color: var(--muted); text-decoration: none; white-space: nowrap; font-size: 13px; transition: color .25s var(--ease);
}
.nav-item::after {
  position: absolute; right: 0; bottom: 0; left: 0; height: 2px; content: ""; transform: scaleX(0); transform-origin: left;
  background: var(--accent); transition: transform .35s var(--ease);
}
.nav-item:hover, .nav-item.active { color: var(--text); }
.nav-item.active { color: var(--accent); }
.nav-item.active::after { transform: scaleX(1); }
.nav-index { color: var(--muted-light); font-size: 9px; letter-spacing: .08em; }
.nav-item.active .nav-index { color: var(--accent-deep); }
.nav-end { display: flex; align-items: center; gap: clamp(12px, 2vw, 26px); flex: 0 0 auto; }
.system-status { display: inline-flex; align-items: center; gap: 7px; color: var(--muted-light); font-size: 10px; letter-spacing: .12em; }
.system-status i { width: 5px; height: 5px; border-radius: 50%; background: var(--accent); box-shadow: 0 0 0 4px rgba(203, 210, 118, .1); }
.user { display: flex; align-items: center; gap: 12px; color: var(--muted); font-size: 12px; }
.user-name { max-width: 96px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.user button {
  padding: 4px 0; color: var(--muted); background: transparent; border: 0; border-bottom: 1px solid transparent;
  cursor: pointer; font-size: 12px; transition: color .2s var(--ease), border-color .2s var(--ease);
}
.user button:hover { color: var(--accent); border-color: var(--accent); }
main { padding-top: var(--nav-h); }
@media (max-width: 860px) {
  .nav { gap: 18px; }
  .brand-copy, .system-status { display: none; }
  .nav-list { gap: 18px; }
}
@media (max-width: 560px) {
  .nav { padding: 0 14px; gap: 14px; }
  .nav-list { gap: 14px; }
  .nav-index { display: none; }
  .user-name { display: none; }
}
</style>
