<template>
  <PageShell tag="03 / TEAMS" title="团队">
    <template #action><el-button type="primary" @click="openForm()">+ 新增</el-button></template>
    <div class="table-frame">
      <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="code" label="编码" min-width="150" />
      <el-table-column prop="name" label="名称" min-width="240" />
      <el-table-column prop="capacity" label="并发上限" min-width="150" />
      <el-table-column label="状态" min-width="150">
        <template #default="{ row }">
          <span class="badge" :class="badgeClass('team', row.status)">{{ teamStatus[row.status] }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openForm(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
      </el-table>
    </div>
    <el-dialog :title="form.id ? '编辑团队' : '新增团队'" v-model="visible" width="420px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="编码" required><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="并发上限"><el-input v-model.number="form.capacity" type="number" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option v-for="(l, v) in teamStatus" :key="v" :label="l" :value="v" />
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
import { ElMessage } from 'element-plus'
import PageShell from '../components/PageShell.vue'
import { useCrud } from '../composables/useCrud'
import { teamStatus, badgeClass } from '../constants'
import api from '../api'

const { list, loading, load, remove } = useCrud('/teams')
const visible = ref(false)
const form = ref({})

function openForm(row) { form.value = row ? { ...row } : { status: 'active', capacity: 5 }; visible.value = true }

async function save() {
  if (!form.value.code || !form.value.name) return ElMessage.warning('编码和名称必填')
  try {
    if (form.value.id) await api.put('/teams', form.value)
    else await api.post('/teams', form.value)
    ElMessage.success('保存成功'); visible.value = false; await load()
  } catch (e) { ElMessage.error(e.message) }
}

onMounted(load)
</script>

<style scoped>
.table-frame :deep(.el-table__cell) { padding: 14px 16px; }
.table-frame :deep(.el-table__row) { transition: background .25s var(--ease); }
@media (max-width: 720px) {
  .table-frame { overflow-x: auto; }
  .table-frame :deep(.el-table) { min-width: 760px !important; }
}
</style>
