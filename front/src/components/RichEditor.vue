<script setup>
import { ref, watch, onMounted } from 'vue'

const model = defineModel({ type: String, default: '' })
const editor = ref(null)
const blockTag = ref('p')

function exec(cmd, val = null) {
  editor.value?.focus()
  document.execCommand(cmd, false, val)
  if (editor.value) model.value = editor.value.innerHTML
}

function execBlock(tag) {
  editor.value?.focus()
  document.execCommand('formatBlock', false, tag)
  if (editor.value) model.value = editor.value.innerHTML
}

function addLink() {
  const url = prompt('请输入链接地址', 'https://')
  if (url) exec('createLink', url)
}

function clearFormat() {
  editor.value?.focus()
  document.execCommand('removeFormat')
  if (editor.value) model.value = editor.value.innerHTML
}

function onInput(e) {
  model.value = e.target.innerHTML
}

onMounted(() => {
  if (editor.value && model.value) editor.value.innerHTML = model.value
})

watch(model, (v) => {
  if (editor.value && editor.value.innerHTML !== v) editor.value.innerHTML = v || ''
})
</script>

<template>
  <div class="rich-editor">
    <div class="toolbar">
      <el-select v-model="blockTag" size="small" class="block-select" @change="execBlock($event)">
        <el-option value="p" label="正文" />
        <el-option value="h2" label="标题 2" />
        <el-option value="h3" label="标题 3" />
        <el-option value="h4" label="标题 4" />
        <el-option value="blockquote" label="引用" />
        <el-option value="pre" label="代码块" />
      </el-select>
      <button type="button" class="tool" title="加粗" @click="exec('bold')"><b>B</b></button>
      <button type="button" class="tool" title="斜体" @click="exec('italic')"><i>I</i></button>
      <button type="button" class="tool" title="下划线" @click="exec('underline')"><u>U</u></button>
      <button type="button" class="tool" title="删除线" @click="exec('strikeThrough')"><s>S</s></button>
      <button type="button" class="tool" title="无序列表" @click="exec('insertUnorderedList')">• 列表</button>
      <button type="button" class="tool" title="有序列表" @click="exec('insertOrderedList')">1. 列表</button>
      <button type="button" class="tool" title="左对齐" @click="exec('justifyLeft')">左</button>
      <button type="button" class="tool" title="居中" @click="exec('justifyCenter')">中</button>
      <button type="button" class="tool" title="链接" @click="addLink">🔗</button>
      <button type="button" class="tool" title="清除格式" @click="clearFormat">⌫</button>
    </div>
    <div
      ref="editor"
      class="body"
      contenteditable
      @input="onInput"
      data-placeholder="在此输入内容，支持富文本格式"
    />
  </div>
</template>

<style scoped>
.rich-editor {
  border: 1px solid var(--border);
  border-radius: var(--radius);
  overflow: hidden;
  background: rgba(23, 27, 25, .86);
  box-shadow: var(--shadow-sm);
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 8px 10px;
  border-bottom: 1px solid var(--border);
  background: rgba(255, 255, 255, .035);
  align-items: center;
}
.block-select { width: 110px; margin-right: 4px; }
.tool {
  padding: 5px 10px;
  font-size: 14px;
  color: var(--muted);
  background: rgba(255, 255, 255, .04);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  cursor: pointer;
  transition: all 0.15s var(--ease);
}
.tool:hover { border-color: var(--accent); color: var(--accent); }
.body {
  min-height: 160px;
  max-height: 420px;
  overflow-y: auto;
  padding: 14px 16px;
  font-size: 16px;
  line-height: 1.7;
  color: var(--text);
  outline: none;
}
.body:empty::before {
  content: attr(data-placeholder);
  color: var(--muted);
}
.body :deep(h2) { font-size: 22px; margin: 12px 0 8px; font-weight: 700; }
.body :deep(h3) { font-size: 19px; margin: 10px 0 6px; font-weight: 700; }
.body :deep(h4) { font-size: 17px; margin: 8px 0 4px; font-weight: 600; }
.body :deep(ul), .body :deep(ol) { padding-left: 22px; margin: 6px 0; }
.body :deep(blockquote) {
  border-left: 3px solid var(--accent);
  padding: 4px 12px;
  margin: 8px 0;
  color: var(--muted);
  background: rgba(203, 210, 118, .06);
}
.body :deep(pre) {
  background: rgba(255, 255, 255, .045);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 10px 12px;
  font-family: 'Consolas', monospace;
  font-size: 14px;
  overflow-x: auto;
}
.body :deep(a) { color: var(--cyan); }
.body :deep(img) { max-width: 100%; border-radius: 8px; }
</style>
