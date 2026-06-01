const state = {
    sessionId: null,
    streaming: false,
    employeeEditingId: null
};

const elements = {
    employeeHeader: document.querySelector("#employeeHeader"),
    roleHeader: document.querySelector("#roleHeader"),
    sessionList: document.querySelector("#sessionList"),
    newSessionButton: document.querySelector("#newSessionButton"),
    loadMessagesButton: document.querySelector("#loadMessagesButton"),
    activeSessionText: document.querySelector("#activeSessionText"),
    messages: document.querySelector("#messages"),
    chatForm: document.querySelector("#chatForm"),
    messageInput: document.querySelector("#messageInput"),
    sendButton: document.querySelector("#sendButton"),
    uploadForm: document.querySelector("#uploadForm"),
    documentFile: document.querySelector("#documentFile"),
    documentList: document.querySelector("#documentList"),
    refreshDocumentsButton: document.querySelector("#refreshDocumentsButton"),
    documentLookupId: document.querySelector("#documentLookupId"),
    lookupDocumentButton: document.querySelector("#lookupDocumentButton"),
    employeeForm: document.querySelector("#employeeForm"),
    employeeId: document.querySelector("#employeeId"),
    employeeName: document.querySelector("#employeeName"),
    employeeDepartment: document.querySelector("#employeeDepartment"),
    employeeEmail: document.querySelector("#employeeEmail"),
    employeeAnnualTotal: document.querySelector("#employeeAnnualTotal"),
    employeeAnnualUsed: document.querySelector("#employeeAnnualUsed"),
    saveEmployeeButton: document.querySelector("#saveEmployeeButton"),
    resetEmployeeFormButton: document.querySelector("#resetEmployeeFormButton"),
    employeeKeyword: document.querySelector("#employeeKeyword"),
    searchEmployeesButton: document.querySelector("#searchEmployeesButton"),
    employeeLookupId: document.querySelector("#employeeLookupId"),
    lookupEmployeeButton: document.querySelector("#lookupEmployeeButton"),
    employeeList: document.querySelector("#employeeList"),
    refreshEmployeesButton: document.querySelector("#refreshEmployeesButton"),
    leaveRecordForm: document.querySelector("#leaveRecordForm"),
    leaveEmployeeId: document.querySelector("#leaveEmployeeId"),
    leaveStartDate: document.querySelector("#leaveStartDate"),
    leaveEndDate: document.querySelector("#leaveEndDate"),
    leaveStatus: document.querySelector("#leaveStatus"),
    saveLeaveRecordButton: document.querySelector("#saveLeaveRecordButton"),
    refreshPendingLeaveRecordsButton: document.querySelector("#refreshPendingLeaveRecordsButton"),
    refreshLeaveRecordsButton: document.querySelector("#refreshLeaveRecordsButton"),
    leaveRecordList: document.querySelector("#leaveRecordList"),
    diagnosticsButton: document.querySelector("#diagnosticsButton"),
    diagnosticsResult: document.querySelector("#diagnosticsResult"),
    refreshRagLogsButton: document.querySelector("#refreshRagLogsButton"),
    ragLogList: document.querySelector("#ragLogList"),
    refreshToolLogsButton: document.querySelector("#refreshToolLogsButton"),
    toolNameFilter: document.querySelector("#toolNameFilter"),
    filterToolLogsButton: document.querySelector("#filterToolLogsButton"),
    toolLogList: document.querySelector("#toolLogList"),
    toast: document.querySelector("#toast")
};

/**
 * 读取页面身份栏并组装后端鉴权请求头。
 */
function authHeaders() {
    const headers = {};
    const employeeName = elements.employeeHeader.value.trim();
    const role = elements.roleHeader.value.trim();
    if (employeeName) {
        headers["X-HR-EMPLOYEE-NAME"] = employeeName;
    }
    if (role) {
        headers["X-HR-ROLE"] = role;
    }
    return headers;
}

