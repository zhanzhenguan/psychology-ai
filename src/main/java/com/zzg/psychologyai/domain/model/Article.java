package com.zzg.psychologyai.domain.model;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.zzg.psychologyai.domain.common.model.AggregateRoot;
import com.zzg.psychologyai.domain.enums.ArticleStatusEnum;

import java.util.Objects;

/**
 * 知识文章聚合根（对应 knowledge_article 表，主键为 UUID 字符串）
 */
public class Article extends AggregateRoot {

    /** UUID 主键 */
    private String id;

    private Long categoryId;
    private String title;
    private String summary;
    private String content;

    /** 封面图片 URL */
    private String coverImage;

    /** 标签数组（JSON 存储） */
    private String tags;

    private ArticleStatusEnum status;

    /** 浏览次数 */
    private Integer readCount;

    public void publish() {
        this.status = ArticleStatusEnum.PUBLISHED;
    }

    public void offline() {
        this.status = ArticleStatusEnum.OFFLINE;
    }

    public void updateStatus(ArticleStatusEnum status) {
        this.status = status;
    }

    public void incrementReadCount() {
        this.readCount = (this.readCount == null ? 0 : this.readCount) + 1;
    }

    public void uuid(){
        this.id = UUID.fastUUID().toString();
    }


    public void updateArticle(Long categoryId,
                                  String title,
                                  String summary,
                                  String content,
                                  String coverImage,
                                  String tags){

        this.categoryId = categoryId;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.coverImage = coverImage;
//        this.status = ArticleStatusEnum.DRAFT;
        if(StrUtil.isNotBlank(tags)){
            String[] split = tags.split(",");
            this.tags = JSONUtil.toJsonStr(split);
        }
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public ArticleStatusEnum getStatus() { return status; }
    public void setStatus(ArticleStatusEnum status) { this.status = status; }
    public Integer getReadCount() { return readCount; }
    public void setReadCount(Integer readCount) { this.readCount = readCount; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Article article)) return false;
        return Objects.equals(id, article.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
