package com.im.config;

import com.im.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    /**
     * 握手前
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest serverRequest = (ServletServerHttpRequest) request;
            
            // 获取 Token
            String token = serverRequest.getServletRequest().getParameter("token");
            
            if (token != null && jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);
                
                if (userId != null) {
                    // 将用户信息放入 attributes，WebSocketHandler 可以获取
                    attributes.put("userId", userId);
                    attributes.put("username", username);
                    log.info("WebSocket 握手成功：userId={}, username={}", userId, username);
                    return true;
                }
            }
            
            log.warn("WebSocket 握手失败：Token 无效");
            return false;
        }
        
        return true;
    }

    /**
     * 握手后
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // 握手后处理
    }
}
