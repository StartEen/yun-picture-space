<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="250px">
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
          <a-button type="primary" href="/user/login">Login</a-button>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { h, ref } from 'vue'
import { HomeOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { MenuProps } from 'ant-design-vue'
import { useRouter } from 'vue-router'
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
</script>
<style scoped>
.title-bar {
  display: flex;
  align-items: center;
  gap: 12px; /* 控制 logo 与 title 的间距 */
}

.title {
  color: #333; /* 更柔和的深灰色 */
  font-size: 18px;
  font-weight: 600;
  margin: 0; /* 移除默认 margin */
}
.title:hover {
  color: #007bff;
  text-decoration: underline;
}
.logo {
  height: 48px;
  width: 48px; /* 明确设置宽度 */
  object-fit: contain; /* 保持图片比例并完整显示 */
}
</style>
