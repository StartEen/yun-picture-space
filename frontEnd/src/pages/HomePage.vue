<template>
  <div id="homePage">
    <!-- 搜索框 -->
    <div class="search-bar">
      <a-input-search
        v-model:value="searchParams.searchText"
        placeholder="搜索你感兴趣的图片"
        size="large"
        @search="doSearch"
      >
        <template #enterButton>
          <a-button type="primary" size="large">
            <template #icon>
              <SearchOutlined />
            </template>
            搜索
          </a-button>
        </template>
      </a-input-search>
    </div>

    <!-- 分类和标签筛选 -->
    <a-tabs v-model:active-key="selectedCategory" @change="doSearch">
      <a-tab-pane key="all" tab="全部" />
      <a-tab-pane v-for="category in categoryList" :tab="category" :key="category" />
    </a-tabs>

    <div class="tag-bar">
      <span style="margin-right: 8px">标签：</span>
      <a-space :size="[0, 8]" wrap>
        <a-checkable-tag
          v-for="(tag, index) in tagList"
          :key="tag"
          v-model:checked="selectedTagList[index]"
          @change="doSearch"
        >
          {{ tag }}
        </a-checkable-tag>
      </a-space>
    </div>
    <!-- 图片列表 -->
    <PictureList :dataList="dataList" :loading="loading" />
    <!-- 加载更多提示 -->
    <div v-if="loadingMore" class="loading-more">
      <a-spin />
      <span>加载中...</span>
    </div>
    <div v-else-if="noMore" class="no-more">
      没有更多图片了
    </div>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref, onUnmounted } from 'vue'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import PictureList from "@/components/picture/PictureList.vue";
import { SearchOutlined } from '@ant-design/icons-vue';

// 定义数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)
const loadingMore = ref(false)
const noMore = ref(false)

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 20,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 获取数据
const fetchData = async (isLoadMore = false) => {
  if (isLoadMore) {
    loadingMore.value = true
  } else {
    loading.value = true
  }
  // 转换搜索参数
  const params = {
    ...searchParams,
    tags: [] as string[],
  }
  if (selectedCategory.value !== 'all') {
    params.category = selectedCategory.value
  }
  // [true, false, false] => ['java']
  selectedTagList.value.forEach((useTag, index) => {
    if (useTag) {
      params.tags.push(tagList.value[index])
    }
  })
  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    const records = res.data.data.records ?? []
    if (isLoadMore) {
      dataList.value = [...dataList.value, ...records]
    } else {
      dataList.value = records
    }
    total.value = res.data.data.total ?? 0
    // 判断是否还有更多
    noMore.value = dataList.value.length >= total.value
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
  loadingMore.value = false
}

// 加载更多
const loadMore = async () => {
  if (loadingMore.value || noMore.value) return
  searchParams.current += 1
  await fetchData(true)
}

// 滚动监听
const handleScroll = () => {
  const scrollHeight = document.documentElement.scrollHeight
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const clientHeight = document.documentElement.clientHeight
  // 距离底部 200px 时触发加载
  if (scrollHeight - scrollTop - clientHeight < 200) {
    loadMore()
  }
}

// 页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 搜索
const doSearch = () => {
  // 重置搜索条件
  searchParams.current = 1
  noMore.value = false
  fetchData()
}

// 标签和分类列表
const categoryList = ref<string[]>([])
const selectedCategory = ref<string>('all')
const tagList = ref<string[]>([])
const selectedTagList = ref<string[]>([])

//获取标签和分类选项
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    // 转换成下拉选项组件接受的格式
    tagList.value = res.data.data.tagList ?? []
    categoryList.value = res.data.data.categoryList ?? []
  } else {
    message.error('加载选项失败，' + res.data.message)
  }
}
onMounted(() => {
  getTagCategoryOptions()
})
</script>
<style scoped>
#homePage {
  max-width: 1450px;
  margin: 0 auto;
  padding: 20px;
}

#homePage .search-bar {
  max-width: 580px;
  margin: 0 auto 30px;
}

#homePage .search-bar :deep(.ant-input-search-large .ant-input) {
  border-radius: 8px 0 0 8px;
  padding-left: 16px;
}

#homePage .search-bar :deep(.ant-input-search-large .ant-btn) {
  border-radius: 0 8px 8px 0;
  padding: 0 24px;
  min-width: 100px;
}

#homePage :deep(.ant-tabs) {
  margin-bottom: 16px;
}

#homePage :deep(.ant-tabs-tab) {
  font-size: 15px;
  padding: 8px 16px;
}

#homePage .tag-bar {
  margin-bottom: 24px;
  padding: 12px 16px;
  background: #fafafa;
  border-radius: 8px;
}

#homePage .tag-bar :deep(.ant-checkable-tag) {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 13px;
  transition: all 0.2s ease;
}

#homePage .tag-bar :deep(.ant-checkable-tag-checked) {
  background: #1890ff;
  color: #fff;
}

#homePage .loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  color: #666;
}

#homePage .no-more {
  text-align: center;
  padding: 24px;
  color: #999;
  font-size: 14px;
}
</style>
