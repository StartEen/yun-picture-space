<template>
  <div id="spaceDetailPage">
    <!-- 空间信息 -->
    <div class="space-header">
      <h2>{{ space.spaceName }}（私有空间）</h2>
      <div class="space-actions">
        <a-button
          type="primary"
          class="create-button"
          :href="`/add_picture?spaceId=${id}`"
          target="_blank"
        >
          <template #icon>
            <PlusOutlined />
          </template>
          创建图片
        </a-button>
        <div class="batch-actions">
          <div class="select-all">
            <a-checkbox v-model:checked="selectAll" @change="handleSelectAll">
              <span>全选</span>
            </a-checkbox>
          </div>
          <a-button
            class="batch-edit-button"
            :icon="h(EditOutlined)"
            @click="doBatchEdit"
            :disabled="selectedIds.length === 0"
          >
            批量编辑
            <span class="selected-count" v-if="selectedIds.length > 0"
              >({{ selectedIds.length }})</span
            >
          </a-button>
        </div>
        <a-tooltip
          :title="`占用空间 ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`"
        >
          <a-progress
            type="circle"
            :size="42"
            :percent="((space.totalSize * 100) / space.maxSize).toFixed(1)"
          />
        </a-tooltip>
      </div>
    </div>
    <div style="margin-bottom: 16px" />

    <!-- 搜索区域 -->
    <div class="search-container">
      <!-- 搜索表单 -->
      <PictureSearchForm :onSearch="onSearch" />
      <!-- 按颜色搜索 -->
      <div class="color-search">
        <span>按颜色搜索:</span>
        <color-picker format="hex" @pureColorChange="onColorChange" />
      </div>
    </div>

    <div style="margin-bottom: 16px" />
    <!-- 图片列表 -->
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

  <PictureBathEditModal
    ref="batchEditPictureModalRef"
    :spaceId="Number(id)"
    :pictureList="selectedPictures"
    :onSuccess="onBatchEditPictureSuccess"
  />
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref, computed } from 'vue'
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
import { EditOutlined, PlusOutlined } from '@ant-design/icons-vue'

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
  padding: 20px;
}

#spaceDetailPage .space-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s ease;
}

#spaceDetailPage .space-header:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

#spaceDetailPage .space-header h2 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #333333;
}

#spaceDetailPage .space-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

#spaceDetailPage .batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  transition: all 0.3s ease;
}

#spaceDetailPage .batch-actions:hover {
  background: #f0f0f0;
  border-color: #d9d9d9;
}

#spaceDetailPage .batch-actions :deep(.ant-checkbox) {
  font-size: 14px;
  color: #666666;
  font-weight: 500;
}

/* 创建图片按钮 */
#spaceDetailPage .space-actions :deep(.create-button) {
  border-radius: 8px;
  padding: 10px 24px;
  min-width: 140px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

#spaceDetailPage .space-actions :deep(.create-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.4);
  background: linear-gradient(135deg, #40a9ff 0%, #69c0ff 100%);
}

/* 批量编辑按钮 */
#spaceDetailPage .space-actions :deep(.batch-edit-button) {
  border-radius: 8px;
  padding: 8px 20px;
  min-width: 120px;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s ease;
  border: 1px solid #d9d9d9;
  background: #ffffff;
  color: #333333;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

/* 按钮内部图标和文字对齐 */
#spaceDetailPage .space-actions :deep(.ant-btn > span) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

#spaceDetailPage .space-actions :deep(.ant-btn-icon) {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 搜索容器 */
#spaceDetailPage .search-container {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;
  margin-bottom: 24px;
  transition: box-shadow 0.3s ease;
}

#spaceDetailPage .search-container:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

/* 颜色搜索 */
#spaceDetailPage .color-search {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

#spaceDetailPage .color-search span {
  font-size: 14px;
  font-weight: 500;
  color: #666666;
}

/* 搜索表单样式 */

#spaceDetailPage .space-actions :deep(.batch-edit-button:hover:not(:disabled)) {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-color: #1890ff;
  color: #1890ff;
}

#spaceDetailPage .space-actions :deep(.batch-edit-button:disabled) {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
  border-color: #d9d9d9;
  color: #bfbfbf;
}

/* 选中数量显示 */
#spaceDetailPage .space-actions :deep(.selected-count) {
  margin-left: 4px;
  font-size: 12px;
  font-weight: 400;
  color: #1890ff;
  background: rgba(24, 144, 255, 0.1);
  padding: 2px 6px;
  border-radius: 10px;
}

/* 全选复选框 */
#spaceDetailPage .space-actions :deep(.select-all) {
  display: flex;
  align-items: center;
}

#spaceDetailPage .space-actions :deep(.select-all .ant-checkbox) {
  font-size: 14px;
  color: #666666;
  font-weight: 500;
}

#spaceDetailPage
  .space-actions
  :deep(.select-all .ant-checkbox-input:checked + .ant-checkbox-inner) {
  background-color: #1890ff;
  border-color: #1890ff;
}

#spaceDetailPage .space-actions :deep(.ant-tooltip) {
  font-size: 12px;
}

#spaceDetailPage .space-actions :deep(.ant-progress-circle) {
  transition: transform 0.3s ease;
}

#spaceDetailPage .space-actions :deep(.ant-progress-circle:hover) {
  transform: scale(1.05);
}

#spaceDetailPage .pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 16px 0;
}

#spaceDetailPage .pagination-container :deep(.ant-pagination) {
  display: flex;
  justify-content: center;
}

#spaceDetailPage .pagination-container :deep(.ant-pagination-item) {
  border-radius: 4px;
}

#spaceDetailPage .pagination-container :deep(.ant-pagination-item-active) {
  background-color: #1890ff;
  border-color: #1890ff;
}

#spaceDetailPage .pagination-container :deep(.ant-pagination-item-active a) {
  color: #fff;
}
</style>
