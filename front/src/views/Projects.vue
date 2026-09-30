<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { gsap } from 'gsap'
import PageShell from '../components/PageShell.vue'
import api from '../api'

const router = useRouter()
const root = ref(null)
const list = ref([])
const workSummary = ref([])
const completionSummary = ref([])
const keyword = ref('')
const loading = ref(false)
const saving = ref(false)
const loadError = ref(false)
const dialog = ref(false)
const editing = ref(false)
const openingProject = ref(false)
const form = ref({ code: '', name: '', description: '', techStack: '', deliveryType: 'SaaS' })
const activeIndex = ref(0)
const viewportWidth = ref(window.innerWidth)
let context
let openingContext
let hasEntered = false

async function load() {
  loading.value = true
  loadError.value = false
  try {
    const [projects, work, completion] = await Promise.all([
      api.get('/projects'),
      api.get('/reports/projects').catch(() => ({ data: [] })),
      api.get('/reports/completion').catch(() => ({ data: [] })),
    ])
    list.value = projects.data
    workSummary.value = work.data
    completionSummary.value = completion.data
  }
  catch { loadError.value = true }
  finally { loading.value = false }
}

const filteredList = computed(() => {
  const value = keyword.value.trim().toLowerCase()
  if (!value) return list.value
  return list.value.filter((project) => `${project.code} ${project.name} ${project.description || ''}`.toLowerCase().includes(value))
})

watch(filteredList, () => { activeIndex.value = 0 })

watch(loading, async (isLoading) => {
  if (isLoading || hasEntered) return
  await nextTick()
  const cards = root.value?.querySelectorAll('.project-card')
  if (!cards?.length) return
  hasEntered = true
  context = gsap.context(() => {
    const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    gsap.fromTo(cards,
      { autoAlpha: 0, y: reduced ? 0 : 24, rotateX: reduced ? 0 : 2 },
      { autoAlpha: 1, y: 0, rotateX: 0, duration: reduced ? 0 : .85, stagger: reduced ? 0 : .09, ease: 'power3.out' },
    )
  }, root.value)
})

function cardStyle(index) {
  const delta = index - activeIndex.value
  const mobile = viewportWidth.value < 700
  const mobileWidth = viewportWidth.value < 421 ? .82 : .8
  const cardWidth = mobile
    ? Math.min(viewportWidth.value * mobileWidth, 540)
    : Math.min(viewportWidth.value * (viewportWidth.value <= 900 ? .6 : .43), 650)
  const gap = mobile ? 18 : 34
  const angle = delta === 0 ? 0 : -Math.sign(delta) * Math.min(Math.abs(delta) * 5, 13)
  const scale = delta === 0 ? 1 : Math.max(.78, 1 - Math.abs(delta) * .075)
  const depth = delta === 0 ? 0 : -Math.min(Math.abs(delta) * 82, 220)
  return {
    transform: 'translate3d(calc(-50% + ' + delta * (cardWidth + gap) + 'px), -50%, ' + depth + 'px) rotateY(' + angle + 'deg) scale(' + scale + ')',
    opacity: Math.abs(delta) > 2 ? 0 : Math.max(.35, 1 - Math.abs(delta) * .28),
    zIndex: 10 - Math.abs(delta),
    pointerEvents: Math.abs(delta) > 2 ? 'none' : 'auto',
  }
}

function moveTo(index) {
  if (openingProject.value) return
  const count = filteredList.value.length
  if (!count) return
  activeIndex.value = (index + count) % count
}

function activateCard(index, id) {
  if (index !== activeIndex.value) moveTo(index)
  else openProject(id)
}

function openProject(id) {
  if (openingProject.value) return
  const selectedCard = root.value?.querySelector('.project-card.selected')
  const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (!selectedCard || reduced) {
    router.push('/projects/' + id)
    return
  }

  openingProject.value = true
  openingContext = gsap.context(() => {
    gsap.timeline({ onComplete: () => router.push('/projects/' + id) })
      .to(selectedCard.querySelector('.project-cover'), { scale: 1.06, y: -6, duration: .22, ease: 'power3.out' })
      .to(selectedCard.querySelector('.project-cover'), { scale: 1.12, y: -14, autoAlpha: 0, duration: .3, ease: 'power4.in' })
      .to(selectedCard.querySelector('.project-info'), { y: 18, autoAlpha: 0, duration: .3, ease: 'power3.in' }, '<')
  }, root.value)
}

