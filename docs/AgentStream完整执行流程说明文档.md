# Agent Stream 完整执行流程说明文档

## 一、系统概述

Agent Stream 是系统中最复杂的对话模式，通过 **4 步链路编排** 增强 LLM 能力，将提示词组装、知识库检索、技能调用与 LLM 流式输出串联为一条完整链路。

**核心理念**：Agent 不再只是"聊天"，而是在调用 LLM 前先完成一系列准备工作——动态选择提示词模板、检索知识库获取背景知识、调用技能获取实时数据，然后将所有上下文注入 System Prompt 交给 LLM 生成回答。

**与其他模式的区别**：

| 模式 | 端点 | 特点 |
|------|------|------|
| `streaming_llm` | `/ai/dialog/stream` | 直接调 LLM，无链路编排 |
| `llm_chat` | `/ai/dialog/chat` | 非流式同步返回 |
| **`agent_stream`** | **`/ai/dialog/agent-stream`** | **Agent 4 步编排 + 流式输出** |
| `orchestration` | `/ai/dialog/orchestration` | LLM 动态路由到指定 Agent |

---

## 二、数据库表结构

### 2.1 ai_agent — Agent 主表（核心）

| 字段 | 类型 | 说明 |
|--|--|--|
| id | BIGINT | 主键 |
| name | VARCHAR(100) | Agent 名称，如"数据分析助手" |
| code | VARCHAR(50) | 唯一编码，如 `data_analyst` |
| avatar | VARCHAR(200) | 头像 |
| description | VARCHAR(500) | 描述 |
| system_prompt | TEXT | 系统提示词（基础人设） |
| model_code | VARCHAR(50) | 模型编码，如 `gpt-4`（空则用全局默认） |
| temperature | DECIMAL(3,1) | 温度 0-2 |
| max_tokens | INT | 最大输出 token |
| memory_enabled | TINYINT | 1=开启对话记忆 0=关闭 |
| memory_window | INT | 记忆窗口大小（轮数） |
| status | TINYINT | 1=启用 0=禁用 |

### 2.2 ai_agent_prompt — Agent-提示词模板关联

| 字段 | 类型 | 说明 |
|--|--|--|
| id | BIGINT | 主键 |
| agent_id | BIGINT | 关联 ai_agent.id |
| template_id | BIGINT | 关联 ai_prompt_template.id |
| is_primary | TINYINT | 1=主模板 0=辅助模板 |
| enabled | TINYINT | 1=启用 0=禁用 |
| sort_order | INT | 排序号 |

### 2.3 ai_agent_knowledge — Agent-知识库关联

| 字段 | 类型 | 说明 |
|--|--|--|
| id | BIGINT | 主键 |
| agent_id | BIGINT | 关联 ai_agent.id |
| knowledge_id | BIGINT | 关联 ai_knowledge_base.id |
| retrieval_count | INT | 每次检索返回的片段数（默认 3） |
| enabled | TINYINT | 1=启用 0=禁用 |
| sort_order | INT | 排序号 |

### 2.4 ai_agent_skill — Agent-技能关联

| 字段 | 类型 | 说明 |
|--|--|--|
| id | BIGINT | 主键 |
| agent_id | BIGINT | 关联 ai_agent.id |
| skill_id | BIGINT | 关联 ai_skill.id |
| enabled | TINYINT | 1=启用 0=禁用 |
| is_primary | TINYINT | 1=主技能 0=辅助技能 |
| sort_order | INT | 排序号 |

### 2.5 ai_execution_session — 执行记录表

| 字段 | 类型 | 说明 |
|--|--|--|
| id | BIGINT | 主键 |
| biz_type | VARCHAR(20) | 业务类型（`agent`） |
| biz_id | BIGINT | 关联 ai_agent.id |
| biz_name | VARCHAR(100) | Agent 名称（冗余存储） |
| conversation_id | VARCHAR(100) | 对话 ID（前端生成） |
| request_id | VARCHAR(100) | 消息 ID（前端生成） |
| user_message | TEXT | 用户输入 |
| output_result | TEXT | LLM 输出 |
| trace_snapshot | TEXT | 完整链路快照 JSON（见下文） |
| total_duration_ms | BIGINT | 总耗时毫秒 |
| tokens_prompt | INT | 提示词 token 数 |
| tokens_completion | INT | 补全 token 数 |
| model_name | VARCHAR(50) | 使用的模型 |
| status | VARCHAR(20) | 执行状态 |
| error_message | TEXT | 错误信息 |

**关系图**：
```
ai_agent (1) ←── (N) ai_agent_prompt (N) →── (1) ai_prompt_template
    ↑
    ├── (N) ai_agent_knowledge (N) →── (1) ai_knowledge_base
    ├── (N) ai_agent_skill (N) →── (1) ai_skill
    └── (1) ai_execution_session（biz_id = agent.id）
```

---

## 三、后端代码结构

### 3.1 文件清单

```
controller/ai/
├── AiDialogController.java              ← 统一对话控制器（4 个端点）
service/ai/
├── AiDialogService.java                 ← 统一对话调度服务（按 mode 分发）
├── AgentTestChatService.java            ← 【核心】Agent 4 步编排执行（1202 行）
├── AgentSkillExecutor.java              ← 技能执行引擎（详见 Skill 文档）
├── LlmSseHelper.java                    ← LLM 流式调用工具类
dto/ai/
├── AiDialogRequest.java                 ← 统一对话请求 DTO
├── SseEvent.java                        ← SSE 事件协议工具类
├── AgentTraceStep.java                  ← 链路追踪步骤 DTO
└── AgentTraceSnapshot.java              ← 链路追踪快照 DTO
```

