package com.example.enterprisehraiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.enterprisehraiagent.entity.KnowledgeChunk;
import com.example.enterprisehraiagent.entity.KnowledgeDocument;
import com.example.enterprisehraiagent.mapper.KnowledgeChunkMapper;
import com.example.enterprisehraiagent.mapper.KnowledgeDocumentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库向量索引恢复服务。
 *
 * <p>默认 in-memory 向量库不会持久化，应用重启后 VectorStore 会变空。
 * 这个服务在启动完成后，使用数据库中持久化的分块文本重建向量索引，
 * 避免出现“文档记录还在，但 RAG 检索不到内容”的情况。</p>
 */
@Service
public class KnowledgeVectorStoreRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeVectorStoreRecoveryService.class);

    private final VectorStore vectorStore;
    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final KnowledgeChunkMapper knowledgeChunkMapper;

    /**
     * 注入向量库和知识库数据访问对象。
     */
    public KnowledgeVectorStoreRecoveryService(VectorStore vectorStore,
                                               KnowledgeDocumentMapper knowledgeDocumentMapper,
                                               KnowledgeChunkMapper knowledgeChunkMapper) {
        this.vectorStore = vectorStore;
        this.knowledgeDocumentMapper = knowledgeDocumentMapper;
        this.knowledgeChunkMapper = knowledgeChunkMapper;
    }

    /**
     * 应用启动完成后重建已索引文档的向量数据。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void rebuildIndexedDocuments() {
        List<KnowledgeDocument> indexedDocuments = knowledgeDocumentMapper.selectList(
                new LambdaQueryWrapper<KnowledgeDocument>()
                        .eq(KnowledgeDocument::getStatus, "INDEXED")
        );

        int rebuiltChunkCount = 0;
        for (KnowledgeDocument knowledgeDocument : indexedDocuments) {
            List<Document> chunks = loadRecoverableChunks(knowledgeDocument);
            if (chunks.isEmpty()) {
                continue;
            }
            vectorStore.add(chunks);
            rebuiltChunkCount += chunks.size();
        }

        if (rebuiltChunkCount > 0) {
            log.info("Rebuilt knowledge vector store from persisted chunks. documents={}, chunks={}",
                    indexedDocuments.size(), rebuiltChunkCount);
        }
    }

    /**
     * 从数据库读取可恢复的分块，并还原成 Spring AI Document。
     */
    private List<Document> loadRecoverableChunks(KnowledgeDocument knowledgeDocument) {
        return knowledgeChunkMapper.selectList(
                        new LambdaQueryWrapper<KnowledgeChunk>()
                                .eq(KnowledgeChunk::getDocumentId, knowledgeDocument.getId())
                                .isNotNull(KnowledgeChunk::getContent)
                                .orderByAsc(KnowledgeChunk::getChunkIndex)
                )
                .stream()
                .filter(chunk -> StringUtils.hasText(chunk.getContent()))
                .map(chunk -> toVectorDocument(knowledgeDocument, chunk))
                .toList();
    }

    /**
     * 将数据库分块重新包装为向量库文档。
     */
    private Document toVectorDocument(KnowledgeDocument knowledgeDocument, KnowledgeChunk chunk) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("knowledgeDocumentId", knowledgeDocument.getId());
        metadata.put("filename", knowledgeDocument.getFilename());
        metadata.put("chunkIndex", chunk.getChunkIndex());
        return new Document(chunk.getVectorId(), chunk.getContent(), metadata);
    }
}
