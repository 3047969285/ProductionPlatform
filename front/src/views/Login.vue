<template>
  <div class="login">
    <div class="bg" aria-hidden="true"><div class="orb o1" /><div class="orb o2" /></div>
    <form class="card" @submit.prevent="submit">
      <p class="tag">DEV DELIVERY</p>
      <h1>DevFlow 研发交付</h1>
      <p class="hint">软件公司研发管控 · 默认 admin / admin123</p>
      <el-input v-model="form.username" placeholder="用户名" size="large" />
      <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
      <button type="submit" class="btn" :disabled="loading">{{ loading ? '登录中…' : '登 录' }}</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'
import { setAuth } from '../auth'

const router = useRouter()
const loading = ref(false)
const form = ref({ username: 'admin', password: 'admin123' })

async function submit() {
  loading.value = true
  try {
    const res = await api.post('/auth/login', form.value)
    setAuth(res.data.token, res.data.user)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
.bg { position: fixed; inset: 0; z-index: -1; pointer-events: none; }
.orb { position: absolute; border-radius: 50%; filter: blur(100px); opacity: 0.35; }
.o1 { width: 400px; height: 400px; background: var(--purple); top: -10%; right: -5%; }
.o2 { width: 300px; height: 300px; background: var(--cyan); bottom: 0; left: -10%; }
.card {
  width: 100%;
  max-width: 380px;
  padding: 40px 32px;
  background: var(--glass);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  backdrop-filter: blur(20px);
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.tag { font-size: 10px; letter-spacing: 0.3em; color: var(--cyan); text-align: center; }
h1 {
  font-family: var(--font-display);
  font-size: 1.75rem;
  font-weight: 800;
  text-align: center;
  margin-bottom: 4px;
}
.hint { font-size: 12px; color: var(--muted); text-align: center; margin-bottom: 8px; }
.btn {
  margin-top: 8px;
  padding: 14px;
  font-weight: 600;
  font-size: 15px;
  color: var(--bg);
  background: linear-gradient(135deg, var(--cyan), var(--purple));
  border: none;
  border-radius: 999px;
  cursor: pointer;
  transition: opacity 0.2s;
}
.btn:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
