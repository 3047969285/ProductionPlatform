<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '../components/PageShell.vue'
import api from '../api'

const router = useRouter()
const list = ref([])
const workSummary = ref([])
const completionSummary = ref([])
const keyword = ref('')
const loading = ref(false)
const dialog = ref(false)
const editing = ref(false)
const form = ref({ code: '', name: '', description: '', techStack: '', deliveryType: 'SaaS' })

async function load() {
  loading.value = true
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
  catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

const filteredList = computed(() => {
  const value = keyword.value.trim().toLowerCase()
  if (!value) return list.value
  return list.value.filter((project) => `${project.code} ${project.name} ${project.description || ''}`.toLowerCase().includes(value))
})

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
  if (!form.value.code || !form.value.name) return ElMessage.warning('请填写编码和名称')
  try {
    if (editing.value) await api.put('/projects', form.value)
    else await api.post('/projects', form.value)
    ElMessage.success('保存成功')
    dialog.value = false
    load()
  } catch (e) { ElMessage.error(e.message) }
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

onMounted(load)
</script>

<template>
  <PageShell tag="02 / PROJECTS" title="项目">
    <template #action>
      <el-button type="primary" @click="openAdd">+ 新建</el-button>
    </template>
    <div class="toolbar">
      <el-input v-model="keyword" clearable placeholder="搜索项目" class="search" />
      <span class="result-tip">{{ filteredList.length }} 项</span>
    </div>
    <div v-loading="loading" class="grid">
      <article v-for="p in filteredList" :key="p.id" class="card" @click="router.push(`/projects/${p.id}`)">
        <div class="card-top">
          <span class="code">{{ p.code }}</span>
          <span class="hint">{{ deliveryHint(p) }}</span>
          <div class="actions" @click.stop>
            <el-button size="default" @click="openEdit(p)">编辑</el-button>
            <el-button size="default" type="danger" @click="remove(p.id)">删除</el-button>
          </div>
        </div>
        <h2>{{ p.name }}</h2>
        <p class="desc">{{ p.description || '暂无描述' }}</p>
        <p class="meta">{{ p.techStack }} · {{ p.deliveryType }}</p>
        <div class="metrics">
          <span>需求 <b>{{ workFor(p).reqCnt || 0 }}</b></span>
          <span>任务 <b>{{ workFor(p).taskCnt || 0 }}</b></span>
          <span>缺陷 <b>{{ workFor(p).bugCnt || 0 }}</b></span>
        </div>
        <div class="progress-row">
          <span>需求完成率</span>
          <el-progress :percentage="completionRate(p)" :stroke-width="7" :show-text="false" />
          <b>{{ completionRate(p) }}%</b>
        </div>
        <span class="enter">打开驾驶舱 ↗</span>
      </article>
      <p v-if="!loading && !filteredList.length" class="empty">{{ list.length ? '无匹配' : '暂无项目' }}</p>
    </div>

    <el-dialog v-model="dialog" :title="editing ? '编辑项目' : '新建项目'" width="520px">
      <el-form label-width="80px" size="default">
        <el-form-item label="编码"><el-input v-model="form.code" :disabled="editing" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="技术栈"><el-input v-model="form.techStack" /></el-form-item>
        <el-form-item label="形态"><el-input v-model="form.deliveryType" placeholder="SaaS / App / 定制" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </PageShell>
</template>

<style scoped>
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin: 0 0 16px; }
.search { width: min(100%, 360px); max-width: 360px; }
.result-tip { color: var(--muted); font-size: 13px; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1px; padding: 1px; background: var(--border); }
.card {
  position: relative;
  padding: 22px;
  background: rgba(23, 27, 25, .9);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  cursor: pointer;
  transition: border-color .3s var(--ease), transform .3s var(--ease), background .3s var(--ease);
}
.card::before { position: absolute; top: 0; left: 22px; width: 26px; height: 1px; background: var(--accent); content: ""; opacity: .65; }
.card:hover { z-index: 1; border-color: var(--accent); background: rgba(39, 44, 36, .95); transform: translateY(-4px); }
.card-top { display: flex; justify-content: space-between; align-items: center; gap: 8px; margin-bottom: 10px; }
.code { font-size: 14px; color: var(--cyan); font-weight: 600; letter-spacing: 0.05em; }
.hint { margin-left: auto; padding: 3px 8px; border: 1px solid rgba(203, 210, 118, .28); color: var(--accent); background: rgba(203, 210, 118, .06); font-size: 10px; letter-spacing: .04em; white-space: nowrap; }
h2 { font-family: var(--font-display); font-size: 1.7rem; font-weight: 400; letter-spacing: -.03em; margin-bottom: 8px; }
.desc { display: -webkit-box; overflow: hidden; min-height: 42px; margin-bottom: 10px; color: var(--muted); font-size: 13px; line-height: 1.6; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }
.meta { font-size: 11px; color: var(--muted-light); letter-spacing: .04em; margin-bottom: 14px; }
.metrics { display: flex; gap: 14px; padding: 10px 0; border-top: 1px solid var(--border); border-bottom: 1px solid var(--border); color: var(--muted); font-size: 12px; }
.metrics b { color: var(--text); font-size: 14px; margin-left: 3px; }
.progress-row { display: grid; grid-template-columns: auto 1fr auto; gap: 8px; align-items: center; margin: 12px 0 14px; color: var(--muted); font-size: 12px; }
.progress-row b { color: var(--accent); font-size: 12px; }
.progress-row :deep(.el-progress) { min-width: 60px; }
.enter { font-size: 12px; color: var(--accent); letter-spacing: .06em; }
.empty { grid-column: 1 / -1; text-align: center; color: var(--muted); padding: 40px; font-size: 13px; }
@media (max-width: 640px) {
  .toolbar { align-items: stretch; flex-direction: column; gap: 10px; }
  .search { max-width: none; }
}
</style>
