<template>
  <div class="picture-upload">
    <a-upload
      list-type="picture-card"
      :show-upload-list="false"
      :custom-request="handleUpload"
      :before-upload="beforeUpload"
      class="upload-container"
    >
      <img v-if="picture?.url" :src="picture?.url" alt="avatar" class="preview-image" />
      <div v-else class="upload-placeholder">
        <loading-outlined v-if="loading" class="upload-icon"></loading-outlined>
        <plus-outlined v-else class="upload-icon"></plus-outlined>
        <div class="upload-text">点击或拖拽上传图片</div>
      </div>
    </a-upload>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { UploadChangeParam, UploadProps } from 'ant-design-vue'
import { uploadPictureUsingPost } from '@/api/pictureController.ts'

interface Props {
  picture?: API.PictureVO
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()

// 上传前校验图片
const beforeUpload = (file: UploadProps['fileList'][number]) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    message.error('不支持上传该格式的图片，推荐 jpg 或 png')
  }
  const isLt2M = file.size / 1024 / 1024 < 4
  if (!isLt2M) {
    message.error('不能上传超过 4M 的图片')
  }
  return isJpgOrPng && isLt2M
}
const loading = ref<boolean>(false)

/**
 * 上传
 * @param file
 */
const handleUpload = async ({ file }: any) => {
  loading.value = true
  try {
    const params = props.picture ? { id: props.picture.id } : {}
    params.spaceId = props.spaceId
    const res = await uploadPictureUsingPost(params, {}, file)
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
    } else {
      message.error('图片上传失败，' + res.data.message)
    }
  } catch (error) {
    message.error('图片上传失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.picture-upload {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.05),
    0 4px 16px rgba(0, 0, 0, 0.01);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeInUp 0.6s ease-out;
}

.picture-upload:hover {
  transform: translateY(-2px);
  box-shadow:
    0 12px 40px rgba(0, 0, 0, 0.08),
    0 6px 20px rgba(0, 0, 0, 0.05);
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

/* 上传容器 */
.upload-container {
  width: 100% !important;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: #f8f9fa;
  border: 2px dashed #e9ecef;
  transition: all 0.3s ease;
  overflow: hidden;
}

.upload-container:hover {
  border-color: #4096ff;
  background: #f0f7ff;
}

/* 预览图片 */
.preview-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.preview-image:hover {
  transform: scale(1.02);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

/* 上传占位符 */
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  padding: 40px 20px;
  text-align: center;
}

/* 上传图标 */
.upload-icon {
  font-size: 48px;
  color: #999;
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.upload-container:hover .upload-icon {
  color: #4096ff;
  transform: scale(1.1);
}

/* 上传文本 */
.upload-text {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
  transition: all 0.3s ease;
}

.upload-container:hover .upload-text {
  color: #4096ff;
}

/* 移除默认的 Ant Design 上传组件样式限制 */
.picture-upload :deep(.ant-upload) {
  width: 100% !important;
  height: 100% !important;
  min-height: 400px;
  min-width: 100%;
}

.picture-upload :deep(.ant-upload-select-picture-card) {
  width: 100% !important;
  height: 100% !important;
  border: none !important;
  background: transparent !important;
  margin: 0 !important;
  padding: 0 !important;
}

.picture-upload :deep(.ant-upload-select-picture-card > div) {
  width: 100% !important;
  height: 100% !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .picture-upload {
    padding: 16px;
  }

  .upload-container {
    height: 300px;
  }

  .picture-upload :deep(.ant-upload) {
    min-height: 300px;
  }

  .preview-image {
    max-height: 200px;
  }

  .upload-placeholder {
    padding: 30px 16px;
  }

  .upload-icon {
    font-size: 32px;
    margin-bottom: 12px;
  }

  .upload-text {
    font-size: 13px;
  }
}
</style>
