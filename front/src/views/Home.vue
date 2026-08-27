<template>
  <div class="home">
    <div class="bg" aria-hidden="true"><div class="orb o1" /><div class="orb o2" /></div>
    <section class="hero">
      <p class="tag">DASHBOARD</p>
      <h1>研发概览</h1>
      <p class="desc">以项目为中心，管理需求、接口、测试与运维</p>
    </section>

    <section v-if="!loading" class="grid">
      <RouterLink v-for="card in cards" :key="card.label" :to="card.to" class="card card-link">
        <span class="num" :style="{ color: card.color }">{{ card.value }}</span>
        <span class="label">{{ card.label }}</span>
      </RouterLink>
    </section>
    <p v-else class="empty">加载中…</p>

    <section v-if="requirementBreakdown.length" class="breakdown">
      <h2>需求状态</h2>
      <div class="chips">
        <RouterLink v-for="item in requirementBreakdown" :key="item.key" to="/projects" class="chip">
          {{ item.label }} {{ item.value }}
        </RouterLink>
      </div>
    </section>

    <section v-if="recentReqs.length" class="recent">
      <div class="recent-head"><h2>最近需求</h2><RouterLink to="/projects">进入项目 →</RouterLink></div>
      <ul class="list">
        <li v-for="req in recentReqs" :key="req.id">
          <RouterLink :to="`/projects/${req.projectId}?tab=req`" class="row-link">
            <span class="no">{{ req.reqNo }}</span>
            <span class="name">{{ req.title }}</span>
            <span class="badge" :class="badgeClass('req', req.status)">{{ reqStatus[req.status] }}</span>
          </RouterLink>
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
  const dashboard = data.value
  if (!dashboard) return []
  return [
    { label: '项目', value: dashboard.projects ?? 0, color: '#fff', to: '/projects' },
    { label: '需求', value: dashboard.requirements?.total ?? 0, color: 'var(--cyan)', to: '/projects' },
    { label: '接口', value: dashboard.apis ?? 0, color: 'var(--purple)', to: '/projects' },
    { label: '测试中', value: dashboard.tests?.running ?? 0, color: 'var(--cyan)', to: '/projects' },
    { label: '运维待处理', value: dashboard.ops?.open ?? 0, color: 'var(--pink)', to: '/projects' },
    { label: '活跃团队', value: dashboard.teams?.active ?? 0, color: 'var(--lime)', to: '/teams' },
  ]
})

const requirementBreakdown = computed(() => {
  const stats = data.value?.requirements
  if (!stats) return []
  return Object.entries(reqStatus)
    .map(([key, label]) => ({ key, label, value: stats[key] ?? 0 }))
    .filter((item) => item.value > 0)
})

onMounted(async () => {
  try { data.value = (await api.get('/dashboard')).data }
  catch (error) { ElMessage.error(error.message) }
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
.grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 14px; margin-bottom: 28px; }
.card { padding: 22px; background: var(--glass); border: 1px solid var(--border); border-radius: var(--radius); }
.card-link { text-decoration: none; color: inherit; transition: border-color 0.2s, transform 0.2s; display: block; }
.card-link:hover { border-color: rgba(0, 229, 255, 0.35); transform: translateY(-2px); }
.num { display: block; font-family: var(--font-display); font-size: 2rem; font-weight: 800; }
.label { font-size: 14px; color: var(--muted); }
.breakdown { margin-bottom: 32px; }
.breakdown h2 { font-family: var(--font-display); font-size: 1.1rem; margin-bottom: 12px; }
.chips { display: flex; flex-wrap: wrap; gap: 8px; }
.chip {
  border: 1px solid var(--border);
  background: var(--glass);
  color: var(--text);
  border-radius: 999px;
  padding: 8px 14px;
  text-decoration: none;
  font-size: 14px;
}
.recent { margin-bottom: 32px; }
.recent-head { display: flex; justify-content: space-between; margin-bottom: 14px; }
.recent-head h2 { font-family: var(--font-display); font-size: 1.25rem; font-weight: 700; }
.recent-head a { font-size: 15px; color: var(--cyan); text-decoration: none; }
.list { list-style: none; background: var(--glass); border: 1px solid var(--border); border-radius: var(--radius); overflow: hidden; }
.list li { border-bottom: 1px solid var(--border); }
.list li:last-child { border-bottom: none; }
.row-link {
  display: grid;
  grid-template-columns: 1fr 1.2fr auto;
  gap: 12px;
  padding: 16px 20px;
  font-size: 16px;
  align-items: center;
  color: inherit;
  text-decoration: none;
}
.row-link:hover { background: rgba(0, 229, 255, 0.05); }
.no { font-family: var(--font-display); font-weight: 600; }
.name { color: var(--muted); }
.empty { color: var(--muted); text-align: center; padding: 40px; font-size: 16px; }
</style>
