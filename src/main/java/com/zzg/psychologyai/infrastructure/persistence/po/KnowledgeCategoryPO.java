package com.zzg.psychologyai.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zzg.psychologyai.infrastructure.persistence.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识分类 PO（对应 knowledge_category 表）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_category")
public class KnowledgeCategoryPO extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String categoryName;

    private String description;

    private Integer sortOrder;

    private Integer status;
}
