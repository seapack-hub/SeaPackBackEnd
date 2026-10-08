package org.seaPack.config;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 安全配置
 * <p>
 * 注册 JWT 过滤器，在请求到达 Controller 之前完成 token 校验和用户上下文设置。
 * 公开接口（登录、验证码等）无需携带 token，其他接口需携带有效 token。
 * </p>
 */
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * BCrypt 密码编码器（自动加盐，强度 10）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * HTTP 安全过滤链
     * <p>
     * 公开接口放行，其余接口需携带有效 JWT token。
     * </p>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                // 放行错误/异步分发（SSE 走 Servlet 异步，连接生命周期结束时容器会以 ASYNC 分发重新过安全链；
                // JwtAuthenticationFilter 不在 ASYNC 分发执行且认证未持久化，不放行会产生 AccessDenied 噪音并干扰连接收尾）
                .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.ASYNC).permitAll()
                // 公开接口 —— 无需认证
                .requestMatchers(
                        "/auth/login",
                        "/auth/captcha/**",
                        "/auth/rsa/**",
                        "/captcha/**",
                        "/rsa/**",
                        "/hello",
                        "/hello",
                        "/images/**",
                        "/files/**",
                        "/v1/files/download",
                        "/error",
                        // 内部 API 接口（技能调用等）
                        "/ai/skills/execute/**")
                .permitAll()
                // 其余接口 —— 需携带有效 token
                .anyRequest().authenticated())
                .cors(cors -> cors.configurationSource(request -> {
                    // 允许跨域请求中的 Authorization 头
                    var config = new org.springframework.web.cors.CorsConfiguration();
                    config.setAllowedOriginPatterns(java.util.List.of("*"));
                    config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(java.util.List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .csrf(csrf -> csrf.disable())
                // 在 UsernamePasswordAuthenticationFilter 之前执行 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
