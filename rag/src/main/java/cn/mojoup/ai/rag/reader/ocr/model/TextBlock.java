package cn.mojoup.ai.rag.reader.ocr.model;

import lombok.Data;

@Data
public class TextBlock {
    /**
     * 文本内容
     */
    private String text;
    
    /**
     * 识别置信度
     */
    private double confidence;
    
    /**
     * 文本块左上角x坐标
     */
    private double x;
    
    /**
     * 文本块左上角y坐标
     */
    private double y;
    
    /**
     * 文本块宽度
     */
    private double width;
    
    /**
     * 文本块高度
     */
    private double height;
    
    /**
     * 文本方向
     */
    private String orientation;
} 