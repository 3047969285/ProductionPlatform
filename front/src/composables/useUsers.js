import { ref } from 'vue'
import api from '../api'

let cachedUsers = null

/**
 * 加载并缓存系统用户，用于负责人/提出人下拉选择。
 */
export function useUsers() {
  const users = ref(cachedUsers ?? [])
  const loading = ref(false)

  /**
   * 拉取用户列表，多次调用复用缓存。
   *
   * @returns {Promise<Array>} 用户数组
   */
  async function loadUsers() {
    if (cachedUsers) {
      users.value = cachedUsers
      return cachedUsers
    }
    loading.value = true
    try {
      const response = await api.get('/users')
      cachedUsers = response.data ?? []
      users.value = cachedUsers
      return cachedUsers
    } finally {
      loading.value = false
    }
  }

  /**
   * 生成下拉选项。
   *
   * @param {Array} list 用户列表
   * @returns {Array<{label:string,value:string}>}
   */
  function toOptions(list = users.value) {
    return list.map((user) => {
      const label = user.nickname || user.username
      return { label, value: label }
    })
  }

  return { users, loading, loadUsers, toOptions }
}
