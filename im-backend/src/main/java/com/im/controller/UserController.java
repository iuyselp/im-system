package com.im.controller;

import com.im.entity.User;
import com.im.result.Result;
import com.im.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    @ApiOperation("获取用户信息")
    public Result<User> getUserInfo(@RequestAttribute("userId") Long userId) {
        return userService.getUserInfo(userId);
    }

    @PutMapping("/info")
    @ApiOperation("更新用户信息")
    public Result<User> updateUserInfo(
            @RequestAttribute("userId") Long userId,
            @RequestBody User user) {
        return userService.updateUserInfo(userId, user);
    }

    @PutMapping("/password")
    @ApiOperation("修改密码")
    public Result<Void> changePassword(
            @RequestAttribute("userId") Long userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        return userService.changePassword(userId, oldPassword, newPassword);
    }

    @GetMapping("/contacts")
    @ApiOperation("获取通讯录")
    public Result<Object> getContacts(@RequestAttribute("userId") Long userId) {
        return userService.getContacts(userId);
    }

    @PostMapping("/friend")
    @ApiOperation("添加好友")
    public Result<Void> addFriend(
            @RequestAttribute("userId") Long userId,
            @RequestParam Long friendId,
            @RequestParam(required = false) String message) {
        return userService.addFriend(userId, friendId, message);
    }

    @PostMapping("/friend/handle")
    @ApiOperation("处理好友请求")
    public Result<Void> handleFriendRequest(
            @RequestAttribute("userId") Long userId,
            @RequestParam Long requestId,
            @RequestParam Integer status) {
        return userService.handleFriendRequest(userId, requestId, status);
    }

    @PostMapping("/block")
    @ApiOperation("拉黑用户")
    public Result<Void> blockUser(
            @RequestAttribute("userId") Long userId,
            @RequestParam Long targetId) {
        return userService.blockUser(userId, targetId);
    }
}
