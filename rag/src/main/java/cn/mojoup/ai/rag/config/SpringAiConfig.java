package cn.mojoup.ai.rag.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Spring AI配置类
 * 配置向量数据库、嵌入模型、聊天模型等组件
 *
 * @author matt
 */
@Slf4j
@Configuration
public class SpringAiConfig {

    @Value("${rag.spring-ai.openai.api-key:}")
    private String openaiApiKey;

    @Value("${rag.spring-ai.openai.base-url:https://api.openai.com}")
    private String openaiBaseUrl;

    @Value("${rag.spring-ai.openai.embedding-model:text-embedding-ada-002}")
    private String embeddingModel;

    @Value("${rag.spring-ai.openai.chat-model:gpt-3.5-turbo}")
    private String chatModel;

    @Value("${rag.spring-ai.vectorstore.type:chroma}")
    private String vectorStoreType;

    @Value("${rag.spring-ai.vectorstore.chroma.url:http://localhost:8000}")
    private String chromaUrl;

    @Value("${rag.spring-ai.vectorstore.chroma.collection-name:rag_documents}")
    private String chromaCollectionName;

    // TODO: 配置OpenAI嵌入模型
    // @Bean
    // @ConditionalOnProperty(name = "rag.spring-ai.openai.api-key")
    // public OpenAiEmbeddingModel embeddingModel() {
    //     log.info("配置OpenAI嵌入模型: model={}, baseUrl={}", embeddingModel, openaiBaseUrl);
    //     
    //     return OpenAiEmbeddingModel.builder()
    //         .apiKey(openaiApiKey)
    //         .baseUrl(openaiBaseUrl)
    //         .model(embeddingModel)
    //         .build();
    // }

    // TODO: 配置OpenAI聊天模型
    // @Bean
    // @ConditionalOnProperty(name = "rag.spring-ai.openai.api-key")
    // public OpenAiChatModel chatModel() {
    //     log.info("配置OpenAI聊天模型: model={}, baseUrl={}", chatModel, openaiBaseUrl);
    //     
    //     return OpenAiChatModel.builder()
    //         .apiKey(openaiApiKey)
    //         .baseUrl(openaiBaseUrl)
    //         .model(chatModel)
    //         .temperature(0.7)
    //         .maxTokens(2048)
    //         .build();
    // }

    // TODO: 配置Chroma向量数据库
    // @Bean
    // @ConditionalOnProperty(name = "rag.spring-ai.vectorstore.type", havingValue = "chroma")
    // public ChromaVectorStore vectorStore(EmbeddingModel embeddingModel) {
    //     log.info("配置Chroma向量数据库: url={}, collection={}", chromaUrl, chromaCollectionName);
    //     
    //     ChromaApi chromaApi = new ChromaApi(chromaUrl);
    //     return new ChromaVectorStore(embeddingModel, chromaApi, chromaCollectionName);
    // }

    /**
     * 模拟的向量数据库Bean（用于开发测试）
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "rag.spring-ai.openai.api-key", matchIfMissing = true)
    public VectorStore mockVectorStoreForEmbedding() {
        log.warn("使用模拟向量数据库 - 仅用于开发测试");
        return new MockVectorStoreImpl();
    }

    // TODO: 配置Pinecone向量数据库
    // @Bean
    // @ConditionalOnProperty(name = "rag.spring-ai.vectorstore.type", havingValue = "pinecone")
    // public PineconeVectorStore pineconeVectorStore(EmbeddingModel embeddingModel) {
    //     String apiKey = System.getenv("PINECONE_API_KEY");
    //     String environment = System.getenv("PINECONE_ENVIRONMENT");
    //     String indexName = System.getenv("PINECONE_INDEX");
    //     
    //     log.info("配置Pinecone向量数据库: environment={}, index={}", environment, indexName);
    //     
    //     PineconeConnectionProvider connectionProvider = new PineconeConnectionProvider(apiKey, environment);
    //     return new PineconeVectorStore(connectionProvider, embeddingModel, indexName);
    // }

    // TODO: 配置Redis向量数据库
    // @Bean
    // @ConditionalOnProperty(name = "rag.spring-ai.vectorstore.type", havingValue = "redis")
    // public RedisVectorStore redisVectorStore(EmbeddingModel embeddingModel) {
    //     String redisUrl = System.getenv("REDIS_URL");
    //     String indexName = System.getenv("REDIS_INDEX");
    //     
    //     log.info("配置Redis向量数据库: url={}, index={}", redisUrl, indexName);
    //     
    //     RedisConnectionProvider connectionProvider = new RedisConnectionProvider(redisUrl);
    //     return new RedisVectorStore(connectionProvider, embeddingModel, indexName);
    // }

    // TODO: 配置Cross-Encoder重排序模型
    // @Bean
    // @ConditionalOnProperty(name = "rag.search.rerank.enabled", havingValue = "true")
    // public CrossEncoderModel crossEncoderModel() {
    //     String modelName = System.getProperty("rag.search.rerank.cross-encoder-model", 
    //                                          "cross-encoder/ms-marco-MiniLM-L-6-v2");
    //     
    //     log.info("配置Cross-Encoder重排序模型: model={}", modelName);
    //     
    //     return CrossEncoderModel.builder()
    //         .modelName(modelName)
    //         .build();
    // }

    /**
     * 模拟的嵌入模型Bean（用于开发测试）
     */
    @Bean
    @ConditionalOnProperty(name = "rag.spring-ai.openai.api-key", matchIfMissing = true)
    public MockEmbeddingModel mockEmbeddingModel() {
        log.warn("使用模拟嵌入模型 - 仅用于开发测试");
        return new MockEmbeddingModel();
    }

