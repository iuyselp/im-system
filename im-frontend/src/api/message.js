import { get, post } from './request'

/**
 * 发送消息
 */
export function sendMessage(data) {
  return post('/message/send', data)
}

/**
 * 获取消息列表
 */
export function getMessageList(params) {
  return get('/message/list', params)
}

/**
 * 撤回消息
 */
export function revokeMessage(msgId) {
  return post('/message/revoke', null, { params: { msgId } })
}

/**
 * 删除消息
 */
export function deleteMessage(msgId) {
  return post('/message/delete', null, { params: { msgId } })
}

/**
 * 标记已读
 */
export function markAsRead(params) {
  return post('/message/read', null, { params })
}

/**
 * 获取未读消息数
 */
export function getUnreadCount() {
  return get('/message/unread')
}
