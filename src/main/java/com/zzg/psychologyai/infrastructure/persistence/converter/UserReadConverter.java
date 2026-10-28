package com.zzg.psychologyai.infrastructure.persistence.converter;

import com.zzg.psychologyai.application.view.UserView;
import com.zzg.psychologyai.domain.enums.GenderEnum;
import com.zzg.psychologyai.domain.enums.UserTypeEnum;
import com.zzg.psychologyai.infrastructure.persistence.po.UserPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 用户读侧转换器（CQRS Read 侧）：PO → UserView 扁平投影。
 * <p>
 * 枚举字段必须显式用 getValue() 映射，不能用 MapStruct 默认的 ordinal()
 * （ordinal 是枚举声明顺序，与数据库 value 不一定一致）。
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserReadConverter {

    /**
     * PO → ReadModel，枚举按 value 提取
     */
    @Mapping(target = "gender", expression = "java(po.getGender() == null ? null : po.getGender().getValue())")
    @Mapping(target = "userType", expression = "java(po.getUserType() == null ? null : po.getUserType().getValue())")
    UserView toView(UserPO po);

    /**
     * 兼容旧调用：枚举直接取 value
     */
    default Integer map(GenderEnum gender) {
        return gender == null ? null : gender.getValue();
    }

    default Integer map(UserTypeEnum userType) {
        return userType == null ? null : userType.getValue();
    }
}
