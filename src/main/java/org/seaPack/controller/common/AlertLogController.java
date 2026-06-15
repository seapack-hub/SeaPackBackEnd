package org.seaPack.controller.common;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.common.AlertLog;
import org.seaPack.service.common.AlertLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 告警日志控制器
 * <p>提供告警日志的查询接口，支持按用户、股票代码、时间范围筛选。</p>
 */
@Slf4j
@RestController
@RequestMapping("/alertLog")
public class AlertLogController {

    @Autowired
    private AlertLogService alertLogService;

    /**
     * 分页查询告警日志（按 sent_time 降序）
     *
     * @param userId    用户 ID（必填）
     * @param stockCode 股票代码（可选，模糊搜索）
     * @param startTime 起始时间（可选）
     * @param endTime   截止时间（可选）
     */
    @GetMapping("/listByUser/{userId}")
    public ResponseEntity<PageInfo<AlertLog>> listByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String stockCode,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return ResponseEntity.ok(alertLogService.getLogsByUserId(pageNum, pageSize, userId, stockCode, startTime, endTime));
    }
}
