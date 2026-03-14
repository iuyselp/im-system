package com.im.service;

import java.util.List;

/**
 * 消息已读服务接口
 * 
 * @author IM Team
 * @date 2026-03-14
 */
public interface MessageReadService {

    /**
     * 标记消息为已读
     * 
     * @param userId 用户 ID
     * @param messageId 消息 ID
     */
    void markAsRead(Long userId, String messageId);

    /**
     * 批量标记消息为已读
     * 
     * @param userId 用户 ID
     * @param messageIds 消息 ID 列表
     */
    void batchMarkAsRead(Long userId, List<String> messageIds);

    /**
     * 标记会话的所有消息为已读
     * 
     * @param userId 用户 ID
     * @param conversationId 会话 ID（用户 ID 或群 ID）
     */
    void markConversationAsRead(Long userId, String conversationId);

    /**
     * 获取消息的已读用户列表
     * 
     * @param messageId 消息 ID
     * @return 已读用户 ID 列表
     */
    List<Long> getReadUserIds(String messageId);

    /**
     * 获取用户的未读消息数
     * 
     * @param userId 用户 ID
     * @param conversationId 会话 ID（可选）
     * @return 未读消息数
     */
    int getUnreadCount(Long userId, String conversationId);

    /**
     * 检查消息是否已被用户阅读
     * 
     * @param messageId 消息 ID
     * @param userId 用户 ID
     * @return true-已读，false-未读
     */
    boolean isRead(String messageId, Long userId);
}
