package com.zzg.psychologyai.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zzg.psychologyai.infrastructure.persistence.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统文件信息 PO（对应 sys_file_info 表）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file_info")
public class SysFileInfoPO extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String originalFilename;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String contentType;

    private String fileSuffix;

    /** 关联业务类型（如 article_cover, avatar） */
    private String objectType;

    /** 关联业务ID */
    private String objectId;

}
