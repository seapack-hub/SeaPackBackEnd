package org.seaPack.service.common;

import org.seaPack.dto.ai.AgentContext;
import org.springframework.stereotype.Service;

/**
 * 进度上下文服务（基于 InheritableThreadLocal）
 * <p>在 AI Agent 执行期间，持有当前请求的 AgentContext（SSE 发射器），
 * 供工具类（如 WebScraperUtilTool、FileGeneratorTool）推送实时进度到前端。
 * 子线程自动继承父线程的上下文。</p>
 */
@Service
public class ProgressService {

    private final ThreadLocal<AgentContext> contextHolder = new InheritableThreadLocal<>();

    /**
     * 绑定上下文（在 Agent 线程启动前调用）
     */
    public void setContext(AgentContext context) {
        contextHolder.set(context);
    }

    /**
     * 获取当前线程的上下文
     */
    public AgentContext getContext() {
        return contextHolder.get();
    }

    /**
     * 清除上下文（Agent 执行完成后调用，防止内存泄漏）
     */
    public void clearContext() {
        contextHolder.remove();
    }
}