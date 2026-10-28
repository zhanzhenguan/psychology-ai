package com.zzg.psychologyai.application.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzg.psychologyai.application.mapper.ArticleMaps;
import com.zzg.psychologyai.application.view.ArticleDetailView;
import com.zzg.psychologyai.application.view.ArticleView;
import com.zzg.psychologyai.common.enums.ResultCode;
import com.zzg.psychologyai.common.exception.BizException;
import com.zzg.psychologyai.common.result.PageResult;
import com.zzg.psychologyai.infrastructure.persistence.mapper.KnowledgeArticleMapper;
import com.zzg.psychologyai.infrastructure.persistence.read.KnowledgeArticleDO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 文章读侧应用服务实现
 */
@Service
@RequiredArgsConstructor
public class ArticleQueryService {

    private final KnowledgeArticleMapper articleMapper;

    private final ArticleMaps articleMaps;

    public PageResult<ArticleView> page(Long categoryId, String title, Integer status, long currentPage, long size) {
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq("a.deleted_flag","0");
        wrapper.eq(categoryId != null,"a.category_id", categoryId);
        wrapper.eq(status != null, "a.status", status);
        wrapper.like(title != null && !title.isBlank(), "a.title", title);
        wrapper.orderByDesc("a.create_time");
        Page<KnowledgeArticleDO> result = articleMapper.page(new Page<>(currentPage, size), wrapper);
        List<ArticleView> viewList = articleMaps.toViewList(result.getRecords());
        return PageResult.of(viewList,result.getTotal(),result.getSize(),result.getCurrent(),result.getPages());
    }

    public ArticleDetailView detail(String id) {
        KnowledgeArticleDO Do = articleMapper.getById(id);
        if (Do == null) {
            throw new BizException(ResultCode.NOT_FOUND, "文章不存在");
        }
        ArticleDetailView detailView = articleMaps.toDetailView(Do);
        return detailView;
    }



}
