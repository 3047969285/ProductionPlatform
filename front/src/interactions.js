import { onMounted, onUnmounted, ref } from 'vue'
import { gsap } from 'gsap'
import { prefersReducedMotion } from './motion'

// 交互动效层：数字滚动、光标跟随高光、点击波纹、按压反馈。
// 与 motion.js 的「入场动画」互补 —— motion 负责元素第一次出现，这里负责用户操作后的即时反馈。
// 全部动效在 prefers-reduced-motion 下自动降级为直接呈现终态。

const GLOW = 'wb-glow'
const GLOW_ACTIVE = 'is-glowing'
const PRESS = 'wb-press'
const PRESS_ACTIVE = 'is-pressed'
const RIPPLE = 'wb-ripple'
const RIPPLE_DURATION = 760

/* ── 数字滚动 ─────────────────────────────────────────── */

function toNumber(value) {
  if (typeof value === 'number') return value
  if (typeof value === 'string') {
    const trimmed = value.trim()
    if (!trimmed) return Number.NaN
    return Number(trimmed.replace(/[,%\s]/g, ''))
  }
  return Number.NaN
}

function formatNumber(value, decimals, prefix, suffix) {
  const base = Number.isFinite(value) ? value.toFixed(decimals) : String(value)
  return `${prefix}${base}${suffix}`
}

// 非数字文本（如「逾期」「—」）直接落位，不做滚动。
// from 省略时以当前显示值为起点（用于数值刷新）；首次出现应显式传 from: 0。
export function countUp(el, options = {}) {
  if (!el) return null

  el.__wbCount?.kill()
  el.__wbCount = null

  const raw = options.value
  const target = toNumber(raw)
  const decimals = Number.isFinite(options.decimals) ? options.decimals : 0
  const prefix = options.prefix || ''
  const suffix = options.suffix || ''

  if (!Number.isFinite(target)) {
    el.textContent = raw == null || raw === '' ? '—' : String(raw)
    return null
  }

  if (prefersReducedMotion()) {
    el.textContent = formatNumber(target, decimals, prefix, suffix)
    return null
  }

  const start = Number.isFinite(options.from) ? Number(options.from) : toNumber(el.textContent)
  const state = { n: Number.isFinite(start) ? start : 0 }
  if (state.n !== target) el.textContent = formatNumber(state.n, decimals, prefix, suffix)
  const tween = gsap.to(state, {
    n: target,
    duration: Number.isFinite(options.duration) ? options.duration : 0.85,
    delay: Number.isFinite(options.delay) ? options.delay : 0,
    ease: options.ease || 'power2.out',
    onUpdate() { el.textContent = formatNumber(state.n, decimals, prefix, suffix) },
    onComplete() {
      el.textContent = formatNumber(target, decimals, prefix, suffix)
      if (el.__wbCount === tween) el.__wbCount = null
    },
  })
  el.__wbCount = tween
  return tween
}

function resolveCount(binding) {
  const raw = binding.value
  if (raw && typeof raw === 'object' && !Array.isArray(raw)) {
    return {
      value: raw.value,
      decimals: raw.decimals,
      prefix: raw.prefix,
      suffix: raw.suffix,
      duration: raw.duration,
      delay: raw.delay,
      from: raw.from,
    }
  }
  return { value: raw }
}

// 用法：<strong v-count="42" /> 或 <strong v-count="{ value: progress, suffix: '%' }" />
export const vCount = {
  mounted(el, binding) {
    const options = resolveCount(binding)
    el.__wbCountKey = JSON.stringify(options)
    // 元素刚出现时模板里已经是终值，必须显式从 0 起算，否则起点等于终点看不出滚动。
    countUp(el, { ...options, from: options.from ?? 0 })
  },
  updated(el, binding) {
    const options = resolveCount(binding)
    const key = JSON.stringify(options)
    if (key === el.__wbCountKey) return
    el.__wbCountKey = key
    countUp(el, options)
  },
  unmounted(el) {
    el.__wbCount?.kill()
    delete el.__wbCount
    delete el.__wbCountKey
  },
}

