<template>
  <a-modal
    class="image-cropper-modal"
    v-model:visible="visible"
    title="裁剪与编辑图片"
    width="950px"
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
        <a-space size="middle" v-if="isTeamSpace" style="margin-right: 20px">
          <a-button v-if="editingUser" disabled>{{ editingUser.userName }} 正在编辑</a-button>
          <a-button v-if="canEnterEdit" type="primary" ghost @click="enterEdit">进入编辑</a-button>
          <a-button v-if="canExitEdit" danger ghost @click="exitEdit">退出编辑</a-button>
        </a-space>
        <a-space size="middle">
          <a-button @click="rotateLeft" :disabled="!canEdit">
            <template #icon><RotateLeftOutlined /></template>
            向左旋转
          </a-button>
          <a-button @click="rotateRight" :disabled="!canEdit">
            <template #icon><RotateRightOutlined /></template>
            向右旋转
          </a-button>
          <a-button @click="changeScale(1)" :disabled="!canEdit">
            <template #icon><ZoomInOutlined /></template>
            放大
          </a-button>
          <a-button @click="changeScale(-1)" :disabled="!canEdit">
            <template #icon><ZoomOutOutlined /></template>
            缩小
          </a-button>
          <a-button
            type="primary"
            :loading="loading"
            :disabled="!canEdit"
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
import { ref, watch, nextTick, computed, watchEffect, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  RotateLeftOutlined,
  RotateRightOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
  CheckOutlined,
} from '@ant-design/icons-vue'
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'
import { uploadPictureUsingPost } from '@/api/pictureController.ts'
import { SPACE_TYPE_ENUM } from '@/constants/space.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import PictureEditWebSocket from '@/utils/pictureEditWebSocket.ts'
import { PICTURE_EDIT_ACTION_ENUM, PICTURE_EDIT_MESSAGE_TYPE_ENUM } from '@/constants/picture.ts'

interface Props {
  imageUrl?: string
  picture?: API.PictureVo
  spaceId?: number
  space?: API.SpaceVo
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
  },
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
      '9:16': [9, 16],
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
  { immediate: true },
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
const changeScale = (num: number) => {
  cropperRef.value?.changeScale(num)
  if (num > 0) {
    editAction(PICTURE_EDIT_ACTION_ENUM.ZOOM_IN)
  } else {
    editAction(PICTURE_EDIT_ACTION_ENUM.ZOOM_OUT)
  }
}
const rotateLeft = () => {
  cropperRef.value?.rotateLeft()
  editAction(PICTURE_EDIT_ACTION_ENUM.ROTATE_LEFT)
}
const rotateRight = () => {
  cropperRef.value?.rotateRight()
  editAction(PICTURE_EDIT_ACTION_ENUM.ROTATE_RIGHT)
}

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

  //断开链接
  if (webSocket) {
    webSocket.disconnect()
  }
  editingUser.value = undefined
}

// 暴露函数给父组件
defineExpose({
  openModal,
})

/*---------实时编辑-----------*/

// 判断当前空间是否是团队空间
const isTeamSpace = computed(() => {
  return props.space?.spaceType === SPACE_TYPE_ENUM.TEAM
})

const loginUserStore = useLoginUserStore()
const loginUser = loginUserStore.loginUser

// 正在编辑的用户
const editingUser = ref<API.UserVo>()
// 当前用户是否可进入编辑
const canEnterEdit = computed(() => {
  return !editingUser.value
})
// 正在编辑的用户是本人，可退出编辑
const canExitEdit = computed(() => {
  return editingUser.value?.id === loginUser.id
})
// 可以点击编辑图片的操作按钮
const canEdit = computed(() => {
  // 不是团队空间，默认就可以编辑
  if (!isTeamSpace.value) {
    return true
  }
  // 团队空间，只有编辑者才能协同编辑
  return editingUser.value?.id === loginUser.id
})

let webSocket: PictureEditWebSocket | null

//初始化WebSocket链接，绑定事件
const initWebSocket = () => {
  const pictureId = props.picture?.id
  if (!pictureId || !visible.value) {
    return
  }
  //防止之前的链接未释放
  if (webSocket) {
    webSocket.disconnect()
  }

  //创建WebSocket实例
  webSocket = new PictureEditWebSocket(pictureId)

  //建立WebSocket连接
  webSocket.connect()

  //监听通知消息
  webSocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.INFO, (msg) => {
    console.log('收到通知消息：', msg)
    message.info(msg.message)
  })

  //监听进入编辑状态消息
  webSocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.ENTER_EDIT, (msg) => {
    console.log('收到进入编辑状态消息：', msg)
    message.info(msg.message)
    editingUser.value = msg.user
  })

  //监听编辑操作消息
  webSocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.EDIT_ACTION, (msg) => {
    console.log('收到编辑操作消息：', msg)
    message.info(msg.message)
    switch (msg.editAction) {
      case PICTURE_EDIT_ACTION_ENUM.ROTATE_LEFT:
        cropperRef.value.rotateLeft()
        break
      case PICTURE_EDIT_ACTION_ENUM.ROTATE_RIGHT:
        cropperRef.value.rotateRight()
        break
      case PICTURE_EDIT_ACTION_ENUM.ZOOM_IN:
        cropperRef.value.changeScale(1)
        break
      case PICTURE_EDIT_ACTION_ENUM.ZOOM_OUT:
        cropperRef.value.changeScale(-1)
        break
    }
  })
  //监听退出编辑状态消息
  webSocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.EXIT_EDIT, (msg) => {
    console.log('收到退出编辑状态消息：', msg)
    message.info(msg.message)
    editingUser.value = undefined
  })
}
watchEffect(() => {
  initWebSocket()
})

onUnmounted(() => {
  //断开链接
  if (webSocket) {
    webSocket.disconnect()
  }
  editingUser.value = undefined
})

// 进入编辑状态
const enterEdit = () => {
  if (webSocket) {
    // 发送进入编辑状态的请求
    webSocket.sendMessage({
      type: PICTURE_EDIT_MESSAGE_TYPE_ENUM.ENTER_EDIT,
    })
  }
}

// 退出编辑状态
const exitEdit = () => {
  if (webSocket) {
    // 发送退出编辑状态的请求
    webSocket.sendMessage({
      type: PICTURE_EDIT_MESSAGE_TYPE_ENUM.EXIT_EDIT,
    })
  }
}

// 编辑图片操作
const editAction = (action: string) => {
  if (webSocket) {
    // 发送编辑操作的请求
    webSocket.sendMessage({
      type: PICTURE_EDIT_MESSAGE_TYPE_ENUM.EDIT_ACTION,
      editAction: action,
    })
  }
}
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
  background-position:
    0 0,
    0 10px,
    10px -10px,
    -10px 0px;
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
