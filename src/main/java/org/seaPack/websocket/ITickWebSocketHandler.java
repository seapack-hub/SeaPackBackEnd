package org.seaPack.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.market.UserStockMonitorMapper;
import org.seaPack.model.market.StockRealtimeQuote;
import org.seaPack.service.market.StockRealtimeQuoteService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * iTick WebSocket 客户端端点
 * <p>使用 JSR 356 @ClientEndpoint 注解实现，由 WebSocketContainer 自动实例化。
 * Spring Bean 通过 SpringContextHolder 获取。</p>
 */
@Slf4j
@ClientEndpoint(configurator = TokenConfigurator.class)
public class ITickWebSocketHandler {

    private StockRealtimeQuoteService quoteService;
    private QuoteWebSocketHandler quoteWebSocketHandler;
    private UserStockMonitorMapper monitorMapper;

    private Session session;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @OnOpen
    public void onOpen(Session session) {
        // 通过 SpringContextHolder 获取 Spring Bean
        this.quoteService = SpringContextHolder.getBean(StockRealtimeQuoteService.class);
        this.quoteWebSocketHandler = SpringContextHolder.getBean(QuoteWebSocketHandler.class);
        this.monitorMapper = SpringContextHolder.getBean(UserStockMonitorMapper.class);
        // 保存会话引用，后续发送订阅和心跳需要
        this.session = session;
        log.info("iTick 连接已建立，等待认证...");
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        // 清空会话引用，停止后续数据发送
        this.session = null;
        log.warn("iTick 连接已断开，原因：{}", reason);
    }

    @OnMessage
    public void onMessage(String message) {
        log.debug("iTick 原始消息：{}", message);
        try {
            JSONObject json = JSON.parseObject(message);
            String resAc = json.getString("resAc");

            if ("auth".equals(resAc)) {
                handleAuth(json);
            } else if ("subscribe".equals(resAc)) {
                handleSubscribe(json);
            } else if (json.containsKey("data")) {
                handleData(json);
            }
        } catch (Exception e) {
            log.warn("iTick 消息处理失败：{}", e.getMessage());
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("iTick 连接错误", error);
    }

    private void handleAuth(JSONObject json) {
        if (json.getIntValue("code") == 1) {
            log.info("iTick 认证成功");
            sendSubscribe();
            startHeartbeat();
        } else {
            log.error("iTick 认证失败：{}", json.getString("msg"));
        }
    }

    private void handleSubscribe(JSONObject json) {
        if (json.getIntValue("code") == 1) {
            log.info("iTick 订阅成功");
        } else {
            log.warn("iTick 订阅失败：{}", json.getString("msg"));
        }
    }

    private void handleData(JSONObject json) {
        JSONObject data = json.getJSONObject("data");
        if (data == null || data.isEmpty()) return;
        for (String symbol : data.keySet()) {
            try {
                JSONObject tick = data.getJSONObject(symbol);
                BigDecimal price = tick.getBigDecimal("ld");
                BigDecimal open = tick.getBigDecimal("o");
                BigDecimal high = tick.getBigDecimal("h");
                BigDecimal low = tick.getBigDecimal("l");
                String stockCode = symbol.contains("$") ? symbol.split("\\$")[0] : symbol;

                StockRealtimeQuote saved = quoteService.saveQuote(
                        stockCode, price, open, high, low, null);

                quoteWebSocketHandler.broadcast(JSON.toJSONString(saved));
            } catch (Exception e) {
                log.warn("解析行情数据失败 symbol={}", symbol, e);
            }
        }
    }

    private void sendSubscribe() {
        if (session == null || !session.isOpen()) {
            log.warn("iTick 订阅失败：WebSocket 未连接");
            return;
        }
        List<Map<String, String>> codes = monitorMapper.selectDistinctActiveCodes();
        if (codes == null || codes.isEmpty()) {
            log.warn("iTick 订阅失败：监控池无股票");
            return;
        }
        Map<String, String> exchangeMap = new HashMap<>();
        exchangeMap.put("SSE", "SH");
        exchangeMap.put("SZSE", "SZ");
        exchangeMap.put("BSE", "BJ");

        String params = codes.stream()
                .map(row -> {
                    String code = row.get("stock_code");
                    String ex = row.get("exchange");
                    String region = exchangeMap.getOrDefault(ex, "SH");
                    return code + "$" + region;
                })
                .collect(Collectors.joining(","));

        JSONObject msg = new JSONObject();
        msg.put("ac", "subscribe");
        msg.put("params", params);
        msg.put("types", "quote");
        session.getAsyncRemote().sendText(msg.toJSONString());
        log.info("iTick 发送订阅指令，共 {} 只股票", codes.size());
    }

    private void startHeartbeat() {
        scheduler.scheduleAtFixedRate(() -> {
            if (session != null && session.isOpen()) {
                session.getAsyncRemote().sendText("{\"ac\":\"ping\"}");
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
}
