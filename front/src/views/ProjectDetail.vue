<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '../components/PageShell.vue'
import DocPanel from '../components/DocPanel.vue'
import RichEditor from '../components/RichEditor.vue'
import api from '../api'
import { testStatus, opsStatus, opsSeverity, badgeClass } from '../constants'

const route = useRoute()
const router = useRouter()
const projectId = computed(() => Number(route.params.id))
const project = ref(null)
const tab = ref('req')

const tests = ref([])
const testLoading = ref(false)
const testDialog = ref(false)
const testEditing = ref(false)
const testForm = ref({})

const opsList = ref([])
const opsLoading = ref(false)
const opsDialog = ref(false)
const opsEditing = ref(false)
const opsForm = ref({})

async function loadProject() {
  try {
    project.value = (await api.get(`/projects/${projectId.value}`)).data
  } catch (e) {
    ElMessage.error(e.message)
    router.push('/projects')
  }
}

async function loadTests() {
  testLoading.value = true
  try { tests.value = (await api.get('/tests', { params: { projectId: projectId.value } })).data }
  finally { testLoading.value = false }
}

async function loadOps() {
  opsLoading.value = true
  try { opsList.value = (await api.get('/ops', { params: { projectId: projectId.value } })).data }
  finally { opsLoading.value = false }
}

function openTestAdd() {
  testEditing.value = false
  testForm.value = { projectId: projectId.value, title: '', description: '', progress: 0, owner: '', proposer: '', status: 'pending' }
  testDialog.value = true
}

function openTestEdit(row) {
  testEditing.value = true
  testForm.value = { ...row }
  testDialog.value = true
}

async function saveTest() {
  if (!testForm.value.title) return ElMessage.warning('请填写标题')
  try {
    if (testEditing.value) await api.put('/tests', testForm.value)
    else await api.post('/tests', testForm.value)
    ElMessage.success('保存成功')
    testDialog.value = false
    loadTests()
  } catch (e) { ElMessage.error(e.message) }
}

async function removeTest(id) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await api.delete(`/tests/${id}`)
    loadTests()
  } catch { /* cancel */ }
}

function openOpsAdd() {
  opsEditing.value = false
  opsForm.value = { projectId: projectId.value, title: '', content: '', severity: 'medium', status: 'open', owner: '', reporter: '' }
  opsDialog.value = true
}

function openOpsEdit(row) {
  opsEditing.value = true
  opsForm.value = { ...row }
  opsDialog.value = true
}

async function saveOps() {
  if (!opsForm.value.title) return ElMessage.warning('请填写标题')
  try {
    if (opsEditing.value) await api.put('/ops', opsForm.value)
    else await api.post('/ops', opsForm.value)
    ElMessage.success('保存成功')
    opsDialog.value = false
    loadOps()
  } catch (e) { ElMessage.error(e.message) }
}

async function removeOps(id) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await api.delete(`/ops/${id}`)
    loadOps()
  } catch { /* cancel */ }
}

function onTabChange(name) {
  if (name === 'test') loadTests()
  if (name === 'ops') loadOps()
}

onMounted(() => {
  loadProject()
  if (tab.value === 'test') loadTests()
  if (tab.value === 'ops') loadOps()
})
</script>

