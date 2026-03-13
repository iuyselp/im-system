package com.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.im.entity.UserToken;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Token Mapper
 */
@Mapper
public interface UserTokenMapper extends BaseMapper<UserToken> {
}
