package com.im.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求 DTO
 */
@Data
public class LoginDTO {

    /**
     * 用户名/手机号/邮箱
     */
    @NotBlank(message = "账号不能为空")
    private String account;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 设备 ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型 web/ios/android/desktop
     */
    private String deviceType = "web";
}
