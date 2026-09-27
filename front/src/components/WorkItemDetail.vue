<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'
import {
  reqStatus, reqPriority, apiStatus, httpMethods,
  taskStatus, bugStatus, bugSeverity,
  testStatus, opsStatus, opsSeverity,
  badgeClass,
} from '../constants'

const props = defineProps({
  visible: { type: Boolean, default: false },
  workType: { type: String, default: 'requirement' }, // requirement|api|task|bug|test|ops
  workId: { type: Number, default: null },
})

const emit = defineEmits(['update:visible', 'changed'])

const item = ref(null)
const loading = ref(false)
const comments = ref([])
const activities = ref([])
const newComment = ref('')
const commentLoading = ref(false)
const saving = ref(false)

// 每种工作项的 API 路径和编号字段
const typeMeta = {
  requirement: { path: '/requirements', noField: 'reqNo', label: '需求', statusMap: reqStatus, title: '标题', content: 'content' },
  api: { path: '/apis', noField: 'apiNo', label: '接口', statusMap: apiStatus, title: '标题', content: 'content' },
  task: { path: '/tasks', noField: 'id', label: '任务', statusMap: taskStatus, title: '标题', content: 'content' },
  bug: { path: '/bugs', noField: 'id', label: '缺陷', statusMap: bugStatus, title: '标题', content: 'content' },
  test: { path: '/tests', noField: 'id', label: '测试', statusMap: testStatus, title: '标题', content: 'description' },
  ops: { path: '/ops', noField: 'id', label: '运维问题', statusMap: opsStatus, title: '标题', content: 'content' },
}

const meta = computed(() => typeMeta[props.workType] || typeMeta.requirement)
// badgeClass 需要的 key（requirement->req）
const badgeKey = computed(() => (props.workType === 'requirement' ? 'req' : props.workType))

async function loadDetail() {
  if (!props.workId) return
  loading.value = true
  try {
    // 详情：列表接口筛单个（不带多余查询参数）
    const res = await api.get(meta.value.path)
    const arr = res.data || []
    item.value = arr.find((x) => x.id === props.workId) || null
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  try {
    const res = await api.get('/logs/comments', { params: { workType: props.workType, workId: props.workId } })
    comments.value = res.data || []
  } catch { comments.value = [] }
}

async function loadActivities() {
  try {
    const res = await api.get('/logs/activities', { params: { workType: props.workType, workId: props.workId } })
    activities.value = res.data || []
  } catch { activities.value = [] }
}

async function submitComment() {
  const text = newComment.value.trim()
  if (!text) return
  commentLoading.value = true
  try {
    await api.post('/logs/comments', { workType: props.workType, workId: props.workId, content: text })
    newComment.value = ''
    ElMessage.success('评论成功')
    await loadComments()
    emit('changed')
  } catch (e) { ElMessage.error(e.message) }
  finally { commentLoading.value = false }
}

function onClosed() {
  item.value = null
  comments.value = []
  activities.value = []
  newComment.value = ''
}

function fmtTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 19) : ''
}

function actionLabel(a) {
  const map = { create: '创建', update: '更新', status_change: '状态变更', comment: '评论', assign: '指派' }
  return map[a] || a
}

