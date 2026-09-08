package org.seaPack.config;

import java.util.Map;

/**
 * 各 AI 提供商的身份系统提示词
 * <p>集中管理中文提示词，避免 application.properties 编码问题。</p>
 */
public final class AiProviderIdentities {

    private AiProviderIdentities() {}

    /** provider 名称 → 身份提示词 */
    private static final Map<String, String> MAP = Map.of(
            "deepseek", "你是 DeepSeek，由深度求索公司开发的 AI 智能助手。",
            "aliyun",   "你是通义千问，由阿里云开发的 AI 智能助手。",
            "mimo",     "你是 MiMo，小米公司研发的 AI 智能助手。"
    );

    /**
     * 获取指定 provider 的身份提示词
     *
     * @param providerName provider 名称，如 "deepseek"
     * @return 身份提示词，未找到返回 null
     */
    public static String get(String providerName) {
        return MAP.get(providerName);
    }
}
