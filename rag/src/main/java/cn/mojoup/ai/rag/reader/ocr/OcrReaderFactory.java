package cn.mojoup.ai.rag.reader.ocr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * OCR读取器工厂
 * 管理不同的OCR实现,根据配置选择合适的OCR引擎
 *
 * @author matt
 */
@Slf4j
@Component
public class OcrReaderFactory {

    @Value("${rag.ocr.default-engine:paddle}")
    private String defaultEngine;

    @Autowired
    private Map<String, OcrReader> ocrReaders;

    /**
     * 获取OCR读取器
     *
     * @param engineType 引擎类型
     * @return OCR读取器
     */
    public OcrReader getReader(String engineType) {
        String type = Optional.ofNullable(engineType).orElse(defaultEngine);
        OcrReader reader = ocrReaders.get(type);
        
        if (reader == null) {
            log.warn("未找到OCR引擎: {}, 使用默认引擎: {}", type, defaultEngine);
            reader = ocrReaders.get(defaultEngine);
            
            if (reader == null) {
                throw new IllegalStateException("未找到可用的OCR引擎");
            }
        }
        
        return reader;
    }

    /**
     * 获取默认OCR读取器
     *
     * @return OCR读取器
     */
    public OcrReader getDefaultReader() {
        return getReader(defaultEngine);
    }

    /**
     * 获取所有可用的OCR引擎类型
     *
     * @return 引擎类型列表
     */
    public List<String> getAvailableEngines() {
        return List.copyOf(ocrReaders.keySet());
    }

    /**
     * 检查引擎是否可用
     *
     * @param engineType 引擎类型
     * @return 是否可用
     */
    public boolean isEngineAvailable(String engineType) {
        return ocrReaders.containsKey(engineType);
    }
} 