<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '../components/PageShell.vue'
import DocPanel from '../components/DocPanel.vue'
import RichEditor from '../components/RichEditor.vue'
import TaskPanel from '../components/TaskPanel.vue'
import BugPanel from '../components/BugPanel.vue'
import KanbanBoard from '../components/KanbanBoard.vue'
import SprintPanel from '../components/SprintPanel.vue'
import MemberPanel from '../components/MemberPanel.vue'
import TestPanel from '../components/TestPanel.vue'
import ReleasePanel from '../components/ReleasePanel.vue'
import WorkItemDetail from '../components/WorkItemDetail.vue'
import DeliveryCockpit from '../components/DeliveryCockpit.vue'
import DeliveryStageNav from '../components/DeliveryStageNav.vue'
import api from '../api'
import { testStatus, opsStatus, opsSeverity, badgeClass } from '../constants'

const route = useRoute()
const router = useRouter()
const projectId = computed(() => Number(route.params.id))
const projectIdIsValid = computed(() => Number.isInteger(projectId.value) && projectId.value > 0)
const project = ref(null)
const projectError = ref(false)
const tab = ref(typeof route.query.tab === 'string' ? route.query.tab : 'cockpit')

const tests = ref([])
const testLoading = ref(false)
const testError = ref(false)
const testDialog = ref(false)
const testEditing = ref(false)
const testForm = ref({})
const testSaving = ref(false)

const opsList = ref([])
const opsLoading = ref(false)
const opsError = ref(false)
const opsDialog = ref(false)
const opsEditing = ref(false)
const opsForm = ref({})
const opsSaving = ref(false)

// 详情弹窗状态（test/ops 两种）
const detailVisible = ref(false)
const detailType = ref('test')
const detailId = ref(null)
let projectRequestId = 0
let testRequestId = 0
let opsRequestId = 0

// 五段式交付流程：每个阶段保留自己的工作台，避免把所有能力堆在一个菜单里。
const stageDefinitions = [
  {
    key: 'flow', label: '流程', short: '项目全貌', description: '项目全貌 / 迭代 / 成员',
    tabs: [
      { key: 'cockpit', label: '交付驾驶舱', icon: '◈' },
      { key: 'sprint', label: '迭代计划', icon: '↻' },
      { key: 'member', label: '项目成员', icon: '◎' },
    ],
  },
  {
    key: 'requirement', label: '需求', short: '目标与接口', description: '目标 / 需求 / 接口',
    tabs: [
      { key: 'req', label: '需求文档', icon: '▤' },
      { key: 'api', label: '接口文档', icon: '↔' },
    ],
  },
  {
    key: 'development', label: '开发', short: '任务与缺陷', description: '任务 / 看板 / 缺陷',
    tabs: [
      { key: 'task', label: '开发任务', icon: '✓' },
      { key: 'kanban', label: '研发看板', icon: '▦' },
      { key: 'bug', label: '缺陷处理', icon: '!' },
    ],
  },
  {
    key: 'testing', label: '测试', short: '用例与计划', description: '用例 / 计划 / 质量',
    tabs: [
      { key: 'test', label: '用例与计划', icon: '◇' },
      { key: 'test-items', label: '验证任务', icon: '✓' },
    ],
  },
  {
    key: 'deployment', label: '部署', short: '版本与线上', description: '版本 / 环境 / 运维',
    tabs: [
      { key: 'release', label: '发布记录', icon: '↑' },
      { key: 'ops', label: '运维问题', icon: '⌁' },
    ],
  },
]

const activeStageKey = ref('flow')
const activeStage = computed(() => stageDefinitions.find((stage) => stage.key === activeStageKey.value) || stageDefinitions[0])
const activeStageTabs = computed(() => activeStage.value.tabs)
const projectMeta = computed(() => [project.value?.techStack, project.value?.deliveryType].filter(Boolean).join(' · '))

function stageForTab(name) {
  return stageDefinitions.find((stage) => stage.tabs.some((item) => item.key === name)) || stageDefinitions[0]
}

async function loadProject() {
  const requestId = ++projectRequestId
  const requestedProjectId = projectId.value
  projectError.value = false
  if (!projectIdIsValid.value) {
    projectError.value = true
    return
  }
  try {
    const response = await api.get('/projects/' + requestedProjectId)
    if (requestId === projectRequestId && requestedProjectId === projectId.value) {
      if (!response.data) throw new Error('项目不存在')
      project.value = response.data
    }
  } catch {
    if (requestId === projectRequestId && requestedProjectId === projectId.value) projectError.value = true
  }
}

