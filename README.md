# Enterprise HR AI Agent

基于 Spring Boot 3.5、Spring AI 1.1.6、MyBatis-Plus、MySQL 和内存向量库的企业 HR 智能体 Demo。

## 核心能力

- 上传企业制度 PDF/TXT 文档，完成读取、分块、Embedding 和向量入库。
- 记录知识库文档和向量 chunk 索引，支持按文档删除向量。
- `/api/v1/chat/stream` 提供 SSE 流式对话。
- RAG 检索企业制度 Top 3 片段后注入 System Prompt。
- Function Calling 工具支持查询员工联系方式和年假余额。
- 带 `sessionId` 的对话会自动保存历史，并把最近 8 条消息注入模型上下文。

## 启动前准备

1. 使用 Docker 启动本地 MySQL 和 Redis Stack：

```powershell
docker compose up -d
```

也可以手动创建 MySQL 数据库：

```sql
CREATE DATABASE enterprise_hr_ai_agent DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 配置环境变量：

```powershell
$env:OPENAI_API_KEY="你的 OpenAI 兼容 API Key"
$env:AI_BASE_URL="https://api.deepseek.com"
$env:AI_CHAT_MODEL="deepseek-chat"
$env:DASHSCOPE_API_KEY="你的 DashScope API Key"
$env:AI_EMBEDDING_BASE_URL="https://dashscope.aliyuncs.com/compatible-mode/v1"
$env:AI_EMBEDDING_MODEL="text-embedding-v4"
```

3. 默认 MySQL 配置已经按当前项目设置：

```yaml
url: jdbc:mysql://localhost:3306/enterprise_hr_ai_agent
username: root
password: '0206'
```

默认 MySQL 环境使用 Flyway 自动执行 `src/main/resources/db/migration` 下的版本化迁移脚本。首次启动会创建业务表并导入样例员工数据；后续表结构变化应新增 `V{版本号}__说明.sql`，不要直接改已经发布的迁移脚本。

4. Redis 默认配置：

```yaml
host: 127.0.0.1
port: 6379
```

说明：默认 profile 使用 InMemory VectorStore，便于没有 Redis Stack 时先跑通主体功能。启用 `redis` profile 后会切换到 Redis Stack VectorStore。

5. 模型配置说明：

- Chat 使用 DeepSeek OpenAI 兼容接口。
- Embedding 使用 DashScope OpenAI 兼容模式。
- 两个 API Key 分别从 `OPENAI_API_KEY` 和 `DASHSCOPE_API_KEY` 读取，避免把密钥写入代码。
- `app.ai.history-size` 控制每次对话注入的最近历史消息条数，默认 8。

模型诊断接口：

```bash
curl http://localhost:8080/api/v1/diagnostics/chat
curl http://localhost:8080/api/v1/diagnostics/embedding
```

这两个接口会真实调用模型服务。返回 `status=UP` 表示对应模型配置可用。

## 接口文档

启动应用后可以通过 Swagger UI 调试接口：

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON 地址：

```text
http://localhost:8080/v3/api-docs
```

## 本地快速启动

如果暂时没有 MySQL、Redis 或真实大模型 API，可以先用 `local` profile 跑通主体服务：

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

健康检查：

```bash
curl http://localhost:8080/actuator/health
```

查询本地样例员工：

```bash
curl "http://localhost:8080/api/v1/employees?keyword=研发"
```

一键 smoke test：

```powershell
.\scripts\smoke-local.ps1
```

说明：`local` profile 的 Chat/RAG 真实调用仍需要可用的大模型和 Embedding API。它的用途是先验证后端主体服务、数据库映射和普通 REST 接口。

真实 AI/RAG 联调：

先在 IDEA 中使用默认 profile 启动应用，并确保 `OPENAI_API_KEY`、`DASHSCOPE_API_KEY` 已配置到启动环境。应用启动后执行：

```powershell
.\scripts\smoke-ai-rag.ps1
```

该脚本会依次检查健康端点、Chat 模型、Embedding 模型，上传 `docs/samples/2026员工考勤管理办法.txt`，创建会话，并调用 SSE 对话接口验证 RAG 回答和消息落库。

## Redis Stack VectorStore

默认启动仍使用内存向量库：

```powershell
mvn spring-boot:run
```

如果要让知识库向量持久化到 Redis Stack，先确保本机 `127.0.0.1:6379` 是 Redis Stack，而不是普通 Redis。Redis Stack 需要包含 RediSearch 和 RedisJSON 模块。

然后使用 `redis` profile 启动：

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=redis"
```

