package org.seaPack.service;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.seaPack.config.DeepSeekConfig;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RagService {

    /**
     * DeepSeek配置类，注入API密钥、BaseURL和模型名称
     */
    private final DeepSeekConfig deepSeekConfig;
    
    /**
     * 向量化模型，用于将文本转换为向量表示
     */
    private EmbeddingModel embeddingModel;
    
    /**
     * 对话模型，用于生成回答
     */
    private ChatLanguageModel chatModel;
    
    /**
     * 向量存储库，用于存储文档的向量（当前使用内存存储）
     */
    private EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 命名空间到向量存储的映射，支持多知识库隔离
     * Key: 命名空间名称
     * Value: 该命名空间下的向量存储库
     */
    private final Map<String, EmbeddingStore<TextSegment>> namespaceStore = new ConcurrentHashMap<>();

    /**
     * 构造函数注入DeepSeekConfig配置
     * @param deepSeekConfig DeepSeek配置类
     */
    public RagService(DeepSeekConfig deepSeekConfig) {
        this.deepSeekConfig = deepSeekConfig;
    }

    /**
     * 初始化方法，在Bean构造完成后执行
     * 初始化embedding模型和chat模型
     */
    @PostConstruct
    public void init() {
        // 构建向量化模型，用于将文本转换为embedding向量
        // 使用OpenAiEmbeddingModel，支持兼容OpenAI协议的其他API（如DeepSeek）
        embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(deepSeekConfig.getApiKey())                    // 设置API密钥
                .baseUrl(deepSeekConfig.getBaseUrl())                   // 设置API base URL（如DeepSeek的API地址）
                .modelName(deepSeekConfig.getEmbeddingModel())          // 设置embedding模型名称（如text-embedding-3-small）
                .build();

        // 构建对话模型，用于生成回答
        // 使用OpenAiChatModel，支持兼容OpenAI协议的其他API（如DeepSeek）
        chatModel = OpenAiChatModel.builder()
                .apiKey(deepSeekConfig.getApiKey())                     // 设置API密钥
                .baseUrl(deepSeekConfig.getBaseUrl())                   // 设置API base URL
                .modelName(deepSeekConfig.getModel())                   // 设置对话模型名称（如deepseek-chat）
                .temperature(0.7)                                       // 设置温度参数，控制生成随机性（0-2之间，值越大越随机）
                .build();

        // 初始化默认的内存向量存储库（当前未使用，因为每个namespace有独立的存储）
        embeddingStore = new InMemoryEmbeddingStore<>();
    }

    /**
     * 文档入库方法，将文本转换为向量并存储
     * @param namespace 命名空间，用于隔离不同知识库
     * @param text 要入库的文本内容
     */
    public void ingestText(String namespace, String text) {
        // 获取或创建该命名空间对应的向量存储库
        EmbeddingStore<TextSegment> store = namespaceStore.computeIfAbsent(namespace, k -> new InMemoryEmbeddingStore<>());
        
        // 将文本转换为Document对象（使用构造方法）
        Document document = new Document(text);
        
        // 创建文档分割器，按段落分割，每段最大500字符，重叠100字符
        DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(500, 100);
        // 分割文档为多个文本片段
        List<TextSegment> segments = splitter.split(document);

        // 遍历每个文本片段，进行向量化并存储
        for (TextSegment segment : segments) {
            // 使用embedding模型将文本片段转换为向量
            Embedding embedding = embeddingModel.embed(segment.text()).content();
            // 将向量和对应的文本片段存入向量存储库
            store.add(embedding, segment);
        }
    }

    /**
     * 问答方法，根据问题检索相关文档并生成回答
     * @param namespace 命名空间，指定在哪个知识库中搜索
     * @param question 用户问题
     * @return 生成的回答文本
     */
    public String chat(String namespace, String question) {
        // 获取该命名空间下的向量存储库
        EmbeddingStore<TextSegment> store = namespaceStore.get(namespace);
        // 如果该命名空间不存在，返回提示信息
        if (store == null) {
            return "该命名空间下没有文档";
        }

        // 将用户问题转换为向量
        Embedding queryEmbedding = embeddingModel.embed(question).content();
        
        // 在向量存储库中查找最相似的文档
        // 参数：查询向量、返回结果数量3、最小相似度阈值0.5
        List<EmbeddingMatch<TextSegment>> matches = store.findRelevant(queryEmbedding, 3, 0.5);

        // 如果没有找到相关文档，返回提示信息
        if (matches.isEmpty()) {
            return "没有找到相关文档";
        }

        // 提取所有匹配文档的文本内容，用双换行符连接
        String context = matches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));

        // 使用 String 格式化构建提示词内容
        String promptText = String.format(
                "你是一个智能助手。请严格根据以下上下文信息回答用户的问题。\n" +
                        "如果上下文中没有相关信息，请直接说明你不知道，不要编造答案。\n\n" +
                        "上下文信息：\n" +
                        "------\n" +
                        "%s\n" +
                        "------\n\n" +
                        "用户问题：%s\n" +
                        "请给出回答：",
                context, question
        );
        // 将字符串包装为 UserMessage 对象
        UserMessage userMessage = new UserMessage(promptText);

        // 新版本 LangChain4j 需要显式构建请求对象
        ChatRequest request = ChatRequest.builder()
                .messages(userMessage) // 设置消息
                .build();

        // 4. 调用模型，接收 ChatRequest 并返回 ChatResponse
        ChatResponse chatResponse = chatModel.chat(request);

        // 5. 从响应中提取文本
        // aiMessage() 返回 Optional<AiMessage>，我们需要 get() 并取 text()
        return chatResponse.aiMessage().text();
    }

    /**
     * 清除指定命名空间的所有文档
     * @param namespace 要清除的命名空间名称
     */
    public void clearNamespace(String namespace) {
        namespaceStore.remove(namespace);
    }

    /**
     * 获取所有已存在的命名空间列表
     * @return 命名空间名称列表
     */
    public List<String> getNamespaces() {
        return List.copyOf(namespaceStore.keySet());
    }
}