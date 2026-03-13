package com.im.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群成员实体
 */
@Data
@Accessors(chain = true)
@TableName("im_group_member")
public class GroupMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long groupId;

    private Long userId;

    /**
     * 角色 0-成员 1-管理员 2-群主
     */
    private Integer role;

    /**
     * 群昵称
     */
    private String alias;

    /**
     * 禁言截止时间
     */
    private LocalDateTime muteUntil;

    /**
     * 是否免打扰
     */
    private Integer isMuted;

    /**
     * 加入时间
     */
    private LocalDateTime joinedAt;

    /**
     * 邀请人 ID
     */
    private Long joinedBy;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
