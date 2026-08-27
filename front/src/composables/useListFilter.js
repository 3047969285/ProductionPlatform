import { computed, ref, watch } from 'vue'

/**
 * 客户端列表筛选与分页，适用于已加载的全量数据。
 *
 * @param source 原始列表 ref
 * @param options 搜索字段、状态字段与分页大小
 * @returns 筛选/分页相关的响应式状态
 */
export function useListFilter(source, options = {}) {
  const keyword = ref('')
  const status = ref('')
  const page = ref(1)
  const pageSize = ref(options.pageSize ?? 10)
  const searchFields = options.searchFields ?? ['title']
  const statusField = options.statusField ?? 'status'

  const filtered = computed(() => {
    let rows = source.value ?? []
    const query = keyword.value.trim().toLowerCase()
    if (query) {
      rows = rows.filter((row) =>
        searchFields.some((field) => String(row[field] ?? '').toLowerCase().includes(query)),
      )
    }
    if (status.value) {
      rows = rows.filter((row) => row[statusField] === status.value)
    }
    return rows
  })

  const total = computed(() => filtered.value.length)

  const paged = computed(() => {
    const start = (page.value - 1) * pageSize.value
    return filtered.value.slice(start, start + pageSize.value)
  })

  watch([keyword, status], () => {
    page.value = 1
  })

  return { keyword, status, page, pageSize, filtered, paged, total }
}
