<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'

gsap.registerPlugin(ScrollTrigger)

const root = ref(null)
const activeIndex = ref(-1)
const cursorVisible = ref(false)

const eras = [
  {
    year: '2017',
    index: '01',
    model: 'Transformer',
    label: '注意力，成为新的坐标系',
    title: '先改变“看”的方式，才有后来会说话的模型。',
    body: 'Transformer 把序列中的每个词都放进同一张注意力网络。理解不再只能一步一步地发生，而是可以同时捕捉远距离的关系。',
    signal: 'Attention is all you need',
    stat: 'Self-attention',
    accent: 'cyan',
    visual: 'foundation',
    source: 'Attention Is All You Need',
    href: 'https://arxiv.org/abs/1706.03762',
  },
  {
    year: '2018',
    index: '02',
    model: 'GPT',
    label: '预训练，让模型先读懂世界',
    title: '从一张张标签开始，走向未标注的互联网。',
    body: 'GPT 把“生成式预训练”带到语言模型的中心：先在海量文本上学习语言规律，再用少量数据完成具体任务。',
    signal: 'Generative pre-training',
    stat: 'Unsupervised → useful',
    accent: 'violet',
    visual: 'pretrain',
    source: 'Improving Language Understanding by Generative Pre-Training',
    href: 'https://cdn.openai.com/research-covers/language-unsupervised/language_understanding_paper.pdf',
  },
  {
    year: '2019',
    index: '03',
    model: 'GPT-2',
    label: '规模第一次显露出“涌现”的轮廓',
    title: '当模型变大，文本开始显得像是“理解”之后写出来的。',
    body: '更大的模型、更长的上下文和更强的零样本生成，让人第一次直观看见：规模不只是数量，也可能打开新的能力层。',
    signal: '1.5B parameters',
    stat: 'Scale changes the curve',
    accent: 'pink',
    visual: 'scale',
    source: 'Better Language Models and Their Implications',
    href: 'https://openai.com/index/better-language-models/',
  },
  {
    year: '2020',
    index: '04',
    model: 'GPT-3',
    label: '少样本学习，第一次像魔法一样自然',
    title: '不需要为每个任务重新训练，只要给它一个例子。',
    body: 'GPT-3 把 in-context learning 推到了公众视野：模型可以从提示里的少量示例中切换任务，语言本身变成了新的接口。',
    signal: '175B parameters',
    stat: 'Few-shot learning',
    accent: 'orange',
    visual: 'scale',
    source: 'Language Models are Few-Shot Learners',
    href: 'https://openai.com/index/language-models-are-few-shot-learners/',
  },
  {
    year: '2022',
    index: '05',
    model: 'ChatGPT',
    label: '模型走出实验室，成为一种对话',
    title: 'AI 不再只回答问题，它开始和人一起把问题问清楚。',
    body: 'ChatGPT 将对话、上下文和人类反馈结合成产品体验。生成式 AI 的入口从 API 和研究论文，变成了每个人都能使用的聊天窗口。',
    signal: 'Human feedback',
    stat: 'A new interface',
    accent: 'lime',
    visual: 'chat',
    source: 'Introducing ChatGPT',
    href: 'https://openai.com/index/chatgpt/',
  },
  {
    year: '2023',
    index: '06',
    model: 'GPT-4',
    label: '能力边界，开始向多模态延展',
    title: '文字只是入口，模型开始学习看见、推理和更可靠地协作。',
    body: 'GPT-4 把更强的推理、视觉输入与更稳定的任务完成能力带到同一个模型家族中，也让“可控性”和“安全性”成为同样重要的工程目标。',
    signal: 'Reasoning + vision',
    stat: 'More reliable',
    accent: 'cyan',
    visual: 'vision',
    source: 'GPT-4',
    href: 'https://openai.com/index/gpt-4/',
  },
  {
    year: '2024',
    index: '07',
    model: 'GPT-4o',
    label: '从文字响应，走向原生的实时交互',
    title: '听见、看见、说出来，交互终于接近“自然”。',
    body: '“o”代表 omni：文本、视觉与音频被设计成同一条实时体验。AI 不只是在窗口里生成字，它开始回应人的速度、停顿和现场。',
    signal: 'Omni / realtime',
    stat: 'Text · audio · vision',
    accent: 'violet',
    visual: 'omni',
    source: 'Hello GPT-4o',
    href: 'https://openai.com/index/hello-gpt-4o/',
  },
  {
    year: '2025 →',
    index: '08',
    model: 'GPT-5',
    label: '从回答得好，走向完成得好',
    title: '下一站不是更像人，而是更懂目标、更会行动。',
    body: 'GPT-5 把快速响应、深度推理与工具协作放进一个更统一的工作流。GPT 的故事，也从“下一个 token”继续走向“下一个结果”。',
    signal: 'Reasoning at scale',
    stat: 'Think · use tools · deliver',
    accent: 'pink',
    visual: 'agent',
    source: 'GPT-5 model documentation',
    href: 'https://developers.openai.com/api/docs/models/gpt-5',
  },
  {
    year: '2026',
    index: '09',
    model: 'GPT-6',
    label: '从推理，走向可协作的行动',
    title: '模型开始把复杂目标拆解成真正完成的工作。',
    body: 'GPT-6 Astra 将推理、计算机操作与专业任务带入新一代模型；随后推出的 Sol 与 Luna 扩展了能力、速度与成本选择。9 月 29 日，GPT-6.1 Sol 又推动这一代继续迭代。',
    signal: 'GPT-6.1 Sol · Sep 2026',
    stat: 'Reason · use tools · deliver',
    accent: 'cyan',
    visual: 'frontier',
    source: 'GPT-6 Astra · OpenAI',
    href: 'https://openai.com/index/gpt-6-astra/',
  },
]

const currentEra = computed(() => eras[Math.max(activeIndex.value, 0)])

let context
let mediaContext
let pointerMove
let pointerLeave

