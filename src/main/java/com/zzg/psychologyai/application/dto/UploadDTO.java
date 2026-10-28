package com.zzg.psychologyai.application.dto;

/**
 * @author: zzg
 * @date: 2026年10月18日  23:46
 * @version: 1.0
 */
public record UploadDTO(
         Long id,

         Long userId,

         String originalFilename,

         String fileName,

         String filePath,

         Long fileSize,

         String contentType,

         String fileSuffix,

        /** 关联业务类型（如 article_cover, avatar） */
         String objectType,

        /** 关联业务ID */
         String objectId
) {
}
