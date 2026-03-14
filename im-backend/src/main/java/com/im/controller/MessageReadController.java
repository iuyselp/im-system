package com.im.controller;

import com.im.result.Result;
import com.im.service.MessageReadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消息已读控制器
 * 
 * @author IM Team
 * @date 2026-03-14
 */
@Api(tags = "消息已读管理")
@RestController
@RequestMapping("/api/message/read")
public class MessageReadController {

    @Autowired
    private MessageReadService messageReadService;

    @PostMapping("/mark")
    @ApiOperation("标记消息为已读")
    public Result<Void> markAsRead(@RequestParam Long userId, 
                                   @RequestParam String messageId) {
        messageReadService.markAsRead(userId, messageId);
        return Result.success();
    }

    @PostMapping("/mark/batch")
    @ApiOperation("批量标记消息为已读")
    public Result<Void> batchMarkAsRead(@RequestParam Long userId,
                                        @RequestBody List<String> messageIds) {
        messageReadService.batchMarkAsRead(userId, messageIds);
        return Result.success();
    }

    @PostMapping("/mark/conversation")
    @ApiOperation("标记会话为已读")
    public Result<Void> markConversationAsRead(@RequestParam Long userId,
                                               @RequestParam String conversationId) {
        messageReadService.markConversationAsRead(userId, conversationId);
        return Result.success();
    }

    @GetMapping("/readers")
    @ApiOperation("获取消息的已读用户列表")
    public Result<List<Long>> getReadUserIds(@RequestParam String messageId) {
        List<Long> readers = messageReadService.getReadUserIds(messageId);
        return Result.success(readers);
    }

    @GetMapping("/unread/count")
    @ApiOperation("获取未读消息数")
    public Result<Integer> getUnreadCount(@RequestParam Long userId,
                                          @RequestParam(required = false) String conversationId) {
        int count = messageReadService.getUnreadCount(userId, conversationId);
        return Result.success(count);
    }

    @GetMapping("/is-read")
    @ApiOperation("检查消息是否已读")
    public Result<Boolean> isRead(@RequestParam String messageId,
                                  @RequestParam Long userId) {
        boolean isRead = messageReadService.isRead(messageId, userId);
        return Result.success(isRead);
    }
}
