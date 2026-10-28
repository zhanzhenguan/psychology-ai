package com.zzg.psychologyai.interfaces.response;

import com.zzg.psychologyai.application.dto.UploadDTO;
import lombok.Builder;
import lombok.Data;

/**
 * @author: zzg
 * @date: 2026年10月18日  23:45
 * @version: 1.0
 */
@Data
@Builder
public class UploadResponse {

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

    public static UploadResponse of(UploadDTO uploadDTO) {
        return UploadResponse.builder()
                .id(uploadDTO.id())
                .userId(uploadDTO.userId())
                .originalFilename(uploadDTO.originalFilename())
                .fileName(uploadDTO.fileName())
                .filePath(uploadDTO.filePath())
                .fileSize(uploadDTO.fileSize())
                .contentType(uploadDTO.contentType())
                .fileSuffix(uploadDTO.fileSuffix())
                .objectType(uploadDTO.objectType())
                .objectId(uploadDTO.objectId())
                .build();
    }
}
