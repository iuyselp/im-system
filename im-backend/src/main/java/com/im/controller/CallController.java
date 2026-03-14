package com.im.controller;

import com.im.entity.CallRecord;
import com.im.result.Result;
import com.im.service.CallService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 音视频通话控制器
 * 
 * @author IM Team
 * @date 2026-03-14
 */
@Api(tags = "音视频通话管理")
@RestController
@RequestMapping("/api/call")
public class CallController {

    @Autowired
    private CallService callService;

    @PostMapping("/start")
    @ApiOperation("发起通话")
    public Result<CallRecord> startCall(@RequestParam Long callerId,
                                        @RequestParam(required = false) Long receiverId,
                                        @RequestParam(required = false) Long groupId,
                                        @RequestParam String type,
                                        @RequestParam String callId) {
        CallRecord callRecord = callService.createCall(callerId, receiverId, groupId, type, callId);
        return Result.success(callRecord);
    }

    @PostMapping("/answer")
    @ApiOperation("接听通话")
    public Result<Void> answerCall(@RequestParam String callId) {
        callService.updateCallStatus(callId, "ANSWERED", 0);
        return Result.success();
    }

    @PostMapping("/reject")
    @ApiOperation("拒接通话")
    public Result<Void> rejectCall(@RequestParam String callId) {
        callService.updateCallStatus(callId, "REJECTED", 0);
        return Result.success();
    }

    @PostMapping("/end")
    @ApiOperation("结束通话")
    public Result<Void> endCall(@RequestParam String callId,
                                @RequestParam Integer duration) {
        callService.updateCallStatus(callId, "COMPLETED", duration);
        return Result.success();
    }

    @GetMapping("/history")
    @ApiOperation("获取通话历史")
    public Result<List<CallRecord>> getCallHistory(@RequestParam Long userId,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        List<CallRecord> history = callService.getCallHistory(userId, page, size);
        return Result.success(history);
    }

    @GetMapping("/missed/count")
    @ApiOperation("获取未接来电数")
    public Result<Integer> getMissedCallCount(@RequestParam Long userId) {
        int count = callService.getMissedCallCount(userId);
        return Result.success(count);
    }

    @GetMapping("/detail")
    @ApiOperation("获取通话详情")
    public Result<CallRecord> getCallDetail(@RequestParam String callId) {
        CallRecord callRecord = callService.getCallDetail(callId);
        return Result.success(callRecord);
    }

    @GetMapping("/webrtc/config")
    @ApiOperation("获取 WebRTC 配置")
    public Result<Map<String, Object>> getWebRTCConfig() {
        Map<String, Object> config = callService.getWebRTCConfig();
        return Result.success(config);
    }
}