/**
 * 封装 fetch 请求，统一处理鉴权头、超时和错误消息。
 */
async function api(path, options = {}) {
    const { timeoutMs = 15000, ...fetchOptions } = options;
    const controller = new AbortController();
    const timeout = window.setTimeout(() => controller.abort(), timeoutMs);
    const headers = {
        ...authHeaders(),
        ...(options.headers || {})
    };
    let response;
    try {
        response = await fetch(path, {
            ...fetchOptions,
            headers,
            signal: controller.signal
        });
    } catch (error) {
        if (error.name === "AbortError") {
            throw new Error("访问超时，请稍后重试");
        }
        throw error;
    } finally {
        window.clearTimeout(timeout);
    }

    if (!response.ok) {
        throw new Error(await readErrorMessage(response));
    }

    const contentType = response.headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
        return response.json();
    }
    return response.text();
}

/**
 * 从失败响应中读取可展示的错误消息。
 */
async function readErrorMessage(response) {
    const fallback = `${response.status} ${response.statusText}`;
    const contentType = response.headers.get("content-type") || "";
    try {
        if (contentType.includes("application/json")) {
            return formatApiError(await response.json(), fallback);
        }
        return await response.text() || fallback;
    } catch {
        return fallback;
    }
}

/**
 * 将后端结构化错误转换为前端提示文本。
 */
function formatApiError(error, fallback) {
    const message = error.message || fallback;
    const details = error.details && typeof error.details === "object" ? error.details : {};
    const detailText = Object.entries(details)
        .filter(([, value]) => value)
        .map(([field, value]) => `${field}: ${value}`)
        .join("；");
    return detailText ? `${message}：${detailText}` : message;
}

/**
 * 在页面右下角显示短提示。
 */
function showToast(message) {
    elements.toast.textContent = message;
    elements.toast.hidden = false;
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => {
        elements.toast.hidden = true;
    }, 3200);
}

/**
 * 格式化日期时间展示。
 */
function formatDate(value) {
    if (!value) {
        return "-";
    }
    return String(value).replace("T", " ").slice(0, 19);
}

/**
 * 给列表容器渲染空状态。
 */
function setEmpty(container, text) {
    container.innerHTML = `<div class="empty">${text}</div>`;
}

/**
 * 向对话窗口追加一条消息。
 */
function appendMessage(role, content) {
    const node = document.createElement("div");
    node.className = `message ${role}`;
    node.textContent = content;
    elements.messages.appendChild(node);
    elements.messages.scrollTop = elements.messages.scrollHeight;
    return node;
}

/**
 * 加载会话列表。
 */
async function loadSessions() {
    const sessions = await api("/api/v1/chat/sessions");
    elements.sessionList.innerHTML = "";
    if (!sessions.length) {
        setEmpty(elements.sessionList, "暂无会话");
        return;
    }
    sessions.forEach(session => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = `session-item${state.sessionId === session.id ? " active" : ""}`;
        button.innerHTML = `
            <div class="item-title">${session.title || "未命名会话"}</div>
            <div class="meta">${formatDate(session.updatedAt || session.createdAt)}</div>
        `;
        button.addEventListener("click", async () => {
            state.sessionId = session.id;
            elements.activeSessionText.textContent = `当前会话 #${session.id}`;
            await loadMessages();
            await loadSessions();
        });
        elements.sessionList.appendChild(button);
    });
}

/**
 * 创建新的聊天会话。
 */
async function createSession() {
    const title = `HR 咨询 ${new Date().toLocaleString("zh-CN", { hour12: false })}`;
    const result = await api("/api/v1/chat/sessions", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title })
    });
    state.sessionId = result.id;
    elements.activeSessionText.textContent = `当前会话 #${result.id}`;
    elements.messages.innerHTML = "";
    appendMessage("system", "新会话已创建，可以开始咨询 HR 问题。");
    await loadSessions();
}

