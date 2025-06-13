package cn.mojoup.ai.rag.reader.ocr.config;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * PaddleOCR-Serving特定配置
 * 参考: https://github.com/PaddlePaddle/PaddleOCR/blob/release/2.7/deploy/pdserving/README_CN.md
 */
@Data
@Accessors(chain = true)
public class PaddleOcrConfig {
    /**
     * 服务URL
     */
    private String serviceUrl = "http://localhost:9292/ocr/prediction";

    /**
     * 是否启用文本检测
     */
    private boolean enableDetection = true;

    /**
     * 是否启用文本识别
     */
    private boolean enableRecognition = true;

    /**
     * 是否启用方向分类
     */
    private boolean enableClassification = true;

    /**
     * 是否启用表格识别
     */
    private boolean enableTableRecognition = false;

    /**
     * 是否启用结构化信息提取
     */
    private boolean enableStructureRecognition = false;

    /**
     * 检测模型名称
     */
    private String detectionModel = "ch_PP-OCRv4_det_infer";

    /**
     * 识别模型名称
     */
    private String recognitionModel = "ch_PP-OCRv4_rec_infer";

    /**
     * 方向分类模型名称
     */
    private String classificationModel = "ch_ppocr_mobile_v2.0_cls_infer";

    /**
     * 表格识别模型名称
     */
    private String tableModel = "ch_ppstructure_mobile_v2.0_SLANet_infer";

    /**
     * 结构化信息提取模型名称
     */
    private String structureModel = "ch_ppstructure_mobile_v2.0_SLANet_infer";

    /**
     * 检测阈值
     */
    private double detectionThreshold = 0.3;

    /**
     * 识别阈值
     */
    private double recognitionThreshold = 0.5;

    /**
     * 分类阈值
     */
    private double classificationThreshold = 0.9;

    /**
     * 表格识别阈值
     */
    private double tableThreshold = 0.5;

    /**
     * 结构化信息提取阈值
     */
    private double structureThreshold = 0.5;

    /**
     * 最大批处理大小
     */
    private int maxBatchSize = 1;

    /**
     * 超时时间(毫秒)
     */
    private int timeout = 30000;

    /**
     * 重试次数
     */
    private int retryCount = 3;

    /**
     * 重试间隔(毫秒)
     */
    private int retryInterval = 1000;

    /**
     * 创建默认配置
     */
    public static PaddleOcrConfig defaultConfig() {
        return new PaddleOcrConfig();
    }

    /**
     * 创建高精度配置
     */
    public static PaddleOcrConfig highAccuracyConfig() {
        return new PaddleOcrConfig()
            .setDetectionThreshold(0.5)
            .setRecognitionThreshold(0.7)
            .setClassificationThreshold(0.95)
            .setTableThreshold(0.7)
            .setStructureThreshold(0.7);
    }

    /**
     * 创建快速配置
     */
    public static PaddleOcrConfig fastConfig() {
        return new PaddleOcrConfig()
            .setDetectionThreshold(0.3)
            .setRecognitionThreshold(0.5)
            .setClassificationThreshold(0.8)
            .setTableThreshold(0.5)
            .setStructureThreshold(0.5)
            .setMaxBatchSize(4);
    }
} 