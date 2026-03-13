package com.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.im.entity.Group;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 群组 Mapper
 */
@Mapper
public interface GroupMapper extends BaseMapper<Group> {

    /**
     * 查询用户加入的群组列表
     */
    @Select("SELECT g.* FROM im_group g " +
            "INNER JOIN im_group_member gm ON g.id = gm.group_id " +
            "WHERE gm.user_id = #{userId} AND g.deleted = 0 ORDER BY g.created_at DESC")
    List<Group> selectUserGroups(Long userId);
}
