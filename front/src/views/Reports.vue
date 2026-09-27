<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageShell from '../components/PageShell.vue'
import api from '../api'
import { reqStatus, taskStatus, bugStatus, badgeClass } from '../constants'

const loading = ref(false)
const status = ref(null)
const severity = ref([])
const projects = ref([])
const sprints = ref([])
const assignee = ref([])
const trend = ref(null)
const completion = ref([])

const barColors = { high: 'var(--pink)', medium: 'var(--cyan)', low: 'var(--lime)' }

async function load() {
  loading.value = true
  try {
    const [s, sev, p, sp, a, t, c] = await Promise.all([
      api.get('/reports/status'),
      api.get('/reports/bug-severity'),
      api.get('/reports/projects'),
      api.get('/reports/sprints'),
      api.get('/reports/assignee'),
      api.get('/reports/trend'),
      api.get('/reports/completion'),
    ])
    status.value = s.data
    severity.value = sev.data
    projects.value = p.data
    sprints.value = sp.data
    assignee.value = a.data
    trend.value = t.data
    completion.value = c.data
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

function maxBar(items, key = 'cnt') {
  return Math.max(1, ...items.map(i => Number(i[key]) || 0))
}
function statusRows(type) {
  if (!status.value?.[type]) return []
  const map = type === 'requirement' ? reqStatus : type === 'task' ? taskStatus : bugStatus
  return Object.entries(status.value[type]).map(([k, v]) => ({ k, v, label: map[k] || k }))
}
function maxStatus(type) {
  return Math.max(1, ...statusRows(type).map(i => Number(i.v) || 0))
}

onMounted(load)
</script>

<template>
  <PageShell tag="04 / REPORTS" title="报表">
    <template #action>
      <el-button @click="load">刷新</el-button>
    </template>

    <div v-loading="loading" class="reports">
      <!-- 工作项状态分布 -->
      <section class="card">
        <h3>工作项状态分布</h3>
        <div class="status-grid">
          <div v-for="type in ['requirement', 'task', 'bug']" :key="type" class="status-block">
            <p class="type-name">{{ { requirement: '需求', task: '任务', bug: '缺陷' }[type] }}</p>
            <div class="bars">
              <div v-for="row in statusRows(type)" :key="row.k" class="bar-row">
                <span class="bar-label">{{ row.label }}</span>
                <div class="bar-track"><div class="bar-fill" :style="{ width: (row.v / maxStatus(type) * 100) + '%' }" /></div>
                <span class="bar-val">{{ row.v }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 缺陷严重程度 -->
      <section class="card">
        <h3>缺陷严重程度分布</h3>
        <div class="bars">
          <div v-for="row in severity" :key="row.severity" class="bar-row">
            <span class="bar-label">{{ { low: '低', medium: '中', high: '高', critical: '紧急' }[row.severity] || row.severity }}</span>
            <div class="bar-track"><div class="bar-fill" :style="{ width: (row.cnt / maxBar(severity) * 100) + '%', background: barColors[row.severity] }" /></div>
            <span class="bar-val">{{ row.cnt }}</span>
          </div>
          <p v-if="!severity.length" class="empty">暂无数据</p>
        </div>
      </section>

      <!-- 项目工作项 -->
      <section class="card">
        <h3>各项目工作项数量</h3>
        <el-table :data="projects" size="small" stripe>
          <el-table-column prop="projectName" label="项目" min-width="160" />
          <el-table-column prop="reqCnt" label="需求" width="80" />
          <el-table-column prop="taskCnt" label="任务" width="80" />
          <el-table-column prop="bugCnt" label="缺陷" width="80" />
        </el-table>
      </section>

      <!-- 迭代工作项 -->
      <section class="card">
        <h3>各迭代工作项数量</h3>
        <el-table :data="sprints" size="small" stripe>
          <el-table-column prop="sprintName" label="迭代" min-width="160" />
          <el-table-column prop="taskCnt" label="任务" width="80" />
          <el-table-column prop="bugCnt" label="缺陷" width="80" />
        </el-table>
      </section>

      <!-- 成员负载 -->
      <section class="card">
        <h3>成员负载（任务 + 缺陷）</h3>
        <div class="bars">
          <div v-for="row in assignee" :key="row.assignee" class="bar-row">
            <span class="bar-label">{{ row.assignee }}</span>
            <div class="bar-track"><div class="bar-fill" :style="{ width: (row.cnt / maxBar(assignee) * 100) + '%' }" /></div>
            <span class="bar-val">{{ row.cnt }}</span>
          </div>
          <p v-if="!assignee.length" class="empty">暂无数据</p>
        </div>
      </section>

      <!-- 30 天趋势 -->
      <section class="card">
        <h3>近 30 天新增趋势</h3>
        <div class="trend-grid">
          <div v-for="kind in ['requirement', 'bug']" :key="kind" class="trend-block">
            <p class="type-name">{{ kind === 'requirement' ? '需求' : '缺陷' }}</p>
            <div class="trend-bars">
              <div v-for="d in (trend?.[kind] || [])" :key="d.day" class="trend-col" :title="d.day + '：' + d.cnt">
                <div class="trend-fill" :style="{ height: Math.max(3, d.cnt / Math.max(1, ...(trend?.[kind] || []).map(x => x.cnt)) * 120) + 'px' }" />
              </div>
            </div>
          </div>
          <p v-if="!trend" class="empty">暂无数据</p>
        </div>
      </section>

      <!-- 项目完成率 -->
      <section class="card">
        <h3>项目需求完成率</h3>
        <el-table :data="completion" size="small" stripe>
          <el-table-column prop="projectName" label="项目" min-width="160" />
          <el-table-column label="完成率" min-width="240">
            <template #default="{ row }">
              <el-progress :percentage="row.total ? Math.round(row.doneCnt / row.total * 100) : 0" :stroke-width="10" />
            </template>
          </el-table-column>
          <el-table-column label="完成/总数" width="100">
            <template #default="{ row }">{{ row.doneCnt }}/{{ row.total }}</template>
          </el-table-column>
        </el-table>
      </section>
    </div>
  </PageShell>
</template>

<style scoped>
.reports { display: grid; grid-template-columns: repeat(auto-fit, minmax(360px, 1fr)); gap: 16px; }
.card { padding: 20px; background: rgba(23, 27, 25, .86); border: 1px solid var(--border); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); }
.card h3 { font-size: 16px; font-weight: 700; margin-bottom: 16px; }
.status-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; }
.type-name { font-size: 13px; color: var(--cyan); margin-bottom: 10px; }
.bars { display: flex; flex-direction: column; gap: 8px; }
.bar-row { display: grid; grid-template-columns: 60px 1fr 36px; gap: 8px; align-items: center; font-size: 13px; }
.bar-label { color: var(--muted); }
.bar-track { height: 4px; border-radius: 999px; background: rgba(234, 238, 222, .1); overflow: hidden; }
.bar-fill { height: 100%; border-radius: 4px; background: linear-gradient(90deg, var(--cyan), var(--purple)); }
.bar-val { text-align: right; color: var(--text); }
.trend-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.trend-bars { display: flex; align-items: flex-end; gap: 3px; height: 130px; }
.trend-col { flex: 1; display: flex; align-items: flex-end; }
.trend-fill { width: 100%; background: linear-gradient(180deg, var(--cyan), var(--purple)); border-radius: 3px 3px 0 0; min-height: 3px; }
.empty { color: var(--muted); text-align: center; padding: 20px; }
@media (max-width: 640px) { .trend-grid { grid-template-columns: 1fr; } }
</style>
