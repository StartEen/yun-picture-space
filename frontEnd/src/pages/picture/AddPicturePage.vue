<template>
  <div id="addPicturePage">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? '编辑图片' : '创建图片' }}
    </h2>

    <a-typography-paragraph v-if="spaceId" type="secondary">
      保存至空间：<a :href="`/space/${spaceId}`" target="_blank">{{ spaceId }}</a>
    </a-typography-paragraph>

    <a-tabs v-model:activeKey="uploadType">
      <a-tab-pane key="file" tab="文件上传">
        <PictureUpload :picture="picture" :spaceId="spaceId" :onSuccess="onSuccess" />
      </a-tab-pane>
      <a-tab-pane key="url" tab="URL 上传" force-render>
        <UrlPictureUpload :picture="picture" :spaceId="spaceId" :onSuccess="onSuccess" />
      </a-tab-pane>
    </a-tabs>

    <div v-if="picture" class="edit-action-bar">
      <a-button class="edit-btn" :icon="h(EditOutlined)" @click="doEditPicture">
        裁剪与编辑图片
      </a-button>
      <a-button
        class="edit-btn"
        type="primary"
        :icon="h(FullscreenOutlined)"
        @click="doImagePainting"
      >
        AI 一键P图
      </a-button>
      <ImageCropper
        ref="imageCropperRef"
        :imageUrl="picture?.url"
        :picture="picture"
        :spaceId="spaceId"
        :onSuccess="onCropSuccess"
      />
      <ImageAiEdit
        ref="imageOutPaintingRef"
        :picture="picture"
        :spaceId="spaceId"
        :onSuccess="onImageOutPaintingSuccess"
      />
    </div>

    <div v-if="picture" class="form-card">
      <div class="form-title">图片信息</div>
      <a-form layout="vertical" :model="pictureForm" @finish="handleSubmit">
        <a-form-item label="名称" name="name">
          <a-input v-model:value="pictureForm.name" placeholder="请输入生动形象的名称" allowClear />
        </a-form-item>

        <a-form-item label="简介" name="introduction">
          <a-textarea
            v-model:value="pictureForm.introduction"
            placeholder="讲述一下这张图片背后的故事..."
            :auto-size="{ minRows: 1, maxRows: 5 }"
            allowClear
          />
        </a-form-item>

        <a-row :gutter="16">
          <a-col :xs="24" :sm="12">
            <a-form-item label="分类" name="category">
              <a-auto-complete
                v-model:value="pictureForm.category"
                :options="categoryOptions"
                placeholder="选择或输入分类"
                allowClear
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12">
            <a-form-item label="标签" name="tags">
              <a-select
                v-model:value="pictureForm.tags"
                :options="tagOptions"
                mode="tags"
                placeholder="添加相关标签"
                allowClear
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item class="submit-action">
          <a-button type="primary" html-type="submit" class="submit-btn" size="large">
            保存图片信息
          </a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import PictureUpload from '@/components/picture/PictureUpload.vue'
import { computed, onMounted, reactive, ref, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  editPictureUsingPost,
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import UrlPictureUpload from '@/components/picture/UrlPictureUpload.vue'
import ImageCropper from '@/components/picture/ImageCropper.vue'
import { EditOutlined, FullscreenOutlined } from '@ant-design/icons-vue'
import ImageAiEdit from '@/components/picture/ImageAIEdit.vue'

const picture = ref<API.PictureVo>()
const pictureForm = reactive<API.PictureEditRequest>({})

const onSuccess = (newPicture: API.PictureVo) => {
  picture.value = newPicture
  pictureForm.name = newPicture.name
}

const uploadType = ref<'file' | 'url'>('file')

const router = useRouter()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  const pictureId = picture.value?.id
  if (!pictureId) {
    return
  }

  const res = await editPictureUsingPost({
    id: pictureId,
    ...values,
  })

  if (res.data.code === 0 && res.data.data) {
    message.success('保存成功')
    // 跳转到图片详情页
    router.push({
      path: `/picture/${pictureId}`,
    })
  } else {
    message.error('保存失败，' + res.data.message)
  }
}