async function loadTests() {
  const requestId = ++testRequestId
  const requestedProjectId = projectId.value
  testLoading.value = true
  testError.value = false
  try {
    const response = await api.get('/tests', { params: { projectId: requestedProjectId } })
    if (requestId === testRequestId && requestedProjectId === projectId.value) tests.value = Array.isArray(response.data) ? response.data : []
  } catch {
    if (requestId === testRequestId && requestedProjectId === projectId.value) {
      testError.value = true
    }
  } finally {
    if (requestId === testRequestId) testLoading.value = false
  }
}

async function loadOps() {
  const requestId = ++opsRequestId
  const requestedProjectId = projectId.value
  opsLoading.value = true
  opsError.value = false
  try {
    const response = await api.get('/ops', { params: { projectId: requestedProjectId } })
    if (requestId === opsRequestId && requestedProjectId === projectId.value) opsList.value = Array.isArray(response.data) ? response.data : []
  } catch {
    if (requestId === opsRequestId && requestedProjectId === projectId.value) {
      opsError.value = true
    }
  } finally {
    if (requestId === opsRequestId) opsLoading.value = false
  }
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
  if (testSaving.value) return
  if (!testForm.value.title?.trim()) return ElMessage.warning('请填写标题')
  testForm.value.title = testForm.value.title.trim()
  testSaving.value = true
  try {
    if (testEditing.value) await api.put('/tests', testForm.value)
    else await api.post('/tests', testForm.value)
    ElMessage.success('保存成功')
    testDialog.value = false
    await loadTests()
  } catch (e) { ElMessage.error(e.message) }
  finally { testSaving.value = false }
}

async function removeTest(id) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error(e?.message || '删除失败')
    return
  }
  try {
    await api.delete('/tests/' + id)
    ElMessage.success('已删除')
    await loadTests()
  } catch (e) { ElMessage.error(e.message || '删除失败') }
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
  if (opsSaving.value) return
  if (!opsForm.value.title?.trim()) return ElMessage.warning('请填写标题')
  opsForm.value.title = opsForm.value.title.trim()
  opsSaving.value = true
  try {
    if (opsEditing.value) await api.put('/ops', opsForm.value)
    else await api.post('/ops', opsForm.value)
    ElMessage.success('保存成功')
    opsDialog.value = false
    await loadOps()
  } catch (e) { ElMessage.error(e.message) }
  finally { opsSaving.value = false }
}

async function removeOps(id) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error(e?.message || '删除失败')
    return
  }
  try {
    await api.delete('/ops/' + id)
    ElMessage.success('已删除')
    await loadOps()
  } catch (e) { ElMessage.error(e.message || '删除失败') }
}

function openDetail(type, id) {
  detailType.value = type
  detailId.value = id
  detailVisible.value = true
}

function onTabChange(name) {
  if (name === 'test-items') loadTests()
  if (name === 'ops') loadOps()
}

function selectStage(name) {
  const stage = stageDefinitions.find((item) => item.key === name)
  if (!stage) return
  activeStageKey.value = stage.key
  selectTab(stage.tabs[0].key)
}

function selectTab(name) {
  tab.value = name
  activeStageKey.value = stageForTab(name).key
  onTabChange(name)
}

watch(projectId, (nextProjectId, previousProjectId) => {
  if (nextProjectId === previousProjectId) return
  project.value = null
  projectError.value = false
  tests.value = []
  opsList.value = []
  testError.value = false
  opsError.value = false
  testLoading.value = false
  opsLoading.value = false
  testRequestId += 1
  opsRequestId += 1
  detailVisible.value = false
  testDialog.value = false
  opsDialog.value = false
  if (!Number.isInteger(nextProjectId) || nextProjectId <= 0) {
    router.push('/projects')
    return
  }
  loadProject()
  if (tab.value === 'test-items') loadTests()
  if (tab.value === 'ops') loadOps()
})

onMounted(() => {
  const validTabs = stageDefinitions.flatMap((stage) => stage.tabs.map((item) => item.key))
  if (!validTabs.includes(tab.value)) tab.value = 'cockpit'
  activeStageKey.value = stageForTab(tab.value).key
  loadProject()
  if (tab.value === 'test-items') loadTests()
  if (tab.value === 'ops') loadOps()
})
</script>

