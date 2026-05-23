package com.example.enterprisehraiagent.service;

import com.example.enterprisehraiagent.dto.IngestResponse;
import com.example.enterprisehraiagent.entity.KnowledgeChunk;
import com.example.enterprisehraiagent.entity.KnowledgeDocument;
import com.example.enterprisehraiagent.mapper.KnowledgeChunkMapper;
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
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

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
    private final KnowledgeChunkMapper knowledgeChunkMapper;
    private final TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();

    public KnowledgeIngestionService(VectorStore vectorStore,
                                     KnowledgeDocumentMapper knowledgeDocumentMapper,
                                     KnowledgeChunkMapper knowledgeChunkMapper) {
        this.vectorStore = vectorStore;
        this.knowledgeDocumentMapper = knowledgeDocumentMapper;
        this.knowledgeChunkMapper = knowledgeChunkMapper;
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
        List<Document> splitChunks = tokenTextSplitter.apply(rawDocuments);

        KnowledgeDocument knowledgeDocument = new KnowledgeDocument();
        knowledgeDocument.setFilename(filePart.filename());
        knowledgeDocument.setContentType(filePart.headers().getContentType() == null
                ? "application/octet-stream"
                : filePart.headers().getContentType().toString());
        knowledgeDocument.setRawDocumentCount(rawDocuments.size());
        knowledgeDocument.setChunkCount(splitChunks.size());
        knowledgeDocument.setStatus("INDEXING");
        knowledgeDocument.setCreatedAt(LocalDateTime.now());
        knowledgeDocumentMapper.insert(knowledgeDocument);

        try {
            List<Document> chunks = attachBusinessMetadata(splitChunks, knowledgeDocument);
            vectorStore.add(chunks);
            saveChunkIndex(knowledgeDocument.getId(), chunks);

            knowledgeDocument.setStatus("INDEXED");
            knowledgeDocumentMapper.updateById(knowledgeDocument);
        } catch (Exception ex) {
            knowledgeDocument.setStatus("FAILED");
            knowledgeDocumentMapper.updateById(knowledgeDocument);
            throw ex;
        }

        return new IngestResponse(
                knowledgeDocument.getId(),
                filePart.filename(),
                rawDocuments.size(),
                splitChunks.size(),
                "知识库文档已入库"
        );
    }

    private List<Document> attachBusinessMetadata(List<Document> chunks, KnowledgeDocument knowledgeDocument) {
        return IntStream.range(0, chunks.size())
                .mapToObj(index -> {
                    Document chunk = chunks.get(index);
                    Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
                    metadata.put("knowledgeDocumentId", knowledgeDocument.getId());
                    metadata.put("filename", knowledgeDocument.getFilename());
                    metadata.put("chunkIndex", index);
                    return new Document(chunk.getId(), chunk.getText(), metadata);
                })
                .toList();
    }

    private void saveChunkIndex(Long documentId, List<Document> chunks) {
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < chunks.size(); i++) {
            KnowledgeChunk knowledgeChunk = new KnowledgeChunk();
            knowledgeChunk.setDocumentId(documentId);
            knowledgeChunk.setVectorId(chunks.get(i).getId());
            knowledgeChunk.setChunkIndex(i);
            knowledgeChunk.setCreatedAt(now);
            knowledgeChunkMapper.insert(knowledgeChunk);
        }
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
