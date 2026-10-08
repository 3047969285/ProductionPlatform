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
const introVisible = ref(true)
const loginReady = ref(false)
const focusLoginAfterIntro = ref(false)
const loading = ref(false)
const form = ref({ username: '', password: '' })
let introContext
let revealContext
let introTimeline
let revealTween
let reducedMotion = false
const prefersReducedMotion = ref(false)

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

function showLogin() {
  if (loginReady.value) {
    if (focusLoginAfterIntro.value) {
      nextTick(() => {
        if (revealTween?.isActive()) revealTween.eventCallback('onComplete', focusLoginInputIfRequested)
        else focusLoginInputIfRequested()
      })
    }
    return
  }
  loginReady.value = true
  if (reducedMotion) {
    nextTick(focusLoginInputIfRequested)
    return
  }
  nextTick(() => {
    if (!root.value) return
    revealContext = gsap.context(() => {
      revealTween = gsap.fromTo('.login-intro, .login-card',
        { autoAlpha: 0, y: 14 },
        { autoAlpha: 1, y: 0, duration: 0.72, stagger: 0.12, ease: 'power3.out', clearProps: 'all', onComplete: focusLoginInputIfRequested },
      )
    }, root.value)
  })
}

function focusLoginInputIfRequested() {
  if (!focusLoginAfterIntro.value) return
  focusLoginAfterIntro.value = false
  root.value?.querySelector('input[autocomplete="username"]')?.focus()
}

function focusLoginWhenReady() {
  focusLoginAfterIntro.value = true
  nextTick(() => {
    if (revealTween?.isActive()) revealTween.eventCallback('onComplete', focusLoginInputIfRequested)
    else focusLoginInputIfRequested()
  })
}

function skipIntro() {
  if (introTimeline) {
    introTimeline.progress(1)
    return
  }
  showLogin()
  introVisible.value = false
  focusLoginWhenReady()
}

function handleIntroKeydown(event) {
  if (!introVisible.value) return
  if (event.key === 'Escape') {
    skipIntro()
    return
  }
  if (event.key !== 'Tab') return

  const dialog = root.value?.querySelector('.intro-screen')
  const controls = dialog?.querySelectorAll('button:not(:disabled)')
  if (!controls?.length) return
  const first = controls[0]
  const last = controls[controls.length - 1]

  if (!dialog.contains(document.activeElement)) {
    event.preventDefault()
    first.focus()
  } else if (event.shiftKey && document.activeElement === first) {
    event.preventDefault()
    last.focus()
  } else if (!event.shiftKey && document.activeElement === last) {
    event.preventDefault()
    first.focus()
  }
}

function startIntroAnimation() {
  if (!root.value || introTimeline) return
  introVisible.value = true
  introContext = gsap.context(() => {
    gsap.set('.intro-ring, .intro-orbit, .intro-core, .intro-copy > *', { autoAlpha: 0 })
    gsap.set('.intro-work', { autoAlpha: 0, scale: 0.96 })
    gsap.set('.intro-ring--outer', { scale: 0.82, transformOrigin: '50% 50%' })
    gsap.set('.intro-ring--inner', { scale: 1.12, transformOrigin: '50% 50%' })
    gsap.set('.intro-core', { scale: 0.82, y: 8, transformOrigin: '50% 50%' })
    gsap.set('.intro-copy > *', { y: 12 })

    introTimeline = gsap.timeline({
      defaults: { ease: 'power3.out' },
      onComplete: () => {
        introVisible.value = false
        introContext?.revert()
        focusLoginWhenReady()
      },
    })
      .to('.intro-ring--outer', { autoAlpha: 0.64, scale: 1, duration: 1.05 }, 0)
      .to('.intro-ring--inner', { autoAlpha: 1, scale: 1, duration: 1.18 }, 0.08)
      .to('.intro-ring--chromatic', { autoAlpha: 0.72, duration: 0.9 }, 0.12)
      .to('.intro-orbit', { autoAlpha: 0.82, duration: 0.75 }, 0.28)
      .to('.intro-work--left, .intro-work--right', { autoAlpha: 0.74, scale: 1, duration: 0.95, stagger: 0.08 }, 0.16)
      .to('.intro-core', { autoAlpha: 1, scale: 1, y: 0, duration: 0.7 }, 0.42)
      .to('.intro-copy > *', { autoAlpha: 1, y: 0, duration: 0.64, stagger: 0.12 }, 0.64)
      .to('.intro-orbit--slow', { rotation: 22, duration: 2.35, ease: 'none', transformOrigin: '50% 50%' }, 0.2)
      .call(showLogin, [], 1.72)
      .to('.intro-screen', { autoAlpha: 0, duration: 0.56, ease: 'power2.inOut' }, 1.94)
  }, root.value)
}

