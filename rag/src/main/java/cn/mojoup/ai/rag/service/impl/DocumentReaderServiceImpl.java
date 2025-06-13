package cn.mojoup.ai.rag.service.impl;

import cn.mojoup.ai.rag.reader.DocumentReader;
import cn.mojoup.ai.rag.reader.DocumentReaderFactory;
import cn.mojoup.ai.rag.reader.ReaderConfig;
import cn.mojoup.ai.rag.service.DocumentReaderService;
import cn.mojoup.ai.upload.domain.FileInfo;
import cn.mojoup.ai.upload.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文档读取服务实现
 * 使用策略模式管理不同类型的文档读取器
 *
 * @author matt
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentReaderServiceImpl implements DocumentReaderService {

    private final FileUploadService fileUploadService;
    private final DocumentReaderFactory readerFactory;

    // 文本清理模式
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final Pattern MULTIPLE_NEWLINES = Pattern.compile("\\n{3,}");

    @Override
    public List<Document> readDocuments(FileInfo fileInfo) {
        return readDocuments(fileInfo, ReaderConfig.defaultConfig());
    }

    /**
     * 使用自定义配置读取文档
     */
    public List<Document> readDocuments(FileInfo fileInfo, ReaderConfig config) {
        try {
            log.info("Reading documents from file: {} ({}) with config",
                     fileInfo.getOriginalFileName(), fileInfo.getFileExtension());

            // 获取文件资源
            Resource resource = fileUploadService.getFileResourceById(fileInfo.getFileId());

            return readDocuments(resource, fileInfo, config);

        } catch (Exception e) {
            log.error("Failed to read documents from file: {}", fileInfo.getOriginalFileName(), e);
            throw new RuntimeException("Failed to read documents: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Document> readDocuments(Resource resource, FileInfo fileInfo) {
        return readDocuments(resource, fileInfo, ReaderConfig.defaultConfig());
    }

    /**
     * 使用自定义配置读取文档
     */
    public List<Document> readDocuments(Resource resource, FileInfo fileInfo, ReaderConfig config) {
        String extension = fileInfo.getFileExtension().toLowerCase();

        if (!isSupported(extension)) {
            throw new IllegalArgumentException("Unsupported file type: " + extension);
        }

        try {
            // 获取合适的读取器
            Optional<DocumentReader> readerOpt = readerFactory.getReader(extension);
            if (!readerOpt.isPresent()) {
                throw new IllegalArgumentException("No reader found for file type: " + extension);
            }

            DocumentReader reader = readerOpt.get();
            log.debug("Using reader: {} for file: {}", reader.getReaderType(), fileInfo.getOriginalFileName());

            // 读取文档
            List<Document> documents = reader.read(resource, config);

            // 添加文件信息元数据
            documents = documents.stream()
                                 .map(doc -> enrichDocumentMetadata(doc, fileInfo, config))
                                 .collect(Collectors.toList());

            log.info("Successfully read {} documents from file: {} using reader: {}",
                     documents.size(), fileInfo.getOriginalFileName(), reader.getReaderType());
            return documents;

        } catch (Exception e) {
            log.error("Failed to read documents from resource: {}", fileInfo.getOriginalFileName(), e);
            throw new RuntimeException("Failed to read documents: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isSupported(String fileExtension) {
        return StringUtils.hasText(fileExtension) &&
               readerFactory.isSupported(fileExtension);
    }

    @Override
    public List<String> getSupportedExtensions() {
        return readerFactory.getSupportedExtensions();
    }

    @Override
    public String cleanContent(String content) {
        if (!StringUtils.hasText(content)) {
            return content;
        }

        // 去除多余的空格和换行
        String cleaned = MULTIPLE_SPACES.matcher(content).replaceAll(" ");
        cleaned = MULTIPLE_NEWLINES.matcher(cleaned).replaceAll("\n\n");

        // 去除首尾空白
        cleaned = cleaned.trim();

        return cleaned;
    }

    /**
     * 获取文档读取配置信息
     */
    public Map<String, Object> getReaderInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("supported_extensions", getSupportedExtensions());
        info.put("readers", readerFactory.getReadersInfo());
        info.put("total_supported_types", getSupportedExtensions().size());
        return info;
    }

    /**
     * 根据文件类型创建优化的读取配置
     */
    public ReaderConfig createOptimizedConfig(String fileExtension) {
        String ext = fileExtension.toLowerCase();

        switch (ext) {
            case "pdf":
                return ReaderConfig.pdfConfig()
                                   .extractTables(true)
                                   .extractImages(false)
                                   .pdfPageLimit(1000)
                                   .build();

            case "txt":
            case "md":
                return ReaderConfig.textConfig()
                                   .preserveFormatting(ext.equals("md"))
                                   .chunkSize(1000)
                                   .enableChunking(true)
                                   .build();

            case "json":
                return ReaderConfig.jsonConfig()
                                   .flattenJson(true)
                                   .jsonDepthLimit(5)
                                   .build();

            case "doc":
            case "docx":
            case "ppt":
            case "pptx":
            case "xls":
            case "xlsx":
                return ReaderConfig.officeConfig()
                                   .extractFormulas(true)
                                   .extractComments(false)
                                   .parseEmbeddedDocuments(false)
                                   .build();

            default:
                return ReaderConfig.defaultConfig();
        }
    }

    /**
     * 使用优化配置读取文档
     */
    public List<Document> readDocumentsWithOptimizedConfig(FileInfo fileInfo) {
        ReaderConfig config = createOptimizedConfig(fileInfo.getFileExtension());
        return readDocuments(fileInfo, config);
    }

    /**
     * 丰富文档元数据
     */
    private Document enrichDocumentMetadata(Document document, FileInfo fileInfo, ReaderConfig config) {
        Map<String, Object> metadata = new HashMap<>(document.getMetadata());

        // 添加文件基本信息
        metadata.put("file_id", fileInfo.getFileId());
        metadata.put("original_filename", fileInfo.getOriginalFileName());
        metadata.put("file_extension", fileInfo.getFileExtension());
        metadata.put("content_type", fileInfo.getContentType());
        metadata.put("file_size", fileInfo.getFileSize());
        metadata.put("upload_time", fileInfo.getUploadTime());
        metadata.put("upload_user_id", fileInfo.getUploadUserId());
        metadata.put("storage_type", fileInfo.getStorageType().getCode());

        // 添加自定义标签和元数据
        if (StringUtils.hasText(fileInfo.getTags())) {
            metadata.put("tags", fileInfo.getTags());
        }

        if (fileInfo.getMetadata() != null && !fileInfo.getMetadata().isEmpty()) {
            metadata.put("custom_metadata", fileInfo.getMetadata());
        }

        // 添加文档长度信息
        String content = document.getText();
        if (StringUtils.hasText(content)) {
            metadata.put("content_length", content.length());
            metadata.put("word_count", content.split("\\s+").length);
        }

        // 添加读取配置信息
        metadata.put("reader_config", Map.of(
                "chunk_size", config.getChunkSize(),
                "chunk_overlap", config.getChunkOverlap(),
                "enable_chunking", config.isEnableChunking(),
                "language", config.getLanguage(),
                "charset", config.getCharset().name()
        ));

        return Document.builder()
                       .id(document.getId())
                       .text(content)
                       .metadata(metadata)
                       .build();
    }
} 