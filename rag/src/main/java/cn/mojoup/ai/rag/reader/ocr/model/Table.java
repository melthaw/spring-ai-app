package cn.mojoup.ai.rag.reader.ocr.model;

import lombok.Data;
import java.util.List;

@Data
public class Table {
    /**
     * 表格类型(regular/irregular)
     */
    private String type;
    
    /**
     * 表格识别置信度
     */
    private double confidence;
    
    /**
     * 表格左上角x坐标
     */
    private double x;
    
    /**
     * 表格左上角y坐标
     */
    private double y;
    
    /**
     * 表格宽度
     */
    private double width;
    
    /**
     * 表格高度
     */
    private double height;
    
    /**
     * 表格行列表
     */
    private List<TableRow> rows;
} 