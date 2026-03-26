<template>
  <a-modal
    class="image-out-painting"
    v-model:visible="visible"
    title="AI 一键 P 图"
    :footer="false"
    width="800px"
    @cancel="closeModal"
  >
    <a-row :gutter="24">
      <a-col :span="12">
        <div class="section-title">原始图片</div>
        <div class="image-preview-card">
          <a-image
            v-if="picture?.url"
            :src="picture?.url"
            :alt="picture?.name"
            @click="handleImagePreview(picture?.url)"
          />
          <a-empty v-else description="暂无原图" />
        </div>
      </a-col>
      <a-col :span="12">
        <div class="section-title">P 图结果</div>
        <a-spin :spinning="!!taskId" tip="AI 正在施展魔法中，请稍候...">
          <div class="image-preview-card">
            <a-image
              v-if="resultImageUrl"
              :src="resultImageUrl"
              alt="结果图片"
              @click="handleImagePreview(resultImageUrl)"
            />
            <a-empty v-else description="请输入文字描述并点击生成图片" class="custom-empty"/>
          </div>
        </a-spin>
      </a-col>
    </a-row>

    <a-row :gutter="24" style="margin-top: 24px">
      <a-col :span="24">
        <div class="section-title">AI 编辑指令</div>
        <a-textarea
          class="custom-textarea"
          placeholder="例如：把背景换成赛博朋克风格的城市夜景，或者给人物戴上一顶草帽..."
          :auto-size="{ minRows: 3, maxRows: 5 }"
          v-model:value="prompt"
        />
      </a-col>
    </a-row>

    <div style="margin-top: 32px">
      <a-flex gap="16" justify="flex-end">
        <a-button @click="closeModal">取消</a-button>
        <a-button type="primary" :loading="!!taskId" @click="createTask">
          {{ taskId ? '生成中...' : '生成图片' }}
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

  </a-modal>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import {
  createEditPictureTaskUsingPost,
  getAliYunAiTaskUsingGet,
  uploadPictureByUrlUsingPost,
  uploadPictureUsingPost,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

interface Props {
  picture?: API.PictureVo
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVo) => void
}

const props = defineProps<Props>()

//是否可见
const visible = ref(false)

//关闭弹窗
const closeModal = () => {
  // 清理轮询定时器
  clearPollingTimer()
  visible.value = false
}

// 打开弹窗
const openModal = () => {
  visible.value = true
}

// 暴露函数给父组件
defineExpose({
  openModal,
})

// 定义变量，存储图片结果
const resultImageUrl = ref<string>()

const taskId = ref<string | undefined>()

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
  if (!props.picture?.id) {
    return
  }
  if (!prompt.value || prompt.value.trim() === '') {
    message.error('请输入文字描述')
    return
  }
  // 设置 taskId 为一个临时值，让按钮显示加载状态
  taskId.value = 'creating'



  const res = await createEditPictureTaskUsingPost({
    pictureId: props.picture.id,
    text: prompt.value,
  })
  if (res.data.code === 0 && res.data.data) {
    message.success('创建任务成功')

    // 处理响应数据
    const output = res.data.data.output
    if (output?.choices && output.choices.length > 0) {
      const choice = output.choices[0]
      if (choice?.message?.content && choice.message.content.length > 0) {
        const content = choice.message.content[0]
        if (content?.image) {
          // 清理图片URL中的空格和反引号
          let imageUrl = content.image.trim()
          if (imageUrl.startsWith('`') && imageUrl.endsWith('`')) {
            imageUrl = imageUrl.substring(1, imageUrl.length - 1).trim()
          }
          resultImageUrl.value = imageUrl
          message.success('图片生成成功')
          // 不需要轮询，直接显示结果
          clearPollingTimer()
          return
        }
      }
    }

    // 尝试获取任务ID进行轮询
    console.log((res.data.data.output as any)?.taskId)
    taskId.value = (res.data.data.output as any)?.taskId
    if (taskId.value) {
      //开启轮询
      startPolling()
    } else {
      message.error('任务创建成功，但未获取到任务ID')
      taskId.value = undefined
    }
  } else {
    message.error('创建任务失败,' + res.data.message)
    taskId.value = undefined
  }
}

