package cn.mojoup.ai.rag.reader;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 文档读取配置
 * 包含各种读取参数设置
 *
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReaderConfig {

    // 静态工厂方法
    public static ReaderConfig defaultConfig() {
        return new ReaderConfig();
    }

    public static ReaderConfigBuilder pdfConfig() {
        return ReaderConfig.builder()
                           .extractTables(true)
                           .extractImages(false)
                           .pdfPageLimit(1000);
    }

    public static ReaderConfigBuilder textConfig() {
        return ReaderConfig.builder()
                           .preserveFormatting(false)
                           .chunkSize(1000);
    }

    public static ReaderConfigBuilder jsonConfig() {
        return ReaderConfig.builder()
                           .flattenJson(true)
                           .jsonDepthLimit(5);
    }

    public static ReaderConfigBuilder officeConfig() {
        return ReaderConfig.builder()
                           .extractFormulas(true)
                           .extractComments(false)
                           .parseEmbeddedDocuments(false);
    }

    // 通用配置
    private Charset charset = StandardCharsets.UTF_8;
    private boolean extractMetadata = true;
    private int maxContentLength = 1024 * 1024; // 1MB
    private String language = "zh";

    // 文档结构配置
    private boolean readByParagraph = false;  // 是否按段落读取
    private boolean readByPage = false;       // 是否按页读取
    private boolean mergeCrossPageParagraphs = false;  // 是否合并跨页段落
    private boolean mergeCrossPageTables = false;      // 是否合并跨页表格
    private boolean detectAndMergeTables = false;      // 是否检测并合并表格
    private boolean performOcrOnImages = false;        // 是否对图片进行OCR识别
    private boolean extractImageMetadata = false;      // 是否提取图片元数据
    private boolean preserveTableStructure = false;    // 是否保持表格结构
    private boolean extractTableHeaders = false;       // 是否提取表格标题
    private boolean extractTableFooters = false;       // 是否提取表格页脚
    private boolean extractTableCaptions = false;      // 是否提取表格说明文字

    // PDF特定配置
    private boolean extractImages = false;
    private boolean extractTables = true;
    private boolean extractAnnotations = false;
    private int pdfPageLimit = 1000;

    // 文本文件配置
    private boolean preserveFormatting = false;
    private String lineEnding = "\n";

    // JSON配置
    private String jsonPath = "$";
    private boolean flattenJson = false;
    private int jsonDepthLimit = 10;

    // Tika配置（用于Office文档）
    private boolean extractFormulas = true;
    private boolean extractComments = false;
    private boolean extractHiddenText = false;
    private boolean parseEmbeddedDocuments = false;

    // 分块配置
    private int chunkSize = 500;
    private int chunkOverlap = 50;
    private boolean enableChunking = true;

    // 清理配置
    private boolean removeWhitespace = true;
    private boolean removeEmptyLines = true;
    private boolean normalizeUnicode = true;

    // 自定义属性
    private Map<String, Object> customProperties = new HashMap<>();

    public void setCustomProperty(String key, Object value) {
        this.customProperties.put(key, value);
    }

    public Object getCustomProperty(String key) {
        return this.customProperties.get(key);
    }

    public Object getCustomProperty(String key, Object defaultValue) {
        return this.customProperties.getOrDefault(key, defaultValue);
    }

} 