const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])

// 获取标签和分类选项
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    // 转换成下拉选项组件接受的格式
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
  } else {
    message.error('加载选项失败，' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})

const route = useRoute()

// 获取老数据
const getOldPicture = async () => {
  // 获取数据
  const id = route.query?.id
  if (id) {
    const res = await getPictureVoByIdUsingGet({
      id: id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      picture.value = data
      pictureForm.name = data.name
      pictureForm.introduction = data.introduction
      pictureForm.category = data.category
      pictureForm.tags = data.tags
    }
  }
}

onMounted(() => {
  getOldPicture()
})

// 空间 id
const spaceId = computed(() => {
  return route.query?.spaceId
})

// ----- 图片编辑器引用 ------
const imageCropperRef = ref()

// 编辑图片
const doEditPicture = async () => {
  imageCropperRef.value?.openModal()
}

// 编辑成功事件
const onCropSuccess = (newPicture: API.PictureVo) => {
  picture.value = newPicture
}

//------AI 扩图功能--------
const imageOutPaintingRef = ref()

//AI 扩图
const doImagePainting = () => {
  if (imageCropperRef.value) {
    imageOutPaintingRef.value.openModal()
  }
}
// 编辑成功事件
const onImageOutPaintingSuccess = (newPicture: API.PictureVo) => {
  picture.value = newPicture
}
</script>

<style scoped>
#addPicturePage {
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 0;
  min-height: 80vh;
}

#addPicturePage .edit-bar {
  text-align: center;
  margin: 16px 0;
}

/* 页面标题 */
h2 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
  line-height: 1.3;
  animation: fadeIn 0.5s ease-out;
}

/* 标签页样式 */
.ant-tabs {
  margin-bottom: 24px;
  animation: fadeIn 0.5s ease-out 0.1s both;
}

.ant-tabs-nav {
  margin-bottom: 24px;
}

.ant-tabs-tab {
  font-size: 16px;
  font-weight: 500;
  padding: 12px 24px;
}

.ant-tabs-tab.ant-tabs-tab-active .ant-tabs-tab-btn {
  color: #1890ff;
  font-weight: 600;
}

.ant-tabs-ink-bar {
  background: #1890ff;
  height: 3px;
  border-radius: 1.5px;
}

/* 裁剪编辑操作区 */
.edit-action-bar {
  text-align: center;
  margin: 15px 0 24px 0;
  animation: fadeUp 0.5s ease-out forwards;
}

.edit-btn {
  border-radius: 20px;
  height: 40px;
  padding: 0 24px;
  font-weight: 500;
  color: #595959;
  border-color: #d9d9d9;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  transition: all 0.3s;
}
.edit-btn:hover {
  color: #1890ff;
  border-color: #1890ff;
  background: #f0f7ff;
}

/* ======================================= */
/* ====== 下方为重点优化的【图片信息】表单区 ====== */
/* ======================================= */

/* 表单卡片外层 */
.form-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  padding: 32px;
  margin-top: 24px;
  border: 1px solid #f2f2f2;
  transition: all 0.3s ease;
  animation: fadeIn 0.5s ease-out 0.2s both;
}

.form-card:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

/* 表单标题 */
.form-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f1f1f;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
}

/* 标题左侧蓝色点缀线 */
.form-title::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 18px;
  background: #1890ff;
  border-radius: 2px;
  margin-right: 10px;
}

/* 表单项间距 */
.ant-form-item {
  margin-bottom: 24px;
}

/* 表单项 Label 样式 */
:deep(.ant-form-item-label > label) {
  font-size: 14px;
  font-weight: 500;
  color: #454545;
}

/* =========================================================
   核心修复区：统一高度、清除框中框、彻底解决文字偏上问题
   ========================================================= */

