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
  position: fixed; inset: 0 0 auto; z-index: 100;
  height: 78px; display: flex; align-items: center; justify-content: space-between;
  padding: 0 clamp(22px, 5.2vw, 84px);
  pointer-events: none;
}
.brand {
  display: inline-flex; align-items: center; gap: 10px; flex: 0 0 auto;
  color: var(--text); text-decoration: none; pointer-events: auto;
}
.brand-mark {
  display: grid; place-items: center; width: 31px; height: 31px;
  border: 1px solid rgba(239, 239, 235, .64); border-radius: 50%;
  color: var(--text); font-family: var(--font-display); font-size: 13px; letter-spacing: -.04em;
}
.brand-copy { display: flex; flex-direction: column; gap: 1px; line-height: 1; }
.brand-copy strong { font-size: 11px; letter-spacing: .2em; }
.brand-copy small { color: var(--muted-light); font-size: 7px; letter-spacing: .15em; }
.nav-list {
  position: fixed; left: clamp(22px, 5.2vw, 84px); bottom: 27px; z-index: 101;
  display: flex; align-items: center; gap: clamp(15px, 2.1vw, 30px);
  max-width: calc(100vw - 44px); overflow-x: auto; pointer-events: auto;
}
.nav-item {
  position: relative; display: inline-flex; align-items: center; gap: 7px;
  padding: 7px 0; color: rgba(239, 239, 235, .58); text-decoration: none;
  white-space: nowrap; font-size: 10px; letter-spacing: .08em; transition: color .35s var(--ease);
}
.nav-item::after {
  position: absolute; right: 0; bottom: 0; left: 0; height: 1px; content: "";
  transform: scaleX(0); transform-origin: left;
  background: var(--text); transition: transform .4s var(--ease);
}
.nav-item:hover, .nav-item.active { color: var(--text); }
.nav-item.active::after { transform: scaleX(1); }
.nav-index { color: rgba(239, 239, 235, .38); font-size: 8px; letter-spacing: .08em; }
.nav-end { display: flex; align-items: center; gap: 18px; flex: 0 0 auto; pointer-events: auto; }
.system-status { display: inline-flex; align-items: center; gap: 7px; color: var(--muted-light); font-size: 8px; letter-spacing: .14em; }
.system-status i { width: 4px; height: 4px; border-radius: 50%; background: #d1d1cb; }
.user { display: flex; align-items: center; gap: 14px; color: var(--muted); font-size: 10px; letter-spacing: .08em; }
.user-name { max-width: 96px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.user button {
  padding: 4px 0; color: var(--muted); background: transparent; border: 0; border-bottom: 1px solid transparent;
  cursor: pointer; font-size: 10px; transition: color .25s var(--ease), border-color .25s var(--ease);
}
.user button:hover { color: var(--text); border-color: rgba(239, 239, 235, .55); }
main { padding-top: 0; }
@media (max-width: 760px) {
  .nav { height: 64px; padding: 0 20px; }
  .brand-copy, .system-status { display: none; }
  .nav-list { right: 20px; bottom: 18px; left: 20px; justify-content: space-between; gap: 12px; }
  .nav-item { gap: 5px; font-size: 9px; }
}
@media (max-width: 440px) {
  .nav-list { bottom: 14px; gap: 10px; }
  .nav-index { display: none; }
  .user-name { display: none; }
}
</style>
