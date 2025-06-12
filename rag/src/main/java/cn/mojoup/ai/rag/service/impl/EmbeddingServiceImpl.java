package cn.mojoup.ai.rag.service.impl;

import cn.mojoup.ai.rag.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 嵌入服务实现
 *
 * @author matt
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

    private final EmbeddingModel embeddingModel;

    // 支持的模型配置
    private static final Map<String, ModelConfig> MODEL_CONFIGS = Map.of(
            "text-embedding-ada-002", new ModelConfig(1536, "OpenAI Ada 002"),
            "text-embedding-3-small", new ModelConfig(1536, "OpenAI Embedding 3 Small"),
            "text-embedding-3-large", new ModelConfig(3072, "OpenAI Embedding 3 Large"),
            "bge-large-zh", new ModelConfig(1024, "BGE Large Chinese"),
            "m3e-base", new ModelConfig(768, "M3E Base Model")
    );

    @Override
    public List<Double> embedDocument(Document document, String model) {
        validateModel(model);
        
        if (document == null || !StringUtils.hasText(document.getText())) {
            throw new IllegalArgumentException("Document content cannot be empty");
        }

        try {
            log.debug("Embedding document with model: {}", model);
            return embedText(document.getText(), model);
        } catch (Exception e) {
            log.error("Failed to embed document with model: {}", model, e);
            throw new RuntimeException("Failed to embed document: " + e.getMessage(), e);
        }
    }

    @Override
    public List<List<Double>> embedDocuments(List<Document> documents, String model) {
        validateModel(model);
        
        if (documents == null || documents.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            log.debug("Embedding {} documents with model: {}", documents.size(), model);
            
            List<String> texts = documents.stream()
                    .map(Document::getText)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            
            return embedTexts(texts, model);
        } catch (Exception e) {
            log.error("Failed to embed documents with model: {}", model, e);
            throw new RuntimeException("Failed to embed documents: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Double> embedText(String text, String model) {
        validateModel(model);
        
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("Text cannot be empty");
        }

        try {
            log.debug("Embedding text (length: {}) with model: {}", text.length(), model);
            
            EmbeddingRequest request = new EmbeddingRequest(Collections.singletonList(text), null);
            EmbeddingResponse response = embeddingModel.call(request);
            
            if (response.getResults().isEmpty()) {
                throw new RuntimeException("No embedding results returned");
            }
            
            float[] embedding = response.getResults().get(0).getOutput();
            List<Double> result = new ArrayList<>();
            for (float f : embedding) {
                result.add((double) f);
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to embed text with model: {}", model, e);
            throw new RuntimeException("Failed to embed text: " + e.getMessage(), e);
        }
    }

    @Override
    public List<List<Double>> embedTexts(List<String> texts, String model) {
        validateModel(model);
        
        if (texts == null || texts.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            log.debug("Embedding {} texts with model: {}", texts.size(), model);
            
            List<String> validTexts = texts.stream()
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            
            if (validTexts.isEmpty()) {
                return new ArrayList<>();
            }
            
            EmbeddingRequest request = new EmbeddingRequest(validTexts, null);
            EmbeddingResponse response = embeddingModel.call(request);
            
            return response.getResults().stream()
                    .map(result -> {
                        float[] embedding = result.getOutput();
                        List<Double> doubleList = new ArrayList<>();
                        for (float f : embedding) {
                            doubleList.add((double) f);
                        }
                        return doubleList;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to embed texts with model: {}", model, e);
            throw new RuntimeException("Failed to embed texts: " + e.getMessage(), e);
        }
    }

    @Override
    public int getEmbeddingDimension(String model) {
        ModelConfig config = MODEL_CONFIGS.get(model);
        if (config == null) {
            // 默认维度，或者可以通过实际调用获取
            return 1536;
        }
        return config.dimension;
    }

    @Override
    public List<String> getSupportedModels() {
        return new ArrayList<>(MODEL_CONFIGS.keySet());
    }

    @Override
    public boolean isModelSupported(String model) {
        return StringUtils.hasText(model) && MODEL_CONFIGS.containsKey(model);
    }

    @Override
    public Map<String, Object> getModelInfo(String model) {
        ModelConfig config = MODEL_CONFIGS.get(model);
        if (config == null) {
            return Map.of();
        }
        
        return Map.of(
                "model", model,
                "dimension", config.dimension,
                "description", config.description,
                "supported", true
        );
    }

    /**
     * 验证模型
     */
    private void validateModel(String model) {
        if (!StringUtils.hasText(model)) {
            throw new IllegalArgumentException("Model cannot be empty");
        }
        
        // 注意：这里我们不强制要求模型在预定义列表中，因为可能有新的模型
        log.debug("Using embedding model: {}", model);
    }

    /**
     * 模型配置类
     */
    private static class ModelConfig {
        final int dimension;
        final String description;

        ModelConfig(int dimension, String description) {
            this.dimension = dimension;
            this.description = description;
        }
    }
} 