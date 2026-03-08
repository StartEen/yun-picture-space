<template>
  <div id="addPictureBatchPage">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>🖼️ 批量创建图片</h2>
      <p class="subtitle">快速抓取并创建多张图片到您的空间</p>
    </div>

    <!-- 图片信息表单 -->
    <div class="form-section">
      <div class="section-header">
        <span class="section-icon">📝</span>
        <span class="section-title">抓取配置</span>
      </div>
      <a-form name="formData" layout="vertical" :model="formData" @finish="handleSubmit">
        <a-form-item name="searchText" label="关键词">
          <a-input
            v-model:value="formData.searchText"
            placeholder="请输入关键词"
            allow-clear
            size="large"
          />
        </a-form-item>
        <a-form-item name="count" label="抓取数量">
          <a-input-number
            v-model:value="formData.count"
            placeholder="请输入数量"
            :min="1"
            :max="30"
            allow-clear
            size="large"
          />
        </a-form-item>
        <a-form-item name="namePrefix" label="名称前缀">
          <a-input
            v-model:value="formData.namePrefix"
            placeholder="请输入名称前缀，会自动补充序号"
            allow-clear
            size="large"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading" size="large">
            🚀 开始抓取
          </a-button>
        </a-form-item>
      </a-form>
    </div>

    <!-- 使用说明 -->
    <div class="info-section">
      <div class="section-header">
        <span class="section-icon">💡</span>
        <span class="section-title">使用说明</span>
      </div>
      <div class="notice-box">
        <span class="notice-icon">ℹ️</span>
        <span>系统将根据关键词自动抓取图片，请确保关键词准确以获得更好的结果</span>
      </div>
      <div class="tips-list">
        <div class="tip-item">
          <span class="tip-icon">🔍</span>
          <span class="tip-text">输入准确的关键词可以获取更相关的图片</span>
        </div>
        <div class="tip-item">
          <span class="tip-icon">🔢</span>
          <span class="tip-text">每次最多可抓取 30 张图片</span>
        </div>
        <div class="tip-item">
          <span class="tip-icon">🏷️</span>
          <span class="tip-text">设置名称前缀可以更好地管理批量创建的图片</span>
        </div>
      </div>
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
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 24px;
  min-height: 80vh;
  border-radius: 16px;
  margin-top: 24px;
  margin-bottom: 24px;
}

/* 页面头部 */
.page-header {
  text-align: center;
  margin-bottom: 32px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.page-header h2 {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
  background: linear-gradient(45deg, #3498db, #5dade2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
}

/* 区块头部 */
.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e8e8e8;
}

.section-icon {
  font-size: 24px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

/* 表单区块 */
.form-section {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 16px;
  padding: 28px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

/* 信息区块 */
.info-section {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

/* 提示框 */
.notice-box {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fff3cd 0%, #ffeaa7 100%);
  border-radius: 12px;
  border-left: 4px solid #f39c12;
  margin-bottom: 24px;
  font-size: 14px;
  color: #856404;
}

.notice-icon {
  font-size: 20px;
}

/* 提示列表 */
.tips-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.tip-item:hover {
  background: #e9ecef;
  transform: translateX(4px);
}

.tip-icon {
  font-size: 20px;
}

.tip-text {
  font-size: 14px;
  color: #333;
}

/* 表单样式 */
.ant-form {
  width: 100%;
}

.ant-form-item {
  margin-bottom: 24px;
}

.ant-form-item-label > label {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  display: block;
}

/* 输入框样式 */
.ant-input,
.ant-input-number {
  border-radius: 10px;
  border: 2px solid #e8e8e8;
  transition: all 0.3s ease;
  font-size: 15px;
}

.ant-input:hover,
.ant-input-number:hover {
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.ant-input:focus,
.ant-input-number:focus {
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.2);
  outline: none;
}

/* 输入数字样式 */
.ant-input-number {
  width: 100%;
}

/* 执行任务按钮样式 */
.ant-btn-primary {
  width: 100%;
  border-radius: 12px;
  padding: 14px;
  font-weight: 700;
  font-size: 17px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: linear-gradient(45deg, #3498db, #5dade2);
  border: none;
  box-shadow: 0 4px 15px rgba(52, 152, 219, 0.4);
  height: auto;
}

.ant-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(52, 152, 219, 0.5);
  background: linear-gradient(45deg, #5dade2, #3498db);
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
    padding: 20px 16px;
    margin-top: 16px;
    margin-bottom: 16px;
  }

  .page-header h2 {
    font-size: 24px;
  }

  .subtitle {
    font-size: 14px;
  }

  .form-section,
  .info-section {
    padding: 20px;
  }

  .section-title {
    font-size: 18px;
  }

  .ant-form-item {
    margin-bottom: 16px;
  }

  .ant-input-number {
    min-width: 100%;
  }

  .ant-btn-primary {
    padding: 12px;
    font-size: 15px;
  }

  .tip-item {
    padding: 10px 12px;
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
