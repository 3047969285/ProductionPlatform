<template>
  <div ref="root" class="home">
    <div class="bg" aria-hidden="true"><div class="orb o1" /><div class="orb o2" /></div>
    <section class="hero">
      <p class="tag"><i />00 / DELIVERY</p>
      <h1>交付总览</h1>
      <p class="desc">项目交付五阶段，实时汇总</p>
    </section>
    <section v-if="!loading && !loadError" class="grid">
      <RouterLink v-for="c in cards" :key="c.label" :to="c.to" class="card" :aria-label="`${c.label} ${c.value}，${c.action}`">
        <span class="num" :style="{ color: c.color }" v-count="c.value">{{ c.value }}</span>
        <span class="label">{{ c.label }}</span>
        <span class="card-action" aria-hidden="true">{{ c.action }} <i>↗</i></span>
      </RouterLink>
    </section>
    <section v-else-if="loading" class="status-panel" aria-live="polite">
      <span class="status-mark" aria-hidden="true">···</span>
      <p>正在读取交付数据</p>
    </section>
    <section v-else class="status-panel status-error" role="alert">
      <span class="status-mark" aria-hidden="true">!</span>
      <div><h2>暂时无法读取总览</h2><p>检查网络或服务状态后重试；现有项目数据不会被修改。</p></div>
      <button type="button" class="retry-button wb-press" @click="loadDashboard">重新加载 <span aria-hidden="true">↗</span></button>
    </section>

    <section v-if="!loading && !loadError && recentReqs.length" class="recent">
      <div class="recent-head">
        <div><p class="recent-kicker">LIVE / REQUIREMENTS</p><h2>最近更新的需求</h2></div>
        <RouterLink to="/reports">交付看板 <span class="wb-arrow" aria-hidden="true">↗</span></RouterLink>
      </div>
      <ul class="list">
        <li v-for="r in recentReqs" :key="r.id">
          <RouterLink
            class="recent-row"
            :to="requirementTarget(r)"
            :aria-label="`打开${r.projectName || '项目'}中的需求 ${r.reqNo || ''}：${r.title || '未命名需求'}`"
          >
            <span class="no">{{ r.reqNo || '需求' }}</span>
            <span class="recent-copy">
              <strong>{{ r.title || '未命名需求' }}</strong>
              <small>{{ r.projectName || '所属项目' }}<template v-if="recentDate(r)"> · {{ recentDate(r) }}</template></small>
            </span>
            <span class="badge" :class="badgeClass('req', r.status)">{{ reqStatus[r.status] || r.status || '待确认' }}</span>
            <span class="recent-open" aria-hidden="true">打开 <i>↗</i></span>
          </RouterLink>
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
const taskStats = ref(null)
const recentReqs = computed(() => data.value?.recentRequirements || [])
let animationContext
let mediaQuery
let contentContext

const taskTotal = computed(() => {
  if (!taskStats.value) return '—'
  return Object.values(taskStats.value).reduce((total, count) => {
    const value = Number(count)
    return total + (Number.isFinite(value) ? value : 0)
  }, 0)
})

const cards = computed(() => {
  const d = data.value
  if (!d) return []
  return [
    { label: '项目', value: d.projects ?? 0, color: '#fff', to: '/projects', action: '查看项目' },
    { label: '需求总数', value: d.requirements?.total ?? 0, color: 'var(--cyan)', to: '/reports', action: '看需求统计' },
    { label: '开发任务', value: taskTotal.value, color: 'var(--purple)', to: { path: '/projects', query: { tab: 'task' } }, action: '选项目后打开开发任务' },
    { label: '执行中验证任务', value: d.tests?.running ?? 0, color: 'var(--cyan)', to: { path: '/projects', query: { tab: 'test-items' } }, action: '选项目后打开验证任务' },
    { label: '待处理运维问题', value: d.ops?.open ?? 0, color: 'var(--pink)', to: { path: '/projects', query: { tab: 'ops' } }, action: '选项目后打开运维问题' },
  ]
})

function requirementTarget(requirement) {
  const projectId = Number(requirement?.projectId)
  if (!Number.isSafeInteger(projectId) || projectId < 1) return '/projects'
  return { path: `/projects/${projectId}`, query: { tab: 'req' } }
}

