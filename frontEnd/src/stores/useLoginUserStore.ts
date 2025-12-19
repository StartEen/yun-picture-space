import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 定义一个用于管理登录用户状态的store
 * @returns 包含登录用户信息、获取用户方法和设置用户方法的对象
 */
export const useLoginUserStore = defineStore('loginUser', () => {
  /**
   * 存储当前登录用户信息的响应式变量
   * 默认值为未登录状态
   */
  const loginUser = ref<any>({
    userName: '未登录',
  })

  /**
   * 异步获取登录用户信息
   * @returns Promise<void>
   */
  async function getLoginUser() {}

  /**
   * 设置登录用户信息
   * @param newLoginUser 新的登录用户对象
   */
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, getLoginUser, setLoginUser }
})
