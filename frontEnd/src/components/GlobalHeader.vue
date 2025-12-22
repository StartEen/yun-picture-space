<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="380px">
        <router-link to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/img.png" alt="logo" />
            <div class="title">Cloud Picture Space</div>
          </div>
        </router-link>
      </a-col>

      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
        />
      </a-col>
      <a-col flex="120px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <ASpace>
                <a-avatar :src="loginUserStore.loginUser.userAvatar || defaultAvatar" />
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </ASpace>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">Login</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref } from 'vue'
import { HomeOutlined, SearchOutlined, LogoutOutlined } from '@ant-design/icons-vue'
import { MenuProps, message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { userLogoutUsingPost } from '@/api/userController.ts'
const loginUserStore = useLoginUserStore()

const defaultAvatar = new URL('@/assets/user.png', import.meta.url).href

const items = ref<MenuProps['items']>([
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/about',
    label: '关于',
    title: '关于',
  },
  {
    key: '/others',
    icon: () => h(SearchOutlined),
    label: h('a', { href: 'https://www.biying.com', target: '_blank' }, '网站直达'),
    title: '网站直达',
  },
])

//路由跳转事件
const router = useRouter()

// 当前要高亮的菜单项
const current = ref<string[]>([])
// 监听路由变化，更新高亮菜单项
router.afterEach((to, from, next) => {
  current.value = [to.path]
})

const doMenuClick = ({ key }: { key: string }) => {
  router.push({
    path: key,
  })
}

const doLogout = async () => {
  const res = await userLogoutUsingPost()
  console.log(res)
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({ userName: '未登录' })

    message.success('退出成功')
    //路由跳转
    router.push('/user/login')
  } else {
    message.error('退出失败：' + res.data.message)
  }
}
</script>
<style scoped>
.title-bar {
  display: flex;
  align-items: center;
  gap: 12px; /* 控制 logo 与 title 的间距 */
}

.title {
  font-family: 'Brush Script MT', 'Segoe Script', 'Lucida Handwriting', cursive, sans-serif;
  background: linear-gradient(135deg, #53b9c6 0%, #215ac6 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  font-size: 24px;
  font-weight: normal;
  margin: 0;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo {
  height: 48px;
  width: 48px; /* 明确设置宽度 */
  object-fit: contain; /* 保持图片比例并完整显示 */
}
</style>
