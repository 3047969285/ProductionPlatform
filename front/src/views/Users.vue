<template>
  <PageShell tag="05 / USERS" title="用户">
    <template #action><el-button type="primary" :disabled="saving" @click="openForm()">+ 新增</el-button></template>
    <div v-if="loadError" v-reveal class="list-error" role="alert">
      <span>暂时无法读取用户列表</span>
      <el-button link @click="load">重新加载</el-button>
    </div>
    <div v-else v-loading="loading" v-reveal class="table-frame">
      <table class="users-table" aria-label="用户列表">
        <colgroup>
          <col class="username-column" />
          <col class="nickname-column" />
          <col class="role-column" />
          <col class="actions-column" />
        </colgroup>
        <thead>
          <tr><th scope="col">用户名</th><th scope="col">昵称</th><th scope="col">角色</th><th scope="col">操作</th></tr>
        </thead>
        <tbody v-if="list.length">
          <tr v-for="row in list" :key="row.id">
            <td>{{ row.username }}</td>
            <td>{{ row.nickname || '—' }}</td>
            <td>{{ roles[row.role] || row.role || '未知' }}</td>
            <td>
              <div class="user-actions">
                <el-button size="small" @click="openForm(row)">编辑</el-button>
                <el-button size="small" @click="resetPwd(row)">重置密码</el-button>
                <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
              </div>
            </td>
          </tr>
        </tbody>
        <tbody v-else>
          <tr><td class="empty-row" colspan="4">还没有用户，点击右上角「新增」创建</td></tr>
        </tbody>
      </table>
    </div>
    <el-dialog
      :title="form.id ? '编辑用户' : '新增用户'"
      v-model="visible"
      width="420px"
      :close-on-click-modal="!saving"
      :close-on-press-escape="!saving"
      :show-close="!saving"
    >
      <el-form :model="form" label-width="70px" :disabled="saving">
        <el-form-item label="用户名" required><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item v-if="!form.id" label="密码" required><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width:100%">
            <el-option v-for="(l, v) in roles" :key="v" :label="l" :value="v" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="saving" @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </PageShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '../components/PageShell.vue'
import { useCrud } from '../composables/useCrud'
import { roles } from '../constants'
import api from '../api'

const { list, loading, load, remove, loadError } = useCrud('/users')
const visible = ref(false)
const saving = ref(false)
const form = ref({})

function openForm(row) { form.value = row ? { ...row } : { role: 'developer', password: '' }; visible.value = true }

async function save() {
  if (saving.value) return
  if (!form.value.username) return ElMessage.warning('用户名必填')
  if (!form.value.id && !form.value.password) return ElMessage.warning('密码必填')
  saving.value = true
  try {
    if (form.value.id) await api.put('/users', form.value)
    else await api.post('/users', form.value)
    ElMessage.success('保存成功'); visible.value = false; await load()
  } catch (e) { ElMessage.error(e.message) }
  finally { saving.value = false }
}

async function resetPwd(row) {
  try {
    const { value } = await ElMessageBox.prompt('输入新密码', '重置密码', { inputType: 'password' })
    if (!value) return
    await api.put('/users/password', { id: row.id, password: value })
    ElMessage.success('密码已重置')
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error(e?.message || '密码重置失败')
  }
}

onMounted(load)
</script>

<style scoped>
.table-frame { overflow: hidden; border: 1px solid var(--border); border-radius: var(--radius-lg); background: rgba(12, 12, 12, .78); }
.users-table { width: 100%; border-collapse: collapse; table-layout: fixed; text-align: left; }
.username-column { width: 24%; }
.nickname-column { width: 24%; }
.role-column { width: 20%; }
.actions-column { width: 32%; }
.users-table th, .users-table td { padding: 15px 18px; border-bottom: 1px solid var(--border); }
.users-table th { color: var(--muted); background: rgba(255, 255, 255, .025); font-size: 12px; font-weight: 500; }
.users-table td { color: var(--text); font-size: 12px; }
.users-table tbody tr { transition: background .25s var(--ease); }
.users-table tbody tr:hover { background: rgba(203, 210, 118, .045); }
.users-table tbody tr:last-child td { border-bottom: 0; }
.users-table .empty-row { height: 112px; color: var(--muted); text-align: center; }
.list-error { display: flex; min-height: 110px; align-items: center; justify-content: space-between; gap: 12px; padding: 20px; border: 1px solid var(--border); border-radius: var(--radius-lg); color: var(--muted); }
.user-actions { display: flex; align-items: center; gap: 8px; white-space: nowrap; }
.user-actions :deep(.el-button + .el-button) { margin-left: 0; }
@media (max-width: 980px) {
  .table-frame { overflow-x: auto; }
  .users-table { min-width: 810px; }
}
@media (max-width: 720px) {
  .list-error { align-items: flex-start; flex-direction: column; }
}
</style>
