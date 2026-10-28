package com.zzg.psychologyai.application.view;

import com.zzg.psychologyai.domain.model.Article;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章读模型（对齐目标后端响应字段）
 */
public record ArticleView(
        String id,
        Long categoryId,
        String categoryName,
        String title,
        String summary,
        String coverImage,
        String tags,
        String authorName,
        Integer readCount,
        Integer status,
        String statusText,
        Boolean isFavorited,
        Integer favoriteCount,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {

    public static ArticleView from(Article article) {
        return new ArticleView(
                article.getId(),
                article.getCategoryId(),
                null,
                article.getTitle(),
                article.getSummary(),
                article.getCoverImage(),
                article.getTags(),
                null,
                article.getReadCount(),
                article.getStatus().getValue(),
                article.getStatus().getLabel(),
                false,
                0,
                null,
                null,
                null
        );
    }
}
