import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

/** REST 风格 CRUD：GET /path  DELETE /path/{id} */
export function useCrud(path) {
  const list = ref([])
  const loading = ref(false)
  const loadError = ref(false)

  async function load() {
    loading.value = true
    loadError.value = false
    try {
      const response = await api.get(path)
      list.value = Array.isArray(response.data) ? response.data : []
    } catch {
      loadError.value = true
    } finally {
      loading.value = false
    }
  }

  async function remove(id) {
    try {
      await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
      await api.delete(`${path}/${id}`)
      ElMessage.success('已删除')
      await load()
    } catch (e) {
      if (e !== 'cancel' && e !== 'close') ElMessage.error(e?.message || '删除失败')
    }
  }

  return { list, loading, load, remove, loadError }
}
