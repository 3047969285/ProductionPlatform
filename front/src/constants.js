export const orderStatus = { pending: '待启动', running: '开发中', done: '已交付' }
export const lineStatus = { active: '活跃', inactive: '休整' }
export const qualityResult = { pass: '验收通过', fail: '验收未过' }
export const roles = { admin: '管理员', operator: '开发者' }

export const reqStatus = {
  draft: '草稿',
  review: '评审中',
  approved: '已通过',
  developing: '开发中',
  done: '已实现',
  rejected: '已拒绝',
}

export const reqPriority = { high: '高', medium: '中', low: '低' }

export function badgeClass(type, value) {
  const map = {
    order: { pending: 'muted', running: 'cyan', done: 'lime' },
    line: { active: 'lime', inactive: 'muted' },
    quality: { pass: 'lime', fail: 'pink' },
    req: {
      draft: 'muted',
      review: 'cyan',
      approved: 'purple',
      developing: 'cyan',
      done: 'lime',
      rejected: 'pink',
    },
    priority: { high: 'pink', medium: 'muted', low: 'muted' },
  }
  return map[type]?.[value] || 'muted'
}
