package org.seaPack.config;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * RestTemplate 配置类
 * 基于 OkHttp 连接池实现，支持高并发下的连接复用
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 配置带 OkHttp 连接池的 RestTemplate
     * <p>
     * 连接池参数说明：
     * - maxIdleConnections：最大空闲连接数 50
     * - keepAliveDuration：空闲连接保活时间 5 分钟
     * - connectTimeout：连接超时 5 秒
     * - readTimeout：读取超时 10 秒
     */
    @Bean
    public RestTemplate restTemplate() {
        // 创建 OkHttp 连接池：最大空闲连接 50 个，保活 5 分钟
        ConnectionPool connectionPool = new ConnectionPool(50, 5, TimeUnit.MINUTES);

        // 构建 OkHttpClient
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectionPool(connectionPool)           // 设置连接池
                .connectTimeout(5, TimeUnit.SECONDS)     // 连接超时
                .readTimeout(10, TimeUnit.SECONDS)       // 读取超时
                .build();

        // 使用 OkHttp 作为 RestTemplate 的底层 HTTP 客户端
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory(okHttpClient));
    }
}
