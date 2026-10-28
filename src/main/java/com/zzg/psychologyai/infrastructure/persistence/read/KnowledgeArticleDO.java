package com.zzg.psychologyai.infrastructure.persistence.read;

import com.zzg.psychologyai.infrastructure.persistence.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: zzg
 * @date: 2026年10月25日  00:25
 * @version: 1.0
 */
@Data
public class KnowledgeArticleDO extends BaseEntity {
    private String id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String summary;
    private String content;
    private String tags;
    private Long authorId;
    private String authorName;
    private Integer readCount;
    private Integer status;
    private Boolean isFavorited;
    private Integer favoriteCount;

}
