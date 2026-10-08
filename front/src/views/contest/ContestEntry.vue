<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { gsap } from 'gsap'

const discipline = ref('影像')
const title = ref('')
const creator = ref('')
const statement = ref('')
const fileName = ref('')
const preview = ref(false)
const root = ref(null)
const ready = computed(() => title.value.trim() && creator.value.trim() && statement.value.trim())
let animationContext
let mediaQuery
let previewTween

function chooseFile(event) {
  fileName.value = event.target.files?.[0]?.name || ''
}

function makePreview() {
  if (ready.value) preview.value = true
}

watch(preview, async (visible) => {
  if (!visible) return
  await nextTick()
  const previewCard = root.value?.querySelector('.preview-card')
  previewTween?.kill()
  if (previewCard && !window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    previewTween = gsap.fromTo(previewCard, { autoAlpha: 0, y: 22, scale: 0.98 }, { autoAlpha: 1, y: 0, scale: 1, duration: 0.55, ease: 'power3.out' })
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
        .from('.entry-kicker', { autoAlpha: 0, y: 12, duration: duration ?? 0.5 })
        .from('.entry-title-row', { autoAlpha: 0, y: 32, duration: duration ?? 0.8, ease: 'power4.out' }, '-=0.22')
        .from('.entry-deck', { autoAlpha: 0, y: 14, duration: duration ?? 0.5 }, '-=0.32')
        .from('.entry-steps .step', { autoAlpha: 0, x: -18, duration: duration ?? 0.5, stagger: 0.1 }, '-=0.18')
        .from('.entry-form', { autoAlpha: 0, x: 24, duration: duration ?? 0.7 }, '-=0.4')
    })
  }, root.value)
})

onUnmounted(() => {
  previewTween?.kill()
  mediaQuery?.revert()
  animationContext?.revert()
})
</script>

<template>
  <main ref="root" class="entry-page">
    <header class="entry-heading">
      <p class="entry-kicker">OPEN CALL / 参赛入口</p>
      <div class="entry-title-row"><h1>把念头<br /><em>做成形状。</em></h1><span class="entry-seal">O<br />P<br />E<br />N</span></div>
      <p class="entry-deck">一切始于一个问题。<br />然后，你让它发生。</p>
    </header>

    <section class="entry-content">
      <div class="entry-steps">
        <p class="steps-label">HOW TO BEGIN</p>
        <div class="step"><span>01</span><div><h2>提出问题</h2><p>从一个真实的疑问开始。</p></div></div>
        <div class="step"><span>02</span><div><h2>制作回应</h2><p>让想法获得形状与材料。</p></div></div>
        <div class="step"><span>03</span><div><h2>留下坐标</h2><p>让更多人找到它。</p></div></div>
        <div class="step-note">作品不必完整。<br /><i>它只需要真实。</i></div>
      </div>

      <form class="entry-form" @submit.prevent="makePreview">
        <div class="form-topline"><span>ENTRY FORM</span><span>2026 — 01</span></div>
        <label class="field"><span>作品名称 <b>*</b></span><input v-model="title" required maxlength="40" placeholder="给这个念头一个名字" /><small>{{ title.length }} / 40</small></label>
        <div class="form-split">
          <label class="field"><span>创作者 <b>*</b></span><input v-model="creator" required maxlength="30" placeholder="你的名字" /></label>
          <label class="field"><span>创作方向</span><select v-model="discipline"><option>影像</option><option>物件</option><option>空间</option><option>数字</option><option>其他</option></select></label>
        </div>
        <label class="field statement-field"><span>一句话，关于它 <b>*</b></span><textarea v-model="statement" required maxlength="120" rows="3" placeholder="你想改变什么？"></textarea><small>{{ statement.length }} / 120</small></label>
        <label class="upload-zone">
          <input type="file" accept="image/*,.pdf,.mp4,.mov" @change="chooseFile" />
          <span class="upload-symbol">＋</span>
          <span class="upload-label">{{ fileName || '放入一张作品图或方案文件' }}</span>
          <small>{{ fileName ? '文件已选，可在预览中查看作品信息' : 'JPG · PNG · PDF · MP4' }}</small>
        </label>
        <div class="submit-row"><span id="entry-preview-hint" role="status" aria-live="polite">{{ ready ? '点击后生成本地预览' : '先填写带 * 的三项内容' }}</span><button type="submit" aria-describedby="entry-preview-hint" :disabled="!ready">预览作品 <b>↗</b></button></div>
      </form>
    </section>

    <Transition name="entry-preview">
      <section v-if="preview" class="preview-card" aria-live="polite">
        <button class="preview-close" aria-label="关闭预览" @click="preview = false">×</button>
        <div class="preview-icon">{{ discipline.slice(0, 1) }}</div>
        <div><p class="entry-kicker">ENTRY PREVIEW / {{ discipline }}</p><h2>{{ title }}</h2><p>{{ statement }}</p><small>{{ creator }} <span>·</span> {{ fileName || '尚未添加作品文件' }}</small></div>
        <div class="preview-index">未完成<br />2026</div>
      </section>
    </Transition>
  </main>
