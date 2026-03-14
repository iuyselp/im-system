package com.im.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息已读状态实体类
 * 
 * @author IM Team
 * @date 2026-03-14
 */
@Data
@TableName("im_message_read")
public class MessageReadStatus {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 消息 ID
     */
    private String messageId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 阅读时间
     */
    private LocalDateTime readAt;
}
