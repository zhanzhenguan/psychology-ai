package com.zzg.psychologyai.application.command;

/**
 * 创建文章命令
 */
public record CreateArticleCommand(
        Long categoryId,
        String title,
        String summary,
        String content,
        String coverImage,
        String tags
) {
}