/**
 * 加载当前会话的历史消息。
 */
async function loadMessages() {
    if (!state.sessionId) {
        appendMessage("system", "请先选择或新建会话。");
        return;
    }
    const messages = await api(`/api/v1/chat/sessions/${state.sessionId}/messages`);
    elements.messages.innerHTML = "";
    if (!messages.length) {
        appendMessage("system", "当前会话暂无历史消息。");
        return;
    }
    messages.forEach(message => {
        appendMessage(message.role === "USER" ? "user" : "assistant", message.content);
    });
}

/**
 * 发送用户消息并读取模型流式响应。
 */
async function sendMessage(event) {
    event.preventDefault();
    if (state.streaming) {
        return;
    }

    const message = elements.messageInput.value.trim();
    if (!message) {
        showToast("请输入问题");
        return;
    }

    elements.messageInput.value = "";
    appendMessage("user", message);
    const assistantNode = appendMessage("assistant", "");
    state.streaming = true;
    elements.sendButton.disabled = true;
    elements.sendButton.textContent = "生成中";

    try {
        const response = await fetch("/api/v1/chat/stream", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                ...authHeaders()
            },
            body: JSON.stringify({
                message,
                sessionId: state.sessionId
            })
        });

        if (!response.ok || !response.body) {
            throw new Error(await readErrorMessage(response));
        }

        await readEventStream(response, data => {
            if (data === "[DONE]") {
                return;
            }
            assistantNode.textContent += data;
            elements.messages.scrollTop = elements.messages.scrollHeight;
        });
        await loadSessions();
        await Promise.all([
            loadRagLogs().catch(error => showToast(error.message)),
            loadToolLogs().catch(error => showToast(error.message))
        ]);
    } catch (error) {
        assistantNode.textContent = `对话失败：${error.message}`;
    } finally {
        state.streaming = false;
        elements.sendButton.disabled = false;
        elements.sendButton.textContent = "发送";
    }
}

/**
 * 加载 RAG 检索引用日志。
 */
async function loadRagLogs() {
    const sessionQuery = state.sessionId ? `?sessionId=${state.sessionId}&limit=10` : "?limit=10";
    const logs = await api(`/api/v1/observability/rag-search-logs${sessionQuery}`);
    elements.ragLogList.innerHTML = "";
    if (!logs.length) {
        setEmpty(elements.ragLogList, "暂无 RAG 引用记录");
        return;
    }
    logs.forEach(log => {
        const node = document.createElement("div");
        node.className = "audit-item";
        node.innerHTML = `
            <div class="item-title">#${log.rankNo} ${log.filename || "未知文档"}</div>
            <div class="meta">chunk ${log.chunkIndex ?? "-"} · ${formatDate(log.createdAt)}</div>
            <div class="audit-snippet">${log.contentPreview || ""}</div>
        `;
        elements.ragLogList.appendChild(node);
    });
}

/**
 * 加载工具调用审计日志。
 */
async function loadToolLogs() {
    const toolName = elements.toolNameFilter.value.trim();
    const query = new URLSearchParams({ limit: "10" });
    if (toolName) {
        query.set("toolName", toolName);
    }
    const logs = await api(`/api/v1/observability/tool-call-logs?${query.toString()}`);
    elements.toolLogList.innerHTML = "";
    if (!logs.length) {
        setEmpty(elements.toolLogList, toolName ? `暂无包含「${toolName}」的工具调用记录` : "暂无工具调用记录");
        return;
    }
    logs.forEach(log => {
        const node = document.createElement("div");
        node.className = "audit-item";
        node.innerHTML = `
            <div class="item-title">${log.toolName}</div>
            <div class="meta">${log.success ? "成功" : "失败"} · ${log.durationMs}ms · ${formatDate(log.createdAt)}</div>
            <div class="audit-snippet">${log.argumentsJson || ""}</div>
        `;
        elements.toolLogList.appendChild(node);
    });
}

