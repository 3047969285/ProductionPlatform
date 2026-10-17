<script setup>
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import { splitTitle } from '../ambient'

gsap.registerPlugin(ScrollTrigger)

const props = defineProps({
  tag: String,
  title: String,
})

const root = ref(null)
let context

// 标题被拆成逐字 span 后 Vue 的文本节点已脱离 DOM，标题变化（如报表切换项目）要手动重拆并播一遍。
watch(() => props.title, async (next) => {
  const el = root.value?.querySelector('h1')
  if (!el || el.dataset.split !== '1') return
  await nextTick()
  el.dataset.split = ''
  el.textContent = next || ''
  const chars = splitTitle(el)
  if (chars.length) {
    gsap.from(chars, { yPercent: 118, rotateX: -70, autoAlpha: 0, duration: 0.7, stagger: 0.035, ease: 'expo.out', clearProps: 'transform,opacity,visibility' })
  }
})

// 只负责页头入场；内容区的动效由各工作台自己用 v-reveal 声明，
// 避免外层容器与内层面板叠加出两次位移。
onMounted(() => {
  if (!root.value) return
  const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (reduced) return
  context = gsap.context(() => {
    const title = root.value.querySelector('h1')
    const chars = splitTitle(title)
    const timeline = gsap.timeline({ defaults: { ease: 'power3.out' } })
    timeline
      .from(root.value.querySelectorAll('.tag'), { autoAlpha: 0, x: -14, duration: 0.5, clearProps: 'all' })
      .from(root.value.querySelectorAll('.tag i'), { scaleX: 0, transformOrigin: 'left center', duration: 0.7, ease: 'power4.out' }, '<')
    if (chars.length) {
      // 逐字从遮罩下方翻起，带一点 3D 旋转和字间延迟
      timeline.from(chars, {
        yPercent: 118,
        rotateX: -70,
        autoAlpha: 0,
        transformOrigin: '50% 100%',
        duration: 0.95,
        stagger: 0.055,
        ease: 'expo.out',
        clearProps: 'transform,opacity,visibility',
      }, '-=0.35')
    } else {
      timeline.from(root.value.querySelectorAll('h1'), { autoAlpha: 0, y: 22, duration: 0.6, ease: 'power4.out', clearProps: 'all' }, '-=0.22')
    }
    timeline.from(root.value.querySelectorAll('.page-action'), { autoAlpha: 0, y: 14, duration: 0.55, clearProps: 'all' }, '-=0.6')

    // 滚动时标题缓慢上移并淡出，和下方内容形成景深差
    if (title) {
      gsap.to(title, {
        yPercent: -28,
        autoAlpha: 0.15,
        ease: 'none',
        scrollTrigger: { trigger: root.value, start: 'top top', end: '+=420', scrub: 0.6 },
      })
    }
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
  max-width: none;
  min-height: 100svh;
  margin: 0 auto;
  padding: calc(var(--nav-h) + clamp(10px, 2vh, 20px)) clamp(12px, 1.6vw, 28px) 32px;
  isolation: isolate;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 20px;
  margin-bottom: clamp(10px, 2vh, 16px);
  flex-wrap: wrap;
}
.tag { display: flex; align-items: center; gap: 9px; color: var(--muted); font-size: 11px; font-weight: 500; letter-spacing: .2em; margin-bottom: 6px; }
.tag i { width: 24px; height: 1px; background: var(--muted); }
h1 { font-family: var(--font-display); font-size: clamp(1.9rem, 3.4vw, 3.2rem); font-weight: 400; letter-spacing: -.07em; line-height: .95; }
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
