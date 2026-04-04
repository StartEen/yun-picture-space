<template>
  <div id="spaceManagePage">
    <div class="header-card">
      <div class="search-section">
        <h2>空间成员管理</h2>
      </div>

      <div class="action-section">
        <a-form layout="inline" :model="formData" @finish="handleSubmit" class="search-form">
          <a-form-item label="用户 id" name="spaceName">
            <a-input
              v-model:value="formData.userId"
              placeholder="请输入用户 id"
              allow-clear
              class="custom-input"
            />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" html-type="submit" class="search-btn">
              <template #icon><TeamOutlined /></template>
              添加用户
            </a-button>
          </a-form-item>
        </a-form>
      </div>
    </div>

    <div class="table-card">
      <a-table
        :columns="columns"
        :data-source="dataList"
        class="custom-table"
        :row-key="(record: any) => record.id"
        :scroll="{ x: 'max-content' }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'userInfo'">
            <a-space>
              <a-avatar :src="record.user?.userAvatar" />
              {{ record.user?.userName }}
            </a-space>
          </template>

          <template v-if="column.dataIndex === 'spaceRole'">
            <a-select
              v-model:value="record.spaceRole"
              :options="SPACE_ROLE_OPTIONS"
              @change="(value) => editSpaceRole(value, record)"
            />
          </template>

          <template v-else-if="column.dataIndex === 'createTime'">
            {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
          </template>

          <template v-else-if="column.key === 'action'">
            <div class="table-action-wrapper">
              <a-popconfirm
                title="确定要删除此空间成员吗？"
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
import { deleteSpaceUsingPost } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import { TeamOutlined, PlusOutlined, BarChartOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { SPACE_LEVEL_MAP, SPACE_LEVEL_OPTIONS, SPACE_ROLE_OPTIONS } from '@/constants/space.ts'
import { formatSize } from '@/utils'
import {
  addSpaceUserUsingPost,
  deleteSpaceUserUsingPost,
  editSpaceUserUsingPost,
  listSpaceUserUsingPost,
} from '@/api/spaceUserController.ts'

const columns = [
  {
    title: '用户',
    dataIndex: 'userInfo',
  },
  {
    title: '角色',
    dataIndex: 'spaceRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]
interface Props {
  id: string
}

const props = defineProps<Props>()
// 数据
const dataList = ref<API.SpaceUserVo[]>([])

// 获取数据
const fetchData = async () => {
  const spaceId = props.id
  if (!spaceId) {
    return
  }
  const res = await listSpaceUserUsingPost({
    spaceId,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data ?? []
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
})

// 添加成员表单
const formData = reactive<API.SpaceUserAddRequest>({})

// 创建成员
const handleSubmit = async () => {
  const spaceId = props.id
  if (!spaceId) {
    return
  }
  const res = await addSpaceUserUsingPost({
    spaceId,
    ...formData,
  })
  if (res.data.code === 0) {
    message.success('添加成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('添加失败，' + res.data.message)
  }
}

// 编辑成员角色
const editSpaceRole = async (value, record) => {
  const res = await editSpaceUserUsingPost({
    id: record.id,
    spaceRole: value,
  })
  if (res.data.code === 0) {
    message.success('修改成功')
  } else {
    message.error('修改失败，' + res.data.message)
  }
}

// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteSpaceUserUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
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
    2: 'purple', // 2 使用紫色
    3: 'gold', // 3 使用金色
  }
  return colors[level] || 'default'
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
