package org.seaPack.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源映射配置
 * <p>将本地图片存储目录映射为可公开访问的 /images/** URL 路径，
 * 前端可直接通过 http://host:port/images/filename.png 访问生成的图片。</p>
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${ai.image-generation.storage-dir:uploads/images}")
    private String storageDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = java.nio.file.Paths.get(storageDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/images/**")
                .addResourceLocations(absolutePath)
                .setCachePeriod(3600);
    }
}
