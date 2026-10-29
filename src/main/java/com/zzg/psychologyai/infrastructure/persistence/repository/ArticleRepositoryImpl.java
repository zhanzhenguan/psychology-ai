package com.zzg.psychologyai.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
        return po == null ? Optional.empty() : Optional.of(articleConverter.toDomain(po));
    }

    @Override
    public void deleteById(String id) {
        articleMapper.deleteById(id);
    }
}
