<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import WorkItemDetail from './WorkItemDetail.vue'
import RichEditor from './RichEditor.vue'
import { bugStatus, bugSeverity, reqPriority, badgeClass } from '../constants'

const props = defineProps({ projectId: { type: Number, required: true } })

const list = ref([])
const loading = ref(false)
const keyword = ref('')
const filter = ref('all')
const sprints = ref([])
const dialog = ref(false)
const editing = ref(false)
const saving = ref(false)
const statusSavingIds = ref({})
const form = ref({})

async function loadSprints() {
  try { sprints.value = (await api.get('/sprints', { params: { projectId: props.projectId } })).data }
  catch { sprints.value = [] }
}

async function load() {
  loading.value = true
  try { list.value = (await api.get('/bugs', { params: { projectId: props.projectId } })).data }
  catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

const filteredList = computed(() => {
  const value = keyword.value.trim().toLowerCase()
  return list.value.filter((item) => {
    const matchKeyword = !value || `${item.title} ${item.assignee || ''} ${item.reporter || ''} ${item.fixVersion || ''}`.toLowerCase().includes(value)
    const matchFilter = filter.value === 'all'
      || (filter.value === 'open' && !['resolved', 'closed'].includes(item.status))
      || (filter.value === 'serious' && ['high', 'critical'].includes(item.severity))
      || (filter.value === 'unassigned' && !item.assignee)
    return matchKeyword && matchFilter
  })
})

function openAdd() {
  editing.value = false
  form.value = { projectId: props.projectId, title: '', content: '', severity: 'medium', priority: 'medium', status: 'open', steps: '', expectedResult: '', actualResult: '', assignee: '', reporter: '', fixVersion: '', sprintId: null }
  dialog.value = true
}

function openEdit(row) {
  editing.value = true
  form.value = { ...row }
  dialog.value = true
}

async function save() {
  if (saving.value) return
  if (!form.value.title?.trim()) return ElMessage.warning('请填写标题')
  form.value.title = form.value.title.trim()
  saving.value = true
  try {
    if (editing.value) await api.put('/bugs', form.value)
    else await api.post('/bugs', form.value)
    ElMessage.success('保存成功')
    dialog.value = false
    await load()
  } catch (e) { ElMessage.error(e.message) }
  finally { saving.value = false }
}

async function changeStatus(row, status) {
  if (statusSavingIds.value[row.id]) return
  statusSavingIds.value = { ...statusSavingIds.value, [row.id]: true }
  try {
    await api.put(`/bugs/${row.id}/status?status=${status}`)
    ElMessage.success('状态已更新')
    await load()
  } catch (e) { ElMessage.error(e.message) }
  finally {
    const next = { ...statusSavingIds.value }
    delete next[row.id]
    statusSavingIds.value = next
  }
}

const detailVisible = ref(false)
const detailId = ref(null)
function openDetail(row) {
  detailId.value = row.id
  detailVisible.value = true
}

async function remove(id) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await api.delete(`/bugs/${id}`)
    load()
  } catch { /* cancel */ }
}

onMounted(() => { load(); loadSprints() })
watch(() => props.projectId, () => { load(); loadSprints() })
</script>

<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">+ 登记缺陷</el-button>
      <el-input v-model="keyword" clearable placeholder="搜索缺陷、处理人或版本" class="search" />
      <el-select v-model="filter" class="filter" aria-label="缺陷筛选">
        <el-option label="全部缺陷" value="all" />
        <el-option label="未解决" value="open" />
        <el-option label="高/紧急" value="serious" />
        <el-option label="未指派" value="unassigned" />
      </el-select>
    </div>
    <div class="table-frame">
      <el-table :data="filteredList" v-loading="loading" stripe :empty-text="keyword || filter !== 'all' ? '没有匹配的缺陷' : '还没有登记缺陷，点击上方登记'" @row-click="openDetail" class="clickable-table">
      <el-table-column prop="title" label="缺陷" min-width="200" show-overflow-tooltip />
      <el-table-column label="严重程度" width="100">
        <template #default="{ row }">
          <span class="badge" :class="badgeClass('severity', row.severity)">{{ bugSeverity[row.severity] }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="130">
        <template #default="{ row }">
          <el-select :model-value="row.status" :disabled="statusSavingIds[row.id]" size="small" style="width: 110px" @change="(v) => changeStatus(row, v)">
            <el-option v-for="(l, k) in bugStatus" :key="k" :label="l" :value="k" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="assignee" label="处理人" width="100" />
      <el-table-column prop="reporter" label="报告人" width="100" />
      <el-table-column prop="fixVersion" label="解决版本" width="100" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click.stop="openDetail(row)">详情</el-button>
          <el-button size="small" @click.stop="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click.stop="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="editing ? '编辑缺陷' : '登记缺陷'" width="640px" destroy-on-close :close-on-click-modal="!saving" :close-on-press-escape="!saving" :show-close="!saving">
      <el-form label-width="90px" size="default" :disabled="saving">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="迭代">
          <el-select v-model="form.sprintId" clearable style="width: 100%" placeholder="不选则无迭代">
            <el-option v-for="s in sprints" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度">
          <el-select v-model="form.severity" style="width: 100%">
            <el-option v-for="(l, k) in bugSeverity" :key="k" :label="l" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority" style="width: 100%">
            <el-option v-for="(l, k) in reqPriority" :key="k" :label="l" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="(l, k) in bugStatus" :key="k" :label="l" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理人"><el-input v-model="form.assignee" /></el-form-item>
        <el-form-item label="报告人"><el-input v-model="form.reporter" /></el-form-item>
        <el-form-item label="解决版本"><el-input v-model="form.fixVersion" placeholder="如 v1.1.0" /></el-form-item>
        <el-form-item label="复现步骤"><el-input v-model="form.steps" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="期望结果"><el-input v-model="form.expectedResult" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="实际结果"><el-input v-model="form.actualResult" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="详情"><RichEditor v-model="form.content" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="saving" @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <WorkItemDetail v-model:visible="detailVisible" work-type="bug" :work-id="detailId" @changed="load" />
  </div>
</template>

<style scoped>
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 14px; padding-bottom: 10px; border-bottom: 1px solid var(--border); }
.search { max-width: 300px; margin-left: auto; }
.filter { width: 130px; }
.table-frame { margin-bottom: 14px; }
.clickable-table :deep(.el-table__row) { cursor: pointer; }
@media (max-width: 720px) {
  .toolbar { flex-wrap: wrap; }
  .search { order: 3; width: 100%; max-width: none; margin-left: 0; }
}
</style>