function scrollToChapter(index) {
  const target = root.value?.querySelector(`[data-chapter="${index}"]`)
  target?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function handleKeydown(event) {
  if (event.key !== 'ArrowDown' && event.key !== 'ArrowUp') return
  event.preventDefault()
  const nextIndex = event.key === 'ArrowDown'
    ? Math.min(activeIndex.value + 1, eras.length - 1)
    : Math.max(activeIndex.value - 1, 0)
  scrollToChapter(nextIndex)
}

onMounted(async () => {
  await nextTick()
  if (!root.value) return

  const select = gsap.utils.selector(root.value)
  const rootElement = root.value

  context = gsap.context(() => {
    const intro = gsap.timeline({ defaults: { ease: 'power3.out' } })
    intro
      .from(select('.atlas-nav'), { y: -20, autoAlpha: 0, duration: 0.7 })
      .from(select('.hero-kicker'), { y: 18, autoAlpha: 0, duration: 0.6 }, '-=0.35')
      .from(select('.hero-word'), { yPercent: 110, rotateX: -70, autoAlpha: 0, duration: 1.1, stagger: 0.08 }, '-=0.25')
      .from(select('.hero-copy'), { y: 22, autoAlpha: 0, duration: 0.7 }, '-=0.55')
      .from(select('.hero-meta'), { y: 18, autoAlpha: 0, duration: 0.65 }, '-=0.45')
      .from(select('.hero-visual'), { scale: 0.82, rotate: -8, autoAlpha: 0, duration: 1.2, ease: 'expo.out' }, '-=0.85')
      .from(select('.scroll-cue'), { autoAlpha: 0, duration: 0.5 }, '-=0.25')

    gsap.to(select('.orbit-ring'), {
      rotation: 360,
      duration: 24,
      repeat: -1,
      ease: 'none',
    })

    gsap.to(select('.hero-orb'), {
      y: -16,
      scale: 1.04,
      duration: 3.8,
      repeat: -1,
      yoyo: true,
      ease: 'sine.inOut',
    })

    mediaContext = gsap.matchMedia()
    mediaContext.add(
      {
        desktop: '(min-width: 840px)',
        reduceMotion: '(prefers-reduced-motion: reduce)',
      },
      ({ conditions }) => {
        const { desktop, reduceMotion } = conditions

        const createActiveChapterMarkers = () => {
          select('.chapter').forEach((chapter, index) => {
            ScrollTrigger.create({
              trigger: chapter,
              start: 'top 55%',
              end: 'bottom 45%',
              onEnter: () => { activeIndex.value = index },
              onEnterBack: () => { activeIndex.value = index },
            })
          })
        }

        if (reduceMotion) {
          gsap.set(select('.hero-word, .hero-copy, .hero-meta, .hero-visual, .chapter-inner, .source-card'), {
            clearProps: 'all',
          })
          createActiveChapterMarkers()
          return
        }

        select('.chapter').forEach((chapter, index) => {
          const inner = chapter.querySelector('.chapter-inner')
          const visual = chapter.querySelector('.chapter-visual')
          const copy = chapter.querySelector('.chapter-copy')
          const lines = chapter.querySelectorAll('.copy-line')
          const signal = chapter.querySelector('.signal')

          const chapterTimeline = gsap.timeline({
            scrollTrigger: {
              trigger: chapter,
              start: 'top 72%',
              end: 'bottom 28%',
              toggleActions: 'play reverse play reverse',
              onEnter: () => { activeIndex.value = index },
              onEnterBack: () => { activeIndex.value = index },
            },
          })

          chapterTimeline
            .fromTo(inner, { autoAlpha: 0, y: desktop ? 42 : 22 }, { autoAlpha: 1, y: 0, duration: 0.9, ease: 'power3.out' })
            .fromTo(visual, { autoAlpha: 0, scale: 0.82, rotation: -6 }, { autoAlpha: 1, scale: 1, rotation: 0, duration: 1.15, ease: 'expo.out' }, '-=0.65')
            .fromTo(copy, { autoAlpha: 0, x: desktop ? 36 : 0, y: desktop ? 0 : 18 }, { autoAlpha: 1, x: 0, y: 0, duration: 0.85, ease: 'power3.out' }, '-=0.8')
            .fromTo(lines, { autoAlpha: 0, y: 14 }, { autoAlpha: 1, y: 0, duration: 0.55, stagger: 0.08, ease: 'power2.out' }, '-=0.45')
            .fromTo(signal, { width: '0%' }, { width: '100%', duration: 1.1, ease: 'power2.inOut' }, '-=0.55')

          gsap.to(chapter.querySelector('.visual-glow'), {
            y: -18,
            x: index % 2 ? 10 : -8,
            duration: 4.5 + index * 0.2,
            repeat: -1,
            yoyo: true,
            ease: 'sine.inOut',
            scrollTrigger: {
              trigger: chapter,
              start: 'top bottom',
              end: 'bottom top',
              scrub: 1,
            },
          })
        })

        gsap.to(select('.timeline-progress'), {
          scaleY: 1,
          ease: 'none',
          scrollTrigger: {
            trigger: select('.chapters')[0],
            start: 'top center',
            end: 'bottom center',
            scrub: 0.8,
          },
        })

        gsap.fromTo(select('.closing-card'),
          { autoAlpha: 0, y: 36, scale: 0.96 },
          {
            autoAlpha: 1,
            y: 0,
            scale: 1,
            duration: 1,
            ease: 'power3.out',
            scrollTrigger: { trigger: select('.closing')[0], start: 'top 72%', toggleActions: 'play none none reverse' },
          },
        )
      }, rootElement)
  }, rootElement)

  const aura = select('.cursor-aura')[0]
  if (aura) {
    const xTo = gsap.quickTo(aura, 'x', { duration: 0.45, ease: 'power3' })
    const yTo = gsap.quickTo(aura, 'y', { duration: 0.45, ease: 'power3' })
    pointerMove = (event) => {
      const rect = rootElement.getBoundingClientRect()
      xTo(event.clientX - rect.left - 90)
      yTo(event.clientY - rect.top - 90)
      cursorVisible.value = true
    }
    pointerLeave = () => { cursorVisible.value = false }
    rootElement.addEventListener('pointermove', pointerMove)
    rootElement.addEventListener('pointerleave', pointerLeave)
  }

  window.addEventListener('keydown', handleKeydown)
  window.setTimeout(() => ScrollTrigger.refresh(), 60)
})

onUnmounted(() => {
  if (pointerMove) root.value?.removeEventListener('pointermove', pointerMove)
  if (pointerLeave) root.value?.removeEventListener('pointerleave', pointerLeave)
  window.removeEventListener('keydown', handleKeydown)
  mediaContext?.revert()
  context?.revert()
})
</script>

<template>
  <main ref="root" class="gpt-atlas" :class="{ 'cursor-active': cursorVisible }">
    <div class="cursor-aura" aria-hidden="true" />
    <div class="noise" aria-hidden="true" />

    <header class="atlas-nav">
      <a class="atlas-brand" href="#top" aria-label="GPT Atlas 首页">
        <span class="brand-mark">◎</span>
        <span>GPT ATLAS</span>
      </a>
      <div class="nav-status"><span class="status-dot" />一部仍在继续的历史</div>
      <button class="nav-action" type="button" @click="scrollToChapter(eras.length - 1)">跳到现在 <span>↗</span></button>
    </header>

    <aside class="chapter-rail" aria-label="GPT 发展章节">
      <button
        v-for="(era, index) in eras"
        :key="era.index"
        class="rail-dot"
        :class="{ active: activeIndex === index }"
        type="button"
        :aria-label="`跳到 ${era.year} ${era.model}`"
        @click="scrollToChapter(index)"
      >
        <span>{{ era.index }}</span>
      </button>
      <div class="rail-line"><i class="timeline-progress" /></div>
    </aside>

    <section id="top" class="hero panel-space">
      <div class="hero-copy-wrap">
        <p class="hero-kicker"><span class="kicker-line" /> OpenAI / A living timeline of intelligence</p>
        <h1 class="hero-title" aria-label="GPT 的跃迁史">
          <span class="hero-word">GPT</span>
          <span class="hero-word hero-word-muted">的跃迁史</span>
        </h1>
        <p class="hero-copy">从预测下一个词，到帮助人类完成下一个目标。<br />一场关于规模、推理与交互方式的长时间实验。</p>
        <div class="hero-meta">
          <span>2017 — NOW</span>
          <span>{{ String(eras.length).padStart(2, '0') }} 个关键节点</span>
          <span>滚动开始穿越</span>
        </div>
        <button class="start-button" type="button" @click="scrollToChapter(0)">
          <span>开始穿越时间线</span><span class="button-arrow">↓</span>
        </button>
      </div>

      <div class="hero-visual" aria-hidden="true">
        <div class="hero-orb" />
        <div class="hero-core"><span>GPT</span><small>GEN·01</small></div>
        <div class="orbit-ring orbit-ring-a"><i /><i /><i /></div>
        <div class="orbit-ring orbit-ring-b"><i /><i /></div>
        <div class="orbit-ring orbit-ring-c"><i /></div>
        <div class="hero-cross cross-a" /><div class="hero-cross cross-b" />
        <div class="hero-coordinate">37°46′N<br />122°25′W</div>
      </div>

      <div class="scroll-cue"><span>SCROLL TO EXPLORE</span><i /></div>
    </section>

    <section class="preface panel-space">
      <div class="preface-label">00 / THE PREMISE</div>
      <div class="preface-content">
        <p class="preface-lead">GPT 不是一条简单的产品升级列表。</p>
        <p class="preface-text">它更像一条不断改变接口的河流：先学习语言，再学习任务，后来学习如何和人一起工作。</p>
      </div>
      <div class="preface-line" />
    </section>

    <section class="chapters" aria-label="GPT 时间线章节">
      <article
        v-for="(era, index) in eras"
        :id="`chapter-${index}`"
        :key="era.model"
        class="chapter"
        :data-chapter="index"
        :data-accent="era.accent"
      >
        <div class="chapter-inner">
          <div class="chapter-visual" :class="`visual-${era.visual}`" aria-hidden="true">
            <div class="visual-grid" />
            <div class="visual-glow" />
            <div class="visual-frame">
              <span class="visual-index">{{ era.index }}</span>
              <span class="visual-year">{{ era.year }}</span>
              <div class="visual-graphic">
                <template v-if="era.visual === 'foundation'">
                  <div class="foundation-core">ATTN</div>
                  <div class="foundation-orbit orbit-one" /><div class="foundation-orbit orbit-two" />
                  <span class="node node-a" /><span class="node node-b" /><span class="node node-c" /><span class="node node-d" />
                </template>
                <template v-else-if="era.visual === 'pretrain'">
                  <div class="pretrain-word">read<span>.</span></div>
                  <div class="pretrain-stack"><i /><i /><i /><i /><i /></div>
                  <span class="pretrain-caption">learn patterns / predict what comes next</span>
                </template>
                <template v-else-if="era.visual === 'scale'">
                  <div class="scale-number">{{ era.model === 'GPT-3' ? '175' : '1.5' }}<small>B</small></div>
                  <div class="scale-bars"><i /><i /><i /><i /><i /><i /><i /></div>
                  <span class="scale-caption">PARAMETERS / THE CURVE BENDS</span>
                </template>
                <template v-else-if="era.visual === 'chat'">
                  <div class="chat-window"><div class="chat-top"><i /><i /><i /></div><p>Write a new beginning<span>▌</span></p><small>context is now a conversation</small></div>
                  <div class="chat-orb" />
                </template>
                <template v-else-if="era.visual === 'vision'">
                  <div class="vision-lens"><span /><span /><span /></div>
                  <div class="vision-scan" />
                  <div class="vision-label">TEXT + IMAGE<br />ONE MODEL</div>
                </template>
                <template v-else-if="era.visual === 'omni'">
                  <div class="omni-core">o</div>
                  <div class="omni-wave wave-one" /><div class="omni-wave wave-two" /><div class="omni-wave wave-three" />
                  <span class="omni-label">audio / vision / text</span>
                </template>
                <template v-else-if="era.visual === 'agent'">
                  <div class="agent-core">GPT<span>5</span></div>
                  <div class="agent-path path-one" /><div class="agent-path path-two" /><div class="agent-path path-three" />
                  <span class="agent-node node-one">THINK</span><span class="agent-node node-two">USE</span><span class="agent-node node-three">SHIP</span>
                </template>
                <template v-else-if="era.visual === 'frontier'">
                  <div class="frontier-map">
                    <svg class="frontier-lines" viewBox="0 0 420 320" aria-hidden="true">
                      <path d="M74 86 C135 98 135 126 194 151 S286 190 342 224" />
                      <path d="M80 237 C135 220 144 189 194 163 S283 112 346 82" />
                      <path d="M204 54 C191 103 193 123 206 157 S220 229 206 272" />
                      <circle cx="74" cy="86" r="4" /><circle cx="80" cy="237" r="4" />
                      <circle cx="346" cy="82" r="4" /><circle cx="342" cy="224" r="4" />
                    </svg>
                    <div class="frontier-core"><span>GPT</span><strong>6</strong></div>
                    <span class="frontier-node frontier-node-a">REASON</span>
                    <span class="frontier-node frontier-node-b">COMPUTER USE</span>
                    <span class="frontier-node frontier-node-c">CREATE</span>
                    <span class="frontier-node frontier-node-d">DELIVER</span>
                  </div>
                </template>
              </div>
              <span class="visual-coord">{{ era.signal }}</span>
            </div>
          </div>

          <div class="chapter-copy">
            <div class="chapter-heading">
              <span class="chapter-number">{{ era.index }} / {{ String(eras.length).padStart(2, '0') }}</span>
              <span class="chapter-year">{{ era.year }}</span>
            </div>
            <p class="chapter-label">{{ era.label }}</p>
            <h2>{{ era.title }}</h2>
            <p class="copy-line chapter-body">{{ era.body }}</p>
            <div class="signal-wrap"><span class="signal" /><span>{{ era.stat }}</span></div>
            <a class="source-link" :href="era.href" target="_blank" rel="noreferrer">阅读原始资料 <span>↗</span></a>
          </div>
        </div>
      </article>
    </section>

    <section class="closing panel-space">
      <div class="closing-card">
        <p class="closing-kicker">THE NEXT TOKEN IS NOT THE DESTINATION</p>
        <h2>历史还没有结束。<br /><em>它正在被每一次交互继续书写。</em></h2>
        <div class="closing-foot">
          <span>GPT ATLAS / 2017 — NOW</span>
          <a href="#top" @click.prevent="scrollToChapter(0)">回到开端 <span>↑</span></a>
        </div>
      </div>
      <div class="sources">
        <span>Sources /</span>
        <a href="https://platform.openai.com/docs/models" target="_blank" rel="noreferrer">OpenAI model catalog</a>
        <a href="https://developers.openai.com/api/docs/deprecations" target="_blank" rel="noreferrer">OpenAI API changelog</a>
        <span class="sources-note">时间线为叙事化概览；GPT 模型与 ChatGPT 产品是相关但不同的概念。</span>
      </div>
    </section>
  </main>
</template>

<style scoped>
:global(html) { scroll-behavior: auto; }
:global(body) { background: #000; }

.gpt-atlas {
  --ink: #000;
  --ink-soft: #0b0b0b;
  --paper: #efefeb;
  --muted: rgba(239, 239, 235, 0.56);
  --muted-soft: rgba(239, 239, 235, 0.32);
  --hairline: rgba(239, 239, 235, 0.14);
  --cyan: #d5d5d0;
  --violet: #aaa9a5;
  --pink: #b9b8b3;
  --orange: #c1bdb6;
  --lime: #d9d9d3;
  position: relative;
  overflow: clip;
  min-height: 100vh;
  color: var(--paper);
  background: var(--ink);
  font-family: var(--font-body);
  isolation: isolate;
}

.gpt-atlas::before {
  position: fixed; top: auto; right: -12vw; bottom: -8vh; left: -12vw; z-index: -2;
  width: 124vw; height: 58vh; content: '';
  opacity: .32; pointer-events: none;
  background-image: linear-gradient(rgba(239,239,235,.09) 1px, transparent 1px), linear-gradient(90deg, rgba(239,239,235,.09) 1px, transparent 1px);
  background-size: 100% 42px, 64px 100%;
  transform: perspective(520px) rotateX(59deg);
  transform-origin: center top;
  mask-image: linear-gradient(to bottom, transparent 0%, black 26%, transparent 93%);
}

.noise { position: fixed; inset: 0; z-index: 10; pointer-events: none; opacity: .034; mix-blend-mode: screen; background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 180 180' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='.8' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='.55'/%3E%3C/svg%3E"); }
.cursor-aura { position: absolute; z-index: 0; width: 180px; height: 180px; border-radius: 50%; pointer-events: none; opacity: 0; background: radial-gradient(circle, rgba(139,246,241,.12), transparent 68%); filter: blur(6px); transform: translate(-180px, -180px); transition: opacity .4s ease; }
.cursor-active .cursor-aura { opacity: 1; }
.hero-visual, .chapter-visual { filter: grayscale(1); }

.atlas-nav { position: absolute; top: 0; left: 0; right: 0; z-index: 20; display: flex; align-items: center; justify-content: space-between; padding: 28px clamp(20px, 6vw, 90px); }
.atlas-brand, .nav-action { color: var(--paper); text-decoration: none; }
.atlas-brand { display: inline-flex; align-items: center; gap: 10px; font-size: 11px; letter-spacing: .2em; font-weight: 700; }
.brand-mark { display: grid; width: 25px; height: 25px; place-items: center; border: 1px solid rgba(243,240,234,.65); border-radius: 50%; font-size: 19px; line-height: 1; }
.nav-status { display: inline-flex; align-items: center; gap: 8px; color: var(--muted); font-size: 11px; letter-spacing: .12em; text-transform: uppercase; }
.status-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--lime); box-shadow: 0 0 16px var(--lime); }
.nav-action { display: flex; align-items: center; gap: 11px; padding: 9px 0; border: 0; border-bottom: 1px solid var(--hairline); background: none; cursor: pointer; font: inherit; font-size: 11px; letter-spacing: .12em; text-transform: uppercase; transition: border-color .3s ease, color .3s ease; }
.nav-action:hover { color: var(--cyan); border-color: var(--cyan); }

.chapter-rail { position: fixed; top: 50%; right: clamp(17px, 3vw, 44px); z-index: 16; display: flex; flex-direction: column; align-items: center; gap: 11px; transform: translateY(-50%); }
.rail-dot { position: relative; display: grid; width: 14px; height: 14px; padding: 0; place-items: center; border: 1px solid rgba(243,240,234,.35); border-radius: 50%; color: transparent; background: transparent; cursor: pointer; transition: all .35s ease; }
.rail-dot span { position: absolute; right: 26px; opacity: 0; color: var(--muted); white-space: nowrap; font-size: 10px; letter-spacing: .12em; transform: translateX(5px); transition: all .35s ease; }
.rail-dot:hover span, .rail-dot.active span { opacity: 1; transform: translateX(0); }
.rail-dot.active { border-color: var(--cyan); background: var(--cyan); box-shadow: 0 0 15px rgba(139,246,241,.55); }
.rail-line { position: absolute; top: 7px; bottom: 7px; z-index: -1; width: 1px; background: rgba(243,240,234,.14); }
.timeline-progress { display: block; width: 1px; height: 100%; background: var(--cyan); transform: scaleY(0); transform-origin: top; }

.panel-space { position: relative; width: min(1320px, calc(100% - 80px)); margin: 0 auto; }
.hero { display: grid; min-height: 100svh; align-items: center; grid-template-columns: minmax(0, 1.05fr) minmax(360px, .95fr); gap: 5vw; padding-top: 80px; }
.hero-copy-wrap { position: relative; z-index: 2; padding: 72px 0 54px; }
.hero-kicker { display: flex; align-items: center; gap: 12px; margin-bottom: 30px; color: var(--cyan); font-size: 10px; letter-spacing: .18em; text-transform: uppercase; }
.kicker-line { width: 32px; height: 1px; background: currentColor; }
.hero-title { margin: 0 0 28px; color: var(--paper); font-family: var(--font-display); font-size: clamp(4.2rem, 10.4vw, 10.5rem); font-weight: 700; letter-spacing: -.1em; line-height: .79; perspective: 600px; }
.hero-word { display: block; transform-origin: 50% 100%; }
.hero-word-muted { color: transparent; -webkit-text-stroke: 1px rgba(243,240,234,.58); }
.hero-copy { max-width: 480px; color: var(--muted); font-size: clamp(1rem, 1.5vw, 1.22rem); line-height: 1.8; }
.hero-meta { display: flex; flex-wrap: wrap; gap: 10px 22px; margin-top: 38px; color: var(--muted-soft); font-size: 10px; letter-spacing: .14em; }
.hero-meta span { position: relative; }
.hero-meta span + span::before { position: absolute; top: 50%; left: -13px; width: 3px; height: 3px; content: ''; border-radius: 50%; background: var(--muted-soft); transform: translateY(-50%); }
.start-button { display: inline-flex; align-items: center; gap: 36px; margin-top: 46px; padding: 14px 16px 14px 20px; border: 1px solid rgba(139,246,241,.45); border-radius: 999px; color: var(--cyan); background: rgba(139,246,241,.045); cursor: pointer; font: inherit; font-size: 12px; letter-spacing: .08em; transition: background .3s ease, transform .3s ease, box-shadow .3s ease; }
.start-button:hover { background: rgba(139,246,241,.12); box-shadow: 0 0 30px rgba(139,246,241,.12); transform: translateY(-2px); }
.button-arrow { display: grid; width: 25px; height: 25px; place-items: center; border-radius: 50%; color: var(--ink); background: var(--cyan); font-size: 16px; }

.hero-visual { position: relative; display: grid; min-height: 620px; place-items: center; }
.hero-orb { position: absolute; width: min(32vw, 480px); aspect-ratio: 1; border-radius: 50%; background: radial-gradient(circle at 35% 28%, rgba(255,255,255,.95), rgba(139,246,241,.8) 8%, rgba(96,78,237,.25) 34%, transparent 68%); filter: blur(1px); box-shadow: 0 0 80px rgba(89,227,222,.12), inset -22px -28px 60px rgba(0,0,0,.25); }
.hero-core { position: relative; z-index: 2; display: flex; width: 190px; aspect-ratio: 1; flex-direction: column; align-items: center; justify-content: center; border: 1px solid rgba(243,240,234,.62); border-radius: 50%; color: var(--paper); background: rgba(7,9,13,.35); box-shadow: 0 0 0 12px rgba(7,9,13,.06), 0 0 0 13px rgba(243,240,234,.18); backdrop-filter: blur(7px); }
.hero-core span { font-family: var(--font-display); font-size: 3.7rem; font-weight: 700; letter-spacing: -.12em; }
.hero-core small { margin-top: 6px; color: var(--muted); font-size: 9px; letter-spacing: .22em; }
.orbit-ring { position: absolute; width: 65%; aspect-ratio: 1; border: 1px solid rgba(243,240,234,.18); border-radius: 50%; transform: rotate(18deg) skewX(-12deg); }
.orbit-ring-a { width: 82%; transform: rotate(25deg) skewX(-20deg); }
.orbit-ring-b { width: 58%; transform: rotate(-36deg) skewX(19deg); }
.orbit-ring-c { width: 104%; border-style: dashed; transform: rotate(52deg) skewX(-25deg); }
.orbit-ring i { position: absolute; width: 8px; height: 8px; border: 1px solid var(--paper); border-radius: 50%; background: var(--ink); box-shadow: 0 0 14px var(--cyan); }
.orbit-ring i:nth-child(1) { top: -4px; left: 20%; }.orbit-ring i:nth-child(2) { right: 5%; bottom: 24%; }.orbit-ring i:nth-child(3) { bottom: 8%; left: 30%; }
.hero-cross { position: absolute; width: 18px; height: 18px; opacity: .62; }.hero-cross::before, .hero-cross::after { position: absolute; top: 50%; left: 0; width: 100%; height: 1px; content: ''; background: var(--paper); }.hero-cross::after { transform: rotate(90deg); }.cross-a { top: 14%; left: 17%; }.cross-b { right: 5%; bottom: 18%; transform: scale(.7); }
.hero-coordinate { position: absolute; right: 4%; bottom: 20%; color: var(--muted-soft); font-size: 9px; letter-spacing: .16em; line-height: 1.8; }
.scroll-cue { position: absolute; bottom: 30px; left: 0; display: flex; align-items: center; gap: 14px; color: var(--muted-soft); font-size: 9px; letter-spacing: .2em; }
.scroll-cue i { display: block; width: 50px; height: 1px; background: var(--muted-soft); }

.preface { display: grid; min-height: 74svh; align-content: center; grid-template-columns: .7fr 1.5fr; gap: 8vw; padding: 14vh 0; }
.preface-label { align-self: start; padding-top: 10px; color: var(--cyan); font-size: 10px; letter-spacing: .18em; }
.preface-lead { max-width: 760px; margin-bottom: 28px; color: var(--paper); font-family: var(--font-display); font-size: clamp(2rem, 4vw, 4.2rem); font-weight: 600; letter-spacing: -.06em; line-height: 1.05; }
.preface-text { max-width: 540px; color: var(--muted); font-size: clamp(1rem, 1.7vw, 1.34rem); line-height: 1.8; }
.preface-line { position: absolute; right: 0; bottom: 0; left: 0; height: 1px; background: var(--hairline); }

.chapter { position: relative; min-height: 100svh; scroll-margin-top: 1px; }
.chapter-inner { display: grid; width: min(1160px, calc(100% - 140px)); min-height: 100svh; align-items: center; grid-template-columns: 1.1fr .9fr; gap: clamp(50px, 9vw, 150px); margin: 0 auto; padding: 100px 0; }
.chapter:nth-child(even) .chapter-inner { grid-template-columns: .9fr 1.1fr; }.chapter:nth-child(even) .chapter-visual { order: 2; }.chapter:nth-child(even) .chapter-copy { order: 1; }
.chapter-visual { position: relative; display: grid; min-height: min(70vh, 610px); place-items: center; }
.visual-grid { position: absolute; width: 84%; height: 74%; opacity: .45; border: 1px solid var(--hairline); background-image: linear-gradient(rgba(243,240,234,.05) 1px, transparent 1px), linear-gradient(90deg, rgba(243,240,234,.05) 1px, transparent 1px); background-size: 38px 38px; transform: perspective(700px) rotateX(58deg) rotateZ(-18deg); mask-image: radial-gradient(ellipse, black 15%, transparent 70%); }
.visual-glow { position: absolute; width: 55%; aspect-ratio: 1; border-radius: 50%; background: var(--accent); filter: blur(70px); opacity: .18; }
.visual-frame { position: relative; display: flex; width: min(100%, 510px); min-height: 460px; flex-direction: column; justify-content: space-between; padding: 22px; border: 1px solid rgba(243,240,234,.23); background: linear-gradient(145deg, rgba(243,240,234,.07), rgba(243,240,234,.015)); box-shadow: 0 30px 90px rgba(0,0,0,.25); backdrop-filter: blur(15px); }
.visual-frame::before, .visual-frame::after { position: absolute; width: 9px; height: 9px; content: ''; border-color: var(--accent); }.visual-frame::before { top: -1px; left: -1px; border-top: 1px solid; border-left: 1px solid; }.visual-frame::after { right: -1px; bottom: -1px; border-right: 1px solid; border-bottom: 1px solid; }
.visual-index, .visual-year, .visual-coord { position: relative; z-index: 1; color: var(--muted); font-size: 10px; letter-spacing: .18em; }.visual-year { align-self: flex-end; color: var(--paper); }.visual-coord { align-self: flex-start; color: var(--muted-soft); letter-spacing: .12em; }
.visual-graphic { position: relative; display: grid; min-height: 320px; place-items: center; }
.chapter-copy { position: relative; z-index: 2; max-width: 520px; }.chapter-heading { display: flex; align-items: center; justify-content: space-between; padding-bottom: 18px; border-bottom: 1px solid var(--hairline); color: var(--muted); font-size: 10px; letter-spacing: .18em; }.chapter-year { color: var(--accent); }.chapter-label { margin: 42px 0 18px; color: var(--accent); font-size: 11px; letter-spacing: .1em; }.chapter-copy h2 { max-width: 590px; margin-bottom: 26px; color: var(--paper); font-family: var(--font-display); font-size: clamp(2.1rem, 4vw, 4.8rem); font-weight: 600; letter-spacing: -.07em; line-height: 1.04; }.chapter-body { max-width: 460px; color: var(--muted); font-size: 1rem; line-height: 1.85; }.signal-wrap { display: flex; align-items: center; gap: 16px; margin-top: 42px; color: var(--muted-soft); font-size: 10px; letter-spacing: .16em; text-transform: uppercase; }.signal { display: block; width: 100%; max-width: 110px; height: 2px; background: var(--accent); transform-origin: left; }.source-link { display: inline-flex; gap: 10px; margin-top: 30px; padding-bottom: 5px; border-bottom: 1px solid rgba(243,240,234,.25); color: var(--paper); text-decoration: none; font-size: 11px; letter-spacing: .08em; transition: color .25s ease, border-color .25s ease; }.source-link:hover { color: var(--accent); border-color: var(--accent); }

.visual-foundation { --accent: var(--cyan); }.foundation-core { position: relative; z-index: 2; display: grid; width: 142px; height: 142px; place-items: center; border: 1px solid var(--accent); border-radius: 50%; color: var(--accent); background: rgba(139,246,241,.06); font-size: 13px; letter-spacing: .16em; box-shadow: 0 0 40px rgba(139,246,241,.18); }.foundation-orbit { position: absolute; width: 64%; height: 34%; border: 1px solid rgba(139,246,241,.45); border-radius: 50%; transform: rotate(30deg); }.foundation-orbit.orbit-two { width: 38%; height: 78%; transform: rotate(-34deg); }.node { position: absolute; width: 10px; height: 10px; border: 1px solid var(--accent); border-radius: 50%; background: var(--ink-soft); box-shadow: 0 0 14px var(--accent); }.node-a { top: 18%; left: 23%; }.node-b { top: 25%; right: 18%; }.node-c { bottom: 19%; left: 30%; }.node-d { right: 26%; bottom: 28%; }
.visual-pretrain { --accent: var(--violet); }.pretrain-word { color: var(--paper); font-family: var(--font-display); font-size: clamp(3rem, 7vw, 5.7rem); font-weight: 600; letter-spacing: -.1em; }.pretrain-word span { color: var(--accent); }.pretrain-stack { position: absolute; right: 12%; bottom: 18%; display: flex; width: 48%; flex-direction: column; gap: 7px; transform: rotate(-18deg); }.pretrain-stack i { display: block; height: 8px; border-radius: 2px; background: linear-gradient(90deg, var(--accent), transparent); }.pretrain-stack i:nth-child(2) { width: 80%; }.pretrain-stack i:nth-child(3) { width: 58%; }.pretrain-stack i:nth-child(4) { width: 90%; }.pretrain-stack i:nth-child(5) { width: 68%; }.pretrain-caption { position: absolute; bottom: 4%; left: 5%; color: var(--muted); font-size: 9px; letter-spacing: .12em; }
.visual-scale { --accent: var(--orange); }.scale-number { color: var(--paper); font-family: var(--font-display); font-size: clamp(5rem, 12vw, 9rem); font-weight: 600; letter-spacing: -.1em; line-height: .8; }.scale-number small { color: var(--accent); font-size: .25em; letter-spacing: .04em; }.scale-bars { position: absolute; right: 4%; bottom: 13%; left: 4%; display: flex; align-items: flex-end; gap: 6px; height: 75px; }.scale-bars i { flex: 1; height: 15%; background: var(--accent); opacity: .65; }.scale-bars i:nth-child(2) { height: 23%; }.scale-bars i:nth-child(3) { height: 31%; }.scale-bars i:nth-child(4) { height: 43%; }.scale-bars i:nth-child(5) { height: 58%; }.scale-bars i:nth-child(6) { height: 73%; }.scale-bars i:nth-child(7) { height: 100%; box-shadow: 0 0 28px rgba(255,187,117,.35); }.scale-caption { position: absolute; top: 8%; left: 4%; color: var(--muted); font-size: 9px; letter-spacing: .17em; }
.visual-chat { --accent: var(--lime); }.chat-window { position: relative; width: 76%; padding: 22px 26px 28px; border: 1px solid rgba(195,239,134,.5); background: rgba(195,239,134,.06); box-shadow: 0 18px 50px rgba(0,0,0,.2); }.chat-top { display: flex; gap: 5px; padding-bottom: 24px; border-bottom: 1px solid rgba(243,240,234,.12); }.chat-top i { width: 6px; height: 6px; border-radius: 50%; background: var(--accent); }.chat-window p { min-height: 96px; margin: 26px 0 18px; color: var(--paper); font-family: var(--font-display); font-size: 1.65rem; letter-spacing: -.04em; }.chat-window p span { color: var(--accent); font-size: 1rem; }.chat-window small { color: var(--muted); font-size: 9px; letter-spacing: .1em; }.chat-orb { position: absolute; right: -34px; bottom: -35px; width: 105px; height: 105px; border: 1px solid var(--accent); border-radius: 50%; background: radial-gradient(circle at 30% 30%, var(--accent), transparent 48%); box-shadow: 0 0 30px rgba(195,239,134,.2); }
.visual-vision { --accent: var(--cyan); }.vision-lens { position: relative; display: grid; width: 196px; height: 196px; place-items: center; border: 1px solid var(--accent); border-radius: 50%; box-shadow: 0 0 0 26px rgba(139,246,241,.025), 0 0 0 27px rgba(139,246,241,.18), 0 0 50px rgba(139,246,241,.2); }.vision-lens::before, .vision-lens::after { position: absolute; content: ''; border: 1px solid rgba(139,246,241,.34); border-radius: 50%; }.vision-lens::before { inset: 18%; }.vision-lens::after { inset: 37%; }.vision-lens span { position: absolute; width: 7px; height: 7px; border-radius: 50%; background: var(--accent); }.vision-lens span:nth-child(1) { top: 17%; left: 31%; }.vision-lens span:nth-child(2) { right: 17%; bottom: 35%; }.vision-lens span:nth-child(3) { bottom: 18%; left: 35%; }.vision-scan { position: absolute; width: 78%; height: 1px; background: linear-gradient(90deg, transparent, var(--accent), transparent); box-shadow: 0 0 18px var(--accent); transform: rotate(-24deg); }.vision-label { position: absolute; right: 0; bottom: 13%; color: var(--muted); font-size: 9px; letter-spacing: .16em; line-height: 1.7; }
.visual-omni { --accent: var(--violet); }.omni-core { position: relative; z-index: 2; display: grid; width: 122px; height: 122px; place-items: center; border: 1px solid var(--accent); border-radius: 50%; color: var(--paper); background: var(--ink-soft); font-family: Georgia, serif; font-size: 6rem; font-weight: 400; line-height: 1; box-shadow: 0 0 45px rgba(173,154,255,.25); }.omni-wave { position: absolute; width: 73%; height: 42%; border: 1px solid rgba(173,154,255,.62); border-radius: 50%; transform: rotate(-25deg); }.wave-two { width: 50%; height: 80%; transform: rotate(27deg); }.wave-three { width: 100%; height: 20%; opacity: .45; transform: rotate(5deg); }.omni-label { position: absolute; bottom: 6%; color: var(--muted); font-size: 9px; letter-spacing: .2em; }
.visual-agent { --accent: var(--pink); }.agent-core { position: relative; z-index: 2; color: var(--paper); font-family: var(--font-display); font-size: clamp(4rem, 9vw, 7.6rem); font-weight: 600; letter-spacing: -.12em; }.agent-core span { color: var(--accent); }.agent-path { position: absolute; width: 42%; height: 27%; border-top: 1px solid rgba(255,125,182,.55); border-right: 1px solid rgba(255,125,182,.55); border-radius: 0 80px 0 0; transform: rotate(-22deg); }.path-two { width: 28%; height: 43%; border-top: 0; border-right: 1px solid rgba(255,125,182,.55); border-bottom: 1px solid rgba(255,125,182,.55); transform: rotate(21deg); }.path-three { width: 70%; height: 63%; border-top: 0; border-right: 0; border-bottom: 1px solid rgba(255,125,182,.4); border-left: 1px solid rgba(255,125,182,.4); transform: rotate(-12deg); }.agent-node { position: absolute; padding: 6px 9px; border: 1px solid rgba(255,125,182,.55); color: var(--muted); background: var(--ink-soft); font-size: 8px; letter-spacing: .14em; }.node-one { top: 12%; right: 7%; }.node-two { right: 11%; bottom: 18%; }.node-three { bottom: 7%; left: 9%; }

.visual-frontier { --accent: var(--cyan); }.frontier-map { position: relative; width: min(100%, 410px); height: 290px; }.frontier-lines { position: absolute; inset: 0; width: 100%; height: 100%; overflow: visible; }.frontier-lines path { fill: none; stroke: rgba(139,246,241,.46); stroke-width: 1; }.frontier-lines circle { fill: var(--accent); filter: drop-shadow(0 0 7px var(--accent)); }.frontier-core { position: absolute; top: 50%; left: 50%; display: flex; width: 116px; height: 116px; align-items: center; justify-content: center; gap: 5px; border: 1px solid rgba(139,246,241,.72); border-radius: 50%; color: var(--paper); background: rgba(0,0,0,.8); box-shadow: 0 0 0 12px rgba(139,246,241,.035), 0 0 48px rgba(139,246,241,.14); transform: translate(-50%, -50%); }.frontier-core span { font-family: var(--font-display); font-size: 17px; letter-spacing: -.08em; }.frontier-core strong { color: var(--accent); font-family: var(--font-display); font-size: 48px; font-weight: 500; letter-spacing: -.1em; }.frontier-node { position: absolute; padding: 7px 10px; border: 1px solid rgba(139,246,241,.32); color: var(--muted); background: rgba(0,0,0,.78); font-size: 8px; letter-spacing: .14em; }.frontier-node-a { top: 12%; left: 7%; }.frontier-node-b { top: 16%; right: 3%; }.frontier-node-c { right: 6%; bottom: 12%; }.frontier-node-d { bottom: 8%; left: 9%; }

.closing { padding-top: 13vh; padding-bottom: 10vh; }.closing-card { position: relative; padding: clamp(36px, 7vw, 90px); border: 1px solid rgba(243,240,234,.2); background: radial-gradient(circle at 86% 24%, rgba(173,154,255,.18), transparent 30%), linear-gradient(135deg, rgba(243,240,234,.07), rgba(243,240,234,.015)); }.closing-card::after { position: absolute; top: -1px; right: 8%; width: 80px; height: 1px; content: ''; background: var(--cyan); }.closing-kicker { margin-bottom: 22px; color: var(--cyan); font-size: 10px; letter-spacing: .2em; }.closing h2 { max-width: 900px; color: var(--paper); font-family: var(--font-display); font-size: clamp(2.4rem, 6vw, 6rem); font-weight: 600; letter-spacing: -.09em; line-height: .99; }.closing h2 em { color: transparent; font-style: normal; -webkit-text-stroke: 1px rgba(243,240,234,.58); }.closing-foot { display: flex; align-items: center; justify-content: space-between; gap: 20px; margin-top: 80px; padding-top: 17px; border-top: 1px solid var(--hairline); color: var(--muted); font-size: 10px; letter-spacing: .15em; }.closing-foot a { color: var(--paper); text-decoration: none; }.closing-foot a:hover { color: var(--cyan); }.sources { display: flex; flex-wrap: wrap; align-items: center; gap: 12px 18px; padding: 24px 0 0; color: var(--muted-soft); font-size: 10px; letter-spacing: .08em; }.sources a { color: var(--muted); text-underline-offset: 3px; }.sources a:hover { color: var(--cyan); }.sources-note { flex-basis: 100%; color: rgba(243,240,234,.27); letter-spacing: .02em; }

@media (max-width: 839px) {
  .atlas-nav { padding: 21px 20px; }.nav-status { display: none; }.chapter-rail { right: 12px; }.rail-dot span { display: none; }.panel-space { width: min(100% - 42px, 650px); }.hero { display: flex; min-height: auto; flex-direction: column; align-items: stretch; gap: 10px; padding-top: 120px; }.hero-copy-wrap { padding-top: 48px; }.hero-title { font-size: clamp(4.2rem, 19vw, 7rem); }.hero-visual { min-height: 460px; margin-top: -12px; }.hero-orb { width: min(74vw, 390px); }.hero-core { width: 150px; }.scroll-cue { bottom: 18px; }.preface { display: block; min-height: 70svh; padding: 12vh 0; }.preface-content { margin-top: 56px; }.preface-line { bottom: 0; }.chapter-inner, .chapter:nth-child(even) .chapter-inner { display: flex; min-height: auto; flex-direction: column; gap: 36px; width: min(100% - 74px, 650px); padding: 130px 0 110px; }.chapter:nth-child(even) .chapter-visual, .chapter:nth-child(even) .chapter-copy { order: initial; }.chapter-visual { width: 100%; min-height: 390px; }.visual-frame { min-height: 380px; }.visual-graphic { min-height: 260px; }.chapter-copy h2 { font-size: clamp(2.3rem, 9vw, 3.8rem); }.chapter-label { margin-top: 28px; }.signal-wrap { margin-top: 32px; }.closing { width: min(100% - 42px, 650px); }.closing-foot { align-items: flex-start; flex-direction: column; margin-top: 50px; }.sources { line-height: 1.7; }.sources-note { flex-basis: 100%; }
}

@media (prefers-reduced-motion: reduce) {
  .gpt-atlas *, .gpt-atlas *::before, .gpt-atlas *::after { scroll-behavior: auto !important; transition-duration: .01ms !important; animation-duration: .01ms !important; animation-iteration-count: 1 !important; }
}
</style>