<template>
  <PageShell v-if="project" :tag="project.code" :title="project.name">
    <template #action>
      <el-button @click="router.push('/projects')">← 项目</el-button>
    </template>
    <p v-if="project.description" class="intro">{{ project.description }}</p>
    <p v-if="projectMeta" class="meta">{{ projectMeta }}</p>

    <div class="project-flow">
      <DeliveryStageNav v-model="activeStageKey" :stages="stageDefinitions" @change="selectStage" />

      <div class="project-body">
      <!-- 当前阶段的工作台导航 -->
      <aside class="side-nav">
        <div class="phase-menu-head">
          <span class="phase-number">{{ String(stageDefinitions.findIndex((stage) => stage.key === activeStageKey) + 1).padStart(2, '0') }}</span>
          <div><strong>{{ activeStage.label }}</strong><small>ACTIVE</small></div>
        </div>
        <button
          v-for="it in activeStageTabs"
          :key="it.key"
          class="nav-item"
          :class="{ active: tab === it.key }"
          @click="selectTab(it.key)"
        >
          <span class="nav-icon">{{ it.icon }}</span>
          <span>{{ it.label }}</span>
          <span v-if="tab === it.key" class="nav-arrow">→</span>
        </button>
        <p class="phase-hint">{{ activeStage.short }}</p>
      </aside>

      <!-- 右侧内容区 -->
      <main class="content">
        <Transition name="stage-panel" mode="out-in">
        <!-- 交付驾驶舱：项目进入后的默认入口 -->
        <section v-if="tab === 'cockpit'" key="cockpit">
          <DeliveryCockpit :project-id="projectId" @navigate="selectTab" />
        </section>

        <!-- 需求 -->
        <section v-else-if="tab === 'req'" key="req">
          <DocPanel :project-id="projectId" module-type="requirement" api-path="/requirements" no-field="reqNo" />
        </section>

        <!-- 接口文档 -->
        <section v-else-if="tab === 'api'" key="api">
          <DocPanel :project-id="projectId" module-type="api" api-path="/apis" no-field="apiNo" is-api />
        </section>

        <!-- 任务 -->
        <section v-else-if="tab === 'task'" key="task">
          <TaskPanel :project-id="projectId" />
        </section>

        <!-- 缺陷 -->
        <section v-else-if="tab === 'bug'" key="bug">
          <BugPanel :project-id="projectId" />
        </section>

        <!-- 看板 -->
        <section v-else-if="tab === 'kanban'" key="kanban">
          <KanbanBoard :project-id="projectId" />
        </section>

        <!-- 迭代 -->
        <section v-else-if="tab === 'sprint'" key="sprint">
          <SprintPanel :project-id="projectId" />
        </section>

        <!-- 用例库和测试计划 -->
        <section v-else-if="tab === 'test'" key="test">
          <TestPanel :project-id="projectId" />
        </section>

        <!-- 项目验证任务 -->
        <section v-else-if="tab === 'test-items'" key="test-items">
          <div class="tab-toolbar test-toolbar">
            <p>跟踪验证目标、执行进度与负责人</p>
            <el-button type="primary" @click="openTestAdd">＋ 新建验证任务</el-button>
          </div>
          <div v-if="testError" class="inline-error" role="alert">
            <span>验证任务暂时无法读取</span>
            <el-button link @click="loadTests">重新加载</el-button>
          </div>
          <div v-else class="table-frame">
            <el-table :data="tests" v-loading="testLoading" stripe empty-text="暂无验证任务，点击上方新建" @row-click="(row) => openDetail('test', row.id)" class="clickable-table">
              <el-table-column prop="title" label="验证任务" min-width="220" show-overflow-tooltip />
              <el-table-column label="进度" min-width="180">
                <template #default="{ row }"><el-progress :percentage="Math.max(0, Math.min(100, Number(row.progress) || 0))" :stroke-width="7" /></template>
              </el-table-column>
              <el-table-column prop="owner" label="负责人" width="130"><template #default="{ row }">{{ row.owner || '未指派' }}</template></el-table-column>
              <el-table-column label="状态" width="120">
                <template #default="{ row }"><span class="badge" :class="badgeClass('test', row.status)">{{ testStatus[row.status] || row.status }}</span></template>
              </el-table-column>
              <el-table-column label="操作" width="222" fixed="right">
                <template #default="{ row }">
                  <div class="test-actions">
                    <el-button size="small" @click.stop="openDetail('test', row.id)">详情</el-button>
                    <el-button size="small" @click.stop="openTestEdit(row)">编辑</el-button>
                    <el-button size="small" type="danger" @click.stop="removeTest(row.id)">删除</el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </section>

        <!-- 发布 -->
        <section v-else-if="tab === 'release'" key="release">
          <ReleasePanel :project-id="projectId" />
        </section>

        <!-- 成员 -->
        <section v-else-if="tab === 'member'" key="member">
          <MemberPanel :project-id="projectId" />
        </section>

        <!-- 运维 -->
        <section v-else-if="tab === 'ops'" key="ops">
          <div class="tab-toolbar">
            <el-button type="primary" @click="openOpsAdd">+ 登记问题</el-button>
          </div>
          <div v-if="opsError" class="inline-error" role="alert">
            <span>运维问题暂时无法读取</span>
            <el-button link @click="loadOps">重新加载</el-button>
          </div>
          <el-table v-else :data="opsList" v-loading="opsLoading" stripe empty-text="暂无运维问题，点击上方登记问题" @row-click="(row) => openDetail('ops', row.id)" class="clickable-table">
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
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <div class="ops-actions">
                  <el-button size="small" @click.stop="openDetail('ops', row.id)">详情</el-button>
                  <el-button size="small" @click.stop="openOpsEdit(row)">编辑</el-button>
                  <el-button size="small" type="danger" @click.stop="removeOps(row.id)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </section>
        </Transition>
      </main>
      </div>
    </div>

    <!-- 测试弹窗（保留旧 test_item 编辑入口，与详情并存） -->
    <el-dialog v-model="testDialog" :title="testEditing ? '编辑测试' : '新增测试'" width="520px" destroy-on-close :close-on-click-modal="!testSaving" :close-on-press-escape="!testSaving" :show-close="!testSaving">
      <el-form label-width="80px" size="default" :disabled="testSaving">
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
        <el-button :disabled="testSaving" @click="testDialog = false">取消</el-button>
        <el-button type="primary" :loading="testSaving" @click="saveTest">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="opsDialog" :title="opsEditing ? '编辑问题' : '登记问题'" width="600px" destroy-on-close :close-on-click-modal="!opsSaving" :close-on-press-escape="!opsSaving" :show-close="!opsSaving">
      <el-form label-width="80px" size="default" :disabled="opsSaving">
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
        <el-button :disabled="opsSaving" @click="opsDialog = false">取消</el-button>
        <el-button type="primary" :loading="opsSaving" @click="saveOps">保存</el-button>
      </template>
    </el-dialog>

    <!-- 通用详情弹窗 -->
    <WorkItemDetail v-model:visible="detailVisible" :work-type="detailType" :work-id="detailId" @changed="() => { if (detailType === 'ops') loadOps(); if (detailType === 'test') loadTests() }" />
  </PageShell>
  <PageShell v-else :tag="projectIdIsValid ? `PROJECT / ${String(projectId).padStart(2, '0')}` : 'PROJECT / —'" :title="projectError ? (projectIdIsValid ? '项目暂不可用' : '项目编号无效') : '正在打开项目'">
    <section class="detail-state" :role="projectError ? 'alert' : 'status'" aria-live="polite">
      <span class="detail-state-mark" aria-hidden="true">{{ projectError ? '!' : '···' }}</span>
      <div>
        <h2>{{ projectError ? (projectIdIsValid ? '无法读取这个项目' : '项目编号格式不正确') : '正在同步项目数据' }}</h2>
        <p>{{ projectError ? (projectIdIsValid ? '检查网络或确认项目仍存在，然后重试或返回项目列表。' : '请从项目列表选择一个有效项目。') : '项目基本信息与交付工作区即将就绪。' }}</p>
      </div>
      <div class="detail-state-actions">
        <el-button v-if="projectError && projectIdIsValid" @click="loadProject">重新加载</el-button>
        <el-button @click="router.push('/projects')">返回项目列表</el-button>
      </div>
    </section>
  </PageShell>
