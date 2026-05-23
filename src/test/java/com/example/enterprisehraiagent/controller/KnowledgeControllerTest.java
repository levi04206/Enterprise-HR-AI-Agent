package com.example.enterprisehraiagent.controller;

import com.example.enterprisehraiagent.entity.KnowledgeDocument;
import com.example.enterprisehraiagent.mapper.KnowledgeDocumentMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KnowledgeControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private KnowledgeDocumentMapper knowledgeDocumentMapper;

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
