<template>
  <div id="generate-picture-Prompt">
    <a-row :gutter="24">
      <a-col :span="24">
        <div class="section-title">最终生成结果</div>
        <a-spin :spinning="generatingLoading" tip="AI 正在施展魔法中，请稍候...">
          <div class="image-preview-card">
            <a-image
              v-if="resultImageUrl"
              :src="resultImageUrl"
              alt="结果图片"
              @click="handleImagePreview(resultImageUrl)"
            />
            <a-empty v-else description="请输入文字描述并点击生成图片" class="custom-empty" />
          </div>
        </a-spin>
      </a-col>
    </a-row>
    <a-row :gutter="24" style="margin-top: 24px">
      <a-col :span="24">
        <div class="section-title">AI 指令输入</div>
        <a-textarea
          class="custom-textarea"
          placeholder="例如：一只橘色的猫咪坐在窗台上，阳光透过窗户洒在它身上，温暖的光影效果，超写实风格..."
          :auto-size="{ minRows: 3, maxRows: 5 }"
          v-model:value="prompt"
        />
      </a-col>
    </a-row>
    <div style="margin-top: 16px">
      <a-flex gap="16" justify="flex-end">
        <a-button type="primary" :loading="generatingLoading" @click="createTask">
          {{ generatingLoading ? '生成中...' : '生成图片' }}
        </a-button>
        <a-button
          type="primary"
          v-if="resultImageUrl"
          :loading="uploadLoading"
          @click="handleUpload"
        >
          应用并保存
        </a-button>
      </a-flex>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  generatePictureUseWordTaskUsingPost,
  uploadPictureByUrlUsingPost,
  uploadPictureUsingPost,
} from '@/api/pictureController.ts'

interface Props {
  picture?: API.PictureVo
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVo) => void
}

const props = defineProps<Props>()

// 定义变量，存储图片结果
const resultImageUrl = ref<string>()

const generatingLoading = ref<boolean>(false)

//定义变量存储提示词
const prompt = ref<string>()

// 图片预览相关
const previewVisible = ref(false)
const previewImage = ref<string>()

// 处理图片预览
const handleImagePreview = (url: string | undefined) => {
  if (url) {
    previewImage.value = url
    previewVisible.value = true
  }
}

/**
 * 创建任务
 */
const createTask = async () => {
  generatingLoading.value = true
  if (!prompt.value || prompt.value.trim() === '') {
    message.error('请输入文字描述')
    generatingLoading.value = false
    return
  }

  try {
    // 调用文生图接口
    const res = await generatePictureUseWordTaskUsingPost({
      prompt: prompt.value,
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('图片生成成功')

      // 处理文生图接口返回的数据结构
      const imageData = res.data.data.data
      if (imageData && imageData.length > 0 && imageData[0].url) {
        resultImageUrl.value = imageData[0].url
      } else {
        message.error('图片生成失败：未返回图片数据')
      }

      // 恢复加载状态
      generatingLoading.value = false
    } else {
      message.error('图片生成失败：' + (res.data.message || '未知错误'))
      generatingLoading.value = false
    }
  } catch (error: any) {
    console.error('图片生成失败', error)
    message.error('图片生成失败：' + (error?.message || '未知错误'))
    generatingLoading.value = false
  }
}

const uploadLoading = ref<boolean>(false)

// 上传图片
const handleUpload = async () => {
  if (!resultImageUrl.value) {
    message.error('没有可上传的图片')
    return
  }

  uploadLoading.value = true
  try {
    // 1. 创建 Image 对象并设置 crossOrigin
    const img = new Image()
    img.crossOrigin = 'anonymous'

    // 2. 等待图片加载完成
    await new Promise((resolve, reject) => {
      img.onload = resolve
      img.onerror = reject
      img.src = resultImageUrl.value
    })

    // 3. 创建 Canvas 并绘制图片
    const canvas = document.createElement('canvas')
    canvas.width = img.naturalWidth
    canvas.height = img.naturalHeight
    const ctx = canvas.getContext('2d')
    ctx?.drawImage(img, 0, 0)

    // 4. 将 Canvas 转换为 Blob
    const blob = await new Promise<Blob>((resolve) => {
      canvas.toBlob((blob) => {
        resolve(blob!)
      }, 'image/png')
    })

    // 5. 创建 File 对象
    const fileName = `ai_generate_${Date.now()}.png`
    const file = new File([blob], fileName, { type: 'image/png' })

    // 6. 准备上传参数
    const params = {
      spaceId: props.spaceId,
    }
    if (props.picture?.id) {
      params.id = props.picture.id
    }

    // 5. 上传图片
    const res = await uploadPictureUsingPost(params, {}, file)

    // 处理响应
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
      // 清空当前状态，准备下一次生成
      resultImageUrl.value = undefined
      prompt.value = undefined
    } else {
      message.error('图片上传失败，' + (res.data.message || '未知错误'))
    }
  } catch (error: any) {
    console.error('图片上传失败', error)
    message.error('图片上传失败：' + (error?.message || '未知错误'))
  } finally {
    uploadLoading.value = false
  }
}
</script>

<style scoped>
#generate-picture-Prompt {
  padding: 24px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.05),
    0 4px 16px rgba(0, 0, 0, 0.01);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeInUp 0.6s ease-out;
}

#generate-picture-Prompt:hover {
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

/* 标题样式 */
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2225;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  transition: all 0.3s;
}

.section-title::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 16px;
  background-color: #1890ff; /* Ant Design 默认主题蓝 */
  border-radius: 2px;
  margin-right: 8px;
  transition: all 0.3s;
}

.section-title:hover::before {
  width: 6px;
  background-color: #40a9ff;
}

/* 图片预览卡片 */
.image-preview-card {
  width: 100%;
  height: 300px;
  background-color: #f8fafc;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.3s ease;
  position: relative;
  box-sizing: border-box;
  padding: 12px;
}

.image-preview-card:hover {
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
  transform: translateY(-2px);
}

/* 图片自适应缩放，不裁切，保持比例 */
.image-preview-card :deep(.ant-image-img) {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.image-preview-card :deep(.ant-image-img:hover) {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: scale(1.02);
}

/* 文本域调整 */
.custom-textarea {
  border-radius: 8px;
  transition: all 0.3s ease;
  width: 100%;
  box-sizing: border-box;
}

.custom-textarea:focus {
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2) !important;
}

/* 自定义空状态样式，使文本居中 */
.custom-empty :deep(.ant-empty-description) {
  text-align: center;
  color: #8c8c8c;
  font-size: 14px;
}

/* 按钮样式优化 */
:deep(.ant-btn) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

:deep(.ant-btn:hover) {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

:deep(.ant-btn-primary) {
  background-color: #1890ff;
  border-color: #1890ff;
}

:deep(.ant-btn-primary:hover) {
  background-color: #40a9ff;
  border-color: #40a9ff;
}

/* 加载动画样式 */
:deep(.ant-spin) {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.ant-spin-tip) {
  color: #1890ff;
  font-size: 14px;
  margin-top: 16px;
}

/* 行和列布局调整 */
:deep(.ant-row) {
  margin-bottom: 24px;
}

:deep(.ant-row:last-child) {
  margin-bottom: 0;
}

:deep(.ant-col) {
  box-sizing: border-box;
}

/* 响应式布局 */
@media (max-width: 768px) {
  #generate-picture-Prompt {
    padding: 16px;
  }

  .image-preview-card {
    height: 200px;
  }

  :deep(.ant-spin) {
    height: 200px;
  }
}
</style>
