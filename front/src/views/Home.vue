<template>
  <div class="home">
    <section class="hero animate-fade-up">
      <div class="hero-ring" aria-hidden="true" />
      <p class="eyebrow">研发交付管控</p>
      <h1>把需求、接口、测试与运维<br>收敛到一个项目空间</h1>
      <p class="desc">参考成熟交付平台的项目中心模式：一眼看清进度，一步进入执行。</p>
      <div class="hero-actions">
        <RouterLink to="/projects" class="cta">进入项目中心</RouterLink>
        <RouterLink to="/teams" class="ghost">查看团队</RouterLink>
      </div>
    </section>

    <section v-if="!loading" class="stats">
      <RouterLink
        v-for="(card, index) in cards"
        :key="card.label"
        :to="card.to"
        class="stat-card surface surface-hover animate-fade-up"
        :class="`stagger-${index + 1}`"
      >
        <span class="stat-number" :style="{ color: card.color }">{{ card.value }}</span>
        <span class="stat-label">{{ card.label }}</span>
        <span class="stat-hint">{{ card.hint }}</span>
      </RouterLink>
    </section>
    <p v-else class="empty">加载中…</p>

    <section v-if="requirementBreakdown.length" class="breakdown animate-fade-up stagger-2">
      <div class="section-head">
        <h2 class="section-title">需求状态分布</h2>
        <RouterLink to="/projects" class="more">查看全部项目</RouterLink>
      </div>
      <div class="chips">
        <span v-for="item in requirementBreakdown" :key="item.key" class="chip">
          {{ item.label }} <strong>{{ item.value }}</strong>
        </span>
      </div>
    </section>

    <section v-if="recentReqs.length" class="recent animate-fade-up stagger-3">
      <div class="section-head">
        <h2 class="section-title">最近更新的需求</h2>
        <RouterLink to="/projects" class="more">进入项目</RouterLink>
      </div>
      <ul class="list surface">
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
    { label: '项目', value: dashboard.projects ?? 0, color: 'var(--text)', hint: '项目空间', to: '/projects' },
    { label: '需求', value: dashboard.requirements?.total ?? 0, color: 'var(--accent)', hint: '交付主线', to: '/projects' },
    { label: '接口', value: dashboard.apis ?? 0, color: 'var(--purple)', hint: 'API 文档', to: '/projects' },
    { label: '测试中', value: dashboard.tests?.running ?? 0, color: 'var(--warning)', hint: '进行中', to: '/projects' },
    { label: '运维待处理', value: dashboard.ops?.open ?? 0, color: 'var(--danger)', hint: '需跟进', to: '/projects' },
    { label: '活跃团队', value: dashboard.teams?.active ?? 0, color: 'var(--success)', hint: '协作资源', to: '/teams' },
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
.home {
  max-width: var(--content-max);
  margin: 0 auto;
  padding: 12px clamp(20px, 4vw, 40px) 80px;
}

.hero {
  position: relative;
  padding: 48px 0 40px;
  overflow: hidden;
}

.hero-ring {
  position: absolute;
  top: -80px;
  right: -40px;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  background: var(--ring);
  animation: ring-pulse 6s ease-in-out infinite;
  pointer-events: none;
}

.eyebrow {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--accent);
  margin-bottom: 14px;
}

.hero h1 {
  font-family: var(--font-display);
  font-size: clamp(2.2rem, 5vw, 3.4rem);
  font-weight: 700;
  letter-spacing: -0.04em;
  line-height: 1.06;
  max-width: 12em;
}

.desc {
  margin-top: 16px;
  max-width: 34rem;
  font-size: 1.125rem;
  color: var(--muted);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 28px;
}

.cta,
.ghost {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 148px;
  padding: 12px 22px;
  border-radius: 980px;
  font-size: 15px;
  font-weight: 600;
  text-decoration: none;
  transition: transform 0.25s var(--ease-spring), box-shadow 0.25s var(--ease-spring), background 0.2s var(--ease);
}

.cta {
  color: #fff;
  background: var(--accent);
  box-shadow: 0 8px 24px rgba(0, 113, 227, 0.22);
}

.cta:hover {
  transform: translateY(-1px);
  background: var(--accent-hover);
}

.ghost {
  color: var(--text);
  background: var(--bg-elevated);
  border: 1px solid var(--border);
}

.ghost:hover {
  background: var(--bg-secondary);
}

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 14px;
  margin-bottom: 36px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 22px 20px;
  text-decoration: none;
  color: inherit;
}

.stat-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
}

.stat-hint {
  font-size: 13px;
  color: var(--muted-light);
}

.breakdown,
.recent {
  margin-bottom: 36px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.more {
  font-size: 14px;
  font-weight: 500;
  color: var(--accent);
  text-decoration: none;
}

.more:hover { text-decoration: underline; }

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.chip {
  padding: 10px 14px;
  border-radius: 980px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  font-size: 14px;
  color: var(--muted);
}

.chip strong {
  color: var(--text);
  font-weight: 700;
}

.list {
  list-style: none;
  overflow: hidden;
}

.list li + li {
  border-top: 1px solid var(--border);
}

.row-link {
  display: grid;
  grid-template-columns: 120px 1fr auto;
  gap: 12px;
  padding: 16px 18px;
  align-items: center;
  color: inherit;
  text-decoration: none;
  transition: background 0.2s var(--ease);
}

.row-link:hover {
  background: var(--bg-secondary);
}

.no {
  font-family: var(--font-display);
  font-weight: 600;
  font-size: 14px;
}

.name {
  color: var(--muted);
}

.empty {
  color: var(--muted);
  text-align: center;
  padding: 40px;
}
</style>
