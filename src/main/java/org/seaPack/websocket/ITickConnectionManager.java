package org.seaPack.websocket;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * iTick WebSocket 连接管理器
 * <p>应用启动时通过 JSR 356 WebSocketContainer 连接 iTick 服务，
 * 将 API Token 写入 TokenConfigurator 静态字段，由 @ClientEndpoint 的 configurator 在握手时注入。</p>
 */
@Slf4j
@Component
public class ITickConnectionManager {

    @Value("${itick.websocket.url}")
    private String itickUrl;

    @Value("${itick.websocket.token}")
    private String apiToken;

    private WebSocketContainer container;
    private jakarta.websocket.Session session;

    @PostConstruct
    public void connect() {
        try {
            // 将 token 写入静态字段，供 TokenConfigurator 在握手时使用
            TokenConfigurator.token = apiToken;

            // 获取 JSR 356 WebSocket 容器
            container = ContainerProvider.getWebSocketContainer();
            // 建立到 iTick 服务端的 WebSocket 连接
            // 由 @ClientEndpoint(configurator=TokenConfigurator.class) 自动处理认证头
            session = container.connectToServer(ITickWebSocketHandler.class, URI.create(itickUrl));
            log.info("iTick WebSocket 客户端启动，连接地址：{}", itickUrl);
        } catch (Exception e) {
            log.error("iTick WebSocket 连接失败", e);
        }
    }

    @PreDestroy
    public void disconnect() {
        if (session != null) {
            try {
                session.close();
                log.info("iTick WebSocket 客户端已停止");
            } catch (Exception e) {
                log.warn("iTick WebSocket 关闭异常", e);
            }
        }
    }
}
