package com.zzg.psychologyai.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zzg.psychologyai.domain.enums.ArticleStatusEnum;
import com.zzg.psychologyai.infrastructure.persistence.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识文章 PO（对应 knowledge_article 表，主键为 UUID 字符串）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_article")
public class KnowledgeArticlePO extends BaseEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    private Long categoryId;

    private String title;

    private String summary;

    private String content;

    private String coverImage;

    private String tags;

    private Integer status;

    private Integer readCount;
}
