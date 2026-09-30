<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { gsap } from 'gsap'
import api from '../api'
import { setAuth } from '../auth'

const router = useRouter()
const root = ref(null)
const loading = ref(false)
const form = ref({ username: 'admin', password: 'admin123' })
let context

async function submit() {
  loading.value = true
  try {
    const res = await api.post('/auth/login', form.value)
    setAuth(res.data.token, res.data.user)
    ElMessage.success('已进入')
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (!root.value) return
  context = gsap.context(() => {
    const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    gsap.from('.login-intro, .login-card', {
      autoAlpha: 0,
      y: reduced ? 0 : 18,
      duration: reduced ? 0 : .7,
      stagger: reduced ? 0 : .12,
      ease: 'power3.out',
      clearProps: 'all',
    })
    if (!reduced) {
      gsap.to('.login-orbit', { rotation: 360, duration: 36, repeat: -1, ease: 'none' })
      gsap.to('.login-beam', { xPercent: 100, duration: 5, repeat: -1, ease: 'none' })
    }
  }, root.value)
})

onUnmounted(() => context?.revert())
</script>

<template>
  <div ref="root" class="login">
    <div class="login-grid" aria-hidden="true"><span class="login-orbit" /><span class="login-beam" /></div>
    <section class="login-intro">
      <p class="login-index">00 / ACCESS</p>
      <h1>DevFlow<span>.</span></h1>
      <p>研发交付系统</p>
    </section>
    <form class="login-card" @submit.prevent="submit">
      <header class="card-top"><p>01 / IDENTITY</p><span>↗</span></header>
      <label>账号<el-input v-model="form.username" placeholder="输入账号" size="large" /></label>
      <label>密码<el-input v-model="form.password" type="password" placeholder="输入密码" size="large" show-password /></label>
      <button type="submit" class="btn" :disabled="loading">{{ loading ? '验证中…' : '进入系统 ↗' }}</button>
      <footer><span>DEVFLOW</span><span>SECURE ENTRY</span></footer>
    </form>
  </div>
</template>

<style scoped>
.login {
  position: relative; display: grid; grid-template-columns: minmax(220px, .8fr) minmax(320px, 430px); align-items: center; gap: clamp(48px, 10vw, 160px);
  min-height: 100svh; max-width: 1320px; margin: 0 auto; padding: 80px clamp(24px, 6vw, 84px); overflow: hidden;
}
.login-grid { position: fixed; inset: 0; pointer-events: none; overflow: hidden; }
.login-grid::before {
  position: absolute; top: -18vw; right: -7vw; width: 58vw; height: 58vw; max-width: 760px; max-height: 760px; border: 1px solid rgba(203, 210, 118, .14); border-radius: 50%; content: "";
  box-shadow: 0 0 0 62px rgba(203, 210, 118, .025), 0 0 0 124px rgba(203, 210, 118, .018);
}
.login-grid::before, .login-orbit, .login-beam { display: none; }
.login-orbit { position: absolute; top: 12vh; right: 15vw; width: 10px; height: 10px; border: 1px solid var(--accent); border-radius: 50%; box-shadow: 0 0 0 5px rgba(203, 210, 118, .12); }
.login-beam { position: absolute; top: 34%; left: -20%; width: 40%; height: 1px; background: linear-gradient(90deg, transparent, var(--accent), transparent); opacity: .35; }
.login-intro { align-self: center; }
.login-index, .card-top p { color: var(--accent); font-size: 10px; letter-spacing: .2em; }
.login-index { margin-bottom: 28px; }
.login-intro h1 { font-family: var(--font-display); font-size: clamp(3.8rem, 8vw, 7.5rem); font-weight: 400; letter-spacing: -.08em; line-height: .86; }
.login-intro h1 span { color: var(--accent); }
.login-intro > p:last-child { margin-top: 28px; color: var(--muted); font-size: 13px; letter-spacing: .18em; }
.login-card {
  display: flex; flex-direction: column; gap: 18px; padding: clamp(24px, 4vw, 42px);
  background: rgba(12, 12, 12, .9); border: 1px solid var(--border-strong); border-radius: 16px; box-shadow: 0 32px 90px rgba(0,0,0,.5); backdrop-filter: blur(20px);
}
.card-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.card-top span { color: var(--muted-light); font-size: 18px; }
.login-card label { display: flex; flex-direction: column; gap: 7px; color: var(--muted); font-size: 11px; letter-spacing: .08em; }
.btn { margin-top: 8px; padding: 14px 16px; color: #11140f; background: var(--accent); border: 1px solid var(--accent); border-radius: var(--radius); cursor: pointer; font-size: 13px; letter-spacing: .08em; transition: background .25s var(--ease), transform .25s var(--ease), box-shadow .25s var(--ease); }
.btn:hover:not(:disabled) { background: #fff; box-shadow: 0 10px 26px rgba(239, 239, 235, .16); transform: translateY(-2px); }
.btn:disabled { cursor: not-allowed; opacity: .55; }
.login-card footer { display: flex; justify-content: space-between; margin-top: 10px; color: var(--muted-light); font-size: 9px; letter-spacing: .16em; }
@media (max-width: 700px) {
  .login { grid-template-columns: 1fr; gap: 42px; align-content: center; padding-top: 40px; padding-bottom: 40px; }
  .login-intro h1 { font-size: clamp(3.4rem, 17vw, 5rem); }
  .login-index { margin-bottom: 18px; }
  .login-intro > p:last-child { margin-top: 17px; }
}
</style>
