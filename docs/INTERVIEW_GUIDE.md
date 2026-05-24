# Enterprise HR AI Agent 面试讲解指南

## 项目定位

Enterprise HR AI Agent 是一个面向企业员工的 HR 智能助理后端项目。它把传统 HR 数据查询和大模型能力结合起来：员工既可以询问公司制度，也可以查询年假余额、同事联系方式等个人数据。

这个项目适合作为 Java 后端实习简历项目，因为它不是单纯 CRUD，而是覆盖了 Spring Boot 工程化、数据库建模、REST API、SSE 流式响应、RAG、Function Calling、测试和 CI。

## 推荐简历写法

项目名称：Enterprise HR AI Agent 企业 HR 智能助理

技术栈：Spring Boot 3、JDK 21、Spring AI、MyBatis-Plus、MySQL、Flyway、Redis Stack VectorStore、WebFlux、SSE、Swagger、GitHub Actions

项目描述：
基于 Spring AI 构建企业 HR 智能助理，支持员工通过统一对话窗口查询公司制度、年假余额和同事联系方式。系统通过 RAG 检索企业知识库，通过 Function Calling 调用内部 HR 数据工具，并使用 SSE 将模型回答实时推送给前端。

可写亮点：

1. 设计并实现 RAG 知识库入库链路，支持 PDF/TXT 上传、文档切分、Embedding 向量化、向量检索和上下文注入。
2. 使用 Spring AI Function Calling 封装 HR 工具，支持模型按需查询员工联系方式和年假余额。
3. 实现基于 WebFlux 的 SSE 流式对话接口，提升大模型回答的实时反馈体验。
4. 设计员工、请假、知识库文档、知识库 chunk、对话会话、对话消息等业务表，并使用 Flyway 管理数据库迁移。
5. 补充 Swagger、统一异常响应、轻量级鉴权、CORS、H2 测试环境、Smoke Test 和 GitHub Actions CI。
6. 增加 RAG 引用来源和 Function Calling 工具调用审计，支持追踪模型回答依据和内部工具调用过程。

## 面试讲解主线

### 1. 用户请求如何进入 Agent

用户调用 `/api/v1/chat/stream`，后端接收 `message` 和可选 `sessionId`。

处理流程：

1. 保存用户消息到 `chat_message`。
2. 使用用户问题去 `VectorStore` 检索 Top 3 制度片段。
3. 读取最近 N 条历史消息，帮助模型理解上下文。
4. 构造 System Prompt，把 HR 助理人设、RAG Context、历史消息一起注入。
5. ChatClient 发起流式调用，并启用 HR 工具函数。
6. SSE 持续返回模型 token。
7. 回答结束后保存完整助手消息。
8. 保存 RAG 检索日志和工具调用日志，用于排查模型回答依据和审计内部系统访问。

### 2. RAG 在项目里的作用

RAG 解决的是“公司制度不能只靠模型记忆”的问题。公司考勤、年假、报销等制度经常变化，直接问大模型可能出现幻觉，所以项目会先检索企业知识库，把相关制度片段作为上下文交给模型。

入库链路：

```text
上传 PDF/TXT -> TikaDocumentReader 读取 -> TokenTextSplitter 分块 -> EmbeddingModel 向量化 -> VectorStore 存储 -> 关系型数据库记录文档和 chunk 元数据
```

检索链路：

```text
用户问题 -> SimilaritySearch Top K -> 拼接 Context -> System Prompt -> ChatClient
```

项目还会把每次检索命中的片段写入 `rag_search_log`，包括会话 ID、用户问题、排名、文档 ID、文件名、chunk 编号、向量 ID、相似度分数和内容摘要。这样面试时可以强调：RAG 不只是把上下文塞给模型，还能追踪回答依据。

### 3. Function Calling 在项目里的作用

Function Calling 解决的是“个人业务数据必须查内部系统”的问题。比如年假余额、同事邮箱不应该由模型编造，而应该调用后端工具方法查询数据库。