function handleGalleryKey(event) {
  if (event.target !== event.currentTarget && event.target.closest('button, a, input, textarea, select, [contenteditable="true"]')) return
  if (openingProject.value) { event.preventDefault(); return }
  if (event.key === 'ArrowLeft') { event.preventDefault(); moveTo(activeIndex.value - 1) }
  if (event.key === 'ArrowRight') { event.preventDefault(); moveTo(activeIndex.value + 1) }
  if (event.key === 'Enter') {
    event.preventDefault()
    const selected = filteredList.value[activeIndex.value]
    if (selected) openProject(selected.id)
  }
}

function syncViewport() { viewportWidth.value = window.innerWidth }

function workFor(project) {
  return workSummary.value.find((item) => Number(item.projectId) === Number(project.id)) || {}
}

function completionFor(project) {
  return completionSummary.value.find((item) => Number(item.projectId) === Number(project.id)) || {}
}

function completionRate(project) {
  const item = completionFor(project)
  return item.total ? Math.round(Number(item.doneCnt || 0) / Number(item.total) * 100) : 0
}

function deliveryHint(project) {
  const work = workFor(project)
  if (Number(work.bugCnt || 0) > 0) return '优先查看缺陷'
  if (Number(work.reqCnt || 0) === 0) return '先录入需求'
  if (Number(work.taskCnt || 0) === 0) return '把需求拆成任务'
  return '继续推进交付'
}

function openAdd() {
  editing.value = false
  form.value = { code: '', name: '', description: '', techStack: '', deliveryType: 'SaaS' }
  dialog.value = true
}

function openEdit(p) {
  editing.value = true
  form.value = { ...p }
  dialog.value = true
}

