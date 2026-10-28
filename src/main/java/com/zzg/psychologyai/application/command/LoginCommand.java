package com.zzg.psychologyai.application.command;

import java.io.Serializable;

/**
 * 登录命令（应用层写输入）：纯数据载体，不携带校验注解与接口契约。
 */
public record LoginCommand(
        String username,
        String password
) implements Serializable {
}
