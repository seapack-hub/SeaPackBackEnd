# Skill 技能系统说明文档

## 一、系统概述

Skill 技能系统是 AI Agent 的核心能力之一，让 Agent 能够"调用外部工具"来获取信息或执行操作。

**核心概念**：Agent 不再只是"聊天"，而是能主动调用 API 获取实时数据（如股票行情、新闻资讯），然后将结果喂给 LLM 生成回答。

---

## 二、数据库表结构（4 张表）

### 2.1 ai_skill — 技能定义表（核心）

| 字段 | 类型 | 说明 |
|--|--|--|
| id | BIGINT | 主键 |
| category_id | BIGINT | 所属分类 ID（关联 ai_skill_category） |
| name | VARCHAR(100) | 技能名称，如"股票行情查询" |
| code | VARCHAR(50) | 技能编码，唯一标识，如 `stock_quote` |
| icon | VARCHAR(100) | 图标 SVG 文件名 |
| description | VARCHAR(500) | 技能功能描述 |
| skill_type | VARCHAR(30) | 技能类型：`tool`=工具调用 / `rag`=知识检索 / `hybrid`=混合 |
| endpoint | VARCHAR(200) | **调用端点**（后端 API 路径如 `/stockInfo/page`，或外部 URL） |
| timeout_ms | INT | 调用超时，默认 30000ms |
| input_schema | JSON | **输入参数 JSON Schema**（LLM 据此提取参数） |
| status | TINYINT | 1=启用 0=禁用 |
| sort_order | INT | 排序号 |
| use_count | INT | 使用次数统计 |
| version | VARCHAR(20) | 版本号，如 v1.0.0 |

### 2.2 ai_skill_category — 技能分类表

| 字段 | 说明 |
|--|--|
| id | 主键 |
| name | 分类名称，如"内容生成"、"数据分析" |
| code | 分类编码，唯一 |
| icon | 图标 |
| description | 描述 |
| sort_order | 排序号 |
| status | 1=启用 0=禁用 |

### 2.3 ai_skill_param — 技能参数表

| 字段 | 说明 |
|--|--|
| id | 主键 |
| skill_id | 所属技能 ID |
| param_name | 参数名，如 `stockCode` |
| label | 参数标签，如"股票代码" |
| param_type | 类型：`string` / `number` / `boolean` / `select` / `json` |
| required | 1=必填 0=可选 |
| default_value | 默认值 |
| options | select 类型的选项列表 JSON `[{"label":"沪市","value":"SH"}]` |
| placeholder | 输入框提示文字 |
| sort_order | 排序号 |

### 2.4 ai_agent_skill — Agent 关联技能表（多对多）

| 字段 | 说明 |
|--|--|
| id | 主键 |
| agent_id | Agent ID |
| skill_id | 技能 ID |
| enabled | 1=启用 0=禁用 |
| is_primary | 1=主技能 0=辅助技能 |
| sort_order | 排序号 |

**关系图**：
```
ai_skill_category (1) ←── (N) ai_skill (1) ←── (N) ai_agent_skill (N) →── (1) ai_agent
                                       ↑
                               ai_skill_param (N)
```

---

## 三、后端代码结构

### 3.1 文件清单

```
controller/ai/
├── SkillController.java           ← 技能 CRUD 接口
├── SkillCategoryController.java   ← 分类 CRUD 接口
service/ai/
├── SkillService.java              ← 技能业务逻辑
├── SkillParamService.java         ← 参数业务逻辑
├── SkillCategoryService.java      ← 分类业务逻辑
└── AgentSkillExecutor.java        ← 【核心】技能执行引擎
mapper/ai/
├── SkillMapper.java + SkillMapper.xml
├── SkillParamMapper.java + SkillParamMapper.xml
├── SkillCategoryMapper.java + SkillCategoryMapper.xml
└── AgentSkillMapper.java + AgentSkillMapper.xml
model/ai/
├── Skill.java                     ← 技能实体
├── SkillCategory.java             ← 分类实体
├── SkillParam.java                ← 参数实体
└── AgentSkill.java                ← Agent-技能关联实体
dto/ai/
└── SkillExecuteResult.java        ← 技能执行结果 DTO
```

### 3.2 SkillController — 技能 CRUD 接口

**基础路径**：`/ai/skills`

| 方法 | 路径 | 说明 |
|--|--|--|
| GET | `/all` | 全量技能列表（不分页，用于下拉选择） |
| GET | `/page/list` | 分页查询（支持按分类、类型、状态、关键词筛选） |
| GET | `/detail/{id}` | 查询技能详情 |
| POST | `/insert` | 新增技能（校验 code 唯一性） |
| POST | `/update` | 更新技能 |
| POST | `/delete/{id}` | 删除技能（级联删除参数） |
| GET | `/{skillId}/params` | 获取技能的参数列表 |
| POST | `/{skillId}/add-param` | 为技能新增参数 |
| POST | `/{skillId}/update-param` | 更新参数 |
| POST | `/{skillId}/delete-param/{paramId}` | 删除参数 |

