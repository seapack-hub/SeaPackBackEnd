package org.seaPack.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源映射配置
 * <p>
 * 将本地存储目录映射为可公开访问的 URL 路径：
 * </p>
 * <ul>
 * <li>/images/** → AI 生成图片（uploads/images/）</li>
 * <li>/files/** → 通用上传文件（uploads/files/），供博客编辑器等使用</li>
 * </ul>
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${ai.image-generation.storage-dir:uploads/images}")
    private String storageDir;

    @Value("${file.upload.dir:uploads/files}")
    private String fileUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String userDir = System.getProperty("user.dir");

        // AI 生成图片：/images/** → uploads/images/
        String imagesPath = java.nio.file.Paths.get(userDir, storageDir).toAbsolutePath().normalize().toUri()
                .toString();
        if (!imagesPath.endsWith("/")) imagesPath += "/";
        registry.addResourceHandler("/images/**")
                .addResourceLocations(imagesPath)
                .setCachePeriod(3600);

        // 通用上传文件：/files/** → uploads/files/
        String filesPath = java.nio.file.Paths.get(userDir, fileUploadDir).toAbsolutePath().normalize().toUri()
                .toString();
        if (!filesPath.endsWith("/")) filesPath += "/";
        registry.addResourceHandler("/files/**")
                .addResourceLocations(filesPath)
                .setCachePeriod(3600);
    }
}
