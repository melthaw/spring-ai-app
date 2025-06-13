package cn.mojoup.ai.rag.reader.ocr;

import cn.mojoup.ai.rag.reader.ocr.model.OcrResult;
import cn.mojoup.ai.rag.reader.ocr.model.TextBlock;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OCR结果转换器
 * 将OCR识别结果转换为Spring AI Document
 *
 * @author matt
 */
public class OcrDocumentConverter {

    /**
     * 将OCR结果转换为Spring AI Document
     *
     * @param ocrResult OCR识别结果
     * @param resource  原始资源
     * @return Spring AI Document
     */
    public static Document convertToDocument(OcrResult ocrResult, Resource resource) {
        Map<String, Object> metadata = new HashMap<>();

        // 基础元数据
        metadata.put("source", resource.getFilename());
        metadata.put("documentType", "ocr");
        metadata.put("language", ocrResult.getLanguage());
        metadata.put("confidence", ocrResult.getConfidence());

        // 添加文本块信息
        List<Map<String, Object>> textBlocks = new ArrayList<>();
        for (TextBlock block : ocrResult.getTextBlocks()) {
            Map<String, Object> blockInfo = new HashMap<>();
            blockInfo.put("text", block.getText());
            blockInfo.put("confidence", block.getConfidence());
            blockInfo.put("orientation", block.getOrientation());

            // 添加边界框信息
//            if (block.getBoundingBox() != null) {
//                Map<String, Integer> bbox = new HashMap<>();
//                bbox.put("x", block.getBoundingBox().getX());
//                bbox.put("y", block.getBoundingBox().getY());
//                bbox.put("width", block.getBoundingBox().getWidth());
//                bbox.put("height", block.getBoundingBox().getHeight());
//                blockInfo.put("boundingBox", bbox);
//            }

            textBlocks.add(blockInfo);
        }
        metadata.put("textBlocks", textBlocks);

        // 添加其他元数据
        metadata.putAll(ocrResult.getMetadata());

        // 创建Document对象
        return new Document(ocrResult.getText(), metadata);
    }

    /**
     * 批量转换OCR结果
     *
     * @param ocrResults OCR结果列表
     * @param resource   原始资源
     * @return Document列表
     */
    public static List<Document> convertToDocuments(List<OcrResult> ocrResults, Resource resource) {
        List<Document> documents = new ArrayList<>();
        for (OcrResult result : ocrResults) {
            documents.add(convertToDocument(result, resource));
        }
        return documents;
    }
} 