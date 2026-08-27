<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import RichEditor from './RichEditor.vue'
import { useListFilter } from '../composables/useListFilter'
import { useUsers } from '../composables/useUsers'
import { formatDateTime } from '../utils/datetime'
import { buildFolderTree, reqStatus, reqPriority, apiStatus, httpMethods, badgeClass } from '../constants'

const props = defineProps({
  projectId: { type: Number, required: true },
  moduleType: { type: String, required: true },
  apiPath: { type: String, required: true },
  noField: { type: String, default: 'reqNo' },
  isApi: { type: Boolean, default: false },
  initialFolderId: { type: Number, default: null },
})

const emit = defineEmits(['update:folderId'])

const folders = ref([])
const folderTree = computed(() => buildFolderTree(folders.value))
const selectedFolderId = ref(props.initialFolderId)
const list = ref([])
const loading = ref(false)
const dialog = ref(false)
const editing = ref(false)
const form = ref({})
const detailVisible = ref(false)
const detailRow = ref(null)

const statusMap = computed(() => (props.isApi ? apiStatus : reqStatus))
const statusOptions = computed(() => Object.entries(statusMap.value))

const { keyword, status: statusFilter, page, pageSize, paged, total } = useListFilter(list, {
  searchFields: props.isApi ? ['title', 'path', 'apiNo'] : ['title', 'reqNo'],
  pageSize: 10,
})

const { loadUsers, toOptions } = useUsers()
const userOptions = computed(() => toOptions())

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

