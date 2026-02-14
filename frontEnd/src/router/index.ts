import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
      component: () => import('@/pages/HomePage.vue'),
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: () => import('@/pages/admin/UserManagePage.vue'),
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: () => import('@/pages/User/UserLoginPage.vue'), // 更新路径
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: () => import('@/pages/User/UserRegisterPage.vue'), // 统一小写路径
    },
  ],
})

export default router
