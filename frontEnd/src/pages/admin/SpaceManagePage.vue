<template>
  <div id="spaceManagePage">
    <!-- 搜索表单 -->
    <div class="search-container">
      <a-form layout="inline" :model="searchParams" @finish="doSearch" class="search-form">
        <div class="form-row">
          <a-form-item label="空间名称" name="spaceName" class="search-item">
            <a-input
              v-model:value="searchParams.spaceName"
              placeholder="请输入空间名称"
              allow-clear
              class="search-input"
            />
          </a-form-item>
          <a-form-item label="空间级别" name="spaceLevel" class="search-item">
            <a-select
              v-model:value="searchParams.spaceLevel"
              placeholder="请选择空间级别"
              :options="SPACE_LEVEL_OPTIONS"
              allow-clear
              class="search-input"
            />
          </a-form-item>
          <a-form-item class="search-item">
            <a-button type="primary" html-type="submit" class="search-button">
              <SearchOutlined />
              搜索
            </a-button>
          </a-form-item>
          <div class="search-right">
            <a-space>
              <a-button type="primary" href="/add_space" target="_blank" class="create-button">
                <PlusOutlined />
                创建空间
              </a-button>
            </a-space>
          </div>
        </div>
      </a-form>
    </div>

    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
      class="data-table"
      :row-key="(record: any) => record.id"
    >
      <template #bodyCell="{ column, record }">
        <!--空间级别-->
        <template v-if="column.dataIndex === 'spaceLevel'">
          <a-tag :color="getSpaceLevelColor(record.spaceLevel)">
            {{ SPACE_LEVEL_MAP[record.spaceLevel] }}
          </a-tag>
        </template>
        <!--空间使用-->
        <template v-if="column.dataIndex === 'spaceUseInfo'">
          <div class="pic-info-card">
            <div class="pic-info-grid">
              <div class="pic-info-item tag-color-0">
                <span class="info-label">已用大小 / 总大小</span>
                <span class="info-value">{{ formatSize(record.totalSize) }} / {{ formatSize(record.maxSize) }}</span>
              </div>
              <div class="pic-info-item tag-color-1">
                <span class="info-label">已用数量 / 总数量 </span>
                <span class="info-value">{{ record.totalCount }} / {{ record.maxCount }}</span>
              </div>
            </div>
          </div>
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <div class="action-buttons">
            <a-button
              type="primary"
              class="action-button edit-button"
              :href="`/add_space?id=${record.id}`"
              target="_blank"
            >
              <EditOutlined />
              编辑
            </a-button>
            <a-button
              type="primary"
              danger
              class="action-button delete-button"
              @click="doDelete(record.id)"
            >
              <DeleteOutlined />
              删除
            </a-button>
          </div>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteSpaceUsingPost, listSpaceByPageUsingPost } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import { EditOutlined, DeleteOutlined, SearchOutlined, PlusOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { SPACE_LEVEL_MAP, SPACE_LEVEL_OPTIONS } from '@/constants/space.ts'
import { formatSize } from '@/utils'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '空间名称',
    dataIndex: 'spaceName',
    width: 150,
  },
  {
    title: '空间级别',
    dataIndex: 'spaceLevel',
    width: 100,
  },
  {
    title: '使用情况',
    dataIndex: 'spaceUseInfo',
    width: 200,
  },
  {
    title: '用户 id',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 150,
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
    width: 150,
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right',
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
    showTotal: (total: number) => `共 ${total} 条`,
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
  //重置页码
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
    //刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}

// 获取空间级别颜色
const getSpaceLevelColor = (level: number) => {
  const colors: Record<number, string> = {
    0: 'default',
    1: 'blue',
    2: 'purple',
  }
  return colors[level] || 'default'
}
</script>

<style scoped>
#spaceManagePage {
  padding: 24px;
  min-height: 80vh;
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 10px;
  position: relative;
}

#spaceManagePage::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  border-radius: 20px;
}

/* 搜索容器 */
.search-container {
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  animation: slideDown 0.5s ease-out;
  flex-wrap: wrap;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 搜索表单 */
.search-form {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 28px;
  border-radius: 20px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.05),
    0 4px 16px rgba(0, 0, 0, 0.01);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex: 1;
  min-width: 300px;
  position: relative;
  overflow: hidden;
}

.search-form::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s ease;
}

.search-form:hover::before {
  left: 100%;
}

.search-form:hover {
  transform: translateY(-2px);
  box-shadow:
    0 12px 40px rgba(0, 0, 0, 0.08),
    0 6px 20px rgba(0, 0, 0, 0.05);
}

/* 表单行布局 - 关键优化：垂直居中对齐 */
.form-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
}

