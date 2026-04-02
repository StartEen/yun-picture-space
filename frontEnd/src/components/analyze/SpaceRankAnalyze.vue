<template>
  <div class="space-rank-analyze">
    <a-card
      title="空间使用排行分析 (Top 10)"
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
import { getSpaceRankAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
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
const dataList = ref<API.Space[]>([])
// 加载状态
const loading = ref(true)

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 转换搜索参数
    const res = await getSpaceRankAnalyzeUsingPost({
      queryAll: props.queryAll,
      queryPublic: props.queryPublic,
      spaceId: props.spaceId,
      topN: 10, // 后端默认是 10
    })
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data ?? []
    } else {
      message.error('获取排行数据失败，' + res.data.message)
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

// 图表选项
const options = computed(() => {
  // 安全处理数据
  const spaceNames = dataList.value.map((item) => item.spaceName || '未命名空间')
  const usageData = dataList.value.map((item) => ((item.totalSize || 0) / (1024 * 1024)).toFixed(2)) // 转为 MB

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }, // 悬浮阴影效果
      valueFormatter: (value: any) => value + ' MB' // 提示框加上单位
    },
    grid: {
      left: '3%',
      right: '8%', // 右侧留点空间给数值标签
      bottom: '3%',
      top: '5%',
      containLabel: true,
    },
    // 将 X 轴改为数值轴
    xAxis: {
      type: 'value',
      name: '使用量 (MB)',
      nameTextStyle: { color: '#8c8c8c' },
      axisLabel: { color: '#8c8c8c' },
      splitLine: {
        lineStyle: { type: 'dashed', color: '#f0f0f0' } // 淡化网格线
      },
    },
    // 将 Y 轴改为类目轴，做成水平条形图
    yAxis: {
      type: 'category',
      data: spaceNames,
      inverse: true, // 核心：反转 Y 轴，让排名第一的在最上面
      axisLine: { show: false }, // 隐藏 Y 轴实线，更清爽
      axisTick: { show: false }, // 隐藏刻度
      axisLabel: {
        color: '#595959',
        fontWeight: 500,
        formatter: function (value: string) {
          // 如果名字太长，截断并加上省略号
          return value.length > 8 ? value.substring(0, 8) + '...' : value;
        }
      },
    },
    series: [
      {
        name: '空间使用量',
        type: 'bar',
        data: usageData,
        barWidth: '20px', // 控制柱子的粗细
        itemStyle: {
          // 添加右侧圆角
          borderRadius: [0, 4, 4, 0],
          // 添加科技感的蓝/青渐变色
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 1, y2: 0, // 从左到右渐变
            colorStops: [
              { offset: 0, color: '#1890ff' }, // 品牌蓝
              { offset: 1, color: '#36cfc9' }  // 青色
            ]
          }
        },
        label: {
          show: true,
          position: 'right', // 数值直接显示在柱子右侧
          color: '#595959',
          formatter: '{c} MB', // 直接展示带单位的数据，不用鼠标 hover 也能看清
        }
      },
    ],
  }
})
</script>

<style scoped>
.space-rank-analyze {
  margin-bottom: 16px;
}

/* 保持与前几个组件完全一致的卡片风格 */
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
