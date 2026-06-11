package org.seaPack.websocket;

import jakarta.websocket.ClientEndpointConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * iTick WebSocket 握手配置器
 * <p>在 WebSocket 握手阶段注入 token 请求头，用于 iTick API 认证。
 * token 值由 ITickConnectionManager 在启动时设置。</p>
 */
public class TokenConfigurator extends ClientEndpointConfig.Configurator {

    /** 静态 token，由 ITickConnectionManager.connect() 启动时赋值 */
    public static String token;

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        if (token != null && !token.isEmpty()) {
            headers.put("token", Arrays.asList(token));
        }
    }
}
