package com.im.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户 Token 实体
 */
@Data
@Accessors(chain = true)
@TableName("im_user_token")
public class UserToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String token;

    private String refreshToken;

    private String deviceId;

    private String deviceName;

    private String deviceType;

    private String ipAddress;

    private String userAgent;

    private LocalDateTime expiresAt;

    private Integer isValid;

    private LocalDateTime createdAt;
}
