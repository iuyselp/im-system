package com.im.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 消息体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WsMessage {
    
    /**
     * 消息类型
     */
    private String type;
    
    /**
     * 消息数据
     */
    private Object data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 消息 ID（用于 ACK）
     */
    private String messageId;
    
    /**
     * 发送者 ID
     */
    private Long fromId;
    
    /**
     * 接收者 ID
     */
    private Long toId;
    
    /**
     * 创建消息
     */
    public static WsMessage of(String type, Object data) {
        return WsMessage.builder()
                .type(type)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建消息（带发送者）
     */
    public static WsMessage of(String type, Object data, Long fromId) {
        return WsMessage.builder()
                .type(type)
                .data(data)
                .fromId(fromId)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
