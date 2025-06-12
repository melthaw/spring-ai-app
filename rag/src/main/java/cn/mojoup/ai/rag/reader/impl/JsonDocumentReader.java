package cn.mojoup.ai.rag.reader.impl;

import cn.mojoup.ai.rag.reader.DocumentReader;
import cn.mojoup.ai.rag.reader.ReaderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON文档读取器
 * 支持JSON文件的解析和配置
 *
 * @author matt
 */
@Component
public class JsonDocumentReader implements DocumentReader {

    private static final Logger logger = LoggerFactory.getLogger(JsonDocumentReader.class);
    
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList("json", "jsonl", "ndjson");

    @Override
    public List<Document> read(Resource resource, ReaderConfig config) {
        try {
            logger.debug("Reading JSON document: {}", resource.getFilename());

            // 创建JSON读取器
            JsonReader jsonReader = new JsonReader(resource, 
                config.getJsonPath(), 
                config.getCustomProperty("keys", "content").toString());
            List<Document> documents = jsonReader.get();

            // 处理文档
            return processDocuments(documents, config, resource);

        } catch (Exception e) {
            logger.error("Failed to read JSON document: {}", resource.getFilename(), e);
            throw new RuntimeException("Failed to read JSON document", e);
        }
    }

    @Override
    public boolean supports(String extension) {
        return SUPPORTED_EXTENSIONS.contains(extension.toLowerCase());
    }

    @Override
    public List<String> getSupportedExtensions() {
        return SUPPORTED_EXTENSIONS;
    }

    @Override
    public String getReaderType() {
        return "JSON";
    }

    /**
     * 处理文档，应用配置参数
     */
    private List<Document> processDocuments(List<Document> documents, ReaderConfig config, Resource resource) {
        return documents.stream()
                .map(doc -> {
                    String content = doc.getText();
                    Map<String, Object> metadata = new HashMap<>(doc.getMetadata());

                    // JSON特殊处理
                    if (config.isFlattenJson()) {
                        // 可以实现JSON扁平化逻辑
                        metadata.put("flattened", true);
                    }

                    // 应用内容清理
                    content = cleanContent(content, config);

                    // 长度限制
                    if (content.length() > config.getMaxContentLength()) {
                        content = content.substring(0, config.getMaxContentLength());
                        metadata.put("truncated", true);
                        metadata.put("original_length", doc.getText().length());
                    }

                    // 添加处理信息
                    metadata.put("reader_type", getReaderType());
                    metadata.put("processed_at", System.currentTimeMillis());
                    metadata.put("language", config.getLanguage());
                    metadata.put("json_path", config.getJsonPath());
                    metadata.put("flatten_json", config.isFlattenJson());
                    metadata.put("depth_limit", config.getJsonDepthLimit());

                    return Document.builder()
                            .id(doc.getId())
                            .text(content)
                            .metadata(metadata)
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 清理内容
     */
    private String cleanContent(String content, ReaderConfig config) {
        if (content == null) return "";

        String cleaned = content;

        // 移除多余空白
        if (config.isRemoveWhitespace()) {
            cleaned = cleaned.replaceAll("\\s+", " ");
        }

        // Unicode标准化
        if (config.isNormalizeUnicode()) {
            cleaned = java.text.Normalizer.normalize(cleaned, java.text.Normalizer.Form.NFC);
        }

        return cleaned.trim();
    }
} 