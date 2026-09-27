<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'
import { taskStatus, bugStatus, bugSeverity, reqPriority, badgeClass } from '../constants'

const props = defineProps({ projectId: { type: Number, required: true } })

const tasks = ref([])
const bugs = ref([])
const loading = ref(false)
const dragId = ref(null)

const taskColumns = computed(() => [
  { status: 'todo', label: taskStatus.todo, items: tasks.value.filter(t => t.status === 'todo') },
  { status: 'doing', label: taskStatus.doing, items: tasks.value.filter(t => t.status === 'doing') },
  { status: 'done', label: taskStatus.done, items: tasks.value.filter(t => t.status === 'done') },
])
const bugColumns = computed(() => [
  { status: 'open', label: bugStatus.open, items: bugs.value.filter(b => b.status === 'open' || b.status === 'reopened') },
  { status: 'fixing', label: bugStatus.fixing, items: bugs.value.filter(b => b.status === 'fixing') },
  { status: 'resolved', label: bugStatus.resolved, items: bugs.value.filter(b => b.status === 'resolved') },
  { status: 'closed', label: bugStatus.closed, items: bugs.value.filter(b => b.status === 'closed') },
])

async function load() {
  loading.value = true
  try {
    const [t, b] = await Promise.all([
      api.get('/tasks', { params: { projectId: props.projectId } }),
      api.get('/bugs', { params: { projectId: props.projectId } }),
    ])
    tasks.value = t.data
    bugs.value = b.data
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

function onDragStart(e, id) { dragId.value = id }
function onDrop(e, type, status) {
  const id = dragId.value
  dragId.value = null
  if (!id) return
  const path = type === 'task' ? '/tasks' : '/bugs'
  api.put(`${path}/${id}/status?status=${status}`).then(() => {
    ElMessage.success('状态已更新')
    load()
  }).catch((err) => ElMessage.error(err.message))
}

onMounted(load)
watch(() => props.projectId, load)
</script>

<template>
  <div v-loading="loading">
    <h3 class="lane-title">任务</h3>
    <div class="kanban">
      <div v-for="col in taskColumns" :key="'t' + col.status" class="col"
           @dragover.prevent @drop="onDrop($event, 'task', col.status)">
        <div class="col-head">{{ col.label }} <span class="cnt">{{ col.items.length }}</span></div>
        <div v-for="item in col.items" :key="item.id" class="card" draggable="true"
             @dragstart="onDragStart($event, item.id)">
          <div class="card-title">{{ item.title }}</div>
          <div class="card-meta">
            <span class="badge" :class="badgeClass('priority', item.priority)">{{ reqPriority[item.priority] }}</span>
            <span>{{ item.assignee || '未指派' }}</span>
          </div>
        </div>
        <div v-if="!col.items.length" class="empty">拖动卡片到这里</div>
      </div>
    </div>
    <h3 class="lane-title">缺陷</h3>
    <div class="kanban">
      <div v-for="col in bugColumns" :key="'b' + col.status" class="col"
           @dragover.prevent @drop="onDrop($event, 'bug', col.status)">
        <div class="col-head">{{ col.label }} <span class="cnt">{{ col.items.length }}</span></div>
        <div v-for="item in col.items" :key="item.id" class="card" draggable="true"
             @dragstart="onDragStart($event, item.id)">
          <div class="card-title">{{ item.title }}</div>
          <div class="card-meta">
            <span class="badge" :class="badgeClass('severity', item.severity)">{{ bugSeverity[item.severity] }}</span>
            <span>{{ item.assignee || '未指派' }}</span>
          </div>
        </div>
        <div v-if="!col.items.length" class="empty">拖动卡片到这里</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.lane-title { font-size: 16px; font-weight: 700; margin: 16px 0 10px; color: var(--text); }
.kanban { display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 12px; margin-bottom: 8px; }
.col {
  min-height: 120px; padding: 10px; border-radius: 12px;
  background: rgba(255, 255, 255, .025); border: 1px dashed var(--border-strong);
}
.col-head { font-size: 14px; font-weight: 600; margin-bottom: 8px; color: var(--muted); }
.cnt { margin-left: 6px; font-size: 12px; color: var(--cyan); }
.card {
  padding: 10px 12px; margin-bottom: 8px; cursor: grab;
  background: rgba(29, 34, 31, .94); border: 1px solid var(--border); border-radius: var(--radius); box-shadow: var(--shadow-sm);
}
.card:active { cursor: grabbing; }
.card-title { font-size: 14px; line-height: 1.5; margin-bottom: 6px; }
.card-meta { display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: var(--muted); }
.empty { text-align: center; color: var(--muted-light); font-size: 13px; padding: 20px 0; }
</style>
