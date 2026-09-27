<script setup>
import { onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { sprintStatus, badgeClass } from '../constants'

const props = defineProps({ projectId: { type: Number, required: true } })

const list = ref([])
const loading = ref(false)
const dialog = ref(false)
const editing = ref(false)
const form = ref({})
const burn = ref(null)
const burnDialog = ref(false)
const burnLoading = ref(false)

async function load() {
  loading.value = true
  try { list.value = (await api.get('/sprints', { params: { projectId: props.projectId } })).data }
  catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

function openAdd() {
  editing.value = false
  form.value = { projectId: props.projectId, name: '', goal: '', startDate: '', endDate: '', status: 'planning' }
  dialog.value = true
}

function openEdit(row) {
  editing.value = true
  form.value = { ...row }
  dialog.value = true
}

async function save() {
  if (!form.value.name?.trim()) return ElMessage.warning('请填写迭代名称')
  try {
    if (editing.value) await api.put('/sprints', form.value)
    else await api.post('/sprints', form.value)
    ElMessage.success('保存成功')
    dialog.value = false
    load()
  } catch (e) { ElMessage.error(e.message) }
}

async function remove(id) {
  try {
    await ElMessageBox.confirm('删除后任务/缺陷将脱离该迭代，确定？', '提示', { type: 'warning' })
    await api.delete(`/sprints/${id}`)
    load()
  } catch { /* cancel */ }
}

async function showBurndown(row) {
  burnDialog.value = true
  burnLoading.value = true
  burn.value = null
  try { burn.value = (await api.get(`/sprints/${row.id}/burndown`)).data }
  catch (e) { ElMessage.error(e.message) }
  finally { burnLoading.value = false }
}

// 极简 SVG 燃尽图：横轴日期、纵轴剩余数
function burnSvg() {
  if (!burn.value || !burn.value.labels?.length) return null
  const { labels, ideal, actual } = burn.value
  const W = 640, H = 240, pad = 36
  const maxY = Math.max(...ideal, ...actual, 1)
  const stepX = (W - pad * 2) / Math.max(labels.length - 1, 1)
  const pt = (arr, i) => `${pad + i * stepX},${H - pad - (arr[i] / maxY) * (H - pad * 2)}`
  const idealPath = ideal.map((_, i) => pt(ideal, i)).join(' L ')
  const actualPath = actual.map((_, i) => pt(actual, i)).join(' L ')
  return { W, H, pad, labels, idealPath, actualPath, maxY }
}

onMounted(load)
watch(() => props.projectId, load)
</script>

<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">+ 新建迭代</el-button>
    </div>
    <div class="table-frame">
      <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="name" label="迭代" min-width="160" />
      <el-table-column prop="goal" label="目标" min-width="180" show-overflow-tooltip />
      <el-table-column label="周期" width="200">
        <template #default="{ row }">{{ row.startDate }} ~ {{ row.endDate }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <span class="badge" :class="badgeClass('sprint', row.status)">{{ sprintStatus[row.status] }}</span>
        </template>
      </el-table-column>
      <el-table-column label="工作项" width="140">
        <template #default="{ row }">
          <el-progress :percentage="row.progress || 0" :stroke-width="8" />
          <span class="mini">{{ row.workDone }}/{{ row.workTotal }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showBurndown(row)">燃尽图</el-button>
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="editing ? '编辑迭代' : '新建迭代'" width="520px" destroy-on-close>
      <el-form label-width="80px" size="default">
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="如 Sprint 2" /></el-form-item>
        <el-form-item label="目标"><el-input v-model="form.goal" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="开始日期"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="结束日期"><el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="(l, k) in sprintStatus" :key="k" :label="l" :value="k" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="burnDialog" title="燃尽图" width="720px" destroy-on-close>
      <div v-loading="burnLoading" style="min-height: 260px">
        <template v-if="burnSvg()">
          <p class="burn-tip">完成 {{ burn.done }}/{{ burn.total }}（{{ burn.progress }}%）</p>
          <svg :viewBox="`0 0 ${burnSvg().W} ${burnSvg().H}`" class="burn-svg">
            <line v-for="i in 5" :key="i" :x1="burnSvg().pad" :x2="burnSvg().W - burnSvg().pad"
                  :y1="burnSvg().H - burnSvg().pad - (i / 5) * (burnSvg().H - burnSvg().pad * 2)"
                  :y2="burnSvg().H - burnSvg().pad - (i / 5) * (burnSvg().H - burnSvg().pad * 2)"
                  stroke="rgba(255,255,255,0.08)" stroke-width="1" />
            <polyline :points="'M' + burnSvg().idealPath" fill="none" stroke="var(--purple)" stroke-width="2" stroke-dasharray="4 4" />
            <polyline :points="'M' + burnSvg().actualPath" fill="none" stroke="var(--cyan)" stroke-width="2.5" />
            <text v-for="(l, i) in burnSvg().labels" :key="i" :x="burnSvg().pad + i * burnSvg().stepX"
                  :y="burnSvg().H - 8" font-size="10" fill="rgba(255,255,255,0.4)" text-anchor="middle">{{ l.slice(5) }}</text>
          </svg>
        </template>
        <p v-else-if="!burnLoading" class="empty">该迭代暂无燃尽数据</p>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 14px; padding-bottom: 10px; border-bottom: 1px solid var(--border); }
.mini { font-size: 12px; color: var(--muted); }
.burn-tip { margin-bottom: 8px; color: var(--muted); font-size: 14px; }
.burn-svg { width: 100%; height: auto; }
.empty { color: var(--muted); text-align: center; padding: 60px 0; }
.table-frame { margin-bottom: 14px; }
</style>
