package org.seaPack.service;

import org.seaPack.dto.AgentContext;
import org.springframework.stereotype.Service;

@Service
public class ProgressService {

    private final ThreadLocal<AgentContext> contextHolder = new InheritableThreadLocal<>();

    /**
     * 设置当前线程的Agent上下文
     * @param context Agent上下文
     */
    public void setContext(AgentContext context) {
        contextHolder.set(context);
    }

    /**
     * 获取当前线程的Agent上下文
     * @return Agent上下文
     */
    public AgentContext getContext() {
        return contextHolder.get();
    }

    /**
     * 清除当前线程的Agent上下文（防止内存泄漏）
     */
    public void clearContext() {
        contextHolder.remove();
    }
}
