package com.zzg.psychologyai.infrastructure.security;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import com.zzg.psychologyai.infrastructure.security.config.JwtProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * JWT 认证过滤器：从请求头解析令牌并写入 SecurityContext，
 * 无状态设计，不创建 HttpSession
 * <p>
 * 故意不加 @Component：避免被 Spring Boot 自动注册进 Servlet 过滤器链而与安全链重复执行，
 * 实例由 {@code SecurityConfig} 显式构造并加入安全过滤器链
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 已认证则跳过，避免重复解析覆盖上下文
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            resolveToken(request)
                    .flatMap(jwtTokenProvider::parseToken)
                    .ifPresent(jwt -> authenticate(jwt, request));
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 从配置的请求头中提取令牌，自动截掉 Bearer 前缀
     */
    private Optional<String> resolveToken(HttpServletRequest request) {
        String header = request.getHeader(jwtProperties.getHeader());
        if (StrUtil.isBlank(header) || !header.startsWith(jwtProperties.getPrefix())) {
            return Optional.empty();
        }
        String token = header.substring(jwtProperties.getPrefix().length()).trim();
        return StrUtil.isBlank(token) ? Optional.empty() : Optional.of(token);
    }

    /**
     * 将令牌载荷转换为认证信息写入上下文
     */
    private void authenticate(JWT jwt, HttpServletRequest request) {
        LoginUser loginUser = jwtTokenProvider.toLoginUser(jwt);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("JWT 认证通过, userId={}", loginUser.getUserId());
    }


    /**
     *
     *
     * 让 `OncePerRequestFilter` 在异步 dispatch 时也重新解析 token，恢复 SecurityContext。
     * @return
     */
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }
}
