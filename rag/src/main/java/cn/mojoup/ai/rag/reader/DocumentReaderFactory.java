package cn.mojoup.ai.rag.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 文档读取器工厂
 * 管理和分发不同类型的文档读取器
 *
 * @author matt
 */
@Component
public class DocumentReaderFactory {

    private static final Logger logger = LoggerFactory.getLogger(DocumentReaderFactory.class);

    private final Map<String, DocumentReader> readerMap = new HashMap<>();

    @Autowired
    public DocumentReaderFactory(List<DocumentReader> readers) {
        // 注册所有的读取器
        for (DocumentReader reader : readers) {
            for (String extension : reader.getSupportedExtensions()) {
                readerMap.put(extension.toLowerCase(), reader);
                logger.debug("Registered reader {} for extension: {}", reader.getReaderType(), extension);
            }
        }
        logger.info("Initialized DocumentReaderFactory with {} readers supporting {} extensions", 
            readers.size(), readerMap.size());
    }

    /**
     * 根据文件扩展名获取合适的读取器
     *
     * @param extension 文件扩展名
     * @return 文档读取器
     */
    public Optional<DocumentReader> getReader(String extension) {
        if (extension == null) {
            return Optional.empty();
        }
        
        String normalizedExtension = extension.toLowerCase();
        if (normalizedExtension.startsWith(".")) {
            normalizedExtension = normalizedExtension.substring(1);
        }
        
        DocumentReader reader = readerMap.get(normalizedExtension);
        if (reader != null) {
            logger.debug("Found reader {} for extension: {}", reader.getReaderType(), extension);
        } else {
            logger.warn("No reader found for extension: {}", extension);
        }
        
        return Optional.ofNullable(reader);
    }

    /**
     * 检查是否支持指定的文件扩展名
     *
     * @param extension 文件扩展名
     * @return 是否支持
     */
    public boolean isSupported(String extension) {
        return getReader(extension).isPresent();
    }

    /**
     * 获取所有支持的文件扩展名
     *
     * @return 支持的扩展名列表
     */
    public List<String> getSupportedExtensions() {
        return readerMap.keySet().stream().sorted().collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取所有注册的读取器信息
     *
     * @return 读取器信息映射
     */
    public Map<String, String> getReadersInfo() {
        Map<String, String> info = new HashMap<>();
        readerMap.forEach((extension, reader) -> 
            info.put(extension, reader.getReaderType()));
        return info;
    }

    /**
     * 根据文件名获取合适的读取器
     *
     * @param filename 文件名
     * @return 文档读取器
     */
    public Optional<DocumentReader> getReaderByFilename(String filename) {
        if (filename == null) {
            return Optional.empty();
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex <= 0) {
            logger.warn("No file extension found in filename: {}", filename);
            return Optional.empty();
        }
        
        String extension = filename.substring(lastDotIndex + 1);
        return getReader(extension);
    }

    /**
     * 获取默认配置的读取器（用于未知类型）
     *
     * @return 默认读取器
     */
    public Optional<DocumentReader> getDefaultReader() {
        // 尝试返回文本读取器作为默认
        return getReader("txt");
    }
} 