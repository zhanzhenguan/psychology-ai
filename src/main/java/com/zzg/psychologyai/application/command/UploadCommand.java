package com.zzg.psychologyai.application.command;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author: zzg
 * @date: 2026年10月18日  23:38
 * @version: 1.0
 */
public record UploadCommand(
        MultipartFile file,
        String businessType,
        String businessId,
        String businessField


) {
}
