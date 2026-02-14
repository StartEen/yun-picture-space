<template>
  <div id="userRegisterPage">
    <h2 class="title">Cloud Picture Space - 用户注册</h2>
    <div class="desc">企业级智能协同云图库</div>
    <a-form :model="formState" name="basic" autocapitalize="off" @finish="handleSubmit">
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入用户名' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入用户名" />
      </a-form-item>
      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码长度不能小于8位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>
      <a-form-item
        name="checkPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码长度不能小于8位' },
        ]"
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="请确认密码" />
      </a-form-item>
      <div class="tips">
        已经有账号？
        <router-link to="/user/login">快去登录吧！</router-link>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { userRegisterUsingPost } from '@/api/userController.ts'

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const router = useRouter()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  //先判断两次密码是否一样
  if (formState.userPassword !== formState.checkPassword) {
    message.error('两次密码不一致')
    return
  }

  const res = await userRegisterUsingPost(values)
  if (res.data.code === 0 && res.data.data) {
    message.success('注册成功')
    //路由跳转
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败：' + res.data.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
  max-width: 520px;
  margin: 0 auto;
}
/*.title {
  text-align: center;
  margin-bottom: 16px;
}
.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}
.tips {
  margin-bottom: 16px;
  color: #bbb;
  font-size: 13px;
  text-align: right;
}*/
.title {
  font-family: 'Brush Script MT', 'Segoe Script', 'Lucida Handwriting', cursive, sans-serif;
  background: linear-gradient(135deg, #53b9c6 0%, #215ac6 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  font-size: 30px;
  font-weight: normal;
  margin: 0;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-top: 90px;
}

.desc {
  text-align: center;
  color: #666;
  margin-bottom: 50px;
  font-size: 15px;
}

:deep(.ant-form-item) {
  margin-bottom: 20px;
}

:deep(.ant-form-item-label) {
  display: flex;
  align-items: center;
  min-height: 45px; /* 与输入框高度保持一致 */
}

:deep(.ant-form-item-label label) {
  margin-bottom: 0;
  line-height: 1.5;
}

:deep(.ant-input) {
  border-radius: 8px;
  padding: 12px 15px;
  border: 1px solid #e1e1e1;
  transition: border-color 0.3s;
}

:deep(.ant-input:hover) {
  border-color: #667eea;
}

:deep(.ant-input:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}
:deep(.ant-input-password) {
  border-radius: 8px;
  padding: 12px 15px;
  border: 1px solid #e1e1e1;
  transition: border-color 0.3s;
}

:deep(.ant-input-password:hover) {
  border-color: #667eea;
}

:deep(.ant-input-password:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.tips {
  margin-bottom: 20px;
  color: #777;
  font-size: 14px;
  text-align: right;
}

.tips a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.tips a:hover {
  color: #764ba2;
  text-decoration: underline;
}

:deep(.ant-btn-primary) {
  height: 45px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #53b9c6 0%, #215ac6 100%);
  border: none;
  transition: all 0.3s ease;
}

:deep(.ant-btn-primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}
</style>
