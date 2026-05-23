package com.example.enterprisehraiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.enterprisehraiagent.dto.KnowledgeDocumentResponse;
import com.example.enterprisehraiagent.entity.KnowledgeChunk;
import com.example.enterprisehraiagent.entity.KnowledgeDocument;
import com.example.enterprisehraiagent.mapper.KnowledgeChunkMapper;
import com.example.enterprisehraiagent.mapper.KnowledgeDocumentMapper;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeDocumentService {

    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final KnowledgeChunkMapper knowledgeChunkMapper;
    private final VectorStore vectorStore;

    public KnowledgeDocumentService(KnowledgeDocumentMapper knowledgeDocumentMapper,
                                    KnowledgeChunkMapper knowledgeChunkMapper,
                                    VectorStore vectorStore) {
        this.knowledgeDocumentMapper = knowledgeDocumentMapper;
        this.knowledgeChunkMapper = knowledgeChunkMapper;
        this.vectorStore = vectorStore;
    }

    public List<KnowledgeDocumentResponse> list() {
        return knowledgeDocumentMapper.selectList(
                        new LambdaQueryWrapper<KnowledgeDocument>()
                                .orderByDesc(KnowledgeDocument::getCreatedAt)
                                .orderByDesc(KnowledgeDocument::getId)
                )
                .stream()
                .map(KnowledgeDocumentResponse::from)
                .toList();
    }

    public KnowledgeDocumentResponse getById(Long id) {
        KnowledgeDocument document = knowledgeDocumentMapper.selectById(id);
        if (document == null) {
            throw new IllegalArgumentException("知识库文档不存在：" + id);
        }
        return KnowledgeDocumentResponse.from(document);
    }

    public KnowledgeDocumentResponse deleteById(Long id) {
        KnowledgeDocument document = knowledgeDocumentMapper.selectById(id);
        if (document == null) {
            throw new IllegalArgumentException("知识库文档不存在：" + id);
        }
        if ("DELETED".equals(document.getStatus())) {
            return KnowledgeDocumentResponse.from(document);
        }

        List<KnowledgeChunk> chunks = knowledgeChunkMapper.selectList(
                new LambdaQueryWrapper<KnowledgeChunk>()
                        .eq(KnowledgeChunk::getDocumentId, id)
        );
        List<String> vectorIds = chunks.stream()
                .map(KnowledgeChunk::getVectorId)
                .toList();

        if (!vectorIds.isEmpty()) {
            vectorStore.delete(vectorIds);
            knowledgeChunkMapper.delete(
                    new LambdaQueryWrapper<KnowledgeChunk>()
                            .eq(KnowledgeChunk::getDocumentId, id)
            );
        }

        document.setStatus("DELETED");
        knowledgeDocumentMapper.updateById(document);
        return KnowledgeDocumentResponse.from(document);
    }
}