/**
 * 解析后端 SSE 数据流。
 */
async function readEventStream(response, onData) {
    const reader = response.body.getReader();
    const decoder = new TextDecoder("utf-8");
    let buffer = "";

    while (true) {
        const { value, done } = await reader.read();
        if (done) {
            break;
        }
        buffer += decoder.decode(value, { stream: true });
        const events = buffer.split("\n\n");
        buffer = events.pop() || "";
        events.forEach(rawEvent => {
            rawEvent.split("\n")
                .filter(line => line.startsWith("data:"))
                .map(line => line.slice(5).trimStart())
                .forEach(onData);
        });
    }
}

/**
 * 上传知识库文档。
 */
async function uploadDocument(event) {
    event.preventDefault();
    const file = elements.documentFile.files[0];
    if (!file) {
        showToast("请选择 PDF 或 TXT 文档");
        return;
    }
    const body = new FormData();
    body.append("file", file);
    await api("/api/v1/knowledge/ingest", {
        method: "POST",
        body
    });
    elements.documentFile.value = "";
    showToast("文档已入库");
    await loadDocuments();
}

/**
 * 加载知识库文档列表。
 */
async function loadDocuments() {
    const documents = await api("/api/v1/knowledge/documents");
    renderDocuments(documents);
}

/**
 * 按文档 ID 查询知识库文档。
 */
async function lookupDocumentById() {
    const id = elements.documentLookupId.value.trim();
    if (!id) {
        showToast("请输入文档 ID");
        return;
    }
    const documentItem = await api(`/api/v1/knowledge/documents/${encodeURIComponent(id)}`);
    renderDocuments([documentItem]);
}

/**
 * 渲染知识库文档列表。
 */
function renderDocuments(documents) {
    elements.documentList.innerHTML = "";
    if (!documents.length) {
        setEmpty(elements.documentList, "暂无知识库文档");
        return;
    }
    documents.forEach(documentItem => {
        const node = document.createElement("div");
        const status = (documentItem.status || "").toLowerCase();
        node.className = "document-item";
        node.innerHTML = `
            <div class="item-title">${documentItem.filename}</div>
            <div class="meta">#${documentItem.id} · ${documentItem.contentType || "-"} · ${documentItem.chunkCount || 0} chunks</div>
            <div class="meta">${formatDate(documentItem.createdAt)}</div>
            <div class="meta"><span class="status ${status}">${documentItem.status}</span></div>
            <div class="employee-actions">
                <button type="button" data-action="detail">详情</button>
                <button class="danger-button" type="button" data-action="delete">删除</button>
            </div>
        `;
        node.querySelector('[data-action="detail"]').addEventListener("click", () => {
            elements.documentLookupId.value = documentItem.id;
            renderDocuments([documentItem]);
        });
        node.querySelector('[data-action="delete"]').addEventListener("click", () => deleteDocument(documentItem));
        elements.documentList.appendChild(node);
    });
}

/**
 * 删除知识库文档。
 */
async function deleteDocument(documentItem) {
    const confirmed = window.confirm(`确定删除知识库文档「${documentItem.filename}」吗？`);
    if (!confirmed) {
        return;
    }
    const deleted = await api(`/api/v1/knowledge/documents/${documentItem.id}`, {
        method: "DELETE"
    });
    showToast(`文档已标记为 ${deleted.status}`);
    await loadDocuments();
}

/**
 * 加载员工列表，支持关键词查询。
 */
async function loadEmployees() {
    const keyword = elements.employeeKeyword.value.trim();
    const path = keyword ? `/api/v1/employees?keyword=${encodeURIComponent(keyword)}` : "/api/v1/employees";
    const employees = await api(path);
    renderEmployees(employees, keyword ? `未找到匹配「${keyword}」的员工` : "未找到员工");
}

/**
 * 按员工 ID 查询员工详情。
 */
