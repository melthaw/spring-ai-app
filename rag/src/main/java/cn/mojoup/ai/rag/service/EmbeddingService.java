package cn.mojoup.ai.rag.service;

import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

/**
 * 嵌入服务接口
 * 负责将文档内容转换为向量嵌入
 *
 * @author matt
 */
public interface EmbeddingService {

    /**
     * 对单个文档进行嵌入
     *
     * @param document 文档
     * @param model 嵌入模型
     * @return 嵌入向量
     */
    List<Double> embedDocument(Document document, String model);

    /**
     * 对多个文档进行批量嵌入
     *
     * @param documents 文档列表
     * @param model 嵌入模型
     * @return 嵌入向量列表
     */
    List<List<Double>> embedDocuments(List<Document> documents, String model);

    /**
     * 对文本进行嵌入
     *
     * @param text 文本内容
     * @param model 嵌入模型
     * @return 嵌入向量
     */
    List<Double> embedText(String text, String model);

    /**
     * 对多个文本进行批量嵌入
     *
     * @param texts 文本列表
     * @param model 嵌入模型
     * @return 嵌入向量列表
     */
    List<List<Double>> embedTexts(List<String> texts, String model);

    /**
     * 获取嵌入模型的向量维度
     *
     * @param model 嵌入模型
     * @return 向量维度
     */
    int getEmbeddingDimension(String model);

    /**
     * 获取支持的嵌入模型列表
     *
     * @return 模型列表
     */
    List<String> getSupportedModels();

    /**
     * 检查模型是否支持
     *
     * @param model 模型名称
     * @return 是否支持
     */
    boolean isModelSupported(String model);

    /**
     * 获取模型信息
     *
     * @param model 模型名称
     * @return 模型信息
     */
    Map<String, Object> getModelInfo(String model);
} 