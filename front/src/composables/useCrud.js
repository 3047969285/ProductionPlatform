import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

/** REST 风格 CRUD：GET /path  DELETE /path/{id} */
export function useCrud(path) {
  const list = ref([])
  const loading = ref(false)

  async function load() {
    loading.value = true
    try {
      list.value = (await api.get(path)).data
    } catch (e) {
      ElMessage.error(e.message)
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
      if (e !== 'cancel') ElMessage.error(e.message)
    }
  }

  return { list, loading, load, remove }
}