async function lookupEmployeeById() {
    const id = elements.employeeLookupId.value.trim();
    if (!id) {
        showToast("请输入员工 ID");
        return;
    }
    const employee = await api(`/api/v1/employees/${encodeURIComponent(id)}`);
    renderEmployees([employee], `未找到 ID 为 ${id} 的员工`);
    fillEmployeeForm(employee);
}

/**
 * 渲染员工列表。
 */
function renderEmployees(employees, emptyText) {
    elements.employeeList.innerHTML = "";
    if (!employees.length) {
        setEmpty(elements.employeeList, emptyText);
        return;
    }
    employees.forEach(employee => {
        const node = document.createElement("div");
        node.className = "employee-item";
        node.innerHTML = `
            <div class="item-title">${employee.name} · ${employee.department}</div>
            <div class="meta">${employee.email}</div>
            <div class="meta">年假余额 ${employee.annualLeaveBalance} 天，已用 ${employee.annualLeaveUsed} / 总计 ${employee.annualLeaveTotal}</div>
            <div class="employee-actions">
                <button type="button" data-action="edit">编辑</button>
                <button class="danger-button" type="button" data-action="delete">删除</button>
            </div>
        `;
        node.querySelector('[data-action="edit"]').addEventListener("click", () => fillEmployeeForm(employee));
        node.querySelector('[data-action="delete"]').addEventListener("click", () => deleteEmployee(employee));
        elements.employeeList.appendChild(node);
    });
}

/**
 * 将员工信息填充到编辑表单。
 */
function fillEmployeeForm(employee) {
    state.employeeEditingId = employee.id;
    elements.employeeId.value = employee.id;
    elements.employeeName.value = employee.name || "";
    elements.employeeDepartment.value = employee.department || "";
    elements.employeeEmail.value = employee.email || "";
    elements.employeeAnnualTotal.value = employee.annualLeaveTotal ?? 0;
    elements.employeeAnnualUsed.value = employee.annualLeaveUsed ?? 0;
    elements.saveEmployeeButton.textContent = `保存 #${employee.id}`;
}

/**
 * 重置员工表单为新增状态。
 */
function resetEmployeeForm() {
    state.employeeEditingId = null;
    elements.employeeForm.reset();
    elements.employeeId.value = "";
    elements.employeeAnnualTotal.value = 10;
    elements.employeeAnnualUsed.value = 0;
    elements.saveEmployeeButton.textContent = "新增员工";
}

/**
 * 从员工表单读取请求数据。
 */
function readEmployeeForm() {
    return {
        name: elements.employeeName.value.trim(),
        department: elements.employeeDepartment.value.trim(),
        email: elements.employeeEmail.value.trim(),
        annualLeaveTotal: Number(elements.employeeAnnualTotal.value),
        annualLeaveUsed: Number(elements.employeeAnnualUsed.value)
    };
}

/**
 * 新增或更新员工。
 */
async function saveEmployee(event) {
    event.preventDefault();
    const body = readEmployeeForm();
    const id = state.employeeEditingId;
    elements.saveEmployeeButton.disabled = true;
    showToast(id ? "正在更新员工..." : "正在新增员工...");
    try {
        await api(id ? `/api/v1/employees/${id}` : "/api/v1/employees", {
            method: id ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
        });
        showToast(id ? "员工信息已更新" : "员工已新增");
        resetEmployeeForm();
        await loadEmployees();
    } finally {
        elements.saveEmployeeButton.disabled = false;
    }
}

/**
 * 删除员工。
 */
async function deleteEmployee(employee) {
    const confirmed = window.confirm(`确定删除员工「${employee.name}」吗？`);
    if (!confirmed) {
        return;
    }
    await api(`/api/v1/employees/${employee.id}`, {
        method: "DELETE"
    });
    showToast("员工已删除");
    if (state.employeeEditingId === employee.id) {
        resetEmployeeForm();
    }
    await loadEmployees();
}

/**
 * 提交请假申请。
 */
