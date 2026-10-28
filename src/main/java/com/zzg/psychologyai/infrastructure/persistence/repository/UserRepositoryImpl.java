package com.zzg.psychologyai.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzg.psychologyai.domain.model.User;
import com.zzg.psychologyai.domain.repository.UserRepository;
import com.zzg.psychologyai.infrastructure.persistence.converter.UserPersistenceConverter;
import com.zzg.psychologyai.infrastructure.persistence.mapper.UserMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.UserPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户仓储实现（写侧，基础设施层）：领域仓储接口的 MyBatis-Plus 落地，
 * 聚合根 ↔ PO 互转交由 MapStruct 写侧转换器完成。
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;
    private final UserPersistenceConverter userPersistenceConverter;

    @Override
    public Optional<User> findByUsername(String username) {
        UserPO po = userMapper.selectOne(Wrappers.<UserPO>lambdaQuery().eq(UserPO::getUsername, username));
        return Optional.ofNullable(po).map(userPersistenceConverter::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.exists(Wrappers.<UserPO>lambdaQuery().eq(UserPO::getUsername, username));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userMapper.exists(Wrappers.<UserPO>lambdaQuery().eq(UserPO::getEmail, email));
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userMapper.exists(Wrappers.<UserPO>lambdaQuery().eq(UserPO::getPhone, phone));
    }

    @Override
    public User save(User user) {
        UserPO po = userPersistenceConverter.toPO(user);
        userMapper.insert(po);
        // 插入后 MyBatis-Plus 会把自增主键回填到实体，审计字段由 MetaObjectHandler 填充
        return userPersistenceConverter.toDomain(po);
    }
}
