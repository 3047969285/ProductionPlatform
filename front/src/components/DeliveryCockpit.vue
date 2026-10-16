<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { gsap } from 'gsap'
import api from '../api'
import { bugSeverity, bugStatus, milestoneStatus, releaseEnv, releaseStatus, taskStatus } from '../constants'
import { getDeliveryRecommendation } from '../deliveryRecommendation'

const props = defineProps({ projectId: { type: Number, required: true } })
const emit = defineEmits(['navigate', 'edit-item', 'edit-delivery-node'])

const root = ref(null)
const loading = ref(false)
const refreshing = ref(false)
const errors = ref([])
const data = ref({
  requirements: [],
  tasks: [],
  bugs: [],
  sprints: [],
  testPlans: [],
  milestones: [],
  releases: [],
})
let animationContext

const collections = [
  ['requirements', '/requirements'],
  ['tasks', '/tasks'],
  ['bugs', '/bugs'],
  ['sprints', '/sprints'],
  ['testPlans', '/test-plans'],
  ['milestones', '/milestones'],
  ['releases', '/releases'],
]
const collectionNames = {
  requirements: '需求',
  tasks: '任务',
  bugs: '缺陷',
  sprints: '迭代',
  testPlans: '测试计划',
  milestones: '里程碑',
  releases: '发布记录',
}

const today = computed(() => new Date())
const failedCollections = computed(() => errors.value.map(({ key }) => collectionNames[key] || key))
let loadRequestId = 0

function listOf(key) {
  return Array.isArray(data.value[key]) ? data.value[key] : []
}

function isClosedBug(item) {
  return ['resolved', 'closed'].includes(item.status)
}

function isDoneRequirement(item) {
  return item.status === 'done'
}

function isDoneTask(item) {
  return item.status === 'done'
}

function isOverdue(item) {
  if (item?.status === 'overdue') return true
  if (!item?.dueDate || item.status === 'done') return false
  const due = new Date(`${item.dueDate}T23:59:59`)
  return !Number.isNaN(due.getTime()) && due < today.value
}

const requirements = computed(() => listOf('requirements'))
const tasks = computed(() => listOf('tasks'))
const bugs = computed(() => listOf('bugs'))
const sprints = computed(() => listOf('sprints'))
const testPlans = computed(() => listOf('testPlans'))
const milestones = computed(() => listOf('milestones'))
const releases = computed(() => listOf('releases'))

const activeSprint = computed(() => sprints.value.find((item) => item.status === 'active'))
const openBugs = computed(() => bugs.value.filter((item) => !isClosedBug(item)))
const seriousBugs = computed(() => openBugs.value.filter((item) => ['high', 'critical'].includes(item.severity)))
const overdueMilestones = computed(() => milestones.value.filter(isOverdue))
const pendingTasks = computed(() => tasks.value.filter((item) => item.status !== 'done'))
const unassignedTasks = computed(() => pendingTasks.value.filter((item) => !item.assignee))
const latestRelease = computed(() => releases.value[0])
const nextMilestone = computed(() => milestones.value
  .filter((item) => item.status !== 'done' && !isOverdue(item))
  .sort((a, b) => {
    const aDate = a.dueDate ? String(a.dueDate) : '9999-12-31'
    const bDate = b.dueDate ? String(b.dueDate) : '9999-12-31'
    return aDate.localeCompare(bDate)
  })[0])
const completedMilestones = computed(() => milestones.value.filter((item) => item.status === 'done').length)

const deliveryWorkTotal = computed(() => requirements.value.length + tasks.value.length)
const deliveryWorkDone = computed(() => requirements.value.filter(isDoneRequirement).length
  + tasks.value.filter(isDoneTask).length)
const progress = computed(() => deliveryWorkTotal.value
  ? Math.round(deliveryWorkDone.value / deliveryWorkTotal.value * 100)
  : null)

