<template>
  <div class="search-container-form">
    <a-form name="searchForm" :model="searchParams" layout="inline" @finish="doSearch">
      <div class="main-search-row">
        <a-form-item label="关键词">
          <a-input
            v-model:value="searchParams.searchText"
            placeholder="搜索名称/简介"
            allow-clear
            class="search-input"
          />
        </a-form-item>

        <a-form-item label="分类">
          <a-auto-complete
            v-model:value="searchParams.category"
            :options="categoryOptions"
            placeholder="分类"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>

        <a-form-item label="标签">
          <a-select
            v-model:value="searchParams.tags"
            mode="tags"
            :options="tagOptions"
            placeholder="标签"
            allow-clear
            style="min-width: 180px"
          />
        </a-form-item>
        <a-form-item label="尺寸">
          <div class="dimension-input">
            <a-input-number v-model:value="searchParams.picWidth" placeholder="宽" />
            <span class="sep">×</span>
            <a-input-number v-model:value="searchParams.picHeight" placeholder="高" />
          </div>
        </a-form-item>
        <div class="action-buttons">
          <a-button type="primary" html-type="submit">搜索</a-button>
          <a-button @click="doClear">重置</a-button>
          <a @click="activeKey = activeKey.length ? [] : ['1']" class="advanced-trigger">
            {{ activeKey.length ? '收起筛选' : '高级筛选' }}
            <component :is="activeKey.length ? 'UpOutlined' : 'DownOutlined'" />
          </a>
        </div>
      </div>

      <transition name="slide-fade">
        <div v-if="activeKey.includes('1')" class="advanced-section">
          <a-divider dashed />
          <div class="advanced-grid">
            <a-form-item label="日期范围">
              <a-range-picker
                v-model:value="dateRange"
                format="YYYY-MM-DD"
                @change="onRangeChange"
              />
            </a-form-item>
            <a-form-item label="格式">
              <a-input v-model:value="searchParams.picFormat" placeholder="如: jpg" />
            </a-form-item>
          </div>
        </div>
      </transition>
    </a-form>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { listPictureTagCategoryUsingGet } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

interface Props {
  onSearch?: (searchParams: API.PictureQueryRequest) => void
}
const props = defineProps<Props>()

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({})

// 折叠面板状态
const activeKey = ref<string[]>([])

// 获取数据
const doSearch = () => {
  props.onSearch?.(searchParams)
}

const dateRange = ref<[]>([])

/**
 *日期发哪位更改时触发
 */
const onRangeChange = (dates: any[], dateStrings: string[]) => {
  if (dates.length < 2) {
    searchParams.startEditTime = undefined
    searchParams.endEditTime = undefined
  } else {
    searchParams.startEditTime = dates[0].toDate()
    searchParams.endEditTime = dates[1].toDate()
  }
}
const rangePresets = ref([
  { label: '过去7天', value: [dayjs().add(-7, 'd'), dayjs()] },
  { label: '过去14天', value: [dayjs().add(-14, 'd'), dayjs()] },
  { label: '过去30天', value: [dayjs().add(-30, 'd'), dayjs()] },
  { label: '过去60天', value: [dayjs().add(-60, 'd'), dayjs()] },
  { label: '过去90天', value: [dayjs().add(-90, 'd'), dayjs()] },
])

const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])

//获取标签和分类选项
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
  } else {
    message.error('获取标签和分类选项失败,' + res.data.message)
  }
}
onMounted(() => {
  getTagCategoryOptions()
})

//清理
const doClear = () => {
  //取消所有对象的值
  Object.keys(searchParams).forEach((key) => {
    searchParams[key] = undefined
  })
  dataRange.value = []
  props.onSearch?.(searchParams)
}
</script>

<style scoped>
.search-container-form {
  padding: 0;
  background: transparent;
}

/* 顶层主搜索行 */
.main-search-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  width: 100%;
}

:deep(.ant-form-inline .ant-form-item) {
  margin-inline-end: 0;
  margin-bottom: 0;
  display: flex;
  align-items: center;
}

:deep(.ant-form-item-label) {
  white-space: nowrap;
  margin-right: 8px;
}

/* 输入框统一样式 */
:deep(.ant-input),
:deep(.ant-select-selector),
:deep(.ant-input-number),
:deep(.ant-picker) {
  border-radius: 6px !important;
  border-color: #d9d9d9 !important;
  background-color: #ffffff !important;
  transition: all 0.3s ease;
}

:deep(.ant-input:focus),
:deep(.ant-select-selector:focus),
:deep(.ant-input-number:focus),
:deep(.ant-picker:focus) {
  border-color: #1890ff !important;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2) !important;
}

/* 按钮与触发器 */
.action-buttons {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}

.advanced-trigger {
  font-size: 13px;
  color: #8c8c8c;
  cursor: pointer;
  user-select: none;
  display: flex;
  align-items: center;
  gap: 4px;
}

.advanced-trigger:hover {
  color: #1890ff;
}

/* 高级筛选区域 */
.advanced-section {
  width: 100%;
  overflow: hidden;
}

.advanced-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 8px 0;
  align-items: center;
}

.advanced-grid .ant-form-item {
  margin-bottom: 0;
  display: flex;
  align-items: center;
}

.dimension-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dimension-input .sep {
  color: #bfbfbf;
}

/* 动画效果 */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease-out;
  max-height: 200px;
}
.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(-10px);
  opacity: 0;
  max-height: 0;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .main-search-row {
    flex-direction: column;
    align-items: stretch;
  }

  .action-buttons {
    margin-left: 0;
    justify-content: space-between;
  }

  .advanced-grid {
    flex-direction: column;
    align-items: stretch;
  }

  .advanced-grid .ant-form-item {
    flex-direction: row;
    justify-content: space-between;
  }

  .advanced-grid .ant-picker {
    flex: 1;
  }
}
</style>
