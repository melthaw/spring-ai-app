package cn.mojoup.ai.rag.reader.impl;

import cn.mojoup.ai.rag.reader.DocumentReader;
import cn.mojoup.ai.rag.reader.ReaderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tika文档读取器
 * 支持Office文档、HTML、XML等多种格式
 *
 * @author matt
 */
@Component
public class TikaDocumentReader implements DocumentReader {

    private static final Logger logger = LoggerFactory.getLogger(TikaDocumentReader.class);
    
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(
        "doc", "docx", "ppt", "pptx", "xls", "xlsx", 
        "html", "htm", "xml", "csv", "rtf", "odt", "ods", "odp"
    );

    @Override
    public List<Document> read(Resource resource, ReaderConfig config) {
        try {
            logger.debug("Reading document with Tika: {}", resource.getFilename());

            // 创建Tika读取器
            org.springframework.ai.reader.tika.TikaDocumentReader tikaReader = 
                new org.springframework.ai.reader.tika.TikaDocumentReader(resource);
            List<Document> documents = tikaReader.get();

            // 处理文档
            return processDocuments(documents, config, resource);

        } catch (Exception e) {
            logger.error("Failed to read document with Tika: {}", resource.getFilename(), e);
            throw new RuntimeException("Failed to read document with Tika", e);
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
        return "TIKA";
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
                    metadata.put("extract_formulas", config.isExtractFormulas());
                    metadata.put("extract_comments", config.isExtractComments());
                    metadata.put("extract_hidden_text", config.isExtractHiddenText());
                    metadata.put("parse_embedded_documents", config.isParseEmbeddedDocuments());

                    // 检测具体文件类型
                    String filename = resource.getFilename();
                    if (filename != null) {
                        String extension = getFileExtension(filename);
                        metadata.put("file_extension", extension);
                        metadata.put("document_type", getDocumentType(extension));
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

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1).toLowerCase() : "";
    }

    /**
     * 根据扩展名确定文档类型
     */
    private String getDocumentType(String extension) {
        switch (extension) {
            case "doc":
            case "docx":
                return "word_document";
            case "ppt":
            case "pptx":
                return "presentation";
            case "xls":
            case "xlsx":
                return "spreadsheet";
            case "html":
            case "htm":
                return "web_page";
            case "xml":
                return "xml_document";
            case "csv":
                return "csv_data";
            case "rtf":
                return "rich_text";
            case "odt":
                return "open_document_text";
            case "ods":
                return "open_document_spreadsheet";
            case "odp":
                return "open_document_presentation";
            default:
                return "unknown";
        }
    }
} 