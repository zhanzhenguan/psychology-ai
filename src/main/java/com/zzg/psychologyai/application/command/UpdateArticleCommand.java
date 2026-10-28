package com.zzg.psychologyai.application.command;

/**
 * 更新文章命令
 */
public record UpdateArticleCommand(
        String id,
        Long categoryId,
        String title,
        String summary,
        String content,
        String coverImage,
        String tags
) {
}