async function saveLeaveRecord(event) {
    event.preventDefault();
    const empId = elements.leaveEmployeeId.value.trim();
    const startDate = elements.leaveStartDate.value;
    const endDate = elements.leaveEndDate.value;
    const body = {
        empId: empId ? Number(empId) : null,
        startDate,
        endDate,
        status: "PENDING"
    };

    elements.saveLeaveRecordButton.disabled = true;
    showToast("正在新增请假记录...");
    try {
        await api("/api/v1/leave-records", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
        });
        showToast("请假申请已提交");
        await Promise.all([
            loadLeaveRecords(),
            loadEmployees()
        ]);
    } finally {
        elements.saveLeaveRecordButton.disabled = false;
    }
}

/**
 * 按员工 ID 加载请假记录。
 */
async function loadLeaveRecords() {
    const empId = elements.leaveEmployeeId.value.trim();
    if (!empId) {
        showToast("请输入员工 ID 查询请假记录");
        setEmpty(elements.leaveRecordList, "请输入员工 ID");
        return;
    }
    const records = await api(`/api/v1/leave-records/employee/${encodeURIComponent(empId)}`);
    renderLeaveRecords(records, `员工 #${empId} 暂无请假记录`);
}

/**
 * 加载所有待审批请假记录。
 */
async function loadPendingLeaveRecords() {
    const records = await api("/api/v1/leave-records?status=PENDING");
    renderLeaveRecords(records, "暂无待审批请假记录");
}

/**
 * 渲染请假记录列表，并在管理员身份下展示审批按钮。
 */
function renderLeaveRecords(records, emptyText) {
    elements.leaveRecordList.innerHTML = "";
    if (!records.length) {
        setEmpty(elements.leaveRecordList, emptyText);
        return;
    }
    const isAdmin = elements.roleHeader.value.trim().toUpperCase() === "ADMIN";
    records.forEach(record => {
        const node = document.createElement("div");
        const status = String(record.status || "").toLowerCase();
        node.className = "audit-item";
        node.innerHTML = `
            <div class="item-title">请假 #${record.id} · 员工 #${record.empId}</div>
            <div class="meta">${record.startDate} 至 ${record.endDate}</div>
            <div class="meta"><span class="status ${status}">${record.status}</span></div>
            ${isAdmin && record.status === "PENDING" ? `
                <div class="employee-actions">
                    <button type="button" data-action="approve">批准</button>
                    <button class="danger-button" type="button" data-action="reject">驳回</button>
                </div>
            ` : ""}
        `;
        const approveButton = node.querySelector('[data-action="approve"]');
        const rejectButton = node.querySelector('[data-action="reject"]');
        approveButton?.addEventListener("click", () => reviewLeaveRecord(record, "APPROVED"));
        rejectButton?.addEventListener("click", () => reviewLeaveRecord(record, "REJECTED"));
        elements.leaveRecordList.appendChild(node);
    });
}

/**
 * 审批请假记录。
 */
async function reviewLeaveRecord(record, status) {
    await api(`/api/v1/leave-records/${record.id}/status`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status })
    });
    showToast(status === "APPROVED" ? "请假申请已批准" : "请假申请已驳回");
    await Promise.all([
        loadPendingLeaveRecords(),
        loadEmployees()
    ]);
}

/**
 * 执行模型和向量配置诊断。
 */
async function runDiagnostics() {
    elements.diagnosticsResult.textContent = "检测中...";
    const [chat, embedding] = await Promise.all([
        api("/api/v1/diagnostics/chat").catch(error => ({ provider: "chat", status: "DOWN", message: error.message })),
        api("/api/v1/diagnostics/embedding").catch(error => ({ provider: "embedding", status: "DOWN", message: error.message }))
    ]);
    elements.diagnosticsResult.innerHTML = [chat, embedding].map(item => {
        const status = String(item.status || "UNKNOWN").toLowerCase();
        return `
            <div>
                <span class="status ${status}">${item.status}</span>
                <span>${item.provider || "provider"}：${item.message || "-"}</span>
            </div>
        `;
    }).join("");
}

