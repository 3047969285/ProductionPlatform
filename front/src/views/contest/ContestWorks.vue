<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'

gsap.registerPlugin(ScrollTrigger)

const filters = ['全部', '影像', '物件', '空间', '数字']
const activeFilter = ref('全部')
const selectedWork = ref(null)
const root = ref(null)
let animationContext
let mediaQuery
let filterTween
let modalTween
const works = [
  { id: '01', title: '潮汐档案', creator: '林间 / 影像', category: '影像', year: '2026', visual: 'tide', note: '把海平面写进一封不会寄出的信。' },
  { id: '02', title: '可以呼吸的墙', creator: '周原 / 空间', category: '空间', year: '2026', visual: 'breath', note: '一面墙，如何记住风经过的方向。' },
  { id: '03', title: '第二种日常', creator: 'MORII / 物件', category: '物件', year: '2025', visual: 'object', note: '为重复的日常，制造一个微小偏差。' },
  { id: '04', title: '看不见的花园', creator: '陈屿 / 数字', category: '数字', year: '2026', visual: 'garden', note: '数据生长的地方，也可以有季节。' },
  { id: '05', title: '给月亮的备忘', creator: '何也 / 影像', category: '影像', year: '2025', visual: 'moon', note: '没有被说出口的事，交给夜色保管。' },
  { id: '06', title: '慢速机器', creator: '野行 / 物件', category: '物件', year: '2026', visual: 'machine', note: '让工具学会等待，让效率暂停片刻。' },
]
const filteredWorks = computed(() => activeFilter.value === '全部' ? works : works.filter((work) => work.category === activeFilter.value))

watch(activeFilter, async () => {
  await nextTick()
  if (!root.value) return
  const cards = root.value.querySelectorAll('.work-card')
  filterTween?.kill()
  if (!cards.length) return

  const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  filterTween = gsap.fromTo(
    cards,
    reducedMotion ? { autoAlpha: 1, y: 0 } : { autoAlpha: 0, y: 18 },
    { autoAlpha: 1, y: 0, duration: reducedMotion ? 0 : 0.48, stagger: reducedMotion ? 0 : 0.06, ease: 'power3.out', overwrite: 'auto' },
  )
  ScrollTrigger.refresh()
})

watch(selectedWork, async (work) => {
  if (!work) return
  await nextTick()
  const modalCard = root.value?.querySelector('.modal-card')
  modalTween?.kill()
  if (modalCard && !window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    modalTween = gsap.fromTo(modalCard, { autoAlpha: 0, y: 26, scale: 0.97 }, { autoAlpha: 1, y: 0, scale: 1, duration: 0.55, ease: 'power3.out' })
  }
})

onMounted(() => {
  if (!root.value) return

  animationContext = gsap.context(() => {
    mediaQuery = gsap.matchMedia()
    mediaQuery.add({ reduced: '(prefers-reduced-motion: reduce)' }, ({ conditions }) => {
      const reducedMotion = conditions.reduced
      const duration = reducedMotion ? 0 : undefined

      gsap.timeline({ defaults: { ease: 'power3.out' } })
        .from('.works-kicker', { autoAlpha: 0, y: 12, duration: duration ?? 0.5 })
        .from('.works-heading h1', { autoAlpha: 0, y: 34, duration: duration ?? 0.8, ease: 'power4.out' }, '-=0.22')
        .from('.heading-note, .heading-count', { autoAlpha: 0, y: 18, duration: duration ?? 0.5 }, '-=0.35')
        .from('.works-toolbar', { autoAlpha: 0, y: 14, duration: duration ?? 0.5 }, '-=0.3')

      if (!reducedMotion) {
        gsap.timeline({
          scrollTrigger: { trigger: '.work-grid', start: 'top 82%', once: true },
        }).from('.work-card', { autoAlpha: 0, y: 30, stagger: 0.08, duration: 0.65 })
      }
    })
  }, root.value)
})

onUnmounted(() => {
  filterTween?.kill()
  modalTween?.kill()
  mediaQuery?.revert()
  animationContext?.revert()
})
</script>

