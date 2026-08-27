<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '../components/PageShell.vue'
import DocPanel from '../components/DocPanel.vue'
import RichEditor from '../components/RichEditor.vue'
import api from '../api'
import { useListFilter } from '../composables/useListFilter'
import { useUsers } from '../composables/useUsers'
import { formatDateTime } from '../utils/datetime'
import {
  projectTabs, reqStatus, testStatus, opsStatus, opsSeverity, badgeClass,
} from '../constants'

const route = useRoute()
const router = useRouter()
const projectId = computed(() => Number(route.params.id))
const project = ref(null)
const overview = ref(null)
const overviewLoading = ref(false)

const tab = computed({
  get: () => route.query.tab || 'overview',
  set: (value) => router.replace({ query: { ...route.query, tab: value } }),
})

const folderId = computed({
  get: () => (route.query.folder ? Number(route.query.folder) : null),
  set: (value) => {
    const query = { ...route.query, tab: tab.value }
    if (value) query.folder = String(value)
    else delete query.folder
    router.replace({ query })
  },
})

const tests = ref([])
const testLoading = ref(false)
const testDialog = ref(false)
const testEditing = ref(false)
const testForm = ref({})
const testDetailVisible = ref(false)
const testDetailRow = ref(null)

const opsList = ref([])
const opsLoading = ref(false)
const opsDialog = ref(false)
const opsEditing = ref(false)
const opsForm = ref({})
const opsDetailVisible = ref(false)
const opsDetailRow = ref(null)

const { loadUsers, toOptions } = useUsers()
const userOptions = computed(() => toOptions())

const {
  keyword: testKeyword,
  status: testStatusFilter,
  page: testPage,
  pageSize: testPageSize,
  paged: pagedTests,
  total: testTotal,
} = useListFilter(tests, { searchFields: ['title', 'description'], pageSize: 10 })

const {
  keyword: opsKeyword,
  status: opsStatusFilter,
  page: opsPage,
  pageSize: opsPageSize,
  paged: pagedOps,
  total: opsTotal,
} = useListFilter(opsList, { searchFields: ['title', 'owner', 'reporter'], pageSize: 10 })

const breadcrumbs = computed(() => [
  { label: '项目', to: '/projects' },
  { label: project.value?.name || '加载中', to: null },
])

const overviewCards = computed(() => [
  { key: 'req', label: '需求', value: overview.value?.requirements?.total ?? 0, color: 'var(--accent)' },
  { key: 'api', label: '接口', value: overview.value?.apis ?? 0, color: 'var(--purple)' },
  { key: 'test', label: '测试中', value: overview.value?.tests?.running ?? 0, color: 'var(--warning)' },
  { key: 'ops', label: '待处理运维', value: overview.value?.ops?.open ?? 0, color: 'var(--danger)' },
])

async function loadProject() {
  try {
    project.value = (await api.get(`/projects/${projectId.value}`)).data
  } catch (e) {
    ElMessage.error(e.message)
    router.push('/projects')
  }
}

async function loadOverview() {
  overviewLoading.value = true
  try {
    overview.value = (await api.get(`/projects/${projectId.value}/overview`)).data
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    overviewLoading.value = false
  }
}

async function loadTests() {
  testLoading.value = true
  try {
    tests.value = (await api.get('/tests', { params: { projectId: projectId.value } })).data
  } finally {
    testLoading.value = false
  }
}

async function loadOps() {
  opsLoading.value = true
  try {
    opsList.value = (await api.get('/ops', { params: { projectId: projectId.value } })).data
  } finally {
    opsLoading.value = false
  }
}

