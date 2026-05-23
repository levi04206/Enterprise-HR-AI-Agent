package com.example.enterprisehraiagent.service;

import com.example.enterprisehraiagent.dto.IngestResponse;
import com.example.enterprisehraiagent.entity.KnowledgeDocument;
import com.example.enterprisehraiagent.mapper.KnowledgeDocumentMapper;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 企业知识库构建服务。
 *
 * <p>处理链路是典型 RAG 入库流程：
 * 上传文件 -> 文档读取 -> Token 级分块 -> Embedding -> VectorStore 入库。</p>
 */
@Service
public class KnowledgeIngestionService {

    private final VectorStore vectorStore;
    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();

    public KnowledgeIngestionService(VectorStore vectorStore, KnowledgeDocumentMapper knowledgeDocumentMapper) {
        this.vectorStore = vectorStore;
        this.knowledgeDocumentMapper = knowledgeDocumentMapper;
    }

    public Mono<IngestResponse> ingest(FilePart filePart) {
        return Mono.usingWhen(
                createTempFile(filePart),
                tempFile -> filePart.transferTo(tempFile)
                        .then(Mono.fromCallable(() -> ingestBlocking(filePart, tempFile))
                                .subscribeOn(Schedulers.boundedElastic())),
                tempFile -> Mono.fromRunnable(() -> deleteQuietly(tempFile))
        );
    }

    private Mono<Path> createTempFile(FilePart filePart) {
        return Mono.fromCallable(() -> {
            String suffix = getSuffix(filePart.filename());
            return Files.createTempFile("hr-knowledge-", suffix);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private IngestResponse ingestBlocking(FilePart filePart, Path tempFile) {
        TikaDocumentReader reader = new TikaDocumentReader(new FileSystemResource(tempFile));
        List<Document> rawDocuments = reader.get();
        List<Document> chunks = tokenTextSplitter.apply(rawDocuments);

        vectorStore.add(chunks);

        KnowledgeDocument knowledgeDocument = new KnowledgeDocument();
        knowledgeDocument.setFilename(filePart.filename());
        knowledgeDocument.setContentType(filePart.headers().getContentType() == null
                ? "application/octet-stream"
                : filePart.headers().getContentType().toString());
        knowledgeDocument.setRawDocumentCount(rawDocuments.size());
        knowledgeDocument.setChunkCount(chunks.size());
        knowledgeDocument.setStatus("INDEXED");
        knowledgeDocument.setCreatedAt(LocalDateTime.now());
        knowledgeDocumentMapper.insert(knowledgeDocument);

        return new IngestResponse(
                knowledgeDocument.getId(),
                filePart.filename(),
                rawDocuments.size(),
                chunks.size(),
                "知识库文档已入库"
        );
    }

    private static String getSuffix(String filename) {
        int dotIndex = filename == null ? -1 : filename.lastIndexOf('.');
        return dotIndex >= 0 ? filename.substring(dotIndex) : ".tmp";
    }

    private static void deleteQuietly(Path tempFile) {
        try {
            Files.deleteIfExists(tempFile);
        } catch (Exception ignored) {
            // 临时文件删除失败不影响业务响应，生产环境可接入日志系统记录。
        }
    }
}
