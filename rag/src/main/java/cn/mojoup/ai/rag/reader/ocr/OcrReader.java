package cn.mojoup.ai.rag.reader.ocr;

import cn.mojoup.ai.rag.reader.ocr.config.OcrConfig;
import cn.mojoup.ai.rag.reader.ocr.model.OcrResult;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

/**
 * OCR读取器接口
 * 定义统一的OCR处理规范
 *
 * @author matt
 */
public interface OcrReader {

    /**
     * 识别图片中的文字
     *
     * @param resource 图片资源
     * @param config OCR配置
     * @return 识别结果
     * @throws IOException 如果读取图片失败
     */
    OcrResult recognize(Resource resource, OcrConfig config) throws IOException;

    /**
     * 检查是否支持指定的MIME类型
     *
     * @param mimeType MIME类型
     * @return 是否支持
     */
    boolean supports(String mimeType);

    /**
     * 获取支持的MIME类型列表
     *
     * @return 支持的MIME类型列表
     */
    List<String> getSupportedMimeTypes();

    /**
     * 获取OCR引擎类型
     *
     * @return OCR引擎类型
     */
    String getEngineType();
} 