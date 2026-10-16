import { gsap } from 'gsap'
import { prefersReducedMotion } from './motion'

// 全站常驻的动态层：粒子星网背景、卡片 3D 倾斜、磁吸按钮。
// 鼠标光斑和顶部滚动进度条已由 interactions.js 提供，这里不重复。页面一打开就在动，不需要任何点击。
// 全部是装饰层（pointer-events: none），并遵循 prefers-reduced-motion。

const TILT_SELECTOR = '.metric-card, .card, .cockpit-section, .stat, .timeline-item, .report-card'
const MAGNET_SELECTOR = '.create-button, .el-button--primary, .nav-search, .back-button'

export function startAmbient() {
  if (typeof window === 'undefined' || prefersReducedMotion()) return () => {}

  const disposers = []

  /* ── 粒子星网 ───────────────────────────────────────── */
  const canvas = document.createElement('canvas')
  canvas.className = 'ambient-canvas'
  canvas.setAttribute('aria-hidden', 'true')
  document.body.prepend(canvas)
  const ctx = canvas.getContext('2d')
  let width = 0
  let height = 0
  let dpr = 1
  let particles = []
  const pointer = { x: -9999, y: -9999, tx: -9999, ty: -9999 }
  let raf = 0
  let running = true

  function resize() {
    dpr = Math.min(window.devicePixelRatio || 1, 2)
    width = window.innerWidth
    height = window.innerHeight
    canvas.width = Math.floor(width * dpr)
    canvas.height = Math.floor(height * dpr)
    canvas.style.width = `${width}px`
    canvas.style.height = `${height}px`
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
    const count = Math.round(Math.min(90, Math.max(28, (width * height) / 22000)))
    particles = Array.from({ length: count }, () => ({
      x: Math.random() * width,
      y: Math.random() * height,
      vx: (Math.random() - 0.5) * 0.28,
      vy: (Math.random() - 0.5) * 0.28,
      r: Math.random() * 1.3 + 0.5,
      phase: Math.random() * Math.PI * 2,
    }))
  }

  function frame(time) {
    if (!running) return
    raf = requestAnimationFrame(frame)
    // 指针缓动，避免光点瞬移
    pointer.x += (pointer.tx - pointer.x) * 0.12
    pointer.y += (pointer.ty - pointer.y) * 0.12
    ctx.clearRect(0, 0, width, height)

    const link = 128
    for (let i = 0; i < particles.length; i += 1) {
      const p = particles[i]
      p.x += p.vx
      p.y += p.vy
      if (p.x < -10) p.x = width + 10
      else if (p.x > width + 10) p.x = -10
      if (p.y < -10) p.y = height + 10
      else if (p.y > height + 10) p.y = -10

      // 靠近鼠标的粒子被轻轻推开，再缓慢漂回
      const dx = p.x - pointer.x
      const dy = p.y - pointer.y
      const d2 = dx * dx + dy * dy
      if (d2 < 140 * 140 && d2 > 1) {
        const d = Math.sqrt(d2)
        const push = (1 - d / 140) * 1.4
        p.x += (dx / d) * push
        p.y += (dy / d) * push
      }

      const twinkle = 0.35 + 0.35 * Math.sin(time / 900 + p.phase)
      ctx.beginPath()
      ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(239, 239, 235, ${twinkle})`
      ctx.fill()

      for (let j = i + 1; j < particles.length; j += 1) {
        const q = particles[j]
        const lx = p.x - q.x
        const ly = p.y - q.y
        const ld = lx * lx + ly * ly
        if (ld < link * link) {
          const alpha = (1 - Math.sqrt(ld) / link) * 0.16
          ctx.strokeStyle = `rgba(239, 239, 235, ${alpha})`
          ctx.lineWidth = 0.6
          ctx.beginPath()
          ctx.moveTo(p.x, p.y)
          ctx.lineTo(q.x, q.y)
          ctx.stroke()
        }
      }

      // 鼠标附近与粒子连线，形成"被指针牵引"的星网
      if (d2 < 190 * 190) {
        const alpha = (1 - Math.sqrt(d2) / 190) * 0.34
        ctx.strokeStyle = `rgba(214, 232, 196, ${alpha})`
        ctx.lineWidth = 0.7
        ctx.beginPath()
        ctx.moveTo(p.x, p.y)
        ctx.lineTo(pointer.x, pointer.y)
        ctx.stroke()
      }
    }
  }

  resize()
  gsap.fromTo(canvas, { autoAlpha: 0 }, { autoAlpha: 1, duration: 1.6, ease: 'power2.out' })
  raf = requestAnimationFrame(frame)
  window.addEventListener('resize', resize, { passive: true })
  const onVisibility = () => {
    running = !document.hidden
    if (running) raf = requestAnimationFrame(frame)
  }
  document.addEventListener('visibilitychange', onVisibility)
  disposers.push(() => {
    running = false
    cancelAnimationFrame(raf)
    window.removeEventListener('resize', resize)
    document.removeEventListener('visibilitychange', onVisibility)
    canvas.remove()
  })

  /* ── 指针联动：光斑、卡片倾斜、磁吸按钮 ─────────────── */
  let tiltEl = null
  let magnetEl = null

  function resetTilt(el) {
    if (!el) return
    gsap.to(el, { rotateX: 0, rotateY: 0, duration: 0.6, ease: 'power3.out', overwrite: 'auto' })
  }

  function resetMagnet(el) {
    if (!el) return
    gsap.to(el, { x: 0, y: 0, duration: 0.55, ease: 'elastic.out(1, 0.55)', overwrite: 'auto' })
  }

  function onMove(event) {
    pointer.tx = event.clientX
    pointer.ty = event.clientY

    const target = event.target instanceof Element ? event.target : null
    const card = target?.closest(TILT_SELECTOR) || null
    if (card !== tiltEl) {
      resetTilt(tiltEl)
      tiltEl = card
    }
    if (card) {
      const rect = card.getBoundingClientRect()
      // 大面积容器只做很轻的倾斜，避免整页晃动
      const strength = rect.width > 520 ? 1.2 : 4.5
      const px = (event.clientX - rect.left) / rect.width - 0.5
      const py = (event.clientY - rect.top) / rect.height - 0.5
      gsap.to(card, {
        rotateY: px * strength,
        rotateX: -py * strength,
        transformPerspective: 900,
        transformOrigin: 'center',
        duration: 0.5,
        ease: 'power3.out',
        overwrite: 'auto',
      })
    }

    const magnet = target?.closest(MAGNET_SELECTOR) || null
    if (magnet !== magnetEl) {
      resetMagnet(magnetEl)
      magnetEl = magnet
    }
    if (magnet) {
      const rect = magnet.getBoundingClientRect()
      const mx = event.clientX - (rect.left + rect.width / 2)
      const my = event.clientY - (rect.top + rect.height / 2)
      gsap.to(magnet, { x: mx * 0.22, y: my * 0.3, duration: 0.35, ease: 'power3.out', overwrite: 'auto' })
    }
  }

  function onLeave() {
    pointer.tx = -9999
    pointer.ty = -9999
    resetTilt(tiltEl)
    resetMagnet(magnetEl)
    tiltEl = null
    magnetEl = null
  }

  window.addEventListener('pointermove', onMove, { passive: true })
  document.documentElement.addEventListener('pointerleave', onLeave)
  disposers.push(() => {
    window.removeEventListener('pointermove', onMove)
    document.documentElement.removeEventListener('pointerleave', onLeave)
  })

  return () => disposers.splice(0).forEach((dispose) => dispose())
}

// 页面标题逐字入场：每个字从遮罩下方翻起，之后随滚动轻微视差上移。
export function splitTitle(el) {
  if (!el || el.dataset.split === '1') return []
  const text = el.textContent || ''
  if (!text.trim()) return []
  el.dataset.split = '1'
  el.setAttribute('aria-label', text)
  el.textContent = ''
  const chars = []
  for (const ch of text) {
    const mask = document.createElement('span')
    mask.className = 'split-mask'
    mask.setAttribute('aria-hidden', 'true')
    const inner = document.createElement('span')
    inner.className = 'split-char'
    inner.textContent = ch === ' ' ? ' ' : ch
    mask.appendChild(inner)
    el.appendChild(mask)
    chars.push(inner)
  }
  return chars
}
