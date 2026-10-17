<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { gsap } from 'gsap'
import PageShell from '../components/PageShell.vue'
import api from '../api'
import { validTabs } from '../stages'

const route = useRoute()
const router = useRouter()
const root = ref(null)
const list = ref([])
const workSummary = ref([])
const completionSummary = ref([])
const workSummaryAvailable = ref(true)
const completionSummaryAvailable = ref(true)
const keyword = ref('')
const loading = ref(false)
const saving = ref(false)
const loadError = ref(false)
const dialog = ref(false)
const editing = ref(false)
const deletingProjectId = ref(null)
const form = ref({ code: '', name: '', description: '', techStack: '', deliveryType: 'SaaS' })
let context
let hasEntered = false

async function load() {
  loading.value = true
  loadError.value = false
  try {
    const [projects, work, completion] = await Promise.all([
      api.get('/projects'),
      api.get('/reports/projects').catch(() => null),
      api.get('/reports/completion').catch(() => null),
    ])
    list.value = projects.data
    workSummaryAvailable.value = Array.isArray(work?.data)
    completionSummaryAvailable.value = Array.isArray(completion?.data)
    workSummary.value = workSummaryAvailable.value ? work.data : []
    completionSummary.value = completionSummaryAvailable.value ? completion.data : []
  }
  catch { loadError.value = true }
  finally { loading.value = false }
}

const filteredList = computed(() => {
  const value = keyword.value.trim().toLowerCase()
  if (!value) return list.value
  return list.value.filter((project) => `${project.code} ${project.name} ${project.description || ''} ${project.techStack || ''}`.toLowerCase().includes(value))
})

// 卡片依次入场（只在第一次载入时播放）
watch(loading, async (isLoading) => {
  if (isLoading || hasEntered) return
  await nextTick()
  const cards = root.value?.querySelectorAll('.project-card')
  if (!cards?.length) return
  hasEntered = true
  context = gsap.context(() => {
    gsap.from(cards, { autoAlpha: 0, y: 26, scale: 0.97, duration: 0.7, stagger: 0.07, ease: 'expo.out', clearProps: 'opacity,visibility,transform' })
  }, root.value)
})

function openProject(id) {
  if (deletingProjectId.value !== null) return
  const destination = { path: '/projects/' + id }
  if (typeof route.query.tab === 'string' && validTabs.has(route.query.tab)) destination.query = { tab: route.query.tab }
  router.push(destination)
}

function workFor(project) {
  return workSummary.value.find((item) => Number(item.projectId) === Number(project.id)) || {}
}

function completionFor(project) {
  return completionSummary.value.find((item) => Number(item.projectId) === Number(project.id)) || {}
}

function completionRate(project) {
  if (!completionSummaryAvailable.value) return null
  const item = completionFor(project)
  const total = Number(item.total) || 0
  return total > 0 ? Math.round(Number(item.doneCnt || 0) / total * 100) : null
}

function completionLabel(project) {
  const rate = completionRate(project)
  if (rate !== null) return `完成 ${rate}%`
  if (!completionSummaryAvailable.value || Number(workFor(project).reqCnt || 0) > 0) return '完成 —'
  return '暂无需求'
}

function deliveryHint(project) {
  if (!workSummaryAvailable.value) return '工作项统计暂不可用'
  const work = workFor(project)
  if (Number(work.bugCnt || 0) > 0) return '优先查看缺陷'
  if (Number(work.reqCnt || 0) === 0) return '先录入需求'
  if (Number(work.taskCnt || 0) === 0) return '把需求拆成任务'
  return '继续推进交付'
}

const unavailableSummaryLabels = computed(() => [
  !workSummaryAvailable.value && '工作项数量',
  !completionSummaryAvailable.value && '需求完成率',
].filter(Boolean))

function openAdd() {
  if (deletingProjectId.value !== null) return
  editing.value = false
  form.value = { code: '', name: '', description: '', techStack: '', deliveryType: 'SaaS' }
  dialog.value = true
}

// 命令面板等入口通过 ?new=1 直接唤起新建表单，消费后立刻清掉参数避免刷新重开。
function consumeNewQuery() {
  if (route.query.new !== '1') return
  openAdd()
  const query = { ...route.query }
  delete query.new
  router.replace({ query })
}

watch(() => route.query.new, (value) => { if (value === '1') consumeNewQuery() })

