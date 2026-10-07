<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { gsap } from 'gsap'

const props = defineProps({
  modelValue: { type: String, required: true },
  stages: { type: Array, required: true },
})

const emit = defineEmits(['update:modelValue', 'change'])
const root = ref(null)
let context
let progressTween
let dotTween
let reducedMotion = false

const activeIndex = computed(() => Math.max(0, props.stages.findIndex((stage) => stage.key === props.modelValue)))
const activeStage = computed(() => props.stages[activeIndex.value] || props.stages[0])

function selectStage(stage) {
  if (stage.key === props.modelValue) return
  emit('update:modelValue', stage.key)
  emit('change', stage.key)
}

async function animateProgress() {
  await nextTick()
  const fill = root.value?.querySelector('.stage-progress-fill')
  const activeDot = root.value?.querySelector('.stage.is-active .stage-dot')
  if (!fill) return
  const ratio = activeIndex.value / Math.max(props.stages.length - 1, 1)
  progressTween?.kill()
  dotTween?.kill()
  progressTween = gsap.to(fill, {
    scaleX: ratio,
    duration: reducedMotion ? 0 : 0.5,
    ease: 'power3.out',
    overwrite: 'auto',
  })
  if (activeDot && !reducedMotion) {
    dotTween = gsap.fromTo(activeDot, { scale: 0.86 }, { scale: 1, duration: 0.45, ease: 'back.out(1.8)', overwrite: 'auto' })
  }
}

onMounted(() => {
  if (!root.value) return
  reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  context = gsap.context(() => {
    gsap.from(root.value.querySelectorAll('.stage'), {
      autoAlpha: 0,
      y: reducedMotion ? 0 : 12,
      duration: reducedMotion ? 0 : 0.45,
      stagger: reducedMotion ? 0 : 0.06,
      ease: 'power3.out',
      clearProps: 'all',
    })
  }, root.value)
  animateProgress()
})

watch(() => props.modelValue, animateProgress)

onUnmounted(() => {
  progressTween?.kill()
  dotTween?.kill()
  context?.revert()
})
</script>

<template>
  <section ref="root" class="stage-nav" aria-label="项目交付阶段">
    <div class="stage-heading">
      <div>
        <p class="eyebrow">DELIVERY FLOW</p>
        <h2>{{ activeStage?.label || '交付流程' }}</h2>
        <p>{{ activeStage?.description }}</p>
      </div>
      <div class="stage-counter">
        <strong>{{ String(activeIndex + 1).padStart(2, '0') }}</strong>
        <span>/ {{ String(stages.length).padStart(2, '0') }}</span>
      </div>
    </div>

    <div class="stage-rail" role="group" aria-label="选择交付阶段">
      <div class="stage-progress" aria-hidden="true"><span class="stage-progress-fill" /></div>
      <button
        v-for="(stage, index) in stages"
        :key="stage.key"
        type="button"
        class="stage"
        :class="{ 'is-active': stage.key === modelValue, 'is-complete': index < activeIndex }"
        :aria-pressed="stage.key === modelValue"
        @click="selectStage(stage)"
      >
        <span class="stage-dot">{{ index + 1 }}</span>
        <span class="stage-text"><strong>{{ stage.label }}</strong><small>{{ stage.short }}</small></span>
      </button>
    </div>
  </section>
</template>

<style scoped>
.stage-nav {
  padding: 22px 22px 20px;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  background: rgba(23, 27, 25, .84);
  box-shadow: var(--shadow-sm);
}
.stage-heading { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; margin-bottom: 22px; }
.eyebrow { color: var(--accent); font-size: 10px; font-weight: 700; letter-spacing: .18em; margin-bottom: 7px; }
h2 { font-family: var(--font-display); font-size: clamp(1.45rem, 2vw, 2rem); font-weight: 400; letter-spacing: -.04em; margin-bottom: 4px; }
.stage-heading p:last-child { color: var(--muted); font-size: 13px; }
.stage-counter { display: flex; align-items: baseline; gap: 4px; color: var(--muted-light); }
.stage-counter strong { color: var(--accent); font-family: var(--font-display); font-size: 2rem; line-height: 1; }
.stage-counter span { font-size: 12px; }
.stage-rail { position: relative; display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 6px; }
.stage-progress { position: absolute; top: 16px; left: 7%; right: 7%; height: 1px; background: rgba(234, 238, 222, .15); z-index: 0; overflow: hidden; }
.stage-progress-fill { display: block; width: 100%; height: 100%; background: var(--accent); transform: scaleX(0); transform-origin: left center; }
.stage { position: relative; z-index: 1; display: flex; flex-direction: column; align-items: center; gap: 8px; min-width: 0; border: 0; background: transparent; color: var(--muted); cursor: pointer; font: inherit; text-align: center; }
.stage-dot { display: grid; place-items: center; width: 34px; height: 34px; border: 1px solid var(--border-strong); border-radius: 50%; background: var(--surface); color: var(--muted); font-size: 11px; font-weight: 700; transition: background .25s var(--ease), border-color .25s var(--ease), color .25s var(--ease), box-shadow .25s var(--ease), transform .25s var(--ease); }
.stage-text strong, .stage-text small { display: block; }
.stage-text strong { font-size: 14px; color: var(--text); transition: color .25s var(--ease); }
.stage-text small { margin-top: 2px; color: var(--muted); font-size: 11px; white-space: nowrap; }
.stage:hover .stage-dot { border-color: var(--accent); color: var(--accent); transform: translateY(-1px); }
.stage.is-active .stage-dot { border-color: var(--accent); background: var(--accent); color: #11140f; box-shadow: 0 0 0 6px rgba(239, 239, 235, .1); }
.stage.is-active .stage-text strong { color: var(--accent); }
.stage.is-complete .stage-dot { border-color: var(--accent); color: var(--accent); background: rgba(239, 239, 235, .08); }
@media (max-width: 700px) {
  .stage-nav { padding: 16px 12px 14px; }
  .stage-heading { margin-bottom: 18px; }
  .stage-text small { display: none; }
  .stage-progress { left: 9%; right: 9%; }
}
</style>
