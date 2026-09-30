<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import RichEditor from './RichEditor.vue'
import WorkItemDetail from './WorkItemDetail.vue'
import { buildFolderTree, reqStatus, reqPriority, apiStatus, httpMethods, badgeClass } from '../constants'

const props = defineProps({
  projectId: { type: Number, required: true },
  moduleType: { type: String, required: true },
  apiPath: { type: String, required: true },
  noField: { type: String, default: 'reqNo' },
  isApi: { type: Boolean, default: false },
})

const folders = ref([])
const folderTree = computed(() => buildFolderTree(folders.value))
const selectedFolderId = ref(null)
const list = ref([])
const loading = ref(false)
const dialog = ref(false)
const editing = ref(false)
const form = ref({})

const statusMap = computed(() => (props.isApi ? apiStatus : reqStatus))
const statusOptions = computed(() => Object.entries(statusMap.value))

async function loadFolders() {
  folders.value = (await api.get('/folders', { params: { projectId: props.projectId, moduleType: props.moduleType } })).data
}

async function loadList() {
  loading.value = true
  try {
    const params = { projectId: props.projectId }
    if (selectedFolderId.value) params.folderId = selectedFolderId.value
    list.value = (await api.get(props.apiPath, { params })).data
  } finally {
    loading.value = false
  }
}

function openAdd() {
  editing.value = false
  form.value = {
    projectId: props.projectId,
    folderId: selectedFolderId.value,
    priority: 'medium',
    status: 'draft',
    method: 'GET',
    content: '',
    proposer: '',
    owner: '',
  }
  dialog.value = true
}

function openEdit(row) {
  editing.value = true
  form.value = { ...row }
  dialog.value = true
}

async function save() {
  if (!form.value.title?.trim()) return ElMessage.warning('请填写标题')
  try {
    if (editing.value) await api.put(props.apiPath, form.value)
    else await api.post(props.apiPath, form.value)
    ElMessage.success('保存成功')
    dialog.value = false
    loadList()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function remove(id) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await api.delete(`${props.apiPath}/${id}`)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

const detailVisible = ref(false)
const detailId = ref(null)

function openDetail(row) {
  detailId.value = row.id
  detailVisible.value = true
}

async function addFolder() {
  try {
    const { value } = await ElMessageBox.prompt('文件夹名称', '新建文件夹', {
      inputPattern: /\S+/,
      inputErrorMessage: '名称不能为空',
    })
    await api.post('/folders', {
      projectId: props.projectId,
      moduleType: props.moduleType,
      parentId: selectedFolderId.value,
      name: value,
    })
    ElMessage.success('文件夹已创建')
    loadFolders()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '创建失败')
  }
}

async function renameFolder(folder) {
  try {
    const { value } = await ElMessageBox.prompt('文件夹名称', '重命名', {
      inputValue: folder.name,
      inputPattern: /\S+/,
      inputErrorMessage: '名称不能为空',
    })
    await api.put('/folders', {
      id: folder.id,
      projectId: folder.projectId,
      moduleType: folder.moduleType,
      parentId: folder.parentId,
      name: value,
      sortOrder: folder.sortOrder,
    })
    ElMessage.success('已重命名')
    loadFolders()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '重命名失败')
  }
}

