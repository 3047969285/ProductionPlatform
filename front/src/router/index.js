import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn } from '../auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', component: () => import('../views/Login.vue'), meta: { guest: true } },
    { path: '/gpt-history', name: 'gpt-history', component: () => import('../views/GptHistory.vue') },
    {
      path: '/contest',
      component: () => import('../components/ContestLayout.vue'),
      children: [
        { path: '', name: 'contest-home', component: () => import('../views/contest/ContestHome.vue') },
        { path: 'works', name: 'contest-works', component: () => import('../views/contest/ContestWorks.vue') },
        { path: 'entry', name: 'contest-entry', component: () => import('../views/contest/ContestEntry.vue') },
      ],
    },
    {
      path: '/',
      component: () => import('../components/AppLayout.vue'),
      meta: { auth: true },
      children: [
        { path: '', component: () => import('../views/Home.vue') },
        { path: 'projects', component: () => import('../views/Projects.vue') },
        { path: 'projects/:id', component: () => import('../views/ProjectDetail.vue') },
        { path: 'teams', component: () => import('../views/Teams.vue') },
        { path: 'reports', component: () => import('../views/Reports.vue') },
        { path: 'users', component: () => import('../views/Users.vue'), meta: { admin: true } },
      ],
    },
  ],
})

router.beforeEach((to) => {
  if (to.meta.auth && !isLoggedIn()) return '/login'
  if (to.meta.guest && isLoggedIn()) return '/'
  if (to.meta.admin) {
    const user = JSON.parse(localStorage.getItem('user') || 'null')
    if (user?.role !== 'admin') return '/'
  }
  return true
})

export default router
