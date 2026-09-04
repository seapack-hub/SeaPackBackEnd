-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '18771721257', '系统管理员', 1, '3270937741@qq.com', 1, 1, '海峰科技', '2025-07-09 11:13:20', 'seapack');
INSERT INTO `sya_user` VALUES (2, 'seaPack', '18771721257', '烈风逍遥', 1, '3270937741@qq.com', 0, 10, NULL, '2026-06-14 18:41:15', 'seapack');

-- 1. 清空旧数据 (防止主键冲突或层级路径错误)
TRUNCATE TABLE sea_pack.department;

-- ==========================================
-- 第一层：根节点 (1条)
-- ID: 1, Level: 1, Path: 1
-- ==========================================
INSERT INTO sea_pack.department (`dept_id`, `dept_name`, `parent_dept_id`, `dept_level`, `dept_path`, `seq`, `create_time`) VALUES
(1, '海峰集团总部', NULL, 1, '1', 1, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 20) DAY));

-- ==========================================
-- 第二层：一级子公司/事业部 (4条)
-- 父级: 1 (海事集团总部)
-- ==========================================
INSERT INTO sea_pack.department (`dept_id`, `dept_name`, `parent_dept_id`, `dept_level`, `dept_path`, `seq`, `create_time`) VALUES
(2, '航运事业部', 1, 2, '1/2', 1, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 18) DAY)),
(3, '港口运营部', 1, 2, '1/3', 2, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 17) DAY)),
(4, '物流供应链部', 1, 2, '1/4', 3, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 16) DAY)),
(5, '船舶重工部', 1, 2, '1/5', 4, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 15) DAY));

-- ==========================================
-- 第三层：二级职能部门 (15条)
-- 父级: 2, 3, 4, 5
-- ==========================================

-- 航运事业部 (ID: 2) 下属部门
INSERT INTO sea_pack.department (`dept_id`, `dept_name`, `parent_dept_id`, `dept_level`, `dept_path`, `seq`, `create_time`) VALUES
(6, '远洋运输队', 2, 3, '1/2/6', 1, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 14) DAY)),
(7, '近海调度室', 2, 3, '1/2/7', 2, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 13) DAY)),
(8, '船员管理中心', 2, 3, '1/2/8', 3, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 12) DAY));

-- 港口运营部 (ID: 3) 下属部门
INSERT INTO sea_pack.department (`dept_id`, `dept_name`, `parent_dept_id`, `dept_level`, `dept_path`, `seq`, `create_time`) VALUES
(9, '集装箱码头', 3, 3, '1/3/9', 1, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 11) DAY)),
(10, '散货码头', 3, 3, '1/3/10', 2, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 10) DAY)),
(11, '港口安保科', 3, 3, '1/3/11', 3, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 9) DAY)),
(12, '装卸设备科', 3, 3, '1/3/12', 4, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 8) DAY));

-- 物流供应链部 (ID: 4) 下属部门
INSERT INTO sea_pack.department (`dept_id`, `dept_name`, `parent_dept_id`, `dept_level`, `dept_path`, `seq`, `create_time`) VALUES
(13, '仓储管理科', 4, 3, '1/4/13', 1, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 7) DAY)),
(14, '陆运车队', 4, 3, '1/4/14', 2, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 6) DAY)),
(15, '报关业务科', 4, 3, '1/4/15', 3, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 5) DAY));

-- 船舶重工部 (ID: 5) 下属部门
INSERT INTO sea_pack.department (`dept_id`, `dept_name`, `parent_dept_id`, `dept_level`, `dept_path`, `seq`, `create_time`) VALUES
(16, '船体设计室', 5, 3, '1/5/16', 1, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 4) DAY)),
(17, '机电安装科', 5, 3, '1/5/17', 2, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 3) DAY)),
(18, '涂装车间', 5, 3, '1/5/18', 3, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 2) DAY)),
(19, '质量检测科', 5, 3, '1/5/19', 4, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 1) DAY)),
(20, '物资采购科', 5, 3, '1/5/20', 5, NOW());


