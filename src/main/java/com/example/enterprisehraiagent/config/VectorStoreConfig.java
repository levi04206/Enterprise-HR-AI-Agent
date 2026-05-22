package com.example.enterprisehraiagent.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 向量库配置。
 *
 * <p>当前工程默认使用 SimpleVectorStore，它是 Spring AI 提供的内存向量库：
 * 优点是无需 Redis Stack / Milvus / PgVector 即可跑通 RAG Demo；
 * 缺点是服务重启后向量数据会丢失，不适合生产环境。</p>
 *
 * <p>后续如果切换 Redis Stack，可引入 Spring AI Redis VectorStore Starter，
 * 然后把这里替换为 RedisVectorStore Bean，业务层无需改动，因为它只依赖
 * VectorStore 接口。</p>
 */
@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
