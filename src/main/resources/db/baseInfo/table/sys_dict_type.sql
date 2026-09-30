CREATE TABLE sys_dict_type (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  dict_type VARCHAR(50) NOT NULL UNIQUE COMMENT '字典类型编码',
  dict_name VARCHAR(100) NOT NULL COMMENT '字典类型名称（中文）',
  remark VARCHAR(500) COMMENT '类型描述',
  order_num INT DEFAULT 0 COMMENT '排序号',
  status CHAR(1) DEFAULT '1' COMMENT '状态（1启用 0停用）',
  gmt_create DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  gmt_modified DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) COMMENT '字典类型表';

-- ============================================================
-- 迁移SQL：从 sys_dict 中提取不重复的 dict_type 生成类型记录
-- 执行前请确认 sys_dict 中数据已就绪
-- ============================================================
INSERT INTO sys_dict_type (dict_type, dict_name, remark, order_num, status)
SELECT
  d.dict_type,
  -- 取同类型下 order_num 最小的那条记录的 dict_name 作为类型名称
  MIN(CASE WHEN d.order_num = m.min_order THEN d.dict_name END) AS dict_name,
  -- 取同类型下 order_num 最小的那条记录的 remark 作为类型描述
  MIN(CASE WHEN d.order_num = m.min_order THEN d.remark END) AS remark,
  ROW_NUMBER() OVER (ORDER BY MIN(d.id)) AS order_num,
  '1'
FROM sys_dict d
JOIN (
  SELECT dict_type, MIN(order_num) AS min_order
  FROM sys_dict
  WHERE status = '1'
  GROUP BY dict_type
) m ON d.dict_type = m.dict_type AND d.order_num = m.min_order
WHERE d.status = '1'
GROUP BY d.dict_type;

-- 对于 remark 为 NULL 或 JSON 格式的类型，手动修正为中文名称
-- （根据实际业务补充）
UPDATE sys_dict_type SET dict_name = '博客分类' WHERE dict_type = 'blog_category';
UPDATE sys_dict_type SET dict_name = '博客标签' WHERE dict_type = 'blog_tag';
UPDATE sys_dict_type SET dict_name = '基金类型' WHERE dict_type = 'fund_type';
UPDATE sys_dict_type SET dict_name = '交易所类型' WHERE dict_type = 'exchange_type';
UPDATE sys_dict_type SET dict_name = '股息分红类型' WHERE dict_type = 'stock_dividend_type';
UPDATE sys_dict_type SET dict_name = '股息分红状态' WHERE dict_type = 'stock_dividend_status';
UPDATE sys_dict_type SET dict_name = '工作流节点类型' WHERE dict_type = 'workflow_node_type';
UPDATE sys_dict_type SET dict_name = '工作流实例状态' WHERE dict_type = 'workflow_instance_status';
UPDATE sys_dict_type SET dict_name = '工作流节点状态' WHERE dict_type = 'workflow_node_status';
UPDATE sys_dict_type SET dict_name = '人机协同任务类型' WHERE dict_type = 'human_task_type';
UPDATE sys_dict_type SET dict_name = '人工任务状态' WHERE dict_type = 'human_task_status';
UPDATE sys_dict_type SET dict_name = '工作流触发方式' WHERE dict_type = 'workflow_trigger_type';