可配置项：

```powershell
$env:REDIS_HOST="127.0.0.1"
$env:REDIS_PORT="6379"
$env:REDIS_PASSWORD=""
$env:AI_REDIS_VECTOR_INDEX="enterprise_hr_ai_agent_idx"
$env:AI_REDIS_VECTOR_PREFIX="enterprise_hr_ai_agent:doc:"
$env:AI_REDIS_VECTOR_INITIALIZE_SCHEMA="true"
```

也可以不启用 profile，而是直接设置：

```powershell
$env:AI_VECTOR_STORE_TYPE="redis"
```

但更推荐使用 `redis` profile，因为它会同时补齐 Redis VectorStore 的索引名、key 前缀和 Jedis 客户端配置。

## 基础鉴权

默认开发模式关闭鉴权，便于本地调试。启用 `secure` profile 后，业务 API 需要携带员工身份请求头：

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=secure"
```

普通员工请求示例：

```bash
curl -H "X-HR-EMPLOYEE-NAME: 张三" http://localhost:8080/api/v1/employees
```

管理接口请求示例：

```bash
curl -H "X-HR-EMPLOYEE-NAME: 管理员" \
  -H "X-HR-ROLE: ADMIN" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"赵六\",\"department\":\"财务部\",\"email\":\"zhaoliu@example.com\",\"annualLeaveTotal\":10,\"annualLeaveUsed\":0}" \
  http://localhost:8080/api/v1/employees
```

当前鉴权模式模拟企业网关/SSO 把员工身份写入请求头。后续接入真实 OAuth2/JWT 时，可以替换过滤器实现，业务接口不需要大改。

## 常用接口

上传知识库文档：

```bash
curl -F "file=@2026员工考勤管理办法.txt" http://localhost:8080/api/v1/knowledge/ingest
```

查询已入库知识库文档：

```bash
curl http://localhost:8080/api/v1/knowledge/documents
```

删除知识库文档及其向量索引：

```bash
curl -X DELETE http://localhost:8080/api/v1/knowledge/documents/1
```

流式对话：

```bash
curl -N -H "Content-Type: application/json" \
  -d "{\"message\":\"张三还剩多少年假？\",\"sessionId\":1}" \
  http://localhost:8080/api/v1/chat/stream
```

创建对话会话：

```bash
curl -H "Content-Type: application/json" \
  -d "{\"title\":\"年假咨询\"}" \
  http://localhost:8080/api/v1/chat/sessions
```

查询会话消息：

```bash
curl http://localhost:8080/api/v1/chat/sessions/1/messages
```

新增员工：

```bash
curl -H "Content-Type: application/json" \
  -d "{\"name\":\"王五\",\"department\":\"财务部\",\"email\":\"wangwu@example.com\",\"annualLeaveTotal\":10,\"annualLeaveUsed\":1}" \
  http://localhost:8080/api/v1/employees
```

查询员工：

```bash
curl "http://localhost:8080/api/v1/employees?keyword=研发"
```

新增请假记录：

```bash
curl -H "Content-Type: application/json" \
  -d "{\"empId\":1,\"startDate\":\"2026-07-01\",\"endDate\":\"2026-07-02\",\"status\":\"PENDING\"}" \
  http://localhost:8080/api/v1/leave-records
```

## 自动化验证

测试环境使用 H2 内存数据库，不依赖本地 MySQL、Redis 或真实大模型 API：

```powershell
mvn test
```

仓库已配置 GitHub Actions CI，每次推送到 `master` 会自动执行 `mvn test`。

打包：

```powershell
mvn "-Dmaven.test.skip=true" package
```

## 统一错误响应

普通 REST 接口的异常会统一返回结构化 JSON，便于前端根据 `status`、`message` 和 `details` 做提示：

```json
{
  "timestamp": "2026-05-24T20:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "请求参数校验失败",
  "path": "/api/v1/employees",
  "details": {
    "name": "员工姓名不能为空"
  }
}
```

说明：SSE 流式对话接口在模型输出过程中发生的错误仍按流式响应链路处理，前端需要同时监听 SSE 连接异常。
