package com.im.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.im.constant.Constants;
import com.im.dto.LoginDTO;
import com.im.dto.RegisterDTO;
import com.im.entity.User;
import com.im.entity.UserToken;
import com.im.exception.BusinessException;
import com.im.mapper.UserMapper;
import com.im.mapper.UserTokenMapper;
import com.im.result.Result;
import com.im.service.LoginResult;
import com.im.service.UserService;
import com.im.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final UserTokenMapper userTokenMapper;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<User> register(RegisterDTO dto) {
        // 检查用户名是否已存在
        User existUser = userMapper.selectByUsername(dto.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 检查手机号
        if (dto.getPhone() != null) {
            existUser = userMapper.selectByPhone(dto.getPhone());
            if (existUser != null) {
                throw new BusinessException("手机号已被注册");
            }
        }

        // 检查邮箱
        if (dto.getEmail() != null) {
            existUser = userMapper.selectByEmail(dto.getEmail());
            if (existUser != null) {
                throw new BusinessException("邮箱已被注册");
            }
        }

        // 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setGender(0);
        user.setStatus(Constants.USER_STATUS_NORMAL);
        user.setOnlineStatus(Constants.ONLINE_STATUS_OFFLINE);
        user.setAvatar("/avatar/default.png");

        baseMapper.insert(user);
        log.info("用户注册成功：{}", user.getUsername());

        return Result.success(user);
    }

    @Override
    public Result<LoginResult> login(LoginDTO dto) {
        // 查询用户（支持用户名/手机号/邮箱）
        User user = null;
        user = userMapper.selectByUsername(dto.getAccount());
        if (user == null) {
            user = userMapper.selectByPhone(dto.getAccount());
        }
        if (user == null) {
            user = userMapper.selectByEmail(dto.getAccount());
        }

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查用户状态
        if (user.getStatus() != Constants.USER_STATUS_NORMAL) {
            throw new BusinessException("账号已被禁用");
        }

        // 验证密码
        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 生成 Token
        String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 保存 Token 到数据库
        UserToken userToken = new UserToken();
        userToken.setUserId(user.getId());
        userToken.setToken(accessToken);
        userToken.setRefreshToken(refreshToken);
        userToken.setDeviceId(dto.getDeviceId());
        userToken.setDeviceName(dto.getDeviceName());
        userToken.setDeviceType(dto.getDeviceType());
        userToken.setExpiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getExpiration() / 1000));
        userToken.setIsValid(1);
        userTokenMapper.insert(userToken);

        // 更新用户最后登录信息
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 缓存用户信息到 Redis
        cacheUserInfo(user);

        // 更新在线状态
        redisTemplate.opsForValue().set(
            Constants.REDIS_KEY_USER_ONLINE + user.getId(),
            dto.getDeviceType(),
            7,
            TimeUnit.DAYS
        );

        LoginResult result = new LoginResult();
        result.setAccessToken(accessToken);
        result.setRefreshToken(refreshToken);
        result.setTokenType("Bearer");
        result.setExpiresIn(jwtUtil.getExpiration() / 1000);
        result.setUser(user);

        log.info("用户登录成功：{}", user.getUsername());
        return Result.success(result);
    }

    @Override
    public Result<Void> logout(Long userId) {
        // 使 Token 失效
        LambdaQueryWrapper<UserToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserToken::getUserId, userId);
        wrapper.eq(UserToken::getIsValid, 1);
        userTokenMapper.delete(wrapper);

        // 清除 Redis 缓存
        redisTemplate.delete(Constants.REDIS_KEY_USER_INFO + userId);
        redisTemplate.delete(Constants.REDIS_KEY_USER_ONLINE + userId);

        log.info("用户登出：{}", userId);
        return Result.success();
    }

    @Override
    public Result<LoginResult> refreshToken(String refreshToken) {
        // 验证刷新 Token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException("刷新 Token 无效");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);

        // 查询数据库中的 Token
        LambdaQueryWrapper<UserToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserToken::getRefreshToken, refreshToken);
        wrapper.eq(UserToken::getIsValid, 1);
        UserToken userToken = userTokenMapper.selectOne(wrapper);

        if (userToken == null || userToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("刷新 Token 已过期");
        }

        // 生成新的 Token
        String newAccessToken = jwtUtil.generateToken(userId, username);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);

        // 更新数据库
        userToken.setToken(newAccessToken);
        userToken.setRefreshToken(newRefreshToken);
        userToken.setExpiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getExpiration() / 1000));
        userTokenMapper.updateById(userToken);

        // 获取用户信息
        User user = baseMapper.selectById(userId);

        LoginResult result = new LoginResult();
        result.setAccessToken(newAccessToken);
        result.setRefreshToken(newRefreshToken);
        result.setTokenType("Bearer");
        result.setExpiresIn(jwtUtil.getExpiration() / 1000);
        result.setUser(user);

        return Result.success(result);
    }

    @Override
    public Result<User> getUserInfo(Long userId) {
        // 先从缓存获取
        User user = getCachedUserInfo(userId);
        if (user == null) {
            user = baseMapper.selectById(userId);
            if (user != null) {
                cacheUserInfo(user);
            }
        }

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 隐藏敏感信息
        user.setPassword(null);
        return Result.success(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<User> updateUserInfo(Long userId, User user) {
        User existUser = baseMapper.selectById(userId);
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新允许的字段
        if (user.getNickname() != null) {
            existUser.setNickname(user.getNickname());
        }
        if (user.getAvatar() != null) {
            existUser.setAvatar(user.getAvatar());
        }
        if (user.getSignature() != null) {
            existUser.setSignature(user.getSignature());
        }
        if (user.getGender() != null) {
            existUser.setGender(user.getGender());
        }
        if (user.getBirthday() != null) {
            existUser.setBirthday(user.getBirthday());
        }

        baseMapper.updateById(existUser);

        // 更新缓存
        cacheUserInfo(existUser);

        existUser.setPassword(null);
        return Result.success(existUser);
    }

    @Override
    public Result<Void> changePassword(Long userId, String oldPassword, String newPassword) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 更新密码
        user.setPassword(BCrypt.hashpw(newPassword));
        baseMapper.updateById(user);

        // 使所有 Token 失效
        LambdaQueryWrapper<UserToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserToken::getUserId, userId);
        userTokenMapper.delete(wrapper);

        log.info("用户修改密码：{}", userId);
        return Result.success();
    }

    @Override
    public Result<Object> getContacts(Long userId) {
        // TODO: 实现通讯录获取
        return Result.success();
    }

    @Override
    public Result<Void> addFriend(Long userId, Long friendId, String message) {
        // TODO: 实现添加好友
        return Result.success();
    }

    @Override
    public Result<Void> handleFriendRequest(Long userId, Long requestId, Integer status) {
        // TODO: 实现处理好友请求
        return Result.success();
    }

    @Override
    public Result<Void> blockUser(Long userId, Long targetId) {
        // TODO: 实现拉黑用户
        return Result.success();
    }

    /**
     * 缓存用户信息
     */
    private void cacheUserInfo(User user) {
        User cacheUser = new User();
        cacheUser.setId(user.getId());
        cacheUser.setUsername(user.getUsername());
        cacheUser.setNickname(user.getNickname());
        cacheUser.setAvatar(user.getAvatar());
        cacheUser.setSignature(user.getSignature());
        cacheUser.setGender(user.getGender());
        cacheUser.setOnlineStatus(user.getOnlineStatus());
        
        redisTemplate.opsForValue().set(
            Constants.REDIS_KEY_USER_INFO + user.getId(),
            cacheUser,
            30,
            TimeUnit.MINUTES
        );
    }

    /**
     * 获取缓存的用户信息
     */
    private User getCachedUserInfo(Long userId) {
        Object obj = redisTemplate.opsForValue().get(Constants.REDIS_KEY_USER_INFO + userId);
        return obj instanceof User ? (User) obj : null;
    }
}
