package com.im.controller;

import com.im.result.Result;
import com.im.service.CallSignalingService;
import com.im.websocket.WebSocketHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * WebSocket 消息控制器（STOMP）
 * 
 * @author IM Team
 * @date 2026-03-14
 */
@Slf4j
@Api(tags = "WebSocket 信令管理")
@RestController
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CallSignalingService callSignalingService;
    private final WebSocketHandler webSocketHandler;

    /**
     * 通话信令请求
     */
    @Data
    public static class SignalingRequest {
        private String callId;
        private Long receiverId;
        private Long groupId;
        private String type; // AUDIO/VIDEO
        private String action; // INVITE/ANSWER/REJECT/END/CANCEL
        private Integer duration;
        private Object sdp; // SDP Offer/Answer
        private Object candidate; // ICE Candidate
    }

    @PostMapping("/ws/signaling/call")
    @ApiOperation("发送通话信令")
    public Result<Void> sendCallSignaling(
            @RequestAttribute("userId") Long userId,
            @RequestBody SignalingRequest request) {
        
        log.info("收到通话信令：userId={}, action={}, callId={}", userId, request.getAction(), request.getCallId());
        
        switch (request.getAction()) {
            case "INVITE":
                callSignalingService.sendCallInvite(
                    userId, 
                    request.getReceiverId(), 
                    request.getGroupId(),
                    request.getType(), 
                    request.getCallId()
                );
                break;
                
            case "ANSWER":
                callSignalingService.sendCallAnswer(request.getCallId(), userId);
                break;
                
            case "REJECT":
                callSignalingService.sendCallReject(request.getCallId(), userId);
                break;
                
            case "END":
                callSignalingService.sendCallEnd(request.getCallId(), userId, request.getDuration());
                break;
                
            case "CANCEL":
                callSignalingService.sendCallCancel(
                    request.getCallId(), 
                    userId, 
                    request.getReceiverId()
                );
                break;
                
            default:
                log.warn("未知的信令动作：{}", request.getAction());
                return Result.error("未知的信令动作");
        }
        
        return Result.success();
    }

    @PostMapping("/ws/signaling/webrtc")
    @ApiOperation("发送 WebRTC 信令（ICE/SDP）")
    public Result<Void> sendWebRTCSignaling(
            @RequestAttribute("userId") Long userId,
            @RequestBody SignalingRequest request) {
        
        Long targetId = request.getReceiverId();
        if (targetId == null) {
            return Result.error("接收方 ID 不能为空");
        }
        
        if (request.getSdp() != null) {
            // 判断是 Offer 还是 Answer
            // 这里简化处理，实际应该根据通话状态判断
            callSignalingService.sendAnswer(userId, targetId, request.getSdp());
        } else if (request.getCandidate() != null) {
            callSignalingService.sendIceCandidate(userId, targetId, request.getCandidate());
        }
        
        return Result.success();
    }

    /**
     * STOMP 方式的消息处理（可选）
     */
    @MessageMapping("/call.invite")
    @SendTo("/topic/call")
    public SignalingRequest handleCallInvite(@Payload SignalingRequest request, 
                                              SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        log.info("STOMP 通话邀请：userId={}, callId={}", userId, request.getCallId());
        return request;
    }

    /**
     * 获取在线用户数
     */
    @GetMapping("/ws/online/count")
    @ApiOperation("获取在线用户数")
    public Result<Integer> getOnlineCount() {
        int count = webSocketHandler.getOnlineCount();
        return Result.success(count);
    }

    /**
     * 检查用户是否在线
     */
    @GetMapping("/ws/online/check")
    @ApiOperation("检查用户是否在线")
    public Result<Boolean> checkOnline(@RequestParam Long userId) {
        boolean online = webSocketHandler.isOnline(userId);
        return Result.success(online);
    }
}