### 3.2 AiDialogController — 对话入口

**基础路径**：`/ai/dialog`

| 方法 | 路径 | 说明 |
|--|--|--|
| POST | `/agent-stream` | Agent 流式对话（核心） |
| POST | `/stream` | streaming_llm 模式 |
| POST | `/chat` | llm_chat 模式 |
| POST | `/orchestration` | 编排对话模式 |

**请求 DTO（AiDialogRequest）**：

| 字段 | 说明 |
|--|--|
| mode | 对话模式（`agent_stream` / `streaming_llm` / `orchestration`） |
| agentId | Agent ID（agent_stream 必填） |
| question | 用户问题 |
| messages | 消息列表（OpenAI 格式） |
| history | 对话历史 |
| sceneId | 场景 ID |
| conversationId | 对话 ID（前端生成） |
| requestId | 消息 ID（前端生成） |

### 3.3 AiDialogService — 调度服务

`handleStream()` 是所有对话模式的统一入口，执行流程为：**额度校验 → 按 mode 分发 → 异常兜底**。

**完整代码（AiDialogService.java 第 103-123 行）**：

```java
public void handleStream(AiDialogRequest request, Long userId, String authToken,
                          SseEmitter emitter, HttpServletResponse response) {
    // 额度校验：调用大模型前检查用户剩余额度，超限则拒绝并返回提示
    String quotaError = tokenQuotaService.checkQuota(userId);
    if (quotaError != null) {
        SseEvent.sendError(emitter, quotaError);
        sendDoneAndClose(emitter, response, quotaError);  // 必须关闭 SSE
        return;
    }

    String mode = request.getMode();
    switch (mode) {
        case "streaming_llm" -> handleLlmStream(request, userId, emitter, response);
        case "agent_stream"  -> handleAgentStream(request, userId, authToken, emitter, response);
        case "orchestration" -> handleOrchestration(request, userId, authToken, emitter, response);
        default -> {
            SseEvent.sendError(emitter, "未知对话模式: " + mode);
            sendDoneAndClose(emitter, response, "未知对话模式");
        }
    }
}
```

**代码分析**：
1. **额度校验前置**：所有模式在调用 LLM 之前统一检查 Token 额度，避免浪费资源
2. **异常兜底 `sendDoneAndClose`**：无论哪种异常路径，都必须发送 `done` 事件并关闭 SSE 连接，否则前端会一直等待（fetch 永远不 resolve）
3. **switch 分发**：每个模式对应一个独立的私有方法，职责清晰

**Agent Stream 分发方法（AiDialogService.java 第 437-451 行）**：

```java
private void handleAgentStream(AiDialogRequest request, Long userId, String authToken,
                                SseEmitter emitter, HttpServletResponse response) {
    if (request.getAgentId() == null) {
        SseEvent.sendError(emitter, "Agent ID 不能为空");
        return;
    }

    // 注册取消标志（AiDialogService 统一管理）
    AtomicBoolean cancelFlag = registerCancelFlag(userId);
    try {
        agentTestChatService.testChatStream(request, userId, emitter, authToken, response);
    } finally {
        removeCancelFlag(userId);  // 无论成功失败，清理取消标志
    }
}
```

**代码分析**：
1. **参数校验**：Agent Stream 必须有 `agentId`，否则直接报错返回
2. **取消标志管理**：`registerCancelFlag` 将 `AtomicBoolean` 放入 `ConcurrentHashMap<Long, AtomicBoolean>`，key 为 `userId`。前端调用 `cancelStream(userId)` 时通过相同的 key 找到标志并设为 `true`，LLM 流读取循环中检测到后退出
3. **finally 清理**：无论执行成功还是异常，都必须移除取消标志，避免内存泄漏

**取消标志机制全景**：

```
前端用户点击"停止" → 前端 AbortController.signal → 后端 /cancel 接口
                                                          ↓
AiDialogService.cancelStream(userId)
  → cancelFlags.get(userId).set(true)
                                                          ↓
AgentTestChatService → callLLMStream → LlmSseHelper.readChunks
  → cancelFlag.get() == true → break（退出流读取循环）
```

---

## 四、AgentTestChatService — 4 步编排（核心）

**文件**：`AgentTestChatService.java`（1202 行）

这是整个 Agent 模式最核心的类，实现了 `testChatStream()` 方法，将用户消息经过 4 步处理后流式返回 LLM 回复。

### 4.1 执行流程

```
testChatStream(request, userId, emitter, authToken, response)
  │
  ├─ 前置：加载 Agent + 校验状态 + 应用场景级配置覆盖
  │
  ├─ Step 1: 提示词组装（prompt_assembly）
  │     assemblePrompt(agent, userMessage)
  │     ├─ 加载 Agent 基础提示词（agent.systemPrompt）
  │     ├─ 查询 Agent 关联的启用模板
  │     ├─ LLM 动态选择相关模板（selectPromptsByLLM）
  │     └─ 拼接选中模板内容 → 返回完整 systemPrompt
  │
  ├─ Step 2: 知识库检索（knowledge_retrieval）
  │     retrieveKnowledge(agent, userMessage)
  │     ├─ 查询 Agent 关联的已启用知识库
  │     ├─ 对每个知识库执行向量检索（topK 默认 3）
  │     └─ 拼接检索结果追加到 systemPrompt
  │
  ├─ Step 3: 技能调用（skill_execution）
  │     skillExecutor.executeSkills(agentId, userMessage, authToken, emitter)
  │     ├─ LLM 智能选择技能 → LLM 提取参数 → HTTP 调用 → 解析响应
  │     └─ 拼接技能结果追加到 systemPrompt
  │     ⚠️ 详细执行流程见 → Skill技能系统说明文档.md「四、AgentSkillExecutor」
  │
  └─ Step 4: LLM 流式调用（llm_call）
        callLLMStream(agent, messages, emitter)
        ├─ 构建消息列表：[system + history + user]
        ├─ HttpURLConnection 调用 LLM API（stream=true）
        ├─ 逐 token 通过 SSE 推送 content 事件
        └─ 返回 token 统计信息
```

