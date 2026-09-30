<script setup>
import { onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { reqPriority, testCaseStatus, testPlanStatus, execStatus, badgeClass } from '../constants'

const props = defineProps({ projectId: { type: Number, required: true } })

const sub = ref('cases')

// 用例库
const cases = ref([])
const caseLoading = ref(false)
const caseDialog = ref(false)
const caseEditing = ref(false)
const caseSaving = ref(false)
const caseForm = ref({})

// 测试计划
const plans = ref([])
const planLoading = ref(false)
const planDialog = ref(false)
const planEditing = ref(false)
const planSaving = ref(false)
const planForm = ref({})
const planCases = ref([])
const planCasesLoading = ref(false)
const planCasesDialog = ref(false)
const activePlan = ref(null)
let planCasesRequest = 0

async function loadCases() {
  caseLoading.value = true
  try { cases.value = (await api.get('/test-cases', { params: { projectId: props.projectId } })).data }
  catch (e) { ElMessage.error(e.message) }
  finally { caseLoading.value = false }
}

async function loadPlans() {
  planLoading.value = true
  try { plans.value = (await api.get('/test-plans', { params: { projectId: props.projectId } })).data }
  catch (e) { ElMessage.error(e.message) }
  finally { planLoading.value = false }
}

function onSubChange() {
  if (sub.value === 'cases') loadCases()
  if (sub.value === 'plans') loadPlans()
}

// 用例 CRUD
function openCaseAdd() {
  caseEditing.value = false
  caseForm.value = { projectId: props.projectId, title: '', preconditions: '', steps: '', expectedResult: '', priority: 'medium', status: 'active', owner: '' }
  caseDialog.value = true
}
function openCaseEdit(row) {
  caseEditing.value = true
  caseForm.value = { ...row }
  caseDialog.value = true
}
async function saveCase() {
  if (caseSaving.value) return
  if (!caseForm.value.title?.trim()) return ElMessage.warning('请填写用例标题')
  caseForm.value.title = caseForm.value.title.trim()
  caseSaving.value = true
  try {
    if (caseEditing.value) await api.put('/test-cases', caseForm.value)
    else await api.post('/test-cases', caseForm.value)
    ElMessage.success('保存成功')
    caseDialog.value = false
    await loadCases()
  } catch (e) { ElMessage.error(e.message) }
  finally { caseSaving.value = false }
}
async function removeCase(id) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await api.delete(`/test-cases/${id}`)
    loadCases()
  } catch { /* cancel */ }
}

// 计划 CRUD
function openPlanAdd() {
  planEditing.value = false
  planForm.value = { projectId: props.projectId, name: '', description: '', startDate: '', endDate: '', status: 'draft', owner: '' }
  planDialog.value = true
}
function openPlanEdit(row) {
  planEditing.value = true
  planForm.value = { ...row }
  planDialog.value = true
}
async function savePlan() {
  if (planSaving.value) return
  if (!planForm.value.name?.trim()) return ElMessage.warning('请填写计划名称')
  planForm.value.name = planForm.value.name.trim()
  planSaving.value = true
  try {
    if (planEditing.value) await api.put('/test-plans', planForm.value)
    else await api.post('/test-plans', planForm.value)
    ElMessage.success('保存成功')
    planDialog.value = false
    await loadPlans()
  } catch (e) { ElMessage.error(e.message) }
  finally { planSaving.value = false }
}
async function removePlan(id) {
  try {
    await ElMessageBox.confirm('删除计划将同时移除其用例关联，确定？', '提示', { type: 'warning' })
    await api.delete(`/test-plans/${id}`)
    loadPlans()
  } catch { /* cancel */ }
}

// 计划内用例
async function loadActivePlanCases() {
  const planId = activePlan.value?.id
  if (!planId) return
  const request = ++planCasesRequest
  planCasesLoading.value = true
  try {
    const result = await api.get(`/test-plans/${planId}/cases`)
    if (request === planCasesRequest && activePlan.value?.id === planId) planCases.value = result.data
  } catch (e) {
    if (request === planCasesRequest) ElMessage.error(e.message || '计划用例加载失败')
  } finally {
    if (request === planCasesRequest) planCasesLoading.value = false
  }
}

async function showPlanCases(plan) {
  activePlan.value = plan
  planCases.value = []
  planCasesDialog.value = true
  await loadActivePlanCases()
}
async function addCasesToPlan() {
  if (!cases.value.length) return ElMessage.warning('用例库为空')
  try {
    const { value } = await ElMessageBox.prompt('输入要加入的用例编号，逗号分隔', '加入用例')
    if (!value) return
    const ids = value.split(/[,，\s]+/).map(Number).filter(Boolean)
    await api.post(`/test-plans/${activePlan.value.id}/cases`, ids)
    ElMessage.success('已加入')
    await loadActivePlanCases()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '加入失败')
  }
}
async function execCase(row, status) {
  try {
    await api.put(`/test-plans/${activePlan.value.id}/cases`, {
      planId: activePlan.value.id, caseId: row.caseId, status, executor: '当前用户',
    })
    await loadActivePlanCases()
  } catch (e) { ElMessage.error(e.message) }
}
async function removePlanCase(row) {
  try {
    await api.delete(`/test-plans/${activePlan.value.id}/cases/${row.caseId}`)
    await loadActivePlanCases()
  } catch (e) { ElMessage.error(e.message) }
}

onMounted(loadCases)
watch(() => props.projectId, () => { loadCases(); if (sub.value === 'plans') loadPlans() })
</script>

