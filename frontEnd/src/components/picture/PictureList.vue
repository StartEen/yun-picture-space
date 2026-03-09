<template>
  <div class="picture-list">
    <div class="waterfall-container" ref="containerRef">
      <div
        v-for="(picture, index) in dataList"
        :key="picture.id || index"
        class="waterfall-item"
        :style="{ width: columnWidth + 'px' }"
        @mouseenter="hoveredIndex = index"
        @mouseleave="hoveredIndex = -1"
      >
        <div class="image-card">
          <div class="image-wrapper" @click="doClickPicture(picture)">
            <img
              :alt="picture.name"
              :src="picture.thumbnailUrl ?? picture.url"
              class="waterfall-image"
              loading="lazy"
              @load="onImageLoad(index)"
            />
            <div class="image-overlay" :class="{ active: hoveredIndex === index }">
              <div class="overlay-content">
                <h4 class="image-title">{{ picture.name || '未命名图片' }}</h4>
                <div class="image-tags">
                  <span v-if="picture.category" class="tag category">{{ picture.category }}</span>
                  <span v-for="tag in (picture.tags || []).slice(0, 3)" :key="tag" class="tag">
                    {{ tag }}
                  </span>
                </div>
                <div v-if="showOp" class="image-actions">
                  <a-space @click="(e) => doEdit(picture, e)">
                    <EditOutlined />
                    编辑
                  </a-space>
                  <a-space @click="(e) => doDelete(picture, e)">
                    <DeleteOutlined />
                    删除
                  </a-space>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { deletePictureUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

interface Props {
  dataList?: API.PictureVO[]
  loading?: boolean
  showOp?: boolean
  onReload?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOp: false,
})

const hoveredIndex = ref(-1)
const containerRef = ref<HTMLElement>()
const columnWidth = ref(280)

// 计算列宽
const calculateColumnWidth = () => {
  if (!containerRef.value) return
  const containerWidth = containerRef.value.offsetWidth
  const gap = 16
  const minColumnWidth = 260
  const maxColumnWidth = 320

  // 计算可以容纳多少列
  const columns = Math.floor((containerWidth + gap) / (minColumnWidth + gap))
  // 计算每列宽度
  const width = (containerWidth - (columns - 1) * gap) / columns

  columnWidth.value = Math.min(Math.max(width, minColumnWidth), maxColumnWidth)
}

// 图片加载完成
const onImageLoad = (index: number) => {
  // 可以在这里处理图片加载后的布局调整
}

// 监听窗口大小变化
const handleResize = () => {
  calculateColumnWidth()
}

onMounted(() => {
  nextTick(() => {
    calculateColumnWidth()
  })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

const router = useRouter()
// 跳转至图片详情页
const doClickPicture = (picture: API.PictureVO) => {
  router.push({
    path: `/picture/${picture.id}`,
  })
}

// 编辑
const doEdit = (picture, e) => {
  // 阻止冒泡
  e.stopPropagation()
  // 跳转时一定要携带 spaceId
  router.push({
    path: '/add_picture',
    query: {
      id: picture.id,
      spaceId: picture.spaceId,
    },
  })
}

// 删除数据
const doDelete = async (picture, e) => {
  // 阻止冒泡
  e.stopPropagation()
  const id = picture.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    props.onReload?.()
  } else {
    message.error('删除失败')
  }
}
</script>

<style scoped>
.picture-list {
  min-height: 210px;
  padding: 8px;
}

/* 瀑布流容器 - 使用 CSS columns */
.waterfall-container {
  column-count: 3;
  column-gap: 24px;
}

/* 响应式列数 */
@media (max-width: 1400px) {
  .waterfall-container {
    column-count: 2;
  }
}

@media (max-width: 768px) {
  .waterfall-container {
    column-count: 2;
    column-gap: 16px;
  }
}

@media (max-width: 576px) {
  .waterfall-container {
    column-count: 1;
  }
}

/* 瀑布流项 */
.waterfall-item {
  break-inside: avoid;
  margin-bottom: 24px;
  width: 100% !important;
}

/* 图片卡片 */
.image-card {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  background: #f5f5f5;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
}

.image-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.25);
}

/* 图片容器 */
.image-wrapper {
  position: relative;
  width: 100%;
}

.waterfall-image {
  display: block;
  width: 100%;
  height: auto;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.image-card:hover .waterfall-image {
  transform: scale(1.03);
}

/* 悬浮遮罩层 */
.image-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to top,
    rgba(0, 0, 0, 0.85) 0%,
    rgba(0, 0, 0, 0.5) 3 0%,
    transparent 60%
  );
  opacity: 0;
  transition: opacity 0.3s ease;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 20px;
}

.image-overlay.active {
  opacity: 1;
}

.overlay-content {
  transform: translateY(10px);
  transition: transform 0.3s ease;
}

.image-overlay.active .overlay-content {
  transform: translateY(0);
}

.image-title {
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 10px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.image-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  display: inline-block;
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  border-radius: 6px;
  font-size: 13px;
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.tag.category {
  background: rgba(24, 144, 255, 0.8);
  border-color: rgba(24, 144, 255, 0.9);
}

/* 操作按钮 */
.image-actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.image-actions :deep(.ant-space) {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.image-actions :deep(.ant-space):first-child {
  background: rgba(24, 144, 255, 0.9);
  color: #fff;
}

.image-actions :deep(.ant-space):last-child {
  background: rgba(255, 77, 77, 0.9);
  color: #fff;
}

.image-actions :deep(.ant-space):first-child:hover {
  background: rgba(24, 144, 255, 1);
  transform: translateY(-1px);
}

.image-actions :deep(.ant-space):last-child:hover {
  background: rgba(255, 77, 77, 1);
  transform: translateY(-1px);
}

/* 加载状态 */
:deep(.ant-spin) {
  display: flex;
  justify-content: center;
  padding: 40px;
}
</style>
