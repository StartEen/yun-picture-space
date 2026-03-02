<template>
  <div id="pictureManagePage">
    <div class="search-container">
      <a-form layout="inline" :model="searchParams" @finish="doSearch" class="search-form">
        <div class="form-row">
          <a-form-item label="关键字" name="searchText" class="search-item">
            <a-input
              v-model:value="searchParams.searchText"
              placeholder="从名称或简介搜索"
              class="search-input"
              allow-clear
            />
          </a-form-item>
          <a-form-item label="类型" name="category" class="search-item">
            <a-input
              v-model:value="searchParams.category"
              placeholder="请输入类型"
              class="search-input"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="标签" name="tag" class="search-item">
            <a-input
              v-model:value="searchParams.tags"
              placeholder="请输入标签"
              class="search-input"
              allow-clear
              style="min-width: 180px"
            />
          </a-form-item>

          <a-form-item class="search-item">
            <a-button type="primary" html-type="submit" class="search-button">
              <SearchOutlined />
              搜索
            </a-button>
          </a-form-item>
          <div class="search-right">
            <a-button type="primary" href="/add_picture" target="_blank" class="create-button">
              <AppstoreAddOutlined />
              创建图片
            </a-button>
          </div>
        </div>
      </a-form>
    </div>

    <!--表格-->
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
      class="data-table"
      :row-key="(record) => record.id"
      :scroll="{ x: 1440 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'url'">
          <a-image :src="record.url" :width="120" />
        </template>
        <!--标签-->
        <template v-if="column.dataIndex === 'tags'">
          <a-space wrap>
            <a-tag v-for="tag in record.tags" :key="tag">
              {{ tag }}
            </a-tag>
          </a-space>
        </template>
        <!--图片信息-->
        <template v-if="column.dataIndex === 'picInfo'">
          <div class="pic-info-item">格式：{{ record.picFormat }}</div>
          <div class="pic-info-item">宽度：{{ record.picWidth }}</div>
          <div class="pic-info-item">高度：{{ record.picHeight }}</div>
          <div class="pic-info-item">宽高比：{{ record.picScale }}</div>
          <div class="pic-info-item">大小：{{ (record.picSize / 1024).toFixed(2) }}KB</div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="primary" @click="doDelete(record.id)">
              <DeleteOutlined />
              删除
            </a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { deletePictureUsingPost, listPictureVoByPageUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import {
  EditOutlined,
  SearchOutlined,
  DeleteOutlined,
  AppstoreAddOutlined,
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '图片',
    dataIndex: 'url',
    width: 140,
  },
  {
    title: '名称',
    dataIndex: 'name',
    width: 150,
    ellipsis: true,
  },
  {
    title: '简介',
    dataIndex: 'introduction',
    width: 180,
    ellipsis: true,
  },
  {
    title: '类型',
    dataIndex: 'category',
    width: 100,
    ellipsis: true,
  },
  {
    title: '标签',
    dataIndex: 'tags',
    width: 150,
  },
  {
    title: '图片信息',
    dataIndex: 'picInfo',
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
    width: 180,
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 100,
    fixed: 'right',
  },
]


// 数据
const dataList = ref<API.Picture[]>([])
const total = ref(0)

//搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
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
    showTotal: (total) => `共 ${total} 条`,
  }
})

// 获取数据
const fetchData = async () => {
  const res = await listPictureVoByPageUsingPost({
    ...searchParams,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

//页面加载时请求一次
onMounted(() => {
  fetchData()
})

// 获取数据
const doSearch = () => {
  //重置搜索条件
  searchParams.current = 1
  fetchData()
}
// 表格变化处理
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}
</script>

<style scoped>
#pictureManagePage {
  padding: 24px;
  min-height: 80vh;
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 10px;
  position: relative;
}

#pictureManagePage::before {
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
  gap: 20px; /* 增加间距 */
  align-items: center; /* 垂直居中对齐 */
  justify-content: space-between; /* 让创建按钮靠右对齐 */
  padding: 8px 0;
}

