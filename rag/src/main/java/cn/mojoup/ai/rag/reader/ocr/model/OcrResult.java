package cn.mojoup.ai.rag.reader.ocr.model;

import lombok.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OCR识别结果
 * 包含识别的文本、表格和元数据信息
 *
 * @author matt
 */
@Data
public class OcrResult {
    /**
     * 完整文本内容
     */
    private String text;
    
    /**
     * 检测到的语言
     */
    private String language;
    
    /**
     * 整体置信度
     */
    private double confidence;
    
    /**
     * 文本块列表
     */
    private List<TextBlock> textBlocks;
    
    /**
     * 表格列表
     */
    private List<Table> tables;
    
    /**
     * 其他元数据
     */
    private Map<String, Object> metadata = new HashMap<>();
} 