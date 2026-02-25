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
        </div>
      </a-form>
    </div>











  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { listPictureVoByPageUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '图片',
    dataIndex: 'url',
  },
  {
    title: '名称',
    dataIndex: 'name',
  },
  {
    title: '简介',
    dataIndex: 'introduction',
    ellipsis: true,
  },
  {
    title: '类型',
    dataIndex: 'category',
  },
  {
    title: '标签',
    dataIndex: 'tags',
  },
  {
    title: '图片信息',
    dataIndex: 'picInfo',
  },
  {
    title: '用户 id',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 数据
const dataList = ref([])
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
  if (res.data.data) {
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
</script>

<style scoped>
#pictureManagePage {
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

</style>
