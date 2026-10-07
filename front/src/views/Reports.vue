<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import PageShell from '../components/PageShell.vue'
import api from '../api'
import {
  bugSeverity,
  bugStatus,
  badgeClass,
  milestoneStatus,
  releaseStatus,
  reqStatus,
  sprintStatus,
  taskStatus,
  testPlanStatus,
} from '../constants'

const loading = ref(false)
const loadError = ref(false)
const projects = ref([])
const selectedProjectId = ref('all')
const globalData = ref({
  status: {},
  severity: [],
  projects: [],
  sprints: [],
  assignee: [],
  trend: { requirement: [], bug: [] },
  completion: [],
})
const projectData = ref(null)
let loadSequence = 0
const completedRequirementStatuses = new Set(['approved', 'done'])

const isAllProjects = computed(() => selectedProjectId.value === 'all')
const selectedProject = computed(() => projects.value.find((item) => String(item.id) === String(selectedProjectId.value)))
const projectName = computed(() => selectedProject.value?.name || '全项目')
const boardTitle = computed(() => isAllProjects.value ? '全项目交付看板' : `${projectName.value} · 交付看板`)
const boardTag = computed(() => isAllProjects.value ? '04 / DELIVERY BOARD' : `04 / ${selectedProject.value?.code || 'PROJECT BOARD'}`)
const boardDescription = computed(() => {
  if (isAllProjects.value) return '基于项目、需求、开发、质量与发布记录的实时汇总'
  const project = selectedProject.value
  return [project?.description, project?.techStack, project?.deliveryType].filter(Boolean).join(' · ') || '项目实际交付状态与风险'
})

function number(value) {
  return Number(value) || 0
}

function countBy(list, field, value) {
  return (list || []).filter((item) => item?.[field] === value).length
}

function sumStatus(statusMap) {
  return Object.values(statusMap || {}).reduce((total, value) => total + number(value), 0)
}

function formatDate(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '—'
}

function dateValue(item) {
  return Date.parse(item?.updatedAt || item?.createdAt || item?.releasedAt || item?.dueDate || '') || 0
}

function sortRecent(list) {
  return [...(list || [])].sort((a, b) => dateValue(b) - dateValue(a))
}

async function loadProjects() {
  return (await api.get('/projects')).data
}

async function loadGlobalBoard() {
  const [status, severity, projectRows, sprints, assignee, trend, completion] = await Promise.all([
    api.get('/reports/status'),
    api.get('/reports/bug-severity'),
    api.get('/reports/projects'),
    api.get('/reports/sprints'),
    api.get('/reports/assignee'),
    api.get('/reports/trend'),
    api.get('/reports/completion'),
  ])
  return {
    status: status.data,
    severity: severity.data,
    projects: projectRows.data,
    sprints: sprints.data,
    assignee: assignee.data,
    trend: trend.data,
    completion: completion.data,
  }
}

async function loadProjectBoard(selectedId) {
  const projectId = Number(selectedId)
  const [requirements, tasks, bugs, sprints, testPlans, milestones, releases, members] = await Promise.all([
    api.get('/requirements', { params: { projectId } }),
    api.get('/tasks', { params: { projectId } }),
    api.get('/bugs', { params: { projectId } }),
    api.get('/sprints', { params: { projectId } }),
    api.get('/test-plans', { params: { projectId } }),
    api.get('/milestones', { params: { projectId } }),
    api.get('/releases', { params: { projectId } }),
    api.get(`/projects/${projectId}/members`),
  ])
  return {
    requirements: requirements.data,
    tasks: tasks.data,
    bugs: bugs.data,
    sprints: sprints.data,
    testPlans: testPlans.data,
    milestones: milestones.data,
    releases: releases.data,
    members: members.data,
  }
}

async function load() {
  const sequence = ++loadSequence
  loading.value = true
  loadError.value = false
  try {
    if (!projects.value.length) {
      const nextProjects = await loadProjects()
      if (sequence !== loadSequence) return
      projects.value = nextProjects
    }

    const allProjects = isAllProjects.value
    const nextData = allProjects
      ? await loadGlobalBoard()
      : await loadProjectBoard(selectedProjectId.value)
    if (sequence !== loadSequence) return

    if (allProjects) {
      globalData.value = nextData
      projectData.value = null
    } else {
      projectData.value = nextData
    }
  } catch {
    if (sequence === loadSequence) loadError.value = true
  } finally {
    if (sequence === loadSequence) loading.value = false
  }
}