/* 搜索项 - 关键优化：统一高度和对齐 */
.search-item {
  margin: 0;
  display: flex;
  align-items: center;
  min-height: 44px; /* 统一最小高度 */
}

.search-item :deep(.ant-form-item-label) {
  padding: 0 12px 0 0;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  width: 70px; /* 固定标签宽度，确保对齐 */
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
  height: 44px; /* 固定高度确保一致性 */
  box-sizing: border-box;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #2c3e50;
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
  gap: 2px; /* 调整图标和文字的间距 */
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

/* 创建图片按钮 */
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

/* 响应式设计 */
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
}

/* 表格样式 */
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

/* 图片预览样式 */
.data-table :deep(.ant-image) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  display: inline-block;
}

.data-table :deep(.ant-image:hover) {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.data-table :deep(.ant-image img) {
  border-radius: 8px;
  object-fit: cover;
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

/* 图片信息样式 */
.pic-info-item {
  margin: 4px 0;
  font-size: 13px;
  color: #5a6c7d;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.pic-info-item::before {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #4096ff;
  margin-right: 8px;
}

/* 操作按钮样式 */
.data-table :deep(.ant-btn-primary) {
  border-radius: 8px;
  font-weight: 500;
  padding: 6px 16px;
  height: 36px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: none;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
}

.data-table :deep(.ant-btn-primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(238, 90, 90, 0.4);
  background: linear-gradient(135deg, #ee5a5a 0%, #ff6b6b 100%);
  color: white !important;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.data-table :deep(.ant-btn-primary:active) {
  transform: translateY(0);
}

/* 分页器样式优化 */
.data-table :deep(.ant-pagination) {
  margin: 10px 0 0 0;
  padding: 8px;
}

.data-table :deep(.ant-pagination-item) {
  border-radius: 8px;
  border: 2px solid #e9ecef;
  transition: all 0.3s ease;
  font-weight: 500;
}

.data-table :deep(.ant-pagination-item:hover) {
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-2px);
}

.data-table :deep(.ant-pagination-item-active) {
  background: linear-gradient(135deg, #72a9ec 0%, #6cc8f8 100%);
  border-color: transparent;
}

.data-table :deep(.ant-pagination-item-active a) {
  color: white;
}

.data-table :deep(.ant-pagination-prev),
.data-table :deep(.ant-pagination-next) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.data-table :deep(.ant-pagination-prev:hover),
.data-table :deep(.ant-pagination-next:hover) {
  border-color: #3ab9fd;
  color: #667eea;
}

.data-table :deep(.ant-pagination-options) {
  display: flex;
  align-items: center;
  gap: 8px;
}

.data-table :deep(.ant-select-selector) {
  border-radius: 8px !important;
  border: 2px solid #e9ecef !important;
  transition: all 0.3s ease !important;
}

.data-table :deep(.ant-select-selector:hover) {
  border-color: #667eea !important;
}

/* 表格滚动条样式 */
.data-table :deep(.ant-table-body) {
  overflow-x: auto !important;
}

.data-table :deep(.ant-table-body::-webkit-scrollbar) {
  height: 8px;
}

.data-table :deep(.ant-table-body::-webkit-scrollbar-thumb) {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
}

.data-table :deep(.ant-table-body::-webkit-scrollbar-track) {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

/* 固定列样式 */
.data-table :deep(.ant-table-fixed-right) {
  box-shadow: -4px 0 8px rgba(0, 0, 0, 0.05);
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
  .data-table :deep(.ant-table-thead > tr > th) {
    padding: 12px 8px;
    font-size: 12px;
  }

  .data-table :deep(.ant-table-tbody > tr > td) {
    padding: 12px 8px;
    font-size: 12px;
  }

  .data-table :deep(.ant-image) {
    width: 80px !important;
  }

  .pic-info-item {
    font-size: 12px;
  }
}

/* 加载动画 */
.data-table :deep(.ant-spin-dot-item) {
  /*background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);*/
}

</style>
