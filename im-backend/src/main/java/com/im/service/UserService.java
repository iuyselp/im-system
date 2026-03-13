package com.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.im.dto.LoginDTO;
import com.im.dto.RegisterDTO;
import com.im.entity.User;
import com.im.result.Result;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    Result<User> register(RegisterDTO dto);

    /**
     * 用户登录
     */
    Result<LoginResult> login(LoginDTO dto);

    /**
     * 用户登出
     */
    Result<Void> logout(Long userId);

    /**
     * 刷新 Token
     */
    Result<LoginResult> refreshToken(String refreshToken);

    /**
     * 获取用户信息
     */
    Result<User> getUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    Result<User> updateUserInfo(Long userId, User user);

    /**
     * 修改密码
     */
    Result<Void> changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 获取通讯录
     */
    Result<Object> getContacts(Long userId);

    /**
     * 添加好友
     */
    Result<Void> addFriend(Long userId, Long friendId, String message);

    /**
     * 处理好友请求
     */
    Result<Void> handleFriendRequest(Long userId, Long requestId, Integer status);

    /**
     * 拉黑用户
     */
    Result<Void> blockUser(Long userId, Long targetId);
}
