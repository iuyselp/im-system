package com.im.controller;

import com.im.dto.LoginDTO;
import com.im.dto.RegisterDTO;
import com.im.result.Result;
import com.im.service.LoginResult;
import com.im.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<?> register(@RequestBody @Validated RegisterDTO dto) {
        return userService.register(dto);
    }

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<LoginResult> login(@RequestBody @Validated LoginDTO dto) {
        return userService.login(dto);
    }

    @PostMapping("/logout")
    @ApiOperation("用户登出")
    public Result<Void> logout(@RequestAttribute("userId") Long userId) {
        return userService.logout(userId);
    }

    @PostMapping("/refresh")
    @ApiOperation("刷新 Token")
    public Result<LoginResult> refreshToken(@RequestParam String refreshToken) {
        return userService.refreshToken(refreshToken);
    }
}
