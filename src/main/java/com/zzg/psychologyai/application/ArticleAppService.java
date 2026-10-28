package com.zzg.psychologyai.application;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.zzg.psychologyai.application.command.CreateArticleCommand;
import com.zzg.psychologyai.application.command.UpdateArticleCommand;
import com.zzg.psychologyai.application.command.UpdateArticleStatusCommand;
import com.zzg.psychologyai.application.view.ArticleView;
import com.zzg.psychologyai.common.enums.ResultCode;
import com.zzg.psychologyai.common.exception.BizException;
import com.zzg.psychologyai.domain.enums.ArticleStatusEnum;
import com.zzg.psychologyai.domain.factory.ArticleFactory;
import com.zzg.psychologyai.domain.model.Article;
import com.zzg.psychologyai.domain.repository.ArticleRepository;
import com.zzg.psychologyai.infrastructure.security.CurrentUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文章写侧应用服务实现
 */
@Service
@RequiredArgsConstructor
public class ArticleAppService {

    private final ArticleRepository articleRepository;

    @Transactional(rollbackFor = Exception.class)
    public ArticleView create(CreateArticleCommand command) {
        Article article = ArticleFactory.create(command.categoryId(), command.title(), command.summary(), command.content(), command.coverImage(), command.tags());
        article.uuid();
        Article saved = articleRepository.save(article);
        return ArticleView.from(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public ArticleView update(UpdateArticleCommand command) {
        Article article = articleRepository.findById(command.id())
                .orElseThrow(() -> new BizException(ResultCode.NOT_FOUND, "文章不存在"));
        article.updateArticle(command.categoryId(), command.title(), command.summary(), command.content(), command.coverImage(), command.tags());
        Article update = articleRepository.update(article);
        return ArticleView.from(update);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        articleRepository.deleteById(id);
    }


    @Transactional(rollbackFor = Exception.class)
    public ArticleView updateStatus(UpdateArticleStatusCommand command) {
        Article article = articleRepository.findById(command.id())
                .orElseThrow(() -> new BizException(ResultCode.NOT_FOUND, "文章不存在"));
        article.updateStatus(ArticleStatusEnum.of(command.status()));
        Article update = articleRepository.update(article);
        return ArticleView.from(update);
    }
}
