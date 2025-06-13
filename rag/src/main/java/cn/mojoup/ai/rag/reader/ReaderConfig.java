package cn.mojoup.ai.rag.reader;

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
public class ReaderConfig {

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

    public ReaderConfig() {}

    // 静态工厂方法
    public static ReaderConfig defaultConfig() {
        return new ReaderConfig();
    }

    public static ReaderConfig pdfConfig() {
        ReaderConfig config = new ReaderConfig();
        config.setExtractTables(true);
        config.setExtractImages(false);
        config.setPdfPageLimit(1000);
        return config;
    }

    public static ReaderConfig textConfig() {
        ReaderConfig config = new ReaderConfig();
        config.setPreserveFormatting(false);
        config.setChunkSize(1000);
        return config;
    }

    public static ReaderConfig jsonConfig() {
        ReaderConfig config = new ReaderConfig();
        config.setFlattenJson(true);
        config.setJsonDepthLimit(5);
        return config;
    }

    public static ReaderConfig officeConfig() {
        ReaderConfig config = new ReaderConfig();
        config.setExtractFormulas(true);
        config.setExtractComments(false);
        config.setParseEmbeddedDocuments(false);
        return config;
    }

    // Getter and Setter methods (with fluent interface)
    public Charset getCharset() { return charset; }
    public ReaderConfig setCharset(Charset charset) { this.charset = charset; return this; }

    public boolean isExtractMetadata() { return extractMetadata; }
    public ReaderConfig setExtractMetadata(boolean extractMetadata) { this.extractMetadata = extractMetadata; return this; }

    public int getMaxContentLength() { return maxContentLength; }
    public ReaderConfig setMaxContentLength(int maxContentLength) { this.maxContentLength = maxContentLength; return this; }

    public String getLanguage() { return language; }
    public ReaderConfig setLanguage(String language) { this.language = language; return this; }

    public boolean isExtractImages() { return extractImages; }
    public ReaderConfig setExtractImages(boolean extractImages) { this.extractImages = extractImages; return this; }

    public boolean isExtractTables() { return extractTables; }
    public ReaderConfig setExtractTables(boolean extractTables) { this.extractTables = extractTables; return this; }

    public boolean isExtractAnnotations() { return extractAnnotations; }
    public ReaderConfig setExtractAnnotations(boolean extractAnnotations) { this.extractAnnotations = extractAnnotations; return this; }

    public int getPdfPageLimit() { return pdfPageLimit; }
    public ReaderConfig setPdfPageLimit(int pdfPageLimit) { this.pdfPageLimit = pdfPageLimit; return this; }

    public boolean isPreserveFormatting() { return preserveFormatting; }
    public ReaderConfig setPreserveFormatting(boolean preserveFormatting) { this.preserveFormatting = preserveFormatting; return this; }

    public String getLineEnding() { return lineEnding; }
    public ReaderConfig setLineEnding(String lineEnding) { this.lineEnding = lineEnding; return this; }

    public String getJsonPath() { return jsonPath; }
    public ReaderConfig setJsonPath(String jsonPath) { this.jsonPath = jsonPath; return this; }

    public boolean isFlattenJson() { return flattenJson; }
    public ReaderConfig setFlattenJson(boolean flattenJson) { this.flattenJson = flattenJson; return this; }

    public int getJsonDepthLimit() { return jsonDepthLimit; }
    public ReaderConfig setJsonDepthLimit(int jsonDepthLimit) { this.jsonDepthLimit = jsonDepthLimit; return this; }

    public boolean isExtractFormulas() { return extractFormulas; }
    public ReaderConfig setExtractFormulas(boolean extractFormulas) { this.extractFormulas = extractFormulas; return this; }

    public boolean isExtractComments() { return extractComments; }
    public ReaderConfig setExtractComments(boolean extractComments) { this.extractComments = extractComments; return this; }

    public boolean isExtractHiddenText() { return extractHiddenText; }
    public ReaderConfig setExtractHiddenText(boolean extractHiddenText) { this.extractHiddenText = extractHiddenText; return this; }

    public boolean isParseEmbeddedDocuments() { return parseEmbeddedDocuments; }
    public ReaderConfig setParseEmbeddedDocuments(boolean parseEmbeddedDocuments) { this.parseEmbeddedDocuments = parseEmbeddedDocuments; return this; }

