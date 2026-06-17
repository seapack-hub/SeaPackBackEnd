# SeaPackBackEnd — 综合金融数据服务平台

## 项目简介

SeaPackBackEnd 是一个面向金融投资场景的后端服务平台，基于 Spring Boot 3 + MyBatis 构建。系统覆盖股票数据采集与解析、行情展示、股息率监控、基金持仓管理、AI 智能问答及完善的权限控制体系，提供 RESTful API 支撑前端 SPA 应用。

---

## 技术栈

| 类别 | 选型 |
|---|---|
| **语言** | Java 17 |
| **框架** | Spring Boot 3.2 + Spring MVC + Spring Security |
| **ORM** | MyBatis + MyBatis-Plus + PageHelper 分页 |
| **数据库** | MySQL 8（Druid 连接池） |
| **缓存** | Redis + Caffeine 本地缓存 |
| **AI** | LangChain4j（DeepSeek / Aliyun DashScope） |
| **消息推送** | JavaMail（SMTP）+ SMS（预留对接） |
| **数据文件** | DuckDB（直接 SQL 查询 Parquet 文件） |
| **构建工具** | Maven |
| **其他** | WebSocket、Jsoup、Apache POI、Caffeine |

---

## 功能模块

### 1. 股票行情与基础数据
- **基础信息管理** — 股票代码、名称、上市板块、行业分类等基础数据的维护
- **实时行情展示** — 以 `stock_basic` 为主表，LEFT JOIN 最新交易日行情和最新年份分红数据，计算涨跌幅与股息率，支持多条件筛选排序
- **历史 K 线数据** — 日线数据的存储与查询，数据源 Parquet 文件
- **板块与行业** — 行业分类树形结构查询

### 2. 数据采集与解析
- **Parquet 文件解析** — 将 Python（AKShare）生成的 Parquet 数据文件解析入库
- **东财接口对接** — 通过 HttpURLConnection 抓取东方财富网行情数据并解析
- **数据导入** — 股票基本信息同步、日线数据导入、实时行情导入、分红数据导入、导出 SQL/文本

### 3. 股息率监控
- **监控池管理** — 用户可创建个人监控池，添加关注的股票
- **阈值配置** — 每条监控支持配置多个 CROSS_UP（向上突破）/ CROSS_DOWN（向下跌破）阈值规则
- **定时检查** — 每天 22:00 自动遍历监控池，计算最新股息率与阈值比较
- **防抖机制** — 同一阈值触发后有 24 小时冷静期，避免重复告警
- **告警通知** — 触发阈值时记录告警日志，通过邮件（SMTP 真实发送）+ SMS（预留）推送给用户

### 4. 基金与持仓
- **基金基本信息管理** — 基金代码、名称、类型、净值等
- **持仓管理** — 用户基金持仓记录，包括份额、成本、收益等
- **交易记录** — 基金买卖流水

### 5. AI 智能助手
- **多模型支持** — 集成 DeepSeek 和阿里云 Qwen，通过统一接口切换
- **流式聊天** — SSE（Server-Sent Events）实时流式对话
- **Agent 工具调用** — AI Agent 可自动调用文件生成（DOCX/PDF/XLSX）、网页抓取等工具完成任务
- **RAG 知识库** — 支持文本导入、向量化存储（InMemoryEmbeddingStore）、语义检索

### 6. 权限与认证体系
- **用户管理** — 用户 CRUD、密码重置、批量操作
- **角色管理** — 角色的分页搜索、CRUD，支持分配权限（全量覆盖）
- **权限/菜单管理** — 树形结构的目录-菜单-按钮三级资源管理
- **用户授权** — 为用户分配角色
- **动态鉴权** — `/auth/user-info` 获取角色与权限标识符，`/auth/menus` 获取动态菜单树

### 7. 系统通用功能
- **验证码** — 滑块验证码支持
- **RSA 加密** — 登录密码 RSA 公钥加密传输
- **文件导出** — Excel / PDF / Word 通用导出工具
- **操作日志** — 通知日志、告警日志查询

---

## 项目目录结构

