package com.zzg.psychologyai.application.command;

/**
 * 更新文章状态命令
 */
public record UpdateArticleStatusCommand(
        String id,
        Integer status
) {
}
