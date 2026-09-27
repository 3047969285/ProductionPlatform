<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { gsap } from 'gsap'

const root = ref(null)
let animationContext
let mediaQuery

onMounted(() => {
  if (!root.value) return

  animationContext = gsap.context(() => {
    mediaQuery = gsap.matchMedia()
    mediaQuery.add({ reduced: '(prefers-reduced-motion: reduce)', desktop: '(min-width: 801px)' }, ({ conditions }) => {
      const reducedMotion = conditions.reduced
      const desktop = conditions.desktop
      const duration = reducedMotion ? 0 : undefined
      const intro = gsap.timeline({ defaults: { ease: 'power3.out' } })

      intro
        .from('.hero-copy > *', { autoAlpha: 0, y: 24, duration: duration ?? 0.65, stagger: 0.1 })
        .from('.hero-art', { autoAlpha: 0, scale: 0.88, duration: duration ?? 0.9, ease: 'power4.out' }, '-=0.55')
        .from('.hero-side-note, .hero-bottom', { autoAlpha: 0, duration: duration ?? 0.55 }, '-=0.45')

      if (reducedMotion) return

      gsap.to('.art-grid', { rotation: 360, duration: desktop ? 55 : 75, repeat: -1, ease: 'none' })
      gsap.to('.orbit-one', { rotation: '+=360', duration: desktop ? 24 : 32, repeat: -1, ease: 'none' })
      gsap.to('.orbit-two', { rotation: '+=360', duration: desktop ? 31 : 42, repeat: -1, ease: 'none' })
      gsap.to('.orbit-three', { rotation: '+=360', duration: desktop ? 38 : 50, repeat: -1, ease: 'none' })
      gsap.to('.art-core', { y: -8, rotation: '+=15', duration: 4.5, repeat: -1, yoyo: true, ease: 'sine.inOut' })

      gsap.timeline({
        scrollTrigger: { trigger: '.home-manifesto', start: 'top 82%', end: 'bottom 58%', scrub: 0.8 },
      })
        .from('.manifesto-copy', { autoAlpha: 0, y: 56 })
        .from('.manifesto-stamp', { autoAlpha: 0, scale: 0.7, rotation: -16 }, 0.12)

      gsap.timeline({
        scrollTrigger: { trigger: '.home-links', start: 'top 88%', once: true },
      }).from('.home-link-card', { autoAlpha: 0, y: 28, stagger: 0.12, duration: 0.65 })
    })
  }, root.value)
})

onUnmounted(() => {
  mediaQuery?.revert()
  animationContext?.revert()
})
</script>

<template>
  <main ref="root" class="chome">
    <section class="chome-hero">
      <div class="hero-copy">
        <p class="eyebrow"><span class="live-dot"></span> OPEN CALL · 2026</p>
        <h1>未完成的<br /><em>明天</em></h1>
        <p class="hero-line">未来不是被预测的。<br />它被做出来。</p>
        <div class="hero-actions">
          <RouterLink to="/contest/works" class="button-dark">观看作品 <span>↗</span></RouterLink>
          <RouterLink to="/contest/entry" class="text-link">成为创作者 <span>→</span></RouterLink>
        </div>
      </div>

      <div class="hero-art" aria-label="由光线与几何构成的抽象雕塑" role="img">
        <div class="art-grid"></div>
        <div class="orbit orbit-one"></div>
        <div class="orbit orbit-two"></div>
        <div class="orbit orbit-three"></div>
        <div class="art-core"></div>
        <span class="art-coordinate">31°13' N<br />121°28' E</span>
        <span class="art-index">FIG. 01 — BECOMING</span>
        <span class="art-caption">一切尚未定形</span>
      </div>
      <div class="hero-side-note">DESIGN<br />BEYOND<br />THE KNOWN <span>↘</span></div>
      <div class="hero-bottom"><span>01 / 03</span><span>向下探索</span><span class="scroll-line"></span></div>
    </section>

    <section class="home-manifesto">
      <div class="manifesto-index">命题 / 2026</div>
      <div class="manifesto-copy">
        <p>每一种未来，最初都只是一个<br /><span>不合时宜的念头。</span></p>
        <div class="manifesto-meta"><span>让想象离开纸面</span><span>01 — 03</span></div>
      </div>
      <div class="manifesto-stamp">MAKE<br />A NEW<br />POSSIBLE</div>
    </section>

    <section class="home-links">
      <RouterLink to="/contest/works" class="home-link-card">
        <span class="card-index">A / 作品集</span><span class="card-title">想法有了形状</span><span class="card-arrow">↗</span>
      </RouterLink>
      <RouterLink to="/contest/entry" class="home-link-card">
        <span class="card-index">B / 参赛指南</span><span class="card-title">下一件作品，等你</span><span class="card-arrow">↗</span>
      </RouterLink>
    </section>
  </main>