    public int getChunkSize() { return chunkSize; }
    public ReaderConfig setChunkSize(int chunkSize) { this.chunkSize = chunkSize; return this; }

    public int getChunkOverlap() { return chunkOverlap; }
    public ReaderConfig setChunkOverlap(int chunkOverlap) { this.chunkOverlap = chunkOverlap; return this; }

    public boolean isEnableChunking() { return enableChunking; }
    public ReaderConfig setEnableChunking(boolean enableChunking) { this.enableChunking = enableChunking; return this; }

    public boolean isRemoveWhitespace() { return removeWhitespace; }
    public ReaderConfig setRemoveWhitespace(boolean removeWhitespace) { this.removeWhitespace = removeWhitespace; return this; }

    public boolean isRemoveEmptyLines() { return removeEmptyLines; }
    public ReaderConfig setRemoveEmptyLines(boolean removeEmptyLines) { this.removeEmptyLines = removeEmptyLines; return this; }

    public boolean isNormalizeUnicode() { return normalizeUnicode; }
    public ReaderConfig setNormalizeUnicode(boolean normalizeUnicode) { this.normalizeUnicode = normalizeUnicode; return this; }

    public Map<String, Object> getCustomProperties() { return customProperties; }
    public void setCustomProperties(Map<String, Object> customProperties) { this.customProperties = customProperties; }

    public void setCustomProperty(String key, Object value) {
        this.customProperties.put(key, value);
    }

    public Object getCustomProperty(String key) {
        return this.customProperties.get(key);
    }

    public Object getCustomProperty(String key, Object defaultValue) {
        return this.customProperties.getOrDefault(key, defaultValue);
    }

    public boolean isReadByParagraph() { return readByParagraph; }
    public ReaderConfig setReadByParagraph(boolean readByParagraph) { this.readByParagraph = readByParagraph; return this; }

    public boolean isReadByPage() { return readByPage; }
    public ReaderConfig setReadByPage(boolean readByPage) { this.readByPage = readByPage; return this; }

    public boolean isMergeCrossPageParagraphs() { return mergeCrossPageParagraphs; }
    public ReaderConfig setMergeCrossPageParagraphs(boolean mergeCrossPageParagraphs) { this.mergeCrossPageParagraphs = mergeCrossPageParagraphs; return this; }

    public boolean isMergeCrossPageTables() { return mergeCrossPageTables; }
    public ReaderConfig setMergeCrossPageTables(boolean mergeCrossPageTables) { this.mergeCrossPageTables = mergeCrossPageTables; return this; }

    public boolean isDetectAndMergeTables() { return detectAndMergeTables; }
    public ReaderConfig setDetectAndMergeTables(boolean detectAndMergeTables) { this.detectAndMergeTables = detectAndMergeTables; return this; }

    public boolean isPerformOcrOnImages() { return performOcrOnImages; }
    public ReaderConfig setPerformOcrOnImages(boolean performOcrOnImages) { this.performOcrOnImages = performOcrOnImages; return this; }

    public boolean isExtractImageMetadata() { return extractImageMetadata; }
    public ReaderConfig setExtractImageMetadata(boolean extractImageMetadata) { this.extractImageMetadata = extractImageMetadata; return this; }

    public boolean isPreserveTableStructure() { return preserveTableStructure; }
    public ReaderConfig setPreserveTableStructure(boolean preserveTableStructure) { this.preserveTableStructure = preserveTableStructure; return this; }

    public boolean isExtractTableHeaders() { return extractTableHeaders; }
    public ReaderConfig setExtractTableHeaders(boolean extractTableHeaders) { this.extractTableHeaders = extractTableHeaders; return this; }

    public boolean isExtractTableFooters() { return extractTableFooters; }
    public ReaderConfig setExtractTableFooters(boolean extractTableFooters) { this.extractTableFooters = extractTableFooters; return this; }

    public boolean isExtractTableCaptions() { return extractTableCaptions; }
    public ReaderConfig setExtractTableCaptions(boolean extractTableCaptions) { this.extractTableCaptions = extractTableCaptions; return this; }
} 