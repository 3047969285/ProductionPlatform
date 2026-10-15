import { onMounted, onUnmounted, ref } from 'vue'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'

gsap.registerPlugin(ScrollTrigger)

export function prefersReducedMotion() {
  if (typeof window === 'undefined' || !window.matchMedia) return false
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

// 内部工作台统一动效目标：
// 容器（工具条 / 表格框 / 看板 / 侧栏）整体上浮，列表（表格行 / 卡片 / 菜单项）逐条淡入，
// 进度条从左展开。列表行在数据到达后由 MutationObserver 补上，因此异步加载也能入场。
const DEFAULT_BLOCKS = [
  '.toolbar',
  '.tab-toolbar',
  '.table-frame',
  '.inline-error',
  '.list-error',
  '.member-load-error',
  '.lane-title',
  '.kanban',
  '.empty-state',
  '.sidebar',
].join(',')

const DEFAULT_ROWS = [
  '.el-table__row',
  'tbody > tr',
  '.kanban .card',
  '.nav-item',
  '.reveal-row',
].join(',')

const DEFAULTS = {
  blocks: DEFAULT_BLOCKS,
  rows: DEFAULT_ROWS,
  bars: '',
  scroll: false,
  blockY: 16,
  rowY: 12,
  blockStagger: 0.06,
  rowStagger: 0.035,
}

function killTween(tween) {
  tween.scrollTrigger?.kill()
  const targets = typeof tween.targets === 'function' ? tween.targets() : []
  targets.forEach((target) => {
    if (target && target.nodeType === 1) gsap.set(target, { clearProps: 'all' })
  })
  tween.kill()
}

export function createReveal(root, options) {
  const noop = { refresh() {}, replay() {}, destroy() {} }
  if (!root || typeof window === 'undefined') return noop

  const config = { ...DEFAULTS, ...(options || {}) }
  const tweens = []
  let seen = new WeakSet()
  let observer
  let frame = 0
  let disposed = false

  function collect(selector, out) {
    if (!selector) return
    const nodes = []
    if (typeof root.matches === 'function' && root.matches(selector)) nodes.push(root)
    root.querySelectorAll(selector).forEach((node) => nodes.push(node))
    nodes.forEach((node) => {
      if (seen.has(node)) return
      seen.add(node)
      out.push(node)
    })
  }

  function collectWithin(el, selector) {
    const out = []
    if (!selector) return out
    el.querySelectorAll(selector).forEach((node) => {
      if (seen.has(node)) return
      seen.add(node)
      out.push(node)
    })
    return out
  }

  function clear() {
    while (tweens.length) killTween(tweens.pop())
  }

  // el-table 的固定列是独立 DOM，行只做透明度动画，避免与主表错位。
  function rowTweens(container, rows, offset) {
    const tableRows = rows.filter((el) => el.classList.contains('el-table__row'))
    const plainRows = rows.filter((el) => !el.classList.contains('el-table__row'))
    if (tableRows.length) {
      container.from(tableRows, {
        autoAlpha: 0,
        duration: 0.42,
        stagger: config.rowStagger,
        ease: 'power2.out',
        clearProps: 'all',
      }, offset)
    }
    if (plainRows.length) {
      container.from(plainRows, {
        autoAlpha: 0,
        y: config.rowY,
        duration: 0.46,
        stagger: config.rowStagger + 0.015,
        ease: 'power3.out',
        clearProps: 'all',
      }, offset)
    }
  }

  function play() {
    if (disposed || prefersReducedMotion()) return

    const blocks = []
    collect(config.blocks, blocks)
    const bars = []
    collect(config.bars, bars)

    if (config.scroll) {
      if (!blocks.length && !bars.length) return
      // 长页面：每个区块连同它内部的行一起，滚入视口时才入场。
      blocks.forEach((el) => {
        const innerRows = collectWithin(el, config.rows)
        const timeline = gsap.timeline({
          scrollTrigger: { trigger: el, start: 'top 92%', once: true },
        })
        timeline.from(el, {
          autoAlpha: 0,
          y: config.blockY,
          duration: 0.6,
          ease: 'power3.out',
          clearProps: 'all',
        })
        rowTweens(timeline, innerRows, '-=0.4')
        tweens.push(timeline)
      })

      bars.forEach((el) => {
        tweens.push(gsap.fromTo(el,
          { scaleX: 0, transformOrigin: 'left center' },
          {
            scaleX: 1,
            duration: 0.8,
            ease: 'power3.out',
            clearProps: 'transform',
            scrollTrigger: { trigger: el, start: 'top 96%', once: true },
          },
        ))
      })

      ScrollTrigger.refresh()
      return
    }

    const rows = []
    collect(config.rows, rows)
    if (!blocks.length && !rows.length && !bars.length) return

    // 短面板：容器整体上浮，列表紧接着逐条淡入。
    if (blocks.length) {
      tweens.push(gsap.from(blocks, {
        autoAlpha: 0,
        y: config.blockY,
        duration: 0.5,
        stagger: config.blockStagger,
        ease: 'power3.out',
        clearProps: 'all',
      }))
    }

    if (rows.length) {
      const timeline = gsap.timeline({ delay: blocks.length ? 0.12 : 0 })
      rowTweens(timeline, rows, 0)
      tweens.push(timeline)
    }

    bars.forEach((el) => {
      tweens.push(gsap.fromTo(el,
        { scaleX: 0, transformOrigin: 'left center' },
        { scaleX: 1, duration: 0.8, ease: 'power3.out', delay: 0.15, clearProps: 'transform' },
      ))
    })
  }

  // rAF 合并多次 DOM 变更，并在绘制前应用起始状态，避免新增行先闪一下再淡入。
  function schedule() {
    if (disposed || frame) return
    frame = requestAnimationFrame(() => {
      frame = 0
      play()
    })
  }

  observer = new MutationObserver(schedule)
  observer.observe(root, { childList: true, subtree: true })
  schedule()

  function replay() {
    if (disposed) return
    clear()
    seen = new WeakSet()
    schedule()
  }

  function destroy() {
    disposed = true
    if (frame) cancelAnimationFrame(frame)
    observer?.disconnect()
    clear()
    ScrollTrigger.refresh()
  }

  return { refresh: play, replay, destroy }
}

// 用法：<div v-reveal> 或 <div v-reveal="{ blocks: '.card', scroll: true }">
export const vReveal = {
  mounted(el, binding) {
    el.__wbReveal = createReveal(el, binding.value)
  },
  unmounted(el) {
    el.__wbReveal?.destroy()
    delete el.__wbReveal
  },
}

// 需要在交互后重新播放时（如切换 el-tabs 子页）用组合式写法。
export function useReveal(options) {
  const root = ref(null)
  let handle

  onMounted(() => {
    handle = createReveal(root.value, options)
  })

  onUnmounted(() => {
    handle?.destroy()
    handle = undefined
  })

  return {
    root,
    refresh: () => handle?.refresh(),
    replay: () => handle?.replay(),
  }
}