const milestoneMetric = computed(() => {
  if (overdueMilestones.value.length) {
    return {
      value: '逾期',
      note: `${overdueMilestones.value.length} 个节点需要调整`,
      tone: 'pink',
    }
  }
  if (nextMilestone.value) {
    return {
      value: nextMilestone.value.dueDate ? formatShortDate(nextMilestone.value.dueDate) : '待定',
      note: `下个：${nextMilestone.value.name || '未命名里程碑'}`,
      tone: 'green',
    }
  }
  if (milestones.value.length && completedMilestones.value === milestones.value.length) {
    return {
      value: '已完成',
      note: `${completedMilestones.value} 个节点全部完成`,
      tone: 'green',
    }
  }
  return { value: '未设置', note: '暂无可跟踪的交付节点', tone: 'muted' }
})

const health = computed(() => {
  if (errors.value.length) {
    return { tone: 'warning', label: '数据未完整', hint: '部分数据暂不可用' }
  }
  if (overdueMilestones.value.length || seriousBugs.value.some((item) => item.severity === 'critical')) {
    return { tone: 'danger', label: '需要关注', hint: '逾期 / 紧急' }
  }
  if (seriousBugs.value.length || unassignedTasks.value.length) {
    return { tone: 'warning', label: '有待处理', hint: '高优 / 待分配' }
  }
  return { tone: 'good', label: '节奏正常', hint: '无明显阻塞' }
})

const stats = computed(() => [
  {
    key: 'progress', label: '交付进度', value: progress.value ?? '—',
    suffix: progress.value === null ? '' : '%',
    note: deliveryWorkTotal.value
      ? `${deliveryWorkDone.value} / ${deliveryWorkTotal.value} 项需求与任务完成`
      : '录入需求或任务后计算',
    tone: 'blue', tab: 'req',
  },
  { key: 'tasks', label: '待办任务', value: pendingTasks.value.length, note: `${tasks.value.filter(isDoneTask).length} 项完成`, tone: 'purple', tab: 'task' },
  { key: 'bugs', label: '待处理缺陷', value: openBugs.value.length, note: `${seriousBugs.value.length} 项高优`, tone: 'pink', tab: 'bug' },
  { key: 'milestones', label: '下个里程碑', ...milestoneMetric.value, tab: 'release' },
])

const workflowRecommendation = computed(() => getDeliveryRecommendation({
  requirements: requirements.value,
  tasks: tasks.value,
  sprints: sprints.value,
  testPlans: testPlans.value,
  releases: releases.value,
  unavailable: errors.value.map(({ key }) => key),
}))

const nextActions = computed(() => {
  const actions = []
  seriousBugs.value.slice(0, 2).forEach((item) => actions.push({
    key: `bug-${item.id}`,
    workType: 'bug',
    workId: item.id,
    tone: item.severity === 'critical' ? 'danger' : 'warning',
    title: `处理${bugSeverity[item.severity] || ''}缺陷：${item.title}`,
    description: item.assignee ? `负责人：${item.assignee}` : '还没有负责人，建议先指派处理人',
    tab: 'bug',
  }))
  overdueMilestones.value.slice(0, 1).forEach((item) => actions.push({
    key: `milestone-${item.id}`,
    tone: 'danger',
    title: `调整逾期里程碑：${item.name}`,
    description: `原计划日期：${formatDate(item.dueDate)}`,
    tab: 'release',
  }))
  unassignedTasks.value.slice(0, 2).forEach((item) => actions.push({
    key: `task-${item.id}`,
    workType: 'task',
    workId: item.id,
    tone: 'neutral',
    title: `给任务补负责人：${item.title}`,
    description: `状态：${taskStatus[item.status] || item.status}`,
    tab: 'task',
  }))
  if (workflowRecommendation.value && !actions.some((item) => item.tab === workflowRecommendation.value.tab)) {
    actions.push(workflowRecommendation.value)
  }
  return actions.slice(0, 4)
})

const releaseSummary = computed(() => {
  if (!latestRelease.value) return '还没有发布记录'
  const env = releaseEnv[latestRelease.value.environment] || latestRelease.value.environment || '未知环境'
  const status = releaseStatus[latestRelease.value.status] || latestRelease.value.status || '未知状态'
  return `${latestRelease.value.version || '未命名版本'} · ${env} · ${status}`
})

