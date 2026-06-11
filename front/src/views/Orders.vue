<template>
  <PageShell tag="DELIVERY" title="交付任务">
    <template #action>
      <el-button type="primary" @click="openForm()">+ 新建任务</el-button>
    </template>

    <el-table :data="orders" v-loading="loading" stripe>
      <el-table-column prop="orderNo" label="任务编号" width="140" />
      <el-table-column prop="productName" label="软件产品" min-width="120" />
      <el-table-column prop="lineName" label="研发团队" width="100" />
      <el-table-column label="Story 进度" width="120">
        <template #default="{ row }">{{ row.completedQty }} / {{ row.quantity }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <span class="badge" :class="badgeClass('order', row.status)">{{ orderStatus[row.status] }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'pending'" size="small" type="primary" @click="setStatus(row.id, 'running')">启动开发</el-button>
          <el-button v-if="row.status === 'running'" size="small" type="primary" @click="setStatus(row.id, 'done')">确认交付</el-button>
          <el-button size="small" @click="openForm(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="form.id ? '编辑任务' : '新建交付任务'" v-model="visible" width="440px" @closed="resetForm">
      <el-form :model="form" label-width="90px">
        <el-form-item label="软件产品" required>
          <el-select v-model="form.productId" placeholder="选择产品" style="width:100%">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="研发团队">
          <el-select v-model="form.lineId" placeholder="选择团队" clearable style="width:100%">
            <el-option v-for="l in lines" :key="l.id" :label="l.name" :value="l.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划 Story" required>
          <el-input v-model.number="form.quantity" type="number" placeholder="Story 点数" />
        </el-form-item>
        <el-form-item label="完成 Story">
          <el-input v-model.number="form.completedQty" type="number" />
        </el-form-item>
        <el-form-item v-if="form.id" label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option v-for="(label, val) in orderStatus" :key="val" :label="label" :value="val" />
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
import api from '../api'
import { orderStatus, badgeClass } from '../constants'

const loading = ref(false)
const orders = ref([])
const products = ref([])
const lines = ref([])
const visible = ref(false)
const form = ref(emptyForm())

function emptyForm() {
  return { id: null, productId: null, lineId: null, quantity: null, completedQty: 0, status: 'pending' }
}

async function load() {
  loading.value = true
  try {
    const [o, p, l] = await Promise.all([
      api.get('/production/list'),
      api.get('/product/list'),
      api.get('/line/list'),
    ])
    orders.value = o.data
    products.value = p.data
    lines.value = l.data
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

function openForm(row) {
  form.value = row ? { ...row } : emptyForm()
  visible.value = true
}
function resetForm() { form.value = emptyForm() }

async function save() {
  if (!form.value.productId) return ElMessage.warning('请选择软件产品')
  if (!form.value.quantity || form.value.quantity <= 0) return ElMessage.warning('计划 Story 点须大于 0')
  try {
    if (form.value.id) await api.put('/production/update', form.value)
    else await api.post('/production/add', form.value)
    ElMessage.success('保存成功')
    visible.value = false
    await load()
  } catch (e) { ElMessage.error(e.message) }
}

async function setStatus(id, status) {
  try {
    await api.put(`/production/status/${id}`, null, { params: { status } })
    ElMessage.success('状态已更新')
    await load()
  } catch (e) { ElMessage.error(e.message) }
}

async function remove(id) {
  try {
    await ElMessageBox.confirm('确定删除该交付任务？', '提示', { type: 'warning' })
    await api.delete(`/production/delete/${id}`)
    ElMessage.success('已删除')
    await load()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.message) }
}

onMounted(load)
</script>
