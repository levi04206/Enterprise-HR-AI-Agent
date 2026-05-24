# Enterprise HR AI Agent 工作记录

## 2026-05-22

### 本次完成内容

1. 在 `D:\code` 下创建 Java 后端工程 `Enterprise HR AI Agent`。
2. 生成 Spring Boot 3.5.0 + JDK 21 + Spring AI 1.1.6 Maven 工程。
3. 配置 OpenAI 兼容 API 接入模板，可用于 DeepSeek 等兼容服务。
4. 设计并生成 MySQL 业务表：
   - `employee` 员工表
   - `leave_record` 请假记录表
5. 生成 MyBatis-Plus 实体和 Mapper：
   - `Employee`
   - `LeaveRecord`
   - `LeaveStatus`
   - `EmployeeMapper`
   - `LeaveRecordMapper`
6. 实现 RAG 文档入库模块：
   - 支持上传 PDF/TXT 等文档
   - 使用 `TikaDocumentReader` 读取文档
   - 使用 `TokenTextSplitter` 分块
   - 写入 Spring AI `VectorStore`
7. 实现智能体 Function Calling 工具箱：
   - `getEmployeeContactTool`：按姓名查询部门和邮箱
   - `getLeaveBalanceTool`：按姓名查询剩余年假和最近审批中请假记录
8. 实现 SSE 流式对话接口：
   - `POST /api/v1/chat/stream`
   - 先检索知识库 Top 3 制度片段
   - 注入 System Prompt
   - 使用 `ChatClient.stream()` 流式返回模型输出
9. 生成配置文件和初始化 SQL：
   - `application.yml`
   - `schema.sql`
   - `data.sql`
10. 添加 `.gitignore`。
11. 执行 Maven 校验：
    - `mvn compile` 成功
    - `mvn "-Dmaven.test.skip=true" package` 成功

### 当前状态

工程可以正常编译和打包。启动前需要准备 MySQL 数据库，并配置可用的 OpenAI 兼容 API Key。

### 待办事项

1. 配置 GitHub 远程仓库并推送代码。
2. 本机安装或登录 GitHub CLI 后，可由 Codex 自动创建 GitHub 仓库。
3. 后续可增加 Redis Stack VectorStore、对话历史、权限校验、前端页面和接口联调。

## 2026-05-22 第二次迭代

### 本次完成内容

1. 添加员工 REST 管理接口：
   - `POST /api/v1/employees`
   - `GET /api/v1/employees`
   - `GET /api/v1/employees/{id}`
2. 添加请假记录 REST 管理接口：
   - `POST /api/v1/leave-records`
   - `GET /api/v1/leave-records/employee/{empId}`
3. 新增员工和请假记录 DTO，避免控制器直接暴露实体。
4. 新增 `EmployeeService` 和 `LeaveRecordService`，把业务校验从 Controller 中拆出。
5. 新增全局异常处理器，统一返回参数错误和业务错误。
6. 添加 `docker-compose.yml`，提供本地 MySQL 8.4 和 Redis Stack 环境。
7. 更新 `README.md`，补充 Docker 启动和新增管理接口示例。

### 当前状态

后端除了 AI 对话和知识库上传外，已经具备基础 HR 数据维护接口，便于后续前端页面和 Function Calling 联调。

## 2026-05-22 第三次迭代

### 本次完成内容

1. 添加 H2 测试依赖，用于在没有本地 MySQL 的情况下跑通自动化测试。
2. 新增 `application-test.yml`，测试 profile 使用 H2 内存数据库和测试用 AI 配置。
3. 新增 H2 专用初始化脚本：
   - `schema-h2.sql`
   - `data-h2.sql`
4. 添加员工接口集成测试：
   - 查询种子员工
   - 新增员工并验证年假余额
5. 添加请假记录接口集成测试：
   - 查询种子请假记录
   - 校验结束日期早于开始日期的错误处理
   - 新增请假记录并验证查询结果
6. 更新 `README.md`，补充 `mvn test` 自动化验证说明。

### 当前状态

项目已经具备基础自动化测试能力。后续即使暂时没有真实 MySQL、Redis 或大模型 API，也可以先用 `mvn test` 验证核心业务接口没有破坏。