/* 搜索项 - 关键优化：统一高度和对齐 */
.search-item {
  margin: 0;
  display: flex;
  align-items: center;
  min-height: 44px;
}

.search-item :deep(.ant-form-item-label) {
  padding: 0 12px 0 0;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  width: 70px;
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
  white-space: nowrap;
  text-align: right;
}

.search-item :deep(.ant-form-item-control) {
  flex: 1;
  display: flex;
  align-items: center;
}

/* 搜索输入框 - 关键优化：固定高度和垂直居中 */
.search-input {
  width: 240px;
  border-radius: 8px;
  border: 2px solid #e9ecef;
  transition: all 0.3s ease;
  padding: 8px 12px;
  height: 44px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #2c3e50;
  background: #fff;
}

/* 移除选择框内部的边框，只保留外部边框 */
.search-input :deep(.ant-select-selector) {
  border: none !important;
  box-shadow: none !important;
  background: transparent !important;
}

/* 保留选择框下拉菜单的边框 */
.search-input :deep(.ant-select-dropdown) {
  border: 1px solid #e8e8e8 !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15) !important;
}

/* 隐藏选择框的清除按钮 */
.search-input :deep(.ant-select-clear),
.search-input :deep(.ant-select-clear-icon),
.search-input :deep(.anticon-close-circle) {
  display: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
  width: 0 !important;
  height: 0 !important;
  padding: 0 !important;
  margin: 0 !important;
}

.search-input:hover {
  border-color: #4096ff;
  box-shadow: 0 0 0 2px rgba(64, 150, 255, 0.2);
}

.search-input:focus {
  border-color: #4096ff;
  box-shadow: 0 0 0 2px rgba(64, 150, 255, 0.2);
  outline: none;
  font-weight: 500;
}

