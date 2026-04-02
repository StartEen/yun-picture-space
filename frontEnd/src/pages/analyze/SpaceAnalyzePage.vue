<template>
  <div id="spaceAnalyzePage">
    <h2>
      空间图库分析 -
      <span v-if="queryAll">全部空间</span>
      <span v-else-if="queryPublic">公共图库</span>
      <span v-else>
        <a :href="`/space/${spaceId}`" target="_blank">空间 id：{{ spaceId }}</a>
      </span>
    </h2>
    <div style="margin-bottom: 16px" />



  </div>
</template>

<script setup lang="ts">
import {useRoute} from "vue-router";
import {computed} from "vue";
import {useLoginUserStore} from "@/stores/useLoginUserStore.ts";

const route = useRoute()

// 空间 id
const spaceId = computed(() => {
  return route.query?.spaceId as string
})

// 判断用户是否为管理员
const loginUserStore = useLoginUserStore()
const loginUser = loginUserStore.loginUser
const isAdmin = computed(() => {
  return loginUser.userRole === 'admin'
})

// 是否查询所有空间
const queryAll = computed(() => {
  return !!route.query?.queryAll
})

// 是否查询公共空间
const queryPublic = computed(() => {
  return !!route.query?.queryPublic
})


</script>

<style scoped>
#spaceAnalyzePage {
  margin-bottom: 16px;
}
</style>
