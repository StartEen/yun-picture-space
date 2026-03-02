<template>
  <div class="picture-list">
    <!-- 图片列表 -->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <!-- 单张图片 -->
          <a-card hoverable>
            <!--<a-card hoverable @click="doClickPicture(picture)">-->
            <template #cover>
              <img
                :alt="picture.name"
                :src="picture.url ?? picture.thumbnailUrl"
                style="height: 180px; object-fit: cover"
              />
            </template>
            <a-card-meta :title="picture.name || '未命名图片'">
              <template #description>
                <a-flex>
                  <a-tag color="green">
                    {{ picture.category || '默认' }}
                  </a-tag>
                  <a-tag v-for="tag in (picture.tags || [])" :key="tag">
                    {{ tag }}
                  </a-tag>
                </a-flex>
              </template>
            </a-card-meta>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { listPictureVoByPageUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

//数据
const dataList = ref([])
const loading = ref(true)
const total = ref(0)

//搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

//分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current,
    pageSize: searchParams.pageSize,
    total: total.value,
    //切换页号时，会修改索索参数并获取数据
    onChange: (page, pageSize) => {
      searchParams.current = page
      searchParams.pageSize = pageSize
      fetchData()
    },
  }
})

//获取数据
const fetchData = async () => {
  loading.value = true
  const res = await listPictureVoByPageUsingPost(searchParams)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

//页面加载时请求
onMounted(() => {
  fetchData()
})
</script>

<style scoped></style>