function recentDate(requirement) {
  const value = requirement?.updatedAt || requirement?.createdAt
  return value ? String(value).replace('T', ' ').slice(0, 16) : ''
}

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
  if (!root.value || loading.value || loadError.value) return

  contentContext?.revert()
  const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  contentContext = gsap.context(() => {
    const cardsEl = root.value.querySelectorAll('.card')
    if (cardsEl.length) {
      gsap.fromTo(
        cardsEl,
        reducedMotion ? { autoAlpha: 1, y: 0 } : { autoAlpha: 0, y: 22 },
        {
          autoAlpha: 1,
          y: 0,
          duration: reducedMotion ? 0 : 0.55,
          stagger: reducedMotion ? 0 : 0.07,
          ease: 'power3.out',
          overwrite: 'auto',
          // 入场结束后交还内联样式，否则 GSAP 留下的 transform 会压掉 CSS 的 hover 抬升。
          clearProps: 'all',
        },
      )
    }

    const recentEl = root.value.querySelector('.recent')
    if (recentEl && !reducedMotion) {
      gsap.timeline({
        scrollTrigger: { trigger: recentEl, start: 'top 86%', once: true },
      })
        .from(recentEl.querySelector('.recent-head'), { autoAlpha: 0, y: 12, duration: 0.42, ease: 'power3.out' })
        .from(recentEl.querySelectorAll('.recent-row'), {
          autoAlpha: 0,
          y: 12,
          duration: 0.42,
          stagger: 0.055,
          ease: 'power3.out',
          clearProps: 'all',
        }, '-=0.18')
      ScrollTrigger.refresh()
    }
  }, root.value)
}

watch(loading, (isLoading) => {
  if (!isLoading && !loadError.value) animateDashboardContent()
})

async function loadDashboard() {
  contentContext?.revert()
  contentContext = null
  loading.value = true
  loadError.value = false
  taskStats.value = null
  try {
    const [dashboard, workStatus] = await Promise.all([
      api.get('/dashboard'),
      api.get('/reports/status').catch(() => null),
    ])
    data.value = dashboard.data
    const taskCounts = workStatus?.data?.task
    taskStats.value = taskCounts && typeof taskCounts === 'object' && !Array.isArray(taskCounts) ? taskCounts : null
  }
  catch { loadError.value = true }
  finally { loading.value = false }
}

onMounted(() => {
  setupAnimations()
  loadDashboard()
})

onUnmounted(() => {
  contentContext?.revert()
  mediaQuery?.revert()
  animationContext?.revert()
})
</script>

