package com.zzg.psychologyai.infrastructure.security.config;

import com.zzg.psychologyai.infrastructure.security.JsonSecurityExceptionHandler;
import com.zzg.psychologyai.infrastructure.security.JwtAuthenticationFilter;
import com.zzg.psychologyai.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Spring Security 集成配置（基础设施层）：
 * 认证（登录 + JWT 过滤器）、授权（请求级 + 方法级）、会话（无状态）
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JsonSecurityExceptionHandler jsonSecurityExceptionHandler;

    private final SecurityProperties securityProperties;

    /**
     * Spring Security 密码编码器：BCrypt 单向散列。
     * 仅用于认证框架（DaoAuthenticationProvider）校验密文；
     * 应用层的写入加密走领域端口 {@link com.zzg.psychologyai.domain.service.PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     *
     * 认证管理器：由 Spring Security 装配（含 UserDetailsServiceImpl + PasswordEncoder）
     * @see com.zzg.psychologyai.infrastructure.security.UserDetailsServiceImpl
     * @param configuration 认证配置器
     * @return 认证管理器
         */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * 安全过滤器链
     *
     * @param http             HttpSecurity 构建器
     * @param jwtTokenProvider 令牌工具
     * @param jwtProperties    令牌配置
     * @return 过滤器链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtTokenProvider jwtTokenProvider,
                                                   JwtProperties jwtProperties) throws Exception {
        // 显式构造而不注册为 Bean，避免被 Boot 自动注册进 Servlet 过滤器链
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider, jwtProperties);

        http
                // 前后端分离的无状态接口，关闭 CSRF、表单登录、HTTP Basic 与默认退出
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                // 会话管理：不创建 HttpSession，认证状态完全由 JWT 承载
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(registry -> {
                    registry.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    // 白名单来自 application.yml 的 security.whitelist，后续新增免登录接口无需改代码
                    List<String> whitelist = securityProperties.getWhitelist();
                    if (!CollectionUtils.isEmpty(whitelist)) {
                        registry.requestMatchers(whitelist.toArray(String[]::new)).permitAll();
                    }
                    registry.anyRequest().authenticated();
                })
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint(jsonSecurityExceptionHandler)
                        .accessDeniedHandler(jsonSecurityExceptionHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
