package com.zzg.psychologyai.application.command;

import java.io.Serializable;

/**
 * 用户注册命令（应用层写输入）：纯数据载体，不携带校验注解与接口契约。
 * 校验由接口层 Request 完成；应用服务只信任已经过接口校验的输入。
 */
public record RegisterUserCommand(String username,
                                  String password,
                                  String email,
                                  String phone,
                                  String nickname,
                                  String avatar,
                                  Integer gender,
                                  Integer userType) implements Serializable {
}
