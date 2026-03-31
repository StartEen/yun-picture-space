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
import { onUnmounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  generatePictureUseWordTaskUsingPost,
  getAliYunAiTaskUsingGet,
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
  generatingLoading.value = true
  if (!prompt.value || prompt.value.trim() === '') {
    message.error('请输入文字描述')
    generatingLoading.value = false
    return
  }
  // 设置 taskId 为一个临时值，让按钮显示加载状态
  taskId.value = 'creating'

  try {
    // 调用文生图接口
    const res = await generatePictureUseWordTaskUsingPost({
      text: prompt.value,
    })

    if (res.data.code === 0 && res.data.data) {
      // 处理响应数据
      const output = res.data.data.output
      if (output?.choices && output.choices.length > 0) {
        const choice = output.choices[0]
        if (choice?.message?.content && choice.message.content.length > 0) {
          const content = choice.message.content[0]
          if (content?.image) {
            //清理图片URL中的空格和反引导
            let imageUrl = content.image.trim()
            if (imageUrl.startsWith('`') && imageUrl.endsWith('`')) {
              imageUrl = imageUrl.substring(1, imageUrl.length - 1).trim()
            }
            resultImageUrl.value = imageUrl
            message.success('图片生成正在处理')
            //不需要轮询，直接显示结果
            clearPollingTimer()
            generatingLoading.value = false
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
        return
      } else {
        message.error('任务创建成功，但未获取到任务ID')
        taskId.value = undefined
        generatingLoading.value = false
      }
      // 恢复加载状态
      generatingLoading.value = false
    } else {
      message.error('图片生成失败：' + (res.data.message || '未知错误'))
      taskId.value = undefined
      generatingLoading.value = false
    }
  } catch (error: any) {
    console.error('图片生成失败', error)
    message.error('图片生成失败：' + (error?.message || '未知错误'))
    generatingLoading.value = false
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
        } else if (
          taskResult?.taskStatus === 'PROCESSING' ||
          taskResult?.taskStatus === 'PENDING'
        ) {
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

const uploadLoading = ref<boolean>(false)

// 上传图片
const handleUpload = async () => {
  if (!resultImageUrl.value) {
    message.error('请先生成图片')
    return
  }
  uploadLoading.value = true
  try {
    //从url下载图片
    const response = await fetch(resultImageUrl.value)
    if (!response.ok) {
      message.error('下载图片失败')
      return
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

    // 开始传送图片
    const res = await uploadPictureUsingPost(params, {}, file)
    if (res.data.code === 0 && res.data.data){
      message.success('图片上传成功')
      //将上传成功的图片信息
      props.onSuccess?.(res.data.data)
    }else {
      message.error('图片上传失败：' + (res.data.message || '未知错误'))
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
