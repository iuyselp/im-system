package com.im.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 发送消息 DTO
 */
@Data
public class SendMessageDTO {

    /**
     * 接收者 ID (单聊)
     */
    private Long toId;

    /**
     * 群 ID (群聊)
     */
    private Long groupId;

    /**
     * 消息类型 TEXT/IMAGE/VOICE/VIDEO/FILE/LOCATION
     */
    @NotBlank(message = "消息类型不能为空")
    private String type;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /**
     * 扩展数据 (JSON 字符串)
     */
    private String extra;

    /**
     * 客户端消息 ID (用于去重)
     */
    private String clientMsgId;
}
