package com.zzg.psychologyai.application.mapper;

import com.zzg.psychologyai.application.dto.UploadDTO;
import com.zzg.psychologyai.infrastructure.persistence.po.SysFileInfoPO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author: zzg
 * @date: 2026年10月18日  23:48
 * @version: 1.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UploadMaps {


    UploadDTO toDTO(SysFileInfoPO po);


    SysFileInfoPO toPO(UploadDTO dto);

}