### 4.2 Step 1 详解：提示词组装（prompt_assembly）

**目标**：将 Agent 基础提示词与 LLM 动态选择的模板拼接为完整的 system prompt。

**SSE 事件序列**：
```
step_start → step_progress(加载基础提示词) → step_detail(agent_prompt)
→ step_progress(LLM选择模板) → step_detail(template_loaded) × N
→ step_progress(完成) → step_done
```

**主流程编排代码（testChatStream 方法中，第 863-896 行）**：

```java
// ===== Step 1: 提示词组装 =====
String systemPrompt;
try {
    // 1. 发送步骤开始事件（前端据此在时间线中新增一个 running 节点）
    SseEvent.send(emitter, "step_start", Map.of(
            "stepIndex", stepIndex,
            "stepType", "prompt_assembly",
            "stepName", "提示词组装"
    ));

    // 2. 注册 SSE 关闭回调：客户端断连时设置标记，用于中断后续 LLM 流
    emitter.onCompletion(() -> {
        isCompleted.set(true);
    });

    // 3. 调用 assemblePrompt 方法执行实际组装
    AgentTraceStepResult stepResult = assemblePrompt(
        agent, stepIndex, extractMessage(request), emitter,
        userId, request.getSceneId(), agent.getId(), request.getRequestId());
    systemPrompt = stepResult.output;           // 组装后的完整提示词
    stepIndex = stepResult.nextStepIndex;
    steps.add(stepResult.step);                 // 记录链路追踪步骤

    // 4. 发送步骤完成事件
    SseEvent.send(emitter, "step_done", Map.of(
            "stepIndex", stepResult.step.getStepIndex(),
            "stepType", "prompt_assembly",
            "stepName", "提示词组装",
            "status", "success",
            "durationMs", stepResult.step.getDurationMs()
    ));
} catch (Exception e) {
    // 提示词组装失败 → 直接终止（没有提示词 LLM 无法工作）
    steps.add(buildFailStep(stepIndex++, "prompt_assembly", "提示词组装", e.getMessage()));
    SseEvent.sendError(emitter, "提示词组装失败: " + e.getMessage());
    sendDoneAndClose(emitter, response, "提示词组装失败");
    return;  // 注意：直接 return，不继续后续步骤
}
```

**assemblePrompt 方法详解（第 354-496 行）**：

```java
private AgentTraceStepResult assemblePrompt(Agent agent, int stepIndex, String userMessage,
                                             SseEmitter emitter, Long userId, Long sceneId,
                                             Long agentId, String requestId) {
    long stepStart = System.currentTimeMillis();
    StringBuilder systemPromptBuilder = new StringBuilder();

    // ─── 1. 加载 Agent 基础提示词 ───
    SseEvent.send(emitter, "step_progress", Map.of(
            "stepIndex", stepIndex,
            "message", "正在加载 Agent 基础提示词..."
    ));

    if (agent.getSystemPrompt() != null && !agent.getSystemPrompt().isBlank()) {
        systemPromptBuilder.append(agent.getSystemPrompt());
        // 发送详情：前端展示基础提示词内容（折叠面板）
        SseEvent.send(emitter, "step_detail", Map.of(
                "stepIndex", stepIndex,
                "detailType", "agent_prompt",
                "content", agent.getSystemPrompt(),
                "contentLength", agent.getSystemPrompt().length()
        ));
    }

    // ─── 2. 查询 Agent 关联的启用模板 ───
    List<AgentPrompt> enabledPrompts = agentPromptMapper.selectByAgentId(agent.getId())
        .stream()
        .filter(p -> p.getEnabled() != null && p.getEnabled() == 1)     // 只取启用的
        .sorted(Comparator.comparingInt(p -> p.getSortOrder() != null ? p.getSortOrder() : 0))
        .collect(Collectors.toList());

    // 批量查询模板详情（避免 N+1 查询）
    List<Long> templateIds = enabledPrompts.stream()
        .map(AgentPrompt::getTemplateId)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    if (!templateIds.isEmpty()) {
        Map<Long, PromptTemplate> templateMap = promptTemplateMapper.selectByIds(templateIds)
            .stream()
            .collect(Collectors.toMap(PromptTemplate::getId, t -> t, (a, b) -> a));

        // 收集所有可用模板
        List<PromptTemplate> allTemplates = new ArrayList<>();
        for (AgentPrompt ap : enabledPrompts) {
            PromptTemplate template = templateMap.get(ap.getTemplateId());
            if (template != null && template.getContent() != null && !template.getContent().isBlank()) {
                allTemplates.add(template);
            }
        }

        // ─── 3. LLM 动态选择相关模板 ───
        if (!allTemplates.isEmpty() && userMessage != null && !userMessage.isBlank()) {
            selectedTemplates = selectPromptsByLLM(userMessage, allTemplates, config,
                    userId, sceneId, agentId, requestId);

            SseEvent.send(emitter, "step_progress", Map.of(
                    "stepIndex", stepIndex,
                    "message", "LLM 选择了 " + selectedTemplates.size() + " 个相关模板（共 " + allTemplates.size() + " 个）"
            ));
        }

        // ─── 4. 拼接选中模板内容 ───
        for (PromptTemplate template : selectedTemplates) {
            systemPromptBuilder.append("\n\n").append(template.getContent());

            // 发送每个模板的加载详情
            SseEvent.send(emitter, "step_detail", Map.of(
                    "stepIndex", stepIndex,
                    "detailType", "template_loaded",
                    "templateId", template.getId(),
                    "templateName", template.getName(),
                    "contentLength", template.getContent().length(),
                    "contentPreview", template.getContent().substring(0,
                        Math.min(100, template.getContent().length())) + "..."
            ));
        }
    }

    // 5. 校验最终结果
    String systemPrompt = systemPromptBuilder.toString();
    if (systemPrompt.isBlank()) {
        throw new RuntimeException("Agent 系统提示词为空: " + agent.getName());
    }

    // 6. 构建链路追踪步骤
    AgentTraceStep step = new AgentTraceStep();
    step.setStepType("prompt_assembly");
    step.setStatus("success");
    step.setDurationMs(System.currentTimeMillis() - stepStart);
    step.setInput(agent.getSystemPrompt());   // 输入：原始提示词
    step.setOutput(systemPrompt);             // 输出：组装后的完整提示词
    // metadata 记录模板详情，前端可展示
    Map<String, Object> meta = new HashMap<>();
    meta.put("templateCount", enabledPrompts.size());
    meta.put("templateDetails", templateDetails);
    meta.put("totalPromptLength", systemPrompt.length());
    step.setMetadata(meta);
    // ...
}
```