/* 搜索按钮 - 关键优化：增加间距和清晰度 */
.search-button {
  height: 40px;
  padding: 0 28px;
  border-radius: 8px;
  font-weight: 600;
  background: linear-gradient(45deg, #4096ff, #1890ff);
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(64, 150, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  letter-spacing: 0.5px;
  gap: 2px;
}

.search-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 150, 255, 0.4);
  background: linear-gradient(45deg, #1890ff, #4096ff);
}

.search-button:active {
  transform: translateY(0);
}

/* 右侧创建按钮容器 */
.search-right {
  display: flex;
  align-items: center;
  margin-left: auto;
}

/* 创建空间按钮 */
.create-button {
  height: 40px;
  padding: 0 24px;
  border-radius: 8px;
  font-weight: 600;
  background: linear-gradient(45deg, #52c41a, #389e0d);
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  letter-spacing: 0.5px;
  gap: 6px;
  white-space: nowrap;
}

.create-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(82, 196, 26, 0.4);
  background: linear-gradient(45deg, #389e0d, #52c41a);
}

.create-button:active {
  transform: translateY(0);
}

/* ========== 表格容器样式 ========== */
.data-table {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e8e8e8;
  animation: fadeInUp 0.6s ease-out;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 12px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.05),
    0 4px 16px rgba(0, 0, 0, 0.01);
}

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

/* 表格头部样式 */
.data-table :deep(.ant-table-thead > tr > th) {
  background: #ececec;
  color: #333333;
  font-weight: 600;
  font-size: 14px;
  padding: 14px;
  border: none;
  text-align: center;
  position: sticky;
  top: 0;
  z-index: 10;
}

.data-table :deep(.ant-table-thead > tr > th::before) {
  display: none;
}

/* 表格行容器 */
.data-table :deep(.ant-table-tbody) {
  padding: 8px 0;
}

/* 表格行样式 */
.data-table :deep(.ant-table-tbody > tr) {
  transition: all 0.3s ease;
  cursor: pointer;
  border-radius: 10px;
  overflow: hidden;
  margin: 6px 0;
  background: #ffffff;
  border: 1px solid #f0f0f0;
}

/* 悬浮效果 */
.data-table :deep(.ant-table-tbody > tr:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 表格单元格样式 */
.data-table :deep(.ant-table-tbody > tr > td) {
  padding: 14px;
  text-align: center;
  vertical-align: middle;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

/* 分页器样式 */
.data-table :deep(.ant-pagination) {
  margin: 25px 0 10px 0;
  text-align: center;
}

.data-table :deep(.ant-pagination-item) {
  border-radius: 6px;
  border: 1px solid #e8e8e8;
  transition: all 0.3s ease;
}

.data-table :deep(.ant-pagination-item:hover) {
  border-color: #4096ff;
  color: #4096ff;
}

.data-table :deep(.ant-pagination-item-active) {
  background: #4096ff;
  border-color: #4096ff;
}

.data-table :deep(.ant-pagination-item-active a) {
  color: white;
}

/* 标签样式优化 */
.data-table :deep(.ant-tag) {
  border-radius: 20px;
  padding: 4px 16px;
  font-size: 13px;
  font-weight: 500;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.data-table :deep(.ant-tag:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 操作按钮容器 */
.action-buttons {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

/* 操作按钮样式 */
.action-button {
  width: 100px;
  border-radius: 8px;
  font-weight: 500;
  padding: 6px 16px;
  height: 36px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.action-button:active {
  transform: translateY(0);
}

/* 编辑按钮样式 */
.edit-button {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%) !important;
  border-color: #1890ff !important;
}

.edit-button:hover {
  background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%) !important;
  border-color: #40a9ff !important;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4) !important;
}

/* 删除按钮样式 */
.delete-button {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%) !important;
  border-color: #ff4d4f !important;
}

.delete-button:hover {
  background: linear-gradient(135deg, #ff7875 0%, #ff4d4f 100%) !important;
  border-color: #ff7875 !important;
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.4) !important;
}

/* 空状态样式 */
.data-table :deep(.ant-empty) {
  padding: 60px 0;
}

.data-table :deep(.ant-empty-description) {
  color: #999;
  font-size: 14px;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .search-container {
    flex-direction: column;
    align-items: stretch;
  }

  .search-form {
    min-width: unset;
  }

  .form-row {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .search-item {
    min-height: auto;
  }

  .search-item :deep(.ant-form-item-label) {
    width: 100%;
    justify-content: flex-start;
    padding: 0 0 8px 0;
  }

  .search-input {
    width: 100%;
  }

  .search-right {
    justify-content: center;
    margin-top: 16px;
  }

  .data-table :deep(.ant-table-thead > tr > th) {
    padding: 12px 8px;
    font-size: 12px;
  }

  .data-table :deep(.ant-table-tbody > tr > td) {
    padding: 12px 8px;
    font-size: 12px;
  }

  .action-button {
    padding: 4px 12px;
    height: 32px;
    font-size: 12px;
  }

  .pic-info-grid {
    grid-template-columns: 1fr;
  }
}

/* 图片信息容器样式 */
/* 图片信息卡片样式 - 现代简约设计 */
.pic-info-card {
  border-radius: 12px;
  padding: 12px;
  transition: all 0.3s ease;
}

.pic-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
}

.pic-info-item {
  display: flex;
  flex-direction: column;
  padding: 4px 6px;
  border-radius: 6px;
  background: rgba(245, 247, 250, 0.9);
}

.info-label {
  font-size: 11px;
  color: #333;
  margin-bottom: 2px;
  font-weight: 700;
}

.info-value {
  font-size: 12px;
  font-weight: 450;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

/* 多彩标签颜色样式 */
.tag-color-0 {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  color: #1890ff;
}

.tag-color-0:hover {
  background: linear-gradient(135deg, #bae7ff 0%, #91d5ff 100%);
  box-shadow: 0 4px 10px rgba(24, 144, 255, 0.25);
}

.tag-color-1 {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  color: #52c41a;
}

.tag-color-1:hover {
  background: linear-gradient(135deg, #d9f7be 0%, #b7eb8f 100%);
  box-shadow: 0 4px 10px rgba(82, 196, 26, 0.25);
}

.tag-color-2 {
  background: linear-gradient(135deg, #fff2e8 0%, #ffccc7 100%);
  color: #fa541c;
}

.tag-color-2:hover {
  background: linear-gradient(135deg, #ffccc7 0%, #ffa39e 100%);
  box-shadow: 0 4px 10px rgba(250, 84, 28, 0.25);
}

.tag-color-3 {
  background: linear-gradient(135deg, #f9f0ff 0%, #efdbff 100%);
  color: #722ed1;
}

.tag-color-3:hover {
  background: linear-gradient(135deg, #efdbff 0%, #d3adf7 100%);
  box-shadow: 0 4px 10px rgba(114, 46, 209, 0.25);
}

.tag-color-4 {
  background: linear-gradient(135deg, #fff0f6 0%, #ffadd2 100%);
  color: #eb2f96;
}

.tag-color-4:hover {
  background: linear-gradient(135deg, #ffadd2 0%, #f759ab 100%);
  box-shadow: 0 4px 10px rgba(235, 47, 150, 0.25);
}

.tag-color-5 {
  background: linear-gradient(135deg, #fffbe6 0%, #ffe58f 100%);
  color: #faad14;
}

.tag-color-5:hover {
  background: linear-gradient(135deg, #ffe58f 0%, #ffd666 100%);
  box-shadow: 0 4px 10px rgba(250, 173, 20, 0.25);
}
</style>
