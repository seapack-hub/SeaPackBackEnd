package org.seaPack.service.ai;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.mapper.ai.ExecutionSessionMapper;
import org.seaPack.model.ai.ExecutionSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Agent 测试会话服务
 * <p>提供测试会话历史的分页查询、详情查看和逻辑删除功能。</p>
 */
@Service
public class AgentTestSessionService {

    @Autowired
    private ExecutionSessionMapper executionSessionMapper;

    /**
     * 分页查询 Agent 的测试会话列表
     */
    public PageInfo<ExecutionSession> getTestSessions(Long agentId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ExecutionSession> list = executionSessionMapper.selectList("agent", agentId, null, null, null);
        return new PageInfo<>(list);
    }

    /**
     * 查询测试会话详情
     */
    public ExecutionSession getTestSessionDetail(Long agentId, Long sessionId) {
        return executionSessionMapper.selectById(sessionId);
    }

    /**
     * 删除测试会话（逻辑删除）
     */
    @Transactional
    public int deleteTestSession(Long agentId, Long sessionId) {
        return executionSessionMapper.logicalDelete(sessionId);
    }
}
