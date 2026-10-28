package com.zzg.psychologyai.interfaces.assembler;

import com.zzg.psychologyai.application.command.LoginCommand;
import com.zzg.psychologyai.application.command.RegisterUserCommand;
import com.zzg.psychologyai.application.view.UserView;
import com.zzg.psychologyai.interfaces.request.LoginRequest;
import com.zzg.psychologyai.interfaces.request.RegisterUserRequest;
import com.zzg.psychologyai.interfaces.response.UserResponse;


public  final class UserAssembler {



    /**
     * 注册请求 → 注册命令
     *
     * @param request 注册请求体
     * @return 注册命令
     */
    public static RegisterUserCommand toCommand(RegisterUserRequest request){
        return new RegisterUserCommand(request.username(),
                request.password(),
                request.email(),
                request.phone(),
                request.nickname(),
                request.avatar(),
                request.gender(),
                request.userType());
    }

    /**
     * 登录请求 → 登录命令
     *
     * @param request 登录请求体
     * @return 登录命令
     */
    public static LoginCommand toCommand(LoginRequest request){
        return new LoginCommand(request.username(), request.password());
    }

    /**
     * 读模型转响应体
     *
     * @param view 用户读模型
     * @return 用户响应体
     */
    public static UserResponse toResponse(UserView view){

        return  UserResponse.builder()
                .id(view.id())
                .username(view.username())
                .email(view.email())
                .phone(view.phone())
                .nickname(view.nickname())
                .avatar(view.avatar())
                .gender(view.gender())
                .genderDisplayName(switch (view.gender()) {
                    case 1 -> "男";
                    case 2 -> "女";
                    default -> "未知";
                })
                .userType(view.userType())
                .userTypeDisplayName(switch (view.userType()) {
                    case 2 -> "管理员";
                    default -> "普通用户";
                })
                .build();
    }
    
}
