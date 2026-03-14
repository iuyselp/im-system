import request from './request'

/**
 * 通话相关 API
 */
export default {
  /**
   * 发起通话
   * @param {Object} data - 通话参数
   * @param {number} data.callerId - 呼叫方 ID
   * @param {number} [data.receiverId] - 接收方 ID
   * @param {number} [data.groupId] - 群组 ID
   * @param {string} data.type - 通话类型 AUDIO/VIDEO
   * @param {string} data.callId - 通话 ID
   */
  startCall(data) {
    return request({
      url: '/api/call/start',
      method: 'post',
      params: data
    })
  },

  /**
   * 接听通话
   * @param {string} callId - 通话 ID
   */
  answerCall(callId) {
    return request({
      url: '/api/call/answer',
      method: 'post',
      params: { callId }
    })
  },

  /**
   * 拒接通话
   * @param {string} callId - 通话 ID
   */
  rejectCall(callId) {
    return request({
      url: '/api/call/reject',
      method: 'post',
      params: { callId }
    })
  },

  /**
   * 结束通话
   * @param {string} callId - 通话 ID
   * @param {number} duration - 通话时长（秒）
   */
  endCall(callId, duration) {
    return request({
      url: '/api/call/end',
      method: 'post',
      params: { callId, duration }
    })
  },

  /**
   * 获取通话历史
   * @param {number} userId - 用户 ID
   * @param {number} page - 页码
   * @param {number} size - 每页数量
   */
  getCallHistory(userId, page = 1, size = 20) {
    return request({
      url: '/api/call/history',
      method: 'get',
      params: { userId, page, size }
    })
  },

  /**
   * 获取未接来电数
   * @param {number} userId - 用户 ID
   */
  getMissedCallCount(userId) {
    return request({
      url: '/api/call/missed/count',
      method: 'get',
      params: { userId }
    })
  },

  /**
   * 获取通话详情
   * @param {string} callId - 通话 ID
   */
  getCallDetail(callId) {
    return request({
      url: '/api/call/detail',
      method: 'get',
      params: { callId }
    })
  },

  /**
   * 获取 WebRTC 配置
   */
  getWebRTCConfig() {
    return request({
      url: '/api/call/webrtc/config',
      method: 'get'
    })
  }
}
