<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <!-- 固定顶部栏 -->
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>

      <!-- 主体区域 -->
      <a-layout class="main-layout" :class="{ 'has-sidebar': loginUserStore.loginUser.id }">
        <!-- 固定侧边栏 -->
        <GlobalSider v-if="loginUserStore.loginUser.id" class="sider" />

        <!-- 可滚动内容区 -->
        <a-layout-content class="content">
          <router-view />
        </a-layout-content>
      </a-layout>

      <!-- 底部 -->
      <a-layout-footer class="footer" :class="{ 'has-sidebar': loginUserStore.loginUser.id }">
        自己做的页面
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalSider from '@/components/GlobalSider.vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'

const loginUserStore = useLoginUserStore()
</script>

<style scoped>
#basicLayout {
  min-height: 100vh;
}

/* 固定顶部栏 */
#basicLayout .header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding-inline: 20px;
  color: unset;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  height: 64px;
  line-height: 64px;
}

/* 主体布局 */
.main-layout {
  margin-top: 64px;
  min-height: calc(100vh - 64px);
}

/* 固定侧边栏 */
#basicLayout .sider {
  position: fixed;
  left: 0;
  top: 64px;
  bottom: 0;
  width: 200px;
  z-index: 999;
  background: linear-gradient(to right, #fefefe, #fff);
  padding-top: 20px;
  border-right: 1px solid #eee;
  overflow-y: auto;
}

/* 内容区域 */
#basicLayout .content {
  background: linear-gradient(to right, #fefefe, #fff);
  padding: 20px;
  min-height: calc(100vh - 64px);
  overflow-y: auto;
  margin-left: 0;
}

/* 底部 - 不固定 */
#basicLayout .footer {
  background: #efefef;
  padding: 16px;
  text-align: center;
  margin-left: 0;
  margin-top: 20px;
}

/* 有侧边栏时的样式 */
.main-layout.has-sidebar .content {
  margin-left: 200px;
}

.footer.has-sidebar {
  margin-left: 200px;
}

/* 响应式 - 移动端侧边栏处理 */
@media (max-width: 991px) {
  #basicLayout .sider {
    position: fixed;
    top: 64px;
    bottom: 0;
    z-index: 1001;
    box-shadow: 2px 0 12px rgba(0, 0, 0, 0.08);
  }

  .main-layout.has-sidebar .content {
    margin-left: 0;
  }

  .footer.has-sidebar {
    margin-left: 0;
  }
}
</style>
