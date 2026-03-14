import request from './request'

/**
 * 消息已读相关 API
 */
export default {
  /**
   * 标记消息为已读
   * @param {number} userId - 用户 ID
   * @param {string} messageId - 消息 ID
   */
  markAsRead(userId, messageId) {
    return request({
      url: '/api/message/read/mark',
      method: 'post',
      params: { userId, messageId }
    })
  },

  /**
   * 批量标记消息为已读
   * @param {number} userId - 用户 ID
   * @param {Array<string>} messageIds - 消息 ID 列表
   */
  batchMarkAsRead(userId, messageIds) {
    return request({
      url: '/api/message/read/mark/batch',
      method: 'post',
      params: { userId },
      data: messageIds
    })
  },

  /**
   * 标记会话为已读
   * @param {number} userId - 用户 ID
   * @param {string} conversationId - 会话 ID
   */
  markConversationAsRead(userId, conversationId) {
    return request({
      url: '/api/message/read/mark/conversation',
      method: 'post',
      params: { userId, conversationId }
    })
  },

  /**
   * 获取消息的已读用户列表
   * @param {string} messageId - 消息 ID
   */
  getReadUserIds(messageId) {
    return request({
      url: '/api/message/read/readers',
      method: 'get',
      params: { messageId }
    })
  },

  /**
   * 获取未读消息数
   * @param {number} userId - 用户 ID
   * @param {string} [conversationId] - 会话 ID（可选）
   */
  getUnreadCount(userId, conversationId) {
    return request({
      url: '/api/message/read/unread/count',
      method: 'get',
      params: { userId, conversationId }
    })
  },

  /**
   * 检查消息是否已读
   * @param {string} messageId - 消息 ID
   * @param {number} userId - 用户 ID
   */
  isRead(messageId, userId) {
    return request({
      url: '/api/message/read/is-read',
      method: 'get',
      params: { messageId, userId }
    })
  }
}
