<template>
  <PageShell tag="03 / TEAMS" title="团队">
    <template #action><el-button type="primary" :disabled="saving" @click="openForm()">+ 新增</el-button></template>
    <div v-if="loadError" v-reveal class="list-error" role="alert">
      <span>暂时无法读取团队列表</span>
      <el-button link @click="load">重新加载</el-button>
    </div>
    <div v-else v-loading="loading" v-reveal class="table-frame">
      <table class="teams-table" aria-label="团队列表">
        <colgroup>
          <col class="code-column" />
          <col class="name-column" />
          <col class="capacity-column" />
          <col class="status-column" />
          <col class="actions-column" />
        </colgroup>
        <thead>
          <tr><th scope="col">编码</th><th scope="col">名称</th><th scope="col">并发上限</th><th scope="col">状态</th><th scope="col">操作</th></tr>
        </thead>
        <tbody v-if="list.length">
          <tr v-for="row in list" :key="row.id">
            <td>{{ row.code }}</td>
            <td>{{ row.name }}</td>
            <td>{{ row.capacity ?? '—' }}</td>
            <td><span class="badge" :class="badgeClass('team', row.status)">{{ teamStatus[row.status] || row.status || '未知' }}</span></td>
            <td>
              <div class="team-actions">
                <el-button size="small" @click="openForm(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
              </div>
            </td>
          </tr>
        </tbody>
        <tbody v-else>
          <tr><td class="empty-row" colspan="5">还没有团队，点击右上角「新增」创建</td></tr>
        </tbody>
      </table>
    </div>
    <el-dialog
      :title="form.id ? '编辑团队' : '新增团队'"
      v-model="visible"
      width="420px"
      :close-on-click-modal="!saving"
      :close-on-press-escape="!saving"
      :show-close="!saving"
    >
      <el-form :model="form" label-width="90px" :disabled="saving">
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
        <el-button :disabled="saving" @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
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

const { list, loading, load, remove, loadError } = useCrud('/teams')
const visible = ref(false)
const saving = ref(false)
const form = ref({})

function openForm(row) { form.value = row ? { ...row } : { status: 'active', capacity: 5 }; visible.value = true }

async function save() {
  if (saving.value) return
  if (!form.value.code || !form.value.name) return ElMessage.warning('编码和名称必填')
  saving.value = true
  try {
    if (form.value.id) await api.put('/teams', form.value)
    else await api.post('/teams', form.value)
    ElMessage.success('保存成功'); visible.value = false; await load()
  } catch (e) { ElMessage.error(e.message) }
  finally { saving.value = false }
}

onMounted(load)
</script>

<style scoped>
.table-frame { overflow: hidden; border: 1px solid var(--border); border-radius: var(--radius-lg); background: rgba(12, 12, 12, .78); }
.teams-table { width: 100%; border-collapse: collapse; table-layout: fixed; text-align: left; }
.code-column { width: 18%; }
.name-column { width: 26%; }
.capacity-column { width: 17%; }
.status-column { width: 18%; }
.actions-column { width: 21%; }
.teams-table th, .teams-table td { padding: 15px 18px; border-bottom: 1px solid var(--border); }
.teams-table th { color: var(--muted); background: rgba(255, 255, 255, .025); font-size: 12px; font-weight: 500; }
.teams-table td { color: var(--text); font-size: 12px; }
.teams-table tbody tr { transition: background .25s var(--ease); }
.teams-table tbody tr:hover { background: rgba(203, 210, 118, .045); }
.teams-table tbody tr:last-child td { border-bottom: 0; }
.teams-table .empty-row { height: 112px; color: var(--muted); text-align: center; }
.list-error { display: flex; min-height: 110px; align-items: center; justify-content: space-between; gap: 12px; padding: 20px; border: 1px solid var(--border); border-radius: var(--radius-lg); color: var(--muted); }
.team-actions { display: flex; align-items: center; gap: 8px; white-space: nowrap; }
.team-actions :deep(.el-button + .el-button) { margin-left: 0; }
@media (max-width: 980px) {
  .table-frame { overflow-x: auto; }
  .teams-table { min-width: 880px; }
}
@media (max-width: 720px) {
  .list-error { align-items: flex-start; flex-direction: column; }
}
</style>
