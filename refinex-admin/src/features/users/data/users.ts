import { faker } from '@faker-js/faker'

// Set a fixed seed for consistent data generation
faker.seed(67890)

const firstNames = ['伟', '芳', '娜', '敏', '静', '磊', '强', '洋', '勇', '艳']
const lastNames = ['张', '王', '李', '赵', '刘', '陈', '杨', '黄', '周', '吴']

export const users = Array.from({ length: 500 }, () => {
  const firstName = faker.helpers.arrayElement(firstNames)
  const lastName = faker.helpers.arrayElement(lastNames)
  return {
    id: faker.string.uuid(),
    firstName,
    lastName,
    username: `${lastName}${firstName}${faker.number.int({ min: 10, max: 99 })}`,
    email: `user${faker.number.int({ min: 1000, max: 9999 })}@example.com`,
    phoneNumber: faker.phone.number({ style: 'international' }),
    status: faker.helpers.arrayElement([
      'active',
      'inactive',
      'invited',
      'suspended',
    ]),
    role: faker.helpers.arrayElement([
      'superadmin',
      'admin',
      'cashier',
      'manager',
    ]),
    createdAt: faker.date.past(),
    updatedAt: faker.date.recent(),
  }
})
