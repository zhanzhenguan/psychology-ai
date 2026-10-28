package com.zzg.psychologyai.domain.repository;

import com.zzg.psychologyai.domain.model.User;

import java.util.Optional;

/**
 * 用户仓储接口（CQRS 写侧端口，领域层定义，基础设施层实现）：
 * 只承载写用例（注册、登录发事件）需要的聚合根加载与持久化能力。
 * <p>
 * 查询用例一律走 {@code UserQueryService} 读侧端口，不经过本接口。
 */
public interface UserRepository {

    /**
     * 按用户名加载用户聚合根（登录发事件场景使用）
     *
     * @param username 用户名
     * @return 用户聚合根，不存在时为空
     */
    Optional<User> findByUsername(String username);


    /**
     * 判断用户名是否已存在
     *
     * @param username 用户名
     * @return true 表示已存在
     */
    boolean existsByUsername(String username);

    /**
     * 判断邮箱是否已存在
     *
     * @param email 邮箱
     * @return true 表示已存在
     */
    boolean existsByEmail(String email);

    /**
     * 判断手机号是否已存在
     *
     * @param phone 手机号
     * @return true 表示已存在
     */
    boolean existsByPhone(String phone);

    /**
     * 新增用户聚合根
     *
     * @param user 用户聚合根
     * @return 保存后的聚合根（含自增主键）
     */
    User save(User user);



}