/**
 * 绑定页面上的按钮、表单和快捷键事件。
 */
function bindEvents() {
    elements.newSessionButton.addEventListener("click", () => createSession().catch(error => showToast(error.message)));
    elements.loadMessagesButton.addEventListener("click", () => loadMessages().catch(error => showToast(error.message)));
    elements.chatForm.addEventListener("submit", sendMessage);
    elements.uploadForm.addEventListener("submit", event => uploadDocument(event).catch(error => showToast(error.message)));
    elements.refreshDocumentsButton.addEventListener("click", () => loadDocuments().catch(error => showToast(error.message)));
    elements.lookupDocumentButton.addEventListener("click", () => lookupDocumentById().catch(error => showToast(error.message)));
    elements.employeeForm.addEventListener("submit", event => saveEmployee(event).catch(error => showToast(error.message)));
    elements.resetEmployeeFormButton.addEventListener("click", resetEmployeeForm);
    elements.refreshEmployeesButton.addEventListener("click", () => loadEmployees().catch(error => showToast(error.message)));
    elements.searchEmployeesButton.addEventListener("click", () => loadEmployees().catch(error => showToast(error.message)));
    elements.lookupEmployeeButton.addEventListener("click", () => lookupEmployeeById().catch(error => showToast(error.message)));
    elements.leaveRecordForm.addEventListener("submit", event => saveLeaveRecord(event).catch(error => showToast(error.message)));
    elements.refreshPendingLeaveRecordsButton.addEventListener("click", () => loadPendingLeaveRecords().catch(error => showToast(error.message)));
    elements.refreshLeaveRecordsButton.addEventListener("click", () => loadLeaveRecords().catch(error => showToast(error.message)));
    elements.refreshRagLogsButton.addEventListener("click", () => loadRagLogs().catch(error => showToast(error.message)));
    elements.refreshToolLogsButton.addEventListener("click", () => loadToolLogs().catch(error => showToast(error.message)));
    elements.filterToolLogsButton.addEventListener("click", () => loadToolLogs().catch(error => showToast(error.message)));
    elements.employeeKeyword.addEventListener("keydown", event => {
        if (event.key === "Enter") {
            event.preventDefault();
            loadEmployees().catch(error => showToast(error.message));
        }
    });
    elements.employeeLookupId.addEventListener("keydown", event => {
        if (event.key === "Enter") {
            event.preventDefault();
            lookupEmployeeById().catch(error => showToast(error.message));
        }
    });
    elements.documentLookupId.addEventListener("keydown", event => {
        if (event.key === "Enter") {
            event.preventDefault();
            lookupDocumentById().catch(error => showToast(error.message));
        }
    });
    elements.leaveEmployeeId.addEventListener("keydown", event => {
        if (event.key === "Enter") {
            event.preventDefault();
            loadLeaveRecords().catch(error => showToast(error.message));
        }
    });
    elements.toolNameFilter.addEventListener("keydown", event => {
        if (event.key === "Enter") {
            event.preventDefault();
            loadToolLogs().catch(error => showToast(error.message));
        }
    });
    elements.diagnosticsButton.addEventListener("click", () => runDiagnostics().catch(error => showToast(error.message)));
}

/**
 * 页面初始化入口。
 */
async function bootstrap() {
    bindEvents();
    appendMessage("system", "可以询问公司制度、年假余额或同事联系方式。");
    await Promise.all([
        loadSessions().catch(error => showToast(error.message)),
        loadDocuments().catch(error => showToast(error.message)),
        loadEmployees().catch(error => showToast(error.message)),
        loadRagLogs().catch(error => showToast(error.message)),
        loadToolLogs().catch(error => showToast(error.message))
    ]);
}

bootstrap();
