<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { gsap } from 'gsap'

defineProps({
  tag: String,
  title: String,
})

const root = ref(null)
let context

// 只负责页头入场；内容区的动效由各工作台自己用 v-reveal 声明，
// 避免外层容器与内层面板叠加出两次位移。
onMounted(() => {
  if (!root.value) return
  const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (reduced) return
  context = gsap.context(() => {
    const timeline = gsap.timeline({ defaults: { ease: 'power3.out' } })
    timeline
      .from(root.value.querySelectorAll('.tag'), { autoAlpha: 0, y: 10, duration: 0.4, clearProps: 'all' })
      .from(root.value.querySelectorAll('h1'), { autoAlpha: 0, y: 22, duration: 0.6, ease: 'power4.out', clearProps: 'all' }, '-=0.22')
      .from(root.value.querySelectorAll('.page-action'), { autoAlpha: 0, y: 10, duration: 0.45, clearProps: 'all' }, '-=0.32')
  }, root.value)
})

onUnmounted(() => context?.revert())
</script>

<template>
  <div ref="root" class="page">
    <header class="head">
      <div>
        <p v-if="tag" class="tag"><i />{{ tag }}</p>
        <h1>{{ title }}</h1>
      </div>
      <div class="page-action"><slot name="action" /></div>
    </header>
    <div class="panel"><slot /></div>
  </div>
</template>

<style scoped>
.page {
  position: relative;
  width: 100%;
  max-width: 1536px;
  min-height: 100svh;
  margin: 0 auto;
  padding: calc(var(--nav-h) + clamp(28px, 4vh, 42px)) clamp(22px, 4.2vw, 64px) 96px;
  isolation: isolate;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 20px;
  margin-bottom: clamp(20px, 4vh, 34px);
  flex-wrap: wrap;
}
.tag { display: flex; align-items: center; gap: 9px; color: var(--muted); font-size: 9px; font-weight: 500; letter-spacing: .2em; margin-bottom: 11px; }
.tag i { width: 24px; height: 1px; background: var(--muted); }
h1 { font-family: var(--font-display); font-size: clamp(2.5rem, 6vw, 5.6rem); font-weight: 400; letter-spacing: -.07em; line-height: .95; }
.page-action { display: flex; align-items: center; }
.panel {
  padding: 0;
}
@media (max-width: 700px) {
  .page { padding-top: calc(var(--nav-h) + 28px); padding-bottom: 72px; }
  .head { align-items: flex-start; }
  .page-action { width: 100%; }
}
</style>
