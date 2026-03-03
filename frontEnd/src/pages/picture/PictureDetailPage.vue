<template>
  <div id="pictureDetailPage">
    <!-- 加载状态 -->
    <a-skeleton :loading="loading" :active="true" class="loading-skeleton">
      <div class="detail-container">
        <!-- 图片预览 -->
        <div class="preview-section">
          <div class="preview-card">
            <div class="image-container">
              <a-image 
                :src="picture.url" 
                class="preview-image" 
                :preview="{
                  visible: fullImageVisible,
                  onVisibleChange: (visible) => fullImageVisible = visible
                }"
              />
            </div>
          </div>
        </div>

        <!-- 图片信息区域 -->
        <div class="info-section">
          <div class="info-card">
            <div class="info-header">
              <h2 class="picture-title">{{ picture.name ?? '未命名' }}</h2>
              <div class="author-info">
                <a-avatar :size="32" :src="picture.user?.userAvatar" />
                <span class="author-name">{{ picture.user?.userName || '未知用户' }}</span>
              </div>
            </div>

            <div class="info-content">
              <div v-if="picture.introduction" class="introduction">
                <p>{{ picture.introduction }}</p>
              </div>

              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">分类</span>
                  <span class="info-value">{{ picture.category ?? '默认' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">格式</span>
                  <span class="info-value">{{ picture.picFormat ?? '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">尺寸</span>
                  <span class="info-value">{{ picture.picWidth }} × {{ picture.picHeight }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">大小</span>
                  <span class="info-value">{{ formatSize(picture.picSize) }}</span>
                </div>
              </div>

              <div v-if="picture.tags && picture.tags.length > 0" class="tags-section">
                <span class="info-label">标签</span>
                <div class="tags-container">
                  <a-tag
                    v-for="(tag, index) in picture.tags"
                    :key="tag"
                    class="picture-tag"
                    :class="`tag-color-${index % 5}`"
                  >
                    {{ tag }}
                  </a-tag>
                </div>
              </div>
            </div>

            <!-- 图片操作 -->
            <div class="action-buttons">
              <a-button type="primary" size="large" class="download-button" @click="doDownload">
                <template #icon>
                  <DownloadOutlined />
                </template>
                免费下载
              </a-button>
              <div class="secondary-buttons">
                <a-button v-if="canEdit" size="large" class="edit-button" @click="doEdit">
                  <template #icon>
                    <EditOutlined />
                  </template>
                  编辑
                </a-button>
                <a-button
                  v-if="canDelete"
                  size="large"
                  danger
                  class="delete-button"
                  @click="doDelete"
                >
                  <template #icon>
                    <DeleteOutlined />
                  </template>
                  删除
                </a-button>
                <a-button size="large" class="share-button" @click="openShareModal">
                  <template #icon>
                    <ShareAltOutlined />
                  </template>
                  分享
                </a-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </a-skeleton>

    <!-- 分享模态框 -->
    <ShareModal ref="shareModalRef" />
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import { deletePictureUsingPost, getPictureVoByIdUsingGet } from '@/api/pictureController.ts'
import { message, Modal } from 'ant-design-vue'
import {
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
  ShareAltOutlined,
} from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { downloadImage, formatSize } from '@/utils'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'

interface Props {
  id: string | number
}

const props = defineProps<Props>()
const picture = ref<API.PictureVO>({})
const loading = ref(true)
const fullImageVisible = ref(false)
const shareModalRef = ref()

// 获取图片详情
const fetchPictureDetail = async () => {
  loading.value = true
  try {
    const res = await getPictureVoByIdUsingGet({
      id: props.id,
    })
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
    } else {
      message.error('获取图片详情失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取图片详情失败：' + e.message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchPictureDetail()
})

//编写权限逻辑：删除和编辑
const loginUserStore = useLoginUserStore()

// 判断是否有编辑权限
const canEdit = computed(() => {
  const loginUser = loginUserStore.loginUser
  if (!loginUser.id) {
    return false
  }
  //仅本人或管理员可编辑
  const user = picture.value.user || {}
  return loginUser.id === user.id || loginUser.userRole === 'admin'
})
//将编辑的权限值赋值给删除权限的值
const canDelete = canEdit

const router = useRouter()
// 编辑
const doEdit = () => {
  router.push({
    path: '/add_picture',
    query: {
      id: picture.value.id,
      spaceId: picture.value.spaceId,
    },
  })
}

// 删除数据
const doDelete = async () => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这张图片吗？此操作不可恢复。',
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      const id = picture.value.id
      if (!id) {
        return
      }
      try {
        const res = await deletePictureUsingPost({ id })
        if (res.data.code === 0) {
          message.success('删除成功')
          // 跳转到上一页
          router.back()
        } else {
          message.error('删除失败')
        }
      } catch (e: any) {
        message.error('删除失败：' + e.message)
      }
    },
  })
}

// 下载图片
const doDownload = () => {
  downloadImage(picture.value.url)
  message.success('开始下载')
}

// 显示全屏图片
const showFullImage = () => {
  fullImageVisible.value = true
}

// 打开分享模态框
const openShareModal = () => {
  if (shareModalRef.value) {
    shareModalRef.value.open()
  }
}
</script>

<style scoped>
#pictureDetailPage {
  padding: 24px 0;
  min-height: 80vh;
}

/* 加载骨架屏 */
.loading-skeleton {
  width: 80%;
}

.detail-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
}

