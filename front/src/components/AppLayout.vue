<script setup>
import { computed } from 'vue'
import { useRoute, useRouter, RouterLink, RouterView } from 'vue-router'
import { clearAuth, getUser } from '../auth'
import api from '../api'

const route = useRoute()
const router = useRouter()
const user = computed(() => getUser())

const links = [
  { to: '/', label: '交付总览' },
  { to: '/projects', label: '项目', match: '/projects' },
  { to: '/teams', label: '团队' },
  { to: '/reports', label: '报表' },
  { to: '/contest', label: '创意大赛', match: '/contest' },
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
    <main>
      <RouterView v-slot="{ Component }">
        <Transition name="page" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </Transition>
      </RouterView>
    </main>
  </div>
</template>

<style scoped>
.layout { min-height: 100vh; }
.nav {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  height: var(--nav-h); display: flex; align-items: center; gap: 24px;
  padding: 0 clamp(16px, 3vw, 32px);
  background: rgba(245, 245, 247, 0.72);
  backdrop-filter: blur(24px) saturate(1.6);
  -webkit-backdrop-filter: blur(24px) saturate(1.6);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}
.brand {
  font-family: var(--font-display); font-weight: 700; font-size: 1.15rem;
  color: var(--text); text-decoration: none; letter-spacing: -0.02em;
}
nav { display: flex; gap: 4px; flex: 1; overflow-x: auto; }
.link {
  padding: 7px 16px; font-size: 15px; color: rgba(0,0,0,0.55);
  text-decoration: none; border-radius: 980px; white-space: nowrap;
  font-weight: 500; transition: all 0.2s var(--ease);
}
.link:hover { color: var(--text); background: rgba(0, 0, 0, 0.05); }
.link.active { color: #fff; background: var(--accent); }
.user { display: flex; align-items: center; gap: 12px; font-size: 15px; color: rgba(0,0,0,0.55); }
.user button {
  padding: 7px 16px; font-size: 14px; color: var(--text);
  background: #fff; border: 1px solid rgba(0,0,0,0.1); border-radius: 980px;
  cursor: pointer; font-weight: 500; box-shadow: var(--shadow-sm);
  transition: all 0.2s var(--ease);
}
.user button:hover { background: #f0f0f2; }
main { padding-top: var(--nav-h); }
@media (max-width: 768px) { .user span { display: none; } }
</style>
