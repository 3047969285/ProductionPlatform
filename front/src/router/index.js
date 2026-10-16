import { createRouter, createWebHistory } from 'vue-router'
import { getUser, isLoggedIn } from '../auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', component: () => import('../views/Login.vue'), meta: { guest: true } },
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
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

router.beforeEach((to) => {
  if (to.meta.auth && !isLoggedIn()) return '/login'
  if (to.meta.guest && isLoggedIn()) return '/'
  if (to.meta.admin && getUser()?.role !== 'admin') return '/'
  return true
})

export default router
