CREATE TABLE sys_dict (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  dict_type VARCHAR(50) NOT NULL COMMENT '字典类型',
  dict_code VARCHAR(50) NOT NULL COMMENT '字典编码',
  dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
  order_num INT DEFAULT 0 COMMENT '排序号',
  status CHAR(1) DEFAULT '1' COMMENT '状态（1启用 0停用）',
  remark VARCHAR(500) COMMENT '备注',
  gmt_create DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  gmt_modified DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  UNIQUE INDEX uk_dict_type_code (dict_type, dict_code) -- 防止同一类型下编码重复
) COMMENT '系统字典表';