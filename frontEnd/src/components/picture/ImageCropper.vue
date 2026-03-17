<template>
  <a-modal
    class="image-cropper-modal"
    v-model:visible="visible"
    title="裁剪与编辑图片"
    width="800px"
    :footer="false"
    destroyOnClose
    @cancel="closeModal"
  >
    <div class="cropper-wrapper">
      <div class="cropper-toolbar">
        <span class="toolbar-label">裁剪比例：</span>
        <a-radio-group v-model:value="aspectRatio" button-style="solid">
          <a-radio-button value="free">自由比例</a-radio-button>
          <a-radio-button value="1:1">1:1</a-radio-button>
          <a-radio-button value="4:3">4:3</a-radio-button>
          <a-radio-button value="16:9">16:9</a-radio-button>
          <a-radio-button value="3:4">3:4</a-radio-button>
          <a-radio-button value="9:16">9:16</a-radio-button>
        </a-radio-group>
      </div>

      <div class="cropper-content" :style="{ height: cropperHeight + 'px' }">
        <VueCropper
          class="vue-cropper"
          ref="cropperRef"
          :img="imageUrl"
          output-type="png"
          :info="true"
          :can-move-box="true"
          :fixed-box="false"
          :auto-crop="true"
          :center-box="true"
          :fixed="aspectRatio !== 'free'"
          :fixedNumber="currentAspectRatio"
          @img-load="onImgLoad"
        />
      </div>

      <div class="cropper-actions">
        <a-space size="middle">
          <a-button @click="rotateLeft">
            <template #icon><RotateLeftOutlined /></template>
            向左旋转
          </a-button>
          <a-button @click="rotateRight">
            <template #icon><RotateRightOutlined /></template>
            向右旋转
          </a-button>
          <a-button @click="changeScale(1)">
            <template #icon><ZoomInOutlined /></template>
            放大
          </a-button>
          <a-button @click="changeScale(-1)">
            <template #icon><ZoomOutOutlined /></template>
            缩小
          </a-button>
          <a-button
            type="primary"
            :loading="loading"
            @click="handleConfirm"
            class="confirm-btn"
          >
            <template #icon><CheckOutlined /></template>
            确认并上传
          </a-button>
        </a-space>
      </div>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, watch, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import {
  RotateLeftOutlined,
  RotateRightOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
  CheckOutlined
} from '@ant-design/icons-vue'
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'
import { uploadPictureUsingPost } from '@/api/pictureController.ts'

interface Props {
  imageUrl?: string
  picture?: API.PictureVo
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVo) => void
}

const props = defineProps<Props>()

// 响应式数据
const imageUrl = ref(props.imageUrl)
const cropperRef = ref()
const cropperHeight = ref(400)
const aspectRatio = ref('free')
const currentAspectRatio = ref([1, 1])
const loading = ref(false)
const visible = ref(false)

// 监听图片 URL 变化
watch(
  () => props.imageUrl,
  (newVal) => {
    imageUrl.value = newVal
  }
)

// 监听比例变化
watch(
  () => aspectRatio.value,
  async (newVal) => {
    const ratioMap: Record<string, number[]> = {
      '1:1': [1, 1],
      '4:3': [4, 3],
      '16:9': [16, 9],
      '3:4': [3, 4],
      '9:16': [9, 16]
    }
    currentAspectRatio.value = ratioMap[newVal] || [1, 1]

    // 【核心修复】：如果 cropper 实例存在，强制刷新裁剪框
    if (cropperRef.value) {
      // 等待 Vue 将新的 currentAspectRatio 传递给子组件
      await nextTick()
      // 清除旧的裁剪框
      cropperRef.value.clearCrop()
      // 按照新的比例重新生成一个居中的裁剪框
      cropperRef.value.goAutoCrop()
    }
  },
  { immediate: true }
)

// 图片加载成功，动态调整容器高度
const onImgLoad = (data: any) => {
  if (data === 'success') {
    // 兼容某些版本的vue-cropper，不一定返回宽高
    cropperHeight.value = 500
  } else if (data && data.width && data.height) {
    const ratio = data.width / data.height
    const containerWidth = 752 // 800 - padding
    const calculatedHeight = containerWidth / ratio
    cropperHeight.value = Math.max(300, Math.min(600, calculatedHeight))
  } else {
    cropperHeight.value = 500
  }
}

// 控件操作
const changeScale = (num: number) => cropperRef.value?.changeScale(num)
const rotateLeft = () => cropperRef.value?.rotateLeft()
const rotateRight = () => cropperRef.value?.rotateRight()

// 确认裁切
const handleConfirm = () => {
  cropperRef.value.getCropBlob((blob: Blob) => {
    if (!blob) {
      message.error('裁切失败，请重试')
      return
    }
    const fileName = (props.picture?.name || 'image_cropped') + '.png'
    const file = new File([blob], fileName, { type: blob.type })
    handleUpload({ file })
  })
}

// 上传逻辑
const handleUpload = async ({ file }: { file: File }) => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = props.picture ? { id: props.picture.id } : {}
    if (props.spaceId) {
      params.spaceId = props.spaceId
    }
    const res = await uploadPictureUsingPost(params, {}, file)

    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      props.onSuccess?.(res.data.data)
      closeModal()
    } else {
      message.error('图片上传失败: ' + res.data.message)
    }
  } catch (error: any) {
    console.error('图片上传异常', error)
    message.error('图片上传异常: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 弹窗控制
const openModal = () => {
  visible.value = true
}
const closeModal = () => {
  visible.value = false
}

// 暴露函数给父组件
defineExpose({
  openModal,
})
</script>

<style scoped>
.cropper-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cropper-toolbar {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.toolbar-label {
  font-weight: 500;
  margin-right: 12px;
  color: #333;
}

/* 核心裁剪区域：透明网格底图与边框 */
.cropper-content {
  width: 100%;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: inset 0 0 4px rgba(0, 0, 0, 0.05);
  transition: height 0.3s ease;

  /* Photoshop风格的透明网格背景 */
  background-image:
    linear-gradient(45deg, #f0f0f0 25%, transparent 25%),
    linear-gradient(-45deg, #f0f0f0 25%, transparent 25%),
    linear-gradient(45deg, transparent 75%, #f0f0f0 75%),
    linear-gradient(-45deg, transparent 75%, #f0f0f0 75%);
  background-size: 20px 20px;
  background-position: 0 0, 0 10px, 10px -10px, -10px 0px;
}

/* 覆盖 vue-cropper 默认背景，让网格透出来 */
:deep(.vue-cropper) {
  background: transparent !important;
}
:deep(.cropper-modal) {
  background: rgba(0, 0, 0, 0.6) !important;
}

/* 底部操作栏 */
.cropper-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.confirm-btn {
  padding: 0 24px;
}
</style>
