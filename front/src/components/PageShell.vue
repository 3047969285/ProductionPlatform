<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { gsap } from 'gsap'

defineProps({
  tag: String,
  title: String,
})

const root = ref(null)
let context

onMounted(() => {
  if (!root.value) return
  context = gsap.context(() => {
    const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    gsap.from(root.value.querySelectorAll('.page-reveal'), {
      autoAlpha: 0,
      y: reduced ? 0 : 14,
      duration: reduced ? 0 : .5,
      stagger: reduced ? 0 : .08,
      ease: 'power3.out',
      clearProps: 'all',
    })
  }, root.value)
})

onUnmounted(() => context?.revert())
</script>

<template>
  <div ref="root" class="page">
    <header class="head page-reveal">
      <div>
        <p v-if="tag" class="tag"><i />{{ tag }}</p>
        <h1>{{ title }}</h1>
        <p class="page-signal">PDE / DELIVERY / LIVE</p>
      </div>
      <div class="page-action"><slot name="action" /></div>
    </header>
    <div class="panel page-reveal"><slot /></div>
  </div>
</template>

<style scoped>
.page {
  position: relative;
  width: 100%;
  max-width: 1700px;
  min-height: 100svh;
  margin: 0 auto;
  padding: clamp(98px, 14vh, 142px) clamp(22px, 5.2vw, 84px) 112px;
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
.page-signal { margin-top: 12px; color: var(--muted-light); font-size: 8px; letter-spacing: .2em; }
.page-action { display: flex; align-items: center; }
.panel {
  padding: 0;
}
@media (max-width: 700px) {
  .page { padding-top: 96px; padding-bottom: 88px; }
  .head { align-items: flex-start; }
  .page-action { width: 100%; }
}
</style>