/* 1. 统一所有外层基础样式 (包裹层、输入框、选择框) */
:deep(.ant-input),
:deep(.ant-input-affix-wrapper),
:deep(.ant-select .ant-select-selector) {
  border-radius: 8px !important;
  background-color: #fafafa !important;
  border: 1px solid #e8e8e8 !important;
  transition: all 0.3s ease !important;
  box-shadow: none !important;
}

/* 2. 修复带有 allowClear 时的“框中框”问题 */
:deep(.ant-input-affix-wrapper > input.ant-input),
:deep(.ant-input-affix-wrapper > textarea.ant-input) {
  background-color: transparent !important;
  border: none !important;
  padding: 0 !important;
  box-shadow: none !important;
}

/* 3. 单行普通输入框高度与内边距 */
:deep(.ant-input:not(textarea)),
:deep(.ant-input-affix-wrapper:not(.ant-input-affix-wrapper-textarea)) {
  min-height: 42px !important;
  padding: 6px 14px !important;
  display: flex;
  align-items: center;
}

/* 4. 重点修复：分类（AutoComplete）/ 单选下拉框 文字偏上问题 */
:deep(.ant-select-single .ant-select-selector) {
  min-height: 42px !important;
  padding: 0 14px !important; /* 去除上下 padding，全靠内部 Flex 拉伸居中 */
  display: flex;
  align-items: center;
}
:deep(.ant-select-single .ant-select-selection-search) {
  position: absolute;
  inset: 0 14px !important; /* 让内部搜索区占满整行高度 */
  display: flex;
  align-items: center;
}
:deep(.ant-select-single .ant-select-selection-search-input) {
  height: 100% !important; /* 输入框强制撑满高度，实现垂直居中 */
  margin: 0 !important;
  padding: 0 !important;
  display: flex;
  align-items: center;
}
:deep(.ant-select-single .ant-select-selection-item),
:deep(.ant-select-single .ant-select-selection-placeholder) {
  display: flex;
  align-items: center;
  line-height: normal !important;
  margin: 0 !important;
  padding: 0 !important;
}

/* 5. 统一多行文本域的内边距，并隐藏原生拖拽 */
:deep(.ant-input-textarea),
:deep(.ant-input-affix-wrapper-textarea) {
  padding: 10px 14px !important;
}
:deep(.ant-input-textarea textarea) {
  resize: none;
}

/* 6. 统一多选标签框 (Tags) 的高度和内部元素对齐 */
:deep(.ant-select-multiple .ant-select-selector) {
  min-height: 42px !important;
  padding: 4px 8px !important;
  display: flex;
  align-items: center;
}
:deep(.ant-select-multiple .ant-select-selection-item) {
  background: #f0f7ff;
  border: 1px solid #cce4ff;
  color: #1890ff;
  border-radius: 6px;
  margin-top: 2px;
  margin-bottom: 2px;
}

/* 7. 统一所有的悬浮 (Hover) 和聚焦 (Focus) 状态 */
:deep(.ant-input:focus),
:deep(.ant-input:hover),
:deep(.ant-input-affix-wrapper:focus-within),
:deep(.ant-input-affix-wrapper:hover),
:deep(.ant-select:not(.ant-select-disabled):hover .ant-select-selector),
:deep(.ant-select-focused .ant-select-selector) {
  background-color: #ffffff !important;
  border-color: #1890ff !important;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1) !important;
}

/* ========================================================= */

/* 提交按钮区域 */
.submit-action {
  margin-top: 36px;
  margin-bottom: 0;
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  background: linear-gradient(135deg, #1890ff 0%, #4096ff 100%);
  border: none;
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.2);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(24, 144, 255, 0.3);
}

.submit-btn:active {
  transform: translateY(1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

/* 动画定义 */
@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

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

/* 响应式设计 */
@media (max-width: 767px) {
  #addPicturePage {
    padding: 16px;
  }
  h2 {
    font-size: 20px;
    margin-bottom: 20px;
  }
  .form-card {
    padding: 20px 16px;
  }
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
