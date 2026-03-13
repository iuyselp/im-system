package com.im.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.im.dto.SendMessageDTO;
import com.im.entity.Message;
import com.im.result.Result;
import com.im.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器
 */
@Api(tags = "消息管理")
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    @ApiOperation("发送消息")
    public Result<Message> sendMessage(
            @RequestAttribute("userId") Long userId,
            @RequestBody @Validated SendMessageDTO dto) {
        return messageService.sendMessage(userId, dto);
    }

    @GetMapping("/list")
    @ApiOperation("获取消息列表")
    public Result<Page<Message>> getMessageList(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) Long groupId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return messageService.getMessageList(userId, targetId, groupId, pageNum, pageSize);
    }

    @PostMapping("/revoke")
    @ApiOperation("撤回消息")
    public Result<Void> revokeMessage(
            @RequestAttribute("userId") Long userId,
            @RequestParam String msgId) {
        return messageService.revokeMessage(userId, msgId);
    }

    @PostMapping("/delete")
    @ApiOperation("删除消息")
    public Result<Void> deleteMessage(
            @RequestAttribute("userId") Long userId,
            @RequestParam String msgId) {
        return messageService.deleteMessage(userId, msgId);
    }

    @PostMapping("/read")
    @ApiOperation("标记已读")
    public Result<Void> markAsRead(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) Long groupId) {
        return messageService.markAsRead(userId, targetId, groupId);
    }

    @GetMapping("/unread")
    @ApiOperation("获取未读消息数")
    public Result<Integer> getUnreadCount(@RequestAttribute("userId") Long userId) {
        return messageService.getUnreadCount(userId);
    }

    @PostMapping("/forward")
    @ApiOperation("转发消息")
    public Result<Void> forwardMessage(
            @RequestAttribute("userId") Long userId,
            @RequestParam String msgId,
            @RequestParam(required = false) List<Long> targetIds,
            @RequestParam(required = false) List<Long> groupIds) {
        return messageService.forwardMessage(userId, msgId, targetIds, groupIds);
    }
}
