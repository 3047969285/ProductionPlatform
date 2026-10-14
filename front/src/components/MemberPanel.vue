<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { memberRole, badgeClass } from '../constants'

const props = defineProps({ projectId: { type: Number, required: true } })

const list = ref([])
const users = ref([])
const usersLoading = ref(false)
const usersError = ref(false)
const loading = ref(false)
const listError = ref(false)
const dialog = ref(false)
const editing = ref(false)
const saving = ref(false)
const form = ref({})
let listRequest = 0

const availableUsers = computed(() => {
  const assignedIds = new Set(list.value.map((member) => member.userId))
  return users.value.filter((user) => !assignedIds.has(user.id))
})

async function loadUsers() {
  usersLoading.value = true
  usersError.value = false
  try {
    const response = await api.get('/users')
    if (!Array.isArray(response.data)) throw new Error('Invalid users response')
    users.value = response.data
  } catch {
    users.value = []
    usersError.value = true
  } finally {
    usersLoading.value = false
  }
}

async function load() {
  const request = ++listRequest
  const projectId = props.projectId
  loading.value = true
  listError.value = false
  try {
    const response = await api.get(`/projects/${projectId}/members`)
    if (!Array.isArray(response.data)) throw new Error('Invalid members response')
    if (request === listRequest) list.value = response.data
  } catch {
    if (request === listRequest) {
      list.value = []
      listError.value = true
    }
  } finally {
    if (request === listRequest) loading.value = false
  }
}

function openAdd() {
  editing.value = false
  form.value = { userId: null, role: 'developer' }
  dialog.value = true
}

function openEdit(row) {
  editing.value = true
  form.value = { ...row }
  dialog.value = true
}

async function save() {
  if (saving.value) return
  if (!editing.value && (loading.value || listError.value || usersLoading.value || usersError.value)) return ElMessage.warning('请先加载项目成员和可选用户')
  if (!editing.value && !form.value.userId) return ElMessage.warning('请选择用户')
  saving.value = true
  try {
    if (editing.value) await api.put(`/projects/${props.projectId}/members`, { id: form.value.id, role: form.value.role })
    else await api.post(`/projects/${props.projectId}/members`, { userId: form.value.userId, role: form.value.role })
    ElMessage.success('保存成功')
    dialog.value = false
    await load()
  } catch (e) { ElMessage.error(e.message) }
  finally { saving.value = false }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定将 ${row.nickname || row.username} 移出项目？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await api.delete(`/projects/${props.projectId}/members/${row.id}`)
    ElMessage.success('成员已移除')
    await load()
  } catch (e) {
    ElMessage.error(e.message || '移除失败')
  }
}

onMounted(() => { load(); loadUsers() })
watch(() => props.projectId, () => { load(); loadUsers() })
</script>

<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" :disabled="loading || listError || saving" @click="openAdd">+ 添加成员</el-button>
    </div>
    <div v-if="listError" class="member-load-error" role="alert">
      <span>项目成员暂时无法读取</span>
      <el-button link :loading="loading" @click="load">重新加载</el-button>
    </div>
    <div v-else class="table-frame">
      <el-table :data="list" v-loading="loading" stripe empty-text="暂无成员，可点击上方添加">
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="nickname" label="昵称" width="140" />
      <el-table-column label="项目角色" width="120">
        <template #default="{ row }">
          <span class="badge" :class="badgeClass('member', row.role)">{{ memberRole[row.role] }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="joinedAt" label="加入时间" width="180">
        <template #default="{ row }">{{ (row.joinedAt || '').replace('T', ' ').slice(0, 16) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">改角色</el-button>
          <el-button size="small" type="danger" @click="remove(row)">移出</el-button>
        </template>
      </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="editing ? '修改成员角色' : '添加成员'" width="420px" destroy-on-close :close-on-click-modal="!saving" :close-on-press-escape="!saving" :show-close="!saving">
      <el-form label-width="80px" size="default" :disabled="saving">
        <el-form-item v-if="!editing" label="用户">
          <div v-if="usersError" class="user-load-error" role="alert">
            <span>用户列表暂时无法读取</span>
            <el-button link size="small" :loading="usersLoading" @click="loadUsers">重试</el-button>
          </div>
          <div v-else-if="!usersLoading && availableUsers.length === 0" class="user-select-empty" role="status">
            {{ users.length ? '所有用户已加入此项目' : '当前没有可添加的用户' }}
          </div>
          <el-select v-else v-model="form.userId" filterable style="width: 100%" placeholder="选择用户" :loading="usersLoading" :disabled="usersLoading">
            <el-option v-for="u in availableUsers" :key="u.id" :label="u.nickname || u.username" :value="u.id" />
            <template #empty><span class="user-select-empty">没有匹配的用户</span></template>
          </el-select>
        </el-form-item>
        <el-form-item v-else label="用户">{{ form.nickname || form.username }}</el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width: 100%">
            <el-option v-for="(l, k) in memberRole" :key="k" :label="l" :value="k" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="saving" @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" :disabled="!editing && (loading || listError || usersLoading || usersError || availableUsers.length === 0)" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 14px; padding-bottom: 10px; border-bottom: 1px solid var(--border); }
.table-frame { margin-bottom: 14px; }
.member-load-error { display: flex; min-height: 110px; align-items: center; justify-content: space-between; gap: 12px; padding: 20px; border: 1px solid var(--border); border-radius: var(--radius-lg); color: var(--muted); }
.user-load-error { display: flex; min-height: 32px; align-items: center; justify-content: space-between; gap: 10px; color: var(--muted); font-size: 12px; }
.user-select-empty { display: block; padding: 10px; color: var(--muted); font-size: 12px; text-align: center; }
@media (max-width: 720px) {
  .member-load-error { align-items: flex-start; flex-direction: column; }
  .table-frame { overflow-x: auto; }
  .table-frame :deep(.el-table) { min-width: 800px !important; }
}
</style>
