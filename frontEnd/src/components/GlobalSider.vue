<template>
  <div class="globalSider">
    <a-menu
      v-if="loginUserStore.loginUser.id"
      mode="inline"
      v-model:selectedKeys="current"
      :items="menuItems"
      @click="doMenuClick"
    />
  </div>
</template>

<script setup lang="ts">
import { PictureOutlined, UserOutlined, TeamOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { computed, h, ref, watchEffect } from 'vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { SPACE_TYPE_ENUM } from '@/constants/space.ts'
import {listMyTeamSpaceUsingPost} from "@/api/spaceUserController.ts";
import {message} from "ant-design-vue";

const fixedMenuItems = [
  {
    key: '/',
    label: '公共图库',
    icon: () => h(PictureOutlined),
  },
  {
    key: '/my_space',
    label: '我的空间',
    icon: () => h(UserOutlined),
  },
  {
    key: '/add_space?type=' + SPACE_TYPE_ENUM.TEAM,
    label: '创建团队',
    icon: () => h(TeamOutlined),
  },
]

const router = useRouter()

const current = ref<string[]>([])

router.afterEach((to, from, failure) => {
  current.value = [to.path]
})

const doMenuClick = ({ key }: { key: string }) => {
  try {
    router.push(key)
    console.log('路由跳转成功')
  } catch (error) {
    console.error('路由跳转失败:', error)
    message.error('跳转失败：' + error)
  }
}

const loginUserStore = useLoginUserStore()

const teamSpaceList = ref<API.SpaceUserVo[]>([])
const menuItems = computed(() => {
  // 如果用户没有团队空间，则只展示固定菜单
  if (teamSpaceList.value.length < 1) {
    return fixedMenuItems
  }
  // 如果用户有团队空间，则展示固定菜单和团队空间菜单
  // 展示团队空间分组
  const teamSpaceSubMenus = teamSpaceList.value.map((spaceUser) => {
    const space = spaceUser.space
    return {
      key: '/space/' + spaceUser.spaceId,
      label: space?.spaceName,
      icon: () => h(TeamOutlined),
    }
  })
  const teamSpaceMenuGroup = {
    type: 'group',
    label: '我的团队',
    key: 'teamSpace',
    children: teamSpaceSubMenus,
  }
  return [...fixedMenuItems, teamSpaceMenuGroup]
})

// 加载团队空间列表
const fetchTeamSpaceList = async () => {
  const res = await listMyTeamSpaceUsingPost()
  if (res.data.code === 0 && res.data.data) {
    teamSpaceList.value = res.data.data
  } else {
    message.error('加载我的团队空间失败，' + res.data.message)
  }
}


/**
 * 监听变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  // 登录才加载
  if (loginUserStore.loginUser.id) {
    fetchTeamSpaceList()
  }
})
</script>

<style scoped>
.globalSider {
  height: 100%;
  padding: 16px 0;
}

.globalSider :deep(.ant-menu) {
  border: none;
  background: transparent;
  padding: 0 12px;
  overflow: visible;
}

/* 书签样式菜单项 */
.globalSider :deep(.ant-menu-item) {
  position: relative;
  margin: 8px 0;
  padding: 14px 20px;
  border-radius: 0 8px 8px 0;
  transition: all 0.3s ease;
  color: #555;
  font-size: 15px;
  font-weight: 500;
  background: transparent;
  clip-path: polygon(0 0, calc(100% + 8px) 0, calc(100% + 8px) 85%, 100% 100%, 0 100%, 8px 50%);
  margin-right: -8px;
  z-index: 1;
}

/* 书签左侧指示条 */
.globalSider :deep(.ant-menu-item)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: linear-gradient(180deg, #2980b9, #3498db);
  border-radius: 0 2px 2px 0;
  transition: height 0.3s ease;
  z-index: 2;
}

.globalSider :deep(.ant-menu-item:hover) {
  color: #2980b9;
  background: rgba(41, 128, 185, 0.08);
  clip-path: polygon(0 0, calc(100% + 10px) 0, calc(100% + 10px) 85%, 100% 100%, 0 100%, 8px 50%);
  margin-right: -10px;
}

.globalSider :deep(.ant-menu-item:hover)::before {
  height: 20px;
}

/* 选中状态 - 书签高亮 */
.globalSider :deep(.ant-menu-item-selected) {
  color: #2980b9;
  background: linear-gradient(135deg, rgba(41, 128, 185, 0.12) 0%, rgba(52, 152, 219, 0.1) 100%);
  font-weight: 600;
  box-shadow: 2px 2px 8px rgba(41, 128, 185, 0.2);
  clip-path: polygon(0 0, calc(100% + 10px) 0, calc(100% + 10px) 85%, 100% 100%, 0 100%, 8px 50%);
  margin-right: -10px;
}

.globalSider :deep(.ant-menu-item-selected)::before {
  height: 60%;
}

.globalSider :deep(.ant-menu-item-selected::after) {
  display: none;
}

.globalSider :deep(.ant-menu-item-icon) {
  font-size: 18px;
  margin-right: 12px;
  transition: all 0.3s ease;
  z-index: 3;
  position: relative;
}

.globalSider :deep(.ant-menu-item:hover .ant-menu-item-icon),
.globalSider :deep(.ant-menu-item-selected .ant-menu-item-icon) {
  color: #2980b9;
  transform: scale(1.1);
}
</style>
