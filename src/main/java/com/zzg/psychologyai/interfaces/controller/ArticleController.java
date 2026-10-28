package com.zzg.psychologyai.interfaces.controller;


import com.zzg.psychologyai.application.ArticleAppService;
import com.zzg.psychologyai.application.command.CreateArticleCommand;
import com.zzg.psychologyai.application.command.UpdateArticleCommand;
import com.zzg.psychologyai.application.command.UpdateArticleStatusCommand;
import com.zzg.psychologyai.application.view.ArticleDetailView;
import com.zzg.psychologyai.application.view.ArticleView;
import com.zzg.psychologyai.application.query.ArticleQueryService;
import com.zzg.psychologyai.common.result.PageResult;
import com.zzg.psychologyai.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * 知识文章接口（对应 knowledge_article 表，主键为 UUID 字符串）
 */
@RestController
@RequestMapping("/knowledge/article")
@RequiredArgsConstructor
public class ArticleController {

    //写侧
    private final ArticleAppService articleAppService;
    //读侧 绕过领域
    private final ArticleQueryService articleQueryService;

    @GetMapping("/page")
    public Result<PageResult<ArticleView>> page(@RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) String title,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(defaultValue = "1") long currentPage,
                                                @RequestParam(defaultValue = "10") long size) {
        PageResult<ArticleView> result = articleQueryService.page(categoryId, title, status, currentPage, size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<ArticleDetailView> detail(@PathVariable(name = "id") String id) {
        ArticleDetailView detail = articleQueryService.detail(id);
        return Result.success(detail);
    }

    @PostMapping
    public Result<ArticleView> create(@Valid @RequestBody CreateArticleRequest request) {
        return Result.success(articleAppService.create(
                new CreateArticleCommand(request.categoryId(), request.title(), request.summary(),
                        request.content(), request.coverImage(), request.tags())));
    }

    @PutMapping("/{id}")
    public Result<ArticleView> update(@PathVariable String id, @RequestBody UpdateArticleRequest request) {
        return Result.success(articleAppService.update(
                new UpdateArticleCommand(id, request.categoryId(), request.title(), request.summary(),
                        request.content(), request.coverImage(), request.tags())));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        articleAppService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<ArticleView> updateStatus(@PathVariable(name = "id") String id, @RequestBody UpdateStatusRequest request) {
        ArticleView articleView = articleAppService.updateStatus(new UpdateArticleStatusCommand(id, request.status()));
        return Result.success(articleView);
    }

    public record CreateArticleRequest(
            Long categoryId,
            @NotBlank(message = "标题不能为空") String title,
            String summary,
            String content,
            String coverImage,
            String tags
    ) {}

    public record UpdateArticleRequest(
            Long categoryId,
            String title,
            String summary,
            String content,
            String coverImage,
            String tags
    ) {}

    public record UpdateStatusRequest(Integer status) {}
}