//注册轮询定时器
let pollingTimer: number | null = null

//开始轮询
const startPolling = () => {
  if (!taskId.value) {
    return
  }

  pollingTimer = window.setInterval(async () => {
    try {
      const res = await getAliYunAiTaskUsingGet({
        taskId: taskId.value,
      })
      if (res.data.code === 0 && res.data.data) {
        const taskResult = res.data.data.output
        if (taskResult?.taskStatus === 'SUCCESS') {
          message.success('任务成功')
          resultImageUrl.value = taskResult.outputImageUrl
          //清理轮询
          clearPollingTimer()
        } else if (taskResult?.taskStatus === 'FAILED') {
          message.error('任务失败')
          //清理轮询
          clearPollingTimer()
        } else if (taskResult?.taskStatus === 'PROCESSING' || taskResult?.taskStatus === 'PENDING') {
          // 任务正在处理中，继续轮询
          console.log('任务正在处理中...')
        } else {
          // 其他状态，停止轮询并显示错误
          message.error('任务状态异常: ' + taskResult?.taskStatus)
          clearPollingTimer()
        }
      } else {
        // API返回成功但数据为空
        message.error('获取任务状态失败: 数据为空')
        clearPollingTimer()
      }
    } catch (error: any) {
      console.log('P图任务轮询失败', error)
      message.error('P图任务轮询失败,' + (error?.message || '未知错误'))
      //清理轮询
      clearPollingTimer()
    }
  }, 3000)
}
const clearPollingTimer = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
  // 无论是否有轮询，都将taskId设置为undefined
  taskId.value = undefined
}

//组件卸载时清理定时器
onUnmounted(() => {
  clearPollingTimer()
})

// 上传图片
const uploadLoading = ref<boolean>(false)
const handleUpload = async () => {
  if (!resultImageUrl.value) {
    message.error('没有可上传的图片')
    return
  }
  
  uploadLoading.value = true
  try {
    // 1. 从URL下载图片
    const response = await fetch(resultImageUrl.value)
    if (!response.ok) {
      throw new Error('图片下载失败')
    }
    
    // 2. 将响应转换为Blob
    const blob = await response.blob()
    
    // 3. 创建File对象
    const fileName = `ai_edit_${Date.now()}.png`
    const file = new File([blob], fileName, { type: blob.type })
    
    // 4. 准备上传参数
    const params: API.uploadPictureUsingPOSTParams = {
      spaceId: props.spaceId,
    }
    if (props.picture) {
      params.id = props.picture.id
    }
    
    // 5. 上传图片
    const res = await uploadPictureUsingPost(params, {}, file)
    if (res.data.code === 0 && res.data.data) {
      message.success('应用结果成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
      // 关闭弹窗
      closeModal()
    } else {
      message.error('应用结果上传失败,' + res.data.message)
    }
  } catch (error: any) {
    console.error('上传失败', error)
    message.error('应用结果上传失败: ' + (error?.message || '未知错误'))
  } finally {
    uploadLoading.value = false
  }
}
</script>

<style scoped>
/* 弹窗容器 */
.image-out-painting {
  border-radius: 12px;
  overflow: hidden;
  padding: 0 !important;
}

/* 弹窗内容区域 */
:deep(.ant-modal-body) {
  padding: 24px;
  max-height: 80vh;
  overflow-y: auto;
  box-sizing: border-box;
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

/* 图片预览卡片通用样式 */
.image-preview-card,
.result-preview-card {
  width: 100%;
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
}

.image-preview-card:hover,
.result-preview-card:hover {
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
  transform: translateY(-2px);
}

/* 图片预览卡片统一高度 */
.image-preview-card {
  height: 250px;
  padding: 12px;
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
  height: 250px;
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
  .image-out-painting {
    width: 95% !important;
    margin: 0 auto;
  }

  :deep(.ant-modal-body) {
    padding: 16px;
  }

  .image-preview-card {
    height: 180px;
  }

  :deep(.ant-spin) {
    height: 180px;
  }

  :deep(.ant-row) {
    flex-direction: column;
  }

  :deep(.ant-col) {
    width: 100% !important;
    margin-bottom: 16px;
  }

  :deep(.ant-col:last-child) {
    margin-bottom: 0;
  }
}
</style>
