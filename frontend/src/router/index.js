import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 路由配置
const routes = [
  {
    path: '/',
    name: 'Welcome',
    component: () => import('@/views/Welcome.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/changelog',
    name: 'Changelog',
    component: () => import('@/views/Changelog.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/ForgotPassword.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/layout/DashboardLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/projects',
        name: 'Projects',
        component: () => import('@/views/Projects.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/projects/:projectId/apis',
        name: 'ProjectApis',
        component: () => import('@/views/ProjectApis.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/apis',
        name: 'Apis',
        component: () => import('@/views/Apis.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/users',
        name: 'Users',
        component: () => import('@/views/Users.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/code-templates',
        name: 'CodeTemplates',
        component: () => import('@/views/CodeTemplates.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/guide',
        name: 'Guide',
        component: () => import('@/views/Guide.vue'),
        meta: { requiresAuth: false }
      },
      {
        path: '/email-templates',
        name: 'EmailTemplates',
        component: () => import('@/views/EmailTemplates.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/ai-settings',
        name: 'AiSettings',
        component: () => import('@/views/AiSettings.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/ai-chat',
        name: 'AiChat',
        component: () => import('@/views/AiChat.vue'),
        meta: { requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn) {
      next('/login')
      return
    }
    
    // 检查是否需要管理员权限
    if (to.meta.requiresAdmin && !userStore.isAdmin) {
      next('/dashboard')
      return
    }
  }
  
  // 如果已登录，不允许访问登录页和注册页
  if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router
