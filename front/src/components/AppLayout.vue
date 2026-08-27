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

const visibleLinks = computed(() => links.filter((link) => !link.admin || user.value?.role === 'admin'))

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
      <RouterLink to="/" class="brand">
        <span class="brand-mark" aria-hidden="true" />
        <span>DevFlow</span>
      </RouterLink>
      <nav aria-label="主导航">
        <RouterLink
          v-for="link in visibleLinks"
          :key="link.to"
          :to="link.to"
          class="link"
          :class="{ active: isActive(link) }"
        >
          {{ link.label }}
        </RouterLink>
      </nav>
      <div class="user">
        <span class="user-name">{{ user?.nickname || user?.username }}</span>
        <button type="button" class="logout" @click="logout">退出</button>
      </div>
    </header>
    <main class="main">
      <RouterView v-slot="{ Component, route: childRoute }">
        <Transition name="page" mode="out-in">
          <component :is="Component" :key="childRoute.fullPath" />
        </Transition>
      </RouterView>
    </main>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  background:
    radial-gradient(circle at 85% 0%, rgba(0, 113, 227, 0.06), transparent 28%),
    radial-gradient(circle at 10% 100%, rgba(88, 86, 214, 0.05), transparent 24%),
    var(--bg);
}

.nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: var(--nav-h);
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 0 clamp(20px, 4vw, 40px);
  background: rgba(251, 251, 253, 0.8);
  backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 1px solid var(--border);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 1.05rem;
  letter-spacing: -0.02em;
  color: var(--text);
  text-decoration: none;
}

.brand-mark {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent), #5ac8fa);
  box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.12);
}

nav {
  display: flex;
  gap: 4px;
  flex: 1;
  overflow-x: auto;
  scrollbar-width: none;
}

nav::-webkit-scrollbar { display: none; }

.link {
  padding: 8px 14px;
  font-size: 14px;
  font-weight: 500;
  color: var(--muted);
  text-decoration: none;
  border-radius: 980px;
  white-space: nowrap;
  transition: color 0.2s var(--ease), background 0.2s var(--ease);
}

.link:hover,
.link.active {
  color: var(--text);
  background: rgba(0, 0, 0, 0.04);
}

.user {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: var(--muted);
}

.logout {
  padding: 7px 14px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text);
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: 980px;
  cursor: pointer;
  transition: background 0.2s var(--ease), border-color 0.2s var(--ease);
}

.logout:hover {
  background: var(--bg-secondary);
  border-color: var(--border-strong);
}

.main {
  padding-top: calc(var(--nav-h) + 12px);
}

@media (max-width: 768px) {
  .user-name { display: none; }
}
</style>
