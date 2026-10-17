<script setup>
import { computed, nextTick, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'
import { getUser } from '../auth'
import { stageDefinitions } from '../stages'

const props = defineProps({ modelValue: { type: Boolean, default: false } })
const emit = defineEmits(['update:modelValue', 'logout', 'refresh'])

const router = useRouter()

const RECENT_KEY = 'devflow:recent-projects'
const RECENT_MAX = 5

const query = ref('')
const mode = ref('root')
const stageTarget = ref(null)
const pickStage = ref(null)
const activeIndex = ref(0)
const projects = ref([])
const loading = ref(false)
const loaded = ref(false)
const loadFailed = ref(false)
const inputEl = ref(null)
const listEl = ref(null)
let requestId = 0

const isAdmin = computed(() => getUser()?.role === 'admin')

/* ── 最近访问 ─────────────────────────────────────────── */
function readRecent() {
  try {
    const raw = JSON.parse(localStorage.getItem(RECENT_KEY) || '[]')
    return Array.isArray(raw) ? raw.filter((id) => Number.isInteger(Number(id))).map(Number) : []
  } catch { return [] }
}

function rememberProject(id) {
  const next = [Number(id), ...readRecent().filter((item) => item !== Number(id))].slice(0, RECENT_MAX)
  try { localStorage.setItem(RECENT_KEY, JSON.stringify(next)) } catch { /* 忽略隐私模式下的写入失败 */ }
}

const recentIds = ref(readRecent())

/* ── 模糊匹配 ─────────────────────────────────────────── */
function score(text, keyword) {
  const target = String(text || '').toLowerCase()
  const needle = keyword.toLowerCase()
  if (!needle) return 1
  const direct = target.indexOf(needle)
  if (direct !== -1) return 200 - direct
  let cursor = 0
  let streak = 0
  let total = 0
  for (const ch of needle) {
    const hit = target.indexOf(ch, cursor)
    if (hit === -1) return 0
    streak = hit === cursor ? streak + 1 : 0
    total += 1 + streak
    cursor = hit + 1
  }
  return total
}

function match(item, keyword) {
  if (!keyword) return 1
  return Math.max(
    score(item.label, keyword) * 2,
    score(item.hint, keyword),
    score(item.keywords, keyword),
  )
}

/* ── 命令定义 ─────────────────────────────────────────── */
const pageCommands = [
  { id: 'nav-home', label: '交付总览', hint: '00 / DELIVERY', icon: '◈', keywords: 'home overview zonglan dashboard', to: '/' },
  { id: 'nav-projects', label: '项目', hint: '02 / PROJECTS', icon: '▦', keywords: 'projects xiangmu gallery', to: '/projects' },
  { id: 'nav-teams', label: '团队', hint: '03 / TEAMS', icon: '◎', keywords: 'teams tuandui', to: '/teams' },
  { id: 'nav-reports', label: '报表', hint: '04 / REPORTS', icon: '▤', keywords: 'reports baobiao chart', to: '/reports' },
  { id: 'nav-users', label: '用户', hint: '05 / USERS', icon: '☰', keywords: 'users yonghu admin', to: '/users', admin: true },
].map((item) => ({ ...item, group: '导航' }))

const actionCommands = computed(() => [
  { id: 'act-new-project', label: '新建项目', hint: '打开创建表单', icon: '＋', keywords: 'new create project xinjian', run: () => router.push({ path: '/projects', query: { new: '1' } }) },
  { id: 'act-refresh', label: '刷新当前页面', hint: '重新拉取数据', icon: '↻', keywords: 'refresh reload shuaxin', run: () => emit('refresh') },
  { id: 'act-logout', label: '退出登录', hint: '结束当前会话', icon: '⏻', keywords: 'logout exit tuichu', run: () => emit('logout') },
].map((item) => ({ ...item, group: '操作' })))

const projectCommands = computed(() => projects.value.map((project) => ({
  id: 'project-' + project.id,
  group: '项目',
  label: project.name,
  hint: [project.code, project.deliveryType].filter(Boolean).join(' · ') || '项目',
  icon: '▦',
  keywords: [project.code, project.techStack, project.description].filter(Boolean).join(' '),
  project,
  run: () => openProject(project),
})))

function stageCommands(project) {
  return [
    { id: 'stage-back', label: '返回上一级', hint: 'ESC', icon: '←', run: goBack },
    { id: 'stage-cockpit', label: '交付驾驶舱', hint: '默认入口', icon: '◈', run: () => openProject(project, 'cockpit') },
    ...stageDefinitions.map((stage) => ({
      id: 'stage-' + stage.key,
      label: stage.label,
      hint: stage.short,
      icon: stage.tabs[0].icon,
      run: () => openProject(project, stage.tabs[0].key),
    })),
  ]
}

// 反向查找：先定阶段、再挑项目，支持「测试 → 某项目」这类入口。
function projectPickerCommands(stage) {
  return [
    { id: 'pick-back', label: '返回上一级', hint: 'ESC', icon: '←', run: goBack },
    ...projects.value.map((project) => ({
      ...projectCommand(project),
      id: 'pick-' + stage.key + '-' + project.id,
      hint: `进入 ${stage.label} · ${project.code || ''}`.trim(),
      run: () => openProject(project, stage.tabs[0].key),
    })),
  ]
}

const stageJumpCommands = stageDefinitions.map((stage) => ({
  id: 'jump-' + stage.key,
  group: '阶段',
  label: stage.label,
  hint: stage.short,
  icon: stage.tabs[0].icon,
  keywords: [stage.short, stage.description, ...stage.tabs.map((item) => item.label)].join(' '),
  stage,
  keepOpen: true,
  run: () => { pickStage.value = stage; mode.value = 'pick'; query.value = '' },
}))

/* ── 结果集合 ─────────────────────────────────────────── */
const results = computed(() => {
  if (mode.value === 'stages' && stageTarget.value) {
    return [{ group: stageTarget.value.name, items: stageCommands(stageTarget.value) }]
  }
  if (mode.value === 'pick' && pickStage.value) {
    return [{ group: `${pickStage.value.label} · 选择项目`, items: projectPickerCommands(pickStage.value) }]
  }

  const keyword = query.value.trim()
  const visiblePages = pageCommands.filter((item) => !item.admin || isAdmin.value)

  const collect = (items) => items
    .map((item) => ({ item, rank: match(item, keyword) }))
    .filter((entry) => entry.rank > 0)
    .sort((a, b) => b.rank - a.rank)
    .map((entry) => entry.item)

  const groups = []

  if (!keyword) {
    const recent = recentIds.value
      .map((id) => projects.value.find((project) => Number(project.id) === id))
      .filter(Boolean)
    if (recent.length) {
      groups.push({ group: '最近访问', items: recent.map((project) => ({ ...projectCommand(project), group: '最近访问' })) })
    }
  }

  const pages = collect(visiblePages)
  if (pages.length) groups.push({ group: '导航', items: pages })

  const matchedProjects = collect(projectCommands.value)
  if (matchedProjects.length) groups.push({ group: '项目', items: matchedProjects.slice(0, 8) })

  // 只有输入关键词时才展开阶段入口，保持默认视图清爽。
  const stages = keyword ? collect(stageJumpCommands) : []
  if (stages.length) groups.push({ group: '阶段', items: stages })

  const actions = collect(actionCommands.value)
  if (actions.length) groups.push({ group: '操作', items: actions })

  return groups
})

function projectCommand(project) {
  return {
    id: 'project-' + project.id,
    group: '项目',
    label: project.name,
    hint: [project.code, project.deliveryType].filter(Boolean).join(' · ') || '项目',
    icon: '▦',
    keywords: [project.code, project.techStack, project.description].filter(Boolean).join(' '),
    project,
    run: () => openProject(project),
  }
}

const flatItems = computed(() => results.value.flatMap((entry) => entry.items))

watch(results, () => { activeIndex.value = 0 })

const activeItem = computed(() => flatItems.value[activeIndex.value] || null)

/* ── 动作 ─────────────────────────────────────────────── */
function close() {
  emit('update:modelValue', false)
}

function openProject(project, tab) {
  rememberProject(project.id)
  recentIds.value = readRecent()
  close()
  const target = { path: '/projects/' + project.id }
  if (tab) target.query = { tab }
  router.push(target)
}

function runItem(item) {
  if (!item) return
  if (item.to) {
    close()
    router.push(item.to)
    return
  }
  // keepOpen 的条目（如「先选阶段再挑项目」）需要面板留在原地继续下钻。
  if (!item.keepOpen) close()
  item.run?.()
}

function drillInto(item) {
  if (!item?.project) return
  stageTarget.value = item.project
  mode.value = 'stages'
  query.value = ''
}

function move(step) {
  const total = flatItems.value.length
  if (!total) return
  activeIndex.value = (activeIndex.value + step + total) % total
  nextTick(() => {
    listEl.value?.querySelector('.palette-item.active')?.scrollIntoView({ block: 'nearest' })
  })
}

function goBack() {
  mode.value = 'root'
  stageTarget.value = null
  pickStage.value = null
  query.value = ''
}

function onKeydown(event) {
  if (!props.modelValue) return
  if (event.key === 'ArrowDown') { event.preventDefault(); move(1) }
  else if (event.key === 'ArrowUp') { event.preventDefault(); move(-1) }
  else if (event.key === 'ArrowRight') {
    if (activeItem.value?.project) { event.preventDefault(); drillInto(activeItem.value) }
  }
  else if (event.key === 'ArrowLeft') {
    if (mode.value !== 'root') { event.preventDefault(); goBack() }
  }
  else if (event.key === 'Enter') { event.preventDefault(); runItem(activeItem.value) }
  else if (event.key === 'Escape') {
    event.preventDefault()
    if (mode.value !== 'root') goBack()
    else close()
  }
}

/* ── 数据加载 ─────────────────────────────────────────── */
async function loadProjects() {
  if (loaded.value || loading.value) return
  const current = ++requestId
  loading.value = true
  loadFailed.value = false
  try {
    const response = await api.get('/projects')
    if (current !== requestId) return
    projects.value = Array.isArray(response.data) ? response.data : []
    loaded.value = true
  } catch {
    if (current === requestId) loadFailed.value = true
  } finally {
    if (current === requestId) loading.value = false
  }
}

watch(() => props.modelValue, async (open) => {
  if (open) {
    mode.value = 'root'
    stageTarget.value = null
    pickStage.value = null
    query.value = ''
    activeIndex.value = 0
    recentIds.value = readRecent()
    // 挂在 document 上（捕获阶段），保证焦点不在输入框时方向键依然生效。
    document.addEventListener('keydown', onKeydown, true)
    await nextTick()
    inputEl.value?.focus()
    loadProjects()
  } else {
    document.removeEventListener('keydown', onKeydown, true)
    requestId += 1
    loading.value = false
  }
})

onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown, true)
  requestId += 1
})
</script>

