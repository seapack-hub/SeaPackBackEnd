package org.seaPack.mapper.market;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 股息率监控数据查询 Mapper
 * <p>提供定时任务所需的批量查询：活跃监控记录、最新行情、最新分红数据。</p>
 */
@Mapper
public interface DividendMonitorMapper {

    /**
     * 查询所有启用状态的监控记录（含 userId、stockCode、stockName）
     */
    List<Map<String, Object>> selectAllActiveMonitors();

    /**
     * 查询指定股票的最新行情 + 最新分红数据（用于计算股息率）
     *
     * @param stockCode 股票代码
     * @return currentPrice + cashPerShare，若无数据返回 null
     */
    Map<String, BigDecimal> selectLatestQuoteAndDividend(@Param("stockCode") String stockCode);
}
