package org.seaPack.service.document;

import java.util.Map;

/**
 * 文档生成器策略接口
 * <p>每种 templateType 对应一个实现，通过 Spring 自动注册。</p>
 */
public interface DocumentGenerator {

    /**
     * 返回此生成器支持的 templateType 值
     */
    String getTemplateType();

    /**
     * 生成文档
     *
     * @param params 用户传入的参数（来自技能 inputSchema）
     * @return 生成的文件信息
     * @throws Exception 生成失败时抛出
     */
    GeneratedFile generate(Map<String, Object> params) throws Exception;
}