**selectPromptsByLLM 方法详解（第 223-337 行）**：

这是 Step 1 的核心——让 LLM 从多个模板中智能选择最相关的模板：

```java
private List<PromptTemplate> selectPromptsByLLM(String userMessage,
                                                 List<PromptTemplate> allTemplates,
                                                 AIProperties.ProviderConfig config, ...) {
    // 优化：只有 1 个或没有模板时，跳过 LLM 调用
    if (allTemplates.size() <= 1) {
        return allTemplates;
    }

    // 1. 构建模板列表描述（供 LLM 参考）
    StringBuilder templateListDesc = new StringBuilder("[");
    for (int i = 0; i < allTemplates.size(); i++) {
        PromptTemplate template = allTemplates.get(i);
        // 提取模板内容前 100 字符作为预览
        String contentPreview = template.getContent().length() > 100
                ? template.getContent().substring(0, 100) + "..."
                : template.getContent();
        templateListDesc.append("{\"id\":").append(template.getId())
                .append(",\"name\":\"").append(template.getName())
                .append("\",\"description\":\"").append(contentPreview).append("\"}");
    }
    templateListDesc.append("]");

    // 2. 构建 System Prompt（指示 LLM 返回 JSON 数组）
    String systemPrompt = "你是一个模板选择器。根据用户消息，从模板列表中选出与用户意图最相关的模板。\n\n"
            + "可用模板：\n" + templateListDesc + "\n\n"
            + "规则：\n"
            + "1. 只返回 JSON 数组，包含选中模板的 ID，如 [1, 3]\n"
            + "2. 根据用户意图选择最相关的模板，可以选多个\n"
            + "3. 如果用户意图不明确或与所有模板无关，返回所有模板的 ID\n"
            + "4. 不要返回任何解释文字、markdown 标记或其他内容\n\n"
            + "用户消息：" + userMessage;

    // 3. 构建 LLM 请求体
    List<Map<String, String>> messages = new ArrayList<>();
    messages.add(Map.of("role", "system", "content", systemPrompt));

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", config.getChatModel());
    requestBody.put("messages", messages);
    requestBody.put("stream", false);     // 非流式：需要完整结果
    requestBody.put("temperature", 0);    // temperature=0：确保选择稳定

    // 4. 调用 LLM（同步非流式）
    Map<String, Object> apiResponse = llmSseHelper.callSync(url, config.getApiKey(), requestBody);

    // 5. 解析 LLM 返回的模板 ID 列表
    String content = choices.get(0).get("message").get("content").trim();
    // 处理可能的 markdown 代码块包裹
    if (content.startsWith("```")) {
        content = content.replaceAll("^```(json)?\\s*", "").replaceAll("\\s*```$", "");
    }
    List<Integer> selectedIds = objectMapper.readValue(content, List.class);

    // 6. 根据 ID 过滤出选中的模板
    Set<Integer> idSet = new HashSet<>(selectedIds);
    return allTemplates.stream()
            .filter(t -> idSet.contains(t.getId().intValue()))
            .collect(Collectors.toList());

    // 降级：LLM 调用异常时返回全部模板
}
```

**代码分析总结**：
- **LLM 调用次数**：Step 1 会触发 1 次 LLM 非流式调用（`stream=false, temperature=0`）
- **输入**：用户消息 + 所有模板的 id/name/内容预览
- **输出**：选中的模板 ID 数组 `[1, 3]`
- **降级策略**：模板 ≤1 个跳过 LLM；LLM 异常时返回全部模板
- **性能优化**：`promptTemplateMapper.selectByIds` 批量查询，避免 N+1 问题
- **链路追踪**：记录原始提示词长度、选中模板数量、组装后总长度

**示例**：
```
Agent 关联了 3 个模板：[财务分析模板, 新闻摘要模板, 代码助手模板]
用户消息："帮我分析一下贵州茅台的财务数据"
→ LLM 返回：[1]（选中财务分析模板）
→ systemPrompt = 基础提示词 + 财务分析模板内容
```

### 4.3 Step 2 详解：知识库检索（knowledge_retrieval）

**目标**：从 Agent 关联的向量知识库中检索与用户问题相关的内容（RAG）。

**SSE 事件序列**：
```
step_start → step_progress(检索知识库) → step_progress(检索知识库: XXX)
→ step_detail(knowledge_result) × N → step_done
```

**主流程编排代码（testChatStream 方法中，第 898-926 行）**：

```java
// ===== Step 2: 知识库检索 =====
String knowledgeContext = "";
try {
    // 1. 发送步骤开始事件
    SseEvent.send(emitter, "step_start", Map.of(
            "stepIndex", stepIndex,
            "stepType", "knowledge_retrieval",
            "stepName", "知识库检索"
    ));

    // 2. 调用 retrieveKnowledge 方法执行向量检索
    AgentTraceStepResult stepResult = retrieveKnowledge(
        agent, extractMessage(request), stepIndex, emitter);
    knowledgeContext = stepResult.output;
    stepIndex = stepResult.nextStepIndex;
    steps.add(stepResult.step);

    // 3. 拼接检索结果到 systemPrompt
    if (knowledgeContext != null && !knowledgeContext.isBlank()) {
        systemPrompt += "\n\n【参考知识】\n" + knowledgeContext;
    }

    // 4. 发送步骤完成事件
    SseEvent.send(emitter, "step_done", Map.of(
            "stepIndex", stepResult.step.getStepIndex(),
            "stepType", "knowledge_retrieval",
            "stepName", "知识库检索",
            "status", stepResult.step.getStatus(),  // 可能是 "skip"（无关联知识库）
            "durationMs", stepResult.step.getDurationMs()
    ));
} catch (Exception e) {
    // 知识库检索失败 → 不终止，继续执行后续步骤（容错设计）
    steps.add(buildFailStep(stepIndex++, "knowledge_retrieval", "知识库检索", e.getMessage()));
    log.warn("知识库检索失败，继续执行: {}", e.getMessage());
    // 注意：这里没有 return，会继续执行 Step 3 和 Step 4
}
```

**代码分析**：
- 与 Step 1 不同，Step 2 失败时**不会 return 终止流程**，只是 `log.warn` 后继续
- 这是因为知识库是增强能力，即使检索失败也不影响核心对话

**retrieveKnowledge 方法详解（第 510-602 行）**：

```java
private AgentTraceStepResult retrieveKnowledge(Agent agent, String query,
                                               int stepIndex, SseEmitter emitter) {
    long stepStart = System.currentTimeMillis();
    StringBuilder knowledgeBuilder = new StringBuilder();
    int totalChunks = 0;

    // ─── 1. 通知前端开始检索 ───
    SseEvent.send(emitter, "step_progress", Map.of(
            "stepIndex", stepIndex,
            "message", "正在检索关联的知识库..."
    ));

    // ─── 2. 查询 Agent 关联的已启用知识库 ───
    List<AgentKnowledge> enabledKnowledge = agentKnowledgeMapper.selectByAgentId(agent.getId())
        .stream()
        .filter(k -> k.getEnabled() != null && k.getEnabled() == 1)  // 只取启用的
        .sorted(Comparator.comparingInt(k -> k.getSortOrder() != null ? k.getSortOrder() : 0))
        .collect(Collectors.toList());

    // ─── 3. 逐个知识库执行向量检索 ───
    for (AgentKnowledge ak : enabledKnowledge) {
        String knowledgeName = ak.getKnowledgeName() != null ? ak.getKnowledgeName() : "知识库";

        // 通知前端正在检索哪个知识库
        SseEvent.send(emitter, "step_progress", Map.of(
                "stepIndex", stepIndex,
                "message", "正在检索知识库: " + knowledgeName
        ));

        // topK 来自数据库配置（ai_agent_knowledge.retrieval_count），默认 3
        int topK = ak.getRetrievalCount() != null ? ak.getRetrievalCount() : 3;

        // 核心调用：向量检索
        List<RetrievalResult> results = knowledgeBaseService.retrieve(
            ak.getKnowledgeId(), query, topK);

        // ─── 4. 拼接检索结果 ───
        if (!results.isEmpty()) {
            knowledgeBuilder.append("【").append(knowledgeName).append("】\n");
            for (RetrievalResult r : results) {
                knowledgeBuilder.append("- ").append(r.getContent()).append("\n");
                totalChunks++;
            }
            knowledgeBuilder.append("\n");
        }

        // ─── 5. 发送检索详情给前端 ───
        SseEvent.send(emitter, "step_detail", Map.of(
                "stepIndex", stepIndex,
                "detailType", "knowledge_result",
                "knowledgeId", ak.getKnowledgeId(),
                "knowledgeName", knowledgeName,
                "foundCount", results.size(),
                "chunks", results.isEmpty() ? List.of() : results.stream()
                    .map(r -> Map.<String, Object>of(
                        "contentPreview", r.getContent().length() > 200
                            ? r.getContent().substring(0, 200) + "..."
                            : r.getContent(),
                        "score", r.getScore() != null ? r.getScore() : 0.0
                    )).collect(Collectors.toList())
        ));
    }

    // ─── 6. 构建链路追踪步骤 ───
    AgentTraceStep step = new AgentTraceStep();
    step.setStepType("knowledge_retrieval");
    step.setStatus(totalChunks > 0 ? "success" : "skip");
    step.setDurationMs(System.currentTimeMillis() - stepStart);
    step.setOutput(knowledgeBuilder.toString());
    Map<String, Object> meta = new HashMap<>();
    meta.put("knowledgeCount", enabledKnowledge.size());  // 知识库数量
    meta.put("chunkCount", totalChunks);                    // 检索到的片段总数
    meta.put("knowledgeDetails", knowledgeDetails);         // 每个知识库的详情
    step.setMetadata(meta);
    // ...
}
```

**代码分析总结**：
- **检索策略**：逐个知识库独立检索，每个知识库返回 topK 个片段
- **topK 来源**：`ai_agent_knowledge.retrieval_count` 字段，可在 Agent 配置中调整
- **容错设计**：单个知识库检索失败不影响其他知识库，全部失败也不影响后续步骤
- **结果格式**：每个知识库的检索结果用 `【知识库名称】` 包裹，片段用 `- ` 前缀
- **前端交互**：每个知识库检索完都发送 `step_detail` 事件，前端可实时展示检索到的片段
- **链路追踪**：记录知识库数量、片段总数、每个片段的内容预览和相关度分数

**检索结果拼接格式**：
```
【公司知识库】
- 贵州茅台2024年第三季度营收约XXX亿元...
- 贵州茅台主要产品为飞天茅台，出厂价XXX元...

