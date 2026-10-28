package com.zzg.psychologyai.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzg.psychologyai.domain.enums.ArticleStatusEnum;
import com.zzg.psychologyai.domain.model.Article;
import com.zzg.psychologyai.domain.repository.ArticleRepository;
import com.zzg.psychologyai.infrastructure.persistence.converter.ArticleConverter;
import com.zzg.psychologyai.infrastructure.persistence.mapper.KnowledgeArticleMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.KnowledgeArticlePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 文章仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepository {

    private final KnowledgeArticleMapper articleMapper;

    private final ArticleConverter articleConverter;

    @Override
    public Article save(Article article) {
        KnowledgeArticlePO po = articleConverter.toPO(article);
        article.setId(java.util.UUID.randomUUID().toString().replace("-", ""));
        po.setId(article.getId());
        articleMapper.insert(po);
        return article;
    }

    @Override
    public Article update(Article article) {
        KnowledgeArticlePO po = articleConverter.toPO(article);
        if(article.getId() != null || !article.getId().isBlank()){
            articleMapper.updateById(po);
        }
        return article;
    }


    @Override
    public Optional<Article> findById(String id) {
        KnowledgeArticlePO po = articleMapper.selectById(id);
        return po == null ? Optional.empty() : Optional.of(toDomain(po));
    }

    @Override
    public void deleteById(String id) {
        articleMapper.deleteById(id);
    }

    private KnowledgeArticlePO toPO(Article a) {
        KnowledgeArticlePO po = new KnowledgeArticlePO();
        po.setId(a.getId());
        po.setCategoryId(a.getCategoryId());
        po.setTitle(a.getTitle());
        po.setSummary(a.getSummary());
        po.setContent(a.getContent());
        po.setCoverImage(a.getCoverImage());
        po.setTags(a.getTags());
        po.setStatus(a.getStatus().getValue());
        po.setReadCount(a.getReadCount());
        return po;
    }

    private Article toDomain(KnowledgeArticlePO po) {
        Article a = new Article();
        a.setId(po.getId());
        a.setCategoryId(po.getCategoryId());
        a.setTitle(po.getTitle());
        a.setSummary(po.getSummary());
        a.setContent(po.getContent());
        a.setCoverImage(po.getCoverImage());
        a.setTags(po.getTags());
        a.setStatus(ArticleStatusEnum.of(po.getStatus()));
        a.setReadCount(po.getReadCount());
        return a;
    }
}