function openEdit(p) {
  if (deletingProjectId.value !== null) return
  editing.value = true
  form.value = { ...p }
  dialog.value = true
}

async function save() {
  if (saving.value || deletingProjectId.value !== null) return
  if (!form.value.code || !form.value.name) return ElMessage.warning('请填写编码和名称')
  saving.value = true
  try {
    if (editing.value) await api.put('/projects', form.value)
    else await api.post('/projects', form.value)
    ElMessage.success('保存成功')
    dialog.value = false
    load()
  } catch (e) { ElMessage.error(e.message) }
  finally { saving.value = false }
}

async function remove(id) {
  if (deletingProjectId.value !== null) return
  deletingProjectId.value = id
  try {
    await ElMessageBox.confirm('删除项目将清空关联数据，确定？', '提示', { type: 'warning' })
    await api.delete(`/projects/${id}`)
    ElMessage.success('已删除')
    await load()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error(e?.message || '删除失败')
  } finally {
    deletingProjectId.value = null
  }
}

onMounted(() => {
  load()
  consumeNewQuery()
})

onUnmounted(() => {
  context?.revert()
})
</script>

<template>
  <PageShell tag="02 / PROJECTS" title="项目">
    <template #action>
      <div class="project-tools">
        <label class="search-wrap">
          <span aria-hidden="true">⌕</span>
          <input v-model="keyword" type="search" placeholder="搜索项目" aria-label="搜索项目" :disabled="deletingProjectId !== null" />
        </label>
        <button class="create-button wb-press" type="button" :disabled="deletingProjectId !== null || saving" @click="openAdd"><span class="wb-arrow" aria-hidden="true">＋</span> 新建</button>
      </div>
    </template>

    <div ref="root" v-loading="loading" class="project-grid-wrap" :class="{ deleting: deletingProjectId !== null }">
      <div v-if="!loading && !loadError && unavailableSummaryLabels.length" class="summary-warning" role="status">
        <span>项目已加载；{{ unavailableSummaryLabels.join('、') }}暂不可用</span>
        <el-button link size="small" :disabled="loading || deletingProjectId !== null" @click="load">重试统计</el-button>
      </div>
      <div v-if="loadError" class="list-error" role="alert">
        <span>项目列表读取失败</span>
        <el-button link @click="load">重新加载</el-button>
      </div>

      <div v-if="!loadError && (filteredList.length || !loading)" class="project-grid">
        <article
          v-for="(p, index) in filteredList"
          :key="p.id"
          class="project-card"
          tabindex="0"
          :aria-label="`打开项目 ${p.name}`"
          @click="openProject(p.id)"
          @keydown.enter.self="openProject(p.id)"
        >
          <div class="project-cover" :class="'cover-' + (index % 4)">
            <div class="cover-topline"><span>{{ p.code }}</span><span>{{ p.deliveryType || '' }}</span></div>
            <div class="cover-art" aria-hidden="true"><i /><i /><i /></div>
            <h2 class="cover-title">{{ p.name }}</h2>
            <div class="cover-bottom"><span>{{ p.techStack || '' }}</span></div>
          </div>
          <div class="project-info">
            <div class="project-heading">
              <span class="project-hint">{{ deliveryHint(p) }}</span>
              <div class="card-actions" @click.stop>
                <button type="button" aria-label="编辑项目" :disabled="deletingProjectId !== null" @click="openEdit(p)">编辑</button>
                <button type="button" class="danger" aria-label="删除项目" :disabled="deletingProjectId !== null" @click="remove(p.id)">{{ deletingProjectId === p.id ? '处理中…' : '删除' }}</button>
              </div>
            </div>
            <p class="project-description">{{ p.description || '暂无描述' }}</p>
            <div class="project-stats">
              <span>需求 <b>{{ workSummaryAvailable ? workFor(p).reqCnt || 0 : '—' }}</b></span>
              <span>任务 <b>{{ workSummaryAvailable ? workFor(p).taskCnt || 0 : '—' }}</b></span>
              <span>缺陷 <b>{{ workSummaryAvailable ? workFor(p).bugCnt || 0 : '—' }}</b></span>
              <span class="project-progress">{{ completionLabel(p) }}</span>
            </div>
            <div class="progress-track"><i :style="{ width: (completionRate(p) ?? 0) + '%' }" /></div>
          </div>
        </article>

        <button v-if="!keyword" type="button" class="project-card project-new" :disabled="deletingProjectId !== null || saving" @click="openAdd">
          <span class="plus" aria-hidden="true">＋</span>
          <span>新建项目</span>
        </button>
      </div>
      <p v-if="!loadError && !loading && !filteredList.length && keyword" class="empty">没有匹配的项目</p>
    </div>

    <el-dialog
      v-model="dialog"
      :title="editing ? '编辑项目' : '新建项目'"
      width="520px"
      :close-on-click-modal="!saving"
      :close-on-press-escape="!saving"
      :show-close="!saving"
    >
      <el-form label-width="80px" size="default" :disabled="saving">
        <el-form-item label="编码"><el-input v-model="form.code" :disabled="editing" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="技术栈"><el-input v-model="form.techStack" /></el-form-item>
        <el-form-item label="形态"><el-input v-model="form.deliveryType" placeholder="SaaS / App / 定制" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="saving" @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </PageShell>
