import { faker } from '@faker-js/faker'

// Set a fixed seed for consistent data generation
faker.seed(12345)

const taskTitles = [
  '修复登录验证码校验问题',
  '优化用户列表分页与筛选',
  '新增应用集成配置项',
  '调整仪表盘数据卡片布局',
  '完善任务导入 CSV 校验',
  '修复深色模式下按钮对比度',
  '增加角色权限校验',
  '优化移动端侧边栏交互',
  '补充接口错误提示文案',
  '重构通知设置表单校验',
]

const taskDescriptions = [
  '请按照需求完成实现，并补充必要的测试与说明。',
  '需要兼容移动端与桌面端，注意键盘操作与可访问性。',
  '优先保证交互一致性，避免影响现有路由与筛选参数。',
]

const assignees = ['张三', '李四', '王五', '赵六', '钱七', '孙八', '周九', '吴十']

export const tasks = Array.from({ length: 100 }, () => {
  const statuses = [
    'todo',
    'in progress',
    'done',
    'canceled',
    'backlog',
  ] as const
  const labels = ['bug', 'feature', 'documentation'] as const
  const priorities = ['low', 'medium', 'high'] as const

  return {
    id: `TASK-${faker.number.int({ min: 1000, max: 9999 })}`,
    title: `${faker.helpers.arrayElement(taskTitles)} #${faker.number.int({ min: 1, max: 99 })}`,
    status: faker.helpers.arrayElement(statuses),
    label: faker.helpers.arrayElement(labels),
    priority: faker.helpers.arrayElement(priorities),
    createdAt: faker.date.past(),
    updatedAt: faker.date.recent(),
    assignee: faker.helpers.arrayElement(assignees),
    description: faker.helpers.arrayElement(taskDescriptions),
    dueDate: faker.date.future(),
  }
})