## 2026-05-22 第四次迭代

### 本次完成内容

1. 添加 Spring Boot Actuator，用于暴露健康检查和基础运行状态。
2. 新增 `application-local.yml`，提供无需 MySQL/Redis/API Key 的本地启动 profile。
3. 新增 local profile 专用初始化脚本：
   - `schema-local.sql`
   - `data-local.sql`
4. 将 H2 依赖调整为 runtime，使本地 profile 可以直接启动应用。
5. 添加 GitHub Actions CI：
   - 每次 push 到 `master` 自动执行 `mvn test`
   - Pull Request 也会自动执行测试
6. 更新 `README.md`，补充本地快速启动、健康检查和 CI 说明。
7. 添加 `scripts/smoke-local.ps1`，用于打包、启动 local profile、检查健康端点和员工样例接口。
8. 已手动执行本地 smoke test：
   - `/actuator/health` 返回 HTTP 200
   - `/api/v1/employees?keyword=研发` 返回样例员工数据

### 当前状态

项目现在可以通过 `mvn spring-boot:run "-Dspring-boot.run.profiles=local"` 或 `.\scripts\smoke-local.ps1` 在无外部中间件的情况下启动主体服务，并通过 `/actuator/health` 和普通 HR REST 接口做 smoke test。

## 2026-05-23 第五次迭代

### 本次完成内容

1. 根据用户提供的信息更新主配置 `application.yml`：
   - DeepSeek Chat base-url：`https://api.deepseek.com`
   - Chat 模型：`deepseek-chat`
   - temperature：`0.5`
   - API Key 改为读取环境变量 `OPENAI_API_KEY`
   - MySQL 地址改为 `localhost:3306/enterprise_hr_ai_agent`
   - MySQL 用户名：`root`
   - MySQL 密码：`0206`
   - Redis 地址：`127.0.0.1:6379`
   - Embedding 模型名：`text-embedding-v4`
2. 新增 `.env.example`，记录本项目需要的环境变量模板。
3. 更新 `README.md`，补充当前真实本地配置说明。

## 2026-05-23 第六次迭代

### 本次完成内容

1. 根据用户提供的 DashScope 信息，配置 Embedding 独立 OpenAI 兼容端点：
   - `AI_EMBEDDING_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1`
   - `AI_EMBEDDING_MODEL=text-embedding-v4`
2. Embedding API Key 改为读取环境变量 `DASHSCOPE_API_KEY`，不写入代码仓库。
3. 更新 `.env.example`，增加 DashScope embedding 环境变量模板。
4. 更新 `README.md`，说明 Chat 与 Embedding 分别使用 DeepSeek 和 DashScope。

### 待确认事项

1. 当前 Redis 仅配置连接信息，项目向量库仍使用 InMemoryVectorStore。切换 Redis Stack VectorStore 需要后续单独接入依赖和配置。

## 2026-05-23 第七次迭代

### 本次完成内容

1. 新增 AI 配置诊断模块：
   - `GET /api/v1/diagnostics/chat`
   - `GET /api/v1/diagnostics/embedding`
2. 新增 `AiDiagnosticsService`，分别对 DeepSeek Chat 和 DashScope Embedding 发起最小真实调用。
3. 新增 `AiDiagnosticResponse`，统一返回 provider、status、message 和 embedding 维度。
4. 诊断接口不会输出 API Key，并对常见认证字段做脱敏处理。
5. 更新 `README.md`，增加模型诊断接口说明。

### 当前状态

用户在 IDEA 中配置好 `OPENAI_API_KEY` 和 `DASHSCOPE_API_KEY` 后，可以先访问诊断接口判断 Chat 与 Embedding 是否可用，再继续做 RAG 上传和流式对话联调。

## 2026-05-23 第八次迭代

### 本次完成内容

1. 新增知识库文档元数据表 `knowledge_document`。
2. 新增实体和 Mapper：
   - `KnowledgeDocument`
   - `KnowledgeDocumentMapper`
3. 新增 DTO 和服务：
   - `KnowledgeDocumentResponse`
   - `KnowledgeDocumentService`