function playIntroAnimation() {
  prefersReducedMotion.value = false
  reducedMotion = false
  startIntroAnimation()
}

onMounted(() => {
  window.addEventListener('keydown', handleIntroKeydown)
  if (!root.value) return
  nextTick(() => introSkipButton.value?.focus({ preventScroll: true }))

  reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  prefersReducedMotion.value = reducedMotion
  if (reducedMotion) {
    showLogin()
    return
  }

  startIntroAnimation()
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleIntroKeydown)
  introContext?.revert()
  revealContext?.revert()
})
</script>

<template>
  <div ref="root" class="login">
    <main v-if="loginReady" class="login-content" :inert="introVisible" :aria-hidden="introVisible ? 'true' : undefined">
      <div class="login-grid" aria-hidden="true">
        <span class="login-orbit" />
        <span class="login-beam" />
      </div>
      <section class="login-intro">
        <p class="login-index">00 / ACCESS</p>
        <h1>DevFlow<span>.</span></h1>
        <p>研发交付系统</p>
      </section>
      <form class="login-card" @submit.prevent="submit">
        <header class="card-top"><p>01 / IDENTITY</p><span aria-hidden="true">↗</span></header>
        <label>账号<el-input v-model="form.username" placeholder="输入账号" size="large" autocomplete="username" /></label>
        <label>密码<el-input v-model="form.password" type="password" placeholder="输入密码" size="large" show-password autocomplete="current-password" /></label>
        <button type="submit" class="btn" :disabled="loading">{{ loading ? '验证中…' : '进入系统 ↗' }}</button>
        <footer><span>DEVFLOW</span><span>SECURE ENTRY</span></footer>
      </form>
    </main>

    <section v-if="introVisible" class="intro-screen" role="dialog" aria-modal="true" aria-labelledby="intro-title">
      <header class="intro-header">
        <div class="intro-brand" aria-label="DevFlow">
          <span class="intro-brand-mark">D</span>
          <span>DEVFLOW<small>PRODUCTION / DELIVERY</small></span>
        </div>
        <div class="intro-actions">
          <button v-if="prefersReducedMotion" class="intro-play" type="button" @click="playIntroAnimation">播放开场 <span aria-hidden="true">↗</span></button>
          <button ref="introSkipButton" class="intro-skip" type="button" @click="skipIntro">{{ prefersReducedMotion ? '进入登录' : '跳过动画' }} <span aria-hidden="true">↗</span></button>
        </div>
      </header>

      <div class="intro-grid" aria-hidden="true" />
      <div class="intro-floor" aria-hidden="true" />
      <article class="intro-work intro-work--left" aria-hidden="true">
        <header><span>01 / DISCOVERY</span><span>PDE / FLOW</span></header>
        <div class="work-visual work-visual--left"><i /><i /><i /></div>
        <footer><strong>需求定义</strong><span>目标 · 范围 · 验收</span></footer>
      </article>
      <article class="intro-work intro-work--right" aria-hidden="true">
        <header><span>04 / DELIVERY</span><span>BUILD / QA</span></header>
        <div class="work-visual work-visual--right"><i /><i /><i /></div>
        <footer><strong>稳定交付</strong><span>构建 · 测试 · 部署</span></footer>
      </article>
      <div class="intro-composition" aria-hidden="true">
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

      <div class="intro-copy">
        <p class="intro-kicker">PRODUCTION / DELIVERY / LIVE</p>
        <h2 id="intro-title">从想法，<span>到交付。</span></h2>
        <p class="intro-caption">让每一步，都清晰向前。</p>
      </div>

      <footer class="intro-footer"><span>DEVFLOW / 01</span><span>研发交付平台</span></footer>
    </section>
  </div>
