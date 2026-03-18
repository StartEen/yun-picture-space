<template>
  <div id="url-picture-upload-container">
    <div class="url-upload-input-container">
      <a-input-search
        v-model:value="fileUrl"
        placeholder="请输入图片地址"
        size="large"
        class="url-input-search"
        @search="handleUpload"
      >
        <template #enterButton>
          <a-button type="primary" size="large" :loading="loading"> 提交 </a-button>
        </template>
      </a-input-search>
    </div>
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
  picture?: API.PictureVo
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVo) => void
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
    params.spaceId = props.spaceId
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
  } catch (error: any) {
    message.error('图片上传失败,' + (error.data?.message || '未知错误'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
#url-picture-upload-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 24px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

/* --- 输入容器（参考 HomePage 搜索框样式） --- */
.url-upload-input-container {
  width: 100%;
}

/* --- 输入搜索框样式 --- */
.url-input-search {
  width: 100%;
}

.url-input-search :deep(.ant-input-group .ant-input) {
  /* 加上 !important 确保右侧直角不被聚焦状态覆盖 */
  border-radius: 8px 0 0 8px !important;
  padding-left: 16px;
}

.url-input-search :deep(.ant-input-group .ant-btn) {
  border-radius: 0 8px 8px 0 !important;
  padding: 0 32px;
  min-width: 120px !important;
  position: relative;
  z-index: 2;
}

/* --- 图片预览容器 --- */
.img-wrapper {
  width: 100%;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: #fafafa;
  border: 1px dashed #cbd5e1;
  transition: all 0.3s ease;
  overflow: hidden;
}

.img-wrapper:hover {
  border-color: #1677ff;
  background: #f0f7ff;
}

.preview-image {
  max-width: 100%;
  max-height: 300px;
  object-fit: contain;
  border-radius: 8px;
  transition: all 0.3s ease;
}

/* --- 响应式适配 --- */
@media (max-width: 767px) {
  #url-picture-upload {
    padding: 16px;
    border-radius: 12px;
  }

  .url-upload-container {
    max-width: 100%;
  }

  .url-input-search :deep(.ant-input-group) {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .url-input-search :deep(.ant-input-group) {
    width: 100%;
  }

  .url-input-search :deep(.ant-input-group .ant-input) {
    border-radius: 8px !important; /* 移动端恢复全圆角 */
  }

  .url-input-search :deep(.ant-input-group .ant-btn) {
    width: 100%;
    min-width: 100% !important; /* 移动端占满宽度 */
    border-radius: 8px !important; /* 移动端恢复全圆角 */
    margin-left: 0 !important; /* 【修改3】在移动端上下堆叠时，必须重置负边距 */
  }

  .img-wrapper {
    min-height: 150px;
    border-radius: 10px;
  }
}
</style>
