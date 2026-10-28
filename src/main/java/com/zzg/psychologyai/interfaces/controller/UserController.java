package com.zzg.psychologyai.interfaces.controller;

import com.zzg.psychologyai.application.AuthAppService;
import com.zzg.psychologyai.application.UserAppService;
import com.zzg.psychologyai.application.command.LoginCommand;
import com.zzg.psychologyai.application.dto.LoginResult;
import com.zzg.psychologyai.application.command.RegisterUserCommand;
import com.zzg.psychologyai.application.view.UserView;
import com.zzg.psychologyai.common.result.Result;
import com.zzg.psychologyai.infrastructure.security.config.JwtProperties;
import com.zzg.psychologyai.interfaces.assembler.UserAssembler;
import com.zzg.psychologyai.interfaces.request.LoginRequest;
import com.zzg.psychologyai.interfaces.request.RegisterUserRequest;
import com.zzg.psychologyai.interfaces.response.LoginResponse;
import com.zzg.psychologyai.interfaces.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口：注册 / 登录 / 登出 / 刷新令牌。
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserAppService userAppService;
    private final AuthAppService authAppService;
    private final JwtProperties jwtProperties;


    @PostMapping("/add")
    public Result<UserResponse> add(@Valid @RequestBody RegisterUserRequest request) {
        RegisterUserCommand command = UserAssembler.toCommand(request);
        UserView view = userAppService.add(command);
        return Result.success(UserAssembler.toResponse(view));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginCommand loginCommand = UserAssembler.toCommand(request);
        LoginResult loginResult = authAppService.login(loginCommand);
        UserResponse userResponse = UserAssembler.toResponse(loginResult.getUserInfo());
        return Result.success(LoginResponse.of(loginResult.getToken(), loginResult.getExpireSeconds(), userResponse));
    }

    /**
     * 登出：把当前 token 加入黑名单
     * 路径：POST /api/user/logout，需携带 Authorization: Bearer <token>
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authAppService.logout(extractToken(authorization));
        return Result.success();
    }

    /**
     * 刷新令牌：旧 token 换发新 token，旧 token 同时作废
     * 路径：POST /api/user/refresh，需携带 Authorization: Bearer <旧token>
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestHeader(value = "Authorization", required = false) String authorization) {
        LoginResult loginResult = authAppService.refresh(extractToken(authorization));
        UserResponse userResponse = UserAssembler.toResponse(loginResult.getUserInfo());
        return Result.success(LoginResponse.of(loginResult.getToken(), loginResult.getExpireSeconds(), userResponse));
    }


    /**
     * 从 Authorization 头截掉 Bearer 前缀
     */
    private String extractToken(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(jwtProperties.getPrefix())) {
            return null;
        }
        return authorization.substring(jwtProperties.getPrefix().length()).trim();
    }
}
