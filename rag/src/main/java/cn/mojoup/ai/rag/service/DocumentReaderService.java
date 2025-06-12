package cn.mojoup.ai.rag.service;

import cn.mojoup.ai.upload.domain.FileInfo;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * 文档读取服务接口
 * 负责从不同类型的文件中读取文档内容
 *
 * @author matt
 */
public interface DocumentReaderService {

    /**
     * 根据FileInfo读取文档
     *
     * @param fileInfo 文件信息
     * @return 文档列表
     */
    List<Document> readDocuments(FileInfo fileInfo);

    /**
     * 根据资源读取文档
     *
     * @param resource 文件资源
     * @param fileInfo 文件信息（用于获取元数据）
     * @return 文档列表
     */
    List<Document> readDocuments(Resource resource, FileInfo fileInfo);

    /**
     * 检查文件类型是否支持
     *
     * @param fileExtension 文件扩展名
     * @return 是否支持
     */
    boolean isSupported(String fileExtension);

    /**
     * 获取支持的文件类型列表
     *
     * @return 支持的文件扩展名列表
     */
    List<String> getSupportedExtensions();

    /**
     * 清理文档内容
     *
     * @param content 原始内容
     * @return 清理后的内容
     */
    String cleanContent(String content);
} 