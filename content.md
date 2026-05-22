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
