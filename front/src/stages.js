// 五段式交付流程定义：项目详情页与全局命令面板共用同一份数据源。
// 每个阶段保留自己的工作台，避免把所有能力堆在一个菜单里。
export const stageDefinitions = [
  {
    key: 'flow',
    label: '流程',
    short: '项目全貌',
    description: '项目全貌 / 迭代 / 成员',
    tabs: [
      { key: 'cockpit', label: '交付驾驶舱', icon: '◈' },
      { key: 'sprint', label: '迭代计划', icon: '↻' },
      { key: 'member', label: '项目成员', icon: '◎' },
    ],
  },
  {
    key: 'requirement',
    label: '需求',
    short: '目标与接口',
    description: '目标 / 需求 / 接口',
    tabs: [
      { key: 'req', label: '需求文档', icon: '▤' },
      { key: 'api', label: '接口文档', icon: '↔' },
    ],
  },
  {
    key: 'development',
    label: '开发',
    short: '任务与缺陷',
    description: '任务 / 看板 / 缺陷',
    tabs: [
      { key: 'task', label: '开发任务', icon: '✓' },
      { key: 'kanban', label: '研发看板', icon: '▦' },
      { key: 'bug', label: '缺陷处理', icon: '!' },
    ],
  },
  {
    key: 'testing',
    label: '测试',
    short: '用例与计划',
    description: '用例 / 计划 / 质量',
    tabs: [
      { key: 'test', label: '用例与计划', icon: '◇' },
      { key: 'test-items', label: '验证任务', icon: '✓' },
    ],
  },
  {
    key: 'deployment',
    label: '部署',
    short: '版本与线上',
    description: '版本 / 环境 / 运维',
    tabs: [
      { key: 'release', label: '发布记录', icon: '↑' },
      { key: 'ops', label: '运维问题', icon: '⌁' },
    ],
  },
]

export const stageKeys = new Set(stageDefinitions.map((stage) => stage.key))
export const validTabs = new Set(stageDefinitions.flatMap((stage) => stage.tabs.map((item) => item.key)))

export function stageForTab(name) {
  return stageDefinitions.find((stage) => stage.tabs.some((item) => item.key === name)) || stageDefinitions[0]
}
