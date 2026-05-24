const state = {
    sessionId: null,
    streaming: false
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
    employeeKeyword: document.querySelector("#employeeKeyword"),
    employeeList: document.querySelector("#employeeList"),
    refreshEmployeesButton: document.querySelector("#refreshEmployeesButton"),
    diagnosticsButton: document.querySelector("#diagnosticsButton"),
    diagnosticsResult: document.querySelector("#diagnosticsResult"),
    toast: document.querySelector("#toast")
};

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

async function api(path, options = {}) {
    const headers = {
        ...authHeaders(),
        ...(options.headers || {})
    };
    const response = await fetch(path, {
        ...options,
        headers
    });

    if (!response.ok) {
        let message = `${response.status} ${response.statusText}`;
        try {
            const error = await response.json();
            message = error.message || message;
        } catch {
            // Keep the HTTP status text when the response has no JSON body.
        }
        throw new Error(message);
    }

    const contentType = response.headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
        return response.json();
    }
    return response.text();
}

function showToast(message) {
    elements.toast.textContent = message;
    elements.toast.hidden = false;
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => {
        elements.toast.hidden = true;
    }, 3200);
}

function formatDate(value) {
    if (!value) {
        return "-";
    }
    return String(value).replace("T", " ").slice(0, 19);
}

function setEmpty(container, text) {
    container.innerHTML = `<div class="empty">${text}</div>`;
}

function appendMessage(role, content) {
    const node = document.createElement("div");
    node.className = `message ${role}`;
    node.textContent = content;
    elements.messages.appendChild(node);
    elements.messages.scrollTop = elements.messages.scrollHeight;
    return node;
}

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
            throw new Error(`${response.status} ${response.statusText}`);
        }

        await readEventStream(response, data => {
            if (data === "[DONE]") {
                return;
            }
            assistantNode.textContent += data;
            elements.messages.scrollTop = elements.messages.scrollHeight;
        });
        await loadSessions();
    } catch (error) {
        assistantNode.textContent = `对话失败：${error.message}`;
    } finally {
        state.streaming = false;
        elements.sendButton.disabled = false;
        elements.sendButton.textContent = "发送";
    }
}

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

async function loadDocuments() {
    const documents = await api("/api/v1/knowledge/documents");
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
            <div class="meta">${documentItem.chunkCount || 0} chunks · ${formatDate(documentItem.createdAt)}</div>
            <div class="meta"><span class="status ${status}">${documentItem.status}</span></div>
        `;
        elements.documentList.appendChild(node);
    });
}

async function loadEmployees() {
    const keyword = elements.employeeKeyword.value.trim();
    const path = keyword ? `/api/v1/employees?keyword=${encodeURIComponent(keyword)}` : "/api/v1/employees";
    const employees = await api(path);
    elements.employeeList.innerHTML = "";
    if (!employees.length) {
        setEmpty(elements.employeeList, "未找到员工");
        return;
    }
    employees.forEach(employee => {
        const node = document.createElement("div");
        node.className = "employee-item";
        node.innerHTML = `
            <div class="item-title">${employee.name} · ${employee.department}</div>
            <div class="meta">${employee.email}</div>
            <div class="meta">年假余额 ${employee.annualLeaveBalance} 天，已用 ${employee.annualLeaveUsed} / 总计 ${employee.annualLeaveTotal}</div>
        `;
        elements.employeeList.appendChild(node);
    });
}

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

function bindEvents() {
    elements.newSessionButton.addEventListener("click", () => createSession().catch(error => showToast(error.message)));
    elements.loadMessagesButton.addEventListener("click", () => loadMessages().catch(error => showToast(error.message)));
    elements.chatForm.addEventListener("submit", sendMessage);
    elements.uploadForm.addEventListener("submit", event => uploadDocument(event).catch(error => showToast(error.message)));
    elements.refreshDocumentsButton.addEventListener("click", () => loadDocuments().catch(error => showToast(error.message)));
    elements.refreshEmployeesButton.addEventListener("click", () => loadEmployees().catch(error => showToast(error.message)));
    elements.employeeKeyword.addEventListener("keydown", event => {
        if (event.key === "Enter") {
            loadEmployees().catch(error => showToast(error.message));
        }
    });
    elements.diagnosticsButton.addEventListener("click", () => runDiagnostics().catch(error => showToast(error.message)));
}

async function bootstrap() {
    bindEvents();
    appendMessage("system", "可以询问公司制度、年假余额或同事联系方式。");
    await Promise.all([
        loadSessions().catch(error => showToast(error.message)),
        loadDocuments().catch(error => showToast(error.message)),
        loadEmployees().catch(error => showToast(error.message))
    ]);
}

bootstrap();
