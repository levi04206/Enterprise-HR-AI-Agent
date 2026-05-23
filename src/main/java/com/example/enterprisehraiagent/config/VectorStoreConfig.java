package com.example.enterprisehraiagent.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * 向量库配置。
 *
 * <p>默认 profile 使用 SimpleVectorStore，它是 Spring AI 提供的内存向量库：
 * 优点是无需 Redis Stack / Milvus / PgVector 即可跑通 RAG Demo；
 * 缺点是服务重启后向量数据会丢失，不适合生产环境。</p>
 *
 * <p>启用 redis profile 后，`spring.ai.vectorstore.type=redis` 会触发
 * Spring AI RedisVectorStore 自动配置。业务层始终只依赖 VectorStore 接口，
 * 因此不需要关心底层到底是内存向量库还是 Redis Stack。</p>
 */
@Configuration
public class VectorStoreConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.ai.vectorstore.type", havingValue = "in-memory", matchIfMissing = true)
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
