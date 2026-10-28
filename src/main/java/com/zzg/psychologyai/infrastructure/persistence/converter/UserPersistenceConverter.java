package com.zzg.psychologyai.infrastructure.persistence.converter;

import com.zzg.psychologyai.domain.model.User;
import com.zzg.psychologyai.domain.valueobject.UserValueObjectMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.UserPO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 用户持久化对象转换器（基础设施层，MapStruct 编译期生成实现）：
 * 负责领域对象 ↔ 数据对象（UserPO）的转换；
 * 值对象（用户名/邮箱/手机号/密码/昵称）与字符串列的转换复用 {@link UserValueObjectMapper}
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = UserValueObjectMapper.class)
public interface UserPersistenceConverter {

    /**
     * 数据对象转领域对象
     *
     * @param po 持久化对象
     * @return 领域对象
     */
    User toDomain(UserPO po);

    /**
     * 领域对象转数据对象
     *
     * @param user 领域对象
     * @return 持久化对象
     */
    UserPO toPO(User user);
}
