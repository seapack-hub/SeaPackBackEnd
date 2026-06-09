package org.seaPack.service.common;

import org.seaPack.dto.ai.AgentContext;
import org.springframework.stereotype.Service;

@Service
public class ProgressService {

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