async function removeFolder(folder) {
  try {
    await ElMessageBox.confirm(
      `删除文件夹「${folder.name}」将同时删除其下全部${props.isApi ? '接口' : '需求'}，确定？`,
      '提示',
      { type: 'warning' },
    )
    await api.delete(`/folders/${folder.id}`)
    if (selectedFolderId.value === folder.id) {
      selectedFolderId.value = null
    }
    ElMessage.success('文件夹已删除')
    loadFolders()
    loadList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

function onFolderClick(data) {
  selectedFolderId.value = data.id === selectedFolderId.value ? null : data.id
  loadList()
}

watch(() => props.projectId, () => { loadFolders(); loadList() })
onMounted(() => { loadFolders(); loadList() })
</script>

<template>
  <div class="doc-panel">
    <aside class="sidebar">
      <div class="side-head">
        <span>文件夹</span>
        <el-button size="default" text type="primary" @click="addFolder">+ 新建</el-button>
      </div>
      <el-tree
        :data="folderTree"
        node-key="id"
        default-expand-all
        highlight-current
        :expand-on-click-node="false"
        @node-click="onFolderClick"
      >
        <template #default="{ node, data }">
          <div class="tree-node" :class="{ selected: selectedFolderId === data.id }">
            <span class="tree-label" @click.stop="onFolderClick(data)">{{ data.name }}</span>
            <span class="tree-actions">
              <span class="tree-action" title="重命名" @click.stop="renameFolder(data)">改名</span>
              <span class="tree-action del" title="删除" @click.stop="removeFolder(data)">删除</span>
            </span>
          </div>
        </template>
      </el-tree>
      <el-button class="all-btn" text @click="selectedFolderId = null; loadList()">查看全部</el-button>
    </aside>
    <div class="main">
      <div class="toolbar">
        <el-button type="primary" @click="openAdd">+ 新增{{ isApi ? '接口' : '需求' }}</el-button>
      </div>
      <el-table
        :data="list"
        v-loading="loading"
        stripe
        empty-text="暂无数据，可点击「查看全部」或新增内容"
        @row-click="openDetail"
        class="clickable-table"
      >
        <el-table-column :prop="noField" label="编号" width="140" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column v-if="isApi" prop="method" label="方法" width="90" />
        <el-table-column v-if="isApi" prop="path" label="路径" min-width="140" show-overflow-tooltip />
        <el-table-column v-if="!isApi" label="优先级" width="90">
          <template #default="{ row }">
            <span class="badge" :class="badgeClass('priority', row.priority)">{{ reqPriority[row.priority] }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span class="badge" :class="badgeClass(isApi ? 'api' : 'req', row.status)">{{ statusMap[row.status] }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="proposer" label="提出人" width="100" />
        <el-table-column prop="owner" label="负责人" width="100" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="default" @click.stop="openDetail(row)">详情</el-button>
            <el-button size="default" @click.stop="openEdit(row)">编辑</el-button>
            <el-button size="default" type="danger" @click.stop="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="editing ? '编辑' : '新增'" width="640px" destroy-on-close>
      <el-form label-width="80px" size="default">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item v-if="isApi" label="方法">
          <el-select v-model="form.method" style="width: 100%">
            <el-option v-for="m in httpMethods" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isApi" label="路径"><el-input v-model="form.path" placeholder="/api/..." /></el-form-item>
        <el-form-item v-if="!isApi" label="优先级">
          <el-select v-model="form.priority" style="width: 100%">
            <el-option v-for="(l, k) in reqPriority" :key="k" :label="l" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="[k, l] in statusOptions" :key="k" :label="l" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="提出人"><el-input v-model="form.proposer" /></el-form-item>
        <el-form-item label="负责人"><el-input v-model="form.owner" /></el-form-item>
        <el-form-item label="文件夹">
          <el-select v-model="form.folderId" clearable style="width: 100%" placeholder="可选">
            <el-option v-for="f in folders" :key="f.id" :label="f.name" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容"><RichEditor v-model="form.content" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <WorkItemDetail v-model:visible="detailVisible" :work-type="isApi ? 'api' : 'requirement'" :work-id="detailId" @changed="loadList" />
  </div>
</template>

<style scoped>
.doc-panel { display: grid; grid-template-columns: 220px 1fr; gap: 16px; min-height: 400px; }
.sidebar {
  padding: 12px;
  background: rgba(255, 255, 255, .025);
  border: 1px solid var(--border);
  border-radius: var(--radius);
}
.side-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; font-size: 15px; color: var(--muted); }
.all-btn { width: 100%; margin-top: 8px; font-size: 14px; }
.toolbar { margin-bottom: 12px; }
.main { min-width: 0; }
:deep(.el-tree) { background: transparent; color: var(--text); font-size: 15px; }
:deep(.el-tree-node__content:hover) { background: rgba(239, 239, 235, .06); }
:deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content) {
  background: rgba(239, 239, 235, .1);
  color: var(--cyan);
}
:deep(.el-tree-node__content) { height: 32px; }
.tree-node {
  flex: 1; display: flex; align-items: center; justify-content: space-between;
  min-width: 0; padding-right: 4px;
}
.tree-label {
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  cursor: pointer; flex: 1;
}
.tree-actions { display: flex; gap: 6px; flex-shrink: 0; }
.tree-action {
  font-size: 12px; color: var(--accent); cursor: pointer;
  border: 1px solid rgba(239, 239, 235, .25);
  border-radius: var(--radius); padding: 1px 8px;
  background: rgba(239, 239, 235, .06);
  opacity: 0.8;
  font-weight: 500;
}
.tree-action:hover { opacity: 1; background: rgba(239, 239, 235, .12); }
.tree-action.del { color: var(--pink); border-color: rgba(245, 108, 108, 0.3); background: rgba(245, 108, 108, 0.06); }
.tree-action.del:hover { background: rgba(245, 108, 108, 0.15); }
.clickable-table :deep(.el-table__row) { cursor: pointer; }
@media (max-width: 768px) { .doc-panel { grid-template-columns: 1fr; } .sidebar { order: 2; } }
</style>