async function save() {
  if (saving.value) return
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
  try {
    await ElMessageBox.confirm('删除项目将清空关联数据，确定？', '提示', { type: 'warning' })
    await api.delete(`/projects/${id}`)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

onMounted(() => {
  window.addEventListener('resize', syncViewport, { passive: true })
  load()
})

onUnmounted(() => {
  window.removeEventListener('resize', syncViewport)
  openingContext?.revert()
  context?.revert()
})
</script>

<template>
  <PageShell tag="02 / PROJECTS" title="项目">
    <template #action>
      <div class="project-tools">
        <label class="search-wrap">
          <span aria-hidden="true">⌕</span>
          <input v-model="keyword" type="search" placeholder="搜索项目" aria-label="搜索项目" :disabled="openingProject" />
        </label>
        <button class="create-button" type="button" :disabled="openingProject || saving" @click="openAdd">＋ 新建</button>
      </div>
    </template>
    <div ref="root" v-loading="loading" class="gallery-layout" :class="{ opening: openingProject }">
      <div v-if="loadError" class="list-error" role="alert">
        <span>项目列表读取失败</span>
        <el-button link @click="load">重新加载</el-button>
      </div>
      <div v-if="filteredList.length" class="gallery-stage" tabindex="0" aria-label="项目画廊，使用左右方向键切换，按回车打开当前项目" :aria-busy="openingProject" @keydown="handleGalleryKey">
        <span class="stage-note">PROJECTS / {{ String(filteredList.length).padStart(2, '0') }}</span>
        <article
          v-for="(p, index) in filteredList"
          :key="p.id"
          class="project-card"
          :class="{ selected: index === activeIndex }"
          :style="cardStyle(index)"
          :aria-current="index === activeIndex ? 'true' : undefined"
          @click="activateCard(index, p.id)"
        >
          <div class="project-cover" :class="'cover-' + (index % 4)">
            <div class="cover-topline"><span>{{ p.code }}</span><span>{{ p.deliveryType || 'DELIVERY' }}</span></div>
            <div class="cover-art" aria-hidden="true"><i /><i /><i /></div>
            <button type="button" class="cover-title" @click.stop="activateCard(index, p.id)">{{ p.name }}</button>
            <div class="cover-bottom"><span>{{ p.techStack || 'PRODUCT DEVELOPMENT' }}</span><span>{{ String(index + 1).padStart(2, '0') }}</span></div>
          </div>
          <div class="project-info">
            <div class="project-heading">
              <div><span class="project-code">{{ p.code }}</span><span class="project-hint">{{ deliveryHint(p) }}</span></div>
              <div v-if="index === activeIndex" class="card-actions" @click.stop>
                <button type="button" aria-label="编辑项目" @click="openEdit(p)">编辑</button>
                <button type="button" aria-label="删除项目" @click="remove(p.id)">删除</button>
              </div>
            </div>
            <p class="project-description">{{ p.description || '暂无描述' }}</p>
            <div class="project-stats">
              <span>需求 <b>{{ workFor(p).reqCnt || 0 }}</b></span>
              <span>任务 <b>{{ workFor(p).taskCnt || 0 }}</b></span>
              <span>缺陷 <b>{{ workFor(p).bugCnt || 0 }}</b></span>
              <span class="project-progress">{{ completionRate(p) }}%</span>
            </div>
            <div class="progress-track"><i :style="{ width: completionRate(p) + '%' }" /></div>
          </div>
        </article>
      </div>
      <p v-if="!loadError && !loading && !filteredList.length" class="empty">{{ list.length ? '没有匹配的项目' : '还没有项目' }}</p>
      <div v-if="filteredList.length" class="gallery-controls">
        <span>{{ String(activeIndex + 1).padStart(2, '0') }} <i /> {{ String(filteredList.length).padStart(2, '0') }}</span>
        <div class="gallery-arrows">
          <button type="button" aria-label="上一个项目" @click="moveTo(activeIndex - 1)">←</button>
          <button type="button" aria-label="下一个项目" @click="moveTo(activeIndex + 1)">→</button>
        </div>
        <span class="gallery-hint">选择项目以打开交付驾驶舱</span>
      </div>
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
:deep(.page) { padding-top: clamp(78px, 11vh, 110px); }
.gallery-layout { margin-top: -44px; }
.project-tools { display: flex; align-items: center; gap: 12px; }
.search-wrap { display: flex; width: min(24vw, 250px); align-items: center; gap: 10px; padding: 9px 12px; border-bottom: 1px solid rgba(239,239,235,.3); color: var(--muted); }
.search-wrap > span { font-size: 18px; line-height: 1; }
.search-wrap input { width: 100%; border: 0; outline: 0; color: var(--text); background: transparent; font: inherit; font-size: 11px; }
.search-wrap input::placeholder { color: var(--muted-light); }
.create-button { padding: 10px 14px; border: 1px solid rgba(239,239,235,.6); border-radius: 999px; color: #080808; background: var(--text); cursor: pointer; font-size: 10px; transition: color .25s ease, background .25s ease, transform .35s var(--ease); }
.create-button:hover { color: var(--text); background: transparent; transform: translateY(-2px); }
.gallery-layout { min-height: 57vh; }
.gallery-layout.opening { pointer-events: none; }
.gallery-stage { position: relative; height: clamp(360px, 47vh, 520px); overflow: visible; outline: none; perspective: 1500px; transform-style: preserve-3d; }
.stage-note { position: absolute; top: -19px; left: 0; color: var(--muted-light); font-size: 8px; letter-spacing: .18em; }
.project-card { position: absolute; top: 50%; left: 50%; display: flex; width: min(43vw, 650px); height: clamp(300px, 43.5vh, 500px); flex-direction: column; overflow: hidden; border: 1px solid rgba(239,239,235,.16); border-radius: 17px; background: #0d0d0d; box-shadow: 0 28px 80px rgba(0,0,0,.55); cursor: pointer; transform-origin: center center; transition: transform .85s cubic-bezier(.2,.75,.2,1), opacity .55s ease, border-color .5s ease, box-shadow .5s ease; will-change: transform, opacity; }
.project-card.selected { border-color: rgba(239,239,235,.54); box-shadow: 0 34px 100px rgba(0,0,0,.62); }
.project-cover { position: relative; display: flex; flex: 1 1 58%; min-height: 54%; flex-direction: column; justify-content: space-between; overflow: hidden; padding: 18px 21px 17px; background: linear-gradient(135deg, #262726, #111212 72%); }
.project-cover::before { position: absolute; inset: 0; content: ""; opacity: .5; background-image: linear-gradient(rgba(255,255,255,.055) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,.055) 1px, transparent 1px); background-size: 35px 35px; mask-image: linear-gradient(135deg, #000, transparent 78%); }
.cover-1 { background: linear-gradient(135deg, #30302e, #161717 70%); }
.cover-2 { background: linear-gradient(135deg, #202121, #343331 60%, #151515); }
.cover-3 { background: linear-gradient(135deg, #333330, #171818 65%); }
.cover-topline, .cover-bottom { position: relative; z-index: 2; display: flex; justify-content: space-between; color: rgba(245,245,240,.7); font-size: 8px; letter-spacing: .15em; }
.cover-art { position: absolute; inset: 10% 20%; display: grid; place-items: center; opacity: .86; }
.cover-art::before, .cover-art::after, .cover-art i { position: absolute; display: block; border: 1px solid rgba(238,238,234,.45); border-radius: 50%; content: ""; }
.cover-art::before { width: min(26vw, 310px); aspect-ratio: 1; box-shadow: 0 0 90px rgba(238,238,234,.06), inset 0 0 50px rgba(238,238,234,.06); }
.cover-art::after { width: min(17vw, 210px); aspect-ratio: 1; border-color: rgba(238,238,234,.32); }
.cover-art i:nth-child(1) { width: 74%; height: 25%; transform: rotate(-24deg); }
.cover-art i:nth-child(2) { width: 34%; height: 85%; transform: rotate(38deg); }
.cover-art i:nth-child(3) { width: 6px; height: 6px; top: 24%; right: 24%; background: #e4e4df; box-shadow: 0 0 24px rgba(255,255,255,.5); }
.cover-1 .cover-art { transform: rotate(26deg) scale(.8); }
.cover-1 .cover-art::before { border-radius: 43% 57% 50% 50%; }
.cover-2 .cover-art { transform: rotate(-18deg) scale(1.15); }
.cover-2 .cover-art::before { width: 70%; height: 56%; border-radius: 2px; }
.cover-2 .cover-art::after { width: 45%; height: 80%; border-radius: 2px; }
.cover-3 .cover-art { transform: rotate(50deg) scale(.9); }
.cover-title { position: relative; z-index: 2; max-width: 75%; padding: 0; border: 0; color: rgba(245,245,240,.88); background: transparent; text-align: left; font-family: var(--font-display); font-size: clamp(21px, 3vw, 42px); letter-spacing: -.06em; cursor: pointer; }
.project-info { display: flex; min-height: 42%; flex-direction: column; padding: 14px 20px 13px; }
.project-heading { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.project-code { color: var(--muted); font-size: 8px; letter-spacing: .13em; }
.project-hint { margin-left: 10px; color: var(--muted-light); font-size: 8px; }
.card-actions { display: flex; gap: 8px; }
.card-actions button { padding: 5px 8px; border: 1px solid rgba(239,239,235,.18); border-radius: 5px; color: var(--muted); background: transparent; cursor: pointer; font-size: 9px; }
.card-actions button:hover { border-color: rgba(239,239,235,.65); color: var(--text); }
.project-description { display: -webkit-box; overflow: hidden; margin-top: 7px; color: var(--muted); font-size: 10px; line-height: 1.5; -webkit-box-orient: vertical; -webkit-line-clamp: 1; }
.project-stats { display: flex; align-items: center; gap: 15px; margin-top: auto; padding-top: 12px; color: var(--muted-light); font-size: 9px; }
.project-stats b { margin-left: 4px; color: var(--text); font-size: 11px; font-weight: 500; }
.project-progress { margin-left: auto; color: var(--text); }
.progress-track { height: 1px; margin-top: 8px; background: rgba(239,239,235,.13); }
.progress-track i { display: block; height: 100%; background: rgba(239,239,235,.78); }
.gallery-controls { display: grid; grid-template-columns: 100px 100px 1fr; align-items: center; gap: 20px; margin-top: 13px; color: var(--muted); font-size: 8px; letter-spacing: .12em; }
.gallery-controls > span:first-child { display: inline-flex; align-items: center; gap: 9px; }
.gallery-controls > span:first-child i { width: 28px; height: 1px; background: rgba(239,239,235,.3); }
.gallery-arrows { display: flex; gap: 8px; }
.gallery-arrows button { display: grid; width: 30px; height: 30px; place-items: center; border: 1px solid rgba(239,239,235,.27); border-radius: 50%; color: var(--text); background: transparent; cursor: pointer; transition: color .25s ease, background .25s ease, transform .3s ease; }
.gallery-arrows button:hover { color: #000; background: var(--text); transform: scale(1.06); }
.gallery-hint { justify-self: end; color: var(--muted-light); letter-spacing: .08em; }
.list-error { display: flex; min-height: 76px; align-items: center; justify-content: space-between; gap: 12px; padding: 16px 20px; border: 1px solid var(--border); color: var(--muted); font-size: 11px; }
.empty { grid-column: 1 / -1; text-align: center; color: var(--muted); padding: 46px 0; font-size: 12px; }
@media (max-width: 900px) { .project-card { width: min(60vw, 650px); } }
@media (max-width: 700px) {
  :deep(.page) { padding-top: 96px; }
  .gallery-layout { margin-top: -24px; }
  .project-tools { width: 100%; }
  .search-wrap { flex: 1; width: auto; }
  .gallery-layout { min-height: 55vh; }
  .gallery-stage { height: 47vh; min-height: 350px; }
  .project-card { width: min(80vw, 540px); height: clamp(330px, 43vh, 430px); }
  .cover-art::before { width: 48vw; }
  .cover-art::after { width: 31vw; }
  .project-info { padding: 13px 15px; }
  .gallery-controls { grid-template-columns: 72px 75px 1fr; gap: 10px; }
  .gallery-hint { max-width: 140px; text-align: right; line-height: 1.5; }
}
@media (max-width: 420px) {
  .project-card { width: 82vw; height: 340px; }
  .project-stats { gap: 9px; }
  .project-stats span { font-size: 8px; }
  .gallery-controls { grid-template-columns: 58px 70px 1fr; gap: 8px; }
}
</style>
