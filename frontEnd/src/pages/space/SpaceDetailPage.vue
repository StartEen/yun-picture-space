<template>
  <div id="spaceDetailPage">
    <div class="control-panel">
      <div class="panel-header">
        <div class="space-info">
          <div class="space-title">
            <h2>{{ space.spaceName }}</h2>
            <span class="space-tag">私有空间</span>
          </div>
          <a-tooltip
            :title="`占用空间 ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`"
          >
            <a-progress
              type="circle"
              :size="42"
              :stroke-width="7"
              :percent="Number(((space.totalSize * 100) / space.maxSize).toFixed(1))"
            />
          </a-tooltip>
        </div>

        <div class="action-group">
          <div class="batch-controls">
            <a-checkbox
              v-model:checked="selectAll"
              @change="handleSelectAll"
              class="select-checkbox"
            >
              全选
            </a-checkbox>
            <div class="divider-v-small"></div>
            <a-button
              type="text"
              class="btn-batch-edit"
              :icon="h(EditOutlined)"
              @click="doBatchEdit"
              :disabled="selectedIds.length === 0"
            >
              批量编辑
              <span class="count-badge" v-if="selectedIds.length > 0">{{
                selectedIds.length
              }}</span>
            </a-button>
          </div>

          <div class="divider-v"></div>

          <a-button class="btn-light-primary" :href="`/ai_create?spaceId=${id}`" target="_blank">
            <template #icon><FullscreenOutlined /></template>
            AI 绘画
          </a-button>
          <a-button
            type="primary"
            class="btn-primary"
            :href="`/add_picture?spaceId=${id}`"
            target="_blank"
          >
            <template #icon><PlusOutlined /></template>
            创建图片
          </a-button>
          <a-button
            type="primary"
            class="btn-primary"
            :href="`/space_analyze?spaceId=${id}`"
            target="_blank"
          >
            <template #icon><BarChartOutlined /></template>
            空间分析
          </a-button>
          <a-button
            type="primary"
            class="btn-primary"
            :href="`/space_user/manage/${id}`"
            target="_blank"
          >
            <template #icon><TeamOutlined /></template>
            成员管理
          </a-button>
        </div>
      </div>

      <div class="panel-search">
        <div class="search-form-wrapper">
          <PictureSearchForm :onSearch="onSearch" />
        </div>
        <div class="filter-color">
          <span class="filter-label">颜色筛选:</span>
          <color-picker format="hex" @pureColorChange="onColorChange" />
        </div>
      </div>
    </div>

    <div class="list-container">
      <PictureList
        :dataList="dataList"
        :loading="loading"
        :showOp="true"
        :onReload="fetchData"
        :selectable="true"
        :selectedIds="selectedIds"
        @selectChange="handleSelectChange"
      />
    </div>
  </div>

  <PictureBathEditModal
    ref="batchEditPictureModalRef"
    :spaceId="Number(id)"
    :pictureList="selectedPictures"
    :onSuccess="onBatchEditPictureSuccess"
  />
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref, computed, watch } from 'vue'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import {
  listPictureVoByPageUsingPost,
  searchPictureByColorUsingPost,
} from '@/api/pictureController.ts'
import { formatSize } from '@/utils'
import PictureList from '@/components/picture/PictureList.vue'
import PictureSearchForm from '@/components/picture/PictureSearchForm.vue'
import { ColorPicker } from 'vue3-colorpicker'
import 'vue3-colorpicker/style.css'
import PictureBathEditModal from '@/components/modal/PictureBathEditModal.vue'
import {
  EditOutlined,
  PlusOutlined,
  FullscreenOutlined,
  BarChartOutlined,
  TeamOutlined,
} from '@ant-design/icons-vue'

interface Props {
  id: string | number
}

const props = defineProps<Props>()
const space = ref<API.SpaceVo>({})

