package com.zzg.psychologyai.application.mapper;

import com.zzg.psychologyai.application.view.UserView;
import com.zzg.psychologyai.infrastructure.persistence.po.UserPO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author: zzg
 * @date: 2026年10月16日  21:15
 * @version: 1.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMaps {


    UserView toView(UserPO userPO);




}
