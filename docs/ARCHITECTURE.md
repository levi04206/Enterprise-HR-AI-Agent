# Enterprise HR AI Agent 架构说明

## 1. 总体架构

```mermaid
flowchart LR
    U[员工浏览器] --> UI[内置演示控制台]
    UI --> REST[REST API]
    UI --> SSE[SSE 流式对话]
    REST --> CTRL[Controller 层]
    SSE --> CHAT[ChatService Agent 编排]
    CTRL --> BIZ[业务 Service 层]
    CHAT --> RAG[VectorStore 相似度检索]
    CHAT --> LLM[Spring AI ChatClient]
    CHAT --> HIST[ChatHistoryService]
    CHAT --> RLOG[RAG 引用日志]
    LLM --> TOOLS[Function Calling Tools]
    TOOLS --> HRDATA[员工/请假数据]
    TOOLS --> TLOG[工具调用审计]
    BIZ --> MYSQL[(MySQL / H2)]
    HIST --> MYSQL
    HRDATA --> MYSQL
    RLOG --> MYSQL
    TLOG --> MYSQL
    RAG --> MEM[InMemory VectorStore]
    RAG --> REDIS[Redis Stack VectorStore]
    FLYWAY[Flyway Migration] --> MYSQL
```

核心分层：

- 前端演示层：Spring Boot 静态资源，访问 `http://localhost:8080/`。
- API 层：RESTful API + SSE 流式接口。
- Agent 编排层：`ChatService` 负责 RAG、历史消息、Prompt、ChatClient 流式输出。
- 工具层：`HrToolsConfig` 暴露 Function Calling 工具。
- 数据层：MySQL + MyBatis-Plus，测试和 local profile 使用 H2。
- 向量层：默认 InMemoryVectorStore，`redis` profile 可切换 Redis Stack。
- 工程化：Flyway、Swagger、Actuator、统一异常、轻量级鉴权、CORS、CI。

## 2. RAG 入库流程

```mermaid
sequenceDiagram
    participant Admin as HR/Admin
    participant API as KnowledgeController
    participant Ingest as KnowledgeIngestionService
    participant Reader as TikaDocumentReader
    participant Splitter as TokenTextSplitter
    participant Embedding as EmbeddingModel
    participant Vector as VectorStore
    participant DB as MySQL

    Admin->>API: 上传 PDF/TXT
    API->>Ingest: ingest(file)
    Ingest->>Reader: 读取文档内容
    Reader-->>Ingest: raw documents
    Ingest->>Splitter: 文本分块
    Splitter-->>Ingest: chunks
    Ingest->>DB: 保存 knowledge_document=INDEXING
    Ingest->>Vector: add(chunks)
    Vector->>Embedding: 生成向量
    Embedding-->>Vector: embeddings
    Vector-->>Ingest: 写入成功
    Ingest->>DB: 保存 knowledge_chunk 索引
    Ingest->>DB: 更新 knowledge_document=INDEXED
```

这个流程体现了 RAG 的知识更新能力：公司制度可以通过上传文档进入知识库，不依赖模型训练数据。

## 3. 对话与 RAG 检索流程

```mermaid
sequenceDiagram
    participant User as 员工
    participant UI as 演示控制台
    participant ChatAPI as ChatController
    participant Chat as ChatService
    participant History as ChatHistoryService
    participant Vector as VectorStore
    participant RLog as RagSearchLogService
    participant LLM as ChatClient
    participant DB as MySQL

    User->>UI: 输入问题
    UI->>ChatAPI: POST /api/v1/chat/stream
    ChatAPI->>Chat: streamChat(message, sessionId)
    Chat->>History: 读取最近 N 条历史消息
    Chat->>DB: 保存 USER 消息
    Chat->>Vector: similaritySearch TopK
    Vector-->>Chat: 命中文档片段
    Chat->>RLog: 保存 rag_search_log
    Chat->>LLM: System Prompt + Context + History
    LLM-->>ChatAPI: token stream
    ChatAPI-->>UI: SSE data
    Chat->>DB: 保存 ASSISTANT 完整回答
```

重点设计：

- `Context` 只来自向量检索命中的制度片段。
- 历史消息只帮助理解上下文，不作为制度依据。
- `rag_search_log` 记录模型回答前检索到了哪些片段，便于排查和展示引用来源。

## 4. Function Calling 时序

```mermaid
sequenceDiagram
    participant User as 员工
    participant Chat as ChatService
    participant LLM as 大模型
    participant Tool as HR Tool
    participant DB as MySQL
    participant Audit as ToolCallLogService

    User->>Chat: 张三还剩多少年假？
    Chat->>LLM: Prompt + 工具定义
    LLM->>Tool: getLeaveBalanceTool(employeeName=张三)
    Tool->>Audit: 开始记录工具调用
    Tool->>DB: 查询 employee / leave_record
    DB-->>Tool: 年假和审批中记录
    Tool->>Audit: 保存入参、结果、耗时、成功状态
    Tool-->>LLM: 确定性业务结果
    LLM-->>Chat: 组织自然语言回答
```

重点设计：

- 模型负责理解用户意图和组织回答。
- 工具负责提供确定性业务数据。
- 审计日志记录模型访问内部能力的全过程。

## 5. 数据库职责划分

| 表 | 作用 |
| --- | --- |
| `employee` | 员工基础信息、年假总数、已用年假 |
| `leave_record` | 请假记录和审批状态 |
| `knowledge_document` | 知识库文档元数据 |
| `knowledge_chunk` | 文档 chunk 和向量 ID 的关联 |
| `chat_session` | 对话会话 |
| `chat_message` | 对话消息 |
| `rag_search_log` | RAG 检索引用来源 |
| `tool_call_log` | Function Calling 工具调用审计 |

## 6. 关键代码入口

| 模块 | 文件 |
| --- | --- |
| Agent 编排 | `src/main/java/com/example/enterprisehraiagent/service/ChatService.java` |
| RAG 入库 | `src/main/java/com/example/enterprisehraiagent/service/KnowledgeIngestionService.java` |
| 工具定义 | `src/main/java/com/example/enterprisehraiagent/tool/HrToolsConfig.java` |
| RAG 日志 | `src/main/java/com/example/enterprisehraiagent/service/RagSearchLogService.java` |
| 工具审计 | `src/main/java/com/example/enterprisehraiagent/service/ToolCallLogService.java` |
| AI 配置 | `src/main/java/com/example/enterprisehraiagent/config/AiConfig.java` |
| 向量库配置 | `src/main/java/com/example/enterprisehraiagent/config/VectorStoreConfig.java` |
| 演示控制台 | `src/main/resources/static/index.html` |

## 7. 面试讲解顺序

1. 先说明业务目标：统一 HR 对话窗口。
2. 讲 RAG：制度问题必须基于企业知识库。
3. 讲 Function Calling：个人数据必须查内部系统。
4. 讲 SSE：大模型回答需要流式体验。
5. 讲工程化：Flyway、测试、CI、Swagger、异常、鉴权、CORS。
6. 讲可观测性：RAG 引用来源和工具调用审计。
