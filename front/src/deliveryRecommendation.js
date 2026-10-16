function action(key, stage, title, description, tab) {
  return { key, stage, title, description, tab, tone: 'neutral', featured: true }
}

export function getDeliveryRecommendation({
  requirements = [],
  tasks = [],
  sprints = [],
  testPlans = [],
  releases = [],
  unavailable = [],
} = {}) {
  const unavailableCollections = new Set(unavailable)
  const canRead = (...keys) => keys.every((key) => !unavailableCollections.has(key))

  if (!canRead('requirements', 'tasks')) return null

  if (!requirements.length) {
    return action('start-requirements', '需求', '先明确交付目标', '记录第一条需求，让后续任务有清晰的验收依据。', 'req')
  }

  const readyRequirements = requirements.filter((item) => ['approved', 'developing', 'done'].includes(item.status))
  if (!readyRequirements.length) {
    return action('review-requirements', '需求', '完成需求确认', '先把目标与验收范围评审清楚，再进入开发。', 'req')
  }

  if (!tasks.length) {
    return action('break-down-tasks', '开发', '把需求拆成开发任务', `${readyRequirements.length} 项需求已具备开发条件，可继续拆分并分配。`, 'task')
  }

  const pendingTasks = tasks.filter((item) => item.status !== 'done')
  if (pendingTasks.length) {
    if (!canRead('sprints')) return null
    if (!sprints.some((item) => item.status === 'active')) {
      return action('plan-sprint', '开发', '为任务安排一个迭代', `${pendingTasks.length} 项任务待推进，设定本轮交付范围。`, 'sprint')
    }
    return action('continue-development', '开发', '继续推进开发任务', `${pendingTasks.length} 项任务尚未完成，完成后进入验证。`, 'task')
  }

  if (!canRead('testPlans')) return null
  if (!testPlans.length) {
    return action('create-test-plan', '测试', '建立测试计划', '把验收范围变成可执行的测试步骤。', 'test')
  }

  const currentPlan = testPlans.find((item) => item.status === 'running')
    || testPlans.find((item) => item.status === 'draft')
    || testPlans[0]
  const failedCases = Number(currentPlan?.caseFail) || 0
  const pendingCases = Number(currentPlan?.casePending) || 0

  if (failedCases > 0) {
    return action('resolve-test-failures', '测试', '处理验证失败项', `「${currentPlan.name || '当前计划'}」有 ${failedCases} 项未通过，先确认问题再发布。`, 'test')
  }
  if (currentPlan?.status === 'running') {
    return action('continue-testing', '测试', `继续执行：${currentPlan.name || '测试计划'}`, '完成剩余用例并确认测试结果。', 'test')
  }
  if (currentPlan?.status === 'draft') {
    return action('start-testing', '测试', `启动测试：${currentPlan.name || '测试计划'}`, '测试范围已建立，可以开始执行验证。', 'test')
  }
  if (pendingCases > 0) {
    return action('complete-test-cases', '测试', '补齐待执行用例', `「${currentPlan.name || '当前计划'}」还有 ${pendingCases} 项未执行。`, 'test')
  }
  if (testPlans.some((item) => item.status !== 'done')) {
    return action('review-test-plans', '测试', '检查测试计划状态', '仍有计划未完成，确认质量结果后再发布。', 'test')
  }

  if (!canRead('releases')) return null
  if (!releases.length) {
    return action('create-release', '部署', '记录首次发布', '补充版本、目标环境与上线结果，形成可追溯交付记录。', 'release')
  }

  const latestRelease = releases[0]
  if (latestRelease.status === 'rollback') {
    return action('review-rollback', '部署', '复核最近一次回滚', `版本 ${latestRelease.version || '未命名'} 已回滚，确认原因与后续方案。`, 'release')
  }
  if (latestRelease.status === 'planned' || latestRelease.status === 'deploying') {
    return action('follow-release', '部署', '跟进发布状态', `版本 ${latestRelease.version || '未命名'} · ${latestRelease.status === 'planned' ? '等待部署' : '正在部署'}。`, 'release')
  }
  if (latestRelease.status !== 'done') {
    return action('review-release', '部署', '核对最近发布记录', '发布状态尚未确认，检查结果后再规划下一轮。', 'release')
  }

  return action('start-next-cycle', '需求', '开始下一轮交付', `最近版本 ${latestRelease.version || '已上线'} 已记录，回到需求规划下一轮。`, 'req')
}
