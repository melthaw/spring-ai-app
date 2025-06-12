package cn.mojoup.ai.rag.reader;

import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * 文档读取器接口
 * 定义统一的文档读取规范
 *
 * @author matt
 */
public interface DocumentReader {

    /**
     * 读取文档
     *
     * @param resource 文档资源
     * @param config 读取配置
     * @return 文档列表
     */
    List<Document> read(Resource resource, ReaderConfig config);

    /**
     * 检查是否支持该文件类型
     *
     * @param extension 文件扩展名
     * @return 是否支持
     */
    boolean supports(String extension);

    /**
     * 获取支持的文件扩展名列表
     *
     * @return 支持的扩展名
     */
    List<String> getSupportedExtensions();

    /**
     * 获取读取器类型名称
     *
     * @return 类型名称
     */
    String getReaderType();
} 