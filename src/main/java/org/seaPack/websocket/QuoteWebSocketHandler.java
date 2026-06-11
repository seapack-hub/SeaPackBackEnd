package org.seaPack.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 行情推送 WebSocket Handler
 * <p>管理前端 WebSocket 连接，当 iTick 推送新行情时广播给所有在线前端。</p>
 */
@Slf4j
public class QuoteWebSocketHandler extends TextWebSocketHandler {

    /** 在线前端会话集合（线程安全） */
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("前端 WebSocket 已连接：{}，当前在线 {}", session.getId(), sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("前端 WebSocket 已断开：{}，当前在线 {}", session.getId(), sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 前端发来的消息暂不处理，可扩展
    }

    /** 向所有连接的前端广播行情数据 */
    public void broadcast(String message) {
        TextMessage msg = new TextMessage(message);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(msg);
                } catch (Exception e) {
                    log.error("广播消息失败，session={}", session.getId(), e);
                }
            }
        }
    }

    public int getOnlineCount() {
        return sessions.size();
    }
}
