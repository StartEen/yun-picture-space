<template>
  <div class="space-tag-analyze">
    <a-card
      title="空间图片标签分析"
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
import 'echarts-wordcloud'
import { computed, ref, watchEffect } from 'vue'
import { getSpaceTagAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
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
const dataList = ref<API.SpaceTagAnalyzeResponse[]>([])
// 加载状态
const loading = ref(true)

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 转换搜索参数
    const res = await getSpaceTagAnalyzeUsingPost({
      queryAll: props.queryAll,
      queryPublic: props.queryPublic,
      spaceId: props.spaceId,
    })
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data ?? []
    } else {
      message.error('获取标签数据失败，' + res.data.message)
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

// 预设一套更具质感的数据可视化色板 (包含主色和中性色点缀)
const modernPalette = [
  '#1677FF', // 极客蓝
  '#00B96B', // 翡翠绿
  '#F5222D', // 珊瑚红
  '#FAAD14', // 万寿黄
  '#722ED1', // 酱紫
  '#13C2C2', // 明青
  '#EB2F96', // 猛男粉
  '#595959', // 中性灰
  '#8C8C8C'
]

// 图表选项配置
const options = computed(() => {
  // 安全处理数据
  const tagData = dataList.value.map((item) => ({
    name: item.tag || '未打标',
    value: item.count || 0,
  }))

  return {
    // 深度定制的富文本提示框
    tooltip: {
      trigger: 'item',
      formatter: function (params: any) {
        return `<div style="padding: 4px 8px;">
                  <span style="font-weight:600;color:${params.color};font-size:14px;">${params.name}</span>
                  <br/>使用频率: <span style="font-weight:600;margin-left:8px;font-size:14px;">${params.value} 次</span>
                </div>`;
      },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#f0f0f0',
      borderWidth: 1,
      textStyle: { color: '#262626' },
      extraCssText: 'box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08); border-radius: 8px;'
    },
    series: [
      {
        type: 'wordCloud',
        // 增加“呼吸感”，避免文字粘连
        gridSize: 12,
        // 拉大字号对比度，让 Top 标签极具视觉冲击力
        sizeRange: [12, 72],
        // 允许水平和垂直排版（0度和90度），形成海报级网格感
        rotationRange: [-90, 90],
        rotationStep: 90,
        shape: 'circle',
        left: 'center',
        top: 'center',
        width: '95%',
        height: '95%',
        drawOutOfBound: false,

        textStyle: {
          // 显式指定现代字体体系，渲染更平滑
          fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif',
          fontWeight: 'bold',
          color: function () {
            // 随机从高级色板中选取颜色
            const index = Math.floor(Math.random() * modernPalette.length);
            return modernPalette[index];
          },
        },
        // 鼠标悬浮特效增强：高亮当前，淡化其他，并增加发光阴影
        emphasis: {
          focus: 'self',
          textStyle: {
            shadowBlur: 12,
            shadowColor: 'rgba(0, 0, 0, 0.25)',
          }
        },
        data: tagData,
      },
    ],
  }
})
</script>

<style scoped>
.space-tag-analyze {
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
