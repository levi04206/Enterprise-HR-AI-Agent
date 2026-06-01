package com.example.enterprisehraiagent.controller;

import com.example.enterprisehraiagent.dto.IngestResponse;
import com.example.enterprisehraiagent.dto.KnowledgeDocumentResponse;
import com.example.enterprisehraiagent.service.KnowledgeDocumentService;
import com.example.enterprisehraiagent.service.KnowledgeIngestionService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

    private final KnowledgeIngestionService knowledgeIngestionService;
    private final KnowledgeDocumentService knowledgeDocumentService;

    /**
     * 注入知识库入库服务和文档管理服务。
     */
    public KnowledgeController(KnowledgeIngestionService knowledgeIngestionService,
                               KnowledgeDocumentService knowledgeDocumentService) {
        this.knowledgeIngestionService = knowledgeIngestionService;
        this.knowledgeDocumentService = knowledgeDocumentService;
    }

    /**
     * 上传企业制度文档并写入向量库。
     *
     * <p>示例：
     * curl -F "file=@2026员工考勤管理办法.txt" http://localhost:8080/api/v1/knowledge/ingest</p>
     */
    @PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)//只接收“文件表单”格式的数据，不纯文本 JSON。
    public Mono<IngestResponse> ingest(@RequestPart("file") FilePart file) {
        return knowledgeIngestionService.ingest(file);
    }

    /**
     * 查询知识库文档列表。
     */
    @GetMapping("/documents")
    public List<KnowledgeDocumentResponse> listDocuments() {
        return knowledgeDocumentService.list();
    }

    /**
     * 根据文档 ID 查询知识库文档详情。
     */
    @GetMapping("/documents/{id}")
    public KnowledgeDocumentResponse getDocument(@PathVariable Long id) {
        return knowledgeDocumentService.getById(id);
    }

    /**
     * 根据文档 ID 删除知识库文档和对应向量索引。
     */
    @DeleteMapping("/documents/{id}")
    public KnowledgeDocumentResponse deleteDocument(@PathVariable Long id) {
        return knowledgeDocumentService.deleteById(id);
    }
}
