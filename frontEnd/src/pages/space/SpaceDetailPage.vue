<template>
  <div id="spaceDetailPage">
    <!-- 空间信息 -->
    <div class="space-header">
      <h2>{{ space.spaceName }}（私有空间）</h2>
      <div class="space-actions">
        <a-button type="primary" :href="`/add_picture?spaceId=${id}`" target="_blank">
          + 创建图片
        </a-button>
        <div class="batch-actions">
          <a-checkbox v-model:checked="selectAll" @change="handleSelectAll">全选</a-checkbox>
          <a-button
            :icon="h(EditOutlined)"
            @click="doBatchEdit"
            :disabled="selectedIds.length === 0"
          >
            批量编辑 ({{ selectedIds.length }})
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

    <!-- 搜索表单 -->
    <PictureSearchForm :onSearch="onSearch" />
    <!--按颜色搜索-->
    <div style="margin-top: 16px">
      <span>按颜色搜索:</span>
      <color-picker format="hex" @pureColorChange="onColorChange" />
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
import { EditOutlined } from '@ant-design/icons-vue'

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
}

#spaceDetailPage .space-header h2 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
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
  padding: 0 16px;
  background: #f5f5f5;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

#spaceDetailPage .batch-actions :deep(.ant-checkbox) {
  font-size: 14px;
  color: #666;
}

#spaceDetailPage .space-actions :deep(.ant-btn) {
  border-radius: 8px;
  padding: 0 24px;
  min-width: 120px;
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
