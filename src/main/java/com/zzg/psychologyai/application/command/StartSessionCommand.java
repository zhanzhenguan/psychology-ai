package com.zzg.psychologyai.application.command;

/**
 * 开始会话命令
 */
public record StartSessionCommand(
        String initialMessage,
        String title
) {
}