### 3.3 SkillService — 技能业务逻辑

- `getAll(status)` — 查全量列表
- `getList(...)` — 分页查询（使用 PageHelper）
- `getById(id)` — 查详情
- `isCodeDuplicate(code, excludeId)` — 校验编码唯一性
- `insert(skill)` — 新增（空 inputSchema 转 null）
- `update(skill)` — 更新
- `deleteById(id)` — 删除（**级联删除参数**：先删 ai_skill_param，再删 ai_skill）

---

## 四、AgentSkillExecutor — 技能执行引擎（核心）

**文件**：`AgentSkillExecutor.java`（619 行）

这是整个 Skill 系统最核心的类，负责**在 Agent 对话过程中自动执行技能**。

### 4.1 执行流程

```
executeSkills(agentId, userMessage, authToken, emitter)
  │
  ├─ 1. 查询 Agent 关联的已启用技能
  │     agentSkillMapper.selectByAgentId(agentId)
  │     → 过滤 enabled=1 → 按 sortOrder 排序
  │
  ├─ 2. LLM 智能选择技能（selectSkillsByLLM）
  │     ├─ 如果只有 0~1 个技能 → 跳过选择，直接用
  │     ├─ 构建技能列表描述（code + name + description）
  │     ├─ System Prompt："你是一个技能选择器，选出最匹配的技能，返回 JSON 数组"
  │     ├─ 调用 LLM（stream=false, temperature=0）
  │     └─ 解析返回的 code 数组，过滤出选中的技能
  │
  ├─ 3. 逐个执行选中的技能
  │     for each skill:
  │       ├─ 加载完整 Skill 记录（skillMapper.selectById）
  │       ├─ 校验：status=1 且 endpoint 不为空
  │       │
  │       ├─ 4. LLM 提取参数（extractParamsByLLM）
  │       │     ├─ 根据 skill.inputSchema + userMessage
  │       │     ├─ System Prompt："你是一个参数提取器，从用户消息中提取参数"
  │       │     ├─ 调用 LLM → 返回结构化 JSON 参数
  │       │     └─ 失败降级：返回 { keywords: userMessage }
  │       │
  │       ├─ 5. 判断调用方式
  │       │     ├─ endpoint 以 "/" 开头 → 内部调用（localhost:port + endpoint）
  │       │     └─ 其他 → 外部 API 调用
  │       │
  │       ├─ 6. 发起 HTTP 请求
  │       │     ├─ 先尝试 POST
  │       │     ├─ POST 失败 → 降级为 GET（自动拼接 query 参数）
  │       │     ├─ Headers：内部调用用 authToken，外部用 API Key
  │       │     └─ 内部调用自动补充 pageNum/pageSize（PageHelper 兼容）
  │       │
  │       └─ 7. 解析响应
  │             extractResponseData() 适配多种格式：
  │             ├─ { result: ... } 或 { data: ... }  → 标准业务返回
  │             ├─ { list: [...], total: N }          → PageHelper 分页格式
  │             └─ { records: [...] }                  → 其他格式
  │
  └─ 8. 组装返回结果（SkillExecuteResult）
        ├─ output: 拼接的文本（含调用路径、参数、返回结果）
        ├─ executedCount: 成功数
        ├─ failedCount: 失败数
        └─ skillNames: 执行的技能名称列表
```

### 4.2 LLM 选择技能（selectSkillsByLLM）

```java
// 核心逻辑：用 LLM 从多个候选技能中选出最相关的
System Prompt = "你是一个技能选择器。根据用户消息，从技能列表中选出最匹配的技能。
                 返回 JSON 数组，包含选中技能的 code。"

// 示例：
用户消息："帮我查一下贵州茅台今天的股价"
LLM 返回：["stock_quote"]

// LLM 只看技能的 code + name + description 来判断，不看 endpoint 等实现细节
```

**降级策略**：LLM 调用异常 → 返回全部技能 → 逐个执行

### 4.3 LLM 提取参数（extractParamsByLLM）

```java
// 根据 inputSchema 从用户消息中提取结构化参数
System Prompt = "你是一个参数提取器。根据 JSON Schema 定义，从用户消息中提取参数。
                 只返回 JSON 对象。"

// 示例：
inputSchema = { "stockCode": "股票代码", "market": "市场(SH/SZ)" }
用户消息 = "查一下茅台"
LLM 返回 = { "stockCode": "600519" }
```

**降级策略**：无 inputSchema 或 LLM 失败 → `{ keywords: userMessage }`

### 4.4 HTTP 请求策略

