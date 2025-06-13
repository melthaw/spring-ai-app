package cn.mojoup.ai.rag.reader.ocr.config;

import lombok.Data;
import lombok.experimental.Accessors;
import java.util.HashMap;
import java.util.Map;

/**
 * OCR配置类
 * 控制OCR识别的行为
 *
 * @author matt
 */
@Data
@Accessors(chain = true)
public class OcrConfig {
    /**
     * 识别语言
     */
    private String language = "ch";
    
    /**
     * 是否启用表格识别
     */
    private boolean enableTableRecognition = false;
    
    /**
     * 表格识别配置
     */
    private TableConfig tableConfig = new TableConfig();
    
    /**
     * 图像预处理配置
     */
    private PreprocessConfig preprocessConfig = new PreprocessConfig();
    
    /**
     * 引擎特定配置
     */
    private Map<String, Object> engineConfig = new HashMap<>();
    
    /**
     * 创建默认配置
     */
    public static OcrConfig defaultConfig() {
        return new OcrConfig();
    }
    
    /**
     * 创建高精度配置
     */
    public static OcrConfig highAccuracyConfig() {
        return new OcrConfig()
            .setPreprocessConfig(new PreprocessConfig()
                .setEnableDenoising(true)
                .setEnableDeskewing(true)
                .setEnableContrastEnhancement(true))
            .setEnableTableRecognition(true)
            .setTableConfig(new TableConfig()
                .setDetectTableLines(true)
                .setMergeCells(true)
                .setPreserveStructure(true));
    }
    
    /**
     * 创建快速配置
     */
    public static OcrConfig fastConfig() {
        return new OcrConfig()
            .setPreprocessConfig(new PreprocessConfig()
                .setEnableDenoising(false)
                .setEnableDeskewing(false)
                .setEnableContrastEnhancement(false))
            .setEnableTableRecognition(false);
    }
    
    /**
     * 表格识别配置
     */
    @Data
    @Accessors(chain = true)
    public static class TableConfig {
        /**
         * 是否检测表格线
         */
        private boolean detectTableLines = false;
        
        /**
         * 是否合并单元格
         */
        private boolean mergeCells = false;
        
        /**
         * 是否保持表格结构
         */
        private boolean preserveStructure = true;
    }
    
    /**
     * 图像预处理配置
     */
    @Data
    @Accessors(chain = true)
    public static class PreprocessConfig {
        /**
         * 是否启用降噪
         */
        private boolean enableDenoising = false;
        
        /**
         * 是否启用倾斜校正
         */
        private boolean enableDeskewing = false;
        
        /**
         * 是否启用对比度增强
         */
        private boolean enableContrastEnhancement = false;
    }
} 