</template>

<style scoped>
.entry-page { min-height: calc(100vh - 130px); padding: 72px clamp(24px, 9vw, 140px) 92px; background: #e7e6df; }
.entry-heading { max-width: 960px; margin: 0 auto; }.entry-kicker { color: #85867d; font-size: 9px; letter-spacing: .17em; }.entry-title-row { display: flex; align-items: flex-start; justify-content: space-between; }.entry-heading h1 { margin: 25px 0 0; font-family: Georgia, "Songti SC", "Noto Serif SC", serif; font-size: clamp(62px, 8.8vw, 112px); font-weight: 400; line-height: .97; letter-spacing: -.09em; }.entry-heading h1 em { color: #848a5f; font-weight: 400; }.entry-seal { display: grid; width: 54px; height: 54px; margin: 35px 5% 0 0; place-content: center; border: 1px solid rgba(25,26,26,.35); border-radius: 50%; color: #6e7255; font-family: Georgia, serif; font-size: 8px; line-height: 1.1; text-align: center; transform: rotate(10deg); }.entry-deck { margin: 21px 0 0; color: #777873; font-family: Georgia, "Songti SC", serif; font-size: 15px; line-height: 1.8; }
.entry-content { display: grid; grid-template-columns: .78fr 1.22fr; gap: clamp(40px, 9vw, 130px); max-width: 960px; margin: 72px auto 0; padding-top: 24px; border-top: 1px solid rgba(25,26,26,.25); }.entry-steps { padding-top: 5px; }.steps-label, .form-topline { color: #85867d; font-size: 8px; letter-spacing: .14em; }.step { display: grid; grid-template-columns: 36px 1fr; gap: 13px; margin-top: 28px; }.step > span { padding-top: 2px; color: #8c8d83; font-family: Georgia, serif; font-size: 10px; }.step h2 { font-family: Georgia, "Songti SC", serif; font-size: 16px; font-weight: 400; }.step p { margin-top: 5px; color: #8c8d83; font-size: 10px; }.step-note { margin: 53px 0 0 49px; color: #78796e; font-family: Georgia, "Songti SC", serif; font-size: 13px; line-height: 1.9; }.step-note i { color: #848a5f; }
.entry-form { padding: 24px 27px 21px; border: 1px solid rgba(25,26,26,.22); background: rgba(246,245,240,.57); will-change: transform, opacity; }.form-topline { display: flex; justify-content: space-between; padding-bottom: 22px; border-bottom: 1px solid rgba(25,26,26,.16); }.field { position: relative; display: flex; flex-direction: column; gap: 9px; padding: 20px 0 14px; border-bottom: 1px solid rgba(25,26,26,.15); }.field > span { color: #71736a; font-size: 9px; letter-spacing: .05em; }.field b { color: #8c8d65; font-weight: 400; }.field input, .field textarea, .field select { width: 100%; padding: 0; border: 0; outline: 0; background: transparent; color: #252622; font: inherit; font-family: Georgia, "Songti SC", serif; font-size: 14px; resize: vertical; }.field input::placeholder, .field textarea::placeholder { color: #acaea5; }.field select { appearance: none; cursor: pointer; }.field small { position: absolute; right: 0; bottom: 16px; color: #aaa99f; font-size: 8px; }.form-split { display: grid; grid-template-columns: 1fr 1fr; gap: 22px; }.statement-field textarea { padding-right: 40px; }.statement-field small { bottom: 19px; }.upload-zone { position: relative; display: flex; flex-direction: column; align-items: center; gap: 8px; margin-top: 19px; padding: 23px 12px; border: 1px dashed rgba(25,26,26,.25); cursor: pointer; transition: background .2s ease, border-color .2s ease, transform .25s ease; }.upload-zone:hover { border-color: #777b58; background: rgba(132,138,95,.07); transform: translateY(-2px); }.upload-zone input { position: absolute; inset: 0; width: 100%; opacity: 0; cursor: pointer; }.upload-symbol { color: #777b58; font-size: 20px; line-height: 1; }.upload-label { color: #606158; font-size: 10px; }.upload-zone small { color: #a0a197; font-size: 8px; letter-spacing: .08em; }.submit-row { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-top: 20px; }.submit-row > span { color: #999a91; font-size: 8px; }.submit-row button { display: inline-flex; align-items: center; gap: 23px; padding: 12px 15px; border: 0; background: #202220; color: #f2f1ec; font: inherit; font-size: 10px; cursor: pointer; transition: background .2s ease, transform .25s ease; }.submit-row button:hover:not(:disabled) { background: #777b58; transform: translateY(-2px); }.submit-row button:disabled { background: #aaa99f; cursor: not-allowed; }.submit-row button b { font-size: 14px; font-weight: 400; }
.field:focus-within { border-color: #777b58; }.field input:focus-visible, .field textarea:focus-visible, .field select:focus-visible { outline: 1px solid #777b58; outline-offset: 4px; border-radius: 2px; }.upload-zone:focus-within { border-color: #777b58; outline: 1px solid #777b58; outline-offset: 4px; background: rgba(132,138,95,.07); }.submit-row button:focus-visible, .preview-close:focus-visible { outline: 2px solid #777b58; outline-offset: 3px; }
.preview-card { position: relative; display: grid; grid-template-columns: 62px 1fr auto; align-items: center; gap: 19px; max-width: 960px; margin: 25px auto 0; padding: 20px 25px; border: 1px solid rgba(25,26,26,.2); background: #f4f3ed; }.preview-icon { display: grid; width: 54px; height: 54px; place-items: center; border-radius: 50%; background: #858a61; color: #f4f3ed; font-family: Georgia, serif; font-size: 21px; }.preview-card h2 { margin-top: 7px; font-family: Georgia, "Songti SC", serif; font-size: 22px; font-weight: 400; }.preview-card > div:nth-child(2) > p:not(.entry-kicker) { margin-top: 5px; color: #777873; font-family: Georgia, "Songti SC", serif; font-size: 12px; }.preview-card small { display: block; margin-top: 10px; color: #8e8f86; font-size: 9px; }.preview-card small span { padding: 0 6px; }.preview-index { color: #85867d; font-family: Georgia, serif; font-size: 9px; line-height: 1.6; letter-spacing: .1em; text-align: right; }.preview-close { position: absolute; top: 7px; right: 8px; border: 0; background: none; color: #777873; font-size: 18px; cursor: pointer; }.entry-preview-enter-active, .entry-preview-leave-active { transition: all .24s ease; }.entry-preview-enter-from, .entry-preview-leave-to { opacity: 0; transform: translateY(7px); }
@media (max-width: 740px) { .entry-page { padding: 57px 24px 66px; }.entry-content { grid-template-columns: 1fr; gap: 35px; margin-top: 48px; }.entry-steps { display: grid; grid-template-columns: repeat(3,1fr); gap: 10px; }.steps-label { grid-column: 1 / -1; }.step { display: block; margin-top: 15px; }.step > span { display: block; margin-bottom: 10px; }.step-note { display: none; }.entry-form { padding: 20px; } }
@media (max-width: 480px) { .entry-page { padding-right: 18px; padding-left: 18px; }.entry-heading h1 { font-size: 62px; }.entry-seal { width: 43px; height: 43px; margin-right: 0; }.entry-content { margin-top: 41px; }.entry-steps { gap: 6px; }.step { grid-template-columns: 1fr; }.step h2 { font-size: 14px; }.step p { font-size: 9px; }.entry-form { padding: 18px 16px; }.form-split { gap: 15px; }.submit-row > span { max-width: 98px; line-height: 1.5; }.preview-card { grid-template-columns: 42px 1fr; gap: 13px; padding: 18px 14px; }.preview-icon { width: 40px; height: 40px; }.preview-index { display: none; } }
</style>
