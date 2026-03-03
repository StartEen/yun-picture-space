import { createRouter, createWebHistory } from 'vue-router'
import AccessEnum from "@/access/accessEnum.ts";

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
      meta: {
        access: AccessEnum.ADMIN,
      },
    },
    {
      path: '/admin/pictureManage',
      name: '图片管理',
      component: () => import('@/pages/admin/PictureManagePage.vue'),
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: () => import('@/pages/user/UserLoginPage.vue'), // 统一使用小写路径
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: () => import('@/pages/user/UserRegisterPage.vue'), // 统一小写路径
    },
    {
      path: '/add_picture',
      name: '创建图片',
      component: () => import('@/pages/picture/AddPicturePage.vue'),
    },
    {
      path: '/picture/:id',
      name: '图片详情',
      component: () => import('@/pages/picture/PictureDetailPage.vue'),
      props: true,
    }

  ],
})

export default router
