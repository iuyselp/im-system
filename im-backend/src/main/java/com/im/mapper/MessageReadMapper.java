package com.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.im.entity.MessageReadStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息已读状态 Mapper
 * 
 * @author IM Team
 * @date 2026-03-14
 */
@Mapper
public interface MessageReadMapper extends BaseMapper<MessageReadStatus> {

    /**
     * 批量标记已读
     */
    int batchMarkAsRead(@Param("userId") Long userId, @Param("messageIds") List<String> messageIds);

    /**
     * 获取消息的已读用户列表
     */
    List<Long> getReadUserIds(@Param("messageId") String messageId);

    /**
     * 获取用户的未读消息数
     */
    int getUnreadCount(@Param("userId") Long userId, @Param("conversationId") String conversationId);
}