<template>
  <main ref="root" class="works-page">
    <header class="works-heading">
      <div>
        <p class="works-kicker">THE EXHIBITION / 作品展厅</p>
        <h1>想法有了<br /><i>形状。</i></h1>
      </div>
      <div class="heading-note"><span>SELECTED WORKS</span><p>每件作品，都是<br />另一种可能的证据。</p></div>
      <span class="heading-count">{{ String(filteredWorks.length).padStart(2, '0') }} <small>件作品</small></span>
    </header>

    <div class="works-toolbar">
      <div class="filter-row" aria-label="作品分类">
        <button v-for="filter in filters" :key="filter" :class="{ active: activeFilter === filter }" @click="activeFilter = filter">{{ filter }}</button>
      </div>
      <span class="sort-label">按时间 / 由新到旧 <span>⌄</span></span>
    </div>

    <section class="work-grid" aria-label="参赛作品">
      <article v-for="(work, index) in filteredWorks" :key="work.id" class="work-card" :class="{ featured: index === 0 && activeFilter === '全部' }" @click="selectedWork = work" @keydown.enter="selectedWork = work" tabindex="0">
        <div class="work-visual" :class="`visual-${work.visual}`">
          <div class="visual-grain"></div>
          <template v-if="work.visual === 'tide'"><div class="tide-sun"></div><div class="tide-line t1"></div><div class="tide-line t2"></div><div class="tide-line t3"></div><span class="visual-type">TIDAL<br />MEMORY</span></template>
          <template v-else-if="work.visual === 'breath'"><div class="breath-arch a1"></div><div class="breath-arch a2"></div><div class="breath-arch a3"></div><div class="breath-light"></div><span class="visual-type">AIR / 01</span></template>
          <template v-else-if="work.visual === 'object'"><div class="object-shape"></div><div class="object-shadow"></div><span class="visual-type">A SMALL<br />DEVIATION</span></template>
          <template v-else-if="work.visual === 'garden'"><div class="garden-ring r1"></div><div class="garden-ring r2"></div><div class="garden-ring r3"></div><div class="garden-seed"></div><span class="visual-type">GARDEN<br />OF DATA</span></template>
          <template v-else-if="work.visual === 'moon'"><div class="moon-disc"></div><div class="moon-shadow"></div><span class="visual-type">NOTES<br />AT NIGHT</span></template>
          <template v-else><div class="machine-disc"></div><div class="machine-arm"></div><div class="machine-orbit"></div><span class="visual-type">SLOW<br />MACHINE</span></template>
          <span class="visual-open">↗</span>
        </div>
        <div class="work-meta"><div><span class="work-id">{{ work.id }} / {{ work.category }}</span><h2>{{ work.title }}</h2></div><span class="work-year">{{ work.year }}</span></div>
        <p class="work-creator">{{ work.creator }}</p>
      </article>
    </section>

    <div class="works-end"><span>END OF SELECTION</span><span>— 留一处空白，给下一件作品。</span><span>2026</span></div>

    <Transition name="work-modal">
      <div v-if="selectedWork" class="work-modal" @click.self="selectedWork = null" @keydown.esc="selectedWork = null">
        <section class="modal-card" role="dialog" aria-modal="true" :aria-label="selectedWork.title">
          <button class="modal-close" aria-label="关闭详情" @click="selectedWork = null">×</button>
          <div class="modal-art" :class="`visual-${selectedWork.visual}`"><span>{{ selectedWork.id }} / {{ selectedWork.category }}</span></div>
          <div class="modal-copy"><p class="works-kicker">{{ selectedWork.creator }} · {{ selectedWork.year }}</p><h2>{{ selectedWork.title }}</h2><p>{{ selectedWork.note }}</p><span class="modal-mark">未完成 · 2026</span></div>
        </section>
      </div>
    </Transition>
  </main>
</template>

