package org.seaPack.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 * <p>
 * 使用 Caffeine 本地缓存，支持缓存名称和过期策略的细粒度配置。
 * 目前已注册的缓存：
 * - industryTree：行业树缓存
 * - stockHistory：股票历史 K 线缓存
 * - stockBillboard：龙虎榜数据缓存
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置 Caffeine 缓存管理器
     * <p>
     * Caffeine 参数说明：
     * - maximumSize：缓存最大条目数
     * - expireAfterWrite：写入后过期时间
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // 行业树缓存：最大 100 条，写入后 1 小时过期
        cacheManager.registerCustomCache("industryTree",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .recordStats()
                        .build());

        // 股票历史K线缓存：最大 10000 条，写入后 1 小时过期
        cacheManager.registerCustomCache("stockHistory",
                Caffeine.newBuilder()
                        .maximumSize(10000)
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .recordStats()
                        .build());

        // 龙虎榜数据缓存：最大 5000 条，写入后 1 小时过期
        cacheManager.registerCustomCache("stockBillboard",
                Caffeine.newBuilder()
                        .maximumSize(5000)
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .recordStats()
                        .build());

        log.info("Caffeine 缓存管理器初始化完成");
        return cacheManager;
    }
}
