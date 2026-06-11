<template>
  <PageShell tag="PRODUCTS" title="软件产品">
    <template #action>
      <el-button type="primary" @click="openForm()">+ 新增</el-button>
    </template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="code" label="编码" width="120" />
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="spec" label="技术栈" min-width="160" />
      <el-table-column prop="unit" label="形态" width="80" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openForm(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog :title="form.id ? '编辑产品' : '新增产品'" v-model="visible" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="编码" required><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="技术栈"><el-input v-model="form.spec" /></el-form-item>
        <el-form-item label="形态"><el-input v-model="form.unit" placeholder="SaaS" /></el-form-item>
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
import { useList } from '../composables/useList'
import api from '../api'

const { list, loading, load, remove } = useList('/product')
const visible = ref(false)
const form = ref({})

function openForm(row) {
  form.value = row ? { ...row } : { unit: 'SaaS' }
  visible.value = true
}

async function save() {
  if (!form.value.code || !form.value.name) return ElMessage.warning('编码和名称必填')
  try {
    if (form.value.id) await api.put('/product/update', form.value)
    else await api.post('/product/add', form.value)
    ElMessage.success('保存成功')
    visible.value = false
    await load()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

onMounted(load)
</script>
