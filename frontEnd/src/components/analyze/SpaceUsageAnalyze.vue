<template>
  <div class="space-user-analyze">
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :md="12">
        <a-card
          title="存储空间"
          :bordered="false"
          class="analyze-card"
          :loading="loading"
        >
          <div class="card-content">
            <div class="data-display">
              <span class="data-used">{{ formatSize(data.usedSize || 0) }}</span>
              <span class="data-divider">/</span>
              <span class="data-total">{{ data.maxSize ? formatSize(data.maxSize) : '无限制' }}</span>
            </div>
            <div class="progress-wrapper">
              <a-progress
                type="dashboard"
                :percent="data.sizeUsageRatio ?? 0"
                :size="160"
                :stroke-color="{
                  '0%': '#108ee9',
                  '100%': '#87d068',
                }"
              />
            </div>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :md="12">
        <a-card
          title="图片数量"
          :bordered="false"
          class="analyze-card"
          :loading="loading"
        >
          <div class="card-content">
            <div class="data-display">
              <span class="data-used">{{ data.usedCount || 0 }}</span>
              <span class="data-divider">/</span>
              <span class="data-total">{{ data.maxCount ?? '无限制' }}</span>
            </div>
            <div class="progress-wrapper">
              <a-progress
                type="dashboard"
                :percent="data.countUsageRatio ?? 0"
                :size="160"
                :stroke-color="{
                  '0%': '#722ed1',
                  '100%': '#13c2c2',
                }"
              />
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, watchEffect } from 'vue'
import { getSpaceUsageAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
import { message } from 'ant-design-vue'
import { formatSize } from "@/utils"

interface Props {
  queryAll?: boolean
  queryPublic?: boolean
  spaceId?: number
}

const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false,
})

// 图表数据临时存储
const data = ref<API.SpaceUsageAnalyzeResponse>({})

// 加载状态
const loading = ref(true)

// 获取数据
const fetchData = async () => {
  loading.value = true

  //转换搜索参数
  try {
    const res = await getSpaceUsageAnalyzeUsingPost({
      queryAll: props.queryAll,
      queryPublic: props.queryPublic,
      spaceId: props.spaceId,
    })
    if (res.data.code === 0 && res.data.data) {
      data.value = res.data.data
    } else {
      message.error('获取数据失败, ' + res.data.message)
    }
  } catch (error) {
    message.error('请求异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 监听变量，参数改变时触发数据的重新加载
 */
watchEffect(() => {
  fetchData()
})
</script>

<style scoped>
.space-user-analyze {
  margin-bottom: 16px;
}

/* 卡片整体样式 */
.analyze-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  overflow: hidden;
}

.analyze-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

/* 修改卡片头部的默认样式以匹配现代感 */
:deep(.ant-card-head) {
  border-bottom: 1px solid #f0f0f0;
  padding: 0 24px;
}

:deep(.ant-card-head-title) {
  font-weight: 600;
  font-size: 16px;
  color: #1f1f1f;
}

/* 卡片内部容器 */
.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 280px;
  padding: 16px 0;
}

/* 数据展示区排版 */
.data-display {
  margin-bottom: 24px;
  text-align: center;
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 6px;
}

.data-used {
  font-size: 32px;
  font-weight: 700;
  color: #1f1f1f;
  line-height: 1.2;
}

.data-divider {
  font-size: 20px;
  color: #bfbfbf;
  margin: 0 4px;
}

.data-total {
  font-size: 18px;
  color: #8c8c8c;
  font-weight: 500;
}

/* 进度条容器 */
.progress-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 自定义进度条内部文字样式 */
:deep(.ant-progress-text) {
  font-weight: 600 !important;
  color: #262626 !important;
  font-size: 24px !important;
}
</style>
