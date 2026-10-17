<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter, RouterLink, RouterView } from 'vue-router'
import { gsap } from 'gsap'
import { clearAuth, getUser } from '../auth'
import api, { connection } from '../api'
import CommandPalette from './CommandPalette.vue'
import { startAmbient } from '../ambient'
import { motionOff, setMotionOff } from '../motionPreference'

const route = useRoute()
const router = useRouter()
const user = computed(() => getUser())
const root = ref(null)
const routeCurtain = ref(null)
const routeTransitioning = ref(false)
const routeTransitionLabel = ref('')
const paletteOpen = ref(false)
const refreshTick = ref(0)
const probing = ref(false)
const navList = ref(null)
const navRail = ref(null)
let animationContext
let mediaContext
let routeAnimationContext
let navObserver
let navRailFrame = 0

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

function transitionLabel(path) {
  if (path.startsWith('/projects/')) return '项目驾驶舱'
  return links.find((link) => (link.match ? path.startsWith(link.match) : path === link.to))?.label || '工作台'
}

async function logout() {
  try { await api.post('/auth/logout') } catch { /* ignore */ }
  clearAuth()
  router.push('/login')
}

/* ── 全局命令面板 ─────────────────────────────────────── */
function openPalette() {
  paletteOpen.value = true
}

function togglePalette() {
  paletteOpen.value = !paletteOpen.value
}

function onGlobalKey(event) {
  if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault()
    togglePalette()
  }
}

function refreshCurrent() {
  refreshTick.value += 1
}

/* ── 后端连接状态 ─────────────────────────────────────── */
const connectionState = computed(() => connection.value.state)
const connectionLabel = computed(() => {
  if (connectionState.value === 'down') return '后端不可达，点击重试'
  if (connectionState.value === 'ok') return '后端连接正常'
  return '尚未发起请求'
})

async function probeConnection() {
  if (probing.value) return
  probing.value = true
  try {
    await api.get('/dashboard')
  } catch { /* 状态由拦截器统一维护 */ }
  finally { probing.value = false }
}

watch(() => route.path, async (path, previousPath) => {
  if (!previousPath || path === '/login' || !root.value) return
  routeTransitionLabel.value = transitionLabel(route.path)
  routeTransitioning.value = true
  await nextTick()
  if (!routeCurtain.value || !root.value) {
    routeTransitioning.value = false
    return
  }

  routeAnimationContext?.revert()
  const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  routeAnimationContext = gsap.context(() => {
    const duration = reduced ? .01 : .4
    const timeline = gsap.timeline({ onComplete: () => { routeTransitioning.value = false } })
    timeline
      .fromTo('.route-transition__veil', { scale: .001 }, { scale: 1, duration: duration * .36, ease: 'power4.in' })
      .fromTo('.route-transition__rings', { rotation: -38, scale: .84, autoAlpha: .2 }, { rotation: 24, scale: 1, autoAlpha: 1, duration: duration * .55, ease: 'power3.out' }, 0)
      .fromTo('.route-transition__label', { y: 10, autoAlpha: 0 }, { y: 0, autoAlpha: 1, duration: duration * .24, ease: 'power2.out' }, duration * .24)
      .to('.route-transition__veil', { scale: .001, duration: duration * .46, ease: 'power4.inOut' }, duration * .45)
      .to('.route-transition__rings', { rotation: 88, scale: .92, autoAlpha: 0, duration: duration * .45, ease: 'power2.inOut' }, duration * .45)
      .to('.route-transition__label', { y: -7, autoAlpha: 0, duration: duration * .2, ease: 'power2.in' }, duration * .62)
  }, root.value)
})

/* ── 导航滑轨 ─────────────────────────────────────────── */

// 激活项的下划线由一条共享的轨来画，切换页面时从左滑到右，而不是原地闪一下。
function syncNavRail() {
  if (navRailFrame) return
  navRailFrame = requestAnimationFrame(() => {
    navRailFrame = 0
    const list = navList.value
    const rail = navRail.value
    if (!list || !rail) return
    const active = list.querySelector('.nav-item.active')
    if (!active) {
      rail.classList.remove('is-on')
      return
    }
    rail.style.left = `${active.offsetLeft}px`
    rail.style.width = `${active.offsetWidth}px`
    rail.classList.add('is-on')
  })
}

