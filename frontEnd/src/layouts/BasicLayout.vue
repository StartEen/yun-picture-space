<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader/>
      </a-layout-header>
      <a-layout-content class="content">
        <router-view/>
      </a-layout-content>
      <a-layout-footer class="footer">
        自己做的页面
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from "@/components/GlobalHeader.vue";
</script>
<style scoped>
#basicLayout{

}

#basicLayout .header{
  padding-inline: 20px;
  margin-bottom: 16px;
  color: unset;
  background: white;
}


#basicLayout .footer{
  background: #efefef;
  padding: 16px;
  position: fixed;
  bottom: 0px;
  left: 0;
  right: 0;
  text-align: center;
}

#basicLayout .content{
  /*渐变*/
  background: linear-gradient(to right,#fefefe,#fff);
  margin-bottom: 28px;
  padding: 20px;
}

</style>
