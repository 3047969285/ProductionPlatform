<script setup>
import { onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { memberRole, badgeClass } from '../constants'

const props = defineProps({ projectId: { type: Number, required: true } })

const list = ref([])
const users = ref([])
const loading = ref(false)
const dialog = ref(false)
const editing = ref(false)
const saving = ref(false)
const form = ref({})

async function loadUsers() {
  try { users.value = (await api.get('/users')).data } catch { users.value = [] }
}

async function load() {
  loading.value = true
  try { list.value = (await api.get(`/projects/${props.projectId}/members`)).data }
  catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
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
  if (!form.value.userId) return ElMessage.warning('请选择用户')
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
    await api.delete(`/projects/${props.projectId}/members/${row.id}`)
    load()
  } catch { /* cancel */ }
}

onMounted(() => { load(); loadUsers() })
watch(() => props.projectId, () => { load(); loadUsers() })
</script>

<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">+ 添加成员</el-button>
    </div>
    <div class="table-frame">
      <el-table :data="list" v-loading="loading" stripe>
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
          <el-select v-model="form.userId" filterable style="width: 100%" placeholder="选择用户">
            <el-option v-for="u in users" :key="u.id" :label="u.nickname || u.username" :value="u.id" />
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
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 14px; padding-bottom: 10px; border-bottom: 1px solid var(--border); }
.table-frame { margin-bottom: 14px; }
</style>