【行业报告库】
- 白酒行业2024年整体增速放缓，高端白酒市场...
```

### 4.4 Step 3 详解：技能调用（skill_execution）

**目标**：LLM 智能选择技能 → 提取参数 → HTTP 调用技能接口 → 返回结果。

> ⚠️ **此步骤的完整执行流程（LLM 选型、参数提取、HTTP 请求策略、响应解析）详见**：
> `Skill技能系统说明文档.md` → 四、AgentSkillExecutor — 技能执行引擎

**在 Agent 对话中的位置**：

```java
// AgentTestChatService 调用技能执行器
SkillExecuteResult skillResult = skillExecutor.executeSkills(
    agentId, userMessage, authToken, emitter);

// 技能结果追加到 systemPrompt
if (skillResult != null && !skillResult.getOutput().isBlank()) {
    systemPrompt += "\n\n【技能执行结果】\n" + skillResult.getOutput();
}
```

**最终 systemPrompt 构成**：
```
systemPrompt = 基础提示词
             + 模板内容（Step 1 选中的）
             + 参考知识（Step 2 检索的）
             + 技能执行结果（Step 3 调用的）
```

### 4.5 Step 4 详解：LLM 流式调用（llm_call）

**目标**：将组装好的完整 system prompt 发送给 LLM，流式返回回复。

**消息构建**：
```java
List<Map<String, String>> messages = new ArrayList<>();