<style scoped>
.works-page { padding: 77px clamp(24px, 8.5vw, 132px) 72px; background: #f2f1ec; }
.works-heading { position: relative; display: grid; grid-template-columns: 1fr .56fr auto; align-items: end; gap: 40px; padding-bottom: 45px; }
.works-kicker { color: #8c8d83; font-size: 9px; letter-spacing: .16em; }
.works-heading h1 { margin: 24px 0 0; font-family: Georgia, "Songti SC", "Noto Serif SC", serif; font-size: clamp(62px, 9vw, 118px); font-weight: 400; line-height: .93; letter-spacing: -.09em; }
.works-heading h1 i { color: #878b62; font-weight: 400; }
.heading-note { padding-bottom: 9px; }.heading-note > span { color: #8c8d83; font-size: 8px; letter-spacing: .15em; }.heading-note p { margin-top: 15px; font-family: Georgia, "Songti SC", serif; font-size: 17px; line-height: 1.7; }
.heading-count { padding-bottom: 9px; font-family: Georgia, serif; font-size: 30px; }.heading-count small { color: #898a83; font-family: inherit; font-size: 10px; }
.works-toolbar { display: flex; justify-content: space-between; align-items: center; padding: 14px 0; border-top: 1px solid rgba(25,26,26,.2); border-bottom: 1px solid rgba(25,26,26,.2); }
.filter-row { display: flex; gap: 7px; }.filter-row button { min-width: 50px; padding: 8px 13px; border: 1px solid transparent; background: none; color: #777873; font: inherit; font-size: 10px; cursor: pointer; transition: all .2s ease; }.filter-row button.active, .filter-row button:hover { border-color: rgba(25,26,26,.55); color: #191a1a; }
.sort-label { color: #777873; font-size: 9px; }.sort-label span { padding-left: 8px; color: #191a1a; }
.work-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 39px 19px; padding-top: 26px; }
.work-card { min-width: 0; cursor: pointer; outline: none; will-change: transform, opacity; }.work-card:focus-visible .work-visual { outline: 2px solid #747951; outline-offset: 4px; }.work-card.featured { grid-column: span 2; }.work-visual { position: relative; overflow: hidden; aspect-ratio: 1.12 / 1; isolation: isolate; transition: transform .4s cubic-bezier(.2,.8,.2,1), filter .4s ease; will-change: transform; }.work-card:hover .work-visual { transform: translateY(-6px) scale(1.012); filter: saturate(1.06); }.featured .work-visual { aspect-ratio: 1.94 / 1; }
.visual-grain { position: absolute; inset: 0; z-index: 3; pointer-events: none; opacity: .18; background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 180 180' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='.9' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='.28'/%3E%3C/svg%3E"); mix-blend-mode: soft-light; }
.visual-tide { background: radial-gradient(ellipse at 49% 92%, #f5a866 0 3%, #dd684b 15%, transparent 42%), linear-gradient(145deg,#173537,#153434 48%,#0e2428); }.tide-sun { position: absolute; top: 18%; left: 61%; width: 21%; aspect-ratio: 1; border-radius: 50%; background: #e3bc77; box-shadow: 0 0 62px rgba(224,155,92,.36); }.tide-line { position: absolute; right: -8%; bottom: -32%; width: 112%; height: 73%; border: 1px solid rgba(237,198,141,.56); border-radius: 50%; transform: rotate(-12deg); }.t2 { bottom: -42%; border-color: rgba(237,198,141,.35); }.t3 { bottom: -52%; border-color: rgba(237,198,141,.2); }.visual-type { position: absolute; top: 11%; left: 8%; z-index: 2; color: rgba(252,240,215,.76); font-family: Georgia, serif; font-size: clamp(9px,1.1vw,14px); line-height: 1.1; letter-spacing: .16em; }
.visual-breath { background: linear-gradient(145deg,#d7d3c6,#a5a899); }.breath-arch { position: absolute; right: 20%; bottom: 0; left: 20%; height: 79%; border: 1px solid rgba(248,246,229,.8); border-bottom: 0; border-radius: 50% 50% 0 0; }.a2 { right: 29%; left: 29%; height: 67%; border-color: rgba(248,246,229,.57); }.a3 { right: 38%; left: 38%; height: 55%; border-color: rgba(248,246,229,.39); }.breath-light { position: absolute; right: 15%; bottom: 0; left: 15%; height: 35%; background: linear-gradient(0deg,rgba(243,240,222,.6),transparent); filter: blur(24px); }.visual-breath .visual-type { color: rgba(42,47,39,.62); }
.visual-object { background: #c7b7a1; }.object-shape { position: absolute; top: 24%; left: 34%; width: 36%; height: 53%; border-radius: 49% 51% 12% 13%; background: linear-gradient(100deg,#e8d8bb,#a9845e 72%); box-shadow: inset -13px -4px 17px rgba(90,61,39,.18), 16px 24px 33px rgba(61,43,29,.18); transform: rotate(8deg); }.object-shadow { position: absolute; bottom: 13%; left: 20%; width: 64%; height: 13%; border-radius: 50%; background: rgba(69,50,34,.25); filter: blur(18px); }.visual-object .visual-type { color: rgba(52,43,33,.7); }
.visual-garden { background: radial-gradient(circle at 51% 49%,#d0ce9f,#7f9275 50%,#3e615b); }.garden-ring { position: absolute; top: 50%; left: 50%; width: 73%; aspect-ratio: 1; border: 1px solid rgba(239,237,184,.65); border-radius: 46% 54% 50% 50%; transform: translate(-50%,-50%) rotate(24deg); }.r2 { width: 55%; transform: translate(-50%,-50%) rotate(-33deg); }.r3 { width: 33%; border-color: rgba(239,237,184,.42); }.garden-seed { position: absolute; top: 43%; left: 46%; width: 12%; height: 18%; border-radius: 50% 0 50% 0; background: #e1dfa8; transform: rotate(-28deg); box-shadow: 0 0 28px rgba(239,237,184,.35); }.visual-garden .visual-type { color: rgba(248,245,216,.8); }
.visual-moon { background: linear-gradient(155deg,#1f2938,#3b4352 57%,#907c7e); }.moon-disc { position: absolute; top: 22%; right: 20%; width: 43%; aspect-ratio: 1; border-radius: 50%; background: radial-gradient(circle at 34% 31%,#f1e9d7,#c2baa9 62%,#8a8d8d); box-shadow: 0 0 55px rgba(226,218,200,.22); }.moon-shadow { position: absolute; top: 14%; right: 8%; width: 44%; aspect-ratio: 1; border-radius: 50%; background: #303746; transform: translate(22px,-7px); opacity: .75; }.visual-moon .visual-type { color: rgba(243,238,225,.72); }
.visual-machine { background: linear-gradient(135deg,#e8dfca,#b4a58f); }.machine-disc { position: absolute; top: 28%; left: 28%; width: 45%; aspect-ratio: 1; border: 1px solid rgba(67,67,52,.65); border-radius: 50%; background: repeating-radial-gradient(circle,transparent 0 8px,rgba(67,67,52,.22) 9px 10px); }.machine-arm { position: absolute; top: 50%; left: 50%; width: 42%; height: 1px; background: #45463b; transform: rotate(37deg); transform-origin: left; }.machine-orbit { position: absolute; top: 21%; left: 38%; width: 25%; height: 58%; border: 1px solid rgba(67,67,52,.45); border-radius: 50%; transform: rotate(43deg); }.visual-machine .visual-type { color: rgba(51,50,42,.66); }
.visual-open { position: absolute; right: 13px; bottom: 11px; z-index: 4; display: grid; width: 30px; height: 30px; place-items: center; border: 1px solid rgba(255,255,255,.6); border-radius: 50%; color: white; opacity: 0; transform: translate(-4px,4px); transition: all .2s ease; }.work-card:hover .visual-open { opacity: 1; transform: translate(0); }
.work-meta { display: flex; justify-content: space-between; align-items: end; padding-top: 13px; }.work-id, .work-year { color: #8c8d83; font-size: 8px; letter-spacing: .08em; }.work-meta h2 { margin-top: 5px; font-family: Georgia, "Songti SC", serif; font-size: 19px; font-weight: 400; letter-spacing: -.04em; }.work-creator { margin-top: 5px; color: #777873; font-size: 9px; }
.works-end { display: flex; justify-content: space-between; gap: 20px; margin-top: 75px; padding-top: 14px; border-top: 1px solid rgba(25,26,26,.2); color: #85867e; font-size: 8px; letter-spacing: .1em; }
.work-modal { position: fixed; inset: 0; z-index: 20; display: grid; place-items: center; padding: 24px; background: rgba(15,17,16,.75); backdrop-filter: blur(7px); }.modal-card { position: relative; display: grid; grid-template-columns: 1.1fr .9fr; width: min(820px,100%); min-height: 380px; background: #f2f1ec; }.modal-close { position: absolute; top: 12px; right: 13px; z-index: 3; width: 34px; height: 34px; border: 1px solid rgba(25,26,26,.2); border-radius: 50%; background: transparent; font-size: 22px; cursor: pointer; }.modal-art { position: relative; display: grid; overflow: hidden; min-height: 380px; place-items: end start; padding: 20px; color: rgba(255,255,255,.8); font-size: 9px; letter-spacing: .15em; }.modal-art.visual-object, .modal-art.visual-breath, .modal-art.visual-machine { color: #393a32; }.modal-copy { display: flex; flex-direction: column; justify-content: center; padding: clamp(26px,5vw,52px); }.modal-copy h2 { margin-top: 21px; font-family: Georgia, "Songti SC", serif; font-size: clamp(31px,5vw,48px); font-weight: 400; }.modal-copy > p:not(.works-kicker) { margin-top: 19px; color: #777873; font-family: Georgia, "Songti SC", serif; font-size: 15px; line-height: 1.9; }.modal-mark { margin-top: 50px; color: #93948c; font-size: 8px; letter-spacing: .12em; }.work-modal-enter-active, .work-modal-leave-active { transition: opacity .2s ease; }.work-modal-enter-from, .work-modal-leave-to { opacity: 0; }
@media (max-width: 760px) { .works-page { padding: 57px 24px 55px; }.works-heading { grid-template-columns: 1fr auto; gap: 15px; }.heading-note { grid-column: 1 / -1; order: 3; }.heading-count { align-self: start; padding-top: 36px; }.work-grid { grid-template-columns: repeat(2,minmax(0,1fr)); gap: 29px 13px; }.work-card.featured { grid-column: span 2; }.works-end { margin-top: 50px; }.sort-label { display: none; } }
@media (max-width: 480px) { .works-heading h1 { font-size: 66px; }.filter-row { width: 100%; justify-content: space-between; gap: 0; }.filter-row button { min-width: 0; padding: 8px; }.work-grid { grid-template-columns: 1fr 1fr; }.featured .work-visual { aspect-ratio: 1.25 / 1; }.work-meta h2 { font-size: 16px; }.works-end span:nth-child(2) { display: none; }.modal-card { grid-template-columns: 1fr; max-height: 90svh; overflow: auto; }.modal-art { min-height: 250px; }.modal-mark { margin-top: 25px; } }
</style>
