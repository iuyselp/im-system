package com.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.im.entity.MessageReadStatus;
import com.im.mapper.MessageReadMapper;
import com.im.service.MessageReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息已读服务实现类
 * 
 * @author IM Team
 * @date 2026-03-14
 */
@Service
public class MessageReadServiceImpl implements MessageReadService {

    @Autowired
    private MessageReadMapper messageReadMapper;

    @Override
    @Transactional
    public void markAsRead(Long userId, String messageId) {
        MessageReadStatus readStatus = new MessageReadStatus();
        readStatus.setMessageId(messageId);
        readStatus.setUserId(userId);
        readStatus.setReadAt(LocalDateTime.now());
        messageReadMapper.insert(readStatus);
    }

    @Override
    @Transactional
    public void batchMarkAsRead(Long userId, List<String> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }
        messageReadMapper.batchMarkAsRead(userId, messageIds);
    }

    @Override
    @Transactional
    public void markConversationAsRead(Long userId, String conversationId) {
        // TODO: 实现会话级别的全部已读
        // 需要查询该会话的所有未读消息，然后批量标记
    }

    @Override
    public List<Long> getReadUserIds(String messageId) {
        return messageReadMapper.getReadUserIds(messageId);
    }

    @Override
    public int getUnreadCount(Long userId, String conversationId) {
        return messageReadMapper.getUnreadCount(userId, conversationId);
    }

    @Override
    public boolean isRead(String messageId, Long userId) {
        LambdaQueryWrapper<MessageReadStatus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageReadStatus::getMessageId, messageId)
               .eq(MessageReadStatus::getUserId, userId);
        return messageReadMapper.selectCount(wrapper) > 0;
    }
}