<style scoped>
.home { max-width: 1536px; min-height: 100svh; margin: 0 auto; padding: calc(var(--nav-h) + clamp(28px, 4vh, 42px)) clamp(22px, 4.2vw, 64px) 96px; }
.bg { position: fixed; inset: 0; z-index: -1; pointer-events: none; }
.orb { position: absolute; border-radius: 50%; filter: blur(110px); opacity: .035; will-change: transform; }
.o1 { width: 360px; height: 360px; background: #fff; top: -8%; right: -5%; }
.o2 { width: 280px; height: 280px; background: #bdbdb7; bottom: 5%; left: -8%; }
.hero { position: relative; margin-bottom: clamp(30px, 6vh, 64px); padding-bottom: 0; border: 0; }
.hero::after { display: none; }
.tag { display: flex; align-items: center; gap: 9px; color: var(--muted); font-size: 11px; letter-spacing: .2em; margin-bottom: 19px; }
.tag i { width: 25px; height: 1px; background: var(--muted); }
h1 { font-family: var(--font-display); font-size: clamp(3rem, 6vw, 6rem); font-weight: 400; letter-spacing: -.09em; line-height: .92; margin-bottom: 18px; }
.desc { color: var(--muted-light); font-size: 11px; letter-spacing: .14em; }
.grid { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 0; margin-bottom: clamp(48px, 10vh, 100px); border-top: 1px solid rgba(239,239,235,.24); border-bottom: 1px solid rgba(239,239,235,.15); background: transparent; }
.card { position: relative; min-width: 0; min-height: 142px; padding: 22px 18px 36px 0; color: inherit; background: transparent; border-right: 1px solid rgba(239,239,235,.12); cursor: pointer; text-decoration: none; will-change: transform, opacity; transition: transform .45s var(--ease), background .35s var(--ease); }
.card:focus-visible { outline: 1px solid rgba(239,239,235,.72); outline-offset: -1px; }
.card + .card { padding-left: 18px; }
.card:last-child { border-right: 0; }
.card::before { position: absolute; top: -1px; left: 0; width: 24px; height: 1px; background: rgba(239,239,235,.72); content: ""; }
.card:hover { z-index: 1; background: rgba(255,255,255,.025); transform: translateY(-5px); }
.num { display: block; font-family: var(--font-display); font-size: clamp(2rem, 4vw, 3.7rem); font-weight: 400; line-height: 1; letter-spacing: -.06em; transition: transform .5s var(--ease), letter-spacing .5s var(--ease); }
.label { display: block; margin-top: 32px; color: var(--muted-light); font-size: 11px; letter-spacing: .11em; transition: color .35s var(--ease), letter-spacing .5s var(--ease); }
.card-action { position: absolute; right: 18px; bottom: 16px; display: flex; align-items: center; gap: 7px; color: var(--muted); font-size: 11px; letter-spacing: .06em; transition: color .3s var(--ease); }
.card-action i { color: var(--muted-light); font-size: 11px; font-style: normal; transition: transform .3s var(--ease); }
.card:hover .num { transform: translateY(-3px); letter-spacing: -.04em; }
.card:hover .label { color: var(--text); letter-spacing: .16em; }
.card:hover .card-action { color: var(--text); }
.card:hover .card-action i { transform: translate(2px, -2px); }
.recent { max-width: 1000px; margin-bottom: 32px; }
.recent-head { display: flex; justify-content: space-between; align-items: flex-end; gap: 20px; margin-bottom: 18px; }
.recent-kicker { margin-bottom: 6px; color: var(--muted); font-size: 11px; letter-spacing: .16em; }
.recent-head h2 { font-family: var(--font-display); font-size: clamp(1.5rem, 2.8vw, 2.2rem); font-weight: 400; letter-spacing: -.04em; }
.recent-head a { display: inline-block; color: var(--muted); text-decoration: none; font-size: 11px; letter-spacing: .12em; transition: color .25s ease, transform .35s var(--ease); }
.recent-head a:hover { color: var(--text); transform: translateX(4px); }
.list { overflow: hidden; border-top: 1px solid rgba(239,239,235,.18); background: transparent; }
.list li { border-bottom: 1px solid rgba(239,239,235,.12); }
.recent-row { display: grid; grid-template-columns: minmax(110px, .55fr) minmax(0, 1.6fr) auto 72px; gap: 18px; align-items: center; min-height: 68px; padding: 11px 10px; margin: 0 -10px; color: inherit; font-size: 11px; text-decoration: none; transition: background .3s var(--ease), transform .35s var(--ease), box-shadow .3s var(--ease); }
.recent-row:hover { background: rgba(255,255,255,.028); box-shadow: inset 2px 0 0 rgba(239,239,235,.5); transform: translateX(4px); }
.recent-row:focus-visible { outline: 1px solid rgba(239,239,235,.72); outline-offset: -1px; }
.recent-copy { display: block; min-width: 0; }
.recent-copy strong { display: block; overflow: hidden; color: var(--text); font-weight: 500; text-overflow: ellipsis; white-space: nowrap; }
.recent-copy small { display: block; margin-top: 4px; overflow: hidden; color: var(--muted); font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }
.recent-open { display: flex; justify-content: flex-end; gap: 8px; color: var(--muted); font-size: 11px; letter-spacing: .08em; transition: color .25s ease; }
.recent-open i { color: var(--muted-light); font-size: 12px; font-style: normal; transition: transform .3s var(--ease); }
.recent-row:hover .recent-open { color: var(--text); }
.recent-row:hover .recent-open i { transform: translate(2px, -2px); }
.no { color: var(--muted-light); font-size: 11px; letter-spacing: .14em; }
.status-panel { display: flex; min-height: 150px; align-items: center; gap: 18px; padding: 26px 0; border-top: 1px solid rgba(239,239,235,.18); border-bottom: 1px solid rgba(239,239,235,.12); color: var(--muted-light); }
.status-mark { display: grid; width: 42px; height: 42px; flex: 0 0 auto; place-items: center; border: 1px solid rgba(239,239,235,.25); border-radius: 50%; color: var(--text); font-family: var(--font-display); font-size: 18px; }
.status-panel h2 { margin: 0 0 8px; color: var(--text); font-size: 15px; font-weight: 500; }
.status-panel p { margin: 0; color: var(--muted); font-size: 12px; line-height: 1.7; }
.retry-button { display: inline-flex; align-items: center; gap: 16px; margin-left: auto; padding: 11px 14px; border: 1px solid rgba(239,239,235,.28); border-radius: 999px; color: var(--text); background: transparent; cursor: pointer; font: inherit; font-size: 11px; transition: background .25s ease, border-color .25s ease; }
.retry-button:hover { border-color: rgba(239,239,235,.62); background: rgba(255,255,255,.05); }
@media (max-width: 900px) { .grid { grid-template-columns: repeat(3, minmax(0, 1fr)); } }
@media (max-width: 540px) {
  .home { padding-top: calc(var(--nav-h) + 28px); padding-bottom: 72px; }
  .grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .card:nth-child(3) { border-top: 1px solid rgba(239,239,235,.12); }
  .card:nth-child(n+4) { border-top: 1px solid rgba(239,239,235,.12); }
  .card:nth-child(3), .card:nth-child(5) { padding-left: 0; }
  .card:nth-child(even) { padding-left: 18px; border-right: 0; }
  .recent-head { align-items: flex-start; }
  .recent-row { grid-template-columns: minmax(0, 1fr) auto 16px; gap: 10px; padding-inline: 8px; }
  .recent-row .no { display: none; }
  .recent-open { font-size: 0; }
  .recent-open i { font-size: 14px; }
  .status-panel { align-items: flex-start; flex-wrap: wrap; }
  .retry-button { margin-left: 60px; }
}
</style>
