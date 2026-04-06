<template>
  <div id="spaceManagePage">
    <div class="header-card">
      <div class="search-section">
        <a-form layout="inline" :model="searchParams" @finish="doSearch" class="search-form">
          <a-form-item label="空间名称" name="spaceName">
            <a-input
              v-model:value="searchParams.spaceName"
              placeholder="请输入空间名称"
              allow-clear
              class="custom-input"
            />
          </a-form-item>
          <a-form-item label="空间级别" name="spaceLevel">
            <a-select
              v-model:value="searchParams.spaceLevel"
              placeholder="请选择空间级别"
              :options="SPACE_LEVEL_OPTIONS"
              allow-clear
              class="custom-select"
            />
          </a-form-item>
          <a-form-item label="空间类别" name="spaceType">
            <a-select
              v-model:value="searchParams.spaceType"
              placeholder="请选择空间类别"
              :options="SPACE_TYPE_OPTIONS"
              allow-clear
              class="custom-select"
            />
          </a-form-item>

          <a-form-item>
            <a-button type="primary" html-type="submit" class="search-btn">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
          </a-form-item>
        </a-form>
      </div>

      <div class="action-section">
        <a-space>
          <a-button
            class="action-btn analyze-btn"
            href="/space_analyze?queryPublic=1"
            target="_blank"
          >
            <template #icon><BarChartOutlined /></template>
            分析公共图库
          </a-button>
          <a-button class="action-btn analyze-btn" href="/space_analyze?queryAll=1" target="_blank">
            <template #icon><BarChartOutlined /></template>
            分析全部空间
          </a-button>
          <a-button type="primary" class="create-btn" href="/add_space" target="_blank">
            <template #icon><PlusOutlined /></template>
            创建空间
          </a-button>
        </a-space>
      </div>
    </div>

    <div class="table-card">
      <a-table
        :columns="columns"
        :data-source="dataList"
        :pagination="pagination"
        @change="doTableChange"
        class="custom-table"
        :row-key="(record: any) => record.id"
        :scroll="{ x: 'max-content' }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'spaceLevel'">
            <a-tag :color="getSpaceLevelColor(record.spaceLevel)" class="level-tag">
              {{ SPACE_LEVEL_MAP[record.spaceLevel] }}
            </a-tag>
          </template>

          <template v-else-if="column.dataIndex === 'spaceType'">
            <a-tag :color="getSpaceTypeColor(record.spaceType)" class="type-tag">
              {{ SPACE_TYPE_MAP[record.spaceType ?? 0] }}
            </a-tag>
          </template>

          <template v-if="column.dataIndex === 'spaceUseInfo'">
            <div class="usage-container">
              <div class="usage-item">
                <div class="usage-label">
                  <span>容量</span>
                  <span class="usage-text"
                    >{{ formatSize(record.totalSize) }} / {{ formatSize(record.maxSize) }}</span
                  >
                </div>
                <a-progress
                  :percent="calcPercent(record.totalSize, record.maxSize)"
                  size="small"
                  :stroke-color="getStrokeColor(calcPercent(record.totalSize, record.maxSize))"
                  :show-info="false"
                />
              </div>
              <div class="usage-item">
                <div class="usage-label">
                  <span>数量</span>
                  <span class="usage-text">{{ record.totalCount }} / {{ record.maxCount }}</span>
                </div>
                <a-progress
                  :percent="calcPercent(record.totalCount, record.maxCount)"
                  size="small"
                  :stroke-color="getStrokeColor(calcPercent(record.totalCount, record.maxCount))"
                  :show-info="false"
                />
              </div>
            </div>
          </template>

          <template v-if="column.dataIndex === 'createTime' || column.dataIndex === 'editTime'">
            <span class="time-text">{{
              dayjs(record[column.dataIndex]).format('YYYY-MM-DD HH:mm:ss')
            }}</span>
          </template>

          <template v-else-if="column.key === 'action'">
            <div class="table-action-wrapper">
              <a-button
                type="link"
                :href="`/add_space?id=${record.id}`"
                target="_blank"
                class="table-action-btn"
              >
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除此空间吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="doDelete(record.id)"
              >
                <a-button type="link" danger class="table-action-btn"> 删除 </a-button>
              </a-popconfirm>
            </div>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteSpaceUsingPost, listSpaceByPageUsingPost } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import { SearchOutlined, PlusOutlined, BarChartOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import {
  SPACE_LEVEL_MAP,
  SPACE_LEVEL_OPTIONS,
  SPACE_TYPE_MAP,
  SPACE_TYPE_OPTIONS,
} from '@/constants/space.ts'
import { formatSize } from '@/utils'

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 150,
    align: 'center',
  },
  {
    title: '空间名称',
    dataIndex: 'spaceName',
    width: 160,
  },
  {
    title: '空间级别',
    dataIndex: 'spaceLevel',
    width: 120,
    align: 'center',
  },
  {
    title: '空间类别',
    dataIndex: 'spaceType',
    width: 120,
    align: 'center',
  },
  {
    title: '使用情况',
    dataIndex: 'spaceUseInfo',
    width: 240,
  },
  {
    title: '归属用户 ID',
    dataIndex: 'userId',
    width: 180,
    align: 'center',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 160,
  },
  {
    title: '更新时间',
    dataIndex: 'editTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    width: 140,
    fixed: 'right',
    align: 'center',
  },
]

