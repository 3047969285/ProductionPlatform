<script setup>
defineProps({
  tag: String,
  title: String,
})
</script>

<template>
  <div class="page">
    <header class="head">
      <div>
        <p v-if="tag" class="tag">{{ tag }}</p>
        <h1>{{ title }}</h1>
      </div>
      <slot name="action" />
    </header>
    <div class="panel"><slot /></div>
  </div>
</template>

<style scoped>
.page {
  max-width: 1440px;
  margin: 0 auto;
  padding: 32px clamp(20px, 3vw, 40px) 64px;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 28px;
  flex-wrap: wrap;
}
.tag { font-size: 12px; font-weight: 600; letter-spacing: 0.08em; color: var(--accent); margin-bottom: 8px; }
h1 { font-family: var(--font-display); font-size: clamp(1.9rem, 3.2vw, 2.6rem); font-weight: 700; letter-spacing: -0.025em; }
.panel {
  background: var(--glass);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: var(--radius-lg);
  padding: 20px;
  backdrop-filter: blur(16px) saturate(1.3);
  -webkit-backdrop-filter: blur(16px) saturate(1.3);
  box-shadow: var(--shadow-sm);
}
</style>