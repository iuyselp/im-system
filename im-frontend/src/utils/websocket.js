import { useUserStore } from '@/store/user'

/**
 * WebSocket 消息类型
 */
export const WsMessageType = {
  // 连接相关
  CONNECTED: 'CONNECTED',
  DISCONNECTED: 'DISCONNECTED',
  ERROR: 'ERROR',
  
  // 心跳
  PING: 'PING',
  PONG: 'PONG',
  
  // 消息相关
  NEW_MESSAGE: 'NEW_MESSAGE',
  MESSAGE_ACK: 'MESSAGE_ACK',
  MESSAGE_READ: 'MESSAGE_READ',
  MESSAGE_REVOKE: 'MESSAGE_REVOKE',
  MESSAGE_DELETE: 'MESSAGE_DELETE',
  
  // 通话相关
  CALL_INVITE: 'CALL_INVITE',
  CALL_ANSWER: 'CALL_ANSWER',
  CALL_REJECT: 'CALL_REJECT',
  CALL_END: 'CALL_END',
  CALL_CANCEL: 'CALL_CANCEL',
  
  // WebRTC 信令
  ICE_CANDIDATE: 'ICE_CANDIDATE',
  OFFER: 'OFFER',
  ANSWER: 'ANSWER',
  
  // 在线状态
  USER_ONLINE: 'USER_ONLINE',
  USER_OFFLINE: 'USER_OFFLINE',
  USER_STATUS: 'USER_STATUS'
}

/**
 * WebSocket 管理器
 */
class WebSocketManager {
  constructor() {
    this.ws = null
    this.url = null
    this.reconnectCount = 0
    this.maxReconnect = 5
    this.reconnectDelay = 3000
    this.heartbeatTimer = null
    this.heartbeatInterval = 30000 // 30 秒
    this.listeners = new Map()
    this.isConnected = false
  }

  /**
   * 连接 WebSocket
   */
  connect() {
    const userStore = useUserStore()
    const token = userStore.token
    
    if (!token) {
      console.error('WebSocket 连接失败：未登录')
      return
    }

    // 构建 WebSocket URL
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = import.meta.env.VITE_WS_HOST || window.location.host || 'localhost:8080'
    this.url = `${protocol}//${host}/ws/text?token=${token}`

    console.log('WebSocket 连接:', this.url)

    try {
      this.ws = new WebSocket(this.url)

      this.ws.onopen = () => {
        console.log('WebSocket 连接成功')
        this.isConnected = true
        this.reconnectCount = 0
        this.startHeartbeat()
        this.emit(WsMessageType.CONNECTED, { timestamp: Date.now() })
      }

      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data)
          console.log('WebSocket 消息:', message)
          
          // 处理心跳响应
          if (message.type === WsMessageType.PONG) {
            return
          }
          
          // 分发事件
          this.emit(message.type, message.data)
          
          // 特殊处理通话邀请
          if (message.type === WsMessageType.CALL_INVITE) {
            this.handleCallInvite(message.data)
          }
        } catch (error) {
          console.error('解析 WebSocket 消息失败:', error)
        }
      }

      this.ws.onclose = (event) => {
        console.log('WebSocket 连接关闭:', event.code, event.reason)
        this.isConnected = false
        this.stopHeartbeat()
        this.emit(WsMessageType.DISCONNECTED, { code: event.code, reason: event.reason })
        
        // 尝试重连
        this.scheduleReconnect()
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket 错误:', error)
        this.emit(WsMessageType.ERROR, { error })
      }
    } catch (error) {
      console.error('创建 WebSocket 连接失败:', error)
      this.scheduleReconnect()
    }
  }

  /**
   * 断开连接
   */
  disconnect() {
    this.stopHeartbeat()
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.isConnected = false
  }

  /**
   * 发送消息
   */
  send(type, data) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.warn('WebSocket 未连接，无法发送消息')
      return false
    }

    const message = {
      type,
      data,
      timestamp: Date.now()
    }

    try {
      this.ws.send(JSON.stringify(message))
      return true
    } catch (error) {
      console.error('发送 WebSocket 消息失败:', error)
      return false
    }
  }

  /**
   * 监听事件
   */
  on(type, callback) {
    if (!this.listeners.has(type)) {
      this.listeners.set(type, [])
    }
    this.listeners.get(type).push(callback)

    // 返回取消监听函数
    return () => {
      this.off(type, callback)
    }
  }

  /**
   * 取消监听
   */
  off(type, callback) {
    const callbacks = this.listeners.get(type)
    if (callbacks) {
      const index = callbacks.indexOf(callback)
      if (index > -1) {
        callbacks.splice(index, 1)
      }
    }
  }

  /**
   * 触发事件
   */
  emit(type, data) {
    const callbacks = this.listeners.get(type)
    if (callbacks) {
      callbacks.forEach(callback => {
        try {
          callback(data)
        } catch (error) {
          console.error('执行 WebSocket 回调失败:', error)
        }
      })
    }
  }

  /**
   * 开始心跳
   */
  startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatTimer = setInterval(() => {
      this.send(WsMessageType.PING, null)
    }, this.heartbeatInterval)
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 计划重连
   */
  scheduleReconnect() {
    if (this.reconnectCount >= this.maxReconnect) {
      console.error('WebSocket 重连次数已达上限')
      return
    }

    const delay = this.reconnectDelay * Math.pow(2, this.reconnectCount)
    this.reconnectCount++

    console.log(`WebSocket 将在 ${delay}ms 后重连 (${this.reconnectCount}/${this.maxReconnect})`)

    setTimeout(() => {
      console.log('WebSocket 开始重连...')
      this.connect()
    }, delay)
  }

  /**
   * 处理通话邀请
   */
  handleCallInvite(data) {
    console.log('收到通话邀请:', data)
    // 实际处理由 call store 完成，这里只是转发事件
  }

  /**
   * 发送通话信令
   */
  sendCallSignaling(action, payload) {
    return this.send(action, payload)
  }

  /**
   * 获取连接状态
   */
  getConnectionState() {
    if (!this.ws) return 'CLOSED'
    
    switch (this.ws.readyState) {
      case WebSocket.CONNECTING: return 'CONNECTING'
      case WebSocket.OPEN: return 'OPEN'
      case WebSocket.CLOSING: return 'CLOSING'
      case WebSocket.CLOSED: return 'CLOSED'
      default: return 'UNKNOWN'
    }
  }
}

// 创建单例
const wsManager = new WebSocketManager()

export default wsManager