function goTab(name) {
  tab.value = name
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

function openTestDetail(row) {
  testDetailRow.value = row
  testDetailVisible.value = true
}

async function saveTest() {
  if (!testForm.value.title) return ElMessage.warning('请填写标题')
  try {
    if (testEditing.value) await api.put('/tests', testForm.value)
    else await api.post('/tests', testForm.value)
    ElMessage.success('保存成功')
    testDialog.value = false
    loadTests()
    loadOverview()
  } catch (e) { ElMessage.error(e.message) }
}

async function changeTestStatus(row, status) {
  const previous = row.status
  row.status = status
  try {
    await api.put('/tests', { ...row, status })
    ElMessage.success('状态已更新')
    loadOverview()
  } catch (e) {
    row.status = previous
    ElMessage.error(e.message)
  }
}

async function removeTest(id, title) {
  try {
    await ElMessageBox.confirm(`确定删除测试项「${title}」？`, '提示', { type: 'warning' })
    await api.delete(`/tests/${id}`)
    loadTests()
    loadOverview()
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

function openOpsDetail(row) {
  opsDetailRow.value = row
  opsDetailVisible.value = true
}

async function saveOps() {
  if (!opsForm.value.title) return ElMessage.warning('请填写标题')
  try {
    if (opsEditing.value) await api.put('/ops', opsForm.value)
    else await api.post('/ops', opsForm.value)
    ElMessage.success('保存成功')
    opsDialog.value = false
    loadOps()
    loadOverview()
  } catch (e) { ElMessage.error(e.message) }
}

async function changeOpsStatus(row, status) {
  const previous = row.status
  row.status = status
  try {
    await api.put('/ops', { ...row, status })
    ElMessage.success('状态已更新')
    loadOverview()
  } catch (e) {
    row.status = previous
    ElMessage.error(e.message)
  }
}

async function removeOps(id, title) {
  try {
    await ElMessageBox.confirm(`确定删除运维问题「${title}」？`, '提示', { type: 'warning' })
    await api.delete(`/ops/${id}`)
    loadOps()
    loadOverview()
  } catch { /* cancel */ }
}

function onTabChange(name) {
  tab.value = name
  if (name === 'overview') loadOverview()
  if (name === 'test') loadTests()
  if (name === 'ops') loadOps()
}

watch(projectId, () => {
  loadProject()
  loadOverview()
  if (tab.value === 'test') loadTests()
  if (tab.value === 'ops') loadOps()
})

onMounted(async () => {
  await loadUsers()
  await loadProject()
  await loadOverview()
  if (tab.value === 'test') loadTests()
  if (tab.value === 'ops') loadOps()
})
</script>

<template>
  <PageShell v-if="project" :tag="project.code" :title="project.name">
    <template #action>
      <el-button @click="router.push('/projects')">← 返回项目列表</el-button>
    </template>

    <nav class="breadcrumb">
      <template v-for="(item, index) in breadcrumbs" :key="item.label">
        <RouterLink v-if="item.to" :to="item.to">{{ item.label }}</RouterLink>
        <span v-else>{{ item.label }}</span>
        <span v-if="index < breadcrumbs.length - 1" class="sep">/</span>
      </template>
    </nav>

    <p class="intro">{{ project.description || '暂无项目描述' }}</p>
    <p class="meta">{{ project.techStack || '未设置技术栈' }} · {{ project.deliveryType }}</p>

    <el-tabs :model-value="tab" class="tabs" @tab-change="onTabChange">
      <el-tab-pane v-for="(label, key) in projectTabs" :key="key" :label="label" :name="key" lazy>
        <template v-if="key === 'overview'">
          <div v-loading="overviewLoading" class="overview">
            <div class="stat-grid">
              <article
                v-for="(card, index) in overviewCards"
                :key="card.key"
                class="stat-card surface surface-hover animate-fade-up"
                :class="`stagger-${index + 1}`"
                @click="goTab(card.key)"
              >
                <span class="stat-number" :style="{ color: card.color }">{{ card.value }}</span>
                <span class="stat-label">{{ card.label }}</span>
              </article>
            </div>
            <section v-if="overview?.requirements" class="status-section">
              <h3 class="section-title">需求状态分布</h3>
              <div class="chips">
                <button
                  v-for="(label, statusKey) in reqStatus"
                  :key="statusKey"
                  type="button"
                  class="chip chip-interactive"
                  @click="goTab('req')"
                >
                  {{ label }} <strong>{{ overview.requirements[statusKey] ?? 0 }}</strong>
                </button>
              </div>
            </section>
            <section v-if="overview?.recentRequirements?.length" class="recent-section">
              <h3 class="section-title">最近需求</h3>
              <ul class="recent-list surface">
                <li v-for="item in overview.recentRequirements" :key="item.id">
                  <span class="no">{{ item.reqNo }}</span>
                  <span class="title">{{ item.title }}</span>
                  <span class="badge" :class="badgeClass('req', item.status)">{{ reqStatus[item.status] }}</span>
                </li>
              </ul>
            </section>
          </div>
        </template>

        <template v-else-if="key === 'req'">
          <DocPanel
            :project-id="projectId"
            module-type="requirement"
            api-path="/requirements"
            no-field="reqNo"
            :initial-folder-id="folderId"
            @update:folder-id="folderId = $event"
          />
        </template>

        <template v-else-if="key === 'api'">
          <DocPanel
            :project-id="projectId"
            module-type="api"
            api-path="/apis"
            no-field="apiNo"
            is-api
            :initial-folder-id="folderId"
            @update:folder-id="folderId = $event"
          />
        </template>

        <template v-else-if="key === 'test'">
          <div class="toolbar">
            <el-input v-model="testKeyword" clearable placeholder="搜索测试项" class="search" />
            <el-select v-model="testStatusFilter" clearable placeholder="全部状态" class="status-filter">
              <el-option v-for="(label, statusKey) in testStatus" :key="statusKey" :label="label" :value="statusKey" />
            </el-select>
            <span class="count">共 {{ testTotal }} 条</span>
            <el-button type="primary" @click="openTestAdd">+ 新增测试</el-button>
          </div>
          <el-table :data="pagedTests" v-loading="testLoading" stripe>
            <el-table-column prop="title" label="测试项" min-width="180">
              <template #default="{ row }">
                <button type="button" class="link-title" @click="openTestDetail(row)">{{ row.title }}</button>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" min-width="160" show-overflow-tooltip />
            <el-table-column label="进度" width="180">
              <template #default="{ row }">
                <el-progress :percentage="row.progress || 0" :stroke-width="10" />
              </template>
            </el-table-column>
            <el-table-column label="状态" width="130">
              <template #default="{ row }">
                <el-select :model-value="row.status" size="small" @change="(value) => changeTestStatus(row, value)">
                  <el-option v-for="(label, statusKey) in testStatus" :key="statusKey" :label="label" :value="statusKey" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="owner" label="负责人" width="110" />
            <el-table-column prop="updatedAt" label="更新时间" width="150">
              <template #default="{ row }">{{ formatDateTime(row.updatedAt || row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button size="default" @click="openTestDetail(row)">查看</el-button>
                <el-button size="default" @click="openTestEdit(row)">编辑</el-button>
                <el-button size="default" type="danger" @click="removeTest(row.id, row.title)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="testTotal > testPageSize" class="pager">
            <el-pagination v-model:current-page="testPage" v-model:page-size="testPageSize" layout="total, prev, pager, next" :total="testTotal" />
          </div>
        </template>

        <template v-else-if="key === 'ops'">
          <div class="toolbar">
            <el-input v-model="opsKeyword" clearable placeholder="搜索运维问题" class="search" />
            <el-select v-model="opsStatusFilter" clearable placeholder="全部状态" class="status-filter">
              <el-option v-for="(label, statusKey) in opsStatus" :key="statusKey" :label="label" :value="statusKey" />
            </el-select>
            <span class="count">共 {{ opsTotal }} 条</span>
            <el-button type="primary" @click="openOpsAdd">+ 登记问题</el-button>
          </div>
          <el-table :data="pagedOps" v-loading="opsLoading" stripe>
            <el-table-column prop="title" label="问题" min-width="180">
              <template #default="{ row }">
                <button type="button" class="link-title" @click="openOpsDetail(row)">{{ row.title }}</button>
              </template>
            </el-table-column>
            <el-table-column label="严重程度" width="100">
              <template #default="{ row }">
                <span class="badge" :class="badgeClass('severity', row.severity)">{{ opsSeverity[row.severity] }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="130">
              <template #default="{ row }">
                <el-select :model-value="row.status" size="small" @change="(value) => changeOpsStatus(row, value)">
                  <el-option v-for="(label, statusKey) in opsStatus" :key="statusKey" :label="label" :value="statusKey" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="owner" label="负责人" width="110" />
            <el-table-column prop="updatedAt" label="更新时间" width="150">
              <template #default="{ row }">{{ formatDateTime(row.updatedAt || row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button size="default" @click="openOpsDetail(row)">查看</el-button>
                <el-button size="default" @click="openOpsEdit(row)">编辑</el-button>
                <el-button size="default" type="danger" @click="removeOps(row.id, row.title)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="opsTotal > opsPageSize" class="pager">
            <el-pagination v-model:current-page="opsPage" v-model:page-size="opsPageSize" layout="total, prev, pager, next" :total="opsTotal" />
          </div>
        </template>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="testDialog" :title="testEditing ? '编辑测试' : '新增测试'" width="520px">
      <el-form label-width="80px" size="default">
        <el-form-item label="标题"><el-input v-model="testForm.title" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="testForm.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="进度"><el-slider v-model="testForm.progress" :max="100" show-input /></el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="testForm.owner" filterable allow-create clearable style="width: 100%">
            <el-option v-for="option in userOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="提出人">
          <el-select v-model="testForm.proposer" filterable allow-create clearable style="width: 100%">
            <el-option v-for="option in userOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="testForm.status" style="width: 100%">
            <el-option v-for="(label, statusKey) in testStatus" :key="statusKey" :label="label" :value="statusKey" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="testDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTest">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="testDetailVisible" :title="testDetailRow?.title || '测试详情'" size="480px">
      <template v-if="testDetailRow">
        <p class="detail-meta">进度 {{ testDetailRow.progress || 0 }}% · 状态 {{ testStatus[testDetailRow.status] }}</p>
        <p class="detail-meta">负责人 {{ testDetailRow.owner || '-' }} · 提出人 {{ testDetailRow.proposer || '-' }}</p>
        <p class="detail-meta">更新于 {{ formatDateTime(testDetailRow.updatedAt || testDetailRow.createdAt) }}</p>
        <div class="detail-box">{{ testDetailRow.description || '暂无说明' }}</div>
      </template>
    </el-drawer>

    <el-dialog v-model="opsDialog" :title="opsEditing ? '编辑问题' : '登记问题'" width="600px">
      <el-form label-width="80px" size="default">
        <el-form-item label="标题"><el-input v-model="opsForm.title" /></el-form-item>
        <el-form-item label="严重程度">
          <el-select v-model="opsForm.severity" style="width: 100%">
            <el-option v-for="(label, statusKey) in opsSeverity" :key="statusKey" :label="label" :value="statusKey" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="opsForm.status" style="width: 100%">
            <el-option v-for="(label, statusKey) in opsStatus" :key="statusKey" :label="label" :value="statusKey" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="opsForm.owner" filterable allow-create clearable style="width: 100%">
            <el-option v-for="option in userOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="提出人">
          <el-select v-model="opsForm.reporter" filterable allow-create clearable style="width: 100%">
            <el-option v-for="option in userOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="详情"><RichEditor v-model="opsForm.content" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="opsDialog = false">取消</el-button>
        <el-button type="primary" @click="saveOps">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="opsDetailVisible" :title="opsDetailRow?.title || '运维详情'" size="520px">
      <template v-if="opsDetailRow">
        <p class="detail-meta">严重程度 {{ opsSeverity[opsDetailRow.severity] }} · 状态 {{ opsStatus[opsDetailRow.status] }}</p>
        <p class="detail-meta">负责人 {{ opsDetailRow.owner || '-' }} · 提出人 {{ opsDetailRow.reporter || '-' }}</p>
        <div class="detail-box rich" v-html="opsDetailRow.content || '<p>暂无详情</p>'" />
      </template>
    </el-drawer>
  </PageShell>
</template>

<style scoped>
.intro { font-size: 16px; color: var(--muted); margin-bottom: 6px; line-height: 1.6; }
.meta { font-size: 14px; color: var(--muted-light); margin-bottom: 20px; }
.tabs { margin-top: 8px; }
.overview { padding: 8px 4px 16px; }
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 14px;
  margin-bottom: 28px;
}
.stat-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 22px 20px;
  cursor: pointer;
}
.stat-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--muted);
}
.status-section,
.recent-section {
  margin-bottom: 28px;
}
.status-section .section-title,
.recent-section .section-title {
  font-size: 1.25rem;
  margin-bottom: 14px;
}
.chips { display: flex; flex-wrap: wrap; gap: 10px; }
.recent-list {
  list-style: none;
  overflow: hidden;
}
.recent-list li {
  display: grid;
  grid-template-columns: 120px 1fr auto;
  gap: 12px;
  padding: 16px 18px;
  align-items: center;
}
.recent-list li + li {
  border-top: 1px solid var(--border);
}
.recent-list .no {
  font-family: var(--font-display);
  font-weight: 600;
  font-size: 14px;
}
.recent-list .title {
  color: var(--muted);
}
</style>
