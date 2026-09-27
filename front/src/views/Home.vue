<template>
  <div ref="root" class="home">
    <div class="bg" aria-hidden="true"><div class="orb o1" /><div class="orb o2" /></div>
    <section class="hero">
      <p class="tag"><i />00 / DELIVERY</p>
      <h1>交付总览</h1>
      <p class="desc">需求 / 开发 / 测试 / 部署</p>
    </section>
    <section v-if="!loading" class="grid">
      <article v-for="c in cards" :key="c.label" class="card">
        <span class="num" :style="{ color: c.color }">{{ c.value }}</span>
        <span class="label">{{ c.label }}</span>
      </article>
    </section>
    <p v-else class="empty">加载中…</p>

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
.home { position: relative; max-width: 1500px; margin: 0 auto; padding: 58px clamp(18px, 3.4vw, 48px) 80px; }
.bg { position: fixed; inset: 0; z-index: -1; pointer-events: none; }
.orb { position: absolute; border-radius: 50%; filter: blur(90px); opacity: .1; will-change: transform; }
.o1 { width: 360px; height: 360px; background: var(--accent); top: -8%; right: -5%; }
.o2 { width: 280px; height: 280px; background: var(--cyan); bottom: 5%; left: -8%; }
.hero { position: relative; margin-bottom: 58px; padding-bottom: 28px; border-bottom: 1px solid var(--border); }
.hero::after { position: absolute; right: 0; bottom: -1px; width: 84px; height: 1px; background: var(--accent); content: ""; }
.tag { display: flex; align-items: center; gap: 9px; color: var(--accent); font-size: 10px; letter-spacing: .2em; margin-bottom: 22px; }
.tag i { width: 28px; height: 1px; background: var(--accent); }
h1 { font-family: var(--font-display); font-size: clamp(3.4rem, 8vw, 6.7rem); font-weight: 400; letter-spacing: -.08em; line-height: .9; margin-bottom: 18px; }
.desc { color: var(--muted); font-size: 12px; letter-spacing: .16em; }
.grid { display: grid; grid-template-columns: repeat(6, minmax(110px, 1fr)); gap: 1px; margin-bottom: 58px; border: 1px solid var(--border); background: var(--border); }
.card { position: relative; min-height: 142px; padding: 20px; background: rgba(23, 27, 25, .84); will-change: transform, opacity; transition: transform .35s var(--ease), background .35s var(--ease); }
.card::before { position: absolute; top: 0; left: 20px; width: 16px; height: 1px; background: var(--accent); content: ""; opacity: .65; }
.card:hover { z-index: 1; background: rgba(39, 44, 36, .95); transform: translateY(-5px); }
.num { display: block; font-family: var(--font-display); font-size: 2.5rem; font-weight: 400; line-height: 1; }
.label { display: block; margin-top: 33px; color: var(--muted); font-size: 11px; letter-spacing: .1em; }
.recent { margin-bottom: 32px; }
.recent-head { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 14px; }
.recent-head h2 { font-family: var(--font-display); font-size: 1.7rem; font-weight: 400; }
.recent-head a { color: var(--accent); text-decoration: none; font-size: 12px; letter-spacing: .08em; }
.list { background: rgba(23, 27, 25, .78); border: 1px solid var(--border); overflow: hidden; box-shadow: var(--shadow-sm); }
.list li { display: grid; grid-template-columns: 1fr 1.2fr auto; gap: 12px; padding: 15px 20px; border-bottom: 1px solid var(--border); font-size: 13px; align-items: center; }
.list li:last-child { border-bottom: none; }
.no { color: var(--muted-light); font-size: 10px; letter-spacing: .12em; }
.name { color: var(--text); }
.empty { color: var(--muted); text-align: center; padding: 40px; font-size: 13px; }
@media (max-width: 900px) { .grid { grid-template-columns: repeat(3, minmax(110px, 1fr)); } }
@media (max-width: 540px) { .home { padding-top: 42px; } .grid { grid-template-columns: repeat(2, minmax(110px, 1fr)); } .list li { grid-template-columns: 1fr auto; } .list .no { display: none; } }
</style>
