package org.seaPack.controller.market;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.dto.market.UserStockMonitorQuery;
import org.seaPack.dto.market.UserStockMonitorVO;
import org.seaPack.service.market.UserStockMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    /** 保存阈值（全量替换：先删后插） */
    @PostMapping("/threshold/save")
    public ResponseEntity<Void> saveThreshold(@RequestBody Map<String, Object> params) {
        Long monitorId = Long.valueOf(params.get("monitorId").toString());
        List<Map<String, Object>> rows = (List<Map<String, Object>>) params.get("thresholds");
        List<UserStockMonitorService.ThresholdRowDTO> list = new java.util.ArrayList<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                UserStockMonitorService.ThresholdRowDTO dto = new UserStockMonitorService.ThresholdRowDTO();
                dto.setType((String) row.get("type"));
                dto.setRate((Number) row.get("rate"));
                list.add(dto);
            }
        }
        monitorService.saveThresholds(monitorId, list);
        return ResponseEntity.ok().build();
    }

    /** 查询指定监控记录的所有阈值 */
    @GetMapping("/threshold/list/{monitorId}")
    public ResponseEntity<List<org.seaPack.model.market.MonitorThresholdConfig>> thresholdList(@PathVariable Long monitorId) {
        return ResponseEntity.ok(monitorService.getThresholdList(monitorId));
    }
}
