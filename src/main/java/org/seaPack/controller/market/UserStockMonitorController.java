package org.seaPack.controller.market;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.dto.market.UserStockMonitorQuery;
import org.seaPack.dto.market.UserStockMonitorVO;
import org.seaPack.model.market.MonitorThresholdConfig;
import org.seaPack.service.market.UserStockMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户股票监控控制器
 * <p>提供监控池的增删改查以及阈值规则的配置接口，配合前端"主列表 + 抽屉配置"交互模式。</p>
 */
@Slf4j
@RestController
@RequestMapping("/userStockMonitor")
public class UserStockMonitorController {

    @Autowired
    private UserStockMonitorService monitorService;

    /** 分页条件查询监控股票列表（含阈值 JSON） */
    @PostMapping("/list")
    public ResponseEntity<PageInfo<UserStockMonitorVO>> list(@RequestBody UserStockMonitorQuery query) {
        if (query.getPageNum() == null) query.setPageNum(1);
        if (query.getPageSize() == null) query.setPageSize(10);
        return ResponseEntity.ok(monitorService.pageMonitorList(query));
    }

    /** 添加股票到监控池（重复 code 会抛异常） */
    @PostMapping("/add")
    public ResponseEntity<Integer> add(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String stockCode = (String) params.get("stockCode");
        String remark = (String) params.get("remark");
        return ResponseEntity.ok(monitorService.addMonitor(userId, stockCode, remark));
    }

    /** 启用/停用监控 */
    @PostMapping("/toggle/{id}")
    public ResponseEntity<Integer> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(monitorService.toggleMonitor(id));
    }

    /** 删除监控记录（级联删除关联的阈值配置） */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(monitorService.deleteMonitor(id));
    }

    /** 新增阈值规则 */
    @PostMapping("/threshold/add")
    public ResponseEntity<Integer> addThreshold(@RequestBody MonitorThresholdConfig threshold) {
        return ResponseEntity.ok(monitorService.addThreshold(threshold));
    }

    /** 更新阈值规则 */
    @PostMapping("/threshold/update")
    public ResponseEntity<Integer> updateThreshold(@RequestBody MonitorThresholdConfig threshold) {
        return ResponseEntity.ok(monitorService.updateThreshold(threshold));
    }

    /** 删除阈值规则 */
    @DeleteMapping("/threshold/delete/{id}")
    public ResponseEntity<Integer> deleteThreshold(@PathVariable Long id) {
        return ResponseEntity.ok(monitorService.deleteThreshold(id));
    }
}
