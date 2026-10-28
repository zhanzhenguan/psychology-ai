package com.zzg.psychologyai.application.view;

import com.zzg.psychologyai.domain.model.Article;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章读模型（对齐目标后端响应字段）
 */
public record ArticleDetailView(
        String id,
        Long categoryId,
        String categoryName,
        String title,
        String summary,

        String content,
        String coverImage,
        String tags,

        List<String> tagArray,

        Long authorId,
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

}
