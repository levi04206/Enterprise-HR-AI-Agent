package com.example.enterprisehraiagent.service;

import com.example.enterprisehraiagent.dto.IngestResponse;
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
    private final TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();

    public KnowledgeIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public Mono<IngestResponse> ingest(FilePart filePart) {
        return Mono.usingWhen(
                createTempFile(filePart),
                tempFile -> filePart.transferTo(tempFile)
                        .then(Mono.fromCallable(() -> ingestBlocking(filePart.filename(), tempFile))
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

    private IngestResponse ingestBlocking(String filename, Path tempFile) {
        // TikaDocumentReader 可以读取 TXT/PDF/DOCX 等常见办公文档，适合企业知识库入口。
        TikaDocumentReader reader = new TikaDocumentReader(new FileSystemResource(tempFile));
        List<Document> rawDocuments = reader.get();

        // TokenTextSplitter 会按模型 token 粒度切分，避免单段文本过长导致检索不准或上下文超限。
        List<Document> chunks = tokenTextSplitter.apply(rawDocuments);

        // VectorStore.add 会调用 EmbeddingModel 将 chunk 转成向量，并写入当前配置的向量库。
        vectorStore.add(chunks);

        return new IngestResponse(filename, rawDocuments.size(), chunks.size(), "知识库文档已入库");
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
