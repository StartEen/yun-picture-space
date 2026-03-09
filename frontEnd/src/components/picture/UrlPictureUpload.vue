<template>
  <div class="url-picture-upload">
    <a-input-group compact>
      <a-input
        v-model:value="fileUrl"
        style="width: calc(100% - 120px)"
        placeholder="请输入图片地址"
        class="url-input"
      />
      <a-button
        type="primary"
        style="width: 120px"
        :loading="loading"
        @click="handleUpload"
        class="upload-button"
      >
        提交
      </a-button>
    </a-input-group>
    <div class="img-wrapper">
      <img v-if="picture?.url" :src="picture?.url" alt="avatar" class="preview-image" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { uploadPictureByUrlUsingPost } from '@/api/pictureController.ts'

interface Props {
  picture?: API.PictureVO
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()
const loading = ref<boolean>(false)
const fileUrl = ref<string>()

/**
 * 上传
 */
const handleUpload = async () => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = {
      fileUrl: fileUrl.value,
    }
    params.spaceId = props.spaceId;
    if (props.picture) {
      params.id = props.picture.id
    }
    const res = await uploadPictureByUrlUsingPost(params)

    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
    } else {
      message.error('图片上传失败,' + res.data.message)
    }
  } catch (error) {
    message.error('图片上传失败,' + error.data.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.url-picture-upload {
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

.url-picture-upload:hover {
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

/* 输入框样式 */
.url-input {
  border-radius: 8px 0 0 8px !important;
  border: 2px solid #e9ecef !important;
  transition: all 0.3s ease !important;
  font-size: 14px !important;
  color: #2c3e50 !important;
  height: 44px !important;
}

.url-input:hover {
  border-color: #4096ff !important;
  box-shadow: 0 0 0 2px rgba(64, 150, 255, 0.2) !important;
}

.url-input:focus {
  border-color: #4096ff !important;
  box-shadow: 0 0 0 2px rgba(64, 150, 255, 0.2) !important;
  outline: none !important;
  font-weight: 500 !important;
}

/* 上传按钮样式 */
.upload-button {
  border-radius: 0 8px 8px 0 !important;
  height: 44px !important;
  font-weight: 600 !important;
  background: linear-gradient(45deg, #4096ff, #1890ff) !important;
  border: none !important;
  transition: all 0.3s ease !important;
  box-shadow: 0 4px 12px rgba(64, 150, 255, 0.3) !important;
  font-size: 14px !important;
  letter-spacing: 0.5px !important;
}

.upload-button:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 6px 16px rgba(64, 150, 255, 0.4) !important;
  background: linear-gradient(45deg, #1890ff, #4096ff) !important;
}

.upload-button:active {
  transform: translateY(0) !important;
}

/* 图片预览容器 */
.img-wrapper {
  width: 100%;
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

.img-wrapper:hover {
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

/* 响应式设计 */
@media (max-width: 768px) {
  .url-picture-upload {
    padding: 16px;
  }

  .url-input {
    height: 40px !important;
    font-size: 13px !important;
  }

  .upload-button {
    height: 40px !important;
    font-size: 13px !important;
  }

  .img-wrapper {
    min-height: 150px;
  }

  .preview-image {
    max-height: 200px;
  }
}
</style>