4. 改造 RAG 文档入库流程，文档成功写入 VectorStore 后同步记录元数据。
5. 新增知识库文档查询接口：
   - `GET /api/v1/knowledge/documents`
   - `GET /api/v1/knowledge/documents/{id}`
6. 更新 MySQL、local H2、test H2 建表脚本。
7. 修复 `KnowledgeIngestionService` 中显示异常的中文注释。

### 当前状态

RAG 入库现在不仅写入向量库，也能在关系型数据库中追踪文档索引记录。后续可以在后台页面展示知识库文档列表，并支持重建索引、删除文档等扩展能力。

## 2026-05-23 第九次迭代

### 本次完成内容

1. 新增对话会话与消息历史表：
   - `chat_session`
   - `chat_message`
2. 新增实体、Mapper、DTO、Service 和 Controller。
3. 新增会话管理接口：
   - `POST /api/v1/chat/sessions`
   - `GET /api/v1/chat/sessions`
   - `GET /api/v1/chat/sessions/{sessionId}/messages`
4. 扩展 `ChatRequest`，支持传入 `sessionId`。
5. 改造 SSE 流式对话服务：
   - 请求带 `sessionId` 时保存用户问题
   - 模型流式输出完成后保存助手完整回复
6. 重写 `ChatService` 和 `ChatRequest` 中显示异常的中文注释和提示文本。
7. 更新 MySQL、local H2、test H2 建表脚本。

### 当前状态

后端已经具备对话会话概念。前端可以先创建会话，再用该 `sessionId` 调用流式聊天接口，最后查询历史消息用于展示聊天记录。

## 2026-05-23 第十次迭代

### 本次完成内容

1. 扩展 `ChatHistoryService`，新增最近 N 条消息查询能力。
2. 改造 `ChatService`，带 `sessionId` 的流式对话会把最近历史消息注入 System Prompt。
3. 明确 Agent 编排规则：历史消息只用于理解上下文和指代关系，制度问题仍以 RAG Context 为依据。
4. 新增配置项 `app.ai.history-size`，默认注入最近 8 条消息。
5. 新增 `ChatHistoryServiceTest`，验证最近消息窗口会按原始对话顺序返回。
6. 更新 `README.md`，补充会话上下文能力说明。

### 当前状态

聊天模块现在不仅能保存历史，还能在后续追问中利用最近对话内容，支持更自然的连续 HR 咨询。

## 2026-05-23 第十一次迭代

### 本次完成内容

1. 新增知识库 chunk 索引表 `knowledge_chunk`，记录业务文档和 VectorStore 向量 ID 的关系。
2. 新增实体和 Mapper：
   - `KnowledgeChunk`
   - `KnowledgeChunkMapper`
3. 改造 RAG 入库流程：
   - 先创建 `knowledge_document` 记录并标记为 `INDEXING`
   - 给每个 chunk 注入 `knowledgeDocumentId`、`filename`、`chunkIndex` 元数据
   - 向量写入成功后保存 chunk 索引并把文档状态改为 `INDEXED`
   - 入库异常时把文档状态标记为 `FAILED`
4. 新增知识库删除接口：
   - `DELETE /api/v1/knowledge/documents/{id}`
5. 删除文档时会按 `knowledge_chunk.vector_id` 从 VectorStore 移除对应向量，并把文档状态标记为 `DELETED`。
6. 更新 MySQL、local H2、test H2 建表脚本。
7. 新增控制器测试，验证删除文档会返回 `DELETED` 状态。

### 当前状态

知识库模块已经从“只入库”升级为可追踪、可删除的管理闭环，为后续后台管理页和重复文档治理打好了基础。

## 2026-05-23 第十二次迭代

### 本次完成内容

1. 接入 springdoc OpenAPI：
   - `springdoc-openapi-starter-webflux-ui`
   - 版本 `2.8.8`
2. 新增 `OpenApiConfig`，配置 API 标题、描述、本地服务地址和 GitHub 外部文档地址。
3. 在 `application.yml` 中配置：
   - OpenAPI JSON：`/v3/api-docs`
   - Swagger UI：`/swagger-ui.html`
4. 更新 `README.md`，补充 Swagger UI 和 OpenAPI JSON 访问地址。

