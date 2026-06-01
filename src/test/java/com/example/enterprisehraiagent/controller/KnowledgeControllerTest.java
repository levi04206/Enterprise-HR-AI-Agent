package com.example.enterprisehraiagent.controller;

import com.example.enterprisehraiagent.entity.KnowledgeDocument;
import com.example.enterprisehraiagent.dto.IngestResponse;
import com.example.enterprisehraiagent.mapper.KnowledgeDocumentMapper;
import com.example.enterprisehraiagent.service.KnowledgeIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KnowledgeControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private KnowledgeDocumentMapper knowledgeDocumentMapper;

    @MockitoBean
    private KnowledgeIngestionService knowledgeIngestionService;

    @Test
    void ingestShouldAcceptMultipartDocument() {
        when(knowledgeIngestionService.ingest(any(FilePart.class)))
                .thenReturn(Mono.just(new IngestResponse(7L, "员工手册.txt", 1, 3, "知识库文档已入库")));

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", "员工手册内容")
                .filename("员工手册.txt")
                .contentType(MediaType.TEXT_PLAIN);

        webTestClient.post()
                .uri("/api/v1/knowledge/ingest")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.documentId").isEqualTo(7)
                .jsonPath("$.filename").isEqualTo("员工手册.txt")
                .jsonPath("$.rawDocumentCount").isEqualTo(1)
                .jsonPath("$.chunkCount").isEqualTo(3)
                .jsonPath("$.message").isEqualTo("知识库文档已入库");
    }

    @Test
    void listDocumentsShouldReturnEmptyWhenNoDocumentIndexed() {
        webTestClient.get()
                .uri("/api/v1/knowledge/documents")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("[]");
    }

    @Test
    void getDocumentShouldReturnBadRequestWhenNotFound() {
        webTestClient.get()
                .uri("/api/v1/knowledge/documents/{id}", 999)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("知识库文档不存在：999");
    }

    @Test
    void deleteDocumentShouldMarkDocumentDeleted() {
        KnowledgeDocument document = new KnowledgeDocument();
        document.setFilename("员工手册.txt");
        document.setContentType("text/plain");
        document.setRawDocumentCount(1);
        document.setChunkCount(0);
        document.setStatus("INDEXED");
        document.setCreatedAt(LocalDateTime.now());
        knowledgeDocumentMapper.insert(document);

        webTestClient.delete()
                .uri("/api/v1/knowledge/documents/{id}", document.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(document.getId().intValue())
                .jsonPath("$.status").isEqualTo("DELETED");
    }
}
