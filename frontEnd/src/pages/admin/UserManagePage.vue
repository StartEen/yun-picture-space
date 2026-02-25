<template>
  <div id="userManagePage">
    <div class="search-container">
      <a-form
        layout="inline"
        :model="searchParams"
        @finish="doSearch"
        class="search-form"
      >
        <div class="form-row">
          <a-form-item label="账号" class="search-item">
            <a-input
              v-model:value="searchParams.userAccount"
              placeholder="请输入账号"
              class="search-input"
            />
          </a-form-item>
          <a-form-item label="用户名" class="search-item">
            <a-input
              v-model:value="searchParams.userName"
              placeholder="请输入用户名"
              class="search-input"
            />
          </a-form-item>
          <a-form-item class="search-item">
            <a-button
              type="primary"
              html-type="submit"
              class="search-button"
            >
              <template #icon>
                <SearchOutlined />
              </template>
              搜索
            </a-button>
          </a-form-item>
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
        <template v-if="['userAccount', 'userName', 'userProfile'].includes(column.dataIndex)">
          <div>
            <a-input
              v-if="editableData[record.id]"
              v-model:value="editableData[record.id][column.dataIndex]"
              style="margin: -5px 0"
            />
            <template v-else>
              {{ record[column.dataIndex] }}
            </template>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar || defaultAvatar" :width="60" />
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole === 'admin'">
            <a-tag color="green">管理员</a-tag>
          </div>
          <div v-else>
            <a-tag color="blue">普通用户</a-tag>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <div class="editable-row-operations">
            <span v-if="editableData[record.id]">
              <a-button type="primary" size="small" @click="save(record.id)" class="action-button" style="margin-right: 15px">
                保存
              </a-button>
              <a-popconfirm title="确定要取消吗？" @confirm="cancel(record.id)">
                <a-button size="small" class="action-button">
                  取消
                </a-button>
              </a-popconfirm>
            </span>
            <span v-else>
              <a-button type="primary" @click="doEdit(record.id)" class="action-button" style="margin-right: 15px">
                编辑
              </a-button>
              <a-button danger @click="doDelete(record.id)" class="action-button delete-btn">
                删除
              </a-button>
            </span>
          </div>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref, type UnwrapRef} from 'vue'
import { SmileOutlined, DownOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { message, type TableColumnsType } from 'ant-design-vue'
import {
  deleteUserUsingPost,
  listUserVoByPageUsingPost,
  updateUserUsingPost
} from '@/api/userController.ts'
import dayjs from 'dayjs' // 显式导入 dayjs
import { cloneDeep } from 'lodash-es'


const defaultAvatar = new URL('@/assets/user.png', import.meta.url).href


const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
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


// 从后端获取数据
// 数据
const dataList = ref([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  const res = await listUserVoByPageUsingPost({
    ...searchParams
  })
  if (res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})


// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
  }
})

// 表格变化处理
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 获取数据
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  fetchData()
}


// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteUserUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}

// 可编辑数据状态
const editableData: UnwrapRef<Record<string, any>> = reactive({})

// 编辑数据 - 开启行内编辑模式
const doEdit = async (id: string) => {
  if (!id) {
    return
  }
  // 找到对应的记录并克隆
  const record = dataList.value.find((item: any) => item.id === id)
  if (record) {
    editableData[id] = cloneDeep(record)
  }
}

// 保存编辑的数据
const save = async (id: string) => {
  if (!id || !editableData[id]) {
    return
  }

  try {
    // 调用更新接口
    const updateData = {
      id: id,
      ...editableData[id]
    }

    const res = await updateUserUsingPost(updateData)
    if (res.data.code === 0) {
      message.success('更新成功')
      // 更新本地数据
      const index = dataList.value.findIndex((item: any) => item.id === id)
      if (index !== -1) {
        dataList.value[index] = { ...dataList.value[index], ...editableData[id] }
      }
      // 清除编辑状态
      delete editableData[id]
    } else {
      message.error('更新失败：' + res.data.message)
    }
  } catch (error) {
    message.error('更新失败')
  }
}

// 取消编辑
const cancel = (id: string) => {
  if (!id) {
    return
  }
  delete editableData[id]
}


</script>



<style scoped>
#userManagePage {
  padding: 24px;
/*  background: linear-gradient(to bottom right, #6bdfed 0%, #215ac6 100%);*/
  min-height: 80vh;
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 10px;
}


/* 搜索容器 */
.search-container {
  margin-bottom: 24px;
  animation: slideDown 0.5s ease-out;
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
  padding: 24px;
  border-radius: 16px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.05),
    0 4px 16px rgba(0, 0, 0, 0.01);
  border: 1px solid rgba(255, 255, 255, 0.01);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
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
  height: 44px;
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
  background: #cccccc;
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
  margin: 5px 0 0 0;
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

/* 头像样式优化 */
.data-table :deep(.ant-image) {
  border-radius: 50%;
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
  border-radius: 50%;
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

/* 操作按钮容器 */
.editable-row-operations {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
}

/* 操作按钮样式 */
.action-button {
  border-radius: 8px;
  font-weight: 500;
  padding: 6px 16px;
  height: 36px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: none;
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.action-button:active {
  transform: translateY(0);
}

/* 删除按钮特殊样式 */
.delete-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  border: none;
  color: white;
}

.delete-btn:hover {
  background: linear-gradient(135deg, #ee5a5a 0%, #ff6b6b 100%);
  box-shadow: 0 4px 12px rgba(238, 90, 90, 0.4);
  color: white !important;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

/* 编辑输入框样式 */
.data-table :deep(.ant-input) {
  border-radius: 6px;
  border: 2px solid #e9ecef;
  transition: all 0.3s ease;
  padding: 6px 12px;
  font-size: 14px;
}

.data-table :deep(.ant-input:hover) {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.data-table :deep(.ant-input:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
  outline: none;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  border-color: #667eea;
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

  .action-button {
    padding: 4px 12px;
    height: 32px;
    font-size: 12px;
  }

  .editable-row-operations {
    gap: 6px;
  }
}

/* 加载动画 */
.data-table :deep(.ant-spin-dot-item) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}


</style>
