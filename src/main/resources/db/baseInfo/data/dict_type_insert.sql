-- ============================================================
-- 新增字典类型数据（共 28 个类型）
-- 前置条件：sys_dict_type 表已创建，sys_dict 中已有数据
-- ============================================================

-- ==========================================
-- 1. 通用状态（多模块复用：Agent/场景/技能/提示词/工作流）
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('common_status', '通用状态', '启用/禁用通用状态，多模块复用', 1, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('common_status', '1', '启用', 1, '1', '启用状态'),
('common_status', '0', '禁用', 2, '1', '禁用状态');

-- ==========================================
-- 2. AI 模型
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('ai_model', 'AI模型', '系统支持的AI大模型列表', 2, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('ai_model', 'deepseek-chat', 'DeepSeek Chat', 1, '1', '深度求索通用对话模型'),
('ai_model', 'deepseek-coder', 'DeepSeek Coder', 2, '1', '深度求索代码专用模型'),
('ai_model', 'gpt-4o', 'GPT-4o', 3, '1', 'OpenAI多模态模型'),
('ai_model', 'gpt-4o-mini', 'GPT-4o Mini', 4, '1', 'OpenAI轻量模型'),
('ai_model', 'qwen-plus', '通义千问', 5, '1', '阿里云通义千问'),
('ai_model', 'glm-4', 'GLM-4', 6, '1', '智谱AI大模型'),
('ai_model', 'mimo-v2.5', 'MiMo-v2.5', 7, '1', '小米MiMo模型');

-- ==========================================
-- 3. AI 输出格式
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('ai_output_format', 'AI输出格式', 'AI模型输出的数据格式', 3, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('ai_output_format', 'markdown', 'Markdown', 1, '1', 'Markdown格式'),
('ai_output_format', 'json', 'JSON', 2, '1', 'JSON格式'),
('ai_output_format', 'text', '纯文本', 3, '1', '纯文本格式'),
('ai_output_format', 'html', 'HTML', 4, '1', 'HTML格式');

-- ==========================================
-- 4. 技能执行器类型
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('skill_executor_type', '技能执行器类型', 'AI技能的执行器类型', 4, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('skill_executor_type', 'http', 'HTTP接口', 1, '1', '调用外部HTTP接口'),
('skill_executor_type', 'llm', '大模型(LLM)', 2, '1', '调用大语言模型'),
('skill_executor_type', 'script', '脚本执行', 3, '1', '执行自定义脚本'),
('skill_executor_type', 'file', '文件生成', 4, '1', '生成文件输出'),
('skill_executor_type', 'rag', '知识检索(RAG)', 5, '1', '检索增强生成'),
('skill_executor_type', 'hybrid', '混合', 6, '1', '混合执行模式');

-- ==========================================
-- 5. 技能输出类型
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('skill_output_type', '技能输出类型', 'AI技能的输出数据类型', 5, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('skill_output_type', 'json', 'JSON数据', 1, '1', '结构化JSON'),
('skill_output_type', 'file', '文件下载', 2, '1', '文件流下载'),
('skill_output_type', 'stream', '流式文本', 3, '1', 'SSE流式输出'),
('skill_output_type', 'markdown', 'Markdown', 4, '1', 'Markdown文本');

-- ==========================================
-- 6. 技能参数类型
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('skill_param_type', '技能参数类型', 'AI技能参数的数据类型', 6, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('skill_param_type', 'string', '字符串', 1, '1', '字符串类型'),
('skill_param_type', 'number', '数字', 2, '1', '数值类型'),
('skill_param_type', 'boolean', '布尔', 3, '1', '布尔类型'),
('skill_param_type', 'select', '选择', 4, '1', '下拉选择'),
('skill_param_type', 'json', 'Json', 5, '1', 'JSON对象'),
('skill_param_type', 'text', '纯文本', 6, '1', '纯文本输入');

-- ==========================================
-- 7. 技能日志状态
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('skill_log_status', '技能日志状态', '技能执行日志的状态', 7, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('skill_log_status', 'all', '全部', 1, '1', '查询全部'),
('skill_log_status', 'success', '成功', 2, '1', '执行成功'),
('skill_log_status', 'fail', '失败', 3, '1', '执行失败'),
('skill_log_status', 'timeout', '超时', 4, '1', '执行超时');

-- ==========================================
-- 8. 提示词模板分类
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('template_category', '提示词模板分类', '提示词模板的业务分类', 8, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('template_category', 'all', '全部', 1, '1', '查询全部'),
('template_category', 'stock_analysis', '股票分析', 2, '1', '股票分析相关提示词'),
('template_category', 'content_gen', '内容生成', 3, '1', '内容创作相关提示词'),
('template_category', 'data_qa', '数据问答', 4, '1', '数据查询问答提示词'),
('template_category', 'general', '通用', 5, '1', '通用提示词模板');

-- ==========================================
-- 9. 提示词模板类型
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('template_type', '提示词模板类型', '提示词模板是系统预设还是用户自定义', 9, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('template_type', '1', '系统预设', 1, '1', '系统内置模板'),
('template_type', '2', '用户自定义', 2, '1', '用户自行创建');

-- ==========================================
-- 10. 提示词模板变量类型
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('template_var_type', '模板变量类型', '提示词模板中变量的数据类型', 10, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('template_var_type', 'string', '字符串', 1, '1', '字符串类型'),
('template_var_type', 'number', '数字', 2, '1', '数值类型'),
('template_var_type', 'boolean', '布尔', 3, '1', '布尔类型'),
('template_var_type', 'select', '下拉选择', 4, '1', '下拉选择'),
('template_var_type', 'date', '日期', 5, '1', '日期类型'),
('template_var_type', 'text', '纯文本', 6, '1', '纯文本输入');

-- ==========================================
-- 11. Embedding 模型
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('embedding_model', 'Embedding模型', '知识库向量化使用的Embedding模型', 11, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('embedding_model', 'text-embedding-v3', '通义千问 text-embedding-v3', 1, '1', '阿里云通义千问向量模型'),
('embedding_model', 'text-embedding-3-small', 'OpenAI text-embedding-3-small', 2, '1', 'OpenAI小尺寸向量模型'),
('embedding_model', 'text-embedding-3-large', 'OpenAI text-embedding-3-large', 3, '1', 'OpenAI大尺寸向量模型');

-- ==========================================
-- 12. 文档分块方式
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('chunk_separator', '文档分块方式', '知识库文档向量化时的分块策略', 12, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('chunk_separator', 'double_newline', '双换行', 1, '1', '按双换行符分块'),
('chunk_separator', 'single_newline', '单换行', 2, '1', '按单换行符分块'),
('chunk_separator', 'paragraph', '段落分隔', 3, '1', '按段落分块'),
('chunk_separator', 'custom', '自定义', 4, '1', '自定义分块方式');

-- ==========================================
-- 13. Token 额度类型
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('quota_type', 'Token额度类型', 'Token配额的周期类型', 13, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('quota_type', 'daily', '日额度', 1, '1', '每日Token限额'),
('quota_type', 'monthly', '月额度', 2, '1', '每月Token限额'),
('quota_type', 'total', '总额度', 3, '1', '总Token限额');

-- ==========================================
-- 14. Token 额度状态
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('quota_status', 'Token额度状态', 'Token配额的当前状态', 14, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('quota_status', 'normal', '正常', 1, '1', '配额正常'),
('quota_status', 'warning', '预警', 2, '1', '配额接近上限'),
('quota_status', 'exceeded', '已超限', 3, '1', '配额已超限'),
('quota_status', 'disabled', '已禁用', 4, '1', '配额已禁用');

-- ==========================================
-- 15. 知识库文档解析状态
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('kb_parse_status', '文档解析状态', '知识库文档的解析进度状态', 15, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('kb_parse_status', '0', '待解析', 1, '1', '文档已上传，等待解析'),
('kb_parse_status', '1', '解析中', 2, '1', '文档正在解析'),
('kb_parse_status', '2', '成功', 3, '1', '文档解析成功'),
('kb_parse_status', '3', '失败', 4, '1', '文档解析失败');

-- ==========================================
-- 16. 知识库文档向量化状态
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('kb_vector_status', '文档向量化状态', '知识库文档的向量化进度状态', 16, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('kb_vector_status', '0', '待处理', 1, '1', '等待向量化'),
('kb_vector_status', '1', '处理中', 2, '1', '向量化进行中'),
('kb_vector_status', '2', '成功', 3, '1', '向量化成功'),
('kb_vector_status', '3', '失败', 4, '1', '向量化失败');

-- ==========================================
-- 17. 编排执行策略
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('orchestration_strategy', '编排执行策略', '多Agent编排的执行策略', 17, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('orchestration_strategy', 'sequential', '顺序执行', 1, '1', '按步骤顺序执行'),
('orchestration_strategy', 'parallel', '并行执行', 2, '1', '多Agent并行执行'),
('orchestration_strategy', 'supervisor', '总控调度', 3, '1', 'Supervisor统一调度'),
('orchestration_strategy', 'crew', '角色协作', 4, '1', '多角色协作模式'),
('orchestration_strategy', 'dynamic', '动态规划', 5, '1', 'AI动态规划执行');

-- ==========================================
-- 18. 编排节点类型
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('orchestration_node_type', '编排节点类型', '编排流程中的节点类型', 18, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('orchestration_node_type', 'agent', '执行', 1, '1', 'Agent执行节点'),
('orchestration_node_type', 'condition', '条件判断', 2, '1', '条件分支节点'),
('orchestration_node_type', 'aggregate', '结果汇总', 3, '1', '结果聚合节点'),
('orchestration_node_type', 'handoff', '任务交接', 4, '1', '任务交接节点');

-- ==========================================
-- 19. 编排输入模式
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('orchestration_input_mode', '编排输入模式', '编排节点的数据输入来源', 19, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('orchestration_input_mode', 'user_input', '用户原始输入', 1, '1', '直接使用用户输入'),
('orchestration_input_mode', 'prev_output', '上一步输出', 2, '1', '使用前一步骤的输出'),
('orchestration_input_mode', 'shared_state', '共享状态', 3, '1', '从共享状态读取'),
('orchestration_input_mode', 'supervisor_instruction', 'Supervisor指令', 4, '1', '使用Supervisor的指令');

-- ==========================================
-- 20. 编排输出目标
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('orchestration_output_target', '编排输出目标', '编排节点的数据输出去向', 20, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('orchestration_output_target', 'next_step', '下一步', 1, '1', '传递给下一步骤'),
('orchestration_output_target', 'supervisor', '回传Supervisor', 2, '1', '回传给Supervisor'),
('orchestration_output_target', 'shared_state', '写入共享状态', 3, '1', '写入共享状态'),
('orchestration_output_target', 'all_peers', '广播', 4, '1', '广播给所有节点');

-- ==========================================
-- 21. 调度类型
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('schedule_type', '调度类型', '工作流的定时调度方式', 21, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('schedule_type', 'cron', 'Cron表达式', 1, '1', '使用Cron表达式调度'),
('schedule_type', 'interval', '固定间隔', 2, '1', '按固定时间间隔执行'),
('schedule_type', 'once', '单次执行', 3, '1', '只执行一次');

-- ==========================================
-- 22. 数据频率
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('data_frequency', '数据频率', '宏观数据的采集频率', 22, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('data_frequency', 'daily', '日频', 1, '1', '每日更新'),
('data_frequency', 'weekly', '周频', 2, '1', '每周更新'),
('data_frequency', 'monthly', '月频', 3, '1', '每月更新');

-- ==========================================
-- 23. 博客文章状态
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('article_status', '博客文章状态', '博客文章的发布状态', 23, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('article_status', '1', '已发布', 1, '1', '文章已发布'),
('article_status', '0', '草稿', 2, '1', '文章为草稿状态');

-- ==========================================
-- 24. 博客项目可见性
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('project_visibility', '项目可见性', '博客项目的显示状态', 24, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('project_visibility', '1', '显示', 1, '1', '项目对外可见'),
('project_visibility', '0', '隐藏', 2, '1', '项目仅自己可见');

-- ==========================================
-- 25. 菜单资源类型
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('menu_resource_type', '菜单资源类型', '系统菜单的资源类型', 25, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('menu_resource_type', '1', '目录', 1, '1', '菜单目录节点'),
('menu_resource_type', '2', '菜单', 2, '1', '页面菜单'),
('menu_resource_type', '3', '按钮', 3, '1', '操作按钮权限');

-- ==========================================
-- 26. 股票阈值触发类型
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('stock_threshold_type', '阈值触发类型', '股票监控阈值的触发方向', 26, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('stock_threshold_type', 'CROSS_UP', '向上突破', 1, '1', '价格向上突破阈值'),
('stock_threshold_type', 'CROSS_DOWN', '向下跌破', 2, '1', '价格向下跌破阈值');

-- ==========================================
-- 27. 股票分析维度
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('stock_analysis_dimension', '股票分析维度', '股票分析的维度类型', 27, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('stock_analysis_dimension', 'all', '综合分析', 1, '1', '综合多个维度分析'),
('stock_analysis_dimension', 'technical', '技术面', 2, '1', '技术指标分析'),
('stock_analysis_dimension', 'fundamental', '基本面', 3, '1', '财务基本面分析'),
('stock_analysis_dimension', 'dividend', '分红', 4, '1', '股息分红分析');

-- ==========================================
-- 28. AI SSE 事件类型
-- ==========================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status) VALUES
('ai_sse_event_type', 'AI SSE事件类型', 'AI流式通信的SSE事件类型', 28, '1');

INSERT INTO sys_dict (dict_type, dict_code, dict_name, order_num, status, remark) VALUES
('ai_sse_event_type', 'step_start', '步骤开始', 1, '1', '执行步骤开始'),
('ai_sse_event_type', 'step_done', '步骤完成', 2, '1', '执行步骤完成'),
('ai_sse_event_type', 'step_progress', '步骤进度', 3, '1', '执行步骤进度更新'),
('ai_sse_event_type', 'step_detail', '步骤详情', 4, '1', '执行步骤详细信息'),
('ai_sse_event_type', 'step_error', '步骤错误', 5, '1', '执行步骤出错'),
('ai_sse_event_type', 'content', '内容', 6, '1', 'LLM流式内容'),
('ai_sse_event_type', 'done', '结束', 7, '1', '流式传输结束'),
('ai_sse_event_type', 'error', '错误', 8, '1', '整体错误'),
('ai_sse_event_type', 'stop', '停止', 9, '1', '用户主动停止'),
('ai_sse_event_type', 'orchestration_start', '编排开始', 10, '1', '编排流程开始'),
('ai_sse_event_type', 'agent_select', 'Agent选择', 11, '1', 'Supervisor选择Agent'),
('ai_sse_event_type', 'routing', '路由', 12, '1', '对话路由信息'),
('ai_sse_event_type', 'route_result', '路由结果', 13, '1', '路由匹配结果');