<template>
  <Teleport to="body">
    <Transition name="palette">
      <div v-if="modelValue" class="palette-root" @mousedown.self="close">
        <section class="palette" role="dialog" aria-modal="true" aria-label="全局命令面板">
          <header class="palette-head">
            <span class="palette-mark" aria-hidden="true">{{ mode === 'root' ? '⌘K' : '↳' }}</span>
            <input
              ref="inputEl"
              v-model="query"
              class="palette-input"
              type="text"
              autocomplete="off"
              spellcheck="false"
              :placeholder="mode === 'stages' ? '选择交付阶段' : mode === 'pick' ? '选择项目' : '搜索页面、项目或操作…'"
              aria-label="搜索页面、项目或操作"
            />
            <button class="palette-esc" type="button" @click="close">ESC</button>
          </header>

          <p v-if="mode === 'stages' && stageTarget" class="palette-crumb">
            <span>项目</span>
            <strong>{{ stageTarget.name }}</strong>
          </p>
          <p v-else-if="mode === 'pick' && pickStage" class="palette-crumb">
            <span>阶段</span>
            <strong>{{ pickStage.label }}</strong>
          </p>

          <div ref="listEl" class="palette-body">
            <p v-if="loadFailed" class="palette-state palette-state--warn">
              项目列表读取失败，仅显示导航与操作
            </p>
            <template v-if="flatItems.length">
              <div v-for="entry in results" :key="entry.group" class="palette-group">
                <p class="palette-group-label">{{ entry.group }}</p>
                <button
                  v-for="item in entry.items"
                  :key="item.id"
                  type="button"
                  class="palette-item"
                  :class="{ active: item === activeItem }"
                  @mousemove="activeIndex = flatItems.indexOf(item)"
                  @click="runItem(item)"
                >
                  <span class="palette-icon" aria-hidden="true">{{ item.icon }}</span>
                  <span class="palette-label">{{ item.label }}</span>
                  <span class="palette-hint">
                    {{ item.hint }}
                    <i v-if="item.project" class="palette-drill" aria-hidden="true">→</i>
                  </span>
                </button>
              </div>
              <!-- 导航与操作立即可用，项目列表在后台补齐 -->
              <p v-if="loading" class="palette-note">正在读取项目…</p>
            </template>
            <p v-else-if="loading" class="palette-state">正在读取项目…</p>
            <p v-else class="palette-state">没有匹配的结果</p>
          </div>

          <footer class="palette-foot">
            <span><b>↑↓</b> 选择 · <b>↵</b> 打开 · <b>→</b> 进入阶段 · <b>ESC</b> 关闭</span>
            <span class="palette-foot-brand">DEVFLOW / COMMAND</span>
          </footer>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.palette-root {
  position: fixed; z-index: 400; inset: 0;
  display: flex; align-items: flex-start; justify-content: center;
  padding: 11vh 20px 40px;
  background: rgba(0, 0, 0, .74);
  backdrop-filter: blur(7px);
}
.palette {
  display: flex; width: min(660px, 100%); max-height: min(72vh, 620px);
  flex-direction: column; overflow: hidden;
  border: 1px solid rgba(238, 238, 234, .18); border-radius: 16px;
  background: rgba(9, 10, 10, .97);
  box-shadow: 0 40px 120px rgba(0, 0, 0, .7), inset 0 1px 0 rgba(255, 255, 255, .04);
}
.palette-head { display: flex; align-items: center; gap: 13px; padding: 15px 18px; border-bottom: 1px solid rgba(238, 238, 234, .12); }
.palette-mark { flex: 0 0 auto; color: var(--muted-light); font-size: 12px; letter-spacing: .1em; }
.palette-input {
  flex: 1; min-width: 0; padding: 2px 0; border: 0; outline: 0;
  color: var(--text); background: transparent; font: inherit; font-size: 15px; letter-spacing: .01em;
}
.palette-input::placeholder { color: var(--muted-light); }
.palette-esc {
  flex: 0 0 auto; padding: 4px 9px; border: 1px solid rgba(238, 238, 234, .22); border-radius: 6px;
  color: var(--muted-light); background: transparent; cursor: pointer; font-size: 12px; letter-spacing: .14em;
  transition: color .2s ease, border-color .2s ease;
}
.palette-esc:hover { color: var(--text); border-color: rgba(238, 238, 234, .55); }
.palette-crumb { display: flex; align-items: baseline; gap: 9px; padding: 11px 20px 0; color: var(--muted-light); font-size: 12px; letter-spacing: .16em; }
.palette-crumb strong { color: var(--text); font-family: var(--font-display); font-size: 14px; font-weight: 400; letter-spacing: -.01em; }
.palette-body { flex: 1; min-height: 0; overflow-y: auto; padding: 8px 9px 12px; }
.palette-group + .palette-group { margin-top: 4px; }
.palette-group-label { padding: 11px 11px 6px; color: var(--muted-light); font-size: 12px; letter-spacing: .2em; }
.palette-item {
  display: grid; width: 100%; grid-template-columns: 22px minmax(0, 1fr) auto;
  align-items: center; gap: 12px; padding: 10px 11px;
  border: 0; border-radius: 10px; color: var(--muted); background: transparent;
  cursor: pointer; text-align: left; font: inherit;
  transition: background .18s ease, color .18s ease;
}
.palette-item.active { color: var(--text); background: rgba(255, 255, 255, .075); }
.palette-icon { color: var(--muted-light); font-size: 12px; text-align: center; }
.palette-item.active .palette-icon { color: var(--text); }
.palette-label { overflow: hidden; font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.palette-hint { display: inline-flex; align-items: center; gap: 8px; color: var(--muted-light); font-size: 12px; letter-spacing: .11em; white-space: nowrap; }
.palette-drill { color: var(--muted-light); font-style: normal; transition: transform .2s ease, color .2s ease; }
.palette-item.active .palette-drill { color: var(--text); transform: translateX(2px); }
.palette-state { padding: 26px 12px; color: var(--muted-light); font-size: 12px; text-align: center; }
.palette-state--warn { color: var(--muted); }
.palette-note { padding: 12px 11px 4px; color: var(--muted-light); font-size: 12px; letter-spacing: .08em; }
.palette-foot {
  display: flex; align-items: center; justify-content: space-between; gap: 14px;
  padding: 10px 18px; border-top: 1px solid rgba(238, 238, 234, .12);
  color: var(--muted-light); font-size: 12px; letter-spacing: .14em;
}
.palette-foot b { color: var(--muted); font-weight: 500; }
.palette-foot-brand { letter-spacing: .18em; }
.palette-enter-active, .palette-leave-active { transition: opacity .22s ease; }
.palette-enter-active .palette, .palette-leave-active .palette { transition: transform .26s var(--ease), opacity .22s ease; }
.palette-enter-from, .palette-leave-to { opacity: 0; }
.palette-enter-from .palette, .palette-leave-to .palette { transform: translateY(-10px) scale(.985); opacity: 0; }
@media (max-width: 620px) {
  .palette-root { padding: 6vh 12px 24px; }
  .palette-foot-brand { display: none; }
  .palette-hint { font-size: 12px; }
}
</style>
