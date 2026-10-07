<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import WorkItemDetail from './WorkItemDetail.vue'
import RichEditor from './RichEditor.vue'
import { taskStatus, reqPriority, badgeClass } from '../constants'

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
  try { list.value = (await api.get('/tasks', { params: { projectId: props.projectId } })).data }
  catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

const filteredList = computed(() => {
  const value = keyword.value.trim().toLowerCase()
  return list.value.filter((item) => {
    const matchKeyword = !value || `${item.title} ${item.assignee || ''} ${item.sprintName || ''}`.toLowerCase().includes(value)
    const matchFilter = filter.value === 'all'
      || item.status === filter.value
      || (filter.value === 'unassigned' && !item.assignee)
    return matchKeyword && matchFilter
  })
})

function openAdd() {
  editing.value = false
  form.value = { projectId: props.projectId, title: '', content: '', priority: 'medium', status: 'todo', assignee: '', creator: '', estimateHours: 0, sprintId: null }
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
    if (editing.value) await api.put('/tasks', form.value)
    else await api.post('/tasks', form.value)
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
    await api.put(`/tasks/${row.id}/status?status=${status}`)
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
  } catch {
    return
  }
  try {
    await api.delete(`/tasks/${id}`)
    ElMessage.success('已删除')
    await load()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

onMounted(() => { load(); loadSprints() })
watch(() => props.projectId, () => { load(); loadSprints() })
</script>

<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">+ 新增任务</el-button>
      <el-input v-model="keyword" clearable placeholder="搜索任务、负责人或迭代" class="search" />
      <el-select v-model="filter" class="filter" aria-label="任务筛选">
        <el-option label="全部任务" value="all" />
        <el-option v-for="(label, key) in taskStatus" :key="key" :label="label" :value="key" />
        <el-option label="未指派" value="unassigned" />
      </el-select>
    </div>
    <div class="table-frame">
      <el-table :data="filteredList" v-loading="loading" stripe :empty-text="keyword || filter !== 'all' ? '没有匹配的任务' : '还没有任务，点击上方新增任务'" @row-click="openDetail" class="clickable-table">
      <el-table-column prop="title" label="任务" min-width="200" show-overflow-tooltip />
      <el-table-column prop="sprintName" label="迭代" width="150">
        <template #default="{ row }">{{ row.sprintName || '—' }}</template>
      </el-table-column>
      <el-table-column label="优先级" width="90">
        <template #default="{ row }">
          <span class="badge" :class="badgeClass('priority', row.priority)">{{ reqPriority[row.priority] }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="130">
        <template #default="{ row }">
          <el-select :model-value="row.status" :disabled="statusSavingIds[row.id]" size="small" style="width: 110px" @change="(v) => changeStatus(row, v)">
            <el-option v-for="(l, k) in taskStatus" :key="k" :label="l" :value="k" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="assignee" label="负责人" width="100" />
      <el-table-column prop="estimateHours" label="预估工时" width="90" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click.stop="openDetail(row)">详情</el-button>
          <el-button size="small" @click.stop="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click.stop="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="editing ? '编辑任务' : '新增任务'" width="600px" destroy-on-close :close-on-click-modal="!saving" :close-on-press-escape="!saving" :show-close="!saving">
      <el-form label-width="80px" size="default" :disabled="saving">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="迭代">
          <el-select v-model="form.sprintId" clearable style="width: 100%" placeholder="不选则无迭代">
            <el-option v-for="s in sprints" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority" style="width: 100%">
            <el-option v-for="(l, k) in reqPriority" :key="k" :label="l" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="(l, k) in taskStatus" :key="k" :label="l" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人"><el-input v-model="form.assignee" /></el-form-item>
        <el-form-item label="创建人"><el-input v-model="form.creator" /></el-form-item>
        <el-form-item label="预估工时"><el-input-number v-model="form.estimateHours" :min="0" :max="1000" /></el-form-item>
        <el-form-item label="详情"><RichEditor v-model="form.content" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="saving" @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <WorkItemDetail v-model:visible="detailVisible" work-type="task" :work-id="detailId" @changed="load" />
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
