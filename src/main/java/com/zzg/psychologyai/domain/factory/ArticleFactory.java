package com.zzg.psychologyai.domain.factory;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.zzg.psychologyai.domain.enums.ArticleStatusEnum;
import com.zzg.psychologyai.domain.model.Article;

/**
 * @author: zzg
 * @date: 2026年10月28日  21:12
 * @version: 1.0
 */
public final class ArticleFactory {

    private ArticleFactory(){

    }


    public static Article create(Long categoryId,
                                 String title,
                                 String summary,
                                 String content,
                                 String coverImage,
                                 String tags){

        Article article = new Article();
        article.setCategoryId(categoryId);
        article.setTitle(title);
        article.setSummary(summary);
        article.setContent(content);
        article.setCoverImage(coverImage);
        article.setStatus(ArticleStatusEnum.DRAFT);
        article.setReadCount(0);
        if(StrUtil.isNotBlank(tags)){
            String[] split = tags.split(",");
            article.setTags(JSONUtil.toJsonStr(split));
        }

        return article;
    }


}
