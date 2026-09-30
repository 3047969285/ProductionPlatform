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

// ── 新模块状态字典 ──────────────────────────────────────────
export const taskStatus = { todo: '待办', doing: '进行中', done: '已完成' }
export const bugStatus = { open: '待处理', fixing: '修复中', resolved: '已修复', closed: '已关闭', reopened: '重新打开' }
export const bugSeverity = { low: '低', medium: '中', high: '高', critical: '紧急' }
export const sprintStatus = { planning: '规划中', active: '进行中', closed: '已结束' }
export const memberRole = { admin: '管理员', pm: '项目经理', developer: '开发者', viewer: '观察者' }
export const testCaseStatus = { active: '启用', disabled: '停用' }
export const testPlanStatus = { draft: '草稿', running: '执行中', done: '已完成' }
export const execStatus = { pending: '待执行', pass: '通过', fail: '失败', blocked: '阻塞' }
export const milestoneStatus = { pending: '未开始', in_progress: '进行中', done: '已完成', overdue: '已逾期' }
export const releaseStatus = { planned: '计划中', deploying: '部署中', done: '已上线', rollback: '已回滚' }
export const releaseEnv = { dev: '开发', test: '测试', staging: '预发', prod: '生产' }

export function badgeClass(type, value) {
  const task = { todo: 'muted', doing: 'cyan', done: 'lime' }
  const bug = { open: 'pink', fixing: 'cyan', resolved: 'lime', closed: 'muted', reopened: 'pink' }
  const sprint = { planning: 'muted', active: 'cyan', closed: 'lime' }
  const member = { admin: 'pink', pm: 'purple', developer: 'cyan', viewer: 'muted' }
  const tcase = { active: 'lime', disabled: 'muted' }
  const tplan = { draft: 'muted', running: 'cyan', done: 'lime' }
  const exec = { pending: 'muted', pass: 'lime', fail: 'pink', blocked: 'pink' }
  const ms = { pending: 'muted', in_progress: 'cyan', done: 'lime', overdue: 'pink' }
  const rel = { planned: 'muted', deploying: 'cyan', done: 'lime', rollback: 'pink' }
  const env = { dev: 'muted', test: 'cyan', staging: 'purple', prod: 'lime' }
  const map = {
    task, bug, sprint, member, tcase, tplan, exec, ms, rel, env,
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
