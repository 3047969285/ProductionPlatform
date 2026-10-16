<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { sprintStatus, badgeClass } from '../constants'

const props = defineProps({ projectId: { type: Number, required: true } })

const list = ref([])
const loading = ref(false)
const dialog = ref(false)
const editing = ref(false)
const saving = ref(false)
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
  if (saving.value) return
  if (!form.value.name?.trim()) return ElMessage.warning('请填写迭代名称')
  form.value.name = form.value.name.trim()
  saving.value = true
  try {
    if (editing.value) await api.put('/sprints', form.value)
    else await api.post('/sprints', form.value)
    ElMessage.success('保存成功')
    dialog.value = false
    await load()
  } catch (e) { ElMessage.error(e.message) }
  finally { saving.value = false }
}

async function remove(id) {
  try {
    await ElMessageBox.confirm('删除后任务/缺陷将脱离该迭代，确定？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await api.delete(`/sprints/${id}`)
    ElMessage.success('已删除')
    await load()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

async function showBurndown(row) {
  burnDialog.value = true
  burnLoading.value = true
  burn.value = null
  try { burn.value = (await api.get(`/sprints/${row.id}/burndown`)).data }
  catch (e) { ElMessage.error(e.message) }
  finally { burnLoading.value = false }
}

// SVG 燃尽图：横轴日期、纵轴剩余数。实际线只画到今天（后端对未来日期返回 null）。
const burnChart = computed(() => {
  if (!burn.value || !burn.value.labels?.length) return null
  const { labels, ideal, actual } = burn.value
  const W = 640, H = 260, padX = 40, padTop = 18, padBottom = 34
  const maxY = Math.max(...ideal, ...actual.filter((v) => v != null), 1)
  const stepX = (W - padX * 2) / Math.max(labels.length - 1, 1)
  const x = (i) => padX + i * stepX
  const y = (v) => H - padBottom - (v / maxY) * (H - padTop - padBottom)
  const idealPath = ideal.map((v, i) => `${x(i)},${y(v)}`).join(' L ')
  const actualPoints = actual.map((v, i) => (v == null ? null : { x: x(i), y: y(v), v, label: labels[i] })).filter(Boolean)
  const actualPath = actualPoints.map((p) => `${p.x},${p.y}`).join(' L ')
  const ticks = Array.from({ length: 5 }, (_, k) => {
    const v = Math.round((maxY * k) / 4)
    return { v, y: y(v) }
  })
  // 日期标签最多显示 8 个，避免长迭代挤成一团
  const every = Math.max(1, Math.ceil(labels.length / 8))
  const xLabels = labels.map((l, i) => ({ text: l.slice(5), x: x(i), show: i % every === 0 || i === labels.length - 1 }))
  const last = actualPoints[actualPoints.length - 1] || null
  return { W, H, padX, padBottom, idealPath, actualPath, actualPoints, ticks, xLabels, last }
})
onMounted(load)
watch(() => props.projectId, load)
</script>

<template>
  <div v-reveal>
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

    <el-dialog v-model="dialog" :title="editing ? '编辑迭代' : '新建迭代'" width="520px" destroy-on-close :close-on-click-modal="!saving" :close-on-press-escape="!saving" :show-close="!saving">
      <el-form label-width="80px" size="default" :disabled="saving">
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
        <el-button :disabled="saving" @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="burnDialog" title="燃尽图" width="720px" destroy-on-close>
      <div v-loading="burnLoading" style="min-height: 260px">
        <template v-if="burnChart">
          <p class="burn-tip">
            完成 {{ burn.done }}/{{ burn.total }}（{{ burn.progress }}%）
            <span v-if="burnChart.last"> · 今日剩余 {{ burnChart.last.v }}</span>
          </p>
          <svg :viewBox="`0 0 ${burnChart.W} ${burnChart.H}`" class="burn-svg" role="img" aria-label="迭代燃尽图">
            <g v-for="t in burnChart.ticks" :key="t.v">
              <line :x1="burnChart.padX" :x2="burnChart.W - burnChart.padX" :y1="t.y" :y2="t.y" stroke="rgba(255,255,255,0.08)" stroke-width="1" />
              <text :x="burnChart.padX - 8" :y="t.y + 3" font-size="10" fill="rgba(255,255,255,0.4)" text-anchor="end">{{ t.v }}</text>
            </g>
            <path :d="'M' + burnChart.idealPath" fill="none" stroke="var(--purple)" stroke-width="2" stroke-dasharray="4 4" />
            <path v-if="burnChart.actualPath" :d="'M' + burnChart.actualPath" fill="none" stroke="var(--cyan)" stroke-width="2.5" stroke-linejoin="round" />
            <circle v-for="p in burnChart.actualPoints" :key="p.label" :cx="p.x" :cy="p.y" r="3" fill="var(--cyan)"><title>{{ p.label }}：剩余 {{ p.v }}</title></circle>
            <template v-for="l in burnChart.xLabels" :key="l.text">
              <text v-if="l.show" :x="l.x" :y="burnChart.H - 10" font-size="10" fill="rgba(255,255,255,0.4)" text-anchor="middle">{{ l.text }}</text>
            </template>
          </svg>
          <p class="burn-legend"><i class="ideal" />理想剩余 <i class="real" />实际剩余（按任务和缺陷的真实完成日期）</p>
        </template>        <p v-else-if="!burnLoading" class="empty">该迭代暂无燃尽数据</p>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 14px; padding-bottom: 10px; border-bottom: 1px solid var(--border); }
.mini { font-size: 12px; color: var(--muted); }
.burn-legend { display: flex; align-items: center; gap: 8px; margin-top: 6px; color: var(--muted); font-size: 12px; }
.burn-legend i { display: inline-block; width: 18px; height: 0; border-top: 2px solid; }
.burn-legend i.ideal { border-top-style: dashed; border-color: var(--purple); }
.burn-legend i.real { margin-left: 10px; border-color: var(--cyan); }
.burn-tip { margin-bottom: 8px; color: var(--muted); font-size: 14px; }
.burn-svg { width: 100%; height: auto; }
.empty { color: var(--muted); text-align: center; padding: 60px 0; }
.table-frame { margin-bottom: 14px; }
</style>
