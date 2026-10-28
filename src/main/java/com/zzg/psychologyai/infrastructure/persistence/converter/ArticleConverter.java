package com.zzg.psychologyai.infrastructure.persistence.converter;

import com.zzg.psychologyai.domain.enums.ArticleStatusEnum;
import com.zzg.psychologyai.domain.model.Article;
import com.zzg.psychologyai.infrastructure.persistence.po.KnowledgeArticlePO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * @author: zzg
 * @date: 2026年10月28日  21:01
 * @version: 1.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArticleConverter {



    @Mapping(source = "status",target = "status")
    KnowledgeArticlePO toPO(Article article);



    @Mapping(source = "status",target = "status")
    Article toDomain(KnowledgeArticlePO po);



    default ArticleStatusEnum intToStatusEnum(Integer code) {
        return ArticleStatusEnum.of(code);
    }

    default String statusToLabel(Integer code) {
        if (code == null) return null;
        return ArticleStatusEnum.of(code).getLabel();
    }

}
