package com.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.im.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 群成员 Mapper
 */
@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {

    /**
     * 查询群成员列表
     */
    @Select("SELECT * FROM im_group_member WHERE group_id = #{groupId} AND deleted = 0")
    List<GroupMember> selectByGroupId(@Param("groupId") Long groupId);

    /**
     * 查询用户在群组中的信息
     */
    @Select("SELECT * FROM im_group_member WHERE group_id = #{groupId} AND user_id = #{userId} AND deleted = 0")
    GroupMember selectByGroupAndUser(@Param("groupId") Long groupId, @Param("userId") Long userId);

    /**
     * 查询群成员数量
     */
    @Select("SELECT COUNT(*) FROM im_group_member WHERE group_id = #{groupId} AND deleted = 0")
    int countByGroupId(@Param("groupId") Long groupId);

    /**
     * 查询群主 ID
     */
    @Select("SELECT g.owner_id FROM im_group g WHERE g.id = #{groupId}")
    Long selectOwnerId(@Param("groupId") Long groupId);
}
