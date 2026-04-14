export default class PictureEditWebSocket {
  private pictureId: number
  private socket: WebSocket | null
  private eventHandlers: any

  constructor(pictureId: number) {
    this.pictureId = pictureId // 图片ID
    this.socket = null // WebSocket实例
    this.eventHandlers = {} // 事件处理函数
  }

  /**
   * 初始化WebSocket连接
   */
  connect() {
    const url = `ws://localhost:8123/api/ws/picture/edit?pictureId=${this.pictureId}`
    this.socket = new WebSocket(url)

    //设置携带token
    this.socket.binaryType = 'blob'

    //监听连接成功事件
    this.socket.onopen = () => {
      console.log('WebSocket连接成功')
      this.triggerEvent('open')
    }

    //监听消息事件
    this.socket.onmessage = (event) => {
      const message = JSON.parse(event.data)
      console.log('WebSocket收到消息:', message)

      //根据消息类型触发对应事件
      const type = message.type
      this.triggerEvent(type, message)
    }

    //监听链接关闭事件
    this.socket.onclose = (event) => {
      console.log('WebSocket连接已关闭:', event)
      this.triggerEvent('close', event)
    }

    //监听错误事件
    this.socket.onerror = (error) => {
      console.error('WebSocket错误:', error)
      this.triggerEvent('error', error)
    }
  }

  /**
   * 关闭WebSocket连接
   */
  disconnect() {
    if (this.socket) {
      this.socket.close()
      console.log('WebSocket已关闭')
    }
  }

  /**
   * 发送信息到后端
   * @param {object}  message 消息对象
   */
  sendMessage(message: object) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(message))
      console.log('已发送消息:', message)
    } else {
      console.error('WebSocket未连接或已关闭,无法发送信息：', message)
    }
  }

  /**
   * 添加自定义事件监听
   * @param {string} type 消息类型
   * @param {Function} handler 事件处理函数
   */
  on(type: string, handler: (data?: any) => void) {
    if (!this.eventHandlers[type]) {
      this.eventHandlers[type] = []
    }
    this.eventHandlers[type].push(handler)
  }

  /**
   * 触发自定义事件
   * @param {string} type 事件类型
   * @param {any} data 事件数据
   */
  triggerEvent(type: string, data?: any) {
    const handlers = this.eventHandlers[type]
    if (handlers) {
      handlers.forEach((handler: any) => handler(data))
    }
  }
}