// 1. System Prompt（已组装完毕）
messages.add({ role: "system", content: systemPrompt });

// 2. 历史消息（如果启用记忆）
if (agent.memoryEnabled == 1 && history != null) {
    int window = agent.memoryWindow != null ? agent.memoryWindow : 20;
    messages.addAll(history.subList(history.size() - window*2, history.size()));
}

// 3. 当前用户消息
messages.add({ role: "user", content: userMessage });
```

**流式调用（callLLMStream）**：
```java
// 构建请求体
requestBody.put("model", modelName);
requestBody.put("messages", messages);
requestBody.put("stream", true);        // 启用流式
requestBody.put("temperature", agent.getTemperature());
requestBody.put("max_tokens", agent.getMaxTokens());

// 通过 LlmSseHelper 流式读取
HttpURLConnection connection = llmSseHelper.createConnection(url, apiKey, requestBody);
llmSseHelper.readChunks(connection, isCompleted, chunk -> {
    if (chunk.hasDeltaContent()) {
        replyContentBuilder.append(chunk.getDeltaContent());
        // 逐 token 发送给前端
        SseEvent.send(emitter, "content", SseEvent.content(chunk.getDeltaContent()));
    }
});
```

### 4.6 完成：组装快照 + 关闭 SSE + 保存记录

```java
// 1. 组装链路追踪快照
AgentTraceSnapshot snapshot = buildTraceSnapshot(agent, steps, totalDuration, ...);

// 2. 先发送 done 事件（关闭 SSE 连接）
SseEvent.send(emitter, "done", Map.of("traceSnapshot", snapshot, "tokens", tokens, "durationMs", totalDuration));

// 3. 强制刷新并关闭响应
response.flushBuffer();
response.getOutputStream().close();
emitter.complete();

// 4. 异步保存执行记录到 DB（不影响 SSE 响应）
saveTestSession(agent, request, replyContent, snapshot, ...);
```

**关键设计**：必须先 `done` + 关闭 SSE，再做 DB 操作，否则 DB 异常会导致前端永远收不到 `done` 事件。

---

## 五、LLM 流式调用底层（LlmSseHelper）

### 5.1 文件说明

`LlmSseHelper.java`（230 行）封装了与 LLM API 的 HTTP 连接和流式读取，供所有需要调用 LLM 的地方使用。

### 5.2 核心方法

| 方法 | 说明 |
|--|--|
| `createConnection(url, apiKey, requestBody)` | 创建 HttpURLConnection，30s 连接超时 / 5min 读取超时 |
| `readChunks(connection, cancelFlag, onChunk)` | 逐行读取 SSE 流，解析 `data:` 行，处理 `[DONE]` 结束标记 |
| `parseChunk(chunk)` | 从 LLM 响应中提取 `delta.content` 和 `usage`（token 用量） |

### 5.3 流式读取逻辑

```
BufferedReader 逐行读取
  │
  ├─ line.startsWith("data:")
  │     ├─ "[DONE]" → 流结束，退出循环
  │     └─ 解析 JSON → 提取 choices[0].delta.content → 回调 onChunk
  │
  ├─ cancelFlag.get() == true → 用户取消，退出循环
  │
  └─ 其他行 → 跳过
