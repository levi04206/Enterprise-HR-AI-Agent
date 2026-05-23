package com.example.enterprisehraiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.enterprisehraiagent.dto.KnowledgeDocumentResponse;
import com.example.enterprisehraiagent.entity.KnowledgeDocument;
import com.example.enterprisehraiagent.mapper.KnowledgeDocumentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeDocumentService {

    private final KnowledgeDocumentMapper knowledgeDocumentMapper;

    public KnowledgeDocumentService(KnowledgeDocumentMapper knowledgeDocumentMapper) {
        this.knowledgeDocumentMapper = knowledgeDocumentMapper;
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
}