function statusRows(type) {
  const maps = { requirement: reqStatus, task: taskStatus, bug: bugStatus }
  const map = maps[type]
  if (isAllProjects.value) {
    const values = globalData.value.status?.[type] || {}
    return Object.entries(map).map(([key, label]) => ({ key, label, value: number(values[key]) }))
  }
  const source = projectData.value?.[{ requirement: 'requirements', task: 'tasks', bug: 'bugs' }[type]] || []
  return Object.entries(map).map(([key, label]) => ({ key, label, value: countBy(source, 'status', key) }))
}

const statusBlocks = computed(() => [
  { key: 'requirement', label: '需求推进', rows: statusRows('requirement') },
  { key: 'task', label: '开发执行', rows: statusRows('task') },
  { key: 'bug', label: '缺陷收敛', rows: statusRows('bug') },
])

function maxRows(rows) {
  return Math.max(1, ...rows.map((row) => row.value))
}

const metrics = computed(() => {
  if (isAllProjects.value) {
    const reqTotal = sumStatus(globalData.value.status?.requirement)
    const reqDone = number(globalData.value.status?.requirement?.approved) + number(globalData.value.status?.requirement?.done)
    const taskTotal = sumStatus(globalData.value.status?.task)
    const taskDone = number(globalData.value.status?.task?.done)
    const bugTotal = sumStatus(globalData.value.status?.bug)
    const bugOpen = bugTotal - number(globalData.value.status?.bug?.resolved) - number(globalData.value.status?.bug?.closed)
    return [
      { label: '项目', value: projects.value.length, note: '实际项目' },
      { label: '需求完成', value: `${reqDone}/${reqTotal}`, note: `${reqTotal ? Math.round(reqDone / reqTotal * 100) : 0}% 已完成` },
      { label: '任务完成', value: `${taskDone}/${taskTotal}`, note: `${taskTotal ? Math.round(taskDone / taskTotal * 100) : 0}% 已完成` },
      { label: '未解决缺陷', value: bugOpen, note: `${bugTotal} 条缺陷记录` },
    ]
  }

  const data = projectData.value || {}
  const requirementDone = (data.requirements || []).filter((item) => completedRequirementStatuses.has(item.status)).length
  const taskDone = countBy(data.tasks, 'status', 'done')
  const openBugs = (data.bugs || []).filter((bug) => !['resolved', 'closed'].includes(bug.status)).length
  const activeSprint = (data.sprints || []).filter((sprint) => sprint.status === 'active').length
  const runningPlans = (data.testPlans || []).filter((plan) => plan.status === 'running').length
  const pendingReleases = (data.releases || []).filter((release) => ['planned', 'deploying'].includes(release.status)).length
  return [
    { label: '需求完成', value: `${requirementDone}/${data.requirements?.length || 0}`, note: `${data.requirements?.length ? Math.round(requirementDone / data.requirements.length * 100) : 0}% 已完成` },
    { label: '任务完成', value: `${taskDone}/${data.tasks?.length || 0}`, note: `${data.tasks?.length ? Math.round(taskDone / data.tasks.length * 100) : 0}% 已完成` },
    { label: '风险缺陷', value: openBugs, note: `${(data.bugs || []).length} 条缺陷记录` },
    { label: '交付节点', value: activeSprint + runningPlans + pendingReleases, note: `${activeSprint} 个迭代 · ${pendingReleases} 个待发布` },
  ]
})

const globalProjectRows = computed(() => {
  const completionMap = new Map((globalData.value.completion || []).map((row) => [String(row.projectId), row]))
  return (globalData.value.projects || []).map((row) => ({
    ...row,
    total: number(row.reqCnt) + number(row.taskCnt) + number(row.bugCnt),
    completion: completionMap.get(String(row.projectId)) || { total: 0, doneCnt: 0 },
  }))
})

const globalSprintRows = computed(() => (globalData.value.sprints || []).slice(0, 6).map((row) => ({
  ...row,
  total: number(row.taskCnt) + number(row.bugCnt),
})))

const globalRiskRows = computed(() => [...(globalData.value.severity || [])]
  .sort((a, b) => number(b.cnt) - number(a.cnt))
  .map((row) => ({ ...row, label: bugSeverity[row.severity] || row.severity })))

