CREATE TABLE `ai_agent_test_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `agent_id` BIGINT NOT NULL COMMENT '测试的 Agent ID',
  `agent_name` VARCHAR(100) DEFAULT NULL COMMENT 'Agent 名称（冗余）',
  -- 测试输入
  `user_message` TEXT NOT NULL COMMENT '用户输入消息',
  `history_messages` JSON DEFAULT NULL COMMENT '对话历史（JSON数组）',
  -- 测试输出
  `agent_reply` TEXT DEFAULT NULL COMMENT 'Agent 回复内容',
  -- 链路追踪快照（核心字段）
  `trace_snapshot` JSON DEFAULT NULL COMMENT '完整调用链路快照，结构如下：
    {
      "steps": [
        {
          "stepIndex": 1,
          "stepType": "prompt_assembly",
          "stepName": "提示词组装",
          "status": "success",
          "durationMs": 12,
          "input": "系统提示词 + 变量替换后的完整prompt",
          "output": "最终组装的prompt内容",
          "metadata": { "templateCount": 2, "variableCount": 5 }
        },
        {
          "stepIndex": 2,
          "stepType": "knowledge_retrieval",
          "stepName": "知识库检索",
          "status": "success",
          "durationMs": 156,
          "input": "检索查询文本",
          "output": ["检索到的片段1", "检索到的片段2"],
          "metadata": { "knowledgeBaseCount": 2, "retrievedChunkCount": 6, "similarityScores": [0.92, 0.87, ...] }
        },
        {
          "stepIndex": 3,
          "stepType": "skill_execution",
          "stepName": "技能调用",
          "status": "success",
          "durationMs": 1230,
          "input": "技能参数",
          "output": "技能执行结果",
          "metadata": { "skillName": "xxx", "skillCode": "xxx" }
        },
        {
          "stepIndex": 4,
          "stepType": "llm_call",
          "stepName": "LLM 调用",
          "status": "success",
          "durationMs": 3456,
          "input": "完整prompt（含检索结果+技能输出）",
          "output": "LLM原始回复",
          "metadata": { "model": "deepseek-chat", "tokensPrompt": 1234, "tokensCompletion": 567, "temperature": 0.7 }
        }
      ],
      "totalDurationMs": 4862,
      "totalTokens": { "prompt": 1234, "completion": 567 }
    }',
  -- 汇总指标
  `total_duration_ms` INT DEFAULT 0 COMMENT '总耗时（毫秒）',
  `tokens_prompt` INT DEFAULT 0 COMMENT '提示词 Token 数',
  `tokens_completion` INT DEFAULT 0 COMMENT '补全 Token 数',
  `model_name` VARCHAR(50) DEFAULT NULL COMMENT '使用的模型',
  `status` VARCHAR(20) DEFAULT 'success' COMMENT '状态：success/fail/timeout',
  `error_message` VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
  -- 管理
  `created_by` BIGINT DEFAULT NULL COMMENT '测试人',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '测试时间',
  PRIMARY KEY (`id`),
  KEY `idx_agent_id` (`agent_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 测试会话（含调用链路追踪）';