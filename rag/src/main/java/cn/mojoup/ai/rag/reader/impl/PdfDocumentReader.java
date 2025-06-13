package cn.mojoup.ai.rag.reader.impl;

import cn.mojoup.ai.rag.reader.DocumentReader;
import cn.mojoup.ai.rag.reader.ReaderConfig;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

            // 创建PDF读取器
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            
            // 加载PDF文档
            PDDocument pdfDocument = Loader.loadPDF(resource.getFile());
            String text = stripper.getText(pdfDocument);
            
            // 创建文档
            Document document = Document.builder()
                    .id(resource.getFilename())
                    .text(text)
                    .metadata(new HashMap<>())
                    .build();
            
            // 处理文档
            List<Document> processedDocs = processDocuments(Collections.singletonList(document), config);
            
            // 关闭PDF文档
            pdfDocument.close();
            
            return processedDocs;

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
        List<Document> processedDocs = new ArrayList<>();
        
        if (config.isReadByPage()) {
            // 按页处理
            processedDocs.addAll(processByPage(documents, config));
        } else if (config.isReadByParagraph()) {
            // 按段落处理
            processedDocs.addAll(processByParagraph(documents, config));
        } else {
            // 默认处理
            processedDocs.addAll(processDefault(documents, config));
        }

        // 处理表格
        if (config.isDetectAndMergeTables()) {
            processedDocs = processTables(processedDocs, config);
        }

        return processedDocs;
    }

    /**
     * 按页处理文档
     */
    private List<Document> processByPage(List<Document> documents, ReaderConfig config) {
        return documents.stream()
                .map(doc -> {
                    String content = doc.getText();
                    Map<String, Object> metadata = new HashMap<>(doc.getMetadata());

                    // 应用内容清理
                    content = cleanContent(content, config);

                    // 添加页码信息
                    metadata.put("page_number", doc.getMetadata().get("page_number"));

                    // 处理跨页段落
                    if (config.isMergeCrossPageParagraphs()) {
                        // 实现跨页段落合并逻辑
                        metadata.put("merged_paragraphs", true);
                    }

                    return Document.builder()
                            .id(doc.getId())
                            .text(content)
                            .metadata(metadata)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 按段落处理文档
     */
    private List<Document> processByParagraph(List<Document> documents, ReaderConfig config) {
        List<Document> paragraphDocs = new ArrayList<>();
        
        for (Document doc : documents) {
            String content = doc.getText();
            String[] paragraphs = content.split("\\n\\s*\\n");
            
            for (String paragraph : paragraphs) {
                if (paragraph.trim().isEmpty()) continue;
                
                Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
                metadata.put("paragraph_number", paragraphDocs.size() + 1);
                metadata.put("page_number", doc.getMetadata().get("page_number"));
                
                paragraphDocs.add(Document.builder()
                        .id(doc.getId() + "_p" + (paragraphDocs.size() + 1))
                        .text(cleanContent(paragraph, config))
                        .metadata(metadata)
                        .build());
            }
        }
        
        return paragraphDocs;
    }

    /**
     * 默认处理文档
     */
    private List<Document> processDefault(List<Document> documents, ReaderConfig config) {
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

                    return Document.builder()
                            .id(doc.getId())
                            .text(content)
                            .metadata(metadata)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 处理表格
     */
    private List<Document> processTables(List<Document> documents, ReaderConfig config) {
        List<Document> processedDocs = new ArrayList<>();
        
        for (Document doc : documents) {
            String content = doc.getText();
            Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
            
            // 检测表格
            if (isTableContent(content)) {
                metadata.put("is_table", true);
                
                // 提取表格标题
                if (config.isExtractTableHeaders()) {
                    String header = extractTableHeader(content);
                    metadata.put("table_header", header);
                }
                
                // 提取表格页脚
                if (config.isExtractTableFooters()) {
                    String footer = extractTableFooter(content);
                    metadata.put("table_footer", footer);
                }
                
                // 提取表格说明
                if (config.isExtractTableCaptions()) {
                    String caption = extractTableCaption(content);
                    metadata.put("table_caption", caption);
                }
                
                // 保持表格结构
                if (config.isPreserveTableStructure()) {
                    content = preserveTableStructure(content);
                }
            }
            
            processedDocs.add(Document.builder()
                    .id(doc.getId())
                    .text(content)
                    .metadata(metadata)
                    .build());
        }
        
        return processedDocs;
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

    // 辅助方法
    private boolean isTableContent(String content) {
        // 实现表格检测逻辑
        return content.contains("|") || content.contains("\t");
    }

    private String extractTableHeader(String content) {
        // 实现表格标题提取逻辑
        return "";
    }

    private String extractTableFooter(String content) {
        // 实现表格页脚提取逻辑
        return "";
    }

    private String extractTableCaption(String content) {
        // 实现表格说明提取逻辑
        return "";
    }

    private String preserveTableStructure(String content) {
        // 实现表格结构保持逻辑
        return content;
    }
} 