<template>
  <div>
    <el-tabs v-model="sub" @tab-change="onSubChange">
      <!-- 用例库 -->
      <el-tab-pane label="用例库" name="cases">
        <div class="toolbar">
          <el-button type="primary" @click="openCaseAdd">+ 新增用例</el-button>
        </div>
        <div class="table-frame">
          <el-table :data="cases" v-loading="caseLoading" stripe>
          <el-table-column prop="id" label="#" width="60" />
          <el-table-column prop="title" label="用例标题" min-width="200" show-overflow-tooltip />
          <el-table-column label="优先级" width="90">
            <template #default="{ row }">
              <span class="badge" :class="badgeClass('priority', row.priority)">{{ reqPriority[row.priority] }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <span class="badge" :class="badgeClass('tcase', row.status)">{{ testCaseStatus[row.status] }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="owner" label="维护人" width="100" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openCaseEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="removeCase(row.id)">删除</el-button>
            </template>
          </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 测试计划 -->
      <el-tab-pane label="测试计划" name="plans">
        <div class="toolbar">
          <el-button type="primary" @click="openPlanAdd">+ 新建计划</el-button>
        </div>
        <div class="table-frame">
          <el-table :data="plans" v-loading="planLoading" stripe>
          <el-table-column prop="name" label="计划" min-width="180" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span class="badge" :class="badgeClass('tplan', row.status)">{{ testPlanStatus[row.status] }}</span>
            </template>
          </el-table-column>
          <el-table-column label="执行进度" width="200">
            <template #default="{ row }">
              <el-progress :percentage="row.progress || 0" :stroke-width="8" />
              <span class="mini">通过 {{ row.casePass }} / 失败 {{ row.caseFail }} / 待执行 {{ row.casePending }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="owner" label="负责人" width="100" />
          <el-table-column label="操作" width="260" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="showPlanCases(row)">执行</el-button>
              <el-button size="small" @click="openPlanEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="removePlan(row.id)">删除</el-button>
            </template>
          </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 用例弹窗 -->
    <el-dialog v-model="caseDialog" :title="caseEditing ? '编辑用例' : '新增用例'" width="620px" destroy-on-close :close-on-click-modal="!caseSaving" :close-on-press-escape="!caseSaving" :show-close="!caseSaving">
      <el-form label-width="90px" size="default" :disabled="caseSaving">
        <el-form-item label="标题"><el-input v-model="caseForm.title" /></el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="caseForm.priority" style="width: 100%">
            <el-option v-for="(l, k) in reqPriority" :key="k" :label="l" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="前置条件"><el-input v-model="caseForm.preconditions" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="测试步骤"><el-input v-model="caseForm.steps" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="预期结果"><el-input v-model="caseForm.expectedResult" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="维护人"><el-input v-model="caseForm.owner" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="caseSaving" @click="caseDialog = false">取消</el-button>
        <el-button type="primary" :loading="caseSaving" @click="saveCase">保存</el-button>
      </template>
    </el-dialog>

    <!-- 计划弹窗 -->
    <el-dialog v-model="planDialog" :title="planEditing ? '编辑计划' : '新建计划'" width="560px" destroy-on-close :close-on-click-modal="!planSaving" :close-on-press-escape="!planSaving" :show-close="!planSaving">
      <el-form label-width="80px" size="default" :disabled="planSaving">
        <el-form-item label="名称"><el-input v-model="planForm.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="planForm.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="起止">
          <div class="date-row">
            <el-date-picker v-model="planForm.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始" />
            <span>~</span>
            <el-date-picker v-model="planForm.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束" />
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="planForm.status" style="width: 100%">
            <el-option v-for="(l, k) in testPlanStatus" :key="k" :label="l" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人"><el-input v-model="planForm.owner" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="planSaving" @click="planDialog = false">取消</el-button>
        <el-button type="primary" :loading="planSaving" @click="savePlan">保存</el-button>
      </template>
    </el-dialog>

    <!-- 计划执行弹窗 -->
    <el-dialog v-model="planCasesDialog" :title="`执行计划：${activePlan?.name || ''}`" width="900px" destroy-on-close>
      <div class="toolbar">
        <el-button size="small" @click="addCasesToPlan">+ 从用例库加入</el-button>
      </div>
      <div class="table-frame">
        <el-table :data="planCases" v-loading="planCasesLoading" stripe>
        <el-table-column prop="caseId" label="#" width="60" />
        <el-table-column prop="caseTitle" label="用例" min-width="160" show-overflow-tooltip />
        <el-table-column prop="steps" label="步骤" min-width="140" show-overflow-tooltip />
        <el-table-column prop="expectedResult" label="预期" min-width="120" show-overflow-tooltip />
        <el-table-column label="执行状态" width="120">
          <template #default="{ row }">
            <el-select :model-value="row.status" size="small" style="width: 100px" @change="(v) => execCase(row, v)">
              <el-option v-for="(l, k) in execStatus" :key="k" :label="l" :value="k" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="removePlanCase(row)">移除</el-button>
          </template>
        </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 14px; padding-bottom: 10px; border-bottom: 1px solid var(--border); }
.mini { font-size: 12px; color: var(--muted); }
.date-row { display: flex; gap: 8px; align-items: center; width: 100%; }
.table-frame { margin-bottom: 14px; }
@media (max-width: 720px) {
  .date-row { flex-wrap: wrap; }
  .date-row :deep(.el-date-editor) { width: 100%; }
}
</style>
