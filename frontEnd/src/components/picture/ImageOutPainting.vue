<template>
  <a-modal
    class="image-out-painting"
    v-model:visible="visible"
    title="AI 扩图"
    :footer="false"
    @cancel="closeModal"
  >
    <a-row gutter="16">
      <a-col span="12">
        <h4>原始图片</h4>
        <img :src="picture?.url" :alt="picture?.name" style="max-width: 100%" />
      </a-col>
      <a-col span="12">
        <h4>AI 扩图结果</h4>
        <img
          v-if="resultImageUrl"
          :src="resultImageUrl"
          :alt="picture?.name"
          style="max-width: 100%"
        />
      </a-col>
    </a-row>
    <div style="margin-bottom: 16px" />
    <a-flex gap="16" justify="center">
      <a-button type="primary" :loading="!!taskId" ghost @click="createTask">生成图片</a-button>
      <a-button type="primary" v-if="resultImageUrl" :loading="uploadLoading" @click="handleUpload"
        >应用结果</a-button
      >
    </a-flex>
  </a-modal>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  createPictureOutPaintingTaskUsingPost,
  getPictureOutPaintingTaskUsingGet,
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

/**
 * 创建任务
 */
const createTask = async () => {
  if (!props.picture?.id) {
    return
  }
  const res = await createPictureOutPaintingTaskUsingPost({
    pictureId: props.picture.id,
    parameters: {
      xScale: 2,
      yScale: 2,
    },
  })
  if (res.data.code === 0 && res.data.data) {
    message.success('创建任务成功,请耐心等待，不要退出界面')
    console.log(res.data.data.output?.taskId)
    taskId.value = res.data.data.output?.taskId
    //开启轮询
    startPolling()
  } else {
    message.error('创建任务失败,' + res.data.message)
  }
}

//注册轮询定时器
let pollingTimer: NodeJS.Timeout = null

//开始轮询
const startPolling = () => {
  if (!taskId.value) {
    return
  }

  pollingTimer = setInterval(async () => {
    try {
      const res = await getPictureOutPaintingTaskUsingGet({
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
    } catch (error) {
      console.log('扩图任务轮询失败', error)
      message.error('扩图任务轮询失败,' + error.message)
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
//销毁定时器防止泄漏
onMounted(() => {
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
      message.error('应用结果长传失败,' + res.data.message)
    }
  } catch (error) {
    message.error('应用结果长传失败')
  } finally {
    uploadLoading.value = false
  }
}
</script>

<style scoped></style>
