import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn } from '../auth'
import AppLayout from '../components/AppLayout.vue'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue'), meta: { guest: true } },
  {
    path: '/',
    component: AppLayout,
    meta: { auth: true },
    children: [
      { path: '', component: () => import('../views/Home.vue') },
      { path: 'requirements', component: () => import('../views/Requirements.vue') },
      { path: 'orders', component: () => import('../views/Orders.vue') },
      { path: 'products', component: () => import('../views/Products.vue') },
      { path: 'lines', component: () => import('../views/Lines.vue') },
      { path: 'quality', component: () => import('../views/Quality.vue') },
      { path: 'users', component: () => import('../views/Users.vue'), meta: { admin: true } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
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
