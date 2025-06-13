package cn.mojoup.ai.rag.service.impl;

import cn.mojoup.ai.rag.domain.DocumentSegment;
import cn.mojoup.ai.rag.service.DocumentRerankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 文档重排序服务实现类
 * 集成Spring AI ChatModel，提供多种重排序策略
 *
 * @author matt
 */
@Slf4j
@Service
public class DocumentRerankServiceImpl implements DocumentRerankService {

    @Autowired
    private ChatClient chatClient;

    @Value("${rag.rerank.model:gpt-3.5-turbo}")
    private String rerankModel;

    @Value("${rag.rerank.temperature:0.0}")
    private Double temperature;

    private static final String RERANK_PROMPT_TEMPLATE = """
            请评估以下文本与查询的相关性（0-1之间的分数）：
            查询：{query}
            文本：{content}
            只返回一个0-1之间的数字，不要其他内容。
            """;

    @Override
    public List<DocumentSegment> rerank(List<DocumentSegment> documents, String query) {
        log.debug("执行文档重排序: query={}, documents={}", query, documents.size());

        if (documents == null || documents.isEmpty()) {
            return documents;
        }

        // 使用交叉编码器计算相关性分数
        for (DocumentSegment doc : documents) {
            double relevanceScore = calculateRelevanceScore(query, doc.getContent());
            doc.setScore(relevanceScore);
        }

        // 按分数降序排序
        documents.sort(Comparator.comparingDouble(DocumentSegment::getScore).reversed());

        return documents;
    }

    private double calculateRelevanceScore(String query, String content) {
        try {
            // 构建提示词
            SystemPromptTemplate promptTemplate = new SystemPromptTemplate(RERANK_PROMPT_TEMPLATE);
            Prompt prompt = promptTemplate.create(Map.of(
                    "query", query,
                    "content", content
            ));

            // 调用AI模型获取相关性分数
            String response = chatClient.prompt(prompt).call().chatResponse().getResult().getOutput().getText();
            return Double.parseDouble(response.trim());

        } catch (Exception e) {
            log.error("计算相关性分数失败", e);
            return 0.0;
        }
    }
} 