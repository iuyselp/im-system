package com.im.service.impl;

import com.im.entity.CallRecord;
import com.im.service.CallService;
import com.im.service.CallSignalingService;
import com.im.websocket.WsMessage;
import com.im.websocket.WebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 通话信令服务实现类
 * 
 * @author IM Team
 * @date 2026-03-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallSignalingServiceImpl implements CallSignalingService {

    private final WebSocketHandler webSocketHandler;
    private final CallService callService;

    @Override
    public void sendCallInvite(Long callerId, Long receiverId, Long groupId, String callType, String callId) {
        log.info("发送通话邀请：callerId={}, receiverId={}, groupId={}, callType={}, callId={}", 
                callerId, receiverId, groupId, callType, callId);
        
        // 创建通话记录
        CallRecord callRecord = callService.createCall(callerId, receiverId, groupId, callType, callId);
        
        // 构建通话邀请数据
        Map<String, Object> inviteData = new HashMap<>();
        inviteData.put("callId", callId);
        inviteData.put("callerId", callerId);
        inviteData.put("receiverId", receiverId);
        inviteData.put("groupId", groupId);
        inviteData.put("type", callType);
        inviteData.put("timestamp", System.currentTimeMillis());
        
        // 发送信令给接收方
        WsMessage message = WsMessage.of("CALL_INVITE", inviteData, callerId);
        
        if (groupId != null) {
            // 群聊通话：发送给所有群成员
            // TODO: 获取群成员列表并群发
            log.info("群聊通话，需要群发信令：groupId={}", groupId);
        } else if (receiverId != null) {
            // 单聊通话：发送给接收方
            webSocketHandler.sendMessage(receiverId, message);
        }
    }

    @Override
    public void sendCallAnswer(String callId, Long userId) {
        log.info("发送接听信令：callId={}, userId={}", callId, callId, userId);
        
        // 更新通话状态
        callService.updateCallStatus(callId, "ANSWERED", 0);
        
        // 构建接听数据
        Map<String, Object> answerData = new HashMap<>();
        answerData.put("callId", callId);
        answerData.put("userId", userId);
        answerData.put("status", "ANSWERED");
        answerData.put("timestamp", System.currentTimeMillis());
        
        // 发送信令给呼叫方
        WsMessage message = WsMessage.of("CALL_ANSWER", answerData, userId);
        
        // 获取通话记录，找到呼叫方
        CallRecord callRecord = callService.getCallDetail(callId);
        if (callRecord != null && callRecord.getCallerId() != null) {
            webSocketHandler.sendMessage(callRecord.getCallerId(), message);
        }
    }

    @Override
    public void sendCallReject(String callId, Long userId) {
        log.info("发送拒接信令：callId={}, userId={}", callId, userId);
        
        // 更新通话状态
        callService.updateCallStatus(callId, "REJECTED", 0);
        
        // 构建拒接数据
        Map<String, Object> rejectData = new HashMap<>();
        rejectData.put("callId", callId);
        rejectData.put("userId", userId);
        rejectData.put("status", "REJECTED");
        rejectData.put("timestamp", System.currentTimeMillis());
        
        // 发送信令给呼叫方
        WsMessage message = WsMessage.of("CALL_REJECT", rejectData, userId);
        
        CallRecord callRecord = callService.getCallDetail(callId);
        if (callRecord != null && callRecord.getCallerId() != null) {
            webSocketHandler.sendMessage(callRecord.getCallerId(), message);
        }
    }

    @Override
    public void sendCallEnd(String callId, Long userId, Integer duration) {
        log.info("发送结束通话信令：callId={}, userId={}, duration={}", callId, userId, duration);
        
        // 更新通话状态
        callService.updateCallStatus(callId, "COMPLETED", duration);
        
        // 构建结束数据
        Map<String, Object> endData = new HashMap<>();
        endData.put("callId", callId);
        endData.put("userId", userId);
        endData.put("duration", duration);
        endData.put("status", "COMPLETED");
        endData.put("timestamp", System.currentTimeMillis());
        
        // 发送信令给通话另一方
        WsMessage message = WsMessage.of("CALL_END", endData, userId);
        
        CallRecord callRecord = callService.getCallDetail(callId);
        if (callRecord != null) {
            // 发送给呼叫方（如果不是当前用户）
            if (!callRecord.getCallerId().equals(userId)) {
                webSocketHandler.sendMessage(callRecord.getCallerId(), message);
            }
            // 发送给接收方（如果不是当前用户）
            if (callRecord.getReceiverId() != null && !callRecord.getReceiverId().equals(userId)) {
                webSocketHandler.sendMessage(callRecord.getReceiverId(), message);
            }
        }
    }

    @Override
    public void sendCallCancel(String callId, Long callerId, Long receiverId) {
        log.info("发送取消通话信令：callId={}, callerId={}, receiverId={}", callId, callerId, receiverId);
        
        // 更新通话状态
        callService.updateCallStatus(callId, "CANCELLED", 0);
        
        // 构建取消数据
        Map<String, Object> cancelData = new HashMap<>();
        cancelData.put("callId", callId);
        cancelData.put("callerId", callerId);
        cancelData.put("timestamp", System.currentTimeMillis());
        
        // 发送信令给接收方
        WsMessage message = WsMessage.of("CALL_CANCEL", cancelData, callerId);
        
        if (receiverId != null) {
            webSocketHandler.sendMessage(receiverId, message);
        }
    }

    @Override
    public void sendIceCandidate(Long fromId, Long toId, Object candidate) {
        log.debug("发送 ICE 候选：fromId={}, toId={}", fromId, toId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("candidate", candidate);
        data.put("fromId", fromId);
        data.put("timestamp", System.currentTimeMillis());
        
        WsMessage message = WsMessage.of("ICE_CANDIDATE", data, fromId);
        webSocketHandler.sendMessage(toId, message);
    }

    @Override
    public void sendOffer(Long fromId, Long toId, Object offer) {
        log.debug("发送 SDP Offer：fromId={}, toId={}", fromId, toId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("offer", offer);
        data.put("fromId", fromId);
        data.put("timestamp", System.currentTimeMillis());
        
        WsMessage message = WsMessage.of("OFFER", data, fromId);
        webSocketHandler.sendMessage(toId, message);
    }

    @Override
    public void sendAnswer(Long fromId, Long toId, Object answer) {
        log.debug("发送 SDP Answer：fromId={}, toId={}", fromId, toId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("answer", answer);
        data.put("fromId", fromId);
        data.put("timestamp", System.currentTimeMillis());
        
        WsMessage message = WsMessage.of("ANSWER", data, fromId);
        webSocketHandler.sendMessage(toId, message);
    }
}
