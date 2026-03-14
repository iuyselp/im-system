package com.im.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通话记录实体类
 * 
 * @author IM Team
 * @date 2026-03-14
 */
@Data
@TableName("im_call_record")
public class CallRecord {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 通话 ID（唯一标识）
     */
    private String callId;

    /**
     * 呼叫方用户 ID
     */
    private Long callerId;

    /**
     * 接收方用户 ID（群聊时为 null）
     */
    private Long receiverId;

    /**
     * 群组 ID（群聊时填写）
     */
    private Long groupId;

    /**
     * 通话类型：AUDIO-语音，VIDEO-视频
     */
    private String type;

    /**
     * 通话时长（秒）
     */
    private Integer duration;

    /**
     * 通话状态：ANSWERED-已接听，MISSED-未接，REJECTED-拒接，CANCELLED-取消
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
