<template>
  <div id="spaceAnalyzePage">
    <div class="page-header">
      <h2 class="page-title">
        空间图库分析
        <span class="page-subtitle">
          <template v-if="queryAll"> / 全部空间</template>
          <template v-else-if="queryPublic"> / 公共图库</template>
          <template v-else>
            <a :href="`/space/${spaceId}`" target="_blank" class="space-link">
              (空间 ID: {{ spaceId }})
            </a>
          </template>
        </span>
      </h2>
      <div class="page-desc">全面掌握空间的使用状况、图片分布与用户行为数据。</div>
    </div>

    <a-row :gutter="[24, 24]">
      <a-col :xs="24" :md="12">
        <SpaceUsageAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic" />
      </a-col>

      <a-col :xs="24" :md="12">
        <SpaceCategoryAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic" />
      </a-col>

      <a-col :xs="24" :md="12">
        <SpaceTagAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic" />
      </a-col>

      <a-col :xs="24" :md="12">
        <SpaceSizeAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic" />
      </a-col>

      <a-col :xs="24" :md="24">
        <SpaceUserAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic" />
      </a-col>

      <a-col :xs="24" :md="24">
        <SpaceRankAnalyze
          v-if="isAdmin"
          :spaceId="spaceId"
          :queryAll="queryAll"
          :queryPublic="queryPublic"
        />
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { computed } from 'vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import SpaceUsageAnalyze from '@/components/analyze/SpaceUsageAnalyze.vue'
import SpaceCategoryAnalyze from '@/components/analyze/SpaceCategoryAnalyze.vue'
import SpaceTagAnalyze from '@/components/analyze/SpaceTagAnalyze.vue'
import SpaceSizeAnalyze from '@/components/analyze/SpaceSizeAnalyze.vue'
import SpaceUserAnalyze from '@/components/analyze/SpaceUserAnalyze.vue'
import SpaceRankAnalyze from '@/components/analyze/SpaceRankAnalyze.vue'

const route = useRoute()

// 空间 id
const spaceId = computed(() => {
  return route.query?.spaceId as string
})

// 判断用户是否为管理员
const loginUserStore = useLoginUserStore()
const loginUser = loginUserStore.loginUser
const isAdmin = computed(() => {
  return loginUser.userRole === 'admin'
})

// 是否查询所有空间
const queryAll = computed(() => {
  return !!route.query?.queryAll
})

// 是否查询公共空间
const queryPublic = computed(() => {
  return !!route.query?.queryPublic
})
</script>

<style scoped>
#spaceAnalyzePage {
  margin-bottom: 24px;
}

/* 页面头部样式优化 */
.page-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0; /* 添加一条淡雅的分割线分隔内容区 */
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f1f1f;
  margin-bottom: 8px;
  display: flex;
  align-items: baseline;
  letter-spacing: 0.5px;
}

.page-subtitle {
  font-size: 16px;
  color: #595959;
  font-weight: normal;
  margin-left: 8px;
}

.space-link {
  color: #1890ff;
  text-decoration: none;
  font-size: 16px;
  transition: all 0.3s;
}

.space-link:hover {
  color: #40a9ff;
  text-decoration: underline;
}

.page-desc {
  color: #8c8c8c;
  font-size: 14px;
}
</style>
