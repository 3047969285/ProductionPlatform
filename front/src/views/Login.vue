<script setup>
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { gsap } from 'gsap'
import api from '../api'
import { setAuth } from '../auth'

const router = useRouter()
const root = ref(null)
const introSkipButton = ref(null)
// introPhase：开场进行中（此时登录卡片不可交互）
// loginReady：已进入登录态，卡片可交互
const introPhase = ref(true)
const loginReady = ref(false)
const loading = ref(false)
const form = ref({ username: '', password: '' })
const prefersReducedMotion = ref(false)

let introTimeline
let transitionTimeline
let ambientTween
let reducedMotion = false

async function submit() {
  loading.value = true
  try {
    const res = await api.post('/auth/login', form.value)
    setAuth(res.data.token, res.data.user)
    ElMessage.success('已进入')
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}

function focusLoginInput() {
  root.value?.querySelector('input[autocomplete="username"]')?.focus({ preventScroll: true })
}

// 开场结束后，圆环留在背景里，舞台整体上移让出中央位置给登录卡片。
function enterLogin() {
  if (loginReady.value) return
  loginReady.value = true

  const stage = root.value?.querySelector('.scene-stage')
  const card = root.value?.querySelector('.login-card')
  if (!stage || !card) return

  if (reducedMotion) {
    introPhase.value = false
    gsap.set(stage, { y: 0 })
    gsap.set(card, { autoAlpha: 1, y: 0, scale: 1 })
    gsap.set('.intro-work', { autoAlpha: 0 })
    gsap.set('.intro-core', { autoAlpha: 0 })
    focusLoginInput()
    return
  }

  transitionTimeline = gsap.timeline({ defaults: { ease: 'power3.out' } })
    .to('.intro-work', { autoAlpha: 0, duration: 0.5, stagger: 0.06 }, 0)
    .to('.intro-skip', { autoAlpha: 0, duration: 0.28 }, 0)
    // 核心标记退场，把圆环中央让给标题与登录卡片
    .to('.intro-core', { autoAlpha: 0, scale: 0.88, duration: 0.45 }, 0.08)
    .to(stage, { y: 0, duration: 0.85, ease: 'power3.inOut' }, 0)
    .fromTo(card,
      { autoAlpha: 0, y: 28, scale: 0.985 },
      { autoAlpha: 1, y: 0, scale: 1, duration: 0.8 },
      0.2)
    .call(() => { introPhase.value = false }, [], 0.62)
    .call(focusLoginInput, [], 0.92)

  startAmbientRotation()
}

// 圆环缓慢自转，让背景保持呼吸感
function startAmbientRotation() {
  if (reducedMotion) return
  ambientTween?.kill()
  ambientTween = gsap.to('.intro-orbits', {
    rotation: '+=360',
    duration: 160,
    repeat: -1,
    ease: 'none',
    transformOrigin: '50% 50%',
  })
}

function skipIntro() {
  if (introTimeline) {
    introTimeline.progress(1)
    return
  }
  enterLogin()
}

function handleKeydown(event) {
  if (event.key === 'Escape' && introPhase.value) skipIntro()
}

function startIntroAnimation() {
  if (!root.value) return

  ambientTween?.kill()
  transitionTimeline?.kill()
  introTimeline?.kill()
  introTimeline = undefined

  introPhase.value = true
  loginReady.value = false

  const stage = root.value.querySelector('.scene-stage')
  const card = root.value.querySelector('.login-card')
  const gap = parseFloat(getComputedStyle(stage).rowGap) || 0

  // 开场阶段登录卡片只占位不显示，把舞台下移，让标题正好落在圆环中心
  gsap.set('.intro-orbits', { rotation: 0 })
  gsap.set(card, { autoAlpha: 0 })
  gsap.set('.intro-skip', { autoAlpha: 1 })
  gsap.set(stage, { y: (card.offsetHeight + gap) / 2 })

  gsap.set('.intro-ring, .intro-orbit, .intro-core, .intro-copy > *', { autoAlpha: 0 })
  gsap.set('.intro-work', { autoAlpha: 0, scale: 0.96 })
  gsap.set('.intro-ring--outer', { scale: 0.82, transformOrigin: '50% 50%' })
  gsap.set('.intro-ring--inner', { scale: 1.12, transformOrigin: '50% 50%' })
  gsap.set('.intro-core', { scale: 0.82, y: 8, transformOrigin: '50% 50%' })
  gsap.set('.intro-copy > *', { y: 12 })

  introTimeline = gsap.timeline({ defaults: { ease: 'power3.out' } })
    .to('.intro-ring--outer', { autoAlpha: 0.64, scale: 1, duration: 1.05 }, 0)
    .to('.intro-ring--inner', { autoAlpha: 1, scale: 1, duration: 1.18 }, 0.08)
    .to('.intro-ring--chromatic', { autoAlpha: 0.72, duration: 0.9 }, 0.12)
    .to('.intro-orbit', { autoAlpha: 0.82, duration: 0.75 }, 0.28)
    .to('.intro-work--left, .intro-work--right', { autoAlpha: 0.74, scale: 1, duration: 0.95, stagger: 0.08 }, 0.16)
    .to('.intro-core', { autoAlpha: 1, scale: 1, y: 0, duration: 0.7 }, 0.42)
    .to('.intro-copy > *', { autoAlpha: 1, y: 0, duration: 0.64, stagger: 0.12 }, 0.64)
    .to('.intro-orbit--slow', { rotation: 22, duration: 2.35, ease: 'none', transformOrigin: '50% 50%' }, 0.2)
    .call(enterLogin, [], 1.66)
}

function playIntroAnimation() {
  prefersReducedMotion.value = false
  reducedMotion = false
  startIntroAnimation()
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
  if (!root.value) return
  nextTick(() => introSkipButton.value?.focus({ preventScroll: true }))

  reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  prefersReducedMotion.value = reducedMotion

  if (reducedMotion) {
    // 减少动效：圆环静态留在背景，直接进入登录态
    introPhase.value = false
    loginReady.value = true
    gsap.set('.intro-work', { autoAlpha: 0 })
    gsap.set('.login-card', { autoAlpha: 1 })
    gsap.set('.scene-stage', { y: 0 })
    return
  }

  startIntroAnimation()
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  ambientTween?.kill()
  transitionTimeline?.kill()
  introTimeline?.kill()
})
</script>

