package com.zzg.psychologyai.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.KnowledgeCategoryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 知识分类 Mapper
 */
@Mapper
public interface KnowledgeCategoryMapper extends BaseMapper<KnowledgeCategoryPO> {

    /**
     * 统计每个分类下的文章数
     */
    @Select("SELECT category_id AS categoryId, COUNT(*) AS cnt FROM knowledge_article WHERE deleted_flag = 0 GROUP BY category_id")
    List<Map<String, Object>> countArticlesByCategory();


}
