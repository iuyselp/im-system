package com.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.im.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消息 Mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 查询单聊消息列表
     */
    @Select("SELECT * FROM ${tableName} WHERE ((from_id = #{userId1} AND to_id = #{userId2}) " +
            "OR (from_id = #{userId2} AND to_id = #{userId1})) " +
            "AND status = 1 AND deleted = 0 ORDER BY created_at DESC")
    IPage<Message> selectSingleChatMessages(Page<?> page, @Param("tableName") String tableName,
                                             @Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 查询群聊消息列表
     */
    @Select("SELECT * FROM ${tableName} WHERE group_id = #{groupId} AND status = 1 AND deleted = 0 " +
            "ORDER BY created_at DESC")
    IPage<Message> selectGroupMessages(Page<?> page, @Param("tableName") String tableName,
                                        @Param("groupId") Long groupId);

    /**
     * 根据消息 ID 查询
     */
    @Select("SELECT * FROM ${tableName} WHERE msg_id = #{msgId}")
    Message selectByMsgId(@Param("tableName") String tableName, @Param("msgId") String msgId);
}
