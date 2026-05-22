package com.example.enterprisehraiagent.controller;

import com.example.enterprisehraiagent.dto.IngestResponse;
import com.example.enterprisehraiagent.service.KnowledgeIngestionService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

    private final KnowledgeIngestionService knowledgeIngestionService;

    public KnowledgeController(KnowledgeIngestionService knowledgeIngestionService) {
        this.knowledgeIngestionService = knowledgeIngestionService;
    }

    /**
     * 上传企业制度文档并写入向量库。
     *
     * <p>示例：
     * curl -F "file=@2026员工考勤管理办法.txt" http://localhost:8080/api/v1/knowledge/ingest</p>
     */
    @PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<IngestResponse> ingest(@RequestPart("file") FilePart file) {
        return knowledgeIngestionService.ingest(file);
    }
}