watch(() => props.visible, (v) => {
  if (v && props.workId) {
    loadDetail()
    loadComments()
    loadActivities()
  } else if (!v) {
    onClosed()
  }
})
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="meta.label + '详情'"
    width="760px"
    destroy-on-close
    @update:model-value="(v) => emit('update:visible', v)"
    @closed="onClosed"
  >
    <div v-loading="loading" class="detail">
      <template v-if="item">
        <!-- 头部 -->
        <div class="head">
          <div class="no">{{ item[meta.noField] }}</div>
          <h2>{{ item.title }}</h2>
          <div class="tags">
            <span v-if="item.priority" class="badge" :class="badgeClass('priority', item.priority)">
              {{ reqPriority[item.priority] || item.priority }}
            </span>
            <span v-if="item.severity" class="badge" :class="badgeClass('severity', item.severity)">
              {{ bugSeverity[item.severity] || item.severity }}
            </span>
            <span v-if="item.status" class="badge" :class="badgeClass(badgeKey, item.status)">
              {{ meta.statusMap[item.status] || item.status }}
            </span>
          </div>
        </div>

        <!-- 接口专用：方法+路径 -->
        <div v-if="workType === 'api'" class="api-line">
          <span class="method" :class="'m-' + (item.method || '').toLowerCase()">{{ item.method }}</span>
          <code>{{ item.path }}</code>
        </div>

        <!-- 字段表 -->
        <el-descriptions :column="2" border size="small" class="fields">
          <el-descriptions-item v-if="item.owner" label="负责人">{{ item.owner || '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="item.assignee" label="处理人">{{ item.assignee || '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="item.proposer || item.reporter" label="提出人">{{ item.proposer || item.reporter || '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="item.creator" label="创建人">{{ item.creator || '—' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ fmtTime(item.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ fmtTime(item.updatedAt) }}</el-descriptions-item>
          <el-descriptions-item v-if="item.fixVersion" label="解决版本">{{ item.fixVersion }}</el-descriptions-item>
          <el-descriptions-item v-if="item.estimateHours != null" label="预估工时">{{ item.estimateHours }}h</el-descriptions-item>
          <el-descriptions-item v-if="item.progress != null" label="进度">
            <el-progress :percentage="item.progress || 0" :stroke-width="8" style="width: 140px" />
          </el-descriptions-item>
          <el-descriptions-item v-if="workType === 'bug'" label="复现步骤">{{ item.steps || '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="workType === 'bug'" label="期望结果">{{ item.expectedResult || '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="workType === 'bug'" label="实际结果">{{ item.actualResult || '—' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 内容：富文本渲染 -->
        <div v-if="item[meta.content]" class="content">
          <h4>内容</h4>
          <div class="rich-body" v-html="item[meta.content]" />
        </div>
      </template>
      <p v-else-if="!loading" class="empty">未找到该工作项</p>
    </div>

    <!-- 评论 + 操作历史 -->
    <template v-if="item">
      <div class="section">
        <h4>评论（{{ comments.length }}）</h4>
        <div class="comment-list">
          <div v-for="c in comments" :key="c.id" class="comment">
            <div class="c-head"><b>{{ c.userName || '匿名' }}</b><span>{{ fmtTime(c.createdAt) }}</span></div>
            <div class="c-body">{{ c.content }}</div>
          </div>
          <p v-if="!comments.length" class="empty">暂无评论</p>
        </div>
        <div class="comment-input">
          <el-input v-model="newComment" type="textarea" :rows="2" placeholder="写下评论..." />
          <el-button type="primary" size="small" :loading="commentLoading" @click="submitComment">发送</el-button>
        </div>
      </div>

      <div class="section">
        <h4>操作历史（{{ activities.length }}）</h4>
        <el-timeline v-if="activities.length">
          <el-timeline-item v-for="a in activities" :key="a.id" :timestamp="fmtTime(a.createdAt)">
            <b>{{ a.operator || '系统' }}</b> {{ actionLabel(a.action) }}：{{ a.detail }}
          </el-timeline-item>
        </el-timeline>
        <p v-else class="empty">暂无操作记录</p>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.detail { min-height: 100px; }
.head { margin-bottom: 16px; }
.no { font-size: 13px; color: var(--cyan); letter-spacing: 0.05em; margin-bottom: 6px; }
.head h2 { font-size: 22px; font-weight: 700; margin-bottom: 10px; }
.tags { display: flex; gap: 8px; }
.api-line { margin-bottom: 14px; display: flex; align-items: center; gap: 10px; }
.method {
  display: inline-block; padding: 2px 10px; border-radius: 6px;
  font-size: 12px; font-weight: 700; color: #fff;
}
.m-get { background: #409eff; } .m-post { background: #67c23a; }
.m-put { background: #e6a23c; } .m-delete { background: #f56c6c; } .m-patch { background: #909399; }
.api-line code { color: var(--text); background: rgba(255,255,255,.045); padding: 3px 8px; border-radius: var(--radius); border: 1px solid var(--border); }
.fields { margin-bottom: 16px; }
.content { margin-bottom: 16px; }
.content h4, .section h4 { font-size: 15px; font-weight: 700; margin-bottom: 10px; color: var(--muted); }
.rich-body {
  border: 1px solid var(--border); border-radius: var(--radius);
  padding: 14px 16px; line-height: 1.7; font-size: 15px;
  background: rgba(255,255,255,.035);
}
.rich-body :deep(h2) { font-size: 21px; margin: 10px 0 6px; }
.rich-body :deep(h3) { font-size: 18px; margin: 8px 0 4px; }
.rich-body :deep(ul), .rich-body :deep(ol) { padding-left: 22px; }
.rich-body :deep(blockquote) { border-left: 3px solid var(--cyan); padding: 4px 12px; color: var(--muted); margin: 8px 0; }
.rich-body :deep(pre) { background: rgba(255,255,255,0.06); border-radius: 8px; padding: 10px; overflow-x: auto; font-size: 13px; }
.rich-body :deep(a) { color: var(--cyan); }
.rich-body :deep(img) { max-width: 100%; }
.section { margin-top: 20px; }
.comment-list { max-height: 220px; overflow-y: auto; margin-bottom: 10px; }
.comment {
  border: 1px solid var(--border); border-radius: var(--radius);
  padding: 8px 12px; margin-bottom: 8px; background: rgba(255,255,255,.035);
}
.c-head { display: flex; justify-content: space-between; font-size: 13px; color: var(--muted); margin-bottom: 4px; }
.c-body { font-size: 14px; line-height: 1.6; white-space: pre-wrap; }
.comment-input { display: flex; gap: 8px; align-items: flex-start; }
.comment-input .el-button { margin-top: 4px; }
.empty { color: var(--muted); text-align: center; padding: 12px; font-size: 13px; }
</style>