```
src/main/java/org/seaPack/
├── SeaPackBackEndApplication.java    # 应用入口（@SpringBootApplication + @EnableScheduling + @MapperScan）
│
├── config/                           # 全局配置
│   ├── SecurityConfig.java           # Spring Security 安全配置
│   ├── AiConfig.java                 # AI 模型动态初始化
│   ├── AIProperties.java             # AI 配置属性映射
│   ├── CacheConfig.java              # Caffeine 缓存配置
│   ├── GlobalExceptionHandler.java   # 全局异常处理
│   ├── GlobalResponseHandler.java    # 统一响应格式
│   ├── RestTemplateConfig.java       # HTTP 客户端配置
│   ├── Result.java / ResultCode.java # 统一返回体
│   └── BusinessException.java        # 业务异常
│
├── controller/                       # 控制器层（RESTful API）
│   ├── ai/                           # AI 聊天、Agent、RAG
│   ├── auth/                         # 登录、验证码、用户信息、菜单
│   ├── common/                       # 文件导出、通知日志、告警日志
│   ├── finance/                      # 基金、持仓、分红
│   ├── market/                       # 行情、监控池、行业板块
│   ├── parse/                        # Parquet 数据解析导入
│   └── system/                       # 用户、角色、权限、部门
│
├── service/                          # 业务逻辑层
│   ├── ai/                           # RAG 检索增强生成
│   ├── auth/                         # 认证鉴权、滑块验证
│   ├── common/                       # 通知推送、日志、进度管理
│   ├── finance/                      # 基金持仓、分红
│   ├── market/                       # 行情、监控定时任务、行业
│   ├── parse/                        # 数据导入导出服务
│   └── system/                       # 用户、角色、权限部门
│
├── mapper/                           # MyBatis 数据访问接口
│   ├── common/                       # 通知日志、告警日志
│   ├── finance/                      # 基金、持仓、交易、分红
│   ├── market/                       # 行情、监控、行业
│   └── system/                       # 用户、角色、权限、部门
│
├── model/                            # 实体类（JPA 注解）
│   ├── common/                       # 告警日志、通知日志
│   ├── finance/                      # 基金、持仓、分红、交易
│   ├── market/                       # 行情、监控、行业
│   └── system/                       # 用户、角色、权限、部门
│       └── permission/               # 角色、菜单/权限、关联表
│
├── dto/                              # 数据传输对象
│   ├── ai/                           # 聊天请求、知识库导入
│   ├── auth/                         # 登录、验证码
│   ├── common/                       # 文件导出
│   ├── market/                       # 行情、K 线、持仓查询
│   └── system/                       # 权限树、用户信息
│
├── components/                       # 通用组件
│   ├── RsaUtil.java                  # RSA 加密解密
│   ├── ExcelExportUtil.java          # Excel 导出
│   ├── EastMoneyJsonParser.java      # 东方财富数据解析
│   ├── GenericTreeBuilder.java       # 通用树形构建
│   └── Assistant.java                # LangChain4j @AiService
│
├── tools/                            # AI Agent 工具
│   ├── FileGeneratorTool.java        # 文件生成（DOCX/XLSX/PDF）
│   └── WebScraperUtilTool.java       # 网页抓取
│
├── exception/                        # 自定义异常
│
└── plugin/                           # MyBatis 插件
    └── LombokPlugin.java             # 代码生成器 Lombok 支持
```

### 资源文件

```
src/main/resources/
├── application.properties            # 主配置文件
├── generatorConfig.xml               # MyBatis Generator 配置
└── mapper/                           # MyBatis XML 映射文件（21个）
    ├── common/                       # AlertLog, NotificationLog
    ├── finance/                      # Fund, Holding, Nav, StockDividend, Transaction
    ├── market/                       # Industry, Monitor, Quote, StockInfo
    └── system/                       # User, Role, Permission, Department, Dict
```

---

## 数据库核心表

| 表 | 说明 |
|---|---|
| `sys_user` | 系统用户 |
| `sys_role` | 角色 |
| `sys_permission` | 权限/菜单（树形结构） |
| `sys_user_role` | 用户-角色关联 |
| `sys_role_permission` | 角色-权限关联 |
| `stock_basic` | 股票基本信息 |
| `stock_realtime_quote` | 实时行情 |
| `stock_dividend` | 分红数据 |
| `user_stock_monitor` | 用户监控池 |
| `monitor_threshold_config` | 阈值配置 |
| `alert_log` | 告警日志 |
| `notification_log` | 通知日志 |

---

## 启动说明

1. 确保 JDK 17+、MySQL 8、Redis 已安装
2. 在 `application.properties` 中配置数据库连接、Redis、邮件 SMTP 和 AI API Key
3. 执行数据库 DDL 脚本创建所需表
4. 运行 `mvn spring-boot:run` 或直接启动 `SeaPackBackEndApplication.java`
5. 访问 `http://localhost:8090` 查看 API

---

## 核心配置

```properties
# 数据库
spring.datasource.url=jdbc:mysql://localhost:3306/sea_pack
spring.datasource.username=root
spring.datasource.password=seapack

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=seapack

# AI 模型（二选一）
ai.active-provider=aliyun
ai.providers.aliyun.api-key=your_key

# 邮件通知
spring.mail.host=smtp.qq.com
spring.mail.port=587
spring.mail.username=your_email@qq.com
spring.mail.password=your_authorization_code

# 通知开关
notify.email.enabled=true
notify.sms.enabled=false
```
