package com.im.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.im.dto.SendMessageDTO;
import com.im.entity.Message;
import com.im.result.Result;

import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService extends IService<Message> {

    /**
     * 发送消息
     */
    Result<Message> sendMessage(Long fromId, SendMessageDTO dto);

    /**
     * 获取消息列表
     */
    Result<Page<Message>> getMessageList(Long userId, Long targetId, Long groupId, Integer pageNum, Integer pageSize);

    /**
     * 撤回消息
     */
    Result<Void> revokeMessage(Long userId, String msgId);

    /**
     * 删除消息
     */
    Result<Void> deleteMessage(Long userId, String msgId);

    /**
     * 标记消息已读
     */
    Result<Void> markAsRead(Long userId, Long targetId, Long groupId);

    /**
     * 获取未读消息数
     */
    Result<Integer> getUnreadCount(Long userId);

    /**
     * 转发消息
     */
    Result<Void> forwardMessage(Long userId, String msgId, List<Long> targetIds, List<Long> groupIds);
}
