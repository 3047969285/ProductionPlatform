<template>
  <div class="home">
    <div class="bg" aria-hidden="true"><div class="orb o1" /><div class="orb o2" /></div>

    <section class="hero">
      <p class="tag">DEV DASHBOARD</p>
      <h1>研发概览</h1>
      <p class="desc">跟踪需求、交付任务、研发团队与测试验收进度</p>
    </section>

    <section v-if="!loading" class="grid">
      <article v-for="c in cards" :key="c.label" class="card">
        <span class="num" :style="{ color: c.color }">{{ c.value }}</span>
        <span class="label">{{ c.label }}</span>
      </article>
    </section>
    <p v-else class="empty">加载中…</p>

    <section v-if="recentReqs.length" class="recent">
      <div class="recent-head">
        <h2>最近需求</h2>
        <RouterLink to="/requirements">查看全部 →</RouterLink>
      </div>
      <ul class="list">
        <li v-for="r in recentReqs" :key="r.id">
          <span class="no">{{ r.reqNo }}</span>
          <span class="name">{{ r.title }}</span>
          <span class="badge" :class="badgeClass('req', r.status)">{{ reqStatus[r.status] }}</span>
        </li>
      </ul>
    </section>

    <section v-if="recent.length" class="recent">
      <div class="recent-head">
        <h2>最近交付任务</h2>
        <RouterLink to="/orders">查看全部 →</RouterLink>
      </div>
      <ul class="list">
        <li v-for="o in recent" :key="o.id">
          <span class="no">{{ o.orderNo }}</span>
          <span class="name">{{ o.productName }}</span>
          <span class="badge" :class="badgeClass('order', o.status)">{{ orderStatus[o.status] }}</span>
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
import { orderStatus, reqStatus, badgeClass } from '../constants'

const loading = ref(true)
const data = ref(null)

const recent = computed(() => data.value?.recentOrders || [])
const recentReqs = computed(() => data.value?.recentRequirements || [])

const cards = computed(() => {
  const d = data.value
  if (!d) return []
  return [
    { label: '产品需求', value: d.requirements?.total ?? 0, color: '#fff' },
    { label: '评审中', value: d.requirements?.review ?? 0, color: 'var(--cyan)' },
    { label: '开发中', value: d.requirements?.developing ?? 0, color: 'var(--purple)' },
    { label: '已实现', value: d.requirements?.done ?? 0, color: 'var(--lime)' },
    { label: '交付任务', value: d.orders?.total ?? 0, color: 'var(--muted)' },
    { label: '验收通过', value: d.quality?.pass ?? 0, color: 'var(--lime)' },
  ]
})

onMounted(async () => {
  try {
    const res = await api.get('/dashboard')
    data.value = res.data
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.home {
  position: relative;
  max-width: 960px;
  margin: 0 auto;
  padding: 40px clamp(20px, 5vw, 48px) 80px;
}
.bg { position: fixed; inset: 0; z-index: -1; pointer-events: none; }
.orb { position: absolute; border-radius: 50%; filter: blur(100px); opacity: 0.3; }
.o1 { width: 360px; height: 360px; background: var(--purple); top: -8%; right: -5%; }
.o2 { width: 280px; height: 280px; background: var(--cyan); bottom: 5%; left: -8%; }
.hero { margin-bottom: 36px; }
.tag { font-size: 10px; letter-spacing: 0.3em; color: var(--cyan); margin-bottom: 12px; }
h1 {
  font-family: var(--font-display);
  font-size: clamp(2rem, 6vw, 3rem);
  font-weight: 800;
  letter-spacing: -0.03em;
  margin-bottom: 8px;
}
.desc { color: var(--muted); font-size: 14px; }
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
  gap: 14px;
  margin-bottom: 40px;
}
.card {
  padding: 20px;
  background: var(--glass);
  border: 1px solid var(--border);
  border-radius: var(--radius);
}
.num { display: block; font-family: var(--font-display); font-size: 1.75rem; font-weight: 800; }
.label { font-size: 11px; color: var(--muted); }
.recent-head { display: flex; justify-content: space-between; margin-bottom: 14px; }
.recent-head h2 { font-family: var(--font-display); font-size: 1.1rem; font-weight: 700; }
.recent-head a { font-size: 13px; color: var(--cyan); text-decoration: none; }
.list { list-style: none; background: var(--glass); border: 1px solid var(--border); border-radius: var(--radius); overflow: hidden; }
.list li {
  display: grid;
  grid-template-columns: 1fr 1.2fr auto;
  gap: 12px;
  padding: 14px 18px;
  border-bottom: 1px solid var(--border);
  font-size: 14px;
  align-items: center;
}
.list li:last-child { border-bottom: none; }
.no { font-family: var(--font-display); font-weight: 600; }
.name { color: var(--muted); }
.empty { color: var(--muted); text-align: center; padding: 40px; }
</style>
