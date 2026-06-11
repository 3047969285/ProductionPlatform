<template>
  <PageShell tag="QA" title="测试验收">
    <template #action>
      <el-button type="primary" @click="openForm()">+ 新增验收</el-button>
    </template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="orderNo" label="任务编号" width="140" />
      <el-table-column prop="productName" label="软件产品" min-width="120" />
      <el-table-column label="结果" width="100">
        <template #default="{ row }">
          <span class="badge" :class="badgeClass('quality', row.result)">{{ qualityResult[row.result] }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="defectCount" label="Bug 数" width="80" />
      <el-table-column prop="inspector" label="测试负责人" width="110" />
      <el-table-column prop="remark" label="备注" min-width="140" />
      <el-table-column label="时间" width="160">
        <template #default="{ row }">{{ fmt(row.inspectTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog title="新增测试验收" v-model="visible" width="440px" @closed="resetForm">
      <el-form :model="form" label-width="90px">
        <el-form-item label="交付任务" required>
          <el-select v-model="form.orderId" placeholder="选择任务" style="width:100%">
            <el-option v-for="o in orders" :key="o.id" :label="`${o.orderNo} · ${o.productName}`" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="验收结果" required>
          <el-select v-model="form.result" style="width:100%">
            <el-option v-for="(label, val) in qualityResult" :key="val" :label="label" :value="val" />
          </el-select>
        </el-form-item>
        <el-form-item label="Bug 数"><el-input v-model.number="form.defectCount" type="number" /></el-form-item>
        <el-form-item label="测试负责人" required><el-input v-model="form.inspector" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" placeholder="回归范围、发布建议等" /></el-form-item>
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
import api from '../api'
import { getUser } from '../auth'
import { qualityResult, badgeClass } from '../constants'

const loading = ref(false)
const list = ref([])
const orders = ref([])
const visible = ref(false)
const form = ref({})

function fmt(t) { return t ? String(t).replace('T', ' ').slice(0, 19) : '-' }
function resetForm() {
  form.value = { result: 'pass', defectCount: 0, inspector: getUser()?.nickname || '' }
}

async function load() {
  loading.value = true
  try {
    const [q, o] = await Promise.all([api.get('/quality/list'), api.get('/production/list')])
    list.value = q.data
    orders.value = o.data
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

function openForm() { resetForm(); visible.value = true }

async function save() {
  if (!form.value.orderId) return ElMessage.warning('请选择交付任务')
  if (!form.value.inspector) return ElMessage.warning('请填写测试负责人')
  try {
    await api.post('/quality/add', form.value)
    ElMessage.success('保存成功'); visible.value = false; await load()
  } catch (e) { ElMessage.error(e.message) }
}

async function remove(id) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await api.delete(`/quality/delete/${id}`)
    ElMessage.success('已删除'); await load()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.message) }
}

onMounted(load)
</script>
