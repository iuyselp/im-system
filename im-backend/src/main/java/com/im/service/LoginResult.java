package com.im.service;

import com.im.entity.User;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 登录结果
 */
@Data
@Accessors(chain = true)
public class LoginResult {

    /**
     * 访问 Token
     */
    private String accessToken;

    /**
     * 刷新 Token
     */
    private String refreshToken;

    /**
     * Token 类型
     */
    private String tokenType = "Bearer";

    /**
     * 过期时间 (秒)
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private User user;
}