```
判断 endpoint：
├─ 以 "/" 开头 → 内部调用
│   URL = http://localhost:{serverPort}{endpoint}
│   Headers = Authorization: {authToken}（从 SSE 线程传入）
│   Body = 扁平化参数 + pageNum=1, pageSize=10
│
└─ 其他 → 外部 API 调用
    URL = endpoint（完整 URL）
    Headers = Authorization: Bearer {apiKey}
    Body = { model, query, params }

请求方式降级：
1. 先 POST
2. POST 异常或返回错误码 → 自动降级 GET（参数拼到 URL query）
```

---

## 五、Agent 对话中的技能调用位置

在 `AgentTestChatService.testChatStream()` 中，技能调用是 **Step 3**：

```
Step 1: 提示词组装（assemblePrompt）
Step 2: 知识库检索（retrieveKnowledge）
Step 3: 技能调用（skillExecutor.executeSkills）  ← 这里
Step 4: LLM 流式调用（callLLMStream）
```

**技能执行结果的流向**：
```java
// 技能执行结果追加到 systemPrompt
if (skillContext != null && !skillContext.isBlank()) {
    systemPrompt += "\n\n【技能执行结果】\n" + skillContext;
}
// 最终 systemPrompt = 基础提示词 + 模板 + 知识库 + 技能结果
```

这意味着 LLM 在生成回答时，能看到技能返回的实时数据（如股票价格），从而生成基于真实数据的回答。

---

## 六、前端代码结构

### 6.1 文件清单

```
views/aiModule/
├── skillManagement/                    ← 技能管理页面
│   ├── index.vue                       ← 主页面（左分类树 + 右技能列表）
│   ├── components/
│   │   ├── SkillCard.vue               ← 技能卡片组件
│   │   ├── SkillFormDialog.vue         ← 技能新增/编辑弹窗
│   │   ├── SkillParamEditor.vue        ← 参数管理弹窗
│   │   ├── SkillCategoryTree.vue       ← 分类树组件
│   │   └── SkillCategoryDialog.vue     ← 分类新增/编辑弹窗
│   └── utils/
│       ├── useSkill.ts                 ← 技能管理 composable（封装 CRUD 逻辑）
│       ├── moduleOptions.ts            ← 下拉选项常量（技能类型、状态等）
│       └── tableColumns.ts             ← 表格列配置
├── agent/components/
│   └── AgentConfigSkillTab.vue         ← Agent 配置中的"关联技能" Tab
api/ai/
├── skill.ts                            ← 技能 API（CRUD + 参数管理）
├── skillCategory.ts                    ← 分类 API
└── types/
    ├── skill.ts                        ← Skill / SkillParam 类型定义
    └── skillCategory.ts               ← SkillCategory 类型定义
utils/
└── skillParam.ts                       ← 参数 options 序列化/反序列化工具
```

### 6.2 页面布局

技能管理页面采用**左右分栏**布局：

```
┌─────────────────────────────────────────────────┐
│ [左侧 220px]          │ [右侧 flex-1]            │
│                        │                          │
│ ┌──────────────┐       │ ┌──────────────────────┐ │
│ │ 分类树        │       │ │ 搜索栏 + 工具栏       │ │
│ │              │       │ ├──────────────────────┤ │
│ │ ○ 全部分类   │       │ │ [卡片模式] 或 [列表]  │ │
│ │   ├ 内容生成 │       │ │                      │ │
│ │   ├ 数据分析 │       │ │ ┌────┐ ┌────┐ ┌────┐│ │
│ │   └ 工具集成 │       │ │ │卡片│ │卡片│ │卡片││ │
│ │              │       │ │ └────┘ └────┘ └────┘│ │
│ └──────────────┘       │ └──────────────────────┘ │
└─────────────────────────────────────────────────┘
```

### 6.3 useSkill composable

封装了技能管理的所有业务逻辑，供 `index.vue` 消费：

- **分类管理**：`fetchCategories()`、`onCategoriesChange()`
- **分页查询**：`handleQuery()`、`handleReset()`
- **技能表单**：`openSkillDialog()`、`onSkillFormConfirm()`
- **删除**：`onDeleteSkill()`
- **状态切换**：`onStatusChange()`
- **参数管理**：`openParamEditor()`

### 6.4 SkillFormDialog — 技能表单

字段说明：

| 字段 | 控件 | 说明 |
|--|--|--|
| name | el-input | 技能名称 |
| code | el-input | 技能编码（编辑时禁用） |
| categoryId | el-select | 所属分类（左侧选中时自动填充） |
| skillType | el-select | 技能类型：tool/rag/hybrid |
| icon | IconPicker | 图标选择器 |
| version | el-input | 版本号 |
| description | el-input textarea | 描述 |
| endpoint | el-input | API 端点 |
| timeoutMs | el-input-number | 超时时间 |
| inputSchema | JsonEditor | JSON Schema（代码编辑器） |
| sortOrder | el-input-number | 排序号 |
| status | el-switch | 启用/禁用 |

