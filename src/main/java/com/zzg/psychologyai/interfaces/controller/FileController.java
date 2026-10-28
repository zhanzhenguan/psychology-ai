package com.zzg.psychologyai.interfaces.controller;

import com.zzg.psychologyai.application.SysFileInfoAppService;
import com.zzg.psychologyai.application.command.UploadCommand;
import com.zzg.psychologyai.application.dto.UploadDTO;
import com.zzg.psychologyai.common.result.Result;
import com.zzg.psychologyai.interfaces.response.UploadResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


/**
 * 文件上传接口（对应 sys_file_info 表）
 * 前端传 businessType/businessId/businessField，落库时映射到 object_type/object_id
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final SysFileInfoAppService sysFileInfoAppService;

    @PostMapping("/upload")
    public Result<UploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("businessType") String businessType,
            @RequestParam("businessId") String businessId,
            @RequestParam(value = "businessField", required = false) String businessField,
            HttpServletRequest request)  {

        UploadDTO uploadDTO = sysFileInfoAppService.upload(new UploadCommand(file, businessType, businessId, businessField));

        return Result.success(UploadResponse.of(uploadDTO));
    }
}
