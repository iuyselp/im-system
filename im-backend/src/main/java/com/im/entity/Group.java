package com.im.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组实体
 */
@Data
@Accessors(chain = true)
@TableName("im_group")
public class Group implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 群 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 群名称
     */
    private String name;

    /**
     * 群头像 URL
     */
    private String avatar;

    /**
     * 群主 ID
     */
    private Long ownerId;

    /**
     * 群公告
     */
    private String notice;

    /**
     * 公告更新人
     */
    private Long noticeUpdatedBy;

    /**
     * 公告更新时间
     */
    private LocalDateTime noticeUpdatedAt;

    /**
     * 最大成员数
     */
    private Integer maxMembers;

    /**
     * 当前成员数
     */
    private Integer memberCount;

    /**
     * 状态 1-正常 0-解散
     */
    private Integer status;

    /**
     * 是否全员禁言
     */
    private Integer isMuted;

    /**
     * 加入方式 0-自由 1-审核 2-禁止
     */
    private Integer joinType;

    /**
     * 聊天类型 0-公开 1-私密
     */
    private Integer chatType;

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

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
