```vue
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
          <img v-if="picture?.url" :src="picture?.url" :alt="picture?.name" />
          <a-empty v-else description="暂无原图" />
        </div>
      </a-col>
      <a-col :span="12">
        <div class="section-title">AI 编辑指令</div>
        <a-textarea
          class="custom-textarea"
          placeholder="例如：把背景换成赛博朋克风格的城市夜景，或者给人物戴上一顶草帽..."
          :auto-size="{ minRows: 3, maxRows: 5 }"
          v-model:value="prompt"
        />
      </a-col>
    </a-row>

    <a-row :gutter="24" style="margin-top: 24px">
      <a-col :span="24">
        <div class="section-title">P 图结果</div>
        <a-spin :spinning="!!taskId" tip="AI 正在施展魔法中，请稍候...">
          <div class="result-preview-card">
            <img v-if="resultImageUrl" :src="resultImageUrl" alt="结果图片" />
            <a-empty v-else description="请输入文字描述并点击生成图片" class="custom-empty"/>
          </div>
        </a-spin>
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

const taskId = ref<string>()

//定义变量存储提示词
const prompt = ref<string>()

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
    console.log(res.data.data.output?.taskId)
    taskId.value = res.data.data.output?.taskId
    if (taskId.value) {
      //开启轮询
      startPolling()
    } else {
      message.error('任务创建成功，但未获取到任务ID')
    }
  } else {
    message.error('创建任务失败,' + res.data.message)
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
        }
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
    taskId.value = null
  }
}

//组件卸载时清理定时器
onUnmounted(() => {
  clearPollingTimer()
})

const uploadLoading = ref<boolean>(false)
const handleUpload = async () => {
  uploadLoading.value = true
  try {
    const param: API.PictureUploadRequest = {
      fileUrl: resultImageUrl.value,
      spaceId: props.spaceId,
    }
    if (props.picture) {
      param.id = props.picture.id
    }
    const res = await uploadPictureByUrlUsingPost(param)
    if (res.data.code === 0 && res.data.data) {
      message.success('应用结果成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
      // 关闭弹窗
      closeModal()
    } else {
      message.error('应用结果上传失败,' + res.data.message)
    }
  } catch (error) {
    message.error('应用结果上传失败')
  } finally {
    uploadLoading.value = false
  }
}
</script>

<style scoped>
/* 标题样式 */
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2225;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.section-title::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 16px;
  background-color: #1890ff; /* Ant Design 默认主题蓝，如果你的项目改了主题色可相应调整 */
  border-radius: 2px;
  margin-right: 8px;
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
  transition: all 0.3s;
}

.image-preview-card:hover,
.result-preview-card:hover {
  border-color: #1890ff;
}

/* 原始图片固定高度 */
.image-preview-card {
  height: 200px;
  padding: 8px;
}

/* 结果图片稍微大一点，以匹配截图中的视觉比例 */
.result-preview-card {
  min-height: 420px;
  padding: 12px;
}

/* 图片自适应缩放，不裁切，保持比例 */
.image-preview-card img,
.result-preview-card img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 文本域调整 */
.custom-textarea {
  border-radius: 8px;
}

/* 自定义空状态样式，使文本居中 */
.custom-empty :deep(.ant-empty-description) {
  text-align: center;
}
</style>
```
