package org.seaPack.controller.workflow;

import org.seaPack.model.workflow.WorkflowStats;
import org.seaPack.service.workflow.WorkflowStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流统计控制器
 * <p>提供工作流执行统计的总览、按工作流/日期维度查询、热门排行榜等接口。</p>
 */
@RestController
@RequestMapping("/workflows/stats")
public class WorkflowStatsController {

    @Autowired
    private WorkflowStatsService workflowStatsService;

    /** 总览统计 */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> overview() {
        return ResponseEntity.ok(workflowStatsService.getOverview());
    }

    /** 按工作流查询统计 */
    @GetMapping("/byWorkflow")
    public ResponseEntity<List<WorkflowStats>> byWorkflow(
            @RequestParam(required = false) Long workflowId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(workflowStatsService.getByWorkflow(workflowId, startDate, endDate));
    }

    /** 按日期查询统计 */
    @GetMapping("/byDate")
    public ResponseEntity<List<WorkflowStats>> byDate(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(workflowStatsService.getByDate(startDate, endDate));
    }

    /** 热门工作流排行 */
    @GetMapping("/topWorkflows")
    public ResponseEntity<List<Map<String, Object>>> topWorkflows(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(workflowStatsService.getTopWorkflows(limit));
    }
}