当前工具：

1. `getEmployeeContactTool`：输入员工姓名，返回部门和邮箱。
2. `getLeaveBalanceTool`：输入员工姓名，返回剩余年假和最近审批中的请假记录。

面试表达重点：模型负责理解意图和组织语言，业务系统负责提供确定性数据。

每次工具调用都会写入 `tool_call_log`，记录工具名、结构化入参、返回结果、是否成功、错误信息和耗时。这个设计可以用来说明 Agent 访问内部系统必须可审计，不能只关注回答结果。

### 4. 为什么使用 SSE

大模型回答通常耗时较长。如果普通 HTTP 等完整结果生成完再返回，用户体验会差。SSE 可以把 token 增量推给前端，前端边接收边渲染。

项目接口：

```text
POST /api/v1/chat/stream
Content-Type: application/json
Accept: text/event-stream
```

### 5. 数据库设计怎么讲

核心业务表：

1. `employee`：员工基础信息和年假总数、已用年假。
2. `leave_record`：请假记录和审批状态。
3. `knowledge_document`：知识库文档元数据。
4. `knowledge_chunk`：文档 chunk 与向量 ID 的关系。
5. `chat_session`：对话会话。
6. `chat_message`：会话消息。
7. `rag_search_log`：RAG 检索引用来源。
8. `tool_call_log`：Function Calling 工具调用审计。

设计思路：

关系型数据库保存业务事实和可追踪元数据，向量数据库保存语义检索索引，两者通过 `knowledge_chunk.vector_id` 关联。

## 常见面试问题

### Q1：为什么不用模型直接回答公司制度？

因为公司制度属于私有知识且会更新，模型训练数据里不一定有，也可能过期。RAG 可以把最新制度文档检索出来作为上下文，降低幻觉。

### Q2：RAG 检索不到内容怎么办？

当前 System Prompt 要求模型优先基于 Context 回答。如果 Context 不足，应提示用户补充文档或联系 HR。后续可以增加置信度阈值和“未命中文档”的明确兜底。

### Q3：为什么个人数据要用 Function Calling？

个人年假、邮箱等数据需要实时、准确、可审计，不能让模型猜。Function Calling 把模型的自然语言理解能力和后端确定性查询结合起来。

### Q4：为什么同时有 MySQL 和 VectorStore？

MySQL 管业务数据、文档元数据、会话历史；VectorStore 管文本向量，用于语义检索。两者职责不同。

### Q5：怎么保证项目可测试？

项目提供 H2 test profile，不依赖本地 MySQL、Redis 和真实模型 API；控制器和服务层有集成测试；GitHub Actions 每次 push 自动运行 `mvn test`。

### Q6：如何排查模型回答是否可信？

可以查 `rag_search_log` 看本次问题命中了哪些制度片段，也可以查 `tool_call_log` 看模型是否调用了 HR 工具、入参是什么、工具返回了什么。这样回答依据和内部系统访问过程都可以追踪。

## 演示流程

1. 启动后端：

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

2. 打开演示控制台：

```text
http://localhost:8080/
```

3. 查看员工数据，确认基础 REST API 正常。

4. 创建聊天会话。

5. 如果使用真实默认 profile，上传 `docs/samples/2026员工考勤管理办法.txt`。

6. 提问：

```text
张三还剩多少年假？
```

7. 再提问：

```text
公司的迟到规则是什么？
```

8. 在演示控制台右侧查看 RAG 引用和工具审计，说明模型回答依据和 Function Calling 调用过程。

## 后续可扩展点

1. 接入 OAuth2/JWT，把轻量级 Header 鉴权替换成正式认证。
2. 增加知识库重建索引和文档版本管理。
3. 增加向量检索命中分数展示和低置信度兜底。
4. 增加前端登录态、会话删除、文档删除按钮。
5. 增加真实企业系统 API Mock Server，展示 Function Calling 调用外部系统。