// 数据
const dataList = ref<API.Space[]>([])
const total = ref(0)

//搜索条件
const searchParams = reactive<API.SpaceQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

//分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 项数据`,
  }
})

//获取数据
const fetchData = async () => {
  const res = await listSpaceByPageUsingPost({
    ...searchParams,
  })
  if (res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

//页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
})

//搜索数据
const doSearch = () => {
  searchParams.current = 1
  fetchData()
}

//表格变化处理
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

//删除数据
const doDelete = async (id: number) => {
  if (!id) {
    return
  }
  const res = await deleteSpaceUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败')
  }
}

// 辅助计算：百分比
const calcPercent = (used: number = 0, max: number = 1) => {
  if (!max || max === 0) return 0
  const percent = (used / max) * 100
  return Number(percent.toFixed(1))
}

// 辅助计算：根据百分比获取颜色（越满越红）
const getStrokeColor = (percent: number) => {
  if (percent >= 90) return '#ff4d4f'
  if (percent >= 75) return '#faad14'
  return '#1677ff'
}

// 获取空间级别颜色 (已更新为高亮颜色)
const getSpaceLevelColor = (level: number) => {
  const colors: Record<number, string> = {
    0: 'cyan', // 0 通常是普通空间，使用青色打破沉闷
    1: 'blue', // 1 使用蓝色
    2: 'green', // 2 使用紫色
    3: 'orange', // 3 使用金色
  }
  return colors[level] || 'default'
}
//获取空间类别颜色
const getSpaceTypeColor = (type: number) => {
  const colors: Record<number, string> = {
    0: 'purple', // 0 通常是普通空间，使用青色打破沉闷
    1: 'gold', // 1 使用蓝色
  }
  return colors[type] || 'default'
}
</script>

<style scoped>
/* ========== 全局优雅滚动条定制 ========== */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

/* ========== 入场动画 ========== */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========== 页面主容器 ========== */
#spaceManagePage {
  padding: 24px;
  background-color: transparent;
  /* 解除死板的高度限制，让页面自然延伸 */
  min-height: 60vh;
  display: flex;
  flex-direction: column;
}

/* ========== 顶部搜索栏卡片 ========== */
.header-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  background: #ffffff;
  padding: 20px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
  animation: fadeInUp 0.5s ease-out forwards;
}

.search-section {
  flex: 1;
}

.custom-input,
.custom-select {
  width: 220px;
}

.search-btn,
.create-btn,
.action-btn {
  border-radius: 6px;
  font-weight: 500;
}

.analyze-btn {
  background-color: #52c41a;
  border-color: #52c41a;
  color: #ffffff;
}

.analyze-btn:hover {
  background-color: #73d13d;
  border-color: #73d13d;
  color: #ffffff;
}

/* ========== 表格卡片 ========== */
.table-card {
  background: #ffffff;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  animation: fadeInUp 0.5s ease-out 0.1s forwards;
  opacity: 0;
}

/* 表头样式（浅灰色，无边框感） */
.custom-table :deep(.ant-table-thead > tr > th) {
  background: #f7f8fa !important;
  color: #4e5969;
  font-weight: 600;
  border-bottom: 1px solid #f0f2f5;
}

/* 单元格样式（极简细线分隔） */
.custom-table :deep(.ant-table-tbody > tr > td) {
  padding: 16px;
  color: #1d2129;
  border-bottom: 1px solid #f0f2f5;
}

/* 悬浮效果（极轻微的背景色变化，不晃眼） */
.custom-table :deep(.ant-table-tbody > tr:hover > td) {
  background-color: #fcfcfd !important;
}

/* 标签样式调整 */
.level-tag {
  border-radius: 4px;
  padding: 2px 10px;
  font-weight: 500;
  border: none;
}

/* 时间文本 */
.time-text {
  color: #86909c;
  font-size: 13px;
}

/* 操作按钮水平居中 */
.table-action-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.table-action-btn {
  padding: 0;
  font-size: 13px;
  font-weight: 500;
  height: auto;
  line-height: 1;
}

/* 空间使用情况（进度条布局） */
.usage-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.usage-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.usage-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #86909c;
}

.usage-text {
  font-family: 'Courier New', Courier, monospace;
  font-size: 12px;
  color: #1d2129;
}

:deep(.ant-progress-line) {
  margin-bottom: 0;
}

/* 分页器底边距微调 */
:deep(.ant-pagination) {
  margin-top: 24px !important;
  margin-bottom: 0 !important;
}
</style>