const timeline = computed(() => [
  ...milestones.value.slice(0, 3).map((item) => ({
    key: `m-${item.id}`,
    kind: 'milestone',
    id: item.id,
    type: '里程碑',
    title: item.name,
    date: item.dueDate,
    status: milestoneStatus[item.status] || item.status,
    tone: isOverdue(item) ? 'danger' : item.status === 'done' ? 'good' : 'blue',
  })),
  ...releases.value.slice(0, 2).map((item) => ({
    key: `r-${item.id}`,
    kind: 'release',
    id: item.id,
    type: '发布',
    title: item.version,
    date: item.releasedAt || item.createdAt,
    status: releaseStatus[item.status] || item.status,
    tone: item.status === 'rollback' ? 'danger' : item.status === 'done' ? 'good' : 'blue',
  })),
].sort((a, b) => String(a.date || '').localeCompare(String(b.date || ''))).slice(0, 5))

function formatDate(value) {
  if (!value) return '未设置'
  return String(value).slice(0, 10)
}

function formatShortDate(value) {
  const date = formatDate(value)
  return date.length >= 10 ? date.slice(5).replace('-', '/') : date
}

async function load(showLoading = true) {
  const requestId = ++loadRequestId
  if (showLoading) loading.value = true
  else refreshing.value = true
  errors.value = []
  const results = await Promise.all(collections.map(async ([key, path]) => {
    try {
      const response = await api.get(path, { params: { projectId: props.projectId } })
      return { key, value: Array.isArray(response.data) ? response.data : [] }
    } catch {
      return { key, error: true }
    }
  }))
  if (requestId !== loadRequestId) return

  const next = {}
  errors.value = []
  results.forEach(({ key, value, error }) => {
    if (error) errors.value.push({ key })
    else next[key] = value
  })
  data.value = { ...data.value, ...next }
  loading.value = false
  refreshing.value = false
  if (errors.value.length === collections.length) ElMessage.error('交付数据暂时无法加载，请检查服务状态')
  await nextTick()
  animateCards()
}

function go(tab) {
  emit('navigate', tab)
}

function activateAction(item) {
  if (item.workType && item.workId != null) {
    emit('edit-item', item.workType, item.workId)
    return
  }
  go(item.tab)
}

function editTimelineItem(item) {
  emit('edit-delivery-node', item.kind, item.id)
}

function setupAnimations() {
  if (!root.value) return
  animationContext = gsap.context(() => {
    const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    if (reduced) return
    gsap.from('.cockpit-hero, .stat-card, .cockpit-section', {
      autoAlpha: 0,
      y: 18,
      duration: 0.55,
      stagger: 0.06,
      ease: 'power3.out',
      clearProps: 'all',
    })
  }, root.value)
}

