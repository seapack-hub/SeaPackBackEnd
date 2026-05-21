package org.seaPack.components;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService(chatModel = "chatLanguageModel") // 自动绑定你配置类里的千问大模型
public interface Assistant {

    // 普通对话用这个
    @SystemMessage("你是一个专业的智能助手，擅长搜集资料并整理成文档。")
    String chat(@UserMessage String userMessage);

    // 流式方法（Controller 里要用这个！）
    // 注意：这里没有返回值类型 String，而是 TokenStream
    @SystemMessage("你是一个专业的智能助手...")
    TokenStream chatStream(@UserMessage String userMessage);
}
