<script setup>
import { ref, watch, onMounted } from 'vue'

const model = defineModel({ type: String, default: '' })
const editor = ref(null)

function exec(cmd, val = null) {
  document.execCommand(cmd, false, val)
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
      <button type="button" @click="exec('bold')"><b>B</b></button>
      <button type="button" @click="exec('italic')"><i>I</i></button>
      <button type="button" @click="exec('underline')"><u>U</u></button>
      <button type="button" @click="exec('insertUnorderedList')">列表</button>
      <button type="button" @click="exec('formatBlock', 'h3')">标题</button>
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
  border-radius: 12px;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.2);
}
.toolbar {
  display: flex;
  gap: 6px;
  padding: 8px 10px;
  border-bottom: 1px solid var(--border);
  background: rgba(255, 255, 255, 0.03);
}
.toolbar button {
  padding: 6px 12px;
  font-size: 14px;
  color: var(--text);
  background: transparent;
  border: 1px solid var(--border);
  border-radius: 8px;
  cursor: pointer;
}
.toolbar button:hover { border-color: var(--cyan); color: var(--cyan); }
.body {
  min-height: 160px;
  max-height: 320px;
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
.body :deep(h3) { font-size: 18px; margin: 8px 0; }
.body :deep(ul) { padding-left: 20px; }
</style>