```

---

## 六、SSE 事件协议

### 6.1 事件类型（SseEvent.java）

| 类型 | 说明 | 触发时机 |
|--|--|--|
| `step_start` | 步骤开始 | 每步开始时 |
| `step_progress` | 步骤进度 | 步骤执行中，附带进度消息 |
| `step_detail` | 步骤详情 | 携带具体数据（提示词/模板/检索结果/技能参数等） |
| `step_done` | 步骤完成 | 每步结束时，附带耗时和状态 |
| `content` | 流式文本 | Step 4 LLM 流式输出时，逐 token 发送 |
| `done` | 对话完成 | 全部流程结束，附带 traceSnapshot 和 token 统计 |
| `error` | 错误 | 异常时发送 |
| `stop` | 手动停止 | 用户中断时发送 |

### 6.2 完整事件时序

```
前端                                    后端
  │                                      │
  │── POST /agent-stream ───────────────>│
  │   {mode, agentId, question}          │
  │                                      │
  │<── step_start(prompt_assembly) ──────│  Step 1 开始
  │<── step_progress(加载基础提示词) ────│
  │<── step_detail(agent_prompt) ────────│
  │<── step_progress(LLM选择模板) ──────│
  │<── step_detail(template_loaded) ─────│  每个选中模板
  │<── step_done(prompt_assembly) ───────│  Step 1 完成
  │                                      │
  │<── step_start(knowledge_retrieval) ──│  Step 2 开始
  │<── step_progress(检索知识库) ────────│
  │<── step_detail(knowledge_result) ────│  检索结果
  │<── step_done(knowledge_retrieval) ───│  Step 2 完成
  │                                      │
  │<── step_start(skill_execution) ──────│  Step 3 开始
  │<── step_progress(选择/调用技能) ─────│
  │<── step_detail(skill_params) ────────│
  │<── step_detail(skill_result) ────────│
  │<── step_done(skill_execution) ───────│  Step 3 完成
  │                                      │
  │<── step_start(llm_call) ─────────────│  Step 4 开始
  │<── content("你") ────────────────────│
  │<── content("好") ────────────────────│  逐 token 流式
  │<── content("！") ────────────────────│
  │<── content(...) ─────────────────────│
  │<── done(traceSnapshot, tokens) ──────│  对话完成
  │                                      │
  │  ← 流关闭 →                          │
```

---

## 七、前端代码结构

### 7.1 文件清单

```
views/aiModule/agent/components/
├── test-chat/                          ← Agent 测试对话区域
│   ├── ChatPanel.vue                   ← 对话面板（消息列表 + 输入框 + SSE 逻辑）
│   ├── MessageBubble.vue               ← 消息气泡（用户/助手消息渲染）
│   ├── StepTimeline.vue                ← 步骤进度时间线（展示 4 步执行状态）
│   └── StepDetail.vue                  ← 步骤详情（技能参数/结果的折叠展示）
├── AgentConfigDialog.vue               ← Agent 配置弹窗
├── AgentConfigSkillTab.vue             ← 技能绑定 Tab
└── AgentConfigKnowledgeTab.vue         ← 知识库绑定 Tab

components/AiAssistant/
├── index.vue                           ← 助手外壳（可拖拽、可折叠的浮动面板）
└── components/
    ├── ChatPanel.vue                   ← 对话面板（含场景选择器）
    ├── SceneSelector.vue               ← 场景选择器
    ├── MessageBubble.vue               ← 消息气泡
    ├── StepTimeline.vue                ← 步骤时间线
    └── TracePanel.vue                  ← 链路追踪面板

hooks/
└── useChatExecution.ts                 ← 主 AI 助手的统一对话执行 composable

api/ai/
├── chatExecute.ts                      ← 公共 SSE 流式对话 API（executeAgentStream）
└── types/
    └── agent.ts                        ← TypeScript 类型定义
```

### 7.2 页面布局

**Agent 测试对话面板**（ChatPanel.vue）：

```
┌─────────────────────────────────────────────────┐
│ ┌─────────────────────────────────────────────┐  │
│ │                 消息列表                     │  │
│ │                                             │  │
│ │  ┌───────────────────┐                      │  │
│ │  │ 用户消息           │                      │  │
│ │  └───────────────────┘                      │  │
│ │                         ┌─────────────────┐ │  │
│ │                         │ 助手回复         │ │  │
│ │                         │                 │ │  │
│ │                         │ [步骤时间线]     │ │  │
│ │                         │ ├ ✓ 提示词组装   │ │  │
│ │                         │ ├ ✓ 知识库检索   │ │  │
│ │                         │ ├ ✓ 技能调用     │ │  │
│ │                         │ └ ● 回复生成     │ │  │
│ │                         └─────────────────┘ │  │
│ │                                             │  │
│ ├─────────────────────────────────────────────┤  │
│ │ [输入框]                          [发送]     │  │
│ └─────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

### 7.3 SSE 事件处理（ChatPanel.vue）

```typescript
function handleSSEEvent(event: AgentTestChatSSEEvent) {
  switch (event.type) {
    case 'step_start':      // 新增一个 running 步骤到时间线
    case 'step_progress':   // 追加进度消息到当前步骤
    case 'step_detail':     // 收集详情数据到 detailList
    case 'step_done':       // 更新状态为 success + 记录耗时
    case 'content':         // 追加到 assistant 消息内容（逐字渲染）
    case 'done':            // 设置 token 统计、链路快照、标记 streamCompleted
    case 'error':           // 追加错误信息到消息
  }
}
```

**关键设计**：
- `streamCompleted` 标记：收到 `done` 事件后设为 `true`，通知 `while` 循环主动退出
- 步骤匹配优先用 `stepType`（如 `prompt_assembly`），因为后端 `stepIndex` 可能硬编码为 0
- `finally` 块兜底：即使 `done` 未触发，也将 running 步骤标记为 success，确保 UI 状态正确恢复

### 7.4 SSE 底层读取器（chatExecute.ts）