</template>

<style scoped>
.intro { font-size: 13px; color: var(--muted); margin-bottom: 6px; line-height: 1.6; }
.meta { font-size: 11px; color: var(--muted-light); margin-bottom: 20px; letter-spacing: .04em; }
.detail-state { display: flex; min-height: 180px; align-items: center; gap: 18px; padding: 28px; border: 1px solid var(--border); border-radius: var(--radius-lg); background: rgba(255,255,255,.018); }
.detail-state-mark { display: grid; width: 42px; height: 42px; flex: 0 0 auto; place-items: center; border: 1px solid var(--border); border-radius: 50%; color: var(--accent); font-family: var(--font-display); }
.detail-state h2 { margin: 0 0 8px; font-size: 15px; font-weight: 500; }
.detail-state p { max-width: 560px; margin: 0; color: var(--muted); font-size: 12px; line-height: 1.7; }
.detail-state-actions { display: flex; gap: 8px; margin-left: auto; }
.project-flow { display: flex; flex-direction: column; gap: 12px; }
.project-body { display: grid; grid-template-columns: 184px minmax(0, 1fr); gap: 12px; align-items: start; }
.side-nav {
  position: sticky; top: calc(var(--nav-h) + 16px);
  background: var(--glass);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: 12px 9px;
  backdrop-filter: blur(16px) saturate(1.3);
  box-shadow: var(--shadow-sm);
}
.phase-menu-head { display: flex; align-items: center; gap: 10px; padding: 2px 8px 14px; border-bottom: 1px solid var(--border); margin-bottom: 10px; }
.phase-number { display: grid; place-items: center; width: 30px; height: 30px; border-radius: 50%; color: var(--accent); background: rgba(239, 239, 235, .1); font-family: var(--font-display); font-size: 13px; font-weight: 700; }
.phase-menu-head strong, .phase-menu-head small { display: block; }
.phase-menu-head strong { font-size: 15px; }
.phase-menu-head small { color: var(--muted); font-size: 11px; margin-top: 2px; }
.nav-item {
  display: flex; align-items: center; gap: 8px;
  width: 100%; padding: 9px 12px; margin-bottom: 2px;
  font-size: 15px; color: var(--muted); text-align: left;
  background: transparent; border: none; border-left: 1px solid transparent; border-radius: 0; cursor: pointer;
  transition: background .25s var(--ease), color .25s var(--ease), border-color .25s var(--ease);
}
.nav-item:hover { background: rgba(239, 239, 235, .05); color: var(--text); }
.nav-item.active { background: rgba(239, 239, 235, .08); border-left-color: var(--accent); color: var(--accent); font-weight: 600; }
.nav-icon { width: 20px; text-align: center; }
.nav-arrow { margin-left: auto; font-size: 16px; }
.phase-hint { padding: 12px 8px 2px; color: var(--muted-light); font-size: 10px; letter-spacing: .08em; }
.content { min-width: 0; background: var(--glass); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 18px; box-shadow: var(--shadow-sm); }
.tab-toolbar { margin-bottom: 12px; }
.test-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.test-toolbar p { color: var(--muted); font-size: 12px; }
.test-actions, .ops-actions { display: flex; align-items: center; gap: 5px; white-space: nowrap; }
.test-actions :deep(.el-button + .el-button), .ops-actions :deep(.el-button + .el-button) { margin-left: 0; }
.inline-error { display: flex; align-items: center; justify-content: space-between; gap: 12px; min-height: 100px; padding: 20px; border: 1px solid var(--border); border-radius: var(--radius); color: var(--muted); }
.clickable-table :deep(.el-table__row) { cursor: pointer; }
:deep(.el-tabs__item) { font-size: 16px; }
:deep(.el-tabs__nav-wrap::after) { background: var(--border); }
.stage-panel-enter-active, .stage-panel-leave-active { transition: opacity .28s var(--ease), transform .28s var(--ease); }
.stage-panel-enter-from { opacity: 0; transform: translateY(10px); }
.stage-panel-leave-to { opacity: 0; transform: translateY(-5px); }
@media (max-width: 768px) {
  .project-body { grid-template-columns: 1fr; }
  .side-nav { position: static; display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 4px; }
  .phase-menu-head, .phase-hint { grid-column: 1 / -1; }
  .test-toolbar { align-items: flex-start; flex-direction: column; }
  .detail-state { align-items: flex-start; flex-wrap: wrap; padding: 20px; }
  .detail-state-actions { width: 100%; margin-left: 60px; }
}
</style>
