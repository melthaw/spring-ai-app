package cn.mojoup.ai.rag.reader.ocr.impl;

import cn.mojoup.ai.rag.reader.ocr.config.OcrConfig;
import cn.mojoup.ai.rag.reader.ocr.config.PaddleOcrConfig;
import cn.mojoup.ai.rag.reader.ocr.OcrReader;
import cn.mojoup.ai.rag.reader.ocr.model.OcrResult;
import cn.mojoup.ai.rag.reader.ocr.model.TextBlock;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * PaddleOCR实现
 * 通过HTTP调用PaddleOCR-Serving的官方API
 * API文档: https://github.com/PaddlePaddle/PaddleOCR/blob/release/2.7/deploy/pdserving/README_CN.md
 *
 * @author matt
 */
@Slf4j
@Component
public class PaddleOcrReader implements OcrReader {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PaddleOcrConfig paddleConfig;

    public PaddleOcrReader(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.paddleConfig = PaddleOcrConfig.defaultConfig();
    }

    @Override
    public OcrResult recognize(Resource resource, OcrConfig config) {
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount <= paddleConfig.getRetryCount()) {
            try {
                return doRecognize(resource, config);
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                if (retryCount <= paddleConfig.getRetryCount()) {
                    log.warn("PaddleOCR识别失败，正在进行第{}次重试: {}", retryCount, e.getMessage());
                    try {
                        TimeUnit.MILLISECONDS.sleep(paddleConfig.getRetryInterval());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                }
            }
        }

        log.error("PaddleOCR识别失败，已重试{}次: {}", paddleConfig.getRetryCount(), lastException.getMessage());
        throw new RuntimeException("PaddleOCR识别失败", lastException);
    }

    private OcrResult doRecognize(Resource resource, OcrConfig config) throws IOException {
        // 准备请求数据
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // PaddleOCR-Serving的请求格式
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("images", List.of(Base64.getEncoder().encodeToString(resource.getInputStream().readAllBytes())));
        requestBody.put("det", paddleConfig.isEnableDetection());
        requestBody.put("rec", paddleConfig.isEnableRecognition());
        requestBody.put("cls", paddleConfig.isEnableClassification());
        requestBody.put("lang", config.getLanguage());

        // 添加模型配置
        Map<String, Object> modelConfig = new HashMap<>();
        modelConfig.put("det_model_dir", paddleConfig.getDetectionModel());
        modelConfig.put("rec_model_dir", paddleConfig.getRecognitionModel());
        modelConfig.put("cls_model_dir", paddleConfig.getClassificationModel());
        modelConfig.put("table_model_dir", paddleConfig.getTableModel());
        modelConfig.put("structure_model_dir", paddleConfig.getStructureModel());

        // 添加阈值配置
        Map<String, Object> thresholdConfig = new HashMap<>();
        thresholdConfig.put("det_db_thresh", paddleConfig.getDetectionThreshold());
        thresholdConfig.put("det_db_box_thresh", paddleConfig.getDetectionThreshold());
        thresholdConfig.put("rec_thresh", paddleConfig.getRecognitionThreshold());
        thresholdConfig.put("cls_thresh", paddleConfig.getClassificationThreshold());
        thresholdConfig.put("table_thresh", paddleConfig.getTableThreshold());
        thresholdConfig.put("structure_thresh", paddleConfig.getStructureThreshold());

        requestBody.put("model_config", modelConfig);
        requestBody.put("threshold_config", thresholdConfig);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // 发送请求
        ResponseEntity<String> response = restTemplate.postForEntity(
            paddleConfig.getServiceUrl(),
            requestEntity,
            String.class
        );

        // 解析响应
        return parseResponse(response.getBody(), config);
    }

    @Override
    public boolean supports(String mimeType) {
        return mimeType != null && (
            mimeType.startsWith("image/") ||
            mimeType.equals("application/pdf")
        );
    }

    @Override
    public List<String> getSupportedMimeTypes() {
        return List.of(
            "image/jpeg",
            "image/png",
            "image/bmp",
            "image/gif",
            "image/webp",
            "application/pdf"
        );
    }

    @Override
    public String getEngineType() {
        return "paddle";
    }

    /**
     * 解析PaddleOCR-Serving的响应
     * 响应格式: https://github.com/PaddlePaddle/PaddleOCR/blob/release/2.7/deploy/pdserving/README_CN.md#%E8%AF%B7%E6%B1%82%E5%92%8C%E5%93%8D%E5%BA%94%E6%A0%BC%E5%BC%8F
     */
    private OcrResult parseResponse(String response, OcrConfig config) throws IOException {
        Map<String, Object> json = objectMapper.readValue(response, Map.class);
        OcrResult result = new OcrResult();
        
        // 获取第一个图片的识别结果
        List<Map<String, Object>> results = (List<Map<String, Object>>) json.get("results");
        if (results == null || results.isEmpty()) {
            return result;
        }
        
        Map<String, Object> firstResult = results.get(0);
        List<Map<String, Object>> data = (List<Map<String, Object>>) firstResult.get("data");
        
        // 设置基本信息
        StringBuilder fullText = new StringBuilder();
        List<TextBlock> textBlocks = new ArrayList<>();
        
        for (Map<String, Object> item : data) {
            String text = (String) item.get("text");
            List<List<Double>> points = (List<List<Double>>) item.get("points");
            Double confidence = ((Number) item.get("confidence")).doubleValue();
            
            // 计算边界框
            double x = points.get(0).get(0);
            double y = points.get(0).get(1);
            double width = points.get(1).get(0) - x;
            double height = points.get(2).get(1) - y;
            
            // 创建文本块
            TextBlock textBlock = new TextBlock();
            textBlock.setText(text);
            textBlock.setConfidence(confidence);
            textBlock.setX(x);
            textBlock.setY(y);
            textBlock.setWidth(width);
            textBlock.setHeight(height);
            textBlock.setOrientation("horizontal");  // PaddleOCR默认水平文本
            
            textBlocks.add(textBlock);
            fullText.append(text).append("\n");
        }
        
        result.setText(fullText.toString().trim());
        result.setTextBlocks(textBlocks);
        result.setLanguage(config.getLanguage());
        result.setConfidence(textBlocks.stream()
            .mapToDouble(TextBlock::getConfidence)
            .average()
            .orElse(0.0));
        
        // 添加元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("engine", "paddle");
        metadata.put("serviceUrl", paddleConfig.getServiceUrl());
        metadata.put("detectionModel", paddleConfig.getDetectionModel());
        metadata.put("recognitionModel", paddleConfig.getRecognitionModel());
        metadata.put("classificationModel", paddleConfig.getClassificationModel());
        result.setMetadata(metadata);
        
        return result;
    }
} 