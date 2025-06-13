package cn.mojoup.ai.rag.reader.ocr.impl;

import cn.mojoup.ai.rag.reader.ocr.config.OcrConfig;
import cn.mojoup.ai.rag.reader.ocr.OcrReader;
import cn.mojoup.ai.rag.reader.ocr.model.OcrResult;
import cn.mojoup.ai.rag.reader.ocr.model.TextBlock;
import cn.mojoup.ai.rag.reader.ocr.model.Table;
import cn.mojoup.ai.rag.reader.ocr.model.TableRow;
import cn.mojoup.ai.rag.reader.ocr.model.TableCell;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LLM OCR实现
 * 使用大语言模型的视觉能力进行文字识别
 *
 * @author matt
 */
@Slf4j
@Component
public class LlmOcrReader implements OcrReader {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final PromptTemplate systemPromptTemplate;
    private final PromptTemplate userPromptTemplate;

    public LlmOcrReader(ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
        this.systemPromptTemplate = new PromptTemplate("classpath:prompts/ocr_system.txt");
        this.userPromptTemplate = new PromptTemplate("classpath:prompts/ocr_user.txt");
    }

    @Override
    public OcrResult recognize(Resource resource, OcrConfig config) throws IOException {
        // 准备提示词
        String systemPrompt = systemPromptTemplate.render(Map.of());
        String userPrompt = userPromptTemplate.render(Map.of(
            "language", config.getLanguage(),
            "enableTableRecognition", config.isEnableTableRecognition()
        ));

        // 准备图片数据
        String imageBase64 = Base64.getEncoder().encodeToString(resource.getInputStream().readAllBytes());
        String imageData = String.format("data:image/jpeg;base64,%s", imageBase64);

        // 构建消息
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(systemPrompt));
        messages.add(new UserMessage(userPrompt + "\n\n图片数据:\n" + imageData));

        // 调用LLM
        ChatResponse response = chatClient.call(new Prompt(messages));
        String result = response.getResult().getOutput().getContent();

        // 解析结果
        return parseResult(result);
    }

    private OcrResult parseResult(String result) throws IOException {
        JsonNode root = objectMapper.readTree(result);
        OcrResult ocrResult = new OcrResult();
        
        // 解析基本信息
        ocrResult.setText(root.path("text").asText());
        ocrResult.setLanguage(root.path("language").asText());
        ocrResult.setConfidence(root.path("confidence").asDouble());

        // 解析文本块
        List<TextBlock> textBlocks = new ArrayList<>();
        root.path("textBlocks").forEach(block -> {
            TextBlock textBlock = new TextBlock();
            textBlock.setText(block.path("text").asText());
            textBlock.setConfidence(block.path("confidence").asDouble());
            
            JsonNode bbox = block.path("boundingBox");
            if (!bbox.isMissingNode()) {
                textBlock.setX(bbox.path("x").asDouble());
                textBlock.setY(bbox.path("y").asDouble());
                textBlock.setWidth(bbox.path("width").asDouble());
                textBlock.setHeight(bbox.path("height").asDouble());
            }
            
            textBlock.setOrientation(block.path("orientation").asText());
            textBlocks.add(textBlock);
        });
        ocrResult.setTextBlocks(textBlocks);

        // 解析表格
        List<Table> tables = new ArrayList<>();
        root.path("tables").forEach(tableNode -> {
            Table table = new Table();
            table.setType(tableNode.path("type").asText());
            table.setConfidence(tableNode.path("confidence").asDouble());
            
            JsonNode bbox = tableNode.path("boundingBox");
            if (!bbox.isMissingNode()) {
                table.setX(bbox.path("x").asDouble());
                table.setY(bbox.path("y").asDouble());
                table.setWidth(bbox.path("width").asDouble());
                table.setHeight(bbox.path("height").asDouble());
            }
            
            List<TableRow> rows = new ArrayList<>();
            tableNode.path("rows").forEach(rowNode -> {
                TableRow row = new TableRow();
                List<TableCell> cells = new ArrayList<>();
                
                rowNode.path("cells").forEach(cellNode -> {
                    TableCell cell = new TableCell();
                    cell.setText(cellNode.path("text").asText());
                    cell.setConfidence(cellNode.path("confidence").asDouble());
                    cell.setRowSpan(cellNode.path("rowSpan").asInt());
                    cell.setColSpan(cellNode.path("colSpan").asInt());
                    cell.setHeader(cellNode.path("header").asBoolean());
                    
                    JsonNode cellBbox = cellNode.path("boundingBox");
                    if (!cellBbox.isMissingNode()) {
                        cell.setX(cellBbox.path("x").asDouble());
                        cell.setY(cellBbox.path("y").asDouble());
                        cell.setWidth(cellBbox.path("width").asDouble());
                        cell.setHeight(cellBbox.path("height").asDouble());
                    }
                    
                    cells.add(cell);
                });
                
                row.setCells(cells);
                rows.add(row);
            });
            
            table.setRows(rows);
            tables.add(table);
        });
        ocrResult.setTables(tables);

        // 添加元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("engine", "llm");
        metadata.put("model", "gpt-4-vision-preview");
        ocrResult.setMetadata(metadata);

        return ocrResult;
    }

    @Override
    public boolean supports(String mimeType) {
        return StringUtils.hasText(mimeType) && (
            mimeType.startsWith("image/") ||
            mimeType.equals("application/pdf")
        );
    }

    @Override
    public List<String> getSupportedMimeTypes() {
        return List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/bmp",
            "image/webp",
            "application/pdf"
        );
    }

    @Override
    public String getEngineType() {
        return "llm";
    }
} 