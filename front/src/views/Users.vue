<template>
  <PageShell tag="05 / USERS" title="用户">
    <template #action><el-button type="primary" :disabled="saving" @click="openForm()">+ 新增</el-button></template>
    <div v-if="loadError" class="list-error" role="alert">
      <span>暂时无法读取用户列表</span>
      <el-button link @click="load">重新加载</el-button>
    </div>
    <div v-else class="table-frame">
      <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="username" label="用户名" min-width="180" />
      <el-table-column prop="nickname" label="昵称" min-width="180" />
      <el-table-column label="角色" min-width="160">
        <template #default="{ row }">{{ roles[row.role] }}</template>
      </el-table-column>
      <el-table-column label="操作" width="290" fixed="right">
        <template #default="{ row }">
          <div class="user-actions">
            <el-button size="small" @click="openForm(row)">编辑</el-button>
            <el-button size="small" @click="resetPwd(row)">重置密码</el-button>
            <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
          </div>
        </template>
      </el-table-column>
      </el-table>
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
    const { value } = await ElMessageBox.prompt('输入新密码', '重置密码')
    if (!value) return
    await api.put('/users/password', { id: row.id, password: value })
    ElMessage.success('密码已重置')
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.message) }
}

onMounted(load)
</script>

<style scoped>
.table-frame :deep(.el-table__cell) { padding: 14px 16px; }
.table-frame :deep(.el-table__row) { transition: background .25s var(--ease); }
.list-error { display: flex; min-height: 110px; align-items: center; justify-content: space-between; gap: 12px; padding: 20px; border: 1px solid var(--border); border-radius: var(--radius-lg); color: var(--muted); }
.user-actions { display: flex; align-items: center; gap: 6px; white-space: nowrap; }
.user-actions :deep(.el-button + .el-button) { margin-left: 0; }
@media (max-width: 720px) {
  .list-error { align-items: flex-start; flex-direction: column; }
  .table-frame { overflow-x: auto; }
  .table-frame :deep(.el-table) { min-width: 680px !important; }
}
</style>
