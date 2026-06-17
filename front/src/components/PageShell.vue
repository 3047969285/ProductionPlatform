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
  max-width: 1200px;
  margin: 0 auto;
  padding: 36px clamp(20px, 5vw, 48px) 80px;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}
.tag { font-size: 12px; letter-spacing: 0.3em; color: var(--cyan); margin-bottom: 8px; }
h1 { font-family: var(--font-display); font-size: clamp(2rem, 4vw, 2.5rem); font-weight: 800; }
.panel {
  background: var(--glass);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 8px;
  backdrop-filter: blur(12px);
}
</style>
