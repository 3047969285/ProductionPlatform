<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { RouterView } from 'vue-router'
import { createInteractions, createScrollProgress, createSpotlight } from './interactions'

// 全局交互动效目标。挂到 body 上，这样登录页、弹窗、命令面板等不在 AppLayout 里的内容也能覆盖到。
// 注意：.nav-item 自带 ::after 下划线，不参与高光，只做按压/波纹。
const GLOW_TARGETS = [
  '.card', '.stat-card', '.metric-card', '.action-card', '.next-item',
  '.project-card', '.work-card', '.home-link-card', '.preview-card', '.modal-card',
  '.login-card', '.palette-item', '.panel', '.doc-panel', '.filter-row',
  '.bar-row', '.pulse-row', '.timeline-item', '.delivery-row', '.member-row', '.sprint-load-row',
].join(',')

const PRESS_TARGETS = 'button, .nav-item, .card, .home-link-card, .work-card, .preview-card, .recent-head a'
const RIPPLE_TARGETS = 'button, .nav-item, .home-link-card, .work-card, .preview-card, .el-table__row'
// 只扫按钮 / 链接里的字形元素，避免全站 span 都被遍历一遍。
const ARROW_TARGETS = 'button span, a span, button i, a i, button b, a b'

const scrollBar = ref(null)
let interactions
let scrollProgress
let spotlight

onMounted(() => {
  scrollProgress = createScrollProgress(scrollBar.value)
  spotlight = createSpotlight()
  interactions = createInteractions(document.body, {
    glow: GLOW_TARGETS,
    ripple: RIPPLE_TARGETS,
    press: PRESS_TARGETS,
    arrow: ARROW_TARGETS,
  })
})

onUnmounted(() => {
  scrollProgress?.destroy()
  spotlight?.destroy()
  interactions?.destroy()
})
</script>

<template>
  <div ref="scrollBar" class="scroll-progress" aria-hidden="true" />
  <RouterView />
</template>
