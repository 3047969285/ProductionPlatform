<script setup>
import { onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { milestoneStatus, releaseStatus, releaseEnv, badgeClass } from '../constants'

const props = defineProps({ projectId: { type: Number, required: true } })

const sub = ref('milestones')
const milestones = ref([])
const msLoading = ref(false)
const msDialog = ref(false)
const msEditing = ref(false)
const msSaving = ref(false)
const msForm = ref({})

const releases = ref([])
const relLoading = ref(false)
const relDialog = ref(false)
const relEditing = ref(false)
const relSaving = ref(false)
const relForm = ref({})

async function loadMilestones() {
  msLoading.value = true
  try { milestones.value = (await api.get('/milestones', { params: { projectId: props.projectId } })).data }
  catch (e) { ElMessage.error(e.message) }
  finally { msLoading.value = false }
}

async function loadReleases() {
  relLoading.value = true
  try { releases.value = (await api.get('/releases', { params: { projectId: props.projectId } })).data }
  catch (e) { ElMessage.error(e.message) }
  finally { relLoading.value = false }
}

function onSubChange() {
  if (sub.value === 'milestones') loadMilestones()
  if (sub.value === 'releases') loadReleases()
}

function openMsAdd() {
  msEditing.value = false
  msForm.value = { projectId: props.projectId, name: '', description: '', dueDate: '', status: 'pending' }
  msDialog.value = true
}
function openMsEdit(row) {
  msEditing.value = true
  msForm.value = { ...row }
  msDialog.value = true
}
async function saveMs() {
  if (msSaving.value) return
  if (!msForm.value.name?.trim()) return ElMessage.warning('请填写名称')
  msForm.value.name = msForm.value.name.trim()
  msSaving.value = true
  try {
    if (msEditing.value) await api.put('/milestones', msForm.value)
    else await api.post('/milestones', msForm.value)
    ElMessage.success('保存成功')
    msDialog.value = false
    await loadMilestones()
  } catch (e) { ElMessage.error(e.message) }
  finally { msSaving.value = false }
}
async function removeMs(id) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await api.delete(`/milestones/${id}`)
    ElMessage.success('已删除')
    await loadMilestones()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

function openRelAdd() {
  relEditing.value = false
  relForm.value = { projectId: props.projectId, version: '', environment: 'test', description: '', status: 'planned', operator: '' }
  relDialog.value = true
}
function openRelEdit(row) {
  relEditing.value = true
  relForm.value = { ...row }
  relDialog.value = true
}
async function saveRel() {
  if (relSaving.value) return
  if (!relForm.value.version?.trim()) return ElMessage.warning('请填写版本号')
  relForm.value.version = relForm.value.version.trim()
  relSaving.value = true
  try {
    if (relEditing.value) await api.put('/releases', relForm.value)
    else await api.post('/releases', relForm.value)
    ElMessage.success('保存成功')
    relDialog.value = false
    await loadReleases()
  } catch (e) { ElMessage.error(e.message) }
  finally { relSaving.value = false }
}
async function removeRel(id) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await api.delete(`/releases/${id}`)
    ElMessage.success('已删除')
    await loadReleases()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

onMounted(loadMilestones)
watch(() => props.projectId, () => { loadMilestones(); if (sub.value === 'releases') loadReleases() })
</script>

<template>
  <div>
    <el-tabs v-model="sub" @tab-change="onSubChange">
      <el-tab-pane label="里程碑" name="milestones">
        <div class="toolbar">
          <el-button type="primary" @click="openMsAdd">+ 新建里程碑</el-button>
        </div>
        <div class="table-frame">
          <el-table :data="milestones" v-loading="msLoading" stripe>
          <el-table-column prop="name" label="里程碑" min-width="180" />
          <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
          <el-table-column prop="dueDate" label="计划日期" width="120" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span class="badge" :class="badgeClass('ms', row.status)">{{ milestoneStatus[row.status] }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openMsEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="removeMs(row.id)">删除</el-button>
            </template>
          </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="发布记录" name="releases">
        <div class="toolbar">
          <el-button type="primary" @click="openRelAdd">+ 登记发布</el-button>
        </div>
        <div class="table-frame">
          <el-table :data="releases" v-loading="relLoading" stripe>
          <el-table-column prop="version" label="版本" width="120" />
          <el-table-column label="环境" width="90">
            <template #default="{ row }">
              <span class="badge" :class="badgeClass('env', row.environment)">{{ releaseEnv[row.environment] }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span class="badge" :class="badgeClass('rel', row.status)">{{ releaseStatus[row.status] }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人" width="100" />
          <el-table-column label="发布时间" width="160">
            <template #default="{ row }">{{ (row.releasedAt || '').replace('T', ' ').slice(0, 16) || '—' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openRelEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="removeRel(row.id)">删除</el-button>
            </template>
          </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="msDialog" :title="msEditing ? '编辑里程碑' : '新建里程碑'" width="480px" destroy-on-close :close-on-click-modal="!msSaving" :close-on-press-escape="!msSaving" :show-close="!msSaving">
      <el-form label-width="80px" size="default" :disabled="msSaving">
        <el-form-item label="名称"><el-input v-model="msForm.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="msForm.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="计划日期"><el-date-picker v-model="msForm.dueDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="msForm.status" style="width: 100%">
            <el-option v-for="(l, k) in milestoneStatus" :key="k" :label="l" :value="k" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="msSaving" @click="msDialog = false">取消</el-button>
        <el-button type="primary" :loading="msSaving" @click="saveMs">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="relDialog" :title="relEditing ? '编辑发布' : '登记发布'" width="520px" destroy-on-close :close-on-click-modal="!relSaving" :close-on-press-escape="!relSaving" :show-close="!relSaving">
      <el-form label-width="80px" size="default" :disabled="relSaving">
        <el-form-item label="版本号"><el-input v-model="relForm.version" placeholder="如 v1.0.0" /></el-form-item>
        <el-form-item label="环境">
          <el-select v-model="relForm.environment" style="width: 100%">
            <el-option v-for="(l, k) in releaseEnv" :key="k" :label="l" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="relForm.status" style="width: 100%">
            <el-option v-for="(l, k) in releaseStatus" :key="k" :label="l" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人"><el-input v-model="relForm.operator" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="relForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="relSaving" @click="relDialog = false">取消</el-button>
        <el-button type="primary" :loading="relSaving" @click="saveRel">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 14px; padding-bottom: 10px; border-bottom: 1px solid var(--border); }
.table-frame { margin-bottom: 14px; }
</style>
