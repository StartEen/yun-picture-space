<template>
  <div class="space-user-analyze">
    <a-card
      title="空间图片用户分析"
      :bordered="false"
      class="analyze-card"
    >
      <template #extra>
        <a-space>
          <a-segmented v-model:value="timeDimension" :options="timeDimensionOptions" />
          <a-input-search
            placeholder="请输入用户 ID"
            enter-button="搜索"
            @search="doSearch"
            allow-clear
          />
        </a-space>
      </template>

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
import { getSpaceUserAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
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

// 时间维度选项
const timeDimension = ref<'day' | 'week' | 'month' | 'year'>('day')
// 分段选择器组件的选项
const timeDimensionOptions = [
  { label: '日', value: 'day' },
  { label: '周', value: 'week' },
  { label: '月', value: 'month' },
  { label: '年', value: 'year' },
]

// 用户选项
const userId = ref<string>()
const doSearch = (value: string) => {
  userId.value = value
}

// 图表数据
const dataList = ref<API.SpaceUserAnalyzeResponse[]>([])
// 加载状态
const loading = ref(true)

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 转换搜索参数
    const res = await getSpaceUserAnalyzeUsingPost({
      queryAll: props.queryAll,
      queryPublic: props.queryPublic,
      spaceId: props.spaceId,
      timeDimension: timeDimension.value,
      userId: userId.value,
    })
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data ?? []
    } else {
      message.error('获取用户分析数据失败，' + res.data.message)
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
  const periods = dataList.value.map((item) => item.period || '未知区间') // 时间区间
  const counts = dataList.value.map((item) => item.count || 0) // 上传数量

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross', // 十字准星指示器
        label: { backgroundColor: '#6a7985' }
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '8%', // 顶部留白，防止 Y 轴名称被遮挡
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: periods,
      boundaryGap: false, // 【核心布局优化】面积图取消两端留白，紧贴 Y 轴更好看
      axisLabel: { color: '#8c8c8c' },
      axisLine: { lineStyle: { color: '#d9d9d9' } }
    },
    yAxis: {
      type: 'value',
      name: '上传数量 (张)',
      nameTextStyle: { color: '#8c8c8c', align: 'left', padding: [0, 0, 0, -20] },
      axisLabel: { color: '#8c8c8c' },
      splitLine: {
        lineStyle: { type: 'dashed', color: '#f0f0f0' } // 柔和的虚线背景
      }
    },
    series: [
      {
        name: '上传数量',
        type: 'line',
        data: counts,
        smooth: true, // 平滑折线
        symbolSize: 6, // 数据点大小
        showSymbol: false, // 平时隐藏数据点，鼠标悬浮时才显示，更显高级
        itemStyle: {
          color: '#1890ff', // 品牌主色
        },
        // 【核心视觉优化】添加面积渐变填充
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1, // 从上到下渐变
            colorStops: [
              { offset: 0, color: 'rgba(24, 144, 255, 0.4)' }, // 顶部颜色较深
              { offset: 1, color: 'rgba(24, 144, 255, 0.05)' } // 底部几乎透明
            ]
          }
        },
      },
    ],
  }
})
</script>

<style scoped>
.space-user-analyze {
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

/* 优化搜索框，使其与分段选择器高度更匹配 */
:deep(.ant-input-search .ant-input) {
  border-radius: 6px 0 0 6px;
}
:deep(.ant-input-search .ant-btn) {
  border-radius: 0 6px 6px 0;
}
</style>