@media (min-width: 768px) {
  .detail-container {
    grid-template-columns: 2fr 1fr;
  }
}

/* 预览区域样式 */
.preview-section {
  display: flex;
  align-items: flex-start;
}

.preview-card {
  width: 100%;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 24px;
  transition: box-shadow 0.3s ease;
}

.preview-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.image-container {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #fafafa;
  border-radius: 8px;
  min-height: 400px;
  position: relative;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s ease;
}

.image-container:hover {
  background: transparent;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: all 0.3s ease;
  border-radius: 8px;
  backdrop-filter: blur(2px);
}

.image-container:hover .image-overlay {
  opacity: 1;
  background: rgba(0, 0, 0, 0.4);
}

.zoom-icon {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  width: 60px;
  height: 60px;
  display: flex;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(4px);
  transition: all 0.3s ease;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.image-container:hover .zoom-icon {
  transform: scale(1.1);
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
}

.preview-image {
  max-width: 100%;
  max-height: 600px;
  object-fit: contain;
  border-radius: 4px;
  transition: transform 0.3s ease;
}

.image-container:hover .preview-image {
  transform: scale(1.02);
}

/* 信息区域样式 */
.info-section {
  display: flex;
  align-items: flex-start;
}

.info-card {
  width: 100%;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 24px;
  transition: box-shadow 0.3s ease;
}

.info-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.info-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.picture-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
  line-height: 1.3;
  word-break: break-word;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-name {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.info-content {
  margin-bottom: 24px;
}

.introduction {
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  border-left: 4px solid #1890ff;
}

.introduction p {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #666;
  word-break: break-word;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  word-break: break-word;
}

.tags-section {
  margin-top: 20px;
}

.tags-container {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.picture-tag {
  font-size: 12px;
  border-radius: 12px;
  padding: 6px 14px;
  border: none;
  transition: all 0.2s ease;
  cursor: pointer;
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.picture-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

/* 多彩标签样式 */
.tag-color-0 {
  background: #e6f7ff;
  color: #1890ff;
}

.tag-color-0:hover {
  background: #bae7ff;
}

.tag-color-1 {
  background: #f6ffed;
  color: #52c41a;
}

.tag-color-1:hover {
  background: #d9f7be;
}

.tag-color-2 {
  background: #fff7e6;
  color: #fa8c16;
}

.tag-color-2:hover {
  background: #ffd591;
}

.tag-color-3 {
  background: #fff0f6;
  color: #f5222d;
}

.tag-color-3:hover {
  background: #ffadd2;
}

.tag-color-4 {
  background: #f9f0ff;
  color: #722ed1;
}

.tag-color-4:hover {
  background: #d3adf7;
}

/* 操作按钮样式 */
.action-buttons {
  border-top: 1px solid #f0f0f0;
  padding-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.download-button {
  width: 100%;
  border-radius: 8px;
  padding: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.download-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.secondary-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.edit-button,
.delete-button,
.share-button {
  flex: 1;
  min-width: 80px;
  border-radius: 8px;
  padding: 10px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.edit-button:hover {
  background: #f0f0f0;
  transform: translateY(-2px);
}

.delete-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
}

.share-button:hover {
  background: #f0f0f0;
  transform: translateY(-2px);
}

/* 全屏图片预览 */
.full-image-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  max-height: 80vh;
}

.full-image {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
  border-radius: 4px;
}

/* 响应式设计 */
@media (max-width: 767px) {
  .detail-container {
    padding: 0 16px;
  }

  .preview-card,
  .info-card {
    padding: 16px;
  }

  .image-container {
    padding: 16px;
    min-height: 300px;
  }

  .picture-title {
    font-size: 20px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .secondary-buttons {
    flex-direction: column;
  }

  .edit-button,
  .delete-button,
  .share-button {
    width: 100%;
  }

  .full-image-container {
    padding: 10px;
  }
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.preview-card,
.info-card {
  animation: fadeIn 0.5s ease-out;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
