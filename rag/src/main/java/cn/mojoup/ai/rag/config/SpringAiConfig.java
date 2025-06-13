package cn.mojoup.ai.rag.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

}