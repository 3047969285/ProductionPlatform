<template>
  <div ref="root" class="home">
    <div class="bg" aria-hidden="true"><div class="orb o1" /><div class="orb o2" /></div>
    <section class="hero">
      <p class="tag"><i />00 / DELIVERY</p>
      <h1>交付总览</h1>
      <p class="desc">需求 / 开发 / 测试 / 部署</p>
    </section>
    <section v-if="!loading && !loadError" class="grid">
      <article v-for="c in cards" :key="c.label" class="card">
        <span class="num" :style="{ color: c.color }">{{ c.value }}</span>
        <span class="label">{{ c.label }}</span>
      </article>
    </section>
    <section v-else-if="loading" class="status-panel" aria-live="polite">
      <span class="status-mark" aria-hidden="true">···</span>
      <p>正在读取交付数据</p>
    </section>
    <section v-else class="status-panel status-error" role="alert">
      <span class="status-mark" aria-hidden="true">!</span>
      <div><h2>暂时无法读取总览</h2><p>检查网络或服务状态后重试；现有项目数据不会被修改。</p></div>
      <button type="button" class="retry-button" @click="loadDashboard">重新加载 <span aria-hidden="true">↗</span></button>
    </section>

    <section v-if="recentReqs.length" class="recent">
      <div class="recent-head"><h2>最近</h2><RouterLink to="/projects">查看全部 ↗</RouterLink></div>
      <ul class="list">
        <li v-for="r in recentReqs" :key="r.id">
          <span class="no">{{ r.reqNo }}</span>
          <span class="name">{{ r.title }}</span>
          <span class="badge" :class="badgeClass('req', r.status)">{{ reqStatus[r.status] }}</span>
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import api from '../api'
import { reqStatus, badgeClass } from '../constants'

gsap.registerPlugin(ScrollTrigger)

const root = ref(null)
const loading = ref(true)
const loadError = ref(false)
const data = ref(null)
const recentReqs = computed(() => data.value?.recentRequirements || [])
let animationContext
let mediaQuery
let cardTween
let recentTween

const cards = computed(() => {
  const d = data.value
  if (!d) return []
  return [
    { label: '项目', value: d.projects ?? 0, color: '#fff' },
    { label: '需求', value: d.requirements?.total ?? 0, color: 'var(--cyan)' },
    { label: '接口', value: d.apis ?? 0, color: 'var(--purple)' },
    { label: '测试中', value: d.tests?.running ?? 0, color: 'var(--cyan)' },
    { label: '运维待处理', value: d.ops?.open ?? 0, color: 'var(--pink)' },
    { label: '活跃团队', value: d.teams?.active ?? 0, color: 'var(--lime)' },
  ]
})

function setupAnimations() {
  if (!root.value) return

  animationContext = gsap.context(() => {
    mediaQuery = gsap.matchMedia()
    mediaQuery.add({ reduced: '(prefers-reduced-motion: reduce)' }, ({ conditions }) => {
      const reducedMotion = conditions.reduced
      const duration = reducedMotion ? 0 : undefined
      const intro = gsap.timeline({ defaults: { ease: 'power3.out' } })

      intro
        .from('.tag', { autoAlpha: 0, y: 12, duration: duration ?? 0.45 })
        .from('h1', { autoAlpha: 0, y: 28, duration: duration ?? 0.7, ease: 'power4.out' }, '-=0.2')
        .from('.desc', { autoAlpha: 0, y: 14, duration: duration ?? 0.5 }, '-=0.3')

      if (!reducedMotion) {
        gsap.to('.o1', { x: 24, y: 18, duration: 8, repeat: -1, yoyo: true, ease: 'sine.inOut' })
        gsap.to('.o2', { x: -18, y: -20, duration: 10, repeat: -1, yoyo: true, ease: 'sine.inOut' })
      }
    })
  }, root.value)
}

async function animateDashboardContent() {
  await nextTick()
  if (!root.value) return

  const cardsEl = root.value.querySelectorAll('.card')
  const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  cardTween?.kill()
  if (cardsEl.length) {
    cardTween = gsap.fromTo(
      cardsEl,
      reducedMotion ? { autoAlpha: 1, y: 0 } : { autoAlpha: 0, y: 22 },
      { autoAlpha: 1, y: 0, duration: reducedMotion ? 0 : 0.55, stagger: reducedMotion ? 0 : 0.07, ease: 'power3.out', overwrite: 'auto' },
    )
  }

  const recentEl = root.value.querySelector('.recent')
  recentTween?.kill()
  if (recentEl && !reducedMotion) {
    recentTween = gsap.timeline({
      scrollTrigger: { trigger: recentEl, start: 'top 86%', once: true },
    }).from(recentEl, { autoAlpha: 0, y: 26, duration: 0.7, ease: 'power3.out' })
    ScrollTrigger.refresh()
  }
}

watch(loading, (isLoading) => {
  if (!isLoading) animateDashboardContent()
})

async function loadDashboard() {
  loading.value = true
  loadError.value = false
  try { data.value = (await api.get('/dashboard')).data }
  catch { loadError.value = true }
  finally { loading.value = false }
}

onMounted(() => {
  setupAnimations()
  loadDashboard()
})

onUnmounted(() => {
  cardTween?.kill()
  recentTween?.kill()
  mediaQuery?.revert()
  animationContext?.revert()
})
</script>