/* ── 光标跟随高光 ─────────────────────────────────────── */

// 指针位置写进 CSS 变量，由 global.css 的 .wb-glow::after 画一层径向光斑。
function bindGlow(el) {
  let frame = 0
  let next = null

  const apply = () => {
    frame = 0
    if (!next) return
    el.style.setProperty('--wb-mx', `${next.x}px`)
    el.style.setProperty('--wb-my', `${next.y}px`)
    next = null
  }

  const move = (event) => {
    const rect = el.getBoundingClientRect()
    if (!rect.width || !rect.height) return
    next = { x: event.clientX - rect.left, y: event.clientY - rect.top }
    if (!frame) frame = requestAnimationFrame(apply)
  }
  const enter = () => el.classList.add(GLOW_ACTIVE)
  const leave = () => {
    el.classList.remove(GLOW_ACTIVE)
    if (frame) { cancelAnimationFrame(frame); frame = 0 }
    next = null
  }

  el.classList.add(GLOW)
  // 只在 static 时补 relative，避免覆盖 .project-card 这类依赖绝对定位的元素。
  const positioned = window.getComputedStyle(el).position !== 'static'
  if (!positioned) el.style.position = 'relative'
  el.addEventListener('pointermove', move, { passive: true })
  el.addEventListener('pointerenter', enter, { passive: true })
  el.addEventListener('pointerleave', leave, { passive: true })

  return () => {
    el.removeEventListener('pointermove', move)
    el.removeEventListener('pointerenter', enter)
    el.removeEventListener('pointerleave', leave)
    if (frame) cancelAnimationFrame(frame)
    el.classList.remove(GLOW, GLOW_ACTIVE)
    el.style.removeProperty('--wb-mx')
    el.style.removeProperty('--wb-my')
    if (!positioned) el.style.removeProperty('position')
  }
}

/* ── 点击波纹 ─────────────────────────────────────────── */

// 波纹画在 body 上的共享图层里，避免往 Vue 管理的 DOM 里插节点。
let rippleLayer = null

function ensureRippleLayer() {
  if (typeof document === 'undefined') return null
  if (rippleLayer?.isConnected) return rippleLayer
  rippleLayer = document.createElement('div')
  rippleLayer.className = 'wb-ripple-layer'
  rippleLayer.setAttribute('aria-hidden', 'true')
  document.body.appendChild(rippleLayer)
  return rippleLayer
}

function spawnRipple(el, event) {
  const layer = ensureRippleLayer()
  if (!layer) return
  const rect = el.getBoundingClientRect()
  if (!rect.width || !rect.height) return

  const node = document.createElement('span')
  node.className = RIPPLE
  node.style.left = `${rect.left}px`
  node.style.top = `${rect.top}px`
  node.style.width = `${rect.width}px`
  node.style.height = `${rect.height}px`
  node.style.borderRadius = window.getComputedStyle(el).borderRadius
  node.style.transformOrigin = `${event.clientX - rect.left}px ${event.clientY - rect.top}px`
  layer.appendChild(node)
  window.setTimeout(() => node.remove(), RIPPLE_DURATION)
}

function bindRipple(el) {
  const down = (event) => {
    if (event.button !== 0 || el.disabled) return
    spawnRipple(el, event)
  }
  el.addEventListener('pointerdown', down, { passive: true })
  return () => el.removeEventListener('pointerdown', down)
}

/* ── 按压反馈 ─────────────────────────────────────────── */

function bindPress(el) {
  const down = (event) => {
    if (event.button !== 0 || el.disabled) return
    el.classList.add(PRESS_ACTIVE)
  }
  const up = () => el.classList.remove(PRESS_ACTIVE)

  el.classList.add(PRESS)
  el.addEventListener('pointerdown', down)
  el.addEventListener('pointerup', up)
  el.addEventListener('pointerleave', up)
  el.addEventListener('pointercancel', up)

  return () => {
    el.removeEventListener('pointerdown', down)
    el.removeEventListener('pointerup', up)
    el.removeEventListener('pointerleave', up)
    el.removeEventListener('pointercancel', up)
    el.classList.remove(PRESS, PRESS_ACTIVE)
  }
}

