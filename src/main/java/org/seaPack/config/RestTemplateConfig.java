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
     * - connectTimeout：连接超时 10 秒
     * - readTimeout：读取超时 30 秒
     * - retryOnConnectionFailure：连接失败自动重试
     */
    @Bean
    public RestTemplate restTemplate() {
        ConnectionPool connectionPool = new ConnectionPool(50, 5, TimeUnit.MINUTES);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        return new RestTemplate(new OkHttp3ClientHttpRequestFactory(okHttpClient));
    }
}
