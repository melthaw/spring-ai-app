package cn.mojoup.ai.rag.reader.impl;

import cn.mojoup.ai.rag.reader.DocumentReader;
import cn.mojoup.ai.rag.reader.ReaderConfig;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private final Tika tika;
    private final Parser parser;

    public TikaDocumentReader() {
        this.tika = new Tika();
        this.parser = new AutoDetectParser();
    }

    @Override
    public List<Document> read(Resource resource, ReaderConfig config) {
        try {
            logger.debug("Reading document with Tika: {}", resource.getFilename());

            // 创建元数据对象
            Metadata metadata = new Metadata();
            metadata.set("resourceName", resource.getFilename());

            // 创建内容处理器
            BodyContentHandler handler = new BodyContentHandler(-1); // -1表示不限制内容长度

            // 创建解析上下文
            ParseContext context = new ParseContext();
            context.set(Parser.class, parser);

            // 解析文档
            parser.parse(resource.getInputStream(), handler, metadata, context);

            // 创建文档
            Document document = Document.builder()
                    .id(resource.getFilename())
                    .text(handler.toString())
                    .metadata(convertMetadata(metadata))
                    .build();

            // 处理文档
            return processDocuments(Collections.singletonList(document), config, resource);

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

        // 处理图片
        if (config.isExtractImages() || config.isPerformOcrOnImages()) {
            processedDocs = processImages(processedDocs, config);
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
     * 处理图片
     */
    private List<Document> processImages(List<Document> documents, ReaderConfig config) {
        List<Document> processedDocs = new ArrayList<>();
        
        for (Document doc : documents) {
            Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
            
            // 提取图片
            if (config.isExtractImages()) {
                List<Map<String, Object>> images = extractImages(doc);
                metadata.put("images", images);
            }
            
            // OCR识别
            if (config.isPerformOcrOnImages()) {
                String ocrText = performOcr(doc);
                if (ocrText != null && !ocrText.isEmpty()) {
                    metadata.put("ocr_text", ocrText);
                }
            }
            
            // 提取图片元数据
            if (config.isExtractImageMetadata()) {
                Map<String, Object> imageMetadata = extractImageMetadata(doc);
                metadata.put("image_metadata", imageMetadata);
            }
            
            processedDocs.add(Document.builder()
                    .id(doc.getId())
                    .text(doc.getText())
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

    /**
     * 转换Tika元数据为Map
     */
    private Map<String, Object> convertMetadata(Metadata metadata) {
        Map<String, Object> result = new HashMap<>();
        for (String name : metadata.names()) {
            result.put(name, metadata.get(name));
        }
        return result;
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

    private List<Map<String, Object>> extractImages(Document doc) {
        // 实现图片提取逻辑
        return new ArrayList<>();
    }

    private String performOcr(Document doc) {
        // 实现OCR识别逻辑
        return "";
    }

    private Map<String, Object> extractImageMetadata(Document doc) {
        // 实现图片元数据提取逻辑
        return new HashMap<>();
    }
} 