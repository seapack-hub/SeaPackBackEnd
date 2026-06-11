package org.seaPack.config;

import org.seaPack.websocket.QuoteWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置
 * <p>注册前端行情推送端点 /ws/quote，允许跨域。</p>
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(quoteWebSocketHandler(), "/ws/quote")
                .setAllowedOrigins("*");
    }

    @Bean
    public QuoteWebSocketHandler quoteWebSocketHandler() {
        return new QuoteWebSocketHandler();
    }
}
