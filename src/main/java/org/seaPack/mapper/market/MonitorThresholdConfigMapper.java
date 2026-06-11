package org.seaPack.mapper.market;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.market.MonitorThresholdConfig;

import java.util.List;

/**
 * 监控阈值配置 Mapper
 * <p>提供阈值规则的 CRUD，按 monitor_id 关联 user_stock_monitor。</p>
 */
@Mapper
public interface MonitorThresholdConfigMapper {

    /** 查询指定监控记录的所有阈值 */
    List<MonitorThresholdConfig> selectByMonitorId(@Param("monitorId") Long monitorId);

    /** 按 ID 查询阈值 */
    MonitorThresholdConfig selectById(@Param("id") Long id);

    /** 新增阈值 */
    int insert(MonitorThresholdConfig record);

    /** 更新阈值 */
    int update(MonitorThresholdConfig record);

    /** 删除阈值 */
    int deleteById(@Param("id") Long id);
}
