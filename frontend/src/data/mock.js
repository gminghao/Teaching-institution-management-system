import heroCampus from '@/assets/reference/hero-campus.jpg'
import courseAlgorithms from '@/assets/reference/course-algorithms.jpg'
import courseFinance from '@/assets/reference/course-finance.jpg'
import courseMl from '@/assets/reference/course-ml.jpg'
import coursePhilosophy from '@/assets/reference/course-philosophy.jpg'
import courseHealthcare from '@/assets/reference/course-healthcare.jpg'
import courseArchitecture from '@/assets/reference/course-architecture.jpg'
import adminProfile from '@/assets/reference/admin-profile.jpg'

export const referenceImages = {
  heroCampus,
  adminProfile
}

export const mockCourses = [
  {
    id: 'cs-101',
    title: '高级算法与数据结构',
    description: '掌握高级软件工程角色必不可少的复杂问题解决技术。',
    category: '计算机科学',
    duration: '12 周',
    instructor: {
      name: '艾伦·图灵博士',
      initials: 'AT'
    },
    price: '¥8,500',
    registrationFee: '¥800',
    format: '在线',
    image: courseAlgorithms,
    popular: true
  },
  {
    id: 'bus-201',
    title: '战略公司财务',
    description: '全面分析现代企业的财务战略、估值和资本配置。',
    category: '工商管理',
    duration: '8 周',
    instructor: {
      name: '伊丽莎白·沃伦教授',
      initials: 'EW'
    },
    price: '¥6,800',
    registrationFee: '¥680',
    format: '混合',
    image: courseFinance,
    popular: true
  },
  {
    id: 'data-301',
    title: '预测建模与机器学习',
    description: '培养使用大型数据集预测未来趋势的模型构建基础技能。',
    category: '数据分析',
    duration: '16 周',
    instructor: {
      name: '杰弗里·辛顿博士',
      initials: 'GH'
    },
    price: '¥10,200',
    registrationFee: '¥1,000',
    format: '在线',
    image: courseMl,
    popular: true
  },
  {
    id: 'hum-401',
    title: '现代哲学论述',
    description: '探索当代伦理框架及其在当今数字社会中的应用。',
    category: '人文艺术',
    duration: '10 周',
    instructor: {
      name: '让-保罗·萨特教授',
      initials: 'JS'
    },
    price: '¥5,600',
    registrationFee: '¥560',
    format: '在校',
    image: coursePhilosophy
  },
  {
    id: 'med-501',
    title: '医疗系统管理',
    description: '研究管理大型医疗网络和设施的运营复杂性。',
    category: '医疗管理',
    duration: '14 周',
    instructor: {
      name: '保罗·法默博士',
      initials: 'PF'
    },
    price: '¥7,800',
    registrationFee: '¥780',
    format: '混合',
    image: courseHealthcare
  },
  {
    id: 'des-601',
    title: '可持续建筑原则',
    description: '将环境科学与先进结构设计相结合，用于现代环保开发。',
    category: '设计',
    duration: '12 周',
    instructor: {
      name: '扎哈·哈迪德教授',
      initials: 'ZH'
    },
    price: '¥9,500',
    registrationFee: '¥950',
    format: '在线',
    image: courseArchitecture
  }
]

export const mockTransactions = [
  { id: 'ORD-2412-001', date: '2024-12-01 10:23', name: '张伟', amount: '¥15,000.00', method: '银行转账', status: 'completed', operator: '系统自动' },
  { id: 'ORD-2412-002', date: '2024-12-01 09:45', name: '李娜', amount: '¥22,500.00', method: '微信支付', status: 'completed', operator: '系统自动' },
  { id: 'ORD-2411-089', date: '2024-11-30 16:20', name: '王强', amount: '¥8,000.00', method: '支付宝', status: 'pending', operator: '陈管理员' },
  { id: 'ORD-2411-088', date: '2024-11-30 14:15', name: '赵雪', amount: '¥12,000.00', method: '信用卡', status: 'failed', operator: '系统自动' }
]

export const mockEnrollments = [
  { orderNo: '#ENR-4920', studentName: 'Eleanor Vance', courseTitle: 'Advanced Data Structures', date: 'Oct 24, 2024', enrollmentStatus: 'Enrolled', paymentStatus: '已结清', amount: '$1,200.00', paid: '$1,200.00' },
  { orderNo: '#ENR-4919', studentName: 'Theodora Crain', courseTitle: 'Introduction to Psychology', date: 'Oct 24, 2024', enrollmentStatus: 'Pending', paymentStatus: '部分', amount: '$850.00', paid: '$400.00' },
  { orderNo: '#ENR-4918', studentName: 'Luke Sanderson', courseTitle: 'Macroeconomics 101', date: 'Oct 23, 2024', enrollmentStatus: 'Enrolled', paymentStatus: '未交', amount: '$1,500.00', paid: '$0.00' },
  { orderNo: '#ENR-4917', studentName: 'Steven Dudley', courseTitle: 'Creative Writing Workshop', date: 'Oct 23, 2024', enrollmentStatus: 'Enrolled', paymentStatus: '已结清', amount: '$920.00', paid: '$920.00' },
  { orderNo: '#ENR-4916', studentName: 'Shirley Jackson', courseTitle: 'Modern American Literature', date: 'Oct 22, 2024', enrollmentStatus: 'Failed', paymentStatus: '未交', amount: '$760.00', paid: '$0.00' }
]

export const courseCategories = ['全部课程', '计算机科学', '工商管理', '人文艺术', '数据分析', '医疗管理', '设计']