<style scoped>
.home { max-width: 1700px; min-height: 100svh; padding: clamp(145px, 20vh, 205px) clamp(22px, 5.2vw, 84px) 112px; }
.bg { position: fixed; inset: 0; z-index: -1; pointer-events: none; }
.orb { position: absolute; border-radius: 50%; filter: blur(110px); opacity: .035; will-change: transform; }
.o1 { width: 360px; height: 360px; background: #fff; top: -8%; right: -5%; }
.o2 { width: 280px; height: 280px; background: #bdbdb7; bottom: 5%; left: -8%; }
.hero { position: relative; margin-bottom: clamp(45px, 9vh, 92px); padding-bottom: 0; border: 0; }
.hero::after { display: none; }
.tag { display: flex; align-items: center; gap: 9px; color: var(--muted); font-size: 8px; letter-spacing: .2em; margin-bottom: 19px; }
.tag i { width: 25px; height: 1px; background: var(--muted); }
h1 { font-family: var(--font-display); font-size: clamp(3.4rem, 8.2vw, 8.1rem); font-weight: 400; letter-spacing: -.09em; line-height: .88; margin-bottom: 22px; }
.desc { color: var(--muted-light); font-size: 9px; letter-spacing: .2em; }
.grid { display: grid; grid-template-columns: repeat(6, minmax(0, 1fr)); gap: 0; margin-bottom: clamp(48px, 10vh, 100px); border-top: 1px solid rgba(239,239,235,.24); border-bottom: 1px solid rgba(239,239,235,.15); background: transparent; }
.card { position: relative; min-width: 0; min-height: 142px; padding: 22px 18px 18px 0; background: transparent; border-right: 1px solid rgba(239,239,235,.12); will-change: transform, opacity; transition: transform .45s var(--ease), background .35s var(--ease); }
.card + .card { padding-left: 18px; }
.card:last-child { border-right: 0; }
.card::before { position: absolute; top: -1px; left: 0; width: 24px; height: 1px; background: rgba(239,239,235,.72); content: ""; }
.card:hover { z-index: 1; background: rgba(255,255,255,.025); transform: translateY(-5px); }
.num { display: block; font-family: var(--font-display); font-size: clamp(2rem, 4vw, 3.7rem); font-weight: 400; line-height: 1; letter-spacing: -.06em; }
.label { display: block; margin-top: 32px; color: var(--muted-light); font-size: 9px; letter-spacing: .11em; }
.recent { max-width: 1000px; margin-bottom: 32px; }
.recent-head { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 18px; }
.recent-head h2 { font-family: var(--font-display); font-size: clamp(1.5rem, 2.8vw, 2.2rem); font-weight: 400; letter-spacing: -.04em; }
.recent-head a { color: var(--muted); text-decoration: none; font-size: 9px; letter-spacing: .12em; transition: color .25s ease; }
.recent-head a:hover { color: var(--text); }
.list { overflow: hidden; border-top: 1px solid rgba(239,239,235,.18); background: transparent; }
.list li { display: grid; grid-template-columns: minmax(120px, .65fr) minmax(180px, 1.7fr) auto; gap: 18px; align-items: center; padding: 14px 0; border-bottom: 1px solid rgba(239,239,235,.12); font-size: 11px; }
.no { color: var(--muted-light); font-size: 8px; letter-spacing: .14em; }
.name { color: var(--text); }
.status-panel { display: flex; min-height: 150px; align-items: center; gap: 18px; padding: 26px 0; border-top: 1px solid rgba(239,239,235,.18); border-bottom: 1px solid rgba(239,239,235,.12); color: var(--muted-light); }
.status-mark { display: grid; width: 42px; height: 42px; flex: 0 0 auto; place-items: center; border: 1px solid rgba(239,239,235,.25); border-radius: 50%; color: var(--text); font-family: var(--font-display); font-size: 18px; }
.status-panel h2 { margin: 0 0 8px; color: var(--text); font-size: 15px; font-weight: 500; }
.status-panel p { margin: 0; color: var(--muted); font-size: 12px; line-height: 1.7; }
.retry-button { display: inline-flex; align-items: center; gap: 16px; margin-left: auto; padding: 11px 14px; border: 1px solid rgba(239,239,235,.28); border-radius: 999px; color: var(--text); background: transparent; cursor: pointer; font: inherit; font-size: 11px; transition: background .25s ease, border-color .25s ease; }
.retry-button:hover { border-color: rgba(239,239,235,.62); background: rgba(255,255,255,.05); }
@media (max-width: 900px) { .grid { grid-template-columns: repeat(3, minmax(0, 1fr)); } }
@media (max-width: 540px) {
  .home { padding-top: 145px; }
  .grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .card:nth-child(3) { border-top: 1px solid rgba(239,239,235,.12); }
  .card:nth-child(n+4) { border-top: 1px solid rgba(239,239,235,.12); }
  .card:nth-child(3), .card:nth-child(5) { padding-left: 0; }
  .card:nth-child(even) { padding-left: 18px; border-right: 0; }
  .list li { grid-template-columns: 1fr auto; }
  .list .no { display: none; }
  .status-panel { align-items: flex-start; flex-wrap: wrap; }
  .retry-button { margin-left: 60px; }
}
</style>