<template>
  <div ref="root" class="login">
    <!-- 场景背景：网格 / 地面 / 作品卡 / 圆环，开场结束后继续保留 -->
    <div class="scene-bg" aria-hidden="true">
      <div class="intro-grid" />
      <div class="intro-floor" />

      <article class="intro-work intro-work--left">
        <header><span>01 / DISCOVERY</span><span>PDE / FLOW</span></header>
        <div class="work-visual work-visual--left"><i /><i /><i /></div>
        <footer><strong>需求定义</strong><span>目标 · 范围 · 验收</span></footer>
      </article>
      <article class="intro-work intro-work--right">
        <header><span>04 / DELIVERY</span><span>BUILD / QA</span></header>
        <div class="work-visual work-visual--right"><i /><i /><i /></div>
        <footer><strong>稳定交付</strong><span>构建 · 测试 · 部署</span></footer>
      </article>

      <div class="intro-composition">
        <svg class="intro-orbits" viewBox="0 0 1000 1000" fill="none">
          <defs>
            <radialGradient id="intro-glow">
              <stop stop-color="#cbd276" stop-opacity=".14" />
              <stop offset="1" stop-color="#cbd276" stop-opacity="0" />
            </radialGradient>
            <linearGradient id="intro-ring-light" x1="130" y1="190" x2="850" y2="760" gradientUnits="userSpaceOnUse">
              <stop stop-color="#cbd276" stop-opacity=".08" />
              <stop offset=".48" stop-color="#e8ecd1" stop-opacity=".76" />
              <stop offset="1" stop-color="#cbd276" stop-opacity=".08" />
            </linearGradient>
            <linearGradient id="intro-metal" x1="120" y1="220" x2="880" y2="760" gradientUnits="userSpaceOnUse">
              <stop stop-color="#171a17" />
              <stop offset=".2" stop-color="#676a60" />
              <stop offset=".34" stop-color="#292c28" />
              <stop offset=".52" stop-color="#c1c3b6" />
              <stop offset=".64" stop-color="#41443e" />
              <stop offset=".82" stop-color="#858878" />
              <stop offset="1" stop-color="#171a17" />
            </linearGradient>
            <linearGradient id="intro-chroma" x1="75" y1="210" x2="920" y2="790" gradientUnits="userSpaceOnUse">
              <stop stop-color="#bd796d" stop-opacity=".22" />
              <stop offset=".24" stop-color="#a59bc2" stop-opacity=".34" />
              <stop offset=".49" stop-color="#d2c99d" stop-opacity=".48" />
              <stop offset=".72" stop-color="#8fa9a0" stop-opacity=".32" />
              <stop offset="1" stop-color="#bd796d" stop-opacity=".22" />
            </linearGradient>
          </defs>
          <circle cx="500" cy="500" r="430" fill="url(#intro-glow)" />
          <circle class="intro-ring intro-ring--chromatic" cx="500" cy="500" r="451" stroke="url(#intro-chroma)" stroke-width="6" />
          <circle class="intro-ring intro-ring--outer" cx="500" cy="500" r="424" stroke="url(#intro-metal)" stroke-width="52" />
          <circle class="intro-ring intro-ring--chromatic" cx="500" cy="500" r="397" stroke="url(#intro-chroma)" stroke-width="3" />
          <circle class="intro-ring intro-ring--inner" cx="500" cy="500" r="399" stroke="url(#intro-ring-light)" stroke-opacity=".68" />
          <circle class="intro-ring intro-ring--inner" cx="500" cy="500" r="385" stroke="#edf0e4" stroke-opacity=".16" />
          <circle class="intro-ring intro-ring--inner" cx="500" cy="500" r="374" stroke="#cbd276" stroke-opacity=".28" stroke-dasharray="1 10" />
          <g class="intro-orbit intro-orbit--slow">
            <ellipse cx="500" cy="500" rx="452" ry="320" transform="rotate(-32 500 500)" stroke="#edf0e4" stroke-opacity=".24" />
            <circle cx="886" cy="252" r="4" fill="#cbd276" />
          </g>
          <g class="intro-orbit">
            <ellipse cx="500" cy="500" rx="443" ry="295" transform="rotate(28 500 500)" stroke="#cbd276" stroke-opacity=".28" stroke-dasharray="2 13" />
            <circle cx="166" cy="283" r="2.5" fill="#edf0e4" />
          </g>
        </svg>
        <div class="intro-core"><span>DF</span><i /></div>
      </div>
    </div>

    <header class="scene-header">
      <div class="intro-brand" aria-label="DevFlow">
        <span class="intro-brand-mark">D</span>
        <span>DEVFLOW<small>PRODUCTION / DELIVERY</small></span>
      </div>
      <div class="intro-actions">
        <button v-if="prefersReducedMotion" class="intro-play" type="button" @click="playIntroAnimation">播放开场 <span aria-hidden="true">↗</span></button>
        <button v-show="introPhase" ref="introSkipButton" class="intro-skip" type="button" @click="skipIntro">跳过动画 <span aria-hidden="true">↗</span></button>
      </div>
    </header>

    <main class="scene-stage">
      <section class="intro-copy">
        <p class="intro-kicker">PRODUCTION / DELIVERY / LIVE</p>
        <h2 id="intro-title">从想法，<span>到交付。</span></h2>
        <p class="intro-caption">让每一步，都清晰向前。</p>
      </section>

      <form
        class="login-card"
        :inert="introPhase || undefined"
        :aria-hidden="introPhase ? 'true' : undefined"
        @submit.prevent="submit"
      >
        <header class="card-top"><p>01 / IDENTITY</p><span aria-hidden="true">↗</span></header>
        <label>账号<el-input v-model="form.username" placeholder="输入账号" size="large" autocomplete="username" /></label>
        <label>密码<el-input v-model="form.password" type="password" placeholder="输入密码" size="large" show-password autocomplete="current-password" /></label>
        <button type="submit" class="btn" :disabled="loading">{{ loading ? '验证中…' : '进入系统 ↗' }}</button>
        <footer><span>DEVFLOW</span><span>SECURE ENTRY</span></footer>
      </form>
    </main>

    <footer class="scene-footer"><span>DEVFLOW / 01</span><span>研发交付平台</span></footer>
  </div>
