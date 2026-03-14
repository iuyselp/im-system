package com.im.service;

import com.im.entity.CallRecord;

import java.util.List;

/**
 * 通话服务接口
 * 
 * @author IM Team
 * @date 2026-03-14
 */
public interface CallService {

    /**
     * 创建通话记录
     * 
     * @param callerId 呼叫方 ID
     * @param receiverId 接收方 ID
     * @param groupId 群组 ID（群聊时填写）
     * @param type 通话类型 AUDIO/VIDEO
     * @param callId 通话 ID
     * @return 通话记录
     */
    CallRecord createCall(Long callerId, Long receiverId, Long groupId, String type, String callId);

    /**
     * 更新通话状态
     * 
     * @param callId 通话 ID
     * @param status 通话状态
     * @param duration 通话时长（秒）
     */
    void updateCallStatus(String callId, String status, Integer duration);

    /**
     * 获取通话历史
     * 
     * @param userId 用户 ID
     * @param page 页码
     * @param size 每页数量
     * @return 通话记录列表
     */
    List<CallRecord> getCallHistory(Long userId, int page, int size);

    /**
     * 获取未接来电数
     * 
     * @param userId 用户 ID
     * @return 未接来电数
     */
    int getMissedCallCount(Long userId);

    /**
     * 获取通话详情
     * 
     * @param callId 通话 ID
     * @return 通话记录
     */
    CallRecord getCallDetail(String callId);

    /**
     * 获取 WebRTC 配置
     * 
     * @return ICE 服务器配置
     */
    Object getWebRTCConfig();
}
