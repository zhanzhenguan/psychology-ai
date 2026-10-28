package com.zzg.psychologyai.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzg.psychologyai.infrastructure.persistence.po.KnowledgeArticlePO;
import com.zzg.psychologyai.infrastructure.persistence.read.KnowledgeArticleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 知识库文章 Mapper：对应 knowledge_article 表（主键为 UUID 字符串），只做数据访问
 */
@Mapper
public interface KnowledgeArticleMapper extends BaseMapper<KnowledgeArticlePO> {

    Page<KnowledgeArticleDO> page(Page<KnowledgeArticleDO> objectPage, @Param(Constants.WRAPPER) QueryWrapper wrapper);

    KnowledgeArticleDO getById(@Param("id") String id);

}
