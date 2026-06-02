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

    /**
     * 注入向量库和知识库表的数据访问对象。
     */
    public KnowledgeIngestionService(VectorStore vectorStore,
                                     KnowledgeDocumentMapper knowledgeDocumentMapper,
                                     KnowledgeChunkMapper knowledgeChunkMapper) {
        this.vectorStore = vectorStore;
        this.knowledgeDocumentMapper = knowledgeDocumentMapper;
        this.knowledgeChunkMapper = knowledgeChunkMapper;
    }

    /**
     * 接收上传文件并异步完成知识库入库。
     */
    public Mono<IngestResponse> ingest(FilePart filePart) {
        return Mono.usingWhen(
                createTempFile(filePart),
                tempFile -> filePart.transferTo(tempFile)
                        .then(Mono.fromCallable(() -> ingestBlocking(filePart, tempFile))
                                .subscribeOn(Schedulers.boundedElastic())),
                tempFile -> Mono.fromRunnable(() -> deleteQuietly(tempFile))
        );
    }

    /**
     * 为上传文件创建临时落盘文件。
     */
    private Mono<Path> createTempFile(FilePart filePart) {
        return Mono.fromCallable(() -> {
            String suffix = getSuffix(filePart.filename());
            return Files.createTempFile("hr-knowledge-", suffix);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 阻塞执行文档解析、分块、向量化和数据库记录写入。
     */
    private IngestResponse ingestBlocking(FilePart filePart, Path tempFile) {
        TikaDocumentReader reader = new TikaDocumentReader(new FileSystemResource(tempFile));//把排版全扔掉改为纯文本
        List<Document> rawDocuments = reader.get();
        List<Document> splitChunks = tokenTextSplitter.apply(rawDocuments);//分块

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
            //给每个知识库分块附加文档 ID、文件名和分块序号。
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

    /**
     * 给每个知识库分块附加文档 ID、文件名和分块序号。
     */
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

    /**
     * 保存文档分块和向量 ID 的对应关系。
     */
    private void saveChunkIndex(Long documentId, List<Document> chunks) {
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < chunks.size(); i++) {
            KnowledgeChunk knowledgeChunk = new KnowledgeChunk();
            knowledgeChunk.setDocumentId(documentId);
            knowledgeChunk.setVectorId(chunks.get(i).getId());
            knowledgeChunk.setChunkIndex(i);
            knowledgeChunk.setContent(chunks.get(i).getText());
            knowledgeChunk.setCreatedAt(now);
            knowledgeChunkMapper.insert(knowledgeChunk);
        }
    }

    /**
     * 从文件名中提取临时文件后缀。
     */
    private static String getSuffix(String filename) {
        int dotIndex = filename == null ? -1 : filename.lastIndexOf('.');
        return dotIndex >= 0 ? filename.substring(dotIndex) : ".tmp";
    }

    /**
     * 安静删除临时文件，删除失败不影响主流程。
     */
    private static void deleteQuietly(Path tempFile) {
        try {
            Files.deleteIfExists(tempFile);
        } catch (Exception ignored) {
            // 临时文件删除失败不影响业务响应，生产环境可接入日志系统记录。
        }
    }
}
