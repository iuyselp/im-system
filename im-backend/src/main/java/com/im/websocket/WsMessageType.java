package com.im.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 消息类型枚举
 */
public enumWsMessageType {
    
    // 连接相关
    CONNECTED("CONNECTED"),           // 连接成功
    DISCONNECTED("DISCONNECTED"),     // 连接断开
    ERROR("ERROR"),                   // 错误消息
    
    // 心跳
    PING("PING"),                     // 心跳请求
    PONG("PONG"),                     // 心跳响应
    
    // 消息相关
    NEW_MESSAGE("NEW_MESSAGE"),       // 新消息
    MESSAGE_ACK("MESSAGE_ACK"),       // 消息确认
    MESSAGE_READ("MESSAGE_READ"),     // 消息已读
    MESSAGE_REVOKE("MESSAGE_REVOKE"), // 消息撤回
    MESSAGE_DELETE("MESSAGE_DELETE"), // 消息删除
    
    // 通话相关（信令）
    CALL_INVITE("CALL_INVITE"),       // 通话邀请
    CALL_ANSWER("CALL_ANSWER"),       // 接听通话
    CALL_REJECT("CALL_REJECT"),       // 拒接通话
    CALL_END("CALL_END"),             // 结束通话
    CALL_CANCEL("CALL_CANCEL"),       // 取消通话
    
    // WebRTC 信令
    ICE_CANDIDATE("ICE_CANDIDATE"),   // ICE 候选
    OFFER("OFFER"),                   // SDP Offer
    ANSWER("ANSWER"),                 // SDP Answer
    
    // 在线状态
    USER_ONLINE("USER_ONLINE"),       // 用户上线
    USER_OFFLINE("USER_OFFLINE"),     // 用户下线
    USER_STATUS("USER_STATUS");       // 用户状态变更
    
    private final String value;
    
    WsMessageType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
