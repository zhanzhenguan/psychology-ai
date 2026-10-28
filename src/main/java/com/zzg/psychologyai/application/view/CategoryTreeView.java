package com.zzg.psychologyai.application.view;

import java.io.Serializable;
import java.util.List;

/**
 * 分类树读模型（对齐目标后端响应字段）
 */
public record CategoryTreeView(
        Long id,
        String categoryName,
        String description,
        Integer sortOrder,
        Integer status,
        String statusText,
        Integer articleCount,
        String createdAt,
        String updatedAt,
        List<CategoryTreeView> children
) implements Serializable {
}