### 当前状态

应用启动后可以直接通过 Swagger UI 查看和调试接口，后续做真实 AI/RAG 联调和前端开发会更方便。

## 2026-05-23 第十三次迭代

### 本次完成内容

1. 新增真实 AI/RAG 联调脚本：
   - `scripts/smoke-ai-rag.ps1`
2. 新增样例制度文档：
   - `docs/samples/2026员工考勤管理办法.txt`
3. 脚本覆盖完整链路：
   - 检查 `/actuator/health`
   - 检查 `/api/v1/diagnostics/chat`
   - 检查 `/api/v1/diagnostics/embedding`
   - 上传样例制度文档到知识库
   - 创建对话会话
   - 调用 `/api/v1/chat/stream` 进行 SSE 流式对话
   - 查询会话消息，确认用户问题和助手回复已落库
4. 更新 `README.md`，补充真实 AI/RAG 联调步骤。

### 当前状态

当应用在 IDEA 中以默认 profile 启动，并且 DeepSeek 与 DashScope 环境变量配置正确后，可以直接运行脚本验证真实模型、RAG 入库、流式对话和会话落库的端到端链路。

## 2026-05-23 第十四次迭代

### 本次完成内容

1. 接入 Spring AI Redis VectorStore Starter：
   - `spring-ai-starter-vector-store-redis`
2. 改造 `VectorStoreConfig`：
   - 默认 `spring.ai.vectorstore.type=in-memory` 时创建 `SimpleVectorStore`
   - 启用 `spring.ai.vectorstore.type=redis` 时交给 Spring AI 自动配置 `RedisVectorStore`
3. 新增 `application-redis.yml`：
   - 使用 Jedis 客户端
   - 配置 Redis Stack 地址
   - 配置 RediSearch 索引名和 key 前缀
   - 默认自动初始化向量索引
4. 更新默认、local、test 配置，确保未启用 redis profile 时仍使用内存向量库。
5. 更新 `README.md`，补充 Redis Stack VectorStore 的启动方式和环境变量。

### 当前状态

项目现在支持两种向量库模式：默认内存向量库用于快速开发，`redis` profile 用于 Redis Stack 持久化向量存储。业务层仍只依赖 Spring AI `VectorStore` 接口。

## 2026-05-23 第十五次迭代

### 本次完成内容

1. 新增轻量级企业网关鉴权过滤器：
   - `LightweightSecurityWebFilter`
2. 新增安全配置属性：
   - `SecurityProperties`
3. 新增 `application-secure.yml`，启用后要求业务 API 携带员工身份请求头：
   - `X-HR-EMPLOYEE-NAME`
4. 管理类写接口要求管理员角色：
   - `X-HR-ROLE: ADMIN`
5. 默认 profile 仍关闭鉴权，避免影响本地开发和 Swagger 调试。
6. 新增 `SecurityWebFilterTest`，验证：
   - 缺少员工头返回 401
   - 普通员工可访问普通查询接口
   - 普通员工访问管理写接口返回 403
   - 管理员可访问管理写接口
7. 更新 `README.md`，补充 secure profile 启动和请求头示例。

### 当前状态

项目已有基础身份边界。它模拟企业 SSO/网关向后端透传员工身份的模式，后续可以替换为 OAuth2/JWT，但业务 Controller 和 Service 不需要重写。

## 2026-05-24 第十六次迭代

### 本次完成内容

1. 接入 Flyway 数据库迁移：
   - `flyway-core`
   - `flyway-mysql`
2. 默认 MySQL 环境关闭 `schema.sql/data.sql` 自动初始化，改由 Flyway 管理版本化迁移。
3. 新增迁移脚本：
   - `V1__init_schema.sql`：创建业务表
   - `V2__seed_demo_data.sql`：导入样例员工和请假记录
4. `local` 和 `test` profile 继续关闭 Flyway，沿用 H2 初始化脚本，保证本地快速启动和 CI 测试稳定。
5. 更新 `README.md`，补充 Flyway 迁移说明。

### 当前状态

默认 MySQL 环境具备版本化数据库迁移能力。后续表结构变更应新增迁移脚本，而不是直接改历史 SQL。
