# Enterprise HR AI Agent

基于 Spring Boot 3.5、Spring AI 1.1.6、MyBatis-Plus、MySQL 和内存向量库的企业 HR 智能体 Demo。

## 核心能力

- 上传企业制度 PDF/TXT 文档，完成读取、分块、Embedding 和向量入库。
- `/api/v1/chat/stream` 提供 SSE 流式对话。
- RAG 检索企业制度 Top 3 片段后注入 System Prompt。
- Function Calling 工具支持查询员工联系方式和年假余额。

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

4. Redis 默认配置：

```yaml
host: 127.0.0.1
port: 6379
```

说明：当前向量库仍使用 InMemoryVectorStore，Redis 配置先作为后续切换 Redis Stack VectorStore 的基础。

5. 模型配置说明：

- Chat 使用 DeepSeek OpenAI 兼容接口。
- Embedding 使用 DashScope OpenAI 兼容模式。
- 两个 API Key 分别从 `OPENAI_API_KEY` 和 `DASHSCOPE_API_KEY` 读取，避免把密钥写入代码。

模型诊断接口：

```bash
curl http://localhost:8080/api/v1/diagnostics/chat
curl http://localhost:8080/api/v1/diagnostics/embedding
```

这两个接口会真实调用模型服务。返回 `status=UP` 表示对应模型配置可用。

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

## 常用接口

上传知识库文档：

```bash
curl -F "file=@2026员工考勤管理办法.txt" http://localhost:8080/api/v1/knowledge/ingest
```

查询已入库知识库文档：

```bash
curl http://localhost:8080/api/v1/knowledge/documents
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