</template>

<style scoped>
/* ---------- 场景骨架：同一页面从开场到登录，圆环始终留在背景 ---------- */
.login {
  position: relative;
  display: grid;
  grid-template-rows: auto 1fr auto;
  min-height: 100svh;
  overflow: hidden;
  background: var(--bg-deep);
  isolation: isolate;
}
.scene-bg { position: absolute; z-index: 0; inset: 0; overflow: hidden; pointer-events: none; }
.scene-header {
  position: relative; z-index: 4;
  display: flex; align-items: center; justify-content: space-between; gap: 16px;
  padding: clamp(22px, 4vh, 38px) clamp(22px, 4.4vw, 64px);
}
.scene-stage {
  position: relative; z-index: 3;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: clamp(20px, 3.4vh, 40px);
  width: 100%;
  padding: 0 clamp(22px, 4.4vw, 64px);
}
.scene-footer {
  position: relative; z-index: 4;
  display: flex; align-items: center; justify-content: space-between; gap: 16px;
  padding: 0 clamp(22px, 4.4vw, 64px) clamp(22px, 4vh, 38px);
  color: var(--muted-light); font-size: 9px; letter-spacing: .16em;
}

/* ---------- 登录卡片 ---------- */
.login-card {
  display: flex; flex-direction: column; gap: 18px;
  width: min(100%, 430px);
  padding: clamp(24px, 4vw, 42px);
  background: rgba(12, 15, 14, .9);
  border: 1px solid var(--border-strong);
  border-radius: 12px;
  box-shadow: 0 32px 90px rgba(0, 0, 0, .42);
  backdrop-filter: blur(20px);
}
.card-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.card-top p { color: var(--accent); font-size: 10px; letter-spacing: .2em; }
.card-top span { color: var(--muted-light); font-size: 18px; }
.login-card label { display: flex; flex-direction: column; gap: 7px; color: var(--muted); font-size: 11px; letter-spacing: .08em; }
.login-card :deep(.el-input__inner:autofill),
.login-card :deep(.el-input__inner:-webkit-autofill),
.login-card :deep(.el-input__inner:-webkit-autofill:hover),
.login-card :deep(.el-input__inner:-webkit-autofill:focus),
.login-card :deep(.el-input__inner:-webkit-autofill:active) {
  -webkit-text-fill-color: var(--text) !important;
  caret-color: var(--text);
  -webkit-box-shadow: 0 0 0 1000px var(--surface) inset;
  box-shadow: 0 0 0 1000px var(--surface) inset;
}
.btn { margin-top: 8px; padding: 14px 16px; color: #11140f; background: var(--accent); border: 1px solid var(--accent); border-radius: var(--radius); cursor: pointer; font-size: 13px; letter-spacing: .08em; transition: background .25s var(--ease), transform .25s var(--ease), box-shadow .25s var(--ease); }
.btn:hover:not(:disabled) { background: #fff; box-shadow: 0 10px 26px rgba(239, 239, 235, .16); transform: translateY(-2px); }
.btn:disabled { cursor: not-allowed; opacity: .55; }
.login-card footer { display: flex; justify-content: space-between; margin-top: 10px; color: var(--muted-light); font-size: 9px; letter-spacing: .16em; }

/* ---------- 场景头部 ---------- */
.intro-actions { display: flex; align-items: center; gap: clamp(16px, 2vw, 28px); }
.intro-brand { display: flex; align-items: center; gap: 12px; color: var(--text); font-size: 11px; font-weight: 600; letter-spacing: .14em; }
.intro-brand-mark { display: grid; width: 34px; height: 34px; place-items: center; border: 1px solid rgba(203, 210, 118, .68); color: var(--accent); font-family: var(--font-display); font-size: 19px; font-weight: 400; }
.intro-brand small { display: block; margin-top: 3px; color: var(--muted-light); font-size: 7px; font-weight: 400; letter-spacing: .16em; }
.intro-skip { display: inline-flex; align-items: center; gap: 12px; padding: 10px 0 10px 12px; color: var(--muted); background: transparent; border: 0; cursor: pointer; font-size: 11px; letter-spacing: .08em; transition: color .2s ease; }
.intro-skip:hover, .intro-skip:focus-visible { color: var(--text); }
.intro-skip:focus-visible { outline: 1px solid var(--accent); outline-offset: 4px; }
.intro-skip span { color: var(--accent); font-size: 15px; }
.intro-play { display: inline-flex; align-items: center; gap: 10px; padding: 10px 0; color: var(--accent); background: transparent; border: 0; cursor: pointer; font-size: 11px; letter-spacing: .08em; transition: color .2s ease; }
.intro-play:hover, .intro-play:focus-visible { color: var(--text); }
.intro-play:focus-visible { outline: 1px solid var(--accent); outline-offset: 4px; }
.intro-play span { font-size: 15px; }

/* ---------- 背景元素 ---------- */
.intro-grid { position: absolute; z-index: 0; inset: 0; opacity: .2; background-image: linear-gradient(rgba(234, 238, 222, .05) 1px, transparent 1px), linear-gradient(90deg, rgba(234, 238, 222, .05) 1px, transparent 1px); background-size: 72px 72px; mask-image: radial-gradient(ellipse at center, black, transparent 75%); }
.intro-floor { position: absolute; z-index: 0; right: -22%; bottom: -34%; left: -22%; height: 82%; opacity: .27; transform: perspective(780px) rotateX(64deg); transform-origin: center top; background-image: linear-gradient(rgba(234, 238, 222, .12) 1px, transparent 1px), linear-gradient(90deg, rgba(234, 238, 222, .12) 1px, transparent 1px); background-size: 58px 58px; mask-image: linear-gradient(to bottom, transparent, black 24%, black 78%, transparent); }
.intro-work { position: absolute; z-index: 1; top: 52%; display: flex; width: clamp(300px, 36vw, 520px); height: clamp(250px, 42vh, 390px); flex-direction: column; justify-content: space-between; padding: clamp(18px, 2vw, 28px); border: 1px solid rgba(237, 240, 228, .14); border-radius: 14px; color: var(--text); background: linear-gradient(132deg, rgba(37, 40, 37, .88), rgba(13, 15, 14, .92) 70%); box-shadow: 0 28px 80px rgba(0, 0, 0, .3); opacity: .74; transform-origin: center; }
.intro-work--left { left: -5vw; transform: translateY(-50%) perspective(1200px) rotateY(17deg) rotateZ(-1.5deg); }
.intro-work--right { right: -5vw; transform: translateY(-50%) perspective(1200px) rotateY(-17deg) rotateZ(1.5deg); }
.intro-work > header, .intro-work > footer { position: relative; z-index: 1; display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.intro-work--left > header { justify-content: flex-end; padding-right: 94px; }
.intro-work--left > footer { flex-direction: column; align-items: flex-end; justify-content: flex-end; padding-right: 94px; text-align: right; }
.intro-work--right > header { justify-content: flex-start; padding-left: 94px; }
.intro-work--right > footer { flex-direction: column; align-items: flex-start; justify-content: flex-end; padding-left: 94px; text-align: left; }
.intro-work > header { color: var(--muted-light); font-size: 8px; letter-spacing: .18em; }
.intro-work > footer strong { font-family: var(--font-display); font-size: clamp(20px, 2.2vw, 30px); font-weight: 400; letter-spacing: -.04em; }
.intro-work > footer span { color: var(--muted); font-size: 9px; letter-spacing: .08em; }
.work-visual { position: absolute; inset: 54px 20px 58px; overflow: hidden; border: 1px solid rgba(237, 240, 228, .1); background: linear-gradient(140deg, rgba(90, 94, 84, .28), rgba(9, 11, 10, .32)); }
.work-visual::before, .work-visual::after { position: absolute; border: 1px solid rgba(237, 240, 228, .2); border-radius: 50%; content: ""; }
.work-visual::before { top: 10%; right: 8%; width: 54%; aspect-ratio: 1; box-shadow: 0 0 0 14px rgba(237, 240, 228, .025), inset 0 0 42px rgba(203, 210, 118, .08); }
.work-visual::after { right: 19%; bottom: 7%; width: 66%; height: 22%; border-radius: 50%; transform: rotate(-14deg); }
.work-visual--left { background: linear-gradient(135deg, rgba(73, 78, 69, .36), rgba(17, 19, 17, .72)); }
.work-visual--right { background: linear-gradient(135deg, rgba(73, 77, 70, .24), rgba(21, 23, 21, .76)); }
.work-visual i { position: absolute; z-index: 1; left: 9%; width: 28%; height: 1px; background: rgba(237, 240, 228, .22); }
.work-visual i:first-child { top: 26%; }
.work-visual i:nth-child(2) { top: 33%; width: 19%; }
.work-visual i:last-child { top: 40%; width: 24%; }
.intro-composition { position: absolute; z-index: 2; top: 48%; left: 50%; width: min(92vmin, 1040px); aspect-ratio: 1; transform: translate(-50%, -50%); }
.intro-orbits { display: block; width: 100%; height: 100%; overflow: visible; will-change: transform; }
.intro-ring, .intro-orbit { vector-effect: non-scaling-stroke; }
.intro-core { position: absolute; top: 23%; left: 50%; display: grid; width: clamp(48px, 7vmin, 72px); aspect-ratio: 1; place-items: center; border: 1px solid rgba(237, 240, 228, .36); border-radius: 50%; color: var(--text); background: radial-gradient(circle at 32% 28%, rgba(203, 210, 118, .19), rgba(18, 21, 18, .94) 70%); box-shadow: 0 0 45px rgba(203, 210, 118, .09), inset 0 0 24px rgba(203, 210, 118, .06); transform: translate(-50%, -50%); }
.intro-core span { font-family: var(--font-display); font-size: clamp(26px, 4vmin, 40px); letter-spacing: -.08em; }
.intro-core i { position: absolute; right: 12%; bottom: 16%; width: 5px; height: 5px; border-radius: 50%; background: var(--accent); box-shadow: 0 0 12px var(--accent); }

/* ---------- 场景标题（开场与登录态共用） ---------- */
.intro-copy { position: relative; z-index: 3; width: min(88vw, 620px); text-align: center; }
.intro-kicker { color: var(--accent); font-size: 9px; font-weight: 600; letter-spacing: .24em; }
.intro-copy h2 { margin-top: 16px; color: var(--text); font-family: var(--font-display); font-size: clamp(30px, 4.4vw, 56px); font-weight: 400; letter-spacing: -.045em; line-height: 1.18; }
.intro-copy h2 span { color: var(--accent); }
.intro-caption { margin-top: 12px; color: var(--muted); font-size: 12px; letter-spacing: .14em; }

@media (max-width: 1000px) { .intro-work { display: none; } }
@media (max-width: 700px) {
  .login { grid-template-rows: auto 1fr auto; }
  .scene-stage { gap: 20px; padding: 0 20px; }
  .intro-floor { opacity: .14; background-size: 46px 46px; }
  .intro-composition { top: 45%; width: min(112vw, 680px); }
  .intro-copy h2 { margin-top: 12px; font-size: clamp(28px, 8.4vw, 42px); }
  .intro-caption { margin-top: 8px; font-size: 11px; }
  .intro-grid { background-size: 48px 48px; }
  .login-card { padding: 24px 22px; gap: 15px; }
}
@media (max-height: 780px) and (min-width: 701px) {
  .scene-stage { gap: 18px; }
  .intro-copy h2 { margin-top: 12px; font-size: clamp(26px, 3.4vw, 42px); }
  .intro-caption { margin-top: 8px; }
  .login-card { padding: 26px 30px; gap: 14px; }
}
@media (max-height: 620px) and (min-width: 701px) {
  .scene-header { padding-top: 16px; }
  .scene-footer { padding-bottom: 16px; }
  .intro-composition { width: min(72vmin, 620px); }
  .login-card { padding: 20px 24px; gap: 12px; }
  .intro-copy h2 { font-size: clamp(24px, 3vw, 34px); }
}
@media (max-width: 360px) {
  .intro-brand { gap: 8px; font-size: 10px; letter-spacing: .1em; }
  .intro-brand small { display: none; }
  .intro-actions { gap: 8px; }
  .intro-play, .intro-skip { gap: 6px; font-size: 10px; letter-spacing: 0; white-space: nowrap; }
}
@media (prefers-reduced-motion: reduce) {
  .intro-skip, .intro-play, .btn { transition: none; }
}
</style>
