package cn.mojoup.ai.rag.reader.impl;

import cn.mojoup.ai.rag.reader.DocumentReader;
import cn.mojoup.ai.rag.reader.ReaderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PDF文档读取器
 * 支持PDF文档的解析和参数配置
 *
 * @author matt
 */
@Component
public class PdfDocumentReader implements DocumentReader {

    private static final Logger logger = LoggerFactory.getLogger(PdfDocumentReader.class);
    
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList("pdf");

    @Override
    public List<Document> read(Resource resource, ReaderConfig config) {
        try {
            logger.debug("Reading PDF document: {}", resource.getFilename());

            // 构建PDF读取配置
            PdfDocumentReaderConfig pdfConfig = PdfDocumentReaderConfig.builder()
                    .withPageExtractedTextFormatter(config.isPreserveFormatting() ? 
                        PdfDocumentReaderConfig.ExtractedTextFormatter.builder()
                            .withNumberOfBottomTextLinesToDelete(0)
                            .withNumberOfTopPagesToSkipBeforeDelete(0)
                            .withLeftAlignment(true)
                            .build() : 
                        PdfDocumentReaderConfig.ExtractedTextFormatter.defaults())
                    .withPagesPerDocument(1) // 每页一个文档
                    .build();

            // 创建PDF读取器
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource, pdfConfig);
            List<Document> documents = pdfReader.get();

            // 处理文档
            return processDocuments(documents, config);

        } catch (Exception e) {
            logger.error("Failed to read PDF document: {}", resource.getFilename(), e);
            throw new RuntimeException("Failed to read PDF document", e);
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
        return "PDF";
    }

    /**
     * 处理文档，应用配置参数
     */
    private List<Document> processDocuments(List<Document> documents, ReaderConfig config) {
        return documents.stream()
                .map(doc -> {
                    String content = doc.getText();
                    Map<String, Object> metadata = new HashMap<>(doc.getMetadata());

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

        return cleaned.trim();
    }
} 