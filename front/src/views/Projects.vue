<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageShell from '../components/PageShell.vue'
import api from '../api'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const dialog = ref(false)
const editing = ref(false)
const form = ref({ code: '', name: '', description: '', techStack: '', deliveryType: 'SaaS' })

async function load() {
  loading.value = true
  try { list.value = (await api.get('/projects')).data }
  catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

function openAdd() {
  editing.value = false
  form.value = { code: '', name: '', description: '', techStack: '', deliveryType: 'SaaS' }
  dialog.value = true
}

function openEdit(p) {
  editing.value = true
  form.value = { ...p }
  dialog.value = true
}

async function save() {
  if (!form.value.code || !form.value.name) return ElMessage.warning('请填写编码和名称')
  try {
    if (editing.value) await api.put('/projects', form.value)
    else await api.post('/projects', form.value)
    ElMessage.success('保存成功')
    dialog.value = false
    load()
  } catch (e) { ElMessage.error(e.message) }
}

async function remove(id) {
  try {
    await ElMessageBox.confirm('删除项目将清空关联数据，确定？', '提示', { type: 'warning' })
    await api.delete(`/projects/${id}`)
    ElMessage.success('已删除')
    load()
  } catch { /* cancel */ }
}

onMounted(load)
</script>

<template>
  <PageShell tag="PROJECTS" title="项目中心">
    <template #action>
      <el-button type="primary" @click="openAdd">+ 新建项目</el-button>
    </template>
    <div v-loading="loading" class="grid">
      <article v-for="p in list" :key="p.id" class="card" @click="router.push(`/projects/${p.id}`)">
        <div class="card-top">
          <span class="code">{{ p.code }}</span>
          <div class="actions" @click.stop>
            <el-button size="default" @click="openEdit(p)">编辑</el-button>
            <el-button size="default" type="danger" @click="remove(p.id)">删除</el-button>
          </div>
        </div>
        <h2>{{ p.name }}</h2>
        <p class="desc">{{ p.description || '暂无描述' }}</p>
        <p class="meta">{{ p.techStack }} · {{ p.deliveryType }}</p>
        <span class="enter">进入项目 →</span>
      </article>
      <p v-if="!loading && !list.length" class="empty">暂无项目，点击右上角新建</p>
    </div>

    <el-dialog v-model="dialog" :title="editing ? '编辑项目' : '新建项目'" width="520px">
      <el-form label-width="80px" size="default">
        <el-form-item label="编码"><el-input v-model="form.code" :disabled="editing" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="技术栈"><el-input v-model="form.techStack" /></el-form-item>
        <el-form-item label="形态"><el-input v-model="form.deliveryType" placeholder="SaaS / App / 定制" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </PageShell>
</template>

<style scoped>
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px; padding: 8px; }
.card {
  padding: 22px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  cursor: pointer;
  transition: border-color 0.2s, transform 0.2s;
}
.card:hover { border-color: rgba(0, 229, 255, 0.35); transform: translateY(-2px); }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.code { font-size: 14px; color: var(--cyan); font-weight: 600; letter-spacing: 0.05em; }
h2 { font-family: var(--font-display); font-size: 1.35rem; font-weight: 700; margin-bottom: 8px; }
.desc { font-size: 15px; color: var(--muted); line-height: 1.6; margin-bottom: 10px; min-height: 48px; }
.meta { font-size: 14px; color: rgba(255, 255, 255, 0.35); margin-bottom: 14px; }
.enter { font-size: 14px; color: var(--cyan); }
.empty { grid-column: 1 / -1; text-align: center; color: var(--muted); padding: 40px; font-size: 16px; }
</style>