</template>

<style scoped>
.project-tools { display: flex; align-items: center; gap: 12px; }
.search-wrap { display: flex; width: min(24vw, 250px); align-items: center; gap: 10px; padding: 9px 12px; border-bottom: 1px solid rgba(239,239,235,.3); color: var(--muted); transition: border-color .4s var(--ease), color .4s var(--ease); }
.search-wrap:focus-within { border-bottom-color: rgba(239,239,235,.78); color: var(--text); }
.search-wrap > span { font-size: 18px; line-height: 1; }
.search-wrap input { width: 100%; border: 0; outline: 0; color: var(--text); background: transparent; font: inherit; font-size: 13px; }
.search-wrap input::placeholder { color: var(--muted-light); }
.create-button { padding: 10px 14px; border: 1px solid rgba(239,239,235,.6); border-radius: 999px; color: #080808; background: var(--text); cursor: pointer; font-size: 13px; transition: color .25s ease, background .25s ease, transform .35s var(--ease); }
.create-button:hover { color: var(--text); background: transparent; transform: translateY(-2px); }
.create-button:disabled { opacity: .45; cursor: not-allowed; }

/* 铺满整屏的项目网格：项目少时卡片会拉高填满，项目多时自动换行 */
.project-grid-wrap { min-height: calc(100svh - var(--nav-h) - 124px); display: flex; flex-direction: column; }
.project-grid-wrap.deleting { pointer-events: none; }
.project-grid { flex: 1; display: grid; grid-template-columns: repeat(auto-fill, minmax(min(100%, 230px), 1fr)); grid-auto-rows: minmax(300px, 1fr); gap: 12px; }
.summary-warning, .list-error { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 10px; color: var(--muted-light); font-size: 12px; }
.list-error { min-height: 76px; padding: 16px 20px; border: 1px solid var(--border); color: var(--muted); font-size: 13px; }
.empty { text-align: center; color: var(--muted); padding: 46px 0; font-size: 13px; }

.project-card { position: relative; display: flex; min-width: 0; flex-direction: column; overflow: hidden; border: 1px solid rgba(239,239,235,.16); border-radius: 14px; background: #0d0d0d; color: inherit; text-align: left; cursor: pointer; outline: none; box-shadow: 0 18px 50px -28px rgba(0,0,0,.8); transition: transform .5s var(--ease-smooth, cubic-bezier(.22,1,.36,1)), border-color .35s ease, box-shadow .5s ease; }
.project-card:hover, .project-card:focus-visible { transform: translateY(-4px); border-color: rgba(203,210,118,.5); box-shadow: 0 26px 60px -26px rgba(203,210,118,.35); }
.project-cover { position: relative; display: flex; flex: 1 1 56%; min-height: 150px; flex-direction: column; justify-content: space-between; overflow: hidden; padding: 16px 18px 14px; background: linear-gradient(135deg, #262726, #111212 72%); }
.project-cover::before { position: absolute; inset: 0; content: ""; opacity: .5; background-image: linear-gradient(rgba(255,255,255,.055) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,.055) 1px, transparent 1px); background-size: 35px 35px; mask-image: linear-gradient(135deg, #000, transparent 78%); }
.cover-1 { background: linear-gradient(135deg, #222a22, #101311 72%); }
.cover-2 { background: linear-gradient(135deg, #2a2822, #121110 72%); }
.cover-3 { background: linear-gradient(135deg, #22252a, #101113 72%); }
.cover-topline, .cover-bottom { position: relative; z-index: 2; display: flex; justify-content: space-between; color: rgba(245,245,240,.72); font-size: 12px; letter-spacing: .12em; }
.cover-art { position: absolute; top: 50%; right: -8%; width: 58%; aspect-ratio: 1; transform: translateY(-50%); pointer-events: none; }
.cover-art i { position: absolute; inset: 0; border: 1px solid rgba(239,239,235,.2); border-radius: 50%; transition: transform 1.2s var(--ease-smooth, ease); }
.cover-art i:nth-child(2) { inset: 12% -10%; transform: rotate(-24deg) scaleY(.5); border-color: rgba(203,210,118,.32); }
.cover-art i:nth-child(3) { inset: 26%; border-color: rgba(239,239,235,.14); }
.project-card:hover .cover-art i:nth-child(2) { transform: rotate(-8deg) scaleY(.5); }
.project-card:hover .cover-art i:nth-child(3) { transform: scale(1.12); }
.cover-title { position: relative; z-index: 2; margin: 0; max-width: 78%; font-family: var(--font-display); font-size: clamp(1.5rem, 2.4vw, 2.3rem); font-weight: 400; letter-spacing: -.03em; line-height: 1.1; color: var(--text); transition: letter-spacing .5s var(--ease-smooth, ease), color .3s ease; }
.project-card:hover .cover-title { letter-spacing: -.01em; color: #fff; }

.project-info { display: flex; flex: 1 1 44%; flex-direction: column; gap: 6px; padding: 12px 18px 14px; }
.project-heading { display: flex; align-items: center; justify-content: space-between; gap: 8px; min-height: 26px; }
.project-hint { color: var(--muted); font-size: 12px; }
.card-actions { display: flex; gap: 6px; opacity: 0; transform: translateY(2px); transition: opacity .3s ease, transform .4s var(--ease-smooth, ease); }
.project-card:hover .card-actions, .project-card:focus-within .card-actions { opacity: 1; transform: none; }
.card-actions button { padding: 3px 10px; border: 1px solid rgba(239,239,235,.2); border-radius: 6px; color: var(--muted); background: transparent; cursor: pointer; font-size: 12px; transition: color .2s ease, background .2s ease, border-color .2s ease; }
.card-actions button:hover { color: var(--text); background: rgba(239,239,235,.08); }
.card-actions button.danger:hover { color: #fff; background: rgba(224,143,130,.25); border-color: rgba(224,143,130,.5); }
.project-description { display: -webkit-box; overflow: hidden; margin: 0; color: var(--muted); font-size: 13px; line-height: 1.5; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }
.project-stats { display: flex; align-items: center; gap: 14px; margin-top: auto; color: var(--muted-light); font-size: 12px; }
.project-stats b { margin-left: 3px; color: var(--text); font-weight: 600; }
.project-progress { margin-left: auto; color: var(--muted); white-space: nowrap; }
.progress-track { height: 3px; overflow: hidden; border-radius: 3px; background: rgba(239,239,235,.1); }
.progress-track i { display: block; height: 100%; border-radius: 3px; background: linear-gradient(90deg, rgba(203,210,118,.5), var(--sage, #cbd276)); transition: width 1s var(--ease-smooth, ease); }

.project-new { align-items: center; justify-content: center; gap: 10px; border-style: dashed; border-color: rgba(239,239,235,.22); background: transparent; box-shadow: none; color: var(--muted); font-size: 14px; }
.project-new .plus { display: grid; place-items: center; width: 44px; height: 44px; border: 1px solid rgba(239,239,235,.3); border-radius: 50%; font-size: 22px; transition: transform .5s var(--ease-smooth, ease), background .3s ease; }
.project-new:hover { color: var(--text); }
.project-new:hover .plus { transform: rotate(90deg) scale(1.08); background: rgba(203,210,118,.15); }
.project-new:disabled { opacity: .45; cursor: not-allowed; }

@media (max-width: 700px) {
  .project-tools { width: 100%; }
  .search-wrap { flex: 1; width: auto; }
  .project-grid { grid-template-columns: 1fr; grid-auto-rows: minmax(260px, auto); }
  .card-actions { opacity: 1; transform: none; }
}
</style>
