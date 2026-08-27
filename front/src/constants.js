export const projectTabs = {
  overview: '概览',
  req: '需求',
  api: '接口',
  test: '测试',
  ops: '运维',
}

export const roles = { admin: '管理员', developer: '开发者' }
export const teamStatus = { active: '活跃', inactive: '休整' }

export const reqStatus = {
  draft: '草稿', review: '评审中', approved: '已通过',
  developing: '开发中', done: '已实现', rejected: '已拒绝',
}
export const reqPriority = { high: '高', medium: '中', low: '低' }
export const apiStatus = { draft: '草稿', review: '评审中', published: '已发布', deprecated: '已废弃' }
export const testStatus = { pending: '待测', running: '测试中', done: '已完成' }
export const opsStatus = { open: '待处理', processing: '处理中', resolved: '已解决' }
export const opsSeverity = { low: '低', medium: '中', high: '高', critical: '紧急' }
export const httpMethods = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH']

export function badgeClass(type, value) {
  const map = {
    team: { active: 'lime', inactive: 'muted' },
    req: { draft: 'muted', review: 'cyan', approved: 'purple', developing: 'cyan', done: 'lime', rejected: 'pink' },
    api: { draft: 'muted', review: 'cyan', published: 'lime', deprecated: 'pink' },
    test: { pending: 'muted', running: 'cyan', done: 'lime' },
    ops: { open: 'pink', processing: 'cyan', resolved: 'lime' },
    severity: { low: 'muted', medium: 'cyan', high: 'pink', critical: 'pink' },
    priority: { high: 'pink', medium: 'muted', low: 'muted' },
  }
  return map[type]?.[value] || 'muted'
}

export function buildFolderTree(folders) {
  const map = {}
  const roots = []
  folders.forEach((f) => { map[f.id] = { ...f, label: f.name, children: [] } })
  folders.forEach((f) => {
    if (f.parentId && map[f.parentId]) map[f.parentId].children.push(map[f.id])
    else roots.push(map[f.id])
  })
  return roots
}