### 6.5 SkillParamEditor — 参数管理

为技能定义输入参数，支持 5 种类型：

| 类型 | 默认值控件 | 说明 |
|--|--|--|
| string | el-input | 文本输入 |
| number | el-input-number | 数字输入 |
| boolean | el-select (true/false) | 布尔选择 |
| select | el-select + 选项列表编辑 | 下拉选择 |
| json | JsonEditor | JSON 编辑器 |

### 6.6 AgentConfigSkillTab — Agent 关联技能

在 Agent 编辑页面中，通过 Tab 管理 Agent 关联的技能：

- 从全量技能列表中选择
- 设置为主技能/辅助技能
- 设置排序号和启用状态
- API：`AgentAPI.addSkill()`、`AgentAPI.updateSkill()`、`AgentAPI.deleteSkill()`

---

## 七、完整数据流

### 7.1 管理流程（后台配置）

```
管理员创建技能分类 → 创建技能 → 配置 endpoint + inputSchema → 定义参数
                                    ↓
                        在 Agent 配置中关联技能
```

### 7.2 执行流程（Agent 对话时）

```
用户发送消息："帮我查一下贵州茅台今天的股价"
  ↓
Agent 加载已关联的技能列表
  ↓
LLM 智能选择 → 选中 [stock_quote]
  ↓
LLM 提取参数 → { stockCode: "600519", market: "SH" }
  ↓
HTTP POST /stockInfo/page?stockCode=600519&market=SH
  ↓
返回股票数据 JSON
  ↓
结果追加到 systemPrompt → 【技能执行结果】...
  ↓
LLM 基于技能返回的真实数据生成回答
  ↓
"贵州茅台（600519）今日收盘价为 XXX 元，涨幅 X.XX%..."
```

---

## 八、关键设计要点

### 8.1 LLM 驱动的技能选择

不是所有关联的技能都会执行。系统通过 LLM 分析用户意图，只执行最相关的技能：
- **优点**：避免无关技能浪费时间和资源
- **降级**：LLM 选择失败时执行全部技能

### 8.2 LLM 驱动的参数提取

技能的输入参数由 LLM 从用户自然语言中自动提取：
- **优点**：用户不需要手动填表单，自然对话即可触发技能
- **Schema 驱动**：`input_schema` 字段定义了参数结构，LLM 按此提取

### 8.3 内外部调用统一

endpoint 以 `/` 开头 = 内部 API（补全 localhost + 传递 auth token）
其他 = 外部 API（使用 AI provider 的 API Key 认证）

### 8.4 POST → GET 自动降级

先尝试 POST 调用，失败后自动降级为 GET（参数拼接到 URL query），兼容不支持 POST 的 API。

### 8.5 响应格式自适应

`extractResponseData()` 适配多种后端返回格式：
- `{ result: ... }` 或 `{ data: ... }` — 标准业务返回
- `{ list: [...], total: N }` — PageHelper 分页格式
- `{ records: [...] }` — 其他格式

---

## 九、面试回答要点

**Q: 你项目中的技能系统是怎么实现的？**

> "我们的技能系统分两层：**管理层**和**执行层**。
>
> 管理层是标准的 CRUD，管理员可以创建技能、配置 API 端点和参数 Schema，然后在 Agent 配置中关联技能。
>
> 执行层是核心——`AgentSkillExecutor`。Agent 对话时，系统会先查出关联的所有技能，然后用 **LLM 智能选择**最匹配用户意图的技能（不是全部执行），再用 **LLM 从用户自然语言中自动提取参数**（基于 JSON Schema），最后发起 HTTP 调用获取结果。技能返回的数据会被追加到 System Prompt 中，LLM 基于这些实时数据生成回答。
>
> 这就实现了类似 Function Calling 的能力——Agent 能主动调用外部工具获取实时信息，而不是只靠训练数据回答。"

**Q: 为什么要用 LLM 来选择技能？**

> "因为 Agent 可能关联了多个技能，但用户的一次提问可能只需要其中某一个。如果全部执行会浪费资源和时间。让 LLM 根据用户意图智能选择，既精准又高效。而且 LLM 选择失败时有降级策略——执行全部技能。"

**Q: 参数是怎么从用户消息中提取的？**

> "我们定义了 `inputSchema`，本质是 JSON Schema，描述了技能需要哪些参数。LLM 根据这个 Schema 从用户的自然语言中提取结构化参数。比如用户说'查一下茅台'，LLM 能提取出 `{ stockCode: '600519' }`。提取失败时会降级为 `{ keywords: 用户原话 }`。"
