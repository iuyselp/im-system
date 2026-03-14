package com.im.service.impl;

import com.im.entity.CallRecord;
import com.im.mapper.CallRecordMapper;
import com.im.service.CallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 通话服务实现类
 * 
 * @author IM Team
 * @date 2026-03-14
 */
@Service
public class CallServiceImpl implements CallService {

    @Autowired
    private CallRecordMapper callRecordMapper;

    @Value("${webrtc.stun-servers:stun:stun.l.google.com:19302}")
    private String stunServers;

    @Value("${webrtc.turn-servers:}")
    private String turnServers;

    @Override
    @Transactional
    public CallRecord createCall(Long callerId, Long receiverId, Long groupId, String type, String callId) {
        CallRecord callRecord = new CallRecord();
        callRecord.setCallId(callId);
        callRecord.setCallerId(callerId);
        callRecord.setReceiverId(receiverId);
        callRecord.setGroupId(groupId);
        callRecord.setType(type);
        callRecord.setDuration(0);
        callRecord.setStatus("INITIATED");
        callRecord.setCreatedAt(LocalDateTime.now());
        callRecordMapper.insert(callRecord);
        return callRecord;
    }

    @Override
    @Transactional
    public void updateCallStatus(String callId, String status, Integer duration) {
        CallRecord callRecord = callRecordMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CallRecord>()
                .eq(CallRecord::getCallId, callId)
        );
        
        if (callRecord != null) {
            callRecord.setStatus(status);
            if (duration != null) {
                callRecord.setDuration(duration);
            }
            callRecordMapper.updateById(callRecord);
        }
    }

    @Override
    public List<CallRecord> getCallHistory(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return callRecordMapper.getCallHistory(userId, offset, size);
    }

    @Override
    public int getMissedCallCount(Long userId) {
        return callRecordMapper.getMissedCallCount(userId);
    }

    @Override
    public CallRecord getCallDetail(String callId) {
        return callRecordMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CallRecord>()
                .eq(CallRecord::getCallId, callId)
        );
    }

    @Override
    public Map<String, Object> getWebRTCConfig() {
        Map<String, Object> config = new HashMap<>();
        
        List<Map<String, String>> iceServers = new ArrayList<>();
        
        // 添加 STUN 服务器
        String[] stuns = stunServers.split(",");
        for (String stun : stuns) {
            Map<String, String> server = new HashMap<>();
            server.put("urls", stun.trim());
            iceServers.add(server);
        }
        
        // 添加 TURN 服务器（如果配置了）
        if (turnServers != null && !turnServers.isEmpty()) {
            String[] turns = turnServers.split(",");
            for (String turn : turns) {
                Map<String, String> server = new HashMap<>();
                server.put("urls", turn.trim());
                // TODO: 从配置中读取 TURN 用户名和密码
                server.put("username", "turn_user");
                server.put("credential", "turn_password");
                iceServers.add(server);
            }
        }
        
        config.put("iceServers", iceServers);
        return config;
    }
}
