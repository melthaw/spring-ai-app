package cn.mojoup.ai.rag.reader.ocr.model;

import lombok.Data;

@Data
public class TableCell {
    /**
     * 单元格文本内容
     */
    private String text;
    
    /**
     * 单元格识别置信度
     */
    private double confidence;
    
    /**
     * 跨行数
     */
    private int rowSpan = 1;
    
    /**
     * 跨列数
     */
    private int colSpan = 1;
    
    /**
     * 是否为表头
     */
    private boolean header;
    
    /**
     * 单元格左上角x坐标
     */
    private double x;
    
    /**
     * 单元格左上角y坐标
     */
    private double y;
    
    /**
     * 单元格宽度
     */
    private double width;
    
    /**
     * 单元格高度
     */
    private double height;
} 