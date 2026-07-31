package org.seaPack.service.ai;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.mapper.ai.ExecutionSessionMapper;
import org.seaPack.mapper.ai.SceneOrchestrationMapper;
import org.seaPack.model.ai.ExecutionSession;
import org.seaPack.model.ai.SceneOrchestration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Agent 测试会话服务
 * <p>提供测试会话历史的分页查询、详情查看和逻辑删除功能。</p>
 */
@Service
public class AgentTestSessionService {

    @Autowired
    private ExecutionSessionMapper executionSessionMapper;

    @Autowired
    private SceneOrchestrationMapper orchestrationMapper;

    /**
     * 分页查询 Agent 的测试会话列表
     */
    public PageInfo<ExecutionSession> getTestSessions(Long agentId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ExecutionSession> list = executionSessionMapper.selectList("agent", agentId, null, null);
        return new PageInfo<>(list);
    }

    /**
     * 查询测试会话详情
     */
    public ExecutionSession getTestSessionDetail(Long agentId, Long sessionId) {
        return executionSessionMapper.selectById(sessionId);
    }

    /**
     * 分页查询编排的执行会话列表
     * <p>优先按编排ID精确匹配；查不到时尝试作为场景ID查询该场景下所有编排的会话；
     * 仍无则兜底查询全部编排会话（含动态编排 bizId=0）。</p>
     */
    public PageInfo<ExecutionSession> getOrchestrationSessions(Long orchestrationIdOrSceneId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ExecutionSession> list = executionSessionMapper.selectList("orchestration", orchestrationIdOrSceneId, null, null);
        if (list == null || list.isEmpty()) {
            PageHelper.clearPage();
            // 尝试作为场景ID：查询该场景下所有编排的会话
            List<SceneOrchestration> orchestrations = orchestrationMapper.selectBySceneId(orchestrationIdOrSceneId);
            if (orchestrations != null && !orchestrations.isEmpty()) {
                List<Long> ids = orchestrations.stream()
                        .map(SceneOrchestration::getId)
                        .collect(Collectors.toList());
                PageHelper.startPage(pageNum, pageSize);
                list = executionSessionMapper.selectListByBizIds("orchestration", ids, null, null);
            }
            if (list == null || list.isEmpty()) {
                PageHelper.clearPage();
                // 兜底：查询全部编排会话
                PageHelper.startPage(pageNum, pageSize);
                list = executionSessionMapper.selectList("orchestration", null, null, null);
            }
        }
        return new PageInfo<>(list);
    }

    /**
     * 查询编排执行会话详情
     */
    public ExecutionSession getOrchestrationSessionDetail(Long sessionId) {
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
