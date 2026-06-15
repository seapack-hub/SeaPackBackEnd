package org.seaPack.service.market;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.market.DividendMonitorMapper;
import org.seaPack.mapper.market.MonitorThresholdConfigMapper;
import org.seaPack.model.common.AlertLog;
import org.seaPack.model.market.MonitorThresholdConfig;
import org.seaPack.service.common.AlertLogService;
import org.seaPack.service.common.NotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 股息率监控定时任务
 * <p>
 * 每晚实时行情更新后执行，遍历所有启用的监控记录，
 * 计算最新股息率并与阈值比较。
 * </p>
 *
 * <h3>防抖机制</h3>
 * 为防止阈值附近波动导致重复告警，每条阈值规则记录上次触发时间
 * （last_triggered_time）。在冷静期内即使条件满足也不再触发。
 * 冷静期长度由 DEBOUNCE_HOURS 控制，默认 24 小时。
 */
@Slf4j
@Component
public class DividendMonitorTask {

    /** 防抖冷静期（小时）：同一阈值触发后至少间隔此时间才允许再次触发 */
    private static final int DEBOUNCE_HOURS = 24;

    @Autowired
    private DividendMonitorMapper dividendMonitorMapper;

    @Autowired
    private MonitorThresholdConfigMapper thresholdMapper;

    @Autowired
    private AlertLogService alertLogService;

    @Autowired
    private NotifyService notifyService;

    /**
     * 每天 22:00 执行股息率检查
     * <p>假定实时行情数据在 22:00 前已完成更新。</p>
     */
    @Scheduled(cron = "0 0 22 * * ?")
    public void checkDividendThresholds() {
        log.info("====== 股息率监控定时任务开始 ======");

        // 1. 查询所有启用状态的监控记录
        List<Map<String, Object>> monitors = dividendMonitorMapper.selectAllActiveMonitors();
        log.info("共 {} 条活跃监控记录", monitors.size());

        int triggeredCount = 0;

        for (Map<String, Object> monitor : monitors) {
            Long monitorId = toLong(monitor.get("monitor_id"));
            Long userId = toLong(monitor.get("user_id"));
            String stockCode = (String) monitor.get("stock_code");
            String stockName = (String) monitor.get("stock_name");

            // 2. 查询该股票的最新行情价和最新分红每股派现
            Map<String, BigDecimal> quoteAndDividend = dividendMonitorMapper.selectLatestQuoteAndDividend(stockCode);
            if (quoteAndDividend == null || quoteAndDividend.isEmpty()) {
                log.warn("股票 {} 无最新行情或分红数据，跳过", stockCode);
                continue;
            }

            BigDecimal currentPrice = quoteAndDividend.get("current_price");
            BigDecimal cashPerShare = quoteAndDividend.get("cash_per_share");

            if (currentPrice == null || cashPerShare == null
                    || currentPrice.compareTo(BigDecimal.ZERO) <= 0
                    || cashPerShare.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("股票 {} 数据不完整（price={}, cash={}），跳过", stockCode, currentPrice, cashPerShare);
                continue;
            }

            // 3. 计算股息率 (%) = cashPerShare / currentPrice * 100
            BigDecimal yield = cashPerShare.divide(currentPrice, 6, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(4, RoundingMode.HALF_UP);
            log.debug("股票 {} 股息率: {}%", stockCode, yield);

            // 4. 查询该监控记录的所有已启用阈值
            List<MonitorThresholdConfig> thresholds = thresholdMapper.selectByMonitorId(monitorId);
            for (MonitorThresholdConfig threshold : thresholds) {
                if (threshold.getIsActive() == null || threshold.getIsActive() != 1) {
                    continue;
                }

                // ---- 防抖检查 ----
                if (threshold.getLastTriggeredTime() != null) {
                    long elapsedMs = System.currentTimeMillis() - threshold.getLastTriggeredTime().getTime();
                    long debounceMs = DEBOUNCE_HOURS * 3600L * 1000L;
                    if (elapsedMs < debounceMs) {
                        log.debug("阈值 {} 距上次触发不足 {} 小时，跳过", threshold.getId(), DEBOUNCE_HOURS);
                        continue;
                    }
                }

                BigDecimal rate = threshold.getThresholdRate();
                String type = threshold.getTriggerType();
                boolean triggered = false;

                // 5. 判断阈值条件
                if ("CROSS_UP".equals(type) && yield.compareTo(rate) >= 0) {
                    triggered = true;
                } else if ("CROSS_DOWN".equals(type) && yield.compareTo(rate) <= 0) {
                    triggered = true;
                }

                if (triggered) {
                    // 6. 更新阈值上的触发时间快照
                    threshold.setLastTriggeredTime(new Date());
                    threshold.setTriggerValue(yield);
                    thresholdMapper.update(threshold);

                    // 7. 写入告警日志
                    AlertLog alertLog = new AlertLog();
                    alertLog.setRuleId(threshold.getId());
                    alertLog.setTriggeredRate(yield.setScale(2, RoundingMode.HALF_UP));
                    alertLog.setTriggeredPrice(currentPrice.setScale(3, RoundingMode.HALF_UP));
                    alertLogService.insertLog(alertLog);

                    // 8. 发送通知（邮件 + SMS）
                    try {
                        notifyService.sendAlert(
                                userId, stockCode, stockName,
                                yield.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                                currentPrice.setScale(3, RoundingMode.HALF_UP).toPlainString(),
                                rate.toPlainString(), type);
                    } catch (Exception e) {
                        log.error("发送通知失败 userId={}, error={}", userId, e.getMessage());
                    }

                    triggeredCount++;
                    log.info("触发告警: ruleId={}, stock={}, yield={}%, type={}, rate={}%",
                            threshold.getId(), stockCode, yield, type, rate);
                }
            }
        }

        log.info("====== 股息率监控定时任务结束，共触发 {} 条告警 ======", triggeredCount);
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Long) return (Long) val;
        if (val instanceof Integer) return ((Integer) val).longValue();
        return Long.valueOf(val.toString());
    }
}
