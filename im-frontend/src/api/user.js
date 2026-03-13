import { get, post, put } from './request'

/**
 * 用户登录
 */
export function login(data) {
  return post('/auth/login', data)
}

/**
 * 用户注册
 */
export function register(data) {
  return post('/auth/register', data)
}

/**
 * 用户登出
 */
export function logout() {
  return post('/auth/logout')
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  return get('/user/info')
}

/**
 * 更新用户信息
 */
export function updateUserInfo(data) {
  return put('/user/info', data)
}

/**
 * 修改密码
 */
export function changePassword(oldPassword, newPassword) {
  return put('/user/password', null, {
    params: { oldPassword, newPassword }
  })
}
