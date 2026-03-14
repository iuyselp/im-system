package com.im.service;

/**
 * 通话信令服务接口
 * 
 * @author IM Team
 * @date 2026-03-14
 */
public interface CallSignalingService {

    /**
     * 发起通话（发送信令）
     * 
     * @param callerId 呼叫方 ID
     * @param receiverId 接收方 ID
     * @param groupId 群组 ID（群聊时填写）
     * @param callType 通话类型 AUDIO/VIDEO
     * @param callId 通话 ID
     */
    void sendCallInvite(Long callerId, Long receiverId, Long groupId, String callType, String callId);

    /**
     * 接听通话
     * 
     * @param callId 通话 ID
     * @param userId 用户 ID
     */
    void sendCallAnswer(String callId, Long userId);

    /**
     * 拒接通话
     * 
     * @param callId 通话 ID
     * @param userId 用户 ID
     */
    void sendCallReject(String callId, Long userId);

    /**
     * 结束通话
     * 
     * @param callId 通话 ID
     * @param userId 用户 ID
     * @param duration 通话时长
     */
    void sendCallEnd(String callId, Long userId, Integer duration);

    /**
     * 取消通话
     * 
     * @param callId 通话 ID
     * @param callerId 呼叫方 ID
     * @param receiverId 接收方 ID
     */
    void sendCallCancel(String callId, Long callerId, Long receiverId);

    /**
     * 发送 ICE 候选
     * 
     * @param fromId 发送方 ID
     * @param toId 接收方 ID
     * @param candidate ICE 候选数据
     */
    void sendIceCandidate(Long fromId, Long toId, Object candidate);

    /**
     * 发送 SDP Offer
     * 
     * @param fromId 发送方 ID
     * @param toId 接收方 ID
     * @param offer SDP Offer
     */
    void sendOffer(Long fromId, Long toId, Object offer);

    /**
     * 发送 SDP Answer
     * 
     * @param fromId 发送方 ID
     * @param toId 接收方 ID
     * @param answer SDP Answer
     */
    void sendAnswer(Long fromId, Long toId, Object answer);
}
