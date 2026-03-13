package com.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.im.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM im_user WHERE username = #{username} AND deleted = 0")
    User selectByUsername(String username);

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM im_user WHERE phone = #{phone} AND deleted = 0")
    User selectByPhone(String phone);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM im_user WHERE email = #{email} AND deleted = 0")
    User selectByEmail(String email);
}
