package cn.mojoup.ai.rag.reader.impl;

import cn.mojoup.ai.rag.reader.DocumentReader;
import cn.mojoup.ai.rag.reader.ReaderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文本文档读取器
 * 支持txt、md等文本文件的解析
 *
 * @author matt
 */
@Component
public class TextDocumentReader implements DocumentReader {

    private static final Logger logger = LoggerFactory.getLogger(TextDocumentReader.class);
    
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList("txt", "md", "text", "markdown");

    @Override
    public List<Document> read(Resource resource, ReaderConfig config) {
        try {
            logger.debug("Reading text document: {}", resource.getFilename());

            // 创建文本读取器
            TextReader textReader = new TextReader(resource);
            List<Document> documents = textReader.get();

            // 处理文档
            return processDocuments(documents, config, resource);

        } catch (Exception e) {
            logger.error("Failed to read text document: {}", resource.getFilename(), e);
            throw new RuntimeException("Failed to read text document", e);
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
        return "TEXT";
    }

    /**
     * 处理文档，应用配置参数
     */
    private List<Document> processDocuments(List<Document> documents, ReaderConfig config, Resource resource) {
        return documents.stream()
                .map(doc -> {
                    String content = doc.getText();
                    Map<String, Object> metadata = new HashMap<>(doc.getMetadata());

                    // 应用内容清理
                    content = cleanContent(content, config);

                    // 分块处理
                    if (config.isEnableChunking() && content.length() > config.getChunkSize()) {
                        // 这里可以实现分块逻辑，暂时简化处理
                        content = content.substring(0, Math.min(content.length(), config.getChunkSize()));
                        metadata.put("chunked", true);
                    }

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
                    metadata.put("charset", config.getCharset().name());
                    metadata.put("preserve_formatting", config.isPreserveFormatting());

                    // 检测文件类型特殊处理
                    String filename = resource.getFilename();
                    if (filename != null) {
                        if (filename.endsWith(".md") || filename.endsWith(".markdown")) {
                            metadata.put("format", "markdown");
                            // 可以添加Markdown特殊处理逻辑
                        } else {
                            metadata.put("format", "plain_text");
                        }
                    }

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

        // 处理行结束符
        if (!config.getLineEnding().equals("\n")) {
            cleaned = cleaned.replaceAll("\r\n|\n|\r", config.getLineEnding());
        }

        // 移除多余空白
        if (config.isRemoveWhitespace()) {
            cleaned = cleaned.replaceAll("\\s+", " ");
        }

        // 移除空行
        if (config.isRemoveEmptyLines()) {
            cleaned = cleaned.replaceAll("(?m)^\\s*$[\r\n]*", "");
        }

        // Unicode标准化
        if (config.isNormalizeUnicode()) {
            cleaned = java.text.Normalizer.normalize(cleaned, java.text.Normalizer.Form.NFC);
        }

        // 保持格式化
        if (!config.isPreserveFormatting()) {
            // 简单的格式化清理
            cleaned = cleaned.replaceAll("\\s*\n\\s*", " ");
        }

        return cleaned.trim();
    }
} 