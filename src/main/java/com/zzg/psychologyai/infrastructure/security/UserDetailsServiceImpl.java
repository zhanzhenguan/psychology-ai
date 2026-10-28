package com.zzg.psychologyai.infrastructure.security;

import com.zzg.psychologyai.domain.model.User;
import com.zzg.psychologyai.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security 用户加载服务：登录认证时按用户名加载用户
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                // 提示语与密码错误保持一致，避免通过错误信息枚举账号
                .orElseThrow(() -> new UsernameNotFoundException("用户名或密码错误"));
        return new LoginUser(user.getId(), user.getUsername().value(), user.getPassword().value(),
                user.getUserType().getValue(), user.getStatus().getValue());
    }
}