const trendRows = computed(() => {
  const requirementMap = new Map((globalData.value.trend?.requirement || []).map((row) => [row.day, number(row.cnt)]))
  const bugMap = new Map((globalData.value.trend?.bug || []).map((row) => [row.day, number(row.cnt)]))
  const today = new Date()
  const rows = []
  for (let offset = 29; offset >= 0; offset -= 1) {
    const date = new Date(today.getFullYear(), today.getMonth(), today.getDate() - offset)
    const day = [date.getFullYear(), String(date.getMonth() + 1).padStart(2, '0'), String(date.getDate()).padStart(2, '0')].join('-')
    rows.push({ day, total: (requirementMap.get(day) || 0) + (bugMap.get(day) || 0) })
  }
  return rows
})

const trendTicks = computed(() => trendRows.value
  .filter((_, index) => [0, 7, 14, 21, 29].includes(index))
  .map((row) => ({ day: row.day, label: row.day.slice(5).replace('-', '/') })))
const hasTrendData = computed(() => trendRows.value.some((row) => row.total > 0))

const selectedSprint = computed(() => {
  const sprints = projectData.value?.sprints || []
  return sprints.find((sprint) => sprint.status === 'active') || sortRecent(sprints)[0]
})

const selectedPlan = computed(() => {
  const plans = projectData.value?.testPlans || []
  return plans.find((plan) => plan.status === 'running') || sortRecent(plans)[0]
})

const selectedMilestone = computed(() => {
  const milestones = (projectData.value?.milestones || []).filter((item) => item.status !== 'done')
  return [...milestones].sort((a, b) => {
    if (!a.dueDate) return b.dueDate ? 1 : 0
    if (!b.dueDate) return -1
    return String(a.dueDate).localeCompare(String(b.dueDate))
  })[0]
})

const selectedRelease = computed(() => sortRecent(projectData.value?.releases || [])[0])

const projectRisks = computed(() => {
  const severityRank = { critical: 4, high: 3, medium: 2, low: 1 }
  return (projectData.value?.bugs || [])
    .filter((bug) => !['resolved', 'closed'].includes(bug.status))
    .sort((a, b) => (severityRank[b.severity] || 0) - (severityRank[a.severity] || 0) || dateValue(b) - dateValue(a))
    .slice(0, 5)
})

const projectActivity = computed(() => {
  const requirements = (projectData.value?.requirements || []).map((item) => ({
    id: `r-${item.id}`,
    kind: '需求',
    type: 'req',
    title: item.title,
    meta: `${item.reqNo || '需求'} · ${reqStatus[item.status] || item.status}`,
    status: item.status,
    statusLabel: reqStatus[item.status] || item.status,
    time: item.updatedAt || item.createdAt,
  }))
  const tasks = (projectData.value?.tasks || []).map((item) => ({
    id: `t-${item.id}`,
    kind: '任务',
    type: 'task',
    title: item.title,
    meta: `${item.assignee || '未指派'} · ${taskStatus[item.status] || item.status}`,
    status: item.status,
    statusLabel: taskStatus[item.status] || item.status,
    time: item.updatedAt || item.createdAt,
  }))
  const bugs = (projectData.value?.bugs || []).map((item) => ({
    id: `b-${item.id}`,
    kind: '缺陷',
    type: 'bug',
    title: item.title,
    meta: `${item.assignee || '未指派'} · ${bugStatus[item.status] || item.status}`,
    status: item.status,
    statusLabel: bugStatus[item.status] || item.status,
    time: item.updatedAt || item.createdAt,
  }))
  return [...requirements, ...tasks, ...bugs].sort((a, b) => dateValue({ updatedAt: b.time }) - dateValue({ updatedAt: a.time })).slice(0, 7)
})

const memberLoad = computed(() => {
  const load = new Map()
  for (const item of [...(projectData.value?.tasks || []), ...(projectData.value?.bugs || [])]) {
    const name = item.assignee?.trim() || '未指派'
    load.set(name, (load.get(name) || 0) + 1)
  }
  return [...load.entries()].map(([name, count]) => ({ name, count })).sort((a, b) => b.count - a.count)
})

const maxMemberLoad = computed(() => Math.max(1, ...memberLoad.value.map((row) => row.count)))
const maxRisk = computed(() => Math.max(1, ...globalRiskRows.value.map((row) => number(row.cnt))))
const maxTrend = computed(() => Math.max(1, ...trendRows.value.map((row) => row.total)))
const maxSprintLoad = computed(() => Math.max(1, ...globalSprintRows.value.map((row) => row.total)))

