<template>
  <div class="login-page">
    <div class="login-ring ring-a" aria-hidden="true" />
    <div class="login-ring ring-b" aria-hidden="true" />

    <form class="card animate-scale-in" @submit.prevent="submit">
      <div class="logo-mark" aria-hidden="true" />
      <h1>DevFlow</h1>
      <p class="subtitle">软件公司研发交付管控平台</p>
      <p class="hint">默认账号 admin / admin123</p>

      <el-input v-model="form.username" placeholder="用户名" size="large" />
      <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />

      <button type="submit" class="submit" :disabled="loading">
        {{ loading ? '登录中…' : '登录' }}
      </button>
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
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(circle at 20% 20%, rgba(0, 113, 227, 0.08), transparent 30%),
    radial-gradient(circle at 80% 80%, rgba(88, 86, 214, 0.06), transparent 28%),
    var(--bg);
  position: relative;
  overflow: hidden;
}

.login-ring {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.ring-a {
  width: 420px;
  height: 420px;
  top: -120px;
  right: -80px;
  background: var(--ring);
  animation: ring-pulse 7s ease-in-out infinite;
}

.ring-b {
  width: 320px;
  height: 320px;
  bottom: -100px;
  left: -60px;
  background: radial-gradient(circle, rgba(88, 86, 214, 0.1) 0%, transparent 70%);
  animation: ring-pulse 8s ease-in-out infinite reverse;
}

.card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
  padding: 40px 32px 32px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  backdrop-filter: saturate(180%) blur(24px);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.logo-mark {
  width: 44px;
  height: 44px;
  margin: 0 auto 4px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent), #5ac8fa);
  box-shadow: 0 10px 24px rgba(0, 113, 227, 0.22);
}

h1 {
  font-family: var(--font-display);
  font-size: 2rem;
  font-weight: 700;
  letter-spacing: -0.03em;
  text-align: center;
}

.subtitle {
  text-align: center;
  color: var(--muted);
  font-size: 15px;
}

.hint {
  text-align: center;
  color: var(--muted-light);
  font-size: 13px;
  margin-bottom: 6px;
}

.submit {
  margin-top: 8px;
  padding: 13px 18px;
  border: none;
  border-radius: 980px;
  background: var(--accent);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.25s var(--ease-spring), background 0.2s var(--ease), box-shadow 0.25s var(--ease-spring);
  box-shadow: 0 8px 24px rgba(0, 113, 227, 0.22);
}

.submit:hover:not(:disabled) {
  transform: translateY(-1px);
  background: var(--accent-hover);
}

.submit:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}
</style>
