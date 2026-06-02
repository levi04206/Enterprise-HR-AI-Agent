package com.example.enterprisehraiagent.service;

import com.example.enterprisehraiagent.entity.KnowledgeChunk;
import com.example.enterprisehraiagent.entity.KnowledgeDocument;
import com.example.enterprisehraiagent.mapper.KnowledgeChunkMapper;
import com.example.enterprisehraiagent.mapper.KnowledgeDocumentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeVectorStoreRecoveryServiceTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private KnowledgeDocumentMapper knowledgeDocumentMapper;

    @Mock
    private KnowledgeChunkMapper knowledgeChunkMapper;

    @InjectMocks
    private KnowledgeVectorStoreRecoveryService recoveryService;

    @Test
    void rebuildIndexedDocumentsShouldRestorePersistedChunksToVectorStore() {
        KnowledgeDocument document = indexedDocument();
        KnowledgeChunk chunk = chunk("vector-1", 0, "员工累计迟到超过三次，将进入主管提醒流程。");

        when(knowledgeDocumentMapper.selectList(any())).thenReturn(List.of(document));
        when(knowledgeChunkMapper.selectList(any())).thenReturn(List.of(chunk));

        recoveryService.rebuildIndexedDocuments();

        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(captor.capture());
        List<Document> restoredDocuments = captor.getValue();

        assertThat(restoredDocuments).hasSize(1);
        assertThat(restoredDocuments.get(0).getId()).isEqualTo("vector-1");
        assertThat(restoredDocuments.get(0).getText()).contains("累计迟到");
        assertThat(restoredDocuments.get(0).getMetadata())
                .containsEntry("knowledgeDocumentId", 1L)
                .containsEntry("filename", "员工考勤制度.pdf")
                .containsEntry("chunkIndex", 0);
    }

    @Test
    void rebuildIndexedDocumentsShouldSkipChunksWithoutPersistedContent() {
        KnowledgeDocument document = indexedDocument();
        KnowledgeChunk chunk = chunk("vector-1", 0, " ");

        when(knowledgeDocumentMapper.selectList(any())).thenReturn(List.of(document));
        when(knowledgeChunkMapper.selectList(any())).thenReturn(List.of(chunk));

        recoveryService.rebuildIndexedDocuments();

        verify(vectorStore, never()).add(any());
    }

    private static KnowledgeDocument indexedDocument() {
        KnowledgeDocument document = new KnowledgeDocument();
        document.setId(1L);
        document.setFilename("员工考勤制度.pdf");
        document.setContentType("application/pdf");
        document.setRawDocumentCount(1);
        document.setChunkCount(1);
        document.setStatus("INDEXED");
        document.setCreatedAt(LocalDateTime.now());
        return document;
    }

    private static KnowledgeChunk chunk(String vectorId, int index, String content) {
        KnowledgeChunk chunk = new KnowledgeChunk();
        chunk.setId(10L);
        chunk.setDocumentId(1L);
        chunk.setVectorId(vectorId);
        chunk.setChunkIndex(index);
        chunk.setContent(content);
        chunk.setCreatedAt(LocalDateTime.now());
        return chunk;
    }
}
