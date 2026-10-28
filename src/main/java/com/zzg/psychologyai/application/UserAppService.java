package com.zzg.psychologyai.application;

import cn.hutool.core.util.StrUtil;
import com.zzg.psychologyai.domain.port.DomainEventPublisher;
import com.zzg.psychologyai.application.command.RegisterUserCommand;
import com.zzg.psychologyai.application.view.UserView;
import com.zzg.psychologyai.common.exception.BizException;
import com.zzg.psychologyai.domain.factory.UserFactory;
import com.zzg.psychologyai.domain.model.User;
import com.zzg.psychologyai.domain.repository.UserRepository;
import com.zzg.psychologyai.domain.service.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户命令服务实现（写侧）：业务校验与事务边界在此，
 * 聚合创建走领域工厂，密码加密走领域端口，事件由聚合根行为登记后统一发布。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAppService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DomainEventPublisher domainEventPublisher;



    /**
     * 注册用户：唯一性校验 → 领域工厂创建聚合 → 密码加密 → 持久化 → 聚合根登记注册事件 → 统一发布
     *
     * @param command 注册命令
     * @return 注册成功的用户读模型
     */
    @Transactional(rollbackFor = Exception.class)
    public UserView add(RegisterUserCommand command) {
        // 唯一性校验属业务规则，放应用服务层而非 Controller
        if (userRepository.existsByUsername(command.username())) {
            throw new BizException("用户名已存在");
        }
        if (StrUtil.isNotBlank(command.email()) && userRepository.existsByEmail(command.email())) {
            throw new BizException("邮箱已被注册");
        }
        if (StrUtil.isNotBlank(command.phone()) && userRepository.existsByPhone(command.phone())) {
            throw new BizException("手机号已被注册");
        }

        // 密码必须单向加密存储，禁止明文落库
        String encodedPassword = passwordEncoder.encode(command.password());
        // 聚合创建规则（值对象校验、昵称缺省、默认类型/状态/性别）由领域工厂统一承载
        User user = UserFactory.create(command.username(), encodedPassword, command.email(),
                command.phone(), command.nickname(), command.avatar(), command.gender(),
                command.userType());


        User saved = userRepository.save(user);
        // 持久化成功后聚合根自发登记"注册成功"事件（此时自增 ID 已回填），事务提交后由监听器协作
        saved.markRegistered();
        domainEventPublisher.publishEvents(saved);

        UserView view = UserView.from(saved);
        log.info("用户注册成功, userId={}, username={}", view.id(), view.username());
        return view;
    }



}
