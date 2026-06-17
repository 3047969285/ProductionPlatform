<template>
  <div class="home">
    <div class="bg" aria-hidden="true"><div class="orb o1" /><div class="orb o2" /></div>
    <section class="hero">
      <p class="tag">DASHBOARD</p>
      <h1>研发概览</h1>
      <p class="desc">以项目为中心，管理需求、接口、测试与运维</p>
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
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'
import { reqStatus, badgeClass } from '../constants'

const loading = ref(true)
const data = ref(null)
const recentReqs = computed(() => data.value?.recentRequirements || [])

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

onMounted(async () => {
  try { data.value = (await api.get('/dashboard')).data }
  catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
})
</script>

<style scoped>
.home { position: relative; max-width: 960px; margin: 0 auto; padding: 40px clamp(20px, 5vw, 48px) 80px; }
.bg { position: fixed; inset: 0; z-index: -1; pointer-events: none; }
.orb { position: absolute; border-radius: 50%; filter: blur(100px); opacity: 0.3; }
.o1 { width: 360px; height: 360px; background: var(--purple); top: -8%; right: -5%; }
.o2 { width: 280px; height: 280px; background: var(--cyan); bottom: 5%; left: -8%; }
.hero { margin-bottom: 36px; }
.tag { font-size: 12px; letter-spacing: 0.3em; color: var(--cyan); margin-bottom: 12px; }
h1 { font-family: var(--font-display); font-size: clamp(2.25rem, 6vw, 3.25rem); font-weight: 800; margin-bottom: 8px; }
.desc { color: var(--muted); font-size: 16px; }
.grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 14px; margin-bottom: 40px; }
.card { padding: 22px; background: var(--glass); border: 1px solid var(--border); border-radius: var(--radius); }
.num { display: block; font-family: var(--font-display); font-size: 2rem; font-weight: 800; }
.label { font-size: 14px; color: var(--muted); }
.recent { margin-bottom: 32px; }
.recent-head { display: flex; justify-content: space-between; margin-bottom: 14px; }
.recent-head h2 { font-family: var(--font-display); font-size: 1.25rem; font-weight: 700; }
.recent-head a { font-size: 15px; color: var(--cyan); text-decoration: none; }
.list { list-style: none; background: var(--glass); border: 1px solid var(--border); border-radius: var(--radius); overflow: hidden; }
.list li { display: grid; grid-template-columns: 1fr 1.2fr auto; gap: 12px; padding: 16px 20px; border-bottom: 1px solid var(--border); font-size: 16px; align-items: center; }
.list li:last-child { border-bottom: none; }
.no { font-family: var(--font-display); font-weight: 600; }
.name { color: var(--muted); }
.empty { color: var(--muted); text-align: center; padding: 40px; font-size: 16px; }
</style>