</template>

<style scoped>
.chome { overflow: hidden; }
.chome-hero { position: relative; display: grid; grid-template-columns: 1fr 1.04fr; align-items: center; min-height: min(750px, calc(100svh - 77px)); padding: 70px clamp(26px, 10.5vw, 170px) 92px clamp(26px, 12vw, 190px); background: #171918; color: #efeee9; }
.hero-copy { position: relative; z-index: 2; padding-bottom: 25px; }
.eyebrow { display: flex; align-items: center; gap: 9px; color: #a3a8a1; font-size: 9px; letter-spacing: .19em; }
.live-dot { width: 6px; height: 6px; border-radius: 50%; background: #cad276; box-shadow: 0 0 14px #cad276; }
h1 { margin: 34px 0 23px; font-family: Georgia, "Songti SC", "Noto Serif SC", serif; font-size: clamp(64px, 9.2vw, 132px); font-weight: 400; line-height: .91; letter-spacing: -.095em; }
h1 em { color: #cad276; font-weight: 400; }
.hero-line { color: #a8aaa3; font-family: Georgia, "Songti SC", "Noto Serif SC", serif; font-size: clamp(15px, 1.45vw, 19px); line-height: 1.9; letter-spacing: .015em; }
.hero-actions { display: flex; align-items: center; gap: 25px; margin-top: 38px; }
.button-dark { display: inline-flex; align-items: center; gap: 30px; padding: 14px 17px; background: #e8e7df; color: #171918; text-decoration: none; font-size: 11px; transition: background .2s ease; }
.button-dark:hover { background: #cad276; }
.button-dark span { font-size: 15px; }
.text-link { color: #c5c8c0; text-decoration: none; font-size: 11px; }
.text-link span { padding-left: 8px; }
.hero-art { position: relative; width: min(40vw, 500px); aspect-ratio: 1; justify-self: center; border: 1px solid rgba(231, 233, 218, .13); border-radius: 50%; background: radial-gradient(circle at 47% 49%, rgba(202,210,118,.12), transparent 46%); }
.hero-art::before, .hero-art::after { position: absolute; inset: 10%; border: 1px solid rgba(231, 233, 218, .12); border-radius: 50%; content: ""; }
.hero-art::after { inset: 23%; border-color: rgba(231, 233, 218, .18); }
.art-grid { position: absolute; inset: 0; border-radius: 50%; opacity: .25; background-image: linear-gradient(rgba(221,225,203,.18) 1px, transparent 1px), linear-gradient(90deg, rgba(221,225,203,.18) 1px, transparent 1px); background-size: 12.5% 12.5%; mask-image: radial-gradient(circle, transparent 33%, #000 34%, #000 70%, transparent 71%); }
.orbit { position: absolute; top: 50%; left: 50%; border: 1px solid rgba(219,224,184,.55); border-radius: 50%; transform: translate(-50%, -50%) rotate(-34deg); }
.orbit-one { width: 88%; height: 35%; }
.orbit-two { width: 67%; height: 91%; transform: translate(-50%, -50%) rotate(39deg); border-color: rgba(219,224,184,.25); }
.orbit-three { width: 45%; height: 82%; transform: translate(-50%, -50%) rotate(73deg); border-color: rgba(219,224,184,.34); }
.art-core { position: absolute; top: 50%; left: 50%; width: 23%; aspect-ratio: 1; border-radius: 49% 51% 63% 37% / 52% 40% 60% 48%; background: radial-gradient(circle at 30% 25%, #fbffd1 0 3%, #cbd27c 9%, #6e734b 38%, #252923 74%); box-shadow: 0 0 54px rgba(202,210,118,.18), inset -12px -18px 28px rgba(12,14,12,.55); transform: translate(-50%,-50%) rotate(23deg); will-change: transform; }
.art-coordinate, .art-index, .art-caption { position: absolute; color: rgba(232,231,223,.55); font-size: 8px; line-height: 1.6; letter-spacing: .1em; }
.art-coordinate { top: 15%; left: 11%; }.art-index { right: 8%; bottom: 19%; }.art-caption { top: 50%; left: -10%; transform: rotate(-90deg) translateX(-50%); transform-origin: top left; }
.hero-side-note { position: absolute; right: 3.8%; top: 43%; color: rgba(232,231,223,.35); font-size: 8px; line-height: 1.8; letter-spacing: .2em; }
.hero-side-note span { display: block; margin-top: 12px; font-size: 15px; }
.hero-bottom { position: absolute; right: clamp(26px, 5.3vw, 84px); bottom: 24px; left: clamp(26px, 5.3vw, 84px); display: flex; align-items: center; gap: 20px; color: rgba(232,231,223,.45); font-size: 8px; letter-spacing: .11em; }
.scroll-line { flex: 1; height: 1px; background: rgba(232,231,223,.2); }
.home-manifesto { display: grid; grid-template-columns: .55fr 1.6fr .35fr; align-items: start; gap: 26px; padding: 104px clamp(26px, 12vw, 190px) 112px; background: #e7e6df; }
.manifesto-index { padding-top: 11px; color: #777873; font-size: 9px; letter-spacing: .12em; }
.manifesto-copy p { font-family: Georgia, "Songti SC", "Noto Serif SC", serif; font-size: clamp(28px, 4.4vw, 58px); line-height: 1.47; letter-spacing: -.065em; }
.manifesto-copy p span { color: #858b58; }
.manifesto-meta { display: flex; justify-content: space-between; margin-top: 48px; padding-top: 12px; border-top: 1px solid rgba(25,26,26,.2); color: #777873; font-size: 9px; letter-spacing: .1em; }
.manifesto-stamp { justify-self: end; margin-top: 8px; padding: 13px 11px; border: 1px solid #a1a28f; border-radius: 50%; color: #80826c; font-family: Georgia, serif; font-size: 8px; line-height: 1.5; text-align: center; transform: rotate(11deg); }
.home-links { display: grid; grid-template-columns: 1fr 1fr; padding: 0 clamp(26px, 12vw, 190px) 90px; background: #e7e6df; }
.home-link-card { position: relative; display: flex; flex-direction: column; gap: 15px; min-height: 158px; padding: 25px 25px 22px 0; border-top: 1px solid rgba(25,26,26,.24); color: inherit; text-decoration: none; }
.home-link-card + .home-link-card { padding-left: 25px; border-left: 1px solid rgba(25,26,26,.16); }
.card-index { color: #777873; font-size: 9px; letter-spacing: .1em; }
.card-title { font-family: Georgia, "Songti SC", "Noto Serif SC", serif; font-size: 22px; }
.card-arrow { position: absolute; right: 22px; bottom: 23px; font-size: 17px; transition: transform .2s ease; }
.home-link-card:hover .card-arrow { transform: translate(4px,-4px); }
@media (max-width: 800px) { .chome-hero { min-height: auto; grid-template-columns: 1fr .9fr; padding: 90px 32px 80px; }.hero-art { width: min(42vw, 360px); }.hero-side-note { display: none; }.home-manifesto { grid-template-columns: .4fr 1.6fr; padding: 76px 32px 80px; }.manifesto-stamp { display: none; }.home-links { padding: 0 32px 60px; } }
@media (max-width: 560px) { .chome-hero { display: block; padding: 65px 25px 78px; }.hero-copy { padding: 0; } h1 { margin: 26px 0 18px; font-size: clamp(70px, 19vw, 104px); }.hero-art { position: absolute; top: 80px; right: -110px; width: 72vw; opacity: .52; }.hero-copy { min-height: 410px; }.hero-line, .hero-actions, .eyebrow, h1 { position: relative; z-index: 1; }.hero-bottom { right: 25px; left: 25px; }.home-manifesto { display: block; padding: 65px 25px 62px; }.manifesto-index { margin-bottom: 29px; }.manifesto-copy p { font-size: 34px; }.manifesto-meta { margin-top: 35px; }.home-links { grid-template-columns: 1fr; padding: 0 25px 45px; }.home-link-card { min-height: 128px; }.home-link-card + .home-link-card { padding-left: 0; border-left: 0; } }
</style>
