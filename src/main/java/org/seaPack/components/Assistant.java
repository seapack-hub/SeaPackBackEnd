package org.seaPack.components;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * AI 助手接口（LangChain4j @AiService）
 * <p>自动绑定配置中的 ChatLanguageModel 和 ChatMemory，提供同步与流式两种对话方式。
 * 流式返回 TokenStream 供 SSE 逐 token 推送。</p>
 */
@AiService(
        chatModel = "chatLanguageModel",
        chatMemory = "chatMemory"
)
public interface Assistant {

    /**
     * 同步对话（等待完整回复）
     * @param userMessage 用户输入
     * @return AI 完整回复文本
     */
    @SystemMessage("你是一个专业的智能助手，擅长搜集资料并整理成文档。")
    String chat(@UserMessage String userMessage);

    /**
     * 流式对话（逐 token 返回）
     * @param userMessage 用户输入
     * @return TokenStream 用于注册 onNext / onComplete / onError 回调
     */
    @SystemMessage("你是一个专业的智能助手...")
    TokenStream chatStream(@UserMessage String userMessage);
}
