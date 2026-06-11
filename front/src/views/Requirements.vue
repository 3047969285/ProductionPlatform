<template>
  <PageShell tag="REQUIREMENTS" title="产品需求">
    <template #action>
      <el-button type="primary" @click="openForm()">+ 新建需求</el-button>
    </template>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="reqNo" label="需求编号" width="140" />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="productName" label="产品" width="120" />
      <el-table-column prop="orderNo" label="交付任务" width="130">
        <template #default="{ row }">{{ row.orderNo || '—' }}</template>
      </el-table-column>
      <el-table-column label="优先级" width="80">
        <template #default="{ row }">
          <span class="badge" :class="badgeClass('priority', row.priority)">{{ reqPriority[row.priority] }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <span class="badge" :class="badgeClass('req', row.status)">{{ reqStatus[row.status] }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="proposer" label="提出人" width="90" />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'draft'" size="small" type="primary" @click="setStatus(row.id, 'review')">提交评审</el-button>
          <el-button v-if="row.status === 'review'" size="small" type="primary" @click="setStatus(row.id, 'approved')">通过</el-button>
          <el-button v-if="row.status === 'review'" size="small" @click="setStatus(row.id, 'rejected')">拒绝</el-button>
          <el-button v-if="row.status === 'approved'" size="small" type="primary" @click="setStatus(row.id, 'developing')">开始开发</el-button>
          <el-button v-if="row.status === 'developing'" size="small" type="primary" @click="setStatus(row.id, 'done')">已实现</el-button>
          <el-button size="small" @click="openForm(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="form.id ? '编辑需求' : '新建需求'" v-model="visible" width="520px" @closed="resetForm">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="需求标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="详细说明、验收标准等" />
        </el-form-item>
        <el-form-item label="关联产品" required>
          <el-select v-model="form.productId" placeholder="选择产品" style="width:100%">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="交付任务">
          <el-select v-model="form.orderId" placeholder="可选，关联迭代" clearable style="width:100%">
            <el-option v-for="o in orders" :key="o.id" :label="`${o.orderNo} · ${o.productName}`" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority" style="width:100%">
            <el-option v-for="(label, val) in reqPriority" :key="val" :label="label" :value="val" />
          </el-select>
        </el-form-item>
        <el-form-item label="提出人">
          <el-input v-model="form.proposer" />
        </el-form-item>
        <el-form-item v-if="form.id" label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option v-for="(label, val) in reqStatus" :key="val" :label="label" :value="val" />
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
import { getUser } from '../auth'
import { reqStatus, reqPriority, badgeClass } from '../constants'

const loading = ref(false)
const list = ref([])
const products = ref([])
const orders = ref([])
const visible = ref(false)
const form = ref(emptyForm())

function emptyForm() {
  return { id: null, title: '', description: '', productId: null, orderId: null, priority: 'medium', status: 'draft', proposer: '' }
}

async function load() {
  loading.value = true
  try {
    const [r, p, o] = await Promise.all([
      api.get('/requirement/list'),
      api.get('/product/list'),
      api.get('/production/list'),
    ])
    list.value = r.data
    products.value = p.data
    orders.value = o.data
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

function openForm(row) {
  form.value = row ? { ...row } : { ...emptyForm(), proposer: getUser()?.nickname || '' }
  visible.value = true
}

function resetForm() { form.value = emptyForm() }

async function save() {
  if (!form.value.title?.trim()) return ElMessage.warning('请填写标题')
  if (!form.value.productId) return ElMessage.warning('请选择关联产品')
  try {
    if (form.value.id) await api.put('/requirement/update', form.value)
    else await api.post('/requirement/add', form.value)
    ElMessage.success('保存成功')
    visible.value = false
    await load()
  } catch (e) { ElMessage.error(e.message) }
}

async function setStatus(id, status) {
  try {
    await api.put(`/requirement/status/${id}`, null, { params: { status } })
    ElMessage.success('状态已更新')
    await load()
  } catch (e) { ElMessage.error(e.message) }
}

async function remove(id) {
  try {
    await ElMessageBox.confirm('确定删除该需求？', '提示', { type: 'warning' })
    await api.delete(`/requirement/delete/${id}`)
    ElMessage.success('已删除')
    await load()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.message) }
}

onMounted(load)
</script>
