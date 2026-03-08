<template>
  <div id="addPictureBatchPage">
    <h2>批量创建图片</h2>
    <!-- 图片信息表单 -->
    <div class="form-card">
      <a-form name="formData" layout="vertical" :model="formData" @finish="handleSubmit">
        <a-form-item name="searchText" label="关键词">
          <a-input v-model:value="formData.searchText" placeholder="请输入关键词" allow-clear />
        </a-form-item>
        <a-form-item name="count" label="抓取数量">
          <a-input-number
            v-model:value="formData.count"
            placeholder="请输入数量"
            :min="1"
            :max="30"
            allow-clear
          />
        </a-form-item>
        <a-form-item name="namePrefix" label="名称前缀">
          <a-input
            v-model:value="formData.namePrefix"
            placeholder="请输入名称前缀，会自动补充序号"
            allow-clear
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading"> 执行任务 </a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { uploadPictureByBatchUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

const formData = reactive<API.PictureUploadByBatchRequest>({
  count: 10,
})
// 提交任务状态
const loading = ref(false)

const router = useRouter()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  loading.value = true
  const res = await uploadPictureByBatchUsingPost({
    ...formData,
  })
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success(`创建成功，共 ${res.data.data} 条`)
    // 跳转到主页
    router.push({
      path: `/`,
    })
  } else {
    message.error('创建失败，' + res.data.message)
  }
  loading.value = false
}
</script>

<style scoped>
#addPictureBatchPage {
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

/* 表单卡片样式 */
.form-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 24px;
  transition: box-shadow 0.3s ease;
  animation: fadeIn 0.5s ease-out 0.1s both;
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
.ant-input-number {
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  transition: all 0.3s ease;
}

.ant-input:hover,
.ant-input-number:hover {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.ant-input:focus,
.ant-input-number:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
  outline: none;
}

/* 输入数字样式 */
.ant-input-number {
  width: 100%;
  min-width: 100%;
}

/* 执行任务按钮样式 */
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
  #addPictureBatchPage {
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

  .ant-input-number {
    min-width: 100%;
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