/* ── 组合入口 ─────────────────────────────────────────── */

const BINDERS = [
  ['glow', bindGlow],
  ['ripple', bindRipple],
  ['press', bindPress],
]

// 模块级去重：多个 createInteractions 作用在同一子树上时（例如全局层 + 页面层），
// 同一个节点只绑一次，避免重复监听和相互拆台。
const bound = { glow: new WeakSet(), ripple: new WeakSet(), press: new WeakSet() }

// 异步数据到达后新出现的节点由 MutationObserver 补挂。
export function createInteractions(root, options = {}) {
  const noop = { refresh() {}, destroy() {} }
  if (!root || typeof window === 'undefined') return noop
  if (prefersReducedMotion()) return noop

  const spec = BINDERS
    .map(([key, bind]) => ({ key, selector: options[key], bind }))
    .filter((item) => item.selector)

  if (!spec.length) return noop

  const teardown = []
  let observer
  let frame = 0
  let disposed = false

  function scan() {
    frame = 0
    if (disposed) return
    spec.forEach(({ key, selector, bind }) => {
      root.querySelectorAll(selector).forEach((el) => {
        if (bound[key].has(el)) return
        bound[key].add(el)
        const off = bind(el)
        if (typeof off === 'function') teardown.push(off)
      })
    })
  }

  function schedule() {
    if (disposed || frame) return
    frame = requestAnimationFrame(scan)
  }

  observer = new MutationObserver(schedule)
  observer.observe(root, { childList: true, subtree: true })
  scan()

  return {
    refresh: schedule,
    destroy() {
      disposed = true
      if (frame) cancelAnimationFrame(frame)
      observer?.disconnect()
      while (teardown.length) {
        try { teardown.pop()() } catch { /* 忽略清理异常 */ }
      }
    },
  }
}

// 用法：<div v-interact="{ glow: '.card', ripple: 'button', press: '.card' }">
export const vInteract = {
  mounted(el, binding) {
    el.__wbInteract = createInteractions(el, binding.value || {})
  },
  unmounted(el) {
    el.__wbInteract?.destroy()
    delete el.__wbInteract
  },
}

// 组合式写法：const { root } = useInteractions({ glow: '.card' })
export function useInteractions(options = {}) {
  const root = ref(null)
  let handle

  onMounted(() => {
    handle = createInteractions(root.value, options)
  })

  onUnmounted(() => {
    handle?.destroy()
    handle = undefined
  })

  return {
    root,
    refresh: () => handle?.refresh(),
    destroy: () => handle?.destroy(),
  }
}

/* ── 滚动进度 ─────────────────────────────────────────── */

// 顶部 1px 进度线，随页面滚动展开，给整个系统一个持续的「活着」信号。
export function createScrollProgress(bar) {
  const noop = { refresh() {}, destroy() {} }
  if (!bar || typeof window === 'undefined') return noop

  let frame = 0
  const update = () => {
    frame = 0
    const doc = document.documentElement
    const max = doc.scrollHeight - window.innerHeight
    const ratio = max > 0 ? Math.min(1, Math.max(0, window.scrollY / max)) : 0
    bar.style.transform = `scaleX(${ratio})`
    bar.classList.toggle('is-active', ratio > 0.001 && ratio < 0.999)
  }
  const schedule = () => { if (!frame) frame = requestAnimationFrame(update) }

  window.addEventListener('scroll', schedule, { passive: true })
  window.addEventListener('resize', schedule, { passive: true })
  schedule()

  return {
    refresh: schedule,
    destroy() {
      window.removeEventListener('scroll', schedule)
      window.removeEventListener('resize', schedule)
      if (frame) cancelAnimationFrame(frame)
    },
  }
}