`readSseStream()` 是所有 SSE 流式对话的底层读取器：

```typescript
async function readSseStream(url, body, onEvent) {
  const response = await fetch(url, { method: 'POST', body: JSON.stringify(body) })
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })  // stream:true 处理多字节截断
    const lines = buffer.split('\n')
    buffer = lines.pop()!  // 最后一行可能不完整，保留到下次

    for (const line of lines) {
      if (line.startsWith('data:')) {
        const raw = line.slice(5).trim()
        if (raw) onEvent(JSON.parse(raw))
      }
    }
  }
}
```

**为什么用 fetch 而不是 EventSource？** 因为 Agent Stream 端点需要 POST 请求传递 JSON 体，而 EventSource 只支持 GET。

### 7.5 链路追踪类型定义（types/agent.ts）

```typescript
interface AgentTraceSnapshot {
  route?: 'agent' | 'orchestration' | 'llm'
  agentName?: string
  model?: string
  tokensPrompt?: number
  tokensCompletion?: number
  steps?: AgentTraceStep[]
  totalDurationMs?: number
}

interface AgentTraceStep {
  stepIndex: number
  stepType: 'prompt_assembly' | 'knowledge_retrieval' | 'skill_execution' | 'llm_call'
  stepName: string
  status: 'success' | 'fail' | 'skip'
  durationMs: number
  input?: string
  output?: string | string[]
  metadata?: Record<string, any>
}
```

---

## 八、关键设计要点

### 8.1 LLM 多次调用策略

一次完整的 Agent 对话可能涉及 **4+N 次 LLM 调用**：
- 1 次：模板选择（非流式，选出相关模板）
- 1 次：技能选择（非流式，选出匹配技能）
- N 次：技能参数提取（每个技能 1 次，非流式）
- 1 次：最终回复生成（流式）

只有最终回复是流式输出，其余都是同步调用后获取完整结果。

### 8.2 场景级配置覆盖

当 Agent 通过场景调用时，场景配置可覆盖 Agent 默认值：
- `model` → 覆盖 Agent 的 modelCode
- `temperature` → 覆盖 Agent 的 temperature
- `systemPrompt` → 追加到 Agent 基础提示词后面

优先级：**场景配置 > Agent 默认值**。

### 8.3 SSE 先完成再持久化

所有 DB 操作（保存执行记录、增加使用次数）必须在 `done` 事件发送并关闭 SSE 之后执行，防止 DB 异常导致前端永远收不到 `done` 事件。

### 8.4 AbortController + AtomicBoolean 双端取消

- 前端：通过 `AbortController.signal` 中断 fetch 请求
- 后端：通过 `AtomicBoolean cancelFlag` 中断 LLM 流读取循环
- 两者独立工作，任一端取消都能立即停止整个流程

### 8.5 每步独立 try-catch

4 步编排中每步都有独立的异常捕获：
- Step 1（提示词）失败 → 直接返回错误
- Step 2（知识库）失败 → 跳过，继续后续步骤
- Step 3（技能）失败 → 跳过，继续后续步骤
- Step 4（LLM）失败 → 返回错误

知识库和技能是增强能力，失败不应阻断核心对话流程。

### 8.6 链路追踪快照

`AgentTraceSnapshot` 记录完整调用链路的所有步骤信息（每步的输入、输出、耗时、元数据），序列化为 JSON 存入 `ai_execution_session.trace_snapshot`。前端在链路追踪面板中展示每步的执行状态和详情。

---

## 九、面试回答要点

**Q: Agent 流式对话的整体流程是什么？**

> "Agent 模式通过 4 步编排增强 LLM 能力：① 提示词组装（Agent 基础提示词 + LLM 动态选择的模板）；② 知识库检索（RAG 从向量数据库检索相关知识）；③ 技能调用（LLM 选择技能 → 提取参数 → HTTP 调用接口）；④ LLM 流式调用（将以上所有上下文注入 system prompt，流式返回回复）。每步都通过 SSE 事件实时推送进度给前端。"

**Q: LLM 在 Agent 模式中做了几次调用？**

> "一个完整的 Agent 对话可能涉及 **4+N 次 LLM 调用**：1 次模板选择、1 次技能选择、N 次技能参数提取、1 次最终回复生成。其中模板选择、技能选择、参数提取都是非流式同步调用，只有最终回复是流式的。"

**Q: SSE 流式通信是如何实现的？**

> "后端使用 Spring 的 `SseEmitter` 发送 SSE 事件，前端通过 `fetch + ReadableStream` 读取。后端异步线程池执行业务逻辑，通过 `SseEvent.send()` 逐个推送 JSON 事件。前端在 `while` 循环中逐块读取流数据，按换行符切分后解析 `data:` 前缀的 JSON 行。关键设计包括：缓冲区处理不完整行、`streamCompleted` 标记主动退出循环、`finally` 块兜底恢复 UI 状态。"

**Q: 如何处理 Agent 的多轮对话记忆？**

> "通过 Agent 配置的 `memoryEnabled` 和 `memoryWindow` 字段控制。启用记忆后，将最近 `window*2` 条历史消息（user/assistant 交替）注入 LLM 消息列表。Agent 模式与 streaming_llm 模式的区别在于：Agent 模式会额外注入完整的 system prompt（含知识和技能结果）。"

**Q: 链路追踪快照的作用是什么？**

> "`AgentTraceSnapshot` 记录了完整调用链路的所有步骤信息（每步的输入、输出、耗时、元数据），序列化为 JSON 存储到 `ai_execution_session.trace_snapshot` 字段。前端收到 `done` 事件后解析快照，在链路追踪面板中展示每步的执行状态和详情。"

