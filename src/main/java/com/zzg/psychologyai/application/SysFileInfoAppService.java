package com.zzg.psychologyai.application;

import com.zzg.psychologyai.application.command.UploadCommand;
import com.zzg.psychologyai.application.dto.UploadDTO;
import com.zzg.psychologyai.application.mapper.UploadMaps;
import com.zzg.psychologyai.common.enums.ResultCode;
import com.zzg.psychologyai.common.exception.BizException;
import com.zzg.psychologyai.infrastructure.persistence.mapper.SysFileInfoMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.SysFileInfoPO;
import com.zzg.psychologyai.infrastructure.security.CurrentUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author: zzg
 * @date: 2026年10月18日  23:30
 * @version: 1.0
 */
@Service
@RequiredArgsConstructor
public class SysFileInfoAppService {


    // 图片保存路径，两种写法：绝对路径 / 项目相对路径
    // private final String savePath = "D:/upload/"; // windows绝对路径
    private final String savePath = System.getProperty("user.dir") + "/upload/";

    private final SysFileInfoMapper sysFileInfoMapper;
    private final UploadMaps uploadMapstruct;


    @Transactional(rollbackFor = Exception.class)
    public UploadDTO upload(UploadCommand uploadCommand) {
        // 上传文件到存储系统
        MultipartFile file = uploadCommand.file();
        String businessType = uploadCommand.businessType();
        String businessId = uploadCommand.businessId();
        String businessField = uploadCommand.businessField();


        String originalFilename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String suffix = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String storedName = UUID.randomUUID().toString().replace("-", "") + suffix;
        String datePath = LocalDate.now().toString();
        String filePath = "/uploads/" + datePath + "/" + storedName;

        // businessField（如 cover）拼到 objectType 后缀，便于按业务字段过滤
        String objectType = businessField == null || businessField.isBlank()
                ? businessType
                : businessType + "_" + businessField;

        SysFileInfoPO po = new SysFileInfoPO();
        po.setUserId(CurrentUserUtils.getCurrentUserId());
        po.setOriginalFilename(originalFilename);
        po.setFileName(storedName);
        po.setFilePath(filePath);
        po.setFileSize(file.getSize());
        po.setContentType(file.getContentType());
        po.setFileSuffix(suffix);
        po.setObjectType(objectType);
        po.setObjectId(businessId);
        sysFileInfoMapper.insert(po);


        // 4. 创建目录，不存在则新建
        File destDir = new File(savePath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        // 5. 保存文件
        File destFile = new File(savePath + storedName);

        try {
            file.transferTo(destFile);

        } catch (IOException e) {
            e.printStackTrace();
            throw new BizException(ResultCode.FAIL, "文件上传失败");
        }

        return uploadMapstruct.toDTO(po);
    }
}
