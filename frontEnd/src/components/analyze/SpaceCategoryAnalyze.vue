<template>
  <div class="space-category-analyze">
    <a-card
      title="空间图片分类分析"
      :bordered="false"
      class="analyze-card"
    >
      <v-chart
        :option="options"
        style="height: 320px; width: 100%;"
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
import { getSpaceCategoryAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
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
const dataList = ref<API.SpaceCategoryAnalyzeResponse[]>([])
// 加载状态
const loading = ref(true)

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 转换搜索参数
    const res = await getSpaceCategoryAnalyzeUsingPost({
      queryAll: props.queryAll,
      queryPublic: props.queryPublic,
      spaceId: props.spaceId,
    })
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data ?? []
    } else {
      message.error('获取分类数据失败，' + res.data.message)
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

// 图表选项配置 (响应式计算)
const options = computed(() => {
  // 安全提取数据，处理空值
  const categories = dataList.value.map((item) => item.category || '未分类')
  const countData = dataList.value.map((item) => item.count || 0)
  const sizeData = dataList.value.map((item) => ((item.totalSize || 0) / (1024 * 1024)).toFixed(2)) // 转为 MB

  return {
    // 鼠标悬浮提示
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross', // 交叉十字准星指示器
        crossStyle: { color: '#999' }
      }
    },
    // 图例
    legend: {
      data: ['图片数量', '图片总大小'],
      bottom: '0', // 放置在底部
      icon: 'circle', // 图例图标改成圆形更好看
      textStyle: { color: '#595959' }
    },
    // 图表边距，防止标签被遮挡
    grid: {
      left: '2%',
      right: '2%',
      bottom: '12%',
      top: '12%',
      containLabel: true,
    },
    // X轴
    xAxis: {
      type: 'category',
      data: categories,
      axisPointer: { type: 'shadow' }, // X轴悬浮阴影
      axisLine: { lineStyle: { color: '#d9d9d9' } }, // 柔和的轴线
      axisLabel: { color: '#8c8c8c' }
    },
    // 双Y轴
    yAxis: [
      {
        type: 'value',
        name: '图片数量 (张)',
        nameTextStyle: { color: '#8c8c8c', padding: [0, 0, 0, 20] },
        axisLabel: { color: '#5470C6' },
        splitLine: {
          lineStyle: { type: 'dashed', color: '#f0f0f0' } // 淡雅的虚线网格
        },
      },
      {
        type: 'value',
        name: '总大小 (MB)',
        position: 'right',
        nameTextStyle: { color: '#8c8c8c', padding: [0, 20, 0, 0] },
        axisLabel: { color: '#91CC75' },
        splitLine: { show: false }, // 【关键】隐藏右侧的网格线，防止两套网格线交叉打架
      },
    ],
    // 数据系列
    series: [
      {
        name: '图片数量',
        type: 'bar', // 柱状图
        yAxisIndex: 0,
        barMaxWidth: 40, // 限制柱子最大宽度
        data: countData,
        itemStyle: {
          color: '#5470C6', // 经典蓝
          borderRadius: [4, 4, 0, 0], // 【细节】顶部圆角，显得不那么生硬
        }
      },
      {
        name: '图片总大小',
        type: 'line', // 【核心视觉优化】改为折线图
        yAxisIndex: 1,
        smooth: true, // 平滑曲线
        symbolSize: 8, // 数据点大小
        data: sizeData,
        itemStyle: {
          color: '#91CC75', // 清新绿
        },
        areaStyle: { // 添加底部渐变颜色填充，增加体积感
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(145, 204, 117, 0.4)' },
              { offset: 1, color: 'rgba(145, 204, 117, 0.05)' }
            ]
          }
        }
      },
    ],
  }
})
</script>

<style scoped>
.space-category-analyze {
  margin-bottom: 16px;
}

/* 统一卡片风格，匹配上一个组件 */
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
