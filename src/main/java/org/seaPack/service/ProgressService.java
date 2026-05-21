package org.seaPack.service;

import org.seaPack.dto.AgentContext;
import org.springframework.stereotype.Service;

@Service
public class ProgressService {

    // 使用 ThreadLocal 确保线程安全，每个请求都有独立的 Context
    private final ThreadLocal<AgentContext> contextHolder = new InheritableThreadLocal<>();

    public void setContext(AgentContext context) {
        contextHolder.set(context);
    }

    public AgentContext getContext() {
        return contextHolder.get();
    }

    public void clearContext() {
        contextHolder.remove();
    }
}
