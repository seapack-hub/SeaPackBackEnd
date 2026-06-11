package org.seaPack.service.market;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.dto.market.UserStockMonitorQuery;
import org.seaPack.dto.market.UserStockMonitorVO;
import org.seaPack.mapper.market.MonitorThresholdConfigMapper;
import org.seaPack.mapper.market.UserStockMonitorMapper;
import org.seaPack.model.market.MonitorThresholdConfig;
import org.seaPack.model.market.UserStockMonitor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户监控池业务逻辑
 * <p>封装监控股票的新增/删除/切换以及阈值规则的配置操作。</p>
 */
@Slf4j
@Service
@Transactional(readOnly = false)
public class UserStockMonitorService {

    @Autowired
    private UserStockMonitorMapper monitorMapper;

    @Autowired
    private MonitorThresholdConfigMapper thresholdMapper;

    /**
     * 分页条件查询监控列表（含阈值 JSON）
     */
    public PageInfo<UserStockMonitorVO> pageMonitorList(UserStockMonitorQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<Map<String, Object>> rows = monitorMapper.selectMonitorList(query);
        PageInfo<Map<String, Object>> raw = new PageInfo<>(rows);

        List<UserStockMonitorVO> voList = convertToVOList(rows);
        PageInfo<UserStockMonitorVO> result = new PageInfo<>();
        BeanUtils.copyProperties(raw, result, "list");
        result.setList(voList);
        return result;
    }

    // ---- 内部转换方法 ----
    private List<UserStockMonitorVO> convertToVOList(List<Map<String, Object>> rows) {
        List<UserStockMonitorVO> list = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            UserStockMonitorVO vo = new UserStockMonitorVO();
            vo.setMonitorId(toLong(row.get("monitor_id")));
            vo.setStockCode((String) row.get("stock_code"));
            vo.setStockName((String) row.get("stock_name"));
            vo.setIsActive(toInteger(row.get("is_active")));
            vo.setRemark((String) row.get("remark"));
            vo.setThresholds((String) row.get("thresholds"));
            list.add(vo);
        }
        return list;
    }

    /**
     * 新增监控股票（防重复）
     */
    public int addMonitor(Long userId, String stockCode, String remark) {
        // 检查是否已监控该股票
        UserStockMonitor existing = monitorMapper.selectByUserAndCode(userId, stockCode);
        if (existing != null) {
            throw new RuntimeException("该股票已在监控池中");
        }
        UserStockMonitor record = new UserStockMonitor();
        record.setUserId(userId);
        record.setStockCode(stockCode);
        record.setIsActive(1);
        record.setRemark(remark);
        return monitorMapper.insert(record);
    }

    /**
     * 启用/停用监控
     */
    public int toggleMonitor(Long id) {
        UserStockMonitor record = monitorMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("监控记录不存在");
        }
        record.setIsActive(record.getIsActive() == 1 ? 0 : 1);
        return monitorMapper.update(record);
    }

    /**
     * 删除监控记录（同时级联删除其阈值配置）
     */
    public int deleteMonitor(Long id) {
        UserStockMonitor record = monitorMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("监控记录不存在");
        }
        // 先删除关联的阈值配置
        List<MonitorThresholdConfig> thresholds = thresholdMapper.selectByMonitorId(id);
        for (MonitorThresholdConfig t : thresholds) {
            thresholdMapper.deleteById(t.getId());
        }
        return monitorMapper.deleteById(id);
    }

    /**
     * 保存阈值（先删后插全量替换）
     */
    @Transactional
    public void saveThresholds(Long monitorId, List<ThresholdRowDTO> rows) {
        thresholdMapper.deleteByMonitorId(monitorId);
        for (ThresholdRowDTO row : rows) {
            MonitorThresholdConfig t = new MonitorThresholdConfig();
            t.setMonitorId(monitorId);
            t.setThresholdRate(BigDecimal.valueOf(row.getRate().doubleValue()));
            t.setTriggerType(row.getType());
            t.setIsActive(1);
            thresholdMapper.insert(t);
        }
    }

    /**
     * 查询指定监控记录的所有阈值
     */
    public List<MonitorThresholdConfig> getThresholdList(Long monitorId) {
        return thresholdMapper.selectByMonitorId(monitorId);
    }

    // ---- 阈值行 DTO ----
    @Data
    public static class ThresholdRowDTO {
        private String type;
        private Number rate;
    }

    // ---- 类型转换辅助方法 ----
    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Long) return (Long) val;
        if (val instanceof Integer) return ((Integer) val).longValue();
        return Long.valueOf(val.toString());
    }

    private Integer toInteger(Object val) {
        if (val == null) return null;
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof Long) return ((Long) val).intValue();
        return Integer.valueOf(val.toString());
    }
}