    /**
     * 模拟的聊天模型Bean（用于开发测试）
     */
    @Bean
    @ConditionalOnProperty(name = "rag.spring-ai.openai.api-key", matchIfMissing = true)
    public MockChatModel mockChatModel() {
        log.warn("使用模拟聊天模型 - 仅用于开发测试");
        return new MockChatModel();
    }

    /**
     * 模拟的向量数据库Bean（用于开发测试）
     */
    @Bean
    @ConditionalOnProperty(name = "rag.spring-ai.openai.api-key", matchIfMissing = true)
    public MockVectorStore mockVectorStore() {
        log.warn("使用模拟向量数据库 - 仅用于开发测试");
        return new MockVectorStore();
    }

    // ==================== 模拟实现类 ====================

    /**
     * 模拟嵌入模型实现
     */
    public static class MockEmbeddingModel {
        public java.util.List<Double> embed(String text) {
            log.debug("模拟嵌入: text={}", text);
            // 返回模拟向量
            java.util.Random random = new java.util.Random(text.hashCode());
            java.util.List<Double> vector = new java.util.ArrayList<>();
            for (int i = 0; i < 1536; i++) {
                vector.add(random.nextGaussian());
            }
            return vector;
        }
    }

    /**
     * 模拟聊天模型实现
     */
    public static class MockChatModel {
        public String generate(String prompt) {
            log.debug("模拟生成: prompt={}", prompt);
            return "这是一个模拟的AI回答，基于提示词: " + prompt.substring(0, Math.min(50, prompt.length())) + "...";
        }
    }

    /**
     * 模拟向量数据库实现
     */
    public static class MockVectorStore {
        public java.util.List<String> similaritySearch(java.util.List<Double> queryVector, int topK) {
            log.debug("模拟向量搜索: vectorDim={}, topK={}", queryVector.size(), topK);
            java.util.List<String> results = new java.util.ArrayList<>();
            for (int i = 0; i < Math.min(topK, 5); i++) {
                results.add("模拟文档内容 " + (i + 1));
            }
            return results;
        }
    }

    /**
     * 模拟向量数据库实现（VectorStore接口）
     */
    public static class MockVectorStoreImpl implements VectorStore {
        
        private final java.util.Map<String, org.springframework.ai.document.Document> documents = 
                new java.util.concurrent.ConcurrentHashMap<>();

        @Override
        public void add(java.util.List<org.springframework.ai.document.Document> documents) {
            log.debug("模拟添加 {} 个文档", documents.size());
            for (org.springframework.ai.document.Document doc : documents) {
                this.documents.put(doc.getId(), doc);
            }
        }

        @Override
        public java.util.Optional<Boolean> delete(java.util.List<String> idList) {
            log.debug("模拟删除 {} 个文档", idList.size());
            for (String id : idList) {
                documents.remove(id);
            }
            return java.util.Optional.of(true);
        }

        @Override
        public java.util.List<org.springframework.ai.document.Document> similaritySearch(String query) {
            return similaritySearch(
                org.springframework.ai.vectorstore.SearchRequest.query(query).withTopK(5)
            );
        }

        @Override
        public java.util.List<org.springframework.ai.document.Document> similaritySearch(
                org.springframework.ai.vectorstore.SearchRequest request) {
            log.debug("模拟相似性搜索: query={}, topK={}", request.getQuery(), request.getTopK());
            
            java.util.List<org.springframework.ai.document.Document> results = new java.util.ArrayList<>();
            int count = 0;
            for (org.springframework.ai.document.Document doc : documents.values()) {
                if (count >= request.getTopK()) break;
                results.add(doc);
                count++;
            }
            
            // 如果没有文档，返回模拟文档
            if (results.isEmpty()) {
                for (int i = 0; i < Math.min(request.getTopK(), 3); i++) {
                    results.add(new org.springframework.ai.document.Document(
                        "mock-doc-" + i,
                        "这是模拟文档内容 " + (i + 1) + "，用于演示向量搜索功能。",
                        java.util.Map.of(
                            "source", "mock",
                            "index", i,
                            "similarity", 0.9 - (i * 0.1)
                        )
                    ));
                }
            }
            
            return results;
        }
    }
} 