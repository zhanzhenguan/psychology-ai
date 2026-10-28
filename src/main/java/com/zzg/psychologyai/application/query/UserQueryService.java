package com.zzg.psychologyai.application.query;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzg.psychologyai.application.mapper.UserMaps;
import com.zzg.psychologyai.application.view.UserView;
import com.zzg.psychologyai.common.enums.ResultCode;
import com.zzg.psychologyai.common.exception.BizException;
import com.zzg.psychologyai.infrastructure.persistence.converter.UserReadConverter;
import com.zzg.psychologyai.infrastructure.persistence.mapper.UserMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.UserPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户查询服务实现（CQRS 读侧，基础设施层）：
 * 直接走 MyBatis 查 PO，再由 {@link UserReadConverter} 投影为读模型；
 * 不加载聚合根、不触发领域事件，为读场景优化。
 */
@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserMapper userMapper;
    private final UserMaps userMapstruct;

    /**
     * 按用户名查询读模型（供登录后回显、登录前预校验等读场景）
     *
     * @param username 用户名
     * @return 用户读模型，不存在时返回空
     */
    public Optional<UserView> findByUsername(String username) {
        UserPO po = userMapper.selectOne(Wrappers.<UserPO>lambdaQuery().eq(UserPO::getUsername, username));
        return Optional.ofNullable(po).map(userMapstruct::toView);
    }

    /**
     * 按用户名查询读模型，不存在时抛业务异常
     *
     * @param username 用户名
     * @return 用户读模型
     */
    public UserView requireByUsername(String username) {
        return findByUsername(username)
                .orElseThrow(() -> new BizException(ResultCode.NOT_FOUND, "用户不存在"));
    }


}
