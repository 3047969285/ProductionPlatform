<template>
  <div ref="root" class="home">
    <div class="bg" aria-hidden="true"><div class="orb o1" /><div class="orb o2" /></div>
    <section class="hero">
      <p class="tag">PDE DELIVERY</p>
      <h1>交付总览</h1>
      <p class="desc">从需求到发布，把每一步变成看得懂、做得到的下一步</p>
    </section>
    <section v-if="!loading" class="grid">
      <article v-for="c in cards" :key="c.label" class="card">
        <span class="num" :style="{ color: c.color }">{{ c.value }}</span>
        <span class="label">{{ c.label }}</span>
      </article>
    </section>
    <p v-else class="empty">加载中…</p>

    <section v-if="recentReqs.length" class="recent">
      <div class="recent-head"><h2>最近需求</h2><RouterLink to="/projects">进入项目 →</RouterLink></div>
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
import { ElMessage } from 'element-plus'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import api from '../api'
import { reqStatus, badgeClass } from '../constants'

const root = ref(null)
const loading = ref(true)
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

onMounted(async () => {
  setupAnimations()
  try { data.value = (await api.get('/dashboard')).data }
  catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
})

onUnmounted(() => {
  cardTween?.kill()
  recentTween?.kill()
  mediaQuery?.revert()
  animationContext?.revert()
})
</script>

<style scoped>
.home { position: relative; max-width: 960px; margin: 0 auto; padding: 40px clamp(20px, 5vw, 48px) 80px; }
.bg { position: fixed; inset: 0; z-index: -1; pointer-events: none; }
.orb { position: absolute; border-radius: 50%; filter: blur(90px); opacity: 0.14; will-change: transform; }
.o1 { width: 360px; height: 360px; background: var(--purple); top: -8%; right: -5%; }
.o2 { width: 280px; height: 280px; background: var(--cyan); bottom: 5%; left: -8%; }
.hero { margin-bottom: 36px; }
.tag { font-size: 12px; letter-spacing: 0.3em; color: var(--cyan); margin-bottom: 12px; }
h1 { font-family: var(--font-display); font-size: clamp(2.25rem, 6vw, 3.25rem); font-weight: 800; margin-bottom: 8px; }
.desc { color: var(--muted); font-size: 16px; }
.grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 14px; margin-bottom: 40px; }
.card { padding: 22px; background: var(--glass); border: 1px solid rgba(0,0,0,0.06); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); backdrop-filter: blur(16px) saturate(1.3); will-change: transform, opacity; transition: transform 0.35s var(--ease), box-shadow 0.35s var(--ease); }
.card:hover { transform: translateY(-5px); box-shadow: var(--shadow-md); }
.num { display: block; font-family: var(--font-display); font-size: 2rem; font-weight: 800; }
.label { font-size: 14px; color: var(--muted); }
.recent { margin-bottom: 32px; }
.recent-head { display: flex; justify-content: space-between; margin-bottom: 14px; }
.recent-head h2 { font-family: var(--font-display); font-size: 1.25rem; font-weight: 700; }
.recent-head a { font-size: 15px; color: var(--cyan); text-decoration: none; }
.list { list-style: none; background: var(--glass); border: 1px solid rgba(0,0,0,0.06); border-radius: var(--radius-lg); overflow: hidden; box-shadow: var(--shadow-sm); }
.list li { display: grid; grid-template-columns: 1fr 1.2fr auto; gap: 12px; padding: 16px 20px; border-bottom: 1px solid rgba(0,0,0,0.05); font-size: 16px; align-items: center; }
.list li:last-child { border-bottom: none; }
.no { font-family: var(--font-display); font-weight: 600; }
.name { color: rgba(0,0,0,0.55); }
.empty { color: var(--muted); text-align: center; padding: 40px; font-size: 16px; }
</style>
