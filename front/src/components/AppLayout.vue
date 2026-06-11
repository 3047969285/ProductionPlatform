<script setup>
import { computed } from 'vue'
import { useRoute, useRouter, RouterLink, RouterView } from 'vue-router'
import { clearAuth, getUser } from '../auth'
import api from '../api'

const route = useRoute()
const router = useRouter()
const user = computed(() => getUser())

const links = [
  { to: '/', label: '概览' },
  { to: '/requirements', label: '需求' },
  { to: '/orders', label: '交付任务' },
  { to: '/products', label: '软件产品' },
  { to: '/lines', label: '研发团队' },
  { to: '/quality', label: '测试验收' },
  { to: '/users', label: '用户', admin: true },
]

const visibleLinks = computed(() =>
  links.filter((l) => !l.admin || user.value?.role === 'admin'),
)

async function logout() {
  try { await api.post('/auth/logout') } catch { /* ignore */ }
  clearAuth()
  router.push('/login')
}
</script>

<template>
  <div class="layout">
    <header class="nav">
      <RouterLink to="/" class="brand">DevFlow<span>·</span>研发</RouterLink>
      <nav>
        <RouterLink
          v-for="link in visibleLinks"
          :key="link.to"
          :to="link.to"
          class="link"
          :class="{ active: route.path === link.to }"
        >{{ link.label }}</RouterLink>
      </nav>
      <div class="user">
        <span>{{ user?.nickname || user?.username }}</span>
        <button @click="logout">退出</button>
      </div>
    </header>
    <main><RouterView /></main>
  </div>
</template>

<style scoped>
.layout { min-height: 100vh; }
.nav {
  position: fixed;
  top: 0; left: 0; right: 0;
  z-index: 100;
  height: var(--nav-h);
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 clamp(16px, 4vw, 40px);
  background: rgba(6, 6, 8, 0.85);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border);
}
.brand {
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 1rem;
  text-decoration: none;
  color: var(--text);
  white-space: nowrap;
}
.brand span { color: var(--cyan); }
nav { display: flex; gap: 4px; flex: 1; overflow-x: auto; }
.link {
  padding: 7px 14px;
  font-size: 13px;
  color: var(--muted);
  text-decoration: none;
  border-radius: 999px;
  white-space: nowrap;
  transition: all 0.2s;
}
.link:hover, .link.active { color: var(--text); background: var(--glass); }
.user {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--muted);
  white-space: nowrap;
}
.user button {
  padding: 6px 14px;
  font-size: 12px;
  color: var(--muted);
  background: transparent;
  border: 1px solid var(--border);
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s;
}
.user button:hover { color: var(--text); border-color: rgba(255,255,255,0.2); }
main { padding-top: var(--nav-h); }
@media (max-width: 768px) {
  .user span { display: none; }
}
</style>
