package com.zzg.psychologyai.domain.repository;

import com.zzg.psychologyai.domain.model.Article;

import java.util.Optional;

/**
 * 文章仓储端口
 */
public interface ArticleRepository {

    Article save(Article article);

    Article update(Article article);

    Optional<Article> findById(String id);

    void deleteById(String id);



}
