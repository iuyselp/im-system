package com.im.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息实体
 */
@Data
@Accessors(chain = true)
@TableName("im_message")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消息唯一 ID
     */
    private String msgId;

    /**
     * 发送者 ID
     */
    private Long fromId;

    /**
     * 接收者 ID (单聊)
     */
    private Long toId;

    /**
     * 群 ID (群聊)
     */
    private Long groupId;

    /**
     * 消息类型 TEXT/IMAGE/VOICE/VIDEO/FILE/LOCATION/SYSTEM
     */
    private String type;

    /**
     * 消息子类型
     */
    private String subType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 扩展数据 (JSON)
     */
    private String extra;

    /**
     * 状态 1-正常 0-撤回 2-删除
     */
    private Integer status;

    /**
     * 是否已读
     */
    private Integer isRead;

    /**
     * 已读人数 (群消息)
     */
    private Integer readCount;

    /**
     * 投递状态 sent/delivered/read/failed
     */
    private String deliveryStatus;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
