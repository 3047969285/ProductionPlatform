<script setup>
import { onBeforeUnmount, onMounted } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'

const route = useRoute()
const previousTitle = document.title
const nav = [
  { to: '/contest', label: '主场' },
  { to: '/contest/works', label: '作品' },
  { to: '/contest/entry', label: '参赛' },
]
onMounted(() => { document.title = '未完成 · 创意大赛 2026' })
onBeforeUnmount(() => { document.title = previousTitle })
</script>

<template>
  <div class="contest-shell">
    <header class="contest-header">
      <RouterLink to="/contest" class="contest-brand" aria-label="未完成 2026 创意大赛首页">
        <span class="brand-mark">未</span>
        <span>未完成 <small>CREATIVE AWARD 2026</small></span>
      </RouterLink>
      <nav class="contest-nav" aria-label="大赛导航">
        <RouterLink v-for="item in nav" :key="item.to" :to="item.to" :class="{ active: route.path === item.to }">{{ item.label }}</RouterLink>
      </nav>
      <RouterLink class="header-cta" to="/contest/entry">提交作品 <span>↗</span></RouterLink>
    </header>

    <RouterView v-slot="{ Component }">
      <Transition name="contest-page" mode="out-in">
        <component :is="Component" :key="route.path" />
      </Transition>
    </RouterView>

    <footer class="contest-footer">
      <span>未完成 · 创意大赛 2026</span>
      <span>留白处，答案正在发生。</span>
      <span>© 2026</span>
    </footer>
  </div>
</template>

<style>
.contest-shell {
  --contest-ink: #191a1a;
  --contest-muted: #777873;
  --contest-paper: #f2f1ec;
  --contest-line: rgba(25, 26, 26, .14);
  min-height: 100vh;
  color: var(--contest-ink);
  background: var(--contest-paper);
  font-family: "Helvetica Neue", "PingFang SC", "Microsoft YaHei", sans-serif;
  letter-spacing: -.015em;
}
.contest-shell * { box-sizing: border-box; }
.contest-header {
  position: relative;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 76px;
  padding: 0 clamp(22px, 5.3vw, 84px);
  border-bottom: 1px solid var(--contest-line);
}
.contest-brand { display: inline-flex; align-items: center; gap: 11px; color: inherit; text-decoration: none; font-size: 14px; font-weight: 650; letter-spacing: -.04em; }
.contest-brand small { display: block; margin-top: 3px; color: var(--contest-muted); font-size: 8px; font-weight: 550; letter-spacing: .16em; }
.brand-mark { display: grid; width: 32px; height: 32px; place-items: center; border: 1px solid currentColor; border-radius: 50%; font-family: Georgia, serif; font-size: 17px; font-weight: 400; }
.contest-nav { display: flex; align-items: center; gap: clamp(22px, 4vw, 54px); }
.contest-nav a { position: relative; color: #777873; text-decoration: none; font-size: 12px; transition: color .2s ease; }
.contest-nav a:hover, .contest-nav a.active { color: var(--contest-ink); }
.contest-nav a.active::after { position: absolute; right: 0; bottom: -10px; left: 0; height: 1px; background: currentColor; content: ""; }
.header-cta { display: inline-flex; align-items: center; gap: 18px; padding: 11px 15px; border: 1px solid var(--contest-line); border-radius: 2px; color: var(--contest-ink); text-decoration: none; font-size: 11px; transition: background .2s ease, color .2s ease; }
.header-cta:hover { background: var(--contest-ink); color: var(--contest-paper); }
.header-cta span { font-size: 14px; }
.contest-footer { display: flex; justify-content: space-between; gap: 20px; padding: 22px clamp(22px, 5.3vw, 84px); border-top: 1px solid var(--contest-line); color: var(--contest-muted); font-size: 9px; letter-spacing: .04em; }
.contest-page-enter-active, .contest-page-leave-active { transition: opacity .22s ease, transform .22s ease; }
.contest-page-enter-from { opacity: 0; transform: translateY(8px); }
.contest-page-leave-to { opacity: 0; transform: translateY(-5px); }
@media (max-width: 680px) {
  .contest-header { min-height: 66px; padding: 0 18px; }
  .contest-nav { gap: 16px; }
  .contest-nav a { font-size: 11px; }
  .header-cta { gap: 6px; padding: 9px; font-size: 10px; }
  .contest-footer { flex-wrap: wrap; padding: 18px; }
  .contest-footer span:nth-child(2) { order: 3; width: 100%; }
}
@media (max-width: 440px) {
  .contest-header { flex-wrap: wrap; gap: 12px; padding-top: 13px; padding-bottom: 12px; }
  .contest-nav { order: 3; width: 100%; justify-content: center; }
  .contest-nav a.active::after { bottom: -5px; }
  .header-cta { margin-left: auto; }
}
</style>