</template>

<style scoped>
.login { position: relative; min-height: 100svh; overflow: hidden; background: var(--bg-deep); }
.login-content {
  position: relative; z-index: 1; display: grid; grid-template-columns: minmax(220px, .8fr) minmax(320px, 430px); align-items: center;
  gap: clamp(48px, 10vw, 160px); width: min(100%, 1320px); min-height: 100svh; margin: 0 auto; padding: 80px clamp(24px, 6vw, 84px);
}
.login-grid { position: absolute; inset: 0; z-index: -1; overflow: hidden; pointer-events: none; }
.login-grid::before { position: absolute; top: -18vw; right: -7vw; width: 58vw; height: 58vw; max-width: 760px; max-height: 760px; border: 1px solid rgba(203,210,118,.14); border-radius: 50%; content: ""; box-shadow: 0 0 0 62px rgba(203,210,118,.025), 0 0 0 124px rgba(203,210,118,.018); }
.login-orbit { position: absolute; top: 12vh; right: 15vw; width: 10px; height: 10px; border: 1px solid var(--accent); border-radius: 50%; box-shadow: 0 0 0 5px rgba(203,210,118,.12); }
.login-beam { position: absolute; top: 34%; left: -20%; width: 40%; height: 1px; background: linear-gradient(90deg, transparent, var(--accent), transparent); opacity: .35; }
.login-intro { align-self: center; }
.login-index, .card-top p { color: var(--accent); font-size: 10px; letter-spacing: .2em; }
.login-index { margin-bottom: 28px; }
.login-intro h1 { font-family: var(--font-display); font-size: clamp(3.8rem, 8vw, 7.5rem); font-weight: 400; letter-spacing: -.08em; line-height: .86; }
.login-intro h1 span, .intro-copy h2 span { color: var(--accent); }
.login-intro > p:last-child { margin-top: 28px; color: var(--muted); font-size: 13px; letter-spacing: .18em; }
.login-card { display: flex; flex-direction: column; gap: 18px; padding: clamp(24px, 4vw, 42px); background: rgba(12,15,14,.9); border: 1px solid var(--border-strong); border-radius: 12px; box-shadow: 0 32px 90px rgba(0,0,0,.42); backdrop-filter: blur(20px); }
.card-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.card-top span { color: var(--muted-light); font-size: 18px; }
.login-card label { display: flex; flex-direction: column; gap: 7px; color: var(--muted); font-size: 11px; letter-spacing: .08em; }
.btn { margin-top: 8px; padding: 14px 16px; color: #11140f; background: var(--accent); border: 1px solid var(--accent); border-radius: var(--radius); cursor: pointer; font-size: 13px; letter-spacing: .08em; transition: background .25s var(--ease), transform .25s var(--ease), box-shadow .25s var(--ease); }
.btn:hover:not(:disabled) { background: #fff; box-shadow: 0 10px 26px rgba(239,239,235,.16); transform: translateY(-2px); }
.btn:disabled { cursor: not-allowed; opacity: .55; }
.login-card footer { display: flex; justify-content: space-between; margin-top: 10px; color: var(--muted-light); font-size: 9px; letter-spacing: .16em; }

.intro-screen { position: fixed; z-index: 5; inset: 0; overflow: hidden; isolation: isolate; background: radial-gradient(ellipse at 50% 48%, rgba(25,29,25,.9), transparent 50%), var(--bg-deep); }
.intro-screen::before { position: absolute; z-index: -1; inset: 0; background: radial-gradient(ellipse at 50% 50%, transparent 25%, rgba(0,0,0,.52) 100%); content: ""; }
.intro-header, .intro-footer { position: absolute; z-index: 2; right: clamp(22px, 4.4vw, 64px); left: clamp(22px, 4.4vw, 64px); display: flex; align-items: center; justify-content: space-between; }
.intro-header { top: clamp(22px, 4vh, 38px); }
.intro-actions { display: flex; align-items: center; gap: clamp(16px, 2vw, 28px); }
.intro-brand { display: flex; align-items: center; gap: 12px; color: var(--text); font-size: 11px; font-weight: 600; letter-spacing: .14em; }
.intro-brand-mark { display: grid; width: 34px; height: 34px; place-items: center; border: 1px solid rgba(203,210,118,.68); color: var(--accent); font-family: var(--font-display); font-size: 19px; font-weight: 400; }
.intro-brand small { display: block; margin-top: 3px; color: var(--muted-light); font-size: 7px; font-weight: 400; letter-spacing: .16em; }
.intro-skip { display: inline-flex; align-items: center; gap: 12px; padding: 10px 0 10px 12px; color: var(--muted); background: transparent; border: 0; cursor: pointer; font-size: 11px; letter-spacing: .08em; transition: color .2s ease; }
.intro-skip:hover, .intro-skip:focus-visible { color: var(--text); }
.intro-skip:focus-visible { outline: 1px solid var(--accent); outline-offset: 4px; }
.intro-skip span { color: var(--accent); font-size: 15px; }
.intro-play { display: inline-flex; align-items: center; gap: 10px; padding: 10px 0; color: var(--accent); background: transparent; border: 0; cursor: pointer; font-size: 11px; letter-spacing: .08em; transition: color .2s ease; }
.intro-play:hover, .intro-play:focus-visible { color: var(--text); }
.intro-play:focus-visible { outline: 1px solid var(--accent); outline-offset: 4px; }
.intro-play span { font-size: 15px; }
.intro-grid { position: absolute; z-index: -1; inset: 0; opacity: .2; background-image: linear-gradient(rgba(234,238,222,.05) 1px, transparent 1px), linear-gradient(90deg, rgba(234,238,222,.05) 1px, transparent 1px); background-size: 72px 72px; mask-image: radial-gradient(ellipse at center, black, transparent 75%); }
.intro-floor { position: absolute; z-index: 0; right: -22%; bottom: -34%; left: -22%; height: 82%; opacity: .27; transform: perspective(780px) rotateX(64deg); transform-origin: center top; background-image: linear-gradient(rgba(234,238,222,.12) 1px, transparent 1px), linear-gradient(90deg, rgba(234,238,222,.12) 1px, transparent 1px); background-size: 58px 58px; mask-image: linear-gradient(to bottom, transparent, black 24%, black 78%, transparent); }
.intro-work { position: absolute; z-index: 1; top: 52%; display: flex; width: clamp(300px, 36vw, 520px); height: clamp(250px, 42vh, 390px); flex-direction: column; justify-content: space-between; padding: clamp(18px, 2vw, 28px); border: 1px solid rgba(237,240,228,.14); border-radius: 14px; color: var(--text); background: linear-gradient(132deg, rgba(37,40,37,.88), rgba(13,15,14,.92) 70%); box-shadow: 0 28px 80px rgba(0,0,0,.3); opacity: .74; transform-origin: center; }
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
.work-visual { position: absolute; inset: 54px 20px 58px; overflow: hidden; border: 1px solid rgba(237,240,228,.1); background: linear-gradient(140deg, rgba(90,94,84,.28), rgba(9,11,10,.32)); }
.work-visual::before, .work-visual::after { position: absolute; border: 1px solid rgba(237,240,228,.2); border-radius: 50%; content: ""; }
.work-visual::before { top: 10%; right: 8%; width: 54%; aspect-ratio: 1; box-shadow: 0 0 0 14px rgba(237,240,228,.025), inset 0 0 42px rgba(203,210,118,.08); }
.work-visual::after { right: 19%; bottom: 7%; width: 66%; height: 22%; border-radius: 50%; transform: rotate(-14deg); }
.work-visual--left { background: linear-gradient(135deg, rgba(73,78,69,.36), rgba(17,19,17,.72)); }
.work-visual--right { background: linear-gradient(135deg, rgba(73,77,70,.24), rgba(21,23,21,.76)); }
.work-visual i { position: absolute; z-index: 1; left: 9%; width: 28%; height: 1px; background: rgba(237,240,228,.22); }
.work-visual i:first-child { top: 26%; }
.work-visual i:nth-child(2) { top: 33%; width: 19%; }
.work-visual i:last-child { top: 40%; width: 24%; }
.intro-composition { position: absolute; z-index: 2; top: 48%; left: 50%; width: min(92vmin, 1040px); aspect-ratio: 1; transform: translate(-50%, -50%); }
.intro-orbits { display: block; width: 100%; height: 100%; overflow: visible; }
.intro-ring, .intro-orbit { vector-effect: non-scaling-stroke; }
.intro-core { position: absolute; top: 23%; left: 50%; display: grid; width: clamp(48px, 7vmin, 72px); aspect-ratio: 1; place-items: center; border: 1px solid rgba(237,240,228,.36); border-radius: 50%; color: var(--text); background: radial-gradient(circle at 32% 28%, rgba(203,210,118,.19), rgba(18,21,18,.94) 70%); box-shadow: 0 0 45px rgba(203,210,118,.09), inset 0 0 24px rgba(203,210,118,.06); transform: translate(-50%, -50%); }
.intro-core span { font-family: var(--font-display); font-size: clamp(26px, 4vmin, 40px); letter-spacing: -.08em; }
.intro-core i { position: absolute; right: 12%; bottom: 16%; width: 5px; height: 5px; border-radius: 50%; background: var(--accent); box-shadow: 0 0 12px var(--accent); }
.intro-copy { position: absolute; z-index: 3; top: 48%; left: 50%; width: min(88vw, 620px); text-align: center; transform: translate(-50%, -50%); }
.intro-kicker { color: var(--accent); font-size: 9px; font-weight: 600; letter-spacing: .24em; }
.intro-copy h2 { margin-top: 20px; color: var(--text); font-family: var(--font-display); font-size: clamp(34px, 5vw, 64px); font-weight: 400; letter-spacing: -.045em; line-height: 1.2; }
.intro-caption { margin-top: 16px; color: var(--muted); font-size: 12px; letter-spacing: .14em; }
.intro-footer { bottom: clamp(22px, 4vh, 38px); color: var(--muted-light); font-size: 9px; letter-spacing: .16em; }

@media (max-width: 700px) {
  .login-content { grid-template-columns: 1fr; gap: 42px; align-content: center; padding-top: 40px; padding-bottom: 40px; }
  .login-intro h1 { font-size: clamp(3.4rem, 17vw, 5rem); }
  .login-index { margin-bottom: 18px; }
  .login-intro > p:last-child { margin-top: 17px; }
  .intro-work { display: none; }
  .intro-floor { opacity: .14; background-size: 46px 46px; }
  .intro-composition { top: 45%; width: min(112vw, 680px); }
  .intro-copy { top: 45%; }
  .intro-copy h2 { margin-top: 16px; font-size: clamp(32px, 9vw, 48px); }
  .intro-caption { margin-top: 12px; font-size: 11px; }
  .intro-grid { background-size: 48px 48px; }
}
@media (max-width: 360px) {
  .intro-brand { gap: 8px; font-size: 10px; letter-spacing: .1em; }
  .intro-brand small { display: none; }
  .intro-actions { gap: 8px; }
  .intro-play, .intro-skip { gap: 6px; font-size: 10px; letter-spacing: 0; white-space: nowrap; }
}
@media (max-width: 360px) and (max-height: 640px) {
  .login-content { gap: 30px; padding-top: 20px; padding-bottom: 20px; }
}
@media (max-width: 1000px) { .intro-work { display: none; } }
@media (max-height: 620px) and (min-width: 701px) {
  .intro-composition { width: min(72vmin, 620px); }
  .intro-copy h2 { font-size: clamp(32px, 5vh, 48px); }
}
@media (prefers-reduced-motion: reduce) {
  .intro-skip, .intro-play, .btn { transition: none; }
}
</style>
