<template>
  <PageShell tag="05 / USERS" title="用户">
    <template #action><el-button type="primary" @click="openForm()">+ 新增</el-button></template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">{{ roles[row.role] }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openForm(row)">编辑</el-button>
          <el-button size="small" @click="resetPwd(row)">重置密码</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog :title="form.id ? '编辑用户' : '新增用户'" v-model="visible" width="420px">
      <el-form :model="form" label-width="70px">
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
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
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

const { list, loading, load, remove } = useCrud('/users')
const visible = ref(false)
const form = ref({})

function openForm(row) { form.value = row ? { ...row } : { role: 'developer', password: '123456' }; visible.value = true }

async function save() {
  if (!form.value.username) return ElMessage.warning('用户名必填')
  if (!form.value.id && !form.value.password) return ElMessage.warning('密码必填')
  try {
    if (form.value.id) await api.put('/users', form.value)
    else await api.post('/users', form.value)
    ElMessage.success('保存成功'); visible.value = false; await load()
  } catch (e) { ElMessage.error(e.message) }
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