// -------- 获取空间详情 --------
const fetchSpaceDetail = async () => {
  try {
    const res = await getSpaceVoByIdUsingGet({
      id: props.id,
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    } else {
      message.error('获取空间详情失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取空间详情失败：' + e.message)
  }
}

onMounted(() => {
  fetchSpaceDetail()
})

// --------- 获取图片列表 --------

// 定义数据
const dataList = ref<API.PictureVo[]>([])
const total = ref(0)
const loading = ref(true)

// 搜索条件
const searchParams = ref<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params = {
    spaceId: props.id,
    ...searchParams.value,
  }
  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
    // 清空选择状态
    selectedIds.value = []
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

// 监听路由参数变化，重新加载数据
watch(
  () => props.id,
  (newId) => {
    if (newId) {
      fetchSpaceDetail()
      fetchData()
    }
  },
)

// 页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const onPageChange = (page: number, pageSize: number) => {
  searchParams.value.current = page
  searchParams.value.pageSize = pageSize
  fetchData()
}

// 搜索
const onSearch = (newSearchParams: API.PictureQueryRequest) => {
  searchParams.value = {
    ...searchParams.value,
    ...newSearchParams,
    current: 1,
  }
  fetchData()
}

// 按颜色搜索
const onColorChange = async (color: string) => {
  const res = await searchPictureByColorUsingPost({
    picColor: color,
    spaceId: props.id,
  })
  if (res.data.code === 0 && res.data.data) {
    const data = res.data.data ?? []
    dataList.value = data
    total.value = data.length
    // 清空选择状态
    selectedIds.value = []
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// ---- 图片选择功能 -----
const selectedIds = ref<number[]>([])
const selectAll = ref(false)

// 处理选择变更
const handleSelectChange = (ids: number[]) => {
  selectedIds.value = ids
  // 更新全选状态
  selectAll.value = ids.length > 0 && ids.length === dataList.value.length
}

// 处理全选
const handleSelectAll = (e: any) => {
  const checked = e.target.checked
  if (checked) {
    // 全选
    selectedIds.value = dataList.value
      .map((item: API.PictureVo) => item.id)
      .filter(Boolean) as number[]
  } else {
    // 取消全选
    selectedIds.value = []
  }
}

// 选中的图片列表
const selectedPictures = computed(() => {
  return dataList.value.filter((item: API.PictureVo) => selectedIds.value.includes(item.id))
})

// ---- 批量编辑图片 -----
const batchEditPictureModalRef = ref()

// 批量编辑图片成功
const onBatchEditPictureSuccess = () => {
  fetchData()
}

// 打开批量编辑图片弹窗
const doBatchEdit = () => {
  if (selectedIds.value.length === 0) {
    message.warning('请先选择要编辑的图片')
    return
  }
  if (batchEditPictureModalRef.value) {
    batchEditPictureModalRef.value.openModal()
  }
}
</script>

<style scoped>
#spaceDetailPage {
  max-width: 1450px;
  margin: 0 auto;
  padding: 24px;
}

/* 统一控制面板 */
.control-panel {
  background: #ffffff;
  border-radius: 12px;
  box-shadow:
    0 4px 24px -4px rgba(0, 0, 0, 0.04),
    0 1px 4px -1px rgba(0, 0, 0, 0.02);
  padding: 20px 24px;
  margin-bottom: 24px;
  border: 1px solid #f0f2f5;
}

/* 头部区域 */
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 左侧标题与信息 */
.space-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.space-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.space-title h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1d2129;
  margin: 0;
  line-height: 1.2;
}

.space-tag {
  background: #e8f3ff;
  color: #1677ff;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
}

/* 环形进度条微调：增大且支持悬浮动画 */
:deep(.ant-progress-circle) {
  cursor: help;
  transition: transform 0.3s ease;
}
:deep(.ant-progress-circle:hover) {
  transform: scale(1.05);
}

/* 右侧操作组合 */
.action-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 分割线 */
.divider-v {
  width: 1px;
  height: 24px;
  background-color: #e5e6eb;
  margin: 0 4px;
}
.divider-v-small {
  width: 1px;
  height: 14px;
  background-color: #dcdfe6;
}

/* 批量操作控件（类 Mac 分段控件风格） */
.batch-controls {
  display: flex;
  align-items: center;
  background: #f2f3f5;
  border-radius: 8px;
  padding: 4px;
  gap: 6px;
  transition: background 0.3s;
}
.batch-controls:hover {
  background: #e5e6eb;
}

.select-checkbox {
  margin: 0;
  padding: 4px 10px;
  color: #4e5969;
  font-weight: 500;
}

/* 批量编辑按钮 - 加入对齐属性 */
.btn-batch-edit {
  border-radius: 6px !important;
  height: 30px !important;
  padding: 0 12px !important;
  color: #4e5969 !important;
  font-weight: 500;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
}
.btn-batch-edit:hover:not(:disabled) {
  background: #ffffff !important;
  color: #1677ff !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.btn-batch-edit:disabled {
  color: #c9cdd4 !important;
  background: transparent !important;
}

/* 数量角标 */
.count-badge {
  background: #1677ff;
  color: #fff;
  font-size: 12px;
  padding: 0 6px;
  border-radius: 10px;
  margin-left: 6px;
  line-height: 18px;
  display: inline-block;
}

/* 核心操作按钮 - 增加 inline-flex 实现绝对上下居中 */
.btn-light-primary {
  background: #e8f3ff !important;
  color: #1677ff !important;
  border: none !important;
  border-radius: 8px !important;
  height: 38px !important;
  font-weight: 500;
  box-shadow: none !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  gap: 6px;
}
.btn-light-primary:hover {
  background: #d4e8ff !important;
  transform: translateY(-1px);
}

.btn-primary {
  border-radius: 8px !important;
  height: 38px !important;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.2) !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  gap: 6px;
}
.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3) !important;
}

/* 搜索与过滤区域：取消 flex 强制排列，让 SearchForm 原生展开 */
.panel-search {
  display: block; /* 恢复为 block 防止挤压内部栅格 */
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px dashed #e5e6eb;
}

.search-form-wrapper {
  width: 100%;
}

/* 颜色筛选器：调整为独立的块，放在搜索组件下方 */
.filter-color {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  background: #f7f8fa;
  padding: 6px 14px;
  border-radius: 8px;
  border: 1px solid #f0f2f5;
  margin-top: 16px; /* 距离上方表单的间距 */
}
.filter-label {
  font-size: 13px;
  font-weight: 500;
  color: #4e5969;
}

/* 全局分页器样式覆盖 */
:deep(.ant-pagination-item) {
  border-radius: 6px;
}
:deep(.ant-pagination-item-active) {
  font-weight: 600;
}
</style>