<template>
  <PageShell v-if="project" :tag="project.code" :title="project.name">
    <template #action>
      <el-button @click="router.push('/projects')">← 返回项目列表</el-button>
    </template>
    <p class="intro">{{ project.description }}</p>
    <p class="meta">{{ project.techStack }} · {{ project.deliveryType }}</p>

    <el-tabs v-model="tab" class="tabs" @tab-change="onTabChange">
      <el-tab-pane label="需求" name="req">
        <DocPanel :project-id="projectId" module-type="requirement" api-path="/requirements" no-field="reqNo" />
      </el-tab-pane>
      <el-tab-pane label="接口" name="api">
        <DocPanel :project-id="projectId" module-type="api" api-path="/apis" no-field="apiNo" is-api />
      </el-tab-pane>
      <el-tab-pane label="测试" name="test">
        <div class="tab-toolbar">
          <el-button type="primary" @click="openTestAdd">+ 新增测试</el-button>
        </div>
        <el-table :data="tests" v-loading="testLoading" stripe>
          <el-table-column prop="title" label="测试项" min-width="180" />
          <el-table-column prop="description" label="说明" min-width="160" show-overflow-tooltip />
          <el-table-column label="进度" width="180">
            <template #default="{ row }">
              <el-progress :percentage="row.progress || 0" :stroke-width="10" />
            </template>
          </el-table-column>
          <el-table-column prop="owner" label="负责人" width="110" />
          <el-table-column prop="proposer" label="提出人" width="110" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span class="badge" :class="badgeClass('test', row.status)">{{ testStatus[row.status] }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button size="default" @click="openTestEdit(row)">编辑</el-button>
              <el-button size="default" type="danger" @click="removeTest(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="运维" name="ops">
        <div class="tab-toolbar">
          <el-button type="primary" @click="openOpsAdd">+ 登记问题</el-button>
        </div>
        <el-table :data="opsList" v-loading="opsLoading" stripe>
          <el-table-column prop="title" label="问题" min-width="180" />
          <el-table-column label="严重程度" width="100">
            <template #default="{ row }">
              <span class="badge" :class="badgeClass('severity', row.severity)">{{ opsSeverity[row.severity] }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span class="badge" :class="badgeClass('ops', row.status)">{{ opsStatus[row.status] }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="owner" label="负责人" width="110" />
          <el-table-column prop="reporter" label="提出人" width="110" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button size="default" @click="openOpsEdit(row)">编辑</el-button>
              <el-button size="default" type="danger" @click="removeOps(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="testDialog" :title="testEditing ? '编辑测试' : '新增测试'" width="520px">
      <el-form label-width="80px" size="default">
        <el-form-item label="标题"><el-input v-model="testForm.title" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="testForm.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="进度"><el-slider v-model="testForm.progress" :max="100" show-input /></el-form-item>
        <el-form-item label="负责人"><el-input v-model="testForm.owner" /></el-form-item>
        <el-form-item label="提出人"><el-input v-model="testForm.proposer" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="testForm.status" style="width: 100%">
            <el-option v-for="(l, k) in testStatus" :key="k" :label="l" :value="k" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="testDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTest">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="opsDialog" :title="opsEditing ? '编辑问题' : '登记问题'" width="600px">
      <el-form label-width="80px" size="default">
        <el-form-item label="标题"><el-input v-model="opsForm.title" /></el-form-item>
        <el-form-item label="严重程度">
          <el-select v-model="opsForm.severity" style="width: 100%">
            <el-option v-for="(l, k) in opsSeverity" :key="k" :label="l" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="opsForm.status" style="width: 100%">
            <el-option v-for="(l, k) in opsStatus" :key="k" :label="l" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人"><el-input v-model="opsForm.owner" /></el-form-item>
        <el-form-item label="提出人"><el-input v-model="opsForm.reporter" /></el-form-item>
        <el-form-item label="详情"><RichEditor v-model="opsForm.content" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="opsDialog = false">取消</el-button>
        <el-button type="primary" @click="saveOps">保存</el-button>
      </template>
    </el-dialog>
  </PageShell>
</template>

<style scoped>
.intro { font-size: 16px; color: var(--muted); margin-bottom: 6px; line-height: 1.6; }
.meta { font-size: 15px; color: rgba(255, 255, 255, 0.35); margin-bottom: 20px; }
.tabs { margin-top: 8px; }
.tab-toolbar { margin-bottom: 12px; }
:deep(.el-tabs__item) { font-size: 16px; }
:deep(.el-tabs__nav-wrap::after) { background: var(--border); }
</style>
