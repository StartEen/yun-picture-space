<template>
  <div id="addPicturePage">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? '编辑图片' : '创建图片' }}
    </h2>

    <!-- 选择上传方式 -->
    <a-tabs v-model:activeKey="uploadType">
      <a-tab-pane key="file" tab="文件上传">
        <!-- 图片上传组件 -->
        <PictureUpload :picture="picture" :onSuccess="onSuccess" />
      </a-tab-pane>
      <a-tab-pane key="url" tab="URL 上传" force-render>
        <!-- URL 图片上传组件 -->
        <UrlPictureUpload :picture="picture" :onSuccess="onSuccess" />
      </a-tab-pane>
    </a-tabs>
    <!--标签化信息-->
    <div v-if="picture" class="form-card">
      <a-form layout="vertical" :model="pictureForm" @finish="handleSubmit">
        <a-form-item label="名称" name="name">
          <a-input v-model:value="pictureForm.name" placeholder="请输入名称" />
        </a-form-item>
        <a-form-item label="简介" name="introduction">
          <a-textarea
            v-model:value="pictureForm.introduction"
            placeholder="请输入简介"
            :rows="2"
            autoSize
            allowClear
          />
        </a-form-item>
        <a-form-item label="分类" name="category">
          <a-auto-complete
            v-model:value="pictureForm.category"
            :options="categoryOptions"
            placeholder="请输入分类"
            allowClear
          />
        </a-form-item>
        <a-form-item label="标签" name="tags">
          <a-select
            v-model:value="pictureForm.tags"
            :options="tagOptions"
            mode="tags"
            placeholder="请输入标签"
            allowClear
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">保存</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import PictureUpload from '@/components/picture/PictureUpload.vue'
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  editPictureUsingPost,
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import UrlPictureUpload from '@/components/picture/UrlPictureUpload.vue'

const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureEditRequest>({})

const onSuccess = (newPicture: API.PictureVO) => {
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
  const pictureId = picture.value.id
  if (!pictureId) {
    return
  }

  const res = await editPictureUsingPost({
    id: pictureId,
    ...values,
  })

  if (res.data.code === 0 && res.data.data) {
    message.success('创建成功')
    // 跳转到图片详情页
    router.push({
      path: `/picture/${pictureId}`,
    })
  } else {
    message.error('创建失败，' + res.data.message)
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
</script>

<style scoped>
#addPicturePage {
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 0;
  min-height: 80vh;
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

/* 表单卡片样式 */
.form-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 24px;
  transition: box-shadow 0.3s ease;
  animation: fadeIn 0.5s ease-out 0.2s both;
}

.form-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

/* 表单样式 */
.ant-form {
  width: 100%;
}

.ant-form-item {
  margin-bottom: 20px;
}

.ant-form-item-label > label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
  display: block;
}

/* 输入框样式 */
.ant-input,
.ant-input-textarea,
.ant-select,
.ant-auto-complete {
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  transition: all 0.3s ease;
}

.ant-input:hover,
.ant-input-textarea:hover,
.ant-select:hover,
.ant-auto-complete:hover {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.ant-input:focus,
.ant-input-textarea:focus,
.ant-select:focus,
.ant-auto-complete:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
  outline: none;
}

/* 文本域样式 */
.ant-input-textarea {
  min-height: 100px;
}

/* 标签选择样式 */
.ant-select-multiple .ant-select-selection-item {
  border-radius: 12px;
  font-size: 12px;
  padding: 4px 12px;
  margin-right: 8px;
  margin-bottom: 8px;
}

/* 保存按钮样式 */
.ant-btn-primary {
  width: 100%;
  border-radius: 8px;
  padding: 12px;
  font-weight: 600;
  font-size: 16px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: linear-gradient(45deg, #4096ff, #1890ff);
  border: none;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.ant-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
  background: linear-gradient(45deg, #1890ff, #4096ff);
}

.ant-btn-primary:active {
  transform: translateY(0);
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
    padding: 16px;
  }

  .ant-form-item {
    margin-bottom: 16px;
  }

  .ant-tabs-tab {
    font-size: 14px;
    padding: 10px 16px;
  }

  .ant-btn-primary {
    padding: 10px;
    font-size: 14px;
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
