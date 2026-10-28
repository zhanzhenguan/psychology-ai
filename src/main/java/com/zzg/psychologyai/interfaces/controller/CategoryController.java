package com.zzg.psychologyai.interfaces.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzg.psychologyai.common.result.Result;
import com.zzg.psychologyai.infrastructure.persistence.mapper.KnowledgeCategoryMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.KnowledgeCategoryPO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识分类接口
 */
@RestController
@RequestMapping("/knowledge/category")
@RequiredArgsConstructor
public class CategoryController {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final KnowledgeCategoryMapper categoryMapper;

    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> tree() {
        List<KnowledgeCategoryPO> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<KnowledgeCategoryPO>()
                        .eq(KnowledgeCategoryPO::getStatus, 1)
                        .orderByAsc(KnowledgeCategoryPO::getSortOrder)
        );

        Map<Long, Long> countMap = new HashMap<>();
        for (Map<String, Object> row : categoryMapper.countArticlesByCategory()) {
            Long categoryId = ((Number) row.get("categoryId")).longValue();
            Long cnt = ((Number) row.get("cnt")).longValue();
            countMap.put(categoryId, cnt);
        }

        List<Map<String, Object>> records = categories.stream().map(po -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", po.getId());
            item.put("categoryName", po.getCategoryName());
            item.put("description", po.getDescription());
            item.put("sortOrder", po.getSortOrder());
            item.put("status", po.getStatus());
            item.put("statusText", po.getStatus() != null && po.getStatus() == 1 ? "启用" : "禁用");
            item.put("articleCount", countMap.getOrDefault(po.getId(), 0L));
            item.put("createdAt", po.getCreateTime() == null ? null : po.getCreateTime().format(DT));
            item.put("updatedAt", po.getUpdateTime() == null ? null : po.getUpdateTime().format(DT));
            return item;
        }).toList();

        return Result.success(records);
    }
}
