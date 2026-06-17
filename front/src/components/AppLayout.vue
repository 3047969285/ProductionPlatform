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
  { to: '/projects', label: '项目', match: '/projects' },
  { to: '/teams', label: '团队' },
  { to: '/users', label: '用户', admin: true },
]

const visibleLinks = computed(() => links.filter((l) => !l.admin || user.value?.role === 'admin'))

function isActive(link) {
  if (link.match) return route.path.startsWith(link.match)
  return route.path === link.to
}

async function logout() {
  try { await api.post('/auth/logout') } catch { /* ignore */ }
  clearAuth()
  router.push('/login')
}
</script>

<template>
  <div class="layout">
    <header class="nav">
      <RouterLink to="/" class="brand">DevFlow</RouterLink>
      <nav>
        <RouterLink v-for="l in visibleLinks" :key="l.to" :to="l.to" class="link" :class="{ active: isActive(l) }">{{ l.label }}</RouterLink>
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
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  height: var(--nav-h); display: flex; align-items: center; gap: 20px;
  padding: 0 clamp(16px, 4vw, 40px);
  background: rgba(6, 6, 8, 0.85); backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border);
}
.brand { font-family: var(--font-display); font-weight: 800; font-size: 1.25rem; color: var(--text); text-decoration: none; }
nav { display: flex; gap: 6px; flex: 1; overflow-x: auto; }
.link { padding: 9px 18px; font-size: 16px; color: var(--muted); text-decoration: none; border-radius: 999px; white-space: nowrap; }
.link:hover, .link.active { color: var(--text); background: var(--glass); }
.user { display: flex; align-items: center; gap: 12px; font-size: 15px; color: var(--muted); }
.user button { padding: 8px 16px; font-size: 14px; color: var(--muted); background: transparent; border: 1px solid var(--border); border-radius: 999px; cursor: pointer; }
main { padding-top: var(--nav-h); }
@media (max-width: 768px) { .user span { display: none; } }
</style>