watch(() => route.path, async () => {
  await nextTick()
  syncNavRail()
})

let stopAmbient

// 开关写入偏好后整页重载，让所有动效模块按新状态重新初始化
const motionIsOff = motionOff()
function toggleMotion() {
  setMotionOff(!motionIsOff)
  window.location.reload()
}

onMounted(() => {
  window.addEventListener('keydown', onGlobalKey)
  stopAmbient = startAmbient()
  if (!root.value) return
  syncNavRail()
  // 字体载入 / 视口变化都会改变导航项宽度，轨要跟着重算。
  if (typeof ResizeObserver !== 'undefined' && navList.value) {
    navObserver = new ResizeObserver(syncNavRail)
    navObserver.observe(navList.value)
  }
  window.addEventListener('resize', syncNavRail, { passive: true })
  document.fonts?.ready?.then(syncNavRail).catch(() => {})
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
      gsap.from('.nav-item, .user', {
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
  stopAmbient?.()
  window.removeEventListener('keydown', onGlobalKey)
  window.removeEventListener('resize', syncNavRail)
  if (navRailFrame) cancelAnimationFrame(navRailFrame)
  navObserver?.disconnect()
  routeAnimationContext?.revert()
  mediaContext?.revert()
  animationContext?.revert()
})
</script>

<template>
  <div ref="root" class="layout">
    <header class="nav">
      <RouterLink to="/" class="brand" aria-label="DevFlow 总览">
        <span class="brand-mark">DF</span>
        <span class="brand-copy"><strong>DEVFLOW</strong></span>
      </RouterLink>
      <nav ref="navList" class="nav-list" aria-label="主导航">
        <span ref="navRail" class="nav-rail" aria-hidden="true" />
        <RouterLink v-for="(l, index) in visibleLinks" :key="l.to" :to="l.to" class="nav-item" :class="{ active: isActive(l) }">
          <span>{{ l.label }}</span>
        </RouterLink>
      </nav>
      <div class="nav-end">
        <button class="nav-search wb-press" type="button" aria-label="打开全局命令面板" @click="openPalette">
          <span class="nav-search-key" aria-hidden="true">⌘K</span>
          <span class="nav-search-text">搜索</span>
        </button>
        <button class="motion-toggle" type="button" :aria-pressed="!motionIsOff" :title="motionIsOff ? '动效已关闭，点击开启' : '动效已开启，点击关闭'" @click="toggleMotion">
          <span aria-hidden="true">{{ motionIsOff ? '静' : '动' }}</span>
        </button>
        <button
          class="conn"
          type="button"
          :class="connectionState"
          :title="connectionLabel"
          :aria-label="connectionLabel"
          :disabled="probing"
          @click="probeConnection"
        >
          <i aria-hidden="true" />
        </button>
        <div class="user">
          <span class="user-name">{{ user?.nickname || user?.username }}</span>
          <button class="wb-press" @click="logout">退出</button>
        </div>
      </div>
    </header>
    <div v-if="routeTransitioning" ref="routeCurtain" class="route-transition" aria-hidden="true">
      <div class="route-transition__veil" />
      <div class="route-transition__rings"><i /><i /></div>
      <div class="route-transition__label"><span>DEVFLOW / TRANSITION</span><strong>{{ routeTransitionLabel }}</strong></div>
    </div>
    <main>
      <RouterView v-slot="{ Component }">
        <Transition name="page" mode="out-in">
          <component :is="Component" :key="route.path + '#' + refreshTick" />
        </Transition>
      </RouterView>
    </main>
    <CommandPalette v-model="paletteOpen" @logout="logout" @refresh="refreshCurrent" />
  </div>
</template>

<style scoped>
.layout { --nav-h: 56px; min-height: 100vh; }
.route-transition { position: fixed; z-index: 200; inset: 0; overflow: hidden; pointer-events: auto; }
.route-transition__veil { position: absolute; top: 50%; left: 50%; width: 240vmax; height: 240vmax; border-radius: 50%; background: #020303; transform: translate3d(-50%, -50%, 0) scale(.001); will-change: transform; }
.route-transition__rings { position: absolute; top: 50%; left: 50%; width: min(62vw, 60vh, 560px); aspect-ratio: 1; border: 1px solid rgba(239, 239, 235, .72); border-radius: 50%; transform: translate3d(-50%, -50%, 0); box-shadow: 0 0 0 13px rgba(239, 239, 235, .035), inset 0 0 72px rgba(239, 239, 235, .035); will-change: transform, opacity; }
.route-transition__rings::before, .route-transition__rings::after, .route-transition__rings i { position: absolute; inset: 11px; border: 1px solid rgba(239, 239, 235, .19); border-radius: 47% 53% 51% 49%; content: ''; }
.route-transition__rings::before { inset: -17px; transform: rotate(24deg); }
.route-transition__rings::after { inset: 25px; border-color: rgba(239, 239, 235, .11); transform: rotate(-31deg); }
.route-transition__rings i:first-child { inset: 21%; border-color: rgba(239, 239, 235, .28); transform: rotate(42deg); }
.route-transition__rings i:last-child { inset: 35%; border-color: rgba(239, 239, 235, .13); transform: rotate(-18deg); }
.route-transition__label { position: absolute; right: clamp(22px, 5.2vw, 84px); bottom: clamp(28px, 7vh, 64px); display: flex; align-items: baseline; gap: 12px; color: rgba(239, 239, 235, .58); font-size: 12px; letter-spacing: .16em; }
.route-transition__label strong { color: var(--text); font-size: 12px; font-weight: 500; letter-spacing: .08em; }
.nav {
  position: fixed; inset: 0 0 auto; z-index: 100;
  height: 56px; display: grid; grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
  align-items: center; gap: 20px;
  padding: 0 clamp(12px, 1.6vw, 28px);
  pointer-events: none;
}
/* 滚动时页面标题会从固定导航下面穿过，给导航垫一层渐隐底色避免文字重叠 */
.nav::before {
  position: absolute; inset: 0 0 -14px; z-index: -1; content: ""; pointer-events: none;
  background: linear-gradient(to bottom, rgba(8, 8, 8, .94) 62%, rgba(8, 8, 8, 0));
  -webkit-backdrop-filter: blur(6px); backdrop-filter: blur(6px);
  -webkit-mask-image: linear-gradient(to bottom, #000 70%, transparent); mask-image: linear-gradient(to bottom, #000 70%, transparent);
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
.brand-copy strong { font-size: 12px; letter-spacing: .2em; }
.brand-copy small { color: var(--muted-light); font-size: 12px; letter-spacing: .15em; }
.nav-list {
  position: relative; justify-self: center; min-width: 0;
  display: flex; align-items: center; gap: clamp(15px, 2.1vw, 30px);
  max-width: 100%; overflow-x: auto; pointer-events: auto;
}
/* 激活指示条：由 syncNavRail 量出位置后滑动过去 */
.nav-rail {
  position: absolute; bottom: 0; left: 0; width: 0; height: 1px;
  background: var(--text);
  opacity: 0; pointer-events: none;
  transition: left .5s var(--ease), width .5s var(--ease), opacity .3s var(--ease);
}
.nav-rail.is-on { opacity: 1; }
.nav-item {
  position: relative; display: inline-flex; align-items: center; gap: 7px;
  padding: 7px 0; color: rgba(239, 239, 235, .58); text-decoration: none;
  white-space: nowrap; font-size: 12px; letter-spacing: .08em; transition: color .35s var(--ease);
}
.nav-item::after {
  position: absolute; right: 0; bottom: 0; left: 0; height: 1px; content: "";
  transform: scaleX(0); transform-origin: left;
  background: var(--text); transition: transform .4s var(--ease);
}
.nav-item:hover, .nav-item.active { color: var(--text); }
.nav-item:hover::after { transform: scaleX(1); background: rgba(239, 239, 235, .42); }
/* 激活态交给 .nav-rail，这里不再自己画一条，否则两条线会重叠。 */
.nav-item.active::after { transform: scaleX(0); }
.nav-index { color: rgba(239, 239, 235, .38); font-size: 12px; letter-spacing: .08em; }
.nav-end { display: flex; align-items: center; justify-self: end; gap: 14px; flex: 0 0 auto; pointer-events: auto; }
.nav-search {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 6px 12px 6px 10px;
  border: 1px solid rgba(238, 238, 234, .18); border-radius: 999px;
  color: var(--muted); background: transparent; cursor: pointer;
  font-size: 12px; letter-spacing: .08em; font-family: inherit;
  transition: color .25s var(--ease), border-color .25s var(--ease), background .25s var(--ease);
}
.nav-search:hover { color: var(--text); border-color: rgba(238, 238, 234, .5); background: rgba(255, 255, 255, .05); }
.nav-search-key {
  display: inline-grid; place-items: center; min-width: 26px; height: 18px; padding: 0 5px;
  border: 1px solid rgba(238, 238, 234, .22); border-radius: 5px;
  color: var(--muted-light); font-size: 12px; letter-spacing: .06em;
}
.motion-toggle {
  display: grid; place-items: center; width: 24px; height: 24px; padding: 0; cursor: pointer;
  border: 1px solid rgba(238, 238, 234, .22); border-radius: 50%; background: transparent;
  color: var(--muted); font-size: 12px; font-family: inherit;
  transition: color .25s var(--ease), border-color .25s var(--ease), transform .4s var(--ease);
}
.motion-toggle:hover { color: var(--text); border-color: rgba(238, 238, 234, .6); transform: rotate(90deg); }
.conn { position: relative; display: grid; width: 22px; height: 22px; place-items: center; border: 0; border-radius: 50%; background: transparent; cursor: pointer; }
.conn::after { position: absolute; inset: 0; border: 1px solid rgba(239, 239, 235, .5); border-radius: 50%; content: ""; opacity: 0; pointer-events: none; }
.conn.ok::after { animation: conn-ring 3.4s var(--ease) infinite; }
.conn i { display: block; width: 6px; height: 6px; border-radius: 50%; background: rgba(239, 239, 235, .3); transition: background .3s ease, box-shadow .3s ease, transform .25s var(--ease); }
.conn.ok i { background: #a9c295; box-shadow: 0 0 0 3px rgba(169, 194, 149, .14); }
.conn.down i { background: #d98b7d; box-shadow: 0 0 0 3px rgba(217, 139, 125, .18); animation: conn-pulse 2s ease-in-out infinite; }
.conn:disabled { cursor: progress; }
.conn:hover i { transform: scale(1.25); }
@keyframes conn-pulse { 0%, 100% { opacity: 1 } 50% { opacity: .35 } }
@keyframes conn-ring { 0% { opacity: .5; transform: scale(.45) } 62%, 100% { opacity: 0; transform: scale(1.7) } }
.user { display: flex; align-items: center; gap: 14px; color: var(--muted); font-size: 12px; letter-spacing: .08em; }
.user-name { max-width: 96px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.user button {
  padding: 4px 0; color: var(--muted); background: transparent; border: 0; border-bottom: 1px solid transparent;
  cursor: pointer; font-size: 12px; transition: color .25s var(--ease), border-color .25s var(--ease);
}
.user button:hover { color: var(--text); border-color: rgba(239, 239, 235, .55); }
main { padding-top: 0; }
@media (max-width: 760px) {
  .layout { --nav-h: 108px; }
  .nav {
    height: 108px;
    grid-template-columns: minmax(0, 1fr) auto;
    grid-template-rows: 40px 32px;
    align-content: center;
    gap: 8px 12px;
    padding: 8px 20px 10px;
  }
  .route-transition__rings { width: min(72vw, 54vh, 420px); }
  .route-transition__label { right: 20px; bottom: 24px; }
  .brand { grid-column: 1; grid-row: 1; }
  .brand-copy { display: flex; }
  .nav-end { grid-column: 2; grid-row: 1; gap: 10px; }
  .nav-search { padding: 5px 9px 5px 7px; }
  .nav-search-text { display: none; }
  .nav-list {
    grid-column: 1 / -1;
    grid-row: 2;
    justify-self: stretch;
    gap: clamp(12px, 4vw, 22px);
    padding-bottom: 3px;
    scrollbar-width: none;
  }
  .nav-list::-webkit-scrollbar { display: none; }
  .nav-item { flex: 0 0 auto; gap: 5px; font-size: 12px; }
}
@media (max-width: 440px) {
  .nav-index { display: none; }
  .user-name { display: none; }
}
</style>
