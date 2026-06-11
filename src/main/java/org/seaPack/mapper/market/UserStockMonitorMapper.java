package org.seaPack.mapper.market;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.dto.market.UserStockMonitorQuery;
import org.seaPack.model.market.UserStockMonitor;

import java.util.List;
import java.util.Map;

/**
 * 用户监控池 Mapper
 * <p>提供监控记录的 CRUD 以及联表查询（含 stock_basic、monitor_threshold_config）。</p>
 */
@Mapper
public interface UserStockMonitorMapper {

    /** 分页条件查询监控列表（含阈值 JSON） */
    List<Map<String, Object>> selectMonitorList(UserStockMonitorQuery query);

    /** 按 ID 查询监控记录 */
    UserStockMonitor selectById(@Param("id") Long id);

    /** 按 userId + stockCode 查询（用于新增时判重） */
    UserStockMonitor selectByUserAndCode(@Param("userId") Long userId, @Param("stockCode") String stockCode);

    /** 查询所有启用状态的监控股票代码及交易所（去重） */
    List<Map<String, String>> selectDistinctActiveCodes();

    /** 新增监控记录 */
    int insert(UserStockMonitor record);

    /** 更新监控记录（启用/停用、备注等） */
    int update(UserStockMonitor record);

    /** 删除监控记录 */
    int deleteById(@Param("id") Long id);
}
