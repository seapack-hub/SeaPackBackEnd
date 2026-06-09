package org.seaPack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 安全配置
 * <p>配置密码编码器（BCrypt）和 HTTP 安全过滤链。
 * 当前放行所有请求并关闭 CSRF，方便前后端分离开发调试。</p>
 */
@Configuration
public class SecurityConfig {

    /**
     * BCrypt 密码编码器（自动加盐，强度 10）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * HTTP 安全过滤链
     * <p>放行所有请求，关闭 CSRF 防护。</p>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf().disable();
        return http.build();
    }
}