function animateCards() {
  if (!root.value || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  gsap.fromTo(root.value.querySelectorAll('.stat-card'), { y: 8 }, { y: 0, duration: 0.35, stagger: 0.04, ease: 'power2.out', overwrite: 'auto' })
}

onMounted(() => {
  setupAnimations()
  load()
})

watch(() => props.projectId, () => {
  data.value = {
    requirements: [],
    tasks: [],
    bugs: [],
    sprints: [],
    testPlans: [],
    milestones: [],
    releases: [],
  }
  errors.value = []
  load()
})

onUnmounted(() => {
  loadRequestId += 1
  animationContext?.revert()
})
</script>

<template>
  <div ref="root" class="cockpit" v-loading="loading">
    <section class="cockpit-hero">
      <div>
        <p class="eyebrow">PDE DELIVERY COCKPIT</p>
        <h2>交付驾驶舱</h2>
        <p class="hero-copy">状态 / 下一步</p>
      </div>
      <div class="health" :class="health.tone">
        <span class="health-dot" />
        <div>
          <strong>{{ health.label }}</strong>
          <small>{{ health.hint }}</small>
        </div>
      </div>
    </section>

    <section class="stats" aria-label="交付概览">
      <button
        v-for="item in stats"
        :key="item.key"
        class="stat-card"
        :class="item.tone"
        type="button"
        @click="go(item.tab)"
      >
        <span class="stat-label">{{ item.label }}</span>
        <strong v-count="{ value: item.value, suffix: item.suffix }">{{ item.value }}</strong>
        <small>{{ item.note }}</small>
      </button>
    </section>

    <div class="cockpit-columns">
      <section class="cockpit-section next-section">
        <div class="section-heading">
          <div>
            <p class="eyebrow">PROJECT FLOW / LIVE</p>
            <h3>交付下一步</h3>
          </div>
          <div class="section-tools">
            <span class="section-count" :aria-label="`${nextActions.length} 条建议`">{{ nextActions.length }}</span>
            <el-button text @click="load(false)" :loading="refreshing">刷新</el-button>
          </div>
        </div>
        <div v-if="nextActions.length" class="next-list">
          <button v-for="item in nextActions" :key="item.key" class="next-item" :class="[item.tone, { 'is-featured': item.featured }]" type="button" @click="activateAction(item)">
            <span class="next-marker" />
            <span class="next-copy">
              <small v-if="item.stage" class="next-stage">{{ item.stage }} · 建议下一步</small>
              <strong>{{ item.title }}</strong>
              <small>{{ item.description }}</small>
            </span>
            <span class="action-arrow">→</span>
          </button>
        </div>
        <div v-else-if="errors.length" class="all-clear incomplete">
          <span>!</span>
          <div><strong>数据未完整，暂不能判断</strong><small>刷新成功后会给出对应阶段建议</small></div>
        </div>
        <div v-else class="all-clear">
          <span>✓</span>
          <div><strong>当前没有待处理事项</strong><small>交付数据已完整读取</small></div>
        </div>
      </section>

      <section class="cockpit-section status-section">
        <div class="section-heading">
          <div>
            <p class="eyebrow">DELIVERY PULSE</p>
            <h3>节奏</h3>
          </div>
        </div>
        <div class="pulse-list">
          <div class="pulse-row"><span>当前迭代</span><strong>{{ activeSprint?.name || '尚未开始' }}</strong></div>
          <div class="pulse-row"><span>测试计划</span><strong>{{ testPlans.length ? `${testPlans.length} 个 · ${testPlans.filter(item => item.status === 'running').length} 个执行中` : '尚未建立' }}</strong></div>
          <div class="pulse-row"><span>最近发布</span><strong>{{ releaseSummary }}</strong></div>
          <div class="pulse-row"><span>待分配任务</span><strong :class="{ dangerText: unassignedTasks.length }">{{ unassignedTasks.length }} 项</strong></div>
        </div>
      </section>
    </div>

    <section v-if="timeline.length" class="cockpit-section timeline-section">
      <div class="section-heading">
        <div>
          <p class="eyebrow">MILESTONES & RELEASES</p>
          <h3>节点</h3>
        </div>
        <el-button text @click="go('release')">管理交付节点 →</el-button>
      </div>
      <div class="timeline">
        <button v-for="item in timeline" :key="item.key" class="timeline-item" type="button" :aria-label="`编辑${item.type}：${item.title}`" @click="editTimelineItem(item)">
          <span class="timeline-dot" :class="item.tone" />
          <div><small>{{ item.type }} · {{ formatDate(item.date) }}</small><strong>{{ item.title }}</strong></div>
          <span class="badge" :class="item.tone === 'danger' ? 'pink' : item.tone === 'good' ? 'lime' : 'cyan'">{{ item.status }}</span>
        </button>
      </div>
    </section>

    <el-alert v-if="errors.length" class="load-alert" type="warning" :closable="false" show-icon>
      <span>未读取：{{ failedCollections.join('、') }}。已保留这些数据上次成功读取的内容。</span>
      <el-button link type="warning" :loading="refreshing" @click="load(false)">重新读取</el-button>
    </el-alert>
  </div>
</template>

<style scoped>
.cockpit { display: flex; flex-direction: column; gap: 16px; }
.cockpit-hero {
  display: flex; justify-content: space-between; align-items: flex-start; gap: 20px;
  padding: 6px 2px 10px;
}
.eyebrow { color: var(--accent); font-size: 11px; font-weight: 700; letter-spacing: .16em; margin-bottom: 6px; }
.cockpit h2 { font-family: var(--font-display); font-size: clamp(1.45rem, 3vw, 2rem); letter-spacing: -.03em; margin-bottom: 6px; }
.hero-copy { color: var(--muted); font-size: 14px; }
.health { display: flex; align-items: center; gap: 10px; min-width: 190px; padding: 12px 14px; border: 1px solid var(--border); border-radius: var(--radius); background: rgba(23, 27, 25, .78); }
.health strong, .health small { display: block; }
.health strong { font-size: 14px; }
.health small { color: var(--muted); font-size: 12px; margin-top: 2px; }
.health-dot { width: 10px; height: 10px; border-radius: 50%; background: var(--lime); box-shadow: 0 0 0 5px rgba(52,199,89,.12); }
.health { position: relative; }
.health.good .health-dot { animation: health-breathe 3.6s var(--ease) infinite; }
@keyframes health-breathe {
  0%, 100% { box-shadow: 0 0 0 5px rgba(52,199,89,.12); }
  50% { box-shadow: 0 0 0 9px rgba(52,199,89,.04); }
}
.health.warning .health-dot { background: var(--orange); box-shadow: 0 0 0 5px rgba(255,159,10,.14); }
.health.danger .health-dot { background: var(--pink); box-shadow: 0 0 0 5px rgba(255,55,95,.12); }
.stats { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; }
.stat-card { position: relative; width: 100%; min-height: 118px; padding: 16px; border: 1px solid var(--border); border-top: 1px solid var(--accent); border-radius: var(--radius); background: rgba(23, 27, 25, .84); box-shadow: var(--shadow-sm); color: var(--text); font: inherit; text-align: left; cursor: pointer; transition: transform .25s var(--ease), border-color .25s var(--ease), box-shadow .25s var(--ease), background .25s var(--ease); }
.stat-card:hover { transform: translateY(-3px); border-color: var(--border-strong); background: rgba(29, 34, 31, .96); box-shadow: var(--shadow-md); }
.stat-card:focus-visible { outline: 2px solid var(--accent); outline-offset: 3px; }
.stat-card.purple { border-top-color: var(--purple); }
.stat-card.pink { border-top-color: var(--pink); }
.stat-card.green { border-top-color: var(--lime); }
.stat-card.muted { border-top-color: var(--muted); }
.stat-label, .stat-card small { display: block; color: var(--muted); }
.stat-label { font-size: 13px; margin-bottom: 8px; }
.stat-card strong { display: block; font-family: var(--font-display); font-size: 1.75rem; line-height: 1.1; margin-bottom: 8px; }
.stat-card small { font-size: 12px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.cockpit-section { padding: 18px; border: 1px solid var(--border); border-radius: var(--radius-lg); background: rgba(23, 27, 25, .78); box-shadow: var(--shadow-sm); }
.section-heading { display: flex; justify-content: space-between; align-items: center; gap: 12px; margin-bottom: 14px; }
.section-heading h3 { font-size: 17px; font-weight: 700; }
.section-tools { display: flex; align-items: center; gap: 8px; }
.section-count { display: inline-grid; place-items: center; min-width: 26px; height: 26px; padding: 0 8px; border: 1px solid var(--border-strong); border-radius: 999px; color: var(--accent); background: rgba(239, 239, 235, .08); font-size: 13px; font-weight: 700; }
.next-item { width: 100%; border: 1px solid var(--border); background: var(--surface); text-align: left; cursor: pointer; font: inherit; transition: transform .25s var(--ease), border-color .25s var(--ease), box-shadow .25s var(--ease), background .25s var(--ease); }
.next-item:hover { transform: translateY(-2px); border-color: rgba(239, 239, 235, .5); box-shadow: var(--shadow-md); }
.next-item.is-featured { border-color: rgba(239, 239, 235, .24); background: linear-gradient(115deg, rgba(239, 239, 235, .08), var(--surface) 68%); }
.next-item.is-featured:hover { border-color: var(--accent); }
.next-copy strong, .next-copy small { display: block; }
.next-copy { min-width: 0; }
.next-copy strong { margin-bottom: 2px; font-size: 14px; line-height: 1.35; overflow-wrap: anywhere; }
.next-copy small { color: var(--muted); font-size: 12px; line-height: 1.45; }
.next-stage { width: fit-content; margin-bottom: 3px; color: var(--accent) !important; font-size: 10px !important; letter-spacing: .1em; }
.action-arrow { color: var(--muted-light); font-size: 18px; transition: transform .2s var(--ease); }
.next-item:hover .action-arrow { transform: translateX(3px); color: var(--accent); }
.cockpit-columns { display: grid; grid-template-columns: minmax(0, 1.15fr) minmax(300px, .85fr); gap: 16px; }
.next-list { display: flex; flex-direction: column; gap: 8px; }
.next-item { display: grid; grid-template-columns: 10px 1fr 18px; gap: 10px; align-items: center; padding: 12px; border-radius: 12px; }
.next-marker { width: 8px; height: 8px; border-radius: 50%; background: var(--accent); box-shadow: 0 0 0 4px rgba(239, 239, 235, .1); }
.next-item.warning .next-marker { background: var(--orange); box-shadow: 0 0 0 4px rgba(255,159,10,.13); }
.next-item.danger .next-marker { background: var(--pink); box-shadow: 0 0 0 4px rgba(255,55,95,.12); }
.all-clear { display: flex; gap: 12px; align-items: center; min-height: 134px; padding: 16px; border-radius: 12px; background: rgba(52,199,89,.08); color: #248a3d; }
.all-clear.incomplete { color: var(--orange); background: rgba(255,159,10,.08); }
.all-clear.incomplete > span { background: rgba(255,159,10,.15); }
.all-clear > span { display: grid; place-items: center; width: 34px; height: 34px; border-radius: 50%; background: rgba(52,199,89,.15); font-weight: 700; }
.all-clear strong, .all-clear small { display: block; }
.all-clear strong { font-size: 14px; }
.all-clear small { color: var(--muted); font-size: 12px; margin-top: 3px; }
.pulse-list { display: flex; flex-direction: column; gap: 0; }
.pulse-row { display: flex; justify-content: space-between; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border); transition: background .3s var(--ease), border-color .3s var(--ease); }
.pulse-row:hover { background: rgba(255, 255, 255, .022); border-bottom-color: var(--border-strong); }
.pulse-row:first-child { padding-top: 2px; }
.pulse-row:last-child { border-bottom: 0; padding-bottom: 2px; }
.pulse-row span { color: var(--muted); font-size: 13px; }
.pulse-row strong { max-width: 62%; font-size: 13px; text-align: right; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.dangerText { color: var(--pink); }
.timeline { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 8px; }
.timeline-item { display: flex; align-items: flex-start; justify-content: space-between; gap: 8px; width: 100%; position: relative; min-height: 84px; padding: 10px 10px 8px 15px; border: 0; border-left: 1px solid var(--border-strong); background: transparent; color: inherit; font: inherit; text-align: left; cursor: pointer; transition: border-color .35s var(--ease), transform .4s var(--ease); }
.timeline-item:hover { border-left-color: rgba(239, 239, 235, .62); transform: translateY(-3px); }
.timeline-item:focus-visible { outline: 2px solid var(--accent); outline-offset: 3px; border-radius: 4px; }
.timeline-item > div { flex: 1; min-width: 0; }
.timeline-dot { position: absolute; left: -5px; top: 12px; width: 9px; height: 9px; border-radius: 50%; background: var(--accent); box-shadow: 0 0 0 4px rgba(239, 239, 235, .1); transition: box-shadow .4s var(--ease); }
.timeline-item:hover .timeline-dot { box-shadow: 0 0 0 8px rgba(239, 239, 235, .09); }
.timeline-dot.good { background: var(--lime); box-shadow: 0 0 0 4px rgba(52,199,89,.12); }
.timeline-dot.danger { background: var(--pink); box-shadow: 0 0 0 4px rgba(255,55,95,.12); }
.timeline-item small, .timeline-item strong { display: block; }
.timeline-item small { color: var(--muted); font-size: 11px; margin-bottom: 5px; }
.timeline-item strong { font-size: 13px; margin-bottom: 8px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.timeline-item .badge { flex: 0 0 auto; font-size: 11px; padding: 2px 8px; }
.load-alert { margin-top: 0; }
@media (max-width: 980px) {
  .stats { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .timeline { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
@media (max-width: 700px) {
  .cockpit-hero { flex-direction: column; }
  .health { width: 100%; }
  .stats, .cockpit-columns, .timeline { grid-template-columns: 1fr; }
  .timeline-item { min-height: auto; }
}
</style>
