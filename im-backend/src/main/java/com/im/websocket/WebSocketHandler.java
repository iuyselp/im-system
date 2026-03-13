package com.im.websocket;

import com.im.constant.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 存储在线用户会话：userId -> WebSocketSession
     */
    private static final Map<Long, WebSocketSession> ONLINE_SESSIONS = new ConcurrentHashMap<>();

    /**
     * 连接建立后
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从 URL 参数或握手信息中获取用户 ID
        Long userId = getUserIdFromSession(session);
        
        if (userId != null) {
            // 存储会话
            ONLINE_SESSIONS.put(userId, session);
            
            // 更新 Redis 在线状态
            redisTemplate.opsForValue().set(
                Constants.REDIS_KEY_USER_ONLINE + userId,
                "online",
                7,
                TimeUnit.DAYS
            );

            // 存储会话 ID 到 Redis
            redisTemplate.opsForValue().set(
                Constants.REDIS_KEY_USER_SESSION + userId,
                session.getId(),
                7,
                TimeUnit.DAYS
            );

            log.info("WebSocket 连接建立：userId={}, sessionId={}", userId, session.getId());

            // 发送欢迎消息
            sendMessage(userId, new WebSocketMessage("CONNECTED", "连接成功"));
        }
    }

    /**
     * 收到消息后
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = getUserIdFromSession(session);
        String payload = message.getPayload();

        log.info("收到 WebSocket 消息：userId={}, payload={}", userId, payload);

        try {
            // 解析消息
            Map<String, Object> msg = objectMapper.readValue(payload, Map.class);
            String type = (String) msg.get("type");

            // 处理不同类型的消息
            switch (type) {
                case "PING":
                    // 心跳响应
                    sendMessage(userId, new WebSocketMessage("PONG", null));
                    break;
                case "ACK":
                    // 消息确认
                    handleAck(userId, msg);
                    break;
                default:
                    log.warn("未知的消息类型：{}", type);
            }
        } catch (Exception e) {
            log.error("处理 WebSocket 消息失败", e);
        }
    }

    /**
     * 连接关闭后
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        
        if (userId != null) {
            // 移除会话
            ONLINE_SESSIONS.remove(userId);
            
            // 更新 Redis 在线状态
            redisTemplate.delete(Constants.REDIS_KEY_USER_ONLINE + userId);
            redisTemplate.delete(Constants.REDIS_KEY_USER_SESSION + userId);

            log.info("WebSocket 连接关闭：userId={}, status={}", userId, status);
        }
    }

    /**
     * 传输错误
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = getUserIdFromSession(session);
        log.error("WebSocket 传输错误：userId={}", userId, exception);
        
        if (session.isOpen()) {
            session.close();
        }
    }

    /**
     * 发送消息给用户
     */
    public void sendMessage(Long userId, WebSocketMessage message) {
        WebSocketSession session = ONLINE_SESSIONS.get(userId);
        
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
                log.debug("消息发送成功：userId={}, type={}", userId, message.getType());
            } catch (IOException e) {
                log.error("发送消息失败：userId={}", userId, e);
                // 发送失败，可能需要存储离线消息
            }
        } else {
            log.warn("用户不在线，存储离线消息：userId={}", userId);
            // TODO: 存储离线消息到 Redis 或数据库
            storeOfflineMessage(userId, message);
        }
    }

    /**
     * 广播消息给多个用户
     */
    public void broadcastMessage(java.util.Collection<Long> userIds, WebSocketMessage message) {
        for (Long userId : userIds) {
            sendMessage(userId, message);
        }
    }

    /**
     * 发送消息给群组
     */
    public void sendToGroup(java.util.Collection<Long> memberIds, WebSocketMessage message) {
        for (Long memberId : memberIds) {
            sendMessage(memberId, message);
        }
    }

    /**
     * 检查用户是否在线
     */
    public boolean isOnline(Long userId) {
        WebSocketSession session = ONLINE_SESSIONS.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineCount() {
        return ONLINE_SESSIONS.size();
    }

    /**
     * 从会话中获取用户 ID
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        // 从 URI 参数获取
        String query = session.getUri().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] kv = param.split("=");
                if ("userId".equals(kv[0]) && kv.length > 1) {
                    try {
                        return Long.parseLong(kv[1]);
                    } catch (NumberFormatException e) {
                        log.error("解析 userId 失败：{}", kv[1]);
                    }
                }
            }
        }
        
        // 从 attributes 获取（在握手拦截器中设置）
        Object userId = session.getAttributes().get("userId");
        return userId instanceof Long ? (Long) userId : null;
    }

    /**
     * 处理消息确认
     */
    private void handleAck(Long userId, Map<String, Object> msg) {
        String msgId = (String) msg.get("msgId");
        log.info("收到消息确认：userId={}, msgId={}", userId, msgId);
        // TODO: 更新消息投递状态
    }

    /**
     * 存储离线消息
     */
    private void storeOfflineMessage(Long userId, WebSocketMessage message) {
        String key = Constants.REDIS_KEY_MSG_OFFLINE + userId;
        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().rightPush(key, json);
            // 设置过期时间 7 天
            redisTemplate.expire(key, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("存储离线消息失败", e);
        }
    }

    /**
     * WebSocket 消息结构
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class WebSocketMessage {
        private String type;
        private Object data;
        private Long timestamp = System.currentTimeMillis();
    }
}
