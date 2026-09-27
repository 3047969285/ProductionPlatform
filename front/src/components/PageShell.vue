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
    <span class="page-grid" aria-hidden="true" />
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
  max-width: 1440px;
  margin: 0 auto;
  padding: 44px clamp(20px, 4vw, 64px) 72px;
  isolation: isolate;
}
.page-grid {
  position: absolute; z-index: -1; top: 18px; right: clamp(20px, 4vw, 64px); width: min(30vw, 360px); height: 180px;
  border-top: 1px solid rgba(203, 210, 118, .12); border-right: 1px solid rgba(203, 210, 118, .12);
  pointer-events: none; opacity: .8;
  background: linear-gradient(90deg, transparent 49.8%, rgba(234, 238, 222, .05) 50%, transparent 50.2%), linear-gradient(rgba(234, 238, 222, .05) 1px, transparent 1px);
  background-size: 100% 100%, 100% 24px;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}
.tag { display: flex; align-items: center; gap: 8px; color: var(--accent); font-size: 10px; font-weight: 600; letter-spacing: .18em; margin-bottom: 9px; }
.tag i { width: 22px; height: 1px; background: var(--accent); }
h1 { font-family: var(--font-display); font-size: clamp(2.1rem, 4.2vw, 3.7rem); font-weight: 400; letter-spacing: -.045em; line-height: 1.05; }
.page-signal { margin-top: 11px; color: var(--muted-light); font-size: 9px; letter-spacing: .18em; }
.page-action { display: flex; align-items: center; }
.panel {
  background: var(--glass);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: clamp(16px, 2vw, 24px);
  backdrop-filter: blur(16px) saturate(1.3);
  -webkit-backdrop-filter: blur(16px) saturate(1.3);
  box-shadow: var(--shadow-sm);
}
@media (max-width: 700px) {
  .page { padding-top: 30px; }
  .page-grid { width: 52vw; height: 130px; }
}
</style>
