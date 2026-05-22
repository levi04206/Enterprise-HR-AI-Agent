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
$env:AI_API_KEY="你的 OpenAI 兼容 API Key"
$env:AI_BASE_URL="https://api.deepseek.com"
$env:AI_CHAT_MODEL="deepseek-chat"
```

3. 修改 `src/main/resources/application.yml` 中的 MySQL 用户名和密码。

## 常用接口

上传知识库文档：

```bash
curl -F "file=@2026员工考勤管理办法.txt" http://localhost:8080/api/v1/knowledge/ingest
```

流式对话：

```bash
curl -N -H "Content-Type: application/json" \
  -d "{\"message\":\"张三还剩多少年假？\"}" \
  http://localhost:8080/api/v1/chat/stream
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

打包：

```powershell
mvn "-Dmaven.test.skip=true" package
```
