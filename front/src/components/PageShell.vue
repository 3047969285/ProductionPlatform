<script setup>
defineProps({
  tag: String,
  title: String,
  subtitle: String,
})
</script>

<template>
  <div class="page">
    <header class="head animate-fade-up">
      <div>
        <p v-if="tag" class="tag">{{ tag }}</p>
        <h1>{{ title }}</h1>
        <p v-if="subtitle" class="subtitle">{{ subtitle }}</p>
      </div>
      <div class="actions">
        <slot name="action" />
      </div>
    </header>
    <div class="panel animate-fade-up stagger-1">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.page {
  max-width: var(--content-max);
  margin: 0 auto;
  padding: 28px clamp(20px, 4vw, 40px) 72px;
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 20px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.tag {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--accent);
  margin-bottom: 8px;
}

h1 {
  font-family: var(--font-display);
  font-size: clamp(2rem, 4vw, 2.75rem);
  font-weight: 700;
  letter-spacing: -0.035em;
  line-height: 1.08;
}

.subtitle {
  margin-top: 10px;
  max-width: 40rem;
  font-size: 1.05rem;
  color: var(--muted);
}

.actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.panel {
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: clamp(12px, 2vw, 20px);
}
</style>
