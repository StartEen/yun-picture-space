<template>
  <div class="space-size-analyze">
    <a-card
      title="空间图片大小分析"
      :bordered="false"
      class="analyze-card"
    >
      <v-chart
        :option="options"
        style="height: 320px; width: 100%"
        :loading="loading"
        autoresize
      />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import VChart from 'vue-echarts'
import 'echarts'
import { computed, ref, watchEffect } from 'vue'
import { getSpaceSizeAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
import { message } from 'ant-design-vue'

interface Props {
  queryAll?: boolean
  queryPublic?: boolean
  spaceId?: number
}

const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false,
})

// 图表数据
const dataList = ref<API.SpaceSizeAnalyzeResponse[]>([])
// 加载状态
const loading = ref(true)

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 转换搜索参数
    const res = await getSpaceSizeAnalyzeUsingPost({
      queryAll: props.queryAll,
      queryPublic: props.queryPublic,
      spaceId: props.spaceId,
    })
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data ?? []
    } else {
      message.error('获取大小分析数据失败，' + res.data.message)
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

// 图表选项配置
const options = computed(() => {
  // 安全处理数据
  const pieData = dataList.value.map((item) => ({
    name: item.sizeRange || '未知大小',
    value: item.count || 0,
  }))

  return {
    // 鼠标悬浮提示
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} 张 ({d}%)', // 加上了“张”字，更清晰
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: '#eee',
      textStyle: { color: '#595959' }
    },
    // 图例配置
    legend: {
      bottom: '0', // 放在最底部
      icon: 'circle', // 圆形图例
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { color: '#595959' }
    },
    // 现代感配色方案 (Ant Design 默认色板优化)
    color: ['#1890ff', '#13c2c2', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#eb2f96'],
    series: [
      {
        name: '图片大小区间',
        type: 'pie',
        // 核心改动：使用数组配置内外半径，将饼图变成环形图
        radius: ['45%', '70%'],
        // 将图表整体稍微向上偏移，给底部的图例留出空间
        center: ['50%', '45%'],
        // 视觉优化：给切片增加白边和圆角
        itemStyle: {
          borderRadius: 8,
          borderColor: '#ffffff',
          borderWidth: 2,
        },
        // 外部标签指示线
        label: {
          show: true,
          formatter: '{b}\n{d}%', // 显示区间名和百分比
          color: '#595959',
          lineHeight: 16
        },
        labelLine: {
          show: true,
          smooth: 0.2, // 让指示线带一点平滑的弧度
          length: 10,
          length2: 15
        },
        data: pieData,
      },
    ],
  }
})
</script>

<style scoped>
.space-size-analyze {
  margin-bottom: 16px;
}

/* 保持系统内卡片UI一致性 */
.analyze-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.analyze-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

:deep(.ant-card-head) {
  border-bottom: 1px solid #f0f0f0;
  padding: 0 24px;
}

:deep(.ant-card-head-title) {
  font-weight: 600;
  font-size: 16px;
  color: #1f1f1f;
}
</style>
