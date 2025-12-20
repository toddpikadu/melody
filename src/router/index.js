import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../components/LoginView.vue'
import TestView from '../components/TestView.vue'

// 定义路由
const routes = [
  {
    path: '/',
    name: 'Root',
    redirect: '/login' // 重定向到登录页面
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/test',
    name: 'Test',
    component: TestView
  },
  // 可以添加其他路由
  {
    path: '/home',
    name: 'Home',
    // 懒加载组件
    component: () => import('../components/HomeView.vue'),
    // 添加路由守卫，需要登录才能访问
    meta: {
      requiresAuth: true
    },
    redirect: '/home/workpoint-approval', // 默认重定向到工分核准模块
    children: [
      {
        path: 'workpoint-approval',
        name: 'WorkpointApproval',
        // 懒加载组件
        component: () => import('../components/WorkPointApproval.vue')
      },
      {
        path: 'process-management',
        name: 'ProcessManagement',
        // 懒加载组件
        component: () => import('../components/ProcessManagement.vue')
      },
      {
        path: 'production-plan',
        name: 'ProductionPlan',
        // 懒加载组件
        component: () => import('../components/ProductionPlan.vue')
      },
      {
        path: 'report-statistics',
        name: 'ReportStatistics',
        // 懒加载组件
        component: () => import('../components/ReportStatistics.vue')
      }
    ]
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 添加路由守卫
router.beforeEach((to, from, next) => {
  // 检查路由是否需要认证
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // 检查是否已登录
    const token = localStorage.getItem('token')
    if (!token) {
      // 未登录，重定向到登录页面
      next({ name: 'Login' })
    } else {
      // 已登录，继续访问
      next()
    }
  } else {
    // 不需要认证的路由，直接访问
    next()
  }
})

export default router
