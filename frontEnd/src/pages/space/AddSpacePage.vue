<template>
  <div id="addSpacePage">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>{{ route.query?.id ? '✏️ 修改空间' : '🚀 创建空间' }}</h2>
      <p class="subtitle">{{ route.query?.id ? '编辑您的空间信息' : '开启您的云端存储之旅' }}</p>
    </div>

    <!-- 空间信息表单 -->
    <div class="form-section">
      <div class="section-header">
        <span class="section-icon">📝</span>
        <span class="section-title">基本信息</span>
      </div>
      <a-form name="spaceForm" layout="vertical" :model="spaceForm" @finish="handleSubmit">
        <a-form-item name="spaceName" label="空间名称">
          <a-input
            v-model:value="spaceForm.spaceName"
            placeholder="请输入空间名称"
            allow-clear
            size="large"
          />
        </a-form-item>
        <a-form-item name="spaceLevel" label="空间级别">
          <a-select
            v-model:value="spaceForm.spaceLevel"
            placeholder="请选择空间级别"
            :options="SPACE_LEVEL_OPTIONS"
            allow-clear
            size="large"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading" size="large">
            {{ route.query?.id ? '💾 保存修改' : '✨ 立即创建' }}
          </a-button>
        </a-form-item>
      </a-form>
    </div>

    <!-- 空间级别介绍 -->
    <div class="info-section">
      <div class="section-header">
        <span class="section-icon">💎</span>
        <span class="section-title">空间级别介绍</span>
      </div>
      <div class="notice-box">
        <span class="notice-icon">ℹ️</span>
        <span>目前仅支持开通普通版，如需升级空间，请联系管理员</span>
      </div>
      <div class="level-cards">
        <div
          v-for="spaceLevel in spaceLevelList"
          :key="spaceLevel.value"
          class="level-card"
          :class="{ active: spaceForm.spaceLevel === spaceLevel.value }"
          @click="spaceForm.spaceLevel = spaceLevel.value"
        >
          <div class="level-header">
            <span class="level-badge">{{ spaceLevel.text }}</span>
            <span v-if="spaceForm.spaceLevel === spaceLevel.value" class="selected-mark">✓</span>
          </div>
          <div class="level-body">
            <div class="level-stat">
              <span class="stat-icon">💾</span>
              <span class="stat-value">{{ formatSize(spaceLevel.maxSize) }}</span>
            </div>
            <div class="level-stat">
              <span class="stat-icon">📁</span>
              <span class="stat-value">{{ spaceLevel.maxCount }} 个文件</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  addSpaceUsingPost,
  getSpaceVoByIdUsingGet,
  listSpaceLevelUsingGet,
  updateSpaceUsingPost,
} from '@/api/spaceController.ts'
import { useRoute, useRouter } from 'vue-router'
import { SPACE_LEVEL_ENUM, SPACE_LEVEL_OPTIONS } from '@/constants/space.ts'
import { formatSize } from '@/utils'

const space = ref<API.SpaceVo>()
const spaceForm = reactive<API.SpaceAddRequest | API.SpaceEditRequest>({
  spaceName: '我的私有空间',
  spaceLevel: SPACE_LEVEL_ENUM.COMMON,
})
const loading = ref(false)

const spaceLevelList = ref<API.SpaceLevel[]>([])

// 获取空间级别
const fetchSpaceLevelList = async () => {
  try {
    const res = await listSpaceLevelUsingGet()
    if (res.data.code === 0 && res.data.data) {
      spaceLevelList.value = res.data.data
    } else {
      message.error('获取空间级别失败，' + res.data.message)
    }
  } catch (error) {
    message.error('获取空间级别失败，网络错误')
  }
}

onMounted(() => {
  fetchSpaceLevelList()
  getOldSpace()
})

const router = useRouter()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  const spaceId = space.value?.id
  loading.value = true
  let res
  if (spaceId) {
    // 更新
    res = await updateSpaceUsingPost({
      id: spaceId,
      ...spaceForm,
    })
  } else {
    // 创建
    res = await addSpaceUsingPost({
      ...spaceForm,
    })
  }
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success('操作成功')
    // 跳转到空间详情页
    router.push({
      path: `/space/${res.data.data}`,
    })
  } else {
    message.error('操作失败，' + res.data.message)
  }
  loading.value = false
}

const route = useRoute()

// 获取老数据
const getOldSpace = async () => {
  try {
    const id = route.query?.id
    if (id) {
      const res = await getSpaceVoByIdUsingGet({
        id,
      })
      if (res.data.code === 0 && res.data.data) {
        const data = res.data.data
        space.value = data
        spaceForm.spaceName = data.spaceName
        spaceForm.spaceLevel = data.spaceLevel
      }
    }
  } catch (error) {
    message.error('获取空间信息失败，网络错误')
  }
}
</script>

<style scoped>
#addSpacePage {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 24px;
  min-height: 80vh;
  border-radius: 16px;
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

/* 级别卡片容器 */
.level-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

/* 级别卡片 */
.level-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  position: relative;
  overflow: hidden;
}

.level-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(52, 152, 219, 0.25);
  border-color: #3498db;
}

.level-card.active {
  background: linear-gradient(135deg, #3498db 0%, #5dade2 100%);
  border-color: #3498db;
  color: white;
}

.level-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.level-badge {
  font-size: 16px;
  font-weight: 700;
  padding: 6px 14px;
  background: rgba(52, 152, 219, 0.1);
  border-radius: 20px;
  color: #3498db;
}

.level-card.active .level-badge {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.selected-mark {
  width: 24px;
  height: 24px;
  background: #52c41a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: white;
  font-weight: bold;
}

.level-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.level-stat {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #666;
}

.level-card.active .level-stat {
  color: rgba(255, 255, 255, 0.9);
}

.stat-icon {
  font-size: 18px;
}

.stat-value {
  font-weight: 600;
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
.ant-select {
  border-radius: 10px;
  border: 2px solid #e8e8e8;
  transition: all 0.3s ease;
  font-size: 15px;
}

.ant-input:hover,
.ant-select:hover {
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.ant-input:focus,
.ant-select:focus {
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.2);
  outline: none;
}

/* 选择器样式 */
.ant-select {
  width: 100%;
}

/* 提交按钮样式 */
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

/* 响应式设计 */
@media (max-width: 767px) {
  #addSpacePage {
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

  .level-cards {
    grid-template-columns: 1fr;
  }

  .ant-btn-primary {
    font-size: 15px;
    padding: 12px;
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
