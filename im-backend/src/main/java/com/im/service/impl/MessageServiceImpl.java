package com.im.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.im.constant.Constants;
import com.im.dto.SendMessageDTO;
import com.im.entity.Message;
import com.im.exception.BusinessException;
import com.im.mapper.MessageMapper;
import com.im.result.Result;
import com.im.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 消息服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final MessageMapper messageMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final com.im.websocket.WebSocketHandler webSocketHandler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Message> sendMessage(Long fromId, SendMessageDTO dto) {
        // 参数校验
        if (dto.getToId() == null && dto.getGroupId() == null) {
            throw new BusinessException("接收者或群组不能为空");
        }

        // 生成消息 ID
        String msgId = IdUtil.fastSimpleUUID();

        // 创建消息
        Message message = new Message();
        message.setMsgId(msgId);
        message.setFromId(fromId);
        message.setToId(dto.getToId());
        message.setGroupId(dto.getGroupId());
        message.setType(dto.getType());
        message.setContent(dto.getContent());
        message.setExtra(dto.getExtra());
        message.setStatus(Constants.MSG_STATUS_NORMAL);
        message.setIsRead(0);
        message.setReadCount(0);
        message.setDeliveryStatus("sent");

        // 获取表名（分表）
        String tableName = getTableName(fromId);

        // 插入消息（使用动态表名需要自定义 SQL）
        // 这里简化处理，使用默认表
        baseMapper.insert(message);

        // 更新会话
        updateConversation(fromId, dto.getToId(), dto.getGroupId(), message);

        // 通过 WebSocket 推送消息给接收方
        pushMessageToReceiver(message);

        // 如果是群消息，更新已读状态
        if (dto.getGroupId() != null) {
            message.setIsRead(1); // 发送者自己的消息标记为已读
        }

        log.info("发送消息成功：msgId={}, fromId={}, toId={}, groupId={}", 
                msgId, fromId, dto.getToId(), dto.getGroupId());

        return Result.success(message);
    }

    /**
     * 推送消息给接收方
     */
    private void pushMessageToReceiver(Message message) {
        // 构建 WebSocket 消息
        com.im.websocket.WsMessage wsMessage = com.im.websocket.WsMessage.of(
            com.im.websocket.WsMessageType.NEW_MESSAGE.getValue(),
            message,
            message.getFromId()
        );

        // 单聊：发送给接收方
        if (message.getToId() != null) {
            webSocketHandler.sendMessage(message.getToId(), wsMessage);
        }

        // 群聊：发送给所有群成员（排除发送者）
        if (message.getGroupId() != null) {
            // TODO: 获取群成员列表并群发
            log.info("群消息推送：groupId={}, 需要获取群成员列表", message.getGroupId());
        }
    }

    @Override
    public Result<Page<Message>> getMessageList(Long userId, Long targetId, Long groupId, 
                                                 Integer pageNum, Integer pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        String tableName = getTableName(userId);

        IPage<Message> result;
        if (groupId != null) {
            // 群聊消息
            result = messageMapper.selectGroupMessages(page, tableName, groupId);
        } else if (targetId != null) {
            // 单聊消息
            result = messageMapper.selectSingleChatMessages(page, tableName, userId, targetId);
        } else {
            throw new BusinessException("参数错误");
        }

        Page<Message> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(result.getRecords());

        return Result.success(resultPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> revokeMessage(Long userId, String msgId) {
        // 查询消息
        Message message = findMessageByMsgId(msgId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        // 只有发送者可以撤回
        if (!message.getFromId().equals(userId)) {
            throw new BusinessException("只能撤回自己的消息");
        }

        // 检查撤回时间（2 分钟内）
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(message.getCreatedAt(), now).toMinutes();
        if (minutes > 2) {
            throw new BusinessException("超过 2 分钟的消息不能撤回");
        }

        // 更新消息状态
        message.setStatus(Constants.MSG_STATUS_REVOKED);
        baseMapper.updateById(message);

        log.info("撤回消息：msgId={}, userId={}", msgId, userId);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteMessage(Long userId, String msgId) {
        // 查询消息
        Message message = findMessageByMsgId(msgId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        // 只有发送者可以删除（或者实现双向删除）
        if (!message.getFromId().equals(userId) && !message.getToId().equals(userId)) {
            throw new BusinessException("无权限删除该消息");
        }

        // 更新消息状态
        message.setStatus(Constants.MSG_STATUS_DELETED);
        baseMapper.updateById(message);

        log.info("删除消息：msgId={}, userId={}", msgId, userId);
        return Result.success();
    }

    @Override
    public Result<Void> markAsRead(Long userId, Long targetId, Long groupId) {
        // TODO: 标记消息已读
        // 更新未读计数
        String key = Constants.REDIS_KEY_MSG_UNREAD + userId;
        if (targetId != null) {
            redisTemplate.opsForHash().delete(key, targetId.toString());
        }
        return Result.success();
    }

    @Override
    public Result<Integer> getUnreadCount(Long userId) {
        String key = Constants.REDIS_KEY_MSG_UNREAD + userId;
        Object count = redisTemplate.opsForValue().get(key);
        int unreadCount = count instanceof Integer ? (Integer) count : 0;
        return Result.success(unreadCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> forwardMessage(Long userId, String msgId, List<Long> targetIds, List<Long> groupIds) {
        // 查询原消息
        Message originalMessage = findMessageByMsgId(msgId);
        if (originalMessage == null) {
            throw new BusinessException("消息不存在");
        }

        // 转发给好友
        if (targetIds != null) {
            for (Long targetId : targetIds) {
                SendMessageDTO dto = new SendMessageDTO();
                dto.setToId(targetId);
                dto.setType(originalMessage.getType());
                dto.setContent(originalMessage.getContent());
                dto.setExtra(originalMessage.getExtra());
                sendMessage(userId, dto);
            }
        }

        // 转发到群组
        if (groupIds != null) {
            for (Long groupId : groupIds) {
                SendMessageDTO dto = new SendMessageDTO();
                dto.setGroupId(groupId);
                dto.setType(originalMessage.getType());
                dto.setContent(originalMessage.getContent());
                dto.setExtra(originalMessage.getExtra());
                sendMessage(userId, dto);
            }
        }

        log.info("转发消息：msgId={}, targetIds={}, groupIds={}", msgId, targetIds, groupIds);
        return Result.success();
    }

    /**
     * 获取表名（分表路由）
     */
    private String getTableName(Long userId) {
        int suffix = Math.abs(userId.hashCode() % 100);
        return String.format("im_message_%02d", suffix);
    }

    /**
     * 根据消息 ID 查找消息
     */
    private Message findMessageByMsgId(String msgId) {
        // 简化处理，遍历所有分表查询
        for (int i = 0; i < 100; i++) {
            String tableName = String.format("im_message_%02d", i);
            Message message = messageMapper.selectByMsgId(tableName, msgId);
            if (message != null) {
                return message;
            }
        }
        return null;
    }

    /**
     * 更新会话
     */
    private void updateConversation(Long fromId, Long toId, Long groupId, Message message) {
        // TODO: 实现会话表更新
        // 这里简化处理
    }

    @Override
    public Result<Page<Message>> searchMessages(Long userId, String keyword, String conversationId,
                                                 String startTime, String endTime,
                                                 Integer pageNum, Integer pageSize) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.success(new Page<>(pageNum, pageSize));
        }

        Page<Message> page = new Page<>(pageNum, pageSize);
        String tableName = getTableName(userId);

        // 构建查询条件
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索（支持全文搜索的数据库可以使用 MATCH AGAINST）
        wrapper.like(Message::getContent, keyword.trim());
        
        // 会话过滤
        if (conversationId != null && !conversationId.isEmpty()) {
            // 判断是用户 ID 还是群 ID
            try {
                Long convId = Long.parseLong(conversationId);
                wrapper.and(w -> w.eq(Message::getToId, convId)
                                 .or()
                                 .eq(Message::getGroupId, convId)
                                 .or()
                                 .eq(Message::getFromId, convId));
            } catch (NumberFormatException e) {
                // 不是数字，忽略
            }
        } else {
            // 没有指定会话，搜索所有相关消息
            wrapper.and(w -> w.eq(Message::getFromId, userId)
                             .or()
                             .eq(Message::getToId, userId));
        }

        // 时间范围过滤
        if (startTime != null && !startTime.isEmpty()) {
            try {
                LocalDateTime start = LocalDateTime.parse(startTime);
                wrapper.ge(Message::getCreatedAt, start);
            } catch (Exception e) {
                log.warn("开始时间格式错误：{}", startTime);
            }
        }

        if (endTime != null && !endTime.isEmpty()) {
            try {
                LocalDateTime end = LocalDateTime.parse(endTime);
                wrapper.le(Message::getCreatedAt, end);
            } catch (Exception e) {
                log.warn("结束时间格式错误：{}", endTime);
            }
        }

        // 只查询正常状态的消息
        wrapper.eq(Message::getStatus, Constants.MSG_STATUS_NORMAL);

        // 按时间倒序
        wrapper.orderByDesc(Message::getCreatedAt);

        Page<Message> result = this.page(page, wrapper);

        log.info("搜索消息：keyword={}, conversationId={}, resultCount={}", 
                keyword, conversationId, result.getRecords().size());

        return Result.success(result);
    }
}
