package org.seaPack.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data // Lombok 自动生成 Getter/Setter/ToString
@Component
@ConfigurationProperties(prefix = "ai") // 对应配置文件中的 ai.*
public class AIProperties {

    private String activeProvider;
    private Map<String, ProviderConfig> providers;

    @Data // 内部类也需要 @Data
    public static class ProviderConfig {
        private String apiKey;
        private String baseUrl;
        private String chatModel;
        private String embeddingModel;
    }
}