/*
-- Query: select * from sys_dict
LIMIT 0, 1000

-- Date: 2026-09-04 16:45
*/
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (1,'fund_type','1','货币型',1,'1','主要投资于短期货币工具，流动性高，风险极低','2025-12-21 16:04:29','2025-12-21 16:04:29');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (2,'fund_type','2','债券型-长债',2,'1','主要投资于长期债券','2025-12-21 16:04:29','2025-12-21 16:04:29');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (3,'fund_type','3','债券型-中短债',3,'1','主要投资于中期和短期债券','2025-12-21 16:04:29','2025-12-21 16:04:29');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (4,'fund_type','4','债券型-可转债',4,'1','主要投资于可转换公司债券','2025-12-21 16:04:29','2025-12-21 16:04:29');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (5,'fund_type','5','混合型-偏股',5,'1','股票配置比例较高，通常高于60%','2025-12-21 16:04:29','2025-12-21 16:04:29');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (6,'fund_type','6','混合型-偏债',6,'1','债券配置比例较高，股票比例相对较低','2025-12-21 16:04:29','2025-12-21 16:04:29');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (7,'fund_type','7','混合型-平衡',7,'1','股票和债券配置相对均衡','2025-12-21 16:04:29','2025-12-21 16:04:29');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (8,'fund_type','8','股票型',8,'1','投资于股票市场的比例很高，通常80%以上','2025-12-21 16:04:29','2025-12-21 16:04:29');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (9,'fund_type','9','指数型-股票',9,'1','跟踪股票指数，如沪深300指数基金','2025-12-21 16:04:29','2025-12-21 16:04:29');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (10,'fund_type','10','QDII',10,'1','合格境内机构投资者基金，主要投资于境外市场','2025-12-21 16:04:29','2025-12-21 16:04:29');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (11,'fund_type','11','指数型-商品',11,'1','跟踪商品，如黄金、原油等','2025-12-21 17:14:11','2025-12-21 17:14:11');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (12,'exchange_type','SSE','上海证券交易所',1,'1','简称上交所，主板代码以60开头','2026-06-02 13:57:01','2026-06-02 13:57:01');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (13,'exchange_type','SZSE','深圳证券交易所',2,'1','简称深交所，主板代码以00开头，创业板以30开头','2026-06-02 13:57:01','2026-06-02 13:57:01');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (14,'exchange_type','BSE','北京证券交易所',3,'1','简称北交所，代码以8开头','2026-06-02 13:57:01','2026-06-02 13:57:01');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (15,'stock_dividend_type','INTERIM','中期分红',1,'1','包含半年报分红、一季报分红等','2026-06-09 16:07:26','2026-06-09 16:07:26');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (16,'stock_dividend_type','FINAL','末期分红',2,'1','即年度分红，通常随年报披露','2026-06-09 16:07:26','2026-06-09 16:07:26');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (17,'stock_dividend_status','PROPOSED','预案阶段',1,'1','董事会已发布分红预案，待股东大会审议','2026-06-09 16:07:26','2026-06-09 16:07:26');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (18,'stock_dividend_status','APPROVED','已批准',2,'1','股东大会已审议通过，尚未派发','2026-06-09 16:07:26','2026-06-09 16:07:26');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (19,'stock_dividend_status','IMPLEMENTED','已实施',3,'1','除权除息日已过，红利已实际派发','2026-06-09 16:07:26','2026-06-09 16:07:26');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (20,'blog_category','vue','Vue3',1,'1',NULL,'2026-07-02 09:25:20','2026-07-02 09:25:20');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (21,'blog_category','java','Java',2,'1',NULL,'2026-07-02 09:25:20','2026-07-02 09:25:20');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (22,'blog_category','gis','GIS',3,'1',NULL,'2026-07-02 09:25:20','2026-07-02 09:25:20');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (23,'blog_category','other','其他',4,'1',NULL,'2026-07-02 09:25:20','2026-07-02 09:25:20');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (24,'blog_tag','Vue3','Vue3',1,'1','{\"colorType\":\"\"}','2026-07-02 09:25:20','2026-07-02 09:25:20');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (25,'blog_tag','GIS','GIS',2,'1','{\"colorType\":\"success\"}','2026-07-02 09:25:20','2026-07-02 09:25:20');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (26,'blog_tag','Java','Java',3,'1','{\"colorType\":\"warning\"}','2026-07-02 09:25:20','2026-07-02 09:25:20');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (27,'blog_tag','其他','其他',4,'1','{\"colorType\":\"info\"}','2026-07-02 09:25:20','2026-07-02 09:25:20');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (28,'workflow_node_type','start','开始节点',1,'1','工作流节点类型定义','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (29,'workflow_node_type','end','结束节点',2,'1','工作流节点类型定义','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (30,'workflow_node_type','skill','AI技能调用',3,'1','工作流节点类型定义','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (31,'workflow_node_type','http_request','HTTP请求',4,'1','工作流节点类型定义','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (32,'workflow_node_type','sql_query','SQL查询',5,'1','工作流节点类型定义','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (33,'workflow_node_type','llm_call','LLM调用',6,'1','工作流节点类型定义','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (34,'workflow_node_type','condition','条件分支',7,'1','工作流节点类型定义','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (35,'workflow_node_type','loop','循环',8,'1','工作流节点类型定义','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (36,'workflow_node_type','parallel','并行网关',9,'1','工作流节点类型定义','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (37,'workflow_node_type','approval','人工审批',10,'1','工作流节点类型定义','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (38,'workflow_node_type','annotation','人工标注',11,'1','工作流节点类型定义','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (39,'workflow_node_type','delay','延时',12,'1','工作流节点类型定义','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (40,'workflow_node_type','variable_set','变量赋值',13,'1','工作流节点类型定义','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (41,'workflow_node_type','sub_workflow','子工作流',14,'1','工作流节点类型定义','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (42,'workflow_node_type','notification','通知',15,'1','工作流节点类型定义','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (43,'workflow_instance_status','0','待执行',1,'1','工作流执行实例状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (44,'workflow_instance_status','1','运行中',2,'1','工作流执行实例状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (45,'workflow_instance_status','2','已完成',3,'1','工作流执行实例状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (46,'workflow_instance_status','3','失败',4,'1','工作流执行实例状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (47,'workflow_instance_status','4','暂停',5,'1','工作流执行实例状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (48,'workflow_instance_status','5','已取消',6,'1','工作流执行实例状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (49,'workflow_node_status','0','待执行',1,'1','工作流节点执行状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (50,'workflow_node_status','1','执行中',2,'1','工作流节点执行状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (51,'workflow_node_status','2','已完成',3,'1','工作流节点执行状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (52,'workflow_node_status','3','失败',4,'1','工作流节点执行状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (53,'workflow_node_status','4','跳过',5,'1','工作流节点执行状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (54,'workflow_node_status','5','等待人工',6,'1','工作流节点执行状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (55,'workflow_node_status','6','超时',7,'1','工作流节点执行状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (56,'workflow_node_status','7','已取消',8,'1','工作流节点执行状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (57,'human_task_type','approval','审批',1,'1','人机协同任务类型','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (58,'human_task_type','review','审核',2,'1','人机协同任务类型','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (59,'human_task_type','annotation','标注',3,'1','人机协同任务类型','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (60,'human_task_type','feedback','反馈',4,'1','人机协同任务类型','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (61,'human_task_type','input','人工输入',5,'1','人机协同任务类型','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (62,'human_task_status','0','待处理',1,'1','人工任务状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (63,'human_task_status','1','处理中',2,'1','人工任务状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (64,'human_task_status','2','已通过',3,'1','人工任务状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (65,'human_task_status','3','已驳回',4,'1','人工任务状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (66,'human_task_status','4','已升级',5,'1','人工任务状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (67,'human_task_status','5','已过期',6,'1','人工任务状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (68,'human_task_status','6','已转办',7,'1','人工任务状态','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (69,'workflow_trigger_type','manual','手动触发',1,'1','工作流触发方式','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (70,'workflow_trigger_type','api','API触发',2,'1','工作流触发方式','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (71,'workflow_trigger_type','schedule','定时触发',3,'1','工作流触发方式','2026-07-09 17:54:50','2026-07-09 17:54:50');
INSERT INTO sea_pack.sys_dict (`id`,`dict_type`,`dict_code`,`dict_name`,`order_num`,`status`,`remark`,`gmt_create`,`gmt_modified`) VALUES (72,'workflow_trigger_type','event','事件触发',4,'1','工作流触发方式','2026-07-09 17:54:50','2026-07-09 17:54:50');


-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO sea_pack.`sys_permission` VALUES (1, 0, '基础信息', 'baseInfo', 1, '/baseInfo', '', 0, 1);
INSERT INTO sea_pack.`sys_permission` VALUES (2, 0, '权限管理', '', 1, '/permission', '', 0, 1);
INSERT INTO sea_pack.`sys_permission` VALUES (3, 0, '基金模块', '', 1, '/fund', '', 0, 1);
INSERT INTO sea_pack.`sys_permission` VALUES (4, 0, '股息监控', '', 1, '/stockManagement', '', 0, 1);
INSERT INTO sea_pack.`sys_permission` VALUES (5, 0, '组件封装', '', 1, '/genericComponent', '', 0, 1);
INSERT INTO sea_pack.`sys_permission` VALUES (6, 0, '图形化管理', '', 1, '/graphical', '', 0, 1);
INSERT INTO sea_pack.`sys_permission` VALUES (7, 0, 'AI交互', '', 2, '/aiInteraction', '', 0, 1);
INSERT INTO sea_pack.`sys_permission` VALUES (8, 0, '博客文档', '', 1, '/doc', '', 0, 1);
INSERT INTO sea_pack.`sys_permission` VALUES (9, 0, 'echarts图表', '', 1, '/echarts', '', 0, 1);
INSERT INTO sea_pack.`sys_permission` VALUES (10, 1, '部门管理', 'dept', 2, '/baseInfo/dept', '/views/systemManagement/baseInfo/deptManagement/index.vue', 0, 1);
INSERT INTO sea_pack.`sys_permission` VALUES (11, 10, '编辑', 'edit', 3, '', '', 0, 1);
INSERT INTO sea_pack.`sys_permission` VALUES (12, 10, '新增', 'add', 3, '', '', 0, 1);

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO sea_pack.`sys_role` VALUES (1, '超级管理员', 'admin', '具有页面的全部权限', 1, '2026-06-16 21:16:21');
INSERT INTO sea_pack.`sys_role` VALUES (2, '股票分析员', 'stock', '拥有股票相关模块的权限', 1, '2026-06-16 21:17:14');
INSERT INTO sea_pack.`sys_role` VALUES (3, '基金分析员', 'fund', '拥有基金相关功能的权限', 1, '2026-06-16 21:17:57');
INSERT INTO sea_pack.`sys_role` VALUES (4, '基础信息员', 'base', '拥有基础信息相关模块的权限', 1, '2026-06-16 21:18:44');

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
INSERT INTO sea_pack.`sys_role_permission` VALUES (4, 1);
INSERT INTO sea_pack.`sys_role_permission` VALUES (4, 2);
INSERT INTO sea_pack.`sys_role_permission` VALUES (4, 3);
INSERT INTO sea_pack.`sys_role_permission` VALUES (4, 4);
INSERT INTO sea_pack.`sys_role_permission` VALUES (4, 5);
INSERT INTO sea_pack.`sys_role_permission` VALUES (4, 6);
INSERT INTO sea_pack.`sys_role_permission` VALUES (4, 7);
INSERT INTO sea_pack.`sys_role_permission` VALUES (4, 8);
INSERT INTO sea_pack.`sys_role_permission` VALUES (4, 9);
INSERT INTO sea_pack.`sys_role_permission` VALUES (4, 10);
INSERT INTO sea_pack.`sys_role_permission` VALUES (4, 11);
INSERT INTO sea_pack.`sys_role_permission` VALUES (4, 12);


-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO sea_pack.`sys_user_role` VALUES (2, 1);
INSERT INTO sea_pack.`sys_user_role` VALUES (2, 2);
INSERT INTO sea_pack.`sys_user_role` VALUES (2, 3);
INSERT INTO sea_pack.`sys_user_role` VALUES (1, 4);
INSERT INTO sea_pack.`sys_user_role` VALUES (2, 4);


/*
-- Query: select * from industry_sector
LIMIT 0, 1000

-- Date: 2026-09-04 16:49
*/
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (1,'tech','科技',NULL,1,0,'2026-06-02 09:34:00','2026-06-02 13:07:25',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (2,'medical','医药',NULL,1,0,'2026-06-02 09:34:34','2026-06-02 09:34:34',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (3,'finance','金融',NULL,1,0,'2026-06-02 09:35:00','2026-06-02 09:35:00',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (4,'new-energy','新能源',NULL,1,0,'2026-06-02 09:35:30','2026-06-02 09:35:30',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (5,'consumer','消费',NULL,1,0,'2026-06-02 09:37:41','2026-06-02 09:37:41',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (6,'manufacture','制造',NULL,1,0,'2026-06-02 09:37:59','2026-06-02 09:37:59',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (7,'semi','半导体',1,2,0,'2026-06-02 09:53:28','2026-06-02 09:53:28',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (8,'sw','软件开发',1,2,0,'2026-06-02 10:30:07','2026-06-02 10:30:07',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (9,'ai','人工智能',1,2,0,'2026-06-02 10:31:05','2026-06-02 10:31:05',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (10,'comm','通信设备',1,2,0,'2026-06-02 10:31:36','2026-06-02 10:31:36',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (11,'pharma','化学制药',2,2,0,'2026-06-02 10:32:12','2026-06-02 10:32:12',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (12,'bio','生物制药',2,2,0,'2026-06-02 12:00:01','2026-06-02 12:00:01',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (13,'med-dev','医疗器械',2,2,0,'2026-06-02 12:01:19','2026-06-02 12:01:19',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (14,'tcm','中药',2,2,0,'2026-06-02 12:01:51','2026-06-02 12:01:51',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (15,'bank','银行',3,2,0,'2026-06-02 12:45:05','2026-06-02 12:45:05',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (16,'secu','证券',3,2,0,'2026-06-02 12:45:30','2026-06-02 12:45:30',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (17,'insure','保险',3,2,0,'2026-06-02 12:45:49','2026-06-02 12:45:49',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (18,'trust','信托',3,2,0,'2026-06-02 13:00:29','2026-06-02 13:00:29',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (19,'pv','光伏',4,2,0,'2026-06-02 13:00:58','2026-06-02 13:00:58',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (20,'ev','新能源汽车',4,2,0,'2026-06-02 13:01:29','2026-06-02 13:01:29',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (21,'wind','风电',4,2,0,'2026-06-02 13:01:47','2026-06-02 13:01:47',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (22,'storage','储能',4,2,0,'2026-06-02 13:02:12','2026-06-02 13:02:12',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (23,'food','食品饮料',5,2,0,'2026-06-02 13:02:42','2026-06-02 15:14:51',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (24,'retail','商贸零售',5,2,0,'2026-06-02 13:03:11','2026-06-02 13:03:11',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (25,'media','传媒',5,2,0,'2026-06-02 13:03:30','2026-06-02 13:03:30',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (26,'travel','旅游',5,2,0,'2026-06-02 13:03:47','2026-06-02 13:03:47',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (27,'mech','机械设备',6,2,0,'2026-06-02 13:04:11','2026-06-02 13:04:11',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (28,'aero','航空航天',6,2,0,'2026-06-02 13:04:35','2026-06-02 13:04:35',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (29,'chem','化工',6,2,0,'2026-06-02 13:04:53','2026-06-02 13:27:12',0);
INSERT INTO sea_pack.industry_sector (`id`,`code`,`label`,`parent_id`,`node_level`,`sort_order`,`created_at`,`updated_at`,`is_deleted`) VALUES (30,'other','其他未计入',NULL,1,0,'2026-06-11 10:14:18','2026-06-11 10:14:18',0);

