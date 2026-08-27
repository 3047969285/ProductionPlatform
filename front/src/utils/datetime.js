/**
 * 将后端 LocalDateTime 字符串格式化为 yyyy-MM-dd HH:mm。
 *
 * @param {string|null|undefined} value ISO 或空格分隔的时间
 * @returns {string} 可读时间或占位符
 */
export function formatDateTime(value) {
  if (!value) return '-'
  const text = String(value).replace('T', ' ')
  return text.length > 16 ? text.slice(0, 16) : text
}
