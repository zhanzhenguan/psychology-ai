package com.zzg.psychologyai.application.mapper;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.zzg.psychologyai.application.view.ArticleDetailView;
import com.zzg.psychologyai.application.view.ArticleView;
import com.zzg.psychologyai.domain.enums.ArticleStatusEnum;
import com.zzg.psychologyai.infrastructure.persistence.read.KnowledgeArticleDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 *
 * 读侧 读数据对象转为视图对象
 *
 * 写侧 命令操作转为领域对象
 *
 * @author: zzg
 * @date: 2026年10月28日  15:14
 * @version: 1.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArticleMaps {


    @Mapping(source = "status", target = "statusText")
    @Mapping(source = "createTime", target = "createdAt")
    @Mapping(source = "updateTime", target = "updatedAt")
    @Mapping(source = "updateTime", target = "publishedAt")
    ArticleView toView(KnowledgeArticleDO Do);


    @Mapping(source = "tags", target = "tagArray")
    ArticleDetailView toDetailView(KnowledgeArticleDO Do);

    List<ArticleView> toViewList(List<KnowledgeArticleDO> doList);



//    default ArticleStatusEnum intToStatus(Integer code) {
//        return ArticleStatusEnum.of(code);
//    }
//
//    default String statusEnumToLabel(ArticleStatusEnum articleStatusEnum) {
//        return articleStatusEnum == null ? null : articleStatusEnum.getLabel();
//    }

    default String statusToText(Integer code) {
        if (code == null) return null;
        return ArticleStatusEnum.of(code).getLabel();
    }


    // 转换方法：json字符串转List<String>
    default List<String> jsonTagToList(String tagsJson) {
        if (StrUtil.isBlank(tagsJson)) {
            return List.of();
        }
        try {
            return JSONUtil.toBean(tagsJson, new TypeReference<List<String>>() {},false);
        } catch (Exception e) {
            return List.of();
        }
    }


    default String listToJsonTag(List<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            return null;
        }
        try {
            return JSONUtil.toJsonStr(tagList);
        } catch (Exception e) {
            return null;
        }
    }


}