function openDetail(row) {
  detailRow.value = row
  detailVisible.value = true
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

async function changeStatus(row, status) {
  if (row.status === status) return
  const previous = row.status
  row.status = status
  try {
    if (props.isApi) {
      await api.put(props.apiPath, { ...row, status })
    } else {
      await api.put(`${props.apiPath}/${row.id}/status`, null, { params: { status } })
    }
    ElMessage.success('状态已更新')
  } catch (e) {
    row.status = previous
    ElMessage.error(e.message)
  }
}

async function remove(id, title) {
  try {
    await ElMessageBox.confirm(`确定删除「${title || '该记录'}」？`, '提示', { type: 'warning' })
    await api.delete(`${props.apiPath}/${id}`)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
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
      emit('update:folderId', null)
    }
    ElMessage.success('文件夹已删除')
    loadFolders()
    loadList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

function onFolderClick(data) {
  selectedFolderId.value = data.id
  emit('update:folderId', data.id)
  loadList()
}

function clearFolder() {
  selectedFolderId.value = null
  emit('update:folderId', null)
  loadList()
}

watch(() => props.projectId, () => { loadFolders(); loadList() })
watch(() => props.initialFolderId, (value) => {
  if (value !== selectedFolderId.value) {
    selectedFolderId.value = value
    loadList()
  }
})

onMounted(async () => {
  await loadUsers()
  await loadFolders()
  await loadList()
})
</script>

<template>
  <div class="doc-panel">
    <aside class="sidebar surface">
      <div class="side-head">
        <span>文件夹</span>
        <el-button size="default" text type="primary" @click="addFolder">+ 新建</el-button>
      </div>
      <el-tree
        :data="folderTree"
        node-key="id"
        default-expand-all
        highlight-current
        :current-node-key="selectedFolderId"
        :expand-on-click-node="false"
        @node-click="onFolderClick"
      >
        <template #default="{ node, data }">
          <div class="folder-node">
            <span class="folder-label" :title="node.label">{{ node.label }}</span>
            <span class="folder-actions">
              <el-button size="small" text type="primary" @click.stop="renameFolder(data)">重命名</el-button>
              <el-button size="small" text type="danger" @click.stop="removeFolder(data)">删除</el-button>
            </span>
          </div>
        </template>
      </el-tree>
      <el-button class="all-btn" text :type="selectedFolderId ? 'default' : 'primary'" @click="clearFolder">查看全部</el-button>
    </aside>

    <div class="main">
      <div class="toolbar">
        <el-input v-model="keyword" clearable placeholder="搜索编号/标题/路径" class="search" />
        <el-select v-model="statusFilter" clearable placeholder="全部状态" class="status-filter">
          <el-option v-for="[key, label] in statusOptions" :key="key" :label="label" :value="key" />
        </el-select>
        <span class="count">共 {{ total }} 条</span>
        <el-button type="primary" @click="openAdd">+ 新增{{ isApi ? '接口' : '需求' }}</el-button>
      </div>

      <el-table :data="paged" v-loading="loading" stripe empty-text="暂无数据，可调整筛选或新增内容">
        <el-table-column :prop="noField" label="编号" width="140" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <button type="button" class="link-title" @click="openDetail(row)">{{ row.title }}</button>
          </template>
        </el-table-column>
        <el-table-column v-if="isApi" prop="method" label="方法" width="90" />
        <el-table-column v-if="isApi" prop="path" label="路径" min-width="140" show-overflow-tooltip />
        <el-table-column v-if="!isApi" label="优先级" width="90">
          <template #default="{ row }">
            <span class="badge" :class="badgeClass('priority', row.priority)">{{ reqPriority[row.priority] }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-select :model-value="row.status" size="small" @change="(value) => changeStatus(row, value)">
              <el-option v-for="[key, label] in statusOptions" :key="key" :label="label" :value="key" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="owner" label="负责人" width="100" />
        <el-table-column prop="updatedAt" label="更新时间" width="150">
          <template #default="{ row }">{{ formatDateTime(row.updatedAt || row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="default" @click="openDetail(row)">查看</el-button>
            <el-button size="default" @click="openEdit(row)">编辑</el-button>
            <el-button size="default" type="danger" @click="remove(row.id, row.title)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > pageSize" class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total"
        />
      </div>
    </div>

    <el-drawer v-model="detailVisible" :title="detailRow?.title || '详情'" size="520px">
      <template v-if="detailRow">
        <p class="detail-meta"><strong>{{ detailRow[noField] }}</strong> · {{ formatDateTime(detailRow.updatedAt || detailRow.createdAt) }}</p>
        <p v-if="isApi" class="detail-meta">{{ detailRow.method }} {{ detailRow.path }}</p>
        <p v-else class="detail-meta">优先级 {{ reqPriority[detailRow.priority] }} · 状态 {{ statusMap[detailRow.status] }}</p>
        <p class="detail-meta">提出人 {{ detailRow.proposer || '-' }} · 负责人 {{ detailRow.owner || '-' }}</p>
        <div class="detail-content detail-box rich" v-html="detailRow.content || '<p>暂无内容</p>'" />
        <div class="detail-actions">
          <el-button type="primary" @click="openEdit(detailRow); detailVisible = false">编辑</el-button>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="dialog" :title="editing ? '编辑' : '新增'" width="640px" destroy-on-close>
      <el-form label-width="80px" size="default">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item v-if="isApi" label="方法">
          <el-select v-model="form.method" style="width: 100%">
            <el-option v-for="method in httpMethods" :key="method" :label="method" :value="method" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isApi" label="路径"><el-input v-model="form.path" placeholder="/api/..." /></el-form-item>
        <el-form-item v-if="!isApi" label="优先级">
          <el-select v-model="form.priority" style="width: 100%">
            <el-option v-for="(label, key) in reqPriority" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="[key, label] in statusOptions" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="提出人">
          <el-select v-model="form.proposer" filterable allow-create clearable style="width: 100%" placeholder="选择或输入">
            <el-option v-for="option in userOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="form.owner" filterable allow-create clearable style="width: 100%" placeholder="选择或输入">
            <el-option v-for="option in userOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件夹">
          <el-select v-model="form.folderId" clearable style="width: 100%" placeholder="可选">
            <el-option v-for="folder in folders" :key="folder.id" :label="folder.name" :value="folder.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容"><RichEditor v-model="form.content" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.doc-panel {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 16px;
  min-height: 420px;
}

.sidebar {
  padding: 14px;
  align-self: start;
  position: sticky;
  top: calc(var(--nav-h) + 16px);
}

.side-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--muted);
}

.folder-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 4px;
  width: 100%;
  min-width: 0;
  padding-right: 4px;
}

.folder-label {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.folder-actions {
  display: inline-flex;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.2s var(--ease);
}

.folder-node:hover .folder-actions { opacity: 1; }

.all-btn {
  width: 100%;
  margin-top: 10px;
  font-size: 14px;
}

.main { min-width: 0; }

.detail-actions { margin-top: 16px; }

@media (max-width: 768px) {
  .doc-panel { grid-template-columns: 1fr; }
  .sidebar { position: static; order: 2; }
  .folder-actions { opacity: 1; }
}
</style>