watch(selectedProjectId, () => {
  if (projects.value.length) load()
})

onMounted(load)
</script>

<template>
  <PageShell :tag="boardTag" :title="boardTitle">
    <template #action>
      <div class="report-actions">
        <el-select v-model="selectedProjectId" class="project-select" aria-label="选择报表项目">
          <el-option label="全部项目" value="all" />
          <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="String(project.id)" />
        </el-select>
        <el-button @click="load">刷新</el-button>
      </div>
    </template>

    <section v-if="loadError" class="load-error" role="alert">
      <span class="error-mark" aria-hidden="true">!</span>
      <div><h2>报表数据暂不可用</h2><p>本次统计没有完整加载，因此不展示可能误导的零值。请检查服务状态后重试。</p></div>
      <el-button @click="load">重新加载</el-button>
    </section>

    <div v-else v-loading="loading" class="reports">
      <section class="card hero-card span-full">
        <div>
          <p class="eyebrow">实际数据看板</p>
          <h2>{{ boardTitle }}</h2>
          <p class="hero-copy">{{ boardDescription }}</p>
        </div>
        <div class="hero-signal">
          <strong>{{ isAllProjects ? globalProjectRows.reduce((total, row) => total + row.total, 0) : (projectData?.requirements?.length || 0) + (projectData?.tasks?.length || 0) + (projectData?.bugs?.length || 0) }}</strong>
          <span>实际工作项</span>
        </div>
      </section>

      <section class="metric-grid span-full">
        <article v-for="metric in metrics" :key="metric.label" class="metric-card">
          <span class="metric-value">{{ metric.value }}</span>
          <span class="metric-label">{{ metric.label }}</span>
          <small>{{ metric.note }}</small>
        </article>
      </section>

      <template v-if="isAllProjects">
        <section class="card span-2">
          <div class="section-head">
            <div><p class="eyebrow">全项目 · 状态</p><h3>交付脉搏</h3></div>
            <span class="section-note">来自实际状态记录</span>
          </div>
          <div class="status-grid">
            <div v-for="block in statusBlocks" :key="block.key" class="status-block">
              <p class="type-name">{{ block.label }}</p>
              <div class="bars">
                <div v-for="row in block.rows" :key="row.key" class="bar-row">
                  <span class="bar-label">{{ row.label }}</span>
                  <div class="bar-track"><div class="bar-fill" :style="{ width: (row.value / maxRows(block.rows) * 100) + '%' }" /></div>
                  <span class="bar-val">{{ row.value }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section class="card">
          <div class="section-head">
            <div><p class="eyebrow">全项目 · 风险</p><h3>缺陷压力</h3></div>
            <span class="section-note">按实际严重程度</span>
          </div>
          <div class="bars">
            <div v-for="row in globalRiskRows" :key="row.severity" class="bar-row">
              <span class="bar-label">{{ row.label }}</span>
              <div class="bar-track"><div class="bar-fill" :class="badgeClass('severity', row.severity)" :style="{ width: (row.cnt / maxRisk * 100) + '%' }" /></div>
              <span class="bar-val">{{ row.cnt }}</span>
            </div>
            <p v-if="!globalRiskRows.length" class="empty">当前没有缺陷记录</p>
          </div>
        </section>

        <section class="card span-full">
          <div class="section-head">
            <div><p class="eyebrow">项目实际内容</p><h3>项目推进</h3></div>
            <span class="section-note">按项目名称查看真实工作量</span>
          </div>
          <div class="table-frame">
            <el-table :data="globalProjectRows" size="small" stripe>
              <el-table-column prop="projectName" label="项目" min-width="180" />
              <el-table-column prop="reqCnt" label="需求" width="90" />
              <el-table-column prop="taskCnt" label="任务" width="90" />
              <el-table-column prop="bugCnt" label="缺陷" width="90" />
              <el-table-column label="需求完成" min-width="220">
                <template #default="{ row }">
                  <el-progress :percentage="row.completion.total ? Math.round(number(row.completion.doneCnt) / number(row.completion.total) * 100) : 0" :stroke-width="8" />
                </template>
              </el-table-column>
            </el-table>
          </div>
          <p v-if="!globalProjectRows.length" class="empty">还没有项目数据</p>
        </section>

        <section class="card">
          <div class="section-head">
            <div><p class="eyebrow">全项目 · 迭代</p><h3>迭代工作量</h3></div>
            <span class="section-note">最近 6 个 · 任务 + 缺陷</span>
          </div>
          <div v-if="globalSprintRows.length" class="sprint-load-list">
            <div v-for="row in globalSprintRows" :key="row.sprintId" class="sprint-load-row">
              <div class="sprint-load-heading">
                <span :title="row.sprintName">{{ row.sprintName }}</span>
                <strong>{{ row.total }}</strong>
              </div>
              <div class="sprint-load-track" aria-hidden="true">
                <span class="sprint-task-fill" :style="{ width: (number(row.taskCnt) / maxSprintLoad * 100) + '%' }" />
                <span class="sprint-bug-fill" :style="{ width: (number(row.bugCnt) / maxSprintLoad * 100) + '%' }" />
              </div>
              <small>{{ row.taskCnt }} 任务 <span>·</span> {{ row.bugCnt }} 缺陷</small>
            </div>
          </div>
          <p v-else class="empty">还没有迭代工作项</p>
        </section>

        <section class="card span-2">
          <div class="section-head">
            <div><p class="eyebrow">全项目 · 时间</p><h3>近 30 天新增内容</h3></div>
            <span class="section-note">需求 + 缺陷</span>
          </div>
          <div class="trend-chart">
            <div class="trend-bars" role="list" aria-label="近 30 天每天新增的需求与缺陷">
              <div v-for="row in trendRows" :key="row.day" class="trend-col" role="listitem" :aria-label="`${row.day}，新增 ${row.total} 条`" :title="`${row.day}：${row.total} 条`">
                <div class="trend-fill" :class="{ 'is-zero': row.total === 0 }" :style="{ height: (row.total ? Math.max(4, row.total / maxTrend * 130) : 2) + 'px' }" />
              </div>
            </div>
            <div class="trend-axis" aria-hidden="true">
              <span v-for="tick in trendTicks" :key="tick.day">{{ tick.label }}</span>
            </div>
          </div>
          <p v-if="!hasTrendData" class="empty">近 30 天没有新增需求或缺陷</p>
        </section>

        <section class="card">
          <div class="section-head">
            <div><p class="eyebrow">全项目 · 协作</p><h3>成员工作量</h3></div>
            <span class="section-note">任务 + 缺陷</span>
          </div>
          <div class="bars">
            <div v-for="row in globalData.assignee.slice(0, 7)" :key="row.assignee" class="bar-row">
              <span class="bar-label">{{ row.assignee }}</span>
              <div class="bar-track"><div class="bar-fill" :style="{ width: (row.cnt / Math.max(1, ...globalData.assignee.map((item) => number(item.cnt))) * 100) + '%' }" /></div>
              <span class="bar-val">{{ row.cnt }}</span>
            </div>
            <p v-if="!globalData.assignee.length" class="empty">暂无负责人数据</p>
          </div>
        </section>
      </template>

      <template v-else>
        <section class="card span-2">
          <div class="section-head">
            <div><p class="eyebrow">{{ projectName }} · 状态</p><h3>交付脉搏</h3></div>
            <span class="section-note">需求 / 开发 / 缺陷</span>
          </div>
          <div class="status-grid">
            <div v-for="block in statusBlocks" :key="block.key" class="status-block">
              <p class="type-name">{{ block.label }}</p>
              <div class="bars">
                <div v-for="row in block.rows" :key="row.key" class="bar-row">
                  <span class="bar-label">{{ row.label }}</span>
                  <div class="bar-track"><div class="bar-fill" :style="{ width: (row.value / maxRows(block.rows) * 100) + '%' }" /></div>
                  <span class="bar-val">{{ row.value }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section class="card">
          <div class="section-head">
            <div><p class="eyebrow">{{ projectName }} · 节奏</p><h3>当前迭代</h3></div>
            <span class="section-note">真实迭代记录</span>
          </div>
          <div v-if="selectedSprint" class="focus-block">
            <div class="focus-title"><strong>{{ selectedSprint.name }}</strong><span class="badge" :class="badgeClass('sprint', selectedSprint.status)">{{ sprintStatus[selectedSprint.status] }}</span></div>
            <p>{{ selectedSprint.goal || '暂无迭代目标' }}</p>
            <el-progress :percentage="number(selectedSprint.progress)" :stroke-width="8" />
            <small>{{ selectedSprint.workDone || 0 }}/{{ selectedSprint.workTotal || 0 }} 个工作项完成 · {{ selectedSprint.startDate || '—' }} ~ {{ selectedSprint.endDate || '—' }}</small>
          </div>
          <p v-else class="empty">当前没有迭代记录</p>
        </section>

        <section class="card span-full">
          <div class="section-head">
            <div><p class="eyebrow">{{ projectName }} · 工作项</p><h3>最近实际内容</h3></div>
            <span class="section-note">按最近更新排序</span>
          </div>
          <ul v-if="projectActivity.length" class="activity-list">
            <li v-for="item in projectActivity" :key="item.id">
              <span class="activity-kind">{{ item.kind }}</span>
              <span class="activity-title">{{ item.title }}</span>
              <span class="activity-meta">{{ item.meta }}</span>
              <span class="activity-time">{{ formatDate(item.time) }}</span>
              <span class="badge" :class="badgeClass(item.type, item.status)">{{ item.statusLabel }}</span>
            </li>
          </ul>
          <p v-else class="empty">这个项目还没有需求、任务或缺陷记录</p>
        </section>

        <section class="card">
          <div class="section-head">
            <div><p class="eyebrow">{{ projectName }} · 质量</p><h3>风险缺陷</h3></div>
            <span class="section-note">只显示未关闭项</span>
          </div>
          <ul v-if="projectRisks.length" class="risk-list">
            <li v-for="bug in projectRisks" :key="bug.id">
              <span class="risk-dot" :class="badgeClass('severity', bug.severity)" />
              <span class="risk-title">{{ bug.title }}</span>
              <span class="badge" :class="badgeClass('severity', bug.severity)">{{ bugSeverity[bug.severity] || bug.severity }}</span>
              <small>{{ bug.assignee || '未指派' }}</small>
            </li>
          </ul>
          <p v-else class="empty">当前没有未关闭缺陷</p>
        </section>

        <section class="card">
          <div class="section-head">
            <div><p class="eyebrow">{{ projectName }} · 交付</p><h3>测试与发布</h3></div>
            <span class="section-note">真实节点状态</span>
          </div>
          <div class="delivery-list">
            <div class="delivery-row"><span>测试计划</span><strong>{{ selectedPlan?.name || '暂无计划' }}</strong><em v-if="selectedPlan">{{ testPlanStatus[selectedPlan.status] || selectedPlan.status }}</em></div>
            <div class="delivery-row"><span>下一节点</span><strong>{{ selectedMilestone?.name || '暂无未完成里程碑' }}</strong><em v-if="selectedMilestone">{{ milestoneStatus[selectedMilestone.status] || selectedMilestone.status }} · {{ selectedMilestone.dueDate ? formatDate(selectedMilestone.dueDate) : '未排期' }}</em></div>
            <div class="delivery-row"><span>最近发布</span><strong>{{ selectedRelease?.version || '暂无发布记录' }}</strong><em v-if="selectedRelease">{{ releaseStatus[selectedRelease.status] || selectedRelease.status }}</em></div>
          </div>
        </section>

        <section class="card span-full">
          <div class="section-head">
            <div><p class="eyebrow">{{ projectName }} · 协作</p><h3>成员工作量</h3></div>
            <span class="section-note">项目成员 {{ projectData?.members?.length || 0 }} 人</span>
          </div>
          <div v-if="memberLoad.length" class="member-grid">
            <div v-for="row in memberLoad" :key="row.name" class="member-row">
              <span>{{ row.name }}</span>
              <div class="bar-track"><div class="bar-fill" :style="{ width: (row.count / maxMemberLoad * 100) + '%' }" /></div>
              <strong>{{ row.count }}</strong>
            </div>
          </div>
          <p v-else class="empty">还没有任务或缺陷负责人数据</p>
        </section>
      </template>
    </div>
  </PageShell>
</template>

<style scoped>
.reports { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; }
.span-full { grid-column: 1 / -1; }
.span-2 { grid-column: span 2; }
.card { min-width: 0; padding: 20px; background: rgba(12, 12, 12, .78); border: 1px solid var(--border); border-radius: var(--radius-lg); box-shadow: none; }
.hero-card { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; min-height: 168px; background: linear-gradient(135deg, rgba(255,255,255,.07), rgba(12,12,12,.72)); }
.eyebrow { margin-bottom: 8px; color: var(--accent); font-size: 10px; letter-spacing: .16em; text-transform: uppercase; }
.hero-card h2 { max-width: 820px; font-family: var(--font-display); font-size: clamp(2rem, 4vw, 4.2rem); font-weight: 400; letter-spacing: -.06em; line-height: .98; }
.hero-copy { max-width: 680px; margin-top: 14px; color: var(--muted); font-size: 13px; }
.hero-signal { display: flex; flex-direction: column; align-items: flex-end; flex: 0 0 auto; color: var(--muted); }
.hero-signal strong { color: var(--accent); font-family: var(--font-display); font-size: 4rem; font-weight: 400; line-height: .9; }
.hero-signal span { margin-top: 10px; font-size: 11px; letter-spacing: .12em; }
.metric-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 1px; border: 1px solid var(--border); background: var(--border); }
.metric-card { min-height: 120px; padding: 18px; background: rgba(10, 10, 10, .88); }
.metric-value { display: block; color: var(--text); font-family: var(--font-display); font-size: 2.2rem; line-height: 1; }
.metric-label { display: block; margin-top: 18px; color: var(--muted); font-size: 12px; letter-spacing: .08em; }
.metric-card small { display: block; margin-top: 7px; color: var(--muted-light); font-size: 11px; }
.section-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 14px; margin-bottom: 18px; }
.section-head h3 { font-size: 17px; font-weight: 600; }
.section-note { color: var(--muted); font-size: 11px; white-space: nowrap; }
.status-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 18px; }
.type-name { margin-bottom: 10px; color: var(--cyan); font-size: 13px; }
.bars { display: flex; flex-direction: column; gap: 9px; }
.bar-row { display: grid; grid-template-columns: 70px 1fr 34px; gap: 8px; align-items: center; min-width: 0; font-size: 12px; }
.bar-label { overflow: hidden; color: var(--muted); text-overflow: ellipsis; white-space: nowrap; }
.bar-track { height: 5px; overflow: hidden; border-radius: 999px; background: rgba(234, 238, 222, .1); }
.bar-fill { height: 100%; border-radius: 4px; background: linear-gradient(90deg, var(--cyan), var(--purple)); }
.bar-fill.lime { background: var(--lime); }
.bar-fill.pink { background: var(--pink); }
.bar-fill.cyan { background: var(--cyan); }
.bar-fill.muted { background: var(--muted); }
.bar-val { color: var(--text); text-align: right; }
.sprint-load-list { display: flex; flex-direction: column; gap: 13px; }
.sprint-load-heading { display: flex; justify-content: space-between; gap: 10px; margin-bottom: 5px; font-size: 12px; }
.sprint-load-heading span { overflow: hidden; color: var(--text); text-overflow: ellipsis; white-space: nowrap; }
.sprint-load-heading strong { color: var(--text); font-weight: 500; }
.sprint-load-track { display: flex; height: 5px; overflow: hidden; border-radius: 999px; background: rgba(234, 238, 222, .1); }
.sprint-load-track span { height: 100%; }
.sprint-task-fill { background: var(--cyan); }
.sprint-bug-fill { background: var(--pink); }
.sprint-load-row small { display: block; margin-top: 4px; color: var(--muted); font-size: 10px; }
.sprint-load-row small span { padding: 0 3px; color: var(--muted-light); }
.table-frame { margin-top: 4px; }
.trend-bars { display: flex; align-items: flex-end; gap: 3px; height: 140px; padding-top: 10px; border-bottom: 1px solid var(--border); }
.trend-col { display: flex; align-items: flex-end; flex: 1; height: 100%; min-width: 2px; }
.trend-fill { width: 100%; min-height: 4px; border-radius: 3px 3px 0 0; background: linear-gradient(180deg, var(--cyan), var(--purple)); }
.trend-fill.is-zero { min-height: 0; background: rgba(234, 238, 222, .18); }
.trend-axis { display: flex; justify-content: space-between; margin-top: 7px; color: var(--muted-light); font-size: 10px; font-variant-numeric: tabular-nums; }
.focus-block { padding: 16px; border: 1px solid var(--border); background: rgba(255, 255, 255, .025); }
.focus-title { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.focus-block p { min-height: 36px; margin: 14px 0; color: var(--muted); font-size: 12px; }
.focus-block small { display: block; margin-top: 9px; color: var(--muted-light); font-size: 11px; }
.activity-list, .risk-list { display: flex; flex-direction: column; gap: 0; border-top: 1px solid var(--border); }
.activity-list li, .risk-list li { display: grid; align-items: center; gap: 12px; padding: 13px 0; border-bottom: 1px solid var(--border); }
.activity-list li { grid-template-columns: 42px minmax(0, 1.4fr) minmax(130px, 1fr) 120px auto; }
.activity-kind { color: var(--cyan); font-size: 11px; }
.activity-title, .risk-title { overflow: hidden; color: var(--text); text-overflow: ellipsis; white-space: nowrap; }
.activity-meta, .activity-time, .risk-list small { overflow: hidden; color: var(--muted); font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }
.risk-list li { grid-template-columns: 8px minmax(0, 1fr) auto 70px; }
.risk-dot { width: 7px; height: 7px; border-radius: 50%; background: var(--muted); }
.risk-dot.pink { background: var(--pink); box-shadow: 0 0 12px rgba(239, 239, 235, .3); }
.risk-dot.cyan { background: var(--cyan); }
.delivery-list { display: flex; flex-direction: column; gap: 0; }
.delivery-row { display: grid; grid-template-columns: 70px minmax(0, 1fr) auto; gap: 12px; align-items: center; padding: 14px 0; border-bottom: 1px solid var(--border); font-size: 12px; }
.delivery-row:first-child { padding-top: 0; }
.delivery-row:last-child { padding-bottom: 0; border-bottom: 0; }
.delivery-row span, .delivery-row em { color: var(--muted); font-style: normal; }
.delivery-row strong { overflow: hidden; color: var(--text); font-weight: 500; text-overflow: ellipsis; white-space: nowrap; }
.member-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px 28px; }
.member-row { display: grid; grid-template-columns: 110px 1fr 30px; gap: 10px; align-items: center; font-size: 12px; }
.member-row > span { overflow: hidden; color: var(--muted); text-overflow: ellipsis; white-space: nowrap; }
.member-row strong { color: var(--text); text-align: right; }
.empty { padding: 24px 0; color: var(--muted); text-align: center; font-size: 12px; }
.report-actions { display: flex; align-items: center; gap: 8px; }
.project-select { width: 180px; }
.load-error { display: flex; min-height: 180px; align-items: center; gap: 18px; padding: 28px; border: 1px solid var(--border); border-radius: var(--radius-lg); background: rgba(255,255,255,.018); }
.error-mark { display: grid; width: 42px; height: 42px; flex: 0 0 auto; place-items: center; border: 1px solid var(--border); border-radius: 50%; color: var(--accent); font-family: var(--font-display); font-size: 18px; }
.load-error h2 { margin: 0 0 8px; font-size: 15px; font-weight: 500; }
.load-error p { max-width: 560px; margin: 0; color: var(--muted); font-size: 12px; line-height: 1.7; }
.load-error :deep(.el-button) { flex: 0 0 auto; margin-left: auto; }
@media (max-width: 1080px) {
  .reports { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .span-2 { grid-column: 1 / -1; }
}
@media (max-width: 720px) {
  .load-error { align-items: flex-start; flex-wrap: wrap; padding: 20px; }
  .load-error :deep(.el-button) { margin-left: 60px; }
  .hero-card { align-items: flex-start; flex-direction: column; }
  .hero-signal { align-items: flex-start; }
  .metric-grid, .status-grid, .member-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .activity-list li { grid-template-columns: 42px minmax(0, 1fr) auto; }
  .activity-meta, .activity-time { display: none; }
  .report-actions { width: 100%; }
  .project-select { flex: 1; }
}
@media (max-width: 520px) {
  .reports { grid-template-columns: 1fr; }
  .metric-grid, .status-grid, .member-grid { grid-template-columns: 1fr; }
  .span-2, .span-full { grid-column: auto; }
  .section-head { flex-direction: column; gap: 5px; }
  .section-note { white-space: normal; }
  .activity-list li { grid-template-columns: 38px minmax(0, 1fr) auto; gap: 8px; }
  .risk-list li { grid-template-columns: 8px minmax(0, 1fr) auto; }
  .risk-list small { display: none; }
  .delivery-row { grid-template-columns: 62px minmax(0, 1fr); }
  .delivery-row em { grid-column: 2; }
}
</style>
