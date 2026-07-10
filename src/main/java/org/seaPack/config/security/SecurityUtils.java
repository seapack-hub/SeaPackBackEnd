package org.seaPack.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具类
 * <p>提供从 SecurityContext 中获取当前登录用户 ID 等通用方法，
 * 避免在每个 Controller 中重复编写 getCurrentUserId()。</p>
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // 工具类禁止实例化
    }

    /**
     * 获取当前登录用户 ID
     * <p>由 JwtAuthenticationFilter 在请求拦截时写入 SecurityContext。</p>
     * @return 用户 ID，未登录时返回 null
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
}
