package cn.mojoup.ai.rag.service.impl;

import cn.mojoup.ai.rag.domain.ConversationMessage;
import cn.mojoup.ai.rag.domain.DocumentSegment;
import cn.mojoup.ai.rag.service.AnswerGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 答案生成服务实现类
 *
 * @author matt
 */
@Slf4j
@Service
public class AnswerGenerationServiceImpl implements AnswerGenerationService {

    // TODO: 注入Spring AI相关组件
    // @Autowired
    // private ChatClient chatClient;

    @Override
    public String generateAnswer(String question, List<DocumentSegment> documents,
                                 Double temperature, Integer maxTokens) {
        log.debug("生成答案: question={}, docCount={}, temperature={}",
                  question, documents.size(), temperature);

        // TODO: 使用Spring AI的ChatClient生成答案
        // 构建提示词，包含问题和检索到的文档
        String prompt = buildPrompt(question, documents, "basic");

        // 模拟生成回答
        return String.format("基于提供的%d个文档片段，关于\"%s\"的回答是：这是一个模拟的回答，实际实现中会使用大语言模型根据检索到的文档内容生成更准确的答案。",
                             documents.size(), question);
    }

    @Override
    public String generateConversationalAnswer(String question, List<DocumentSegment> documents,
                                               List<ConversationMessage> history,
                                               Double temperature, Integer maxTokens) {
        log.debug("生成对话式答案: question={}, historySize={}", question,
                  history != null ? history.size() : 0);

        // TODO: 构建包含对话历史的上下文
        String conversationPrompt = buildConversationalPrompt(question, documents, history);

        return String.format("基于对话上下文和%d个相关文档，关于\"%s\"的回答是：这是一个考虑了对话历史的模拟回答。",
                             documents.size(), question);
    }

    @Override
    public String generateOptimizedAnswer(String question, List<DocumentSegment> documents,
                                          String intent, String strategy,
                                          Double temperature, Integer maxTokens) {
        log.debug("生成优化答案: intent={}, strategy={}", intent, strategy);

        // 根据意图调整生成策略
        Double adjustedTemperature = adjustTemperatureByIntent(intent, temperature);

        // 根据策略选择不同的生成方式
        switch (strategy.toLowerCase()) {
            case "detailed":
                return generateDetailedAnswer(question, documents, adjustedTemperature, maxTokens);
            case "concise":
                return generateConciseAnswer(question, documents, adjustedTemperature, maxTokens);
            case "comparative":
                return generateComparativeAnswer(question, documents, adjustedTemperature, maxTokens);
            default:
                return generateAnswer(question, documents, adjustedTemperature, maxTokens);
        }
    }

    @Override
    public String generateAnswerWithCitations(String question, List<DocumentSegment> documents,
                                              String citationStyle, Boolean includePage,
                                              Double temperature, Integer maxTokens) {
        log.debug("生成带引用的答案: citationStyle={}, includePage={}", citationStyle, includePage);

        String answer = generateAnswer(question, documents, temperature, maxTokens);

        StringBuilder answerWithCitations = new StringBuilder(answer);
        answerWithCitations.append("\n\n参考文献：\n");

        for (int i = 0; i < documents.size(); i++) {
            DocumentSegment doc = documents.get(i);
            String citation = formatCitation(doc, citationStyle, includePage, i + 1);
            answerWithCitations.append(citation).append("\n");
        }

        return answerWithCitations.toString();
    }

    @Override
    public String buildPrompt(String question, List<DocumentSegment> documents, String promptType) {
        log.debug("构建提示词: question={}, docCount={}, promptType={}",
                  question, documents.size(), promptType);

        StringBuilder prompt = new StringBuilder();

        switch (promptType.toLowerCase()) {
            case "basic":
                prompt.append("请根据以下文档回答问题：\n\n");
                break;
            case "detailed":
                prompt.append("请详细回答以下问题，基于提供的文档内容：\n\n");
                break;
            case "concise":
                prompt.append("请简洁地回答以下问题：\n\n");
                break;
            default:
                prompt.append("请回答以下问题：\n\n");
        }

        // 添加文档上下文
        for (int i = 0; i < documents.size(); i++) {
            DocumentSegment doc = documents.get(i);
            prompt.append(String.format("文档%d: %s\n内容: %s\n\n",
                                        i + 1, doc.getTitle(), doc.getContent()));
        }

        prompt.append("问题: ").append(question).append("\n");
        prompt.append("请基于上述文档内容回答：");

        return prompt.toString();
    }

    @Override
    public Double adjustTemperatureByIntent(String intent, Double temperature) {
        switch (intent.toLowerCase()) {
            case "definition":
                return Math.min(temperature, 0.3); // 定义类问题需要准确性
            case "creative":
                return Math.max(temperature, 0.8); // 创意类问题需要多样性
            case "factual":
                return Math.min(temperature, 0.2); // 事实类问题需要准确性
            case "analysis":
                return Math.min(temperature, 0.5); // 分析类问题需要逻辑性
            default:
                return temperature;
        }
    }

    // ==================== 私有辅助方法 ====================

    private String buildConversationalPrompt(String question, List<DocumentSegment> documents,
                                             List<ConversationMessage> history) {
        StringBuilder prompt = new StringBuilder();

        // 添加对话历史
        if (history != null && !history.isEmpty()) {
            prompt.append("对话历史:\n");
            int startIndex = Math.max(0, history.size() - 6); // 最近3轮对话
            for (int i = startIndex; i < history.size(); i++) {
                ConversationMessage msg = history.get(i);
                prompt.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
            }
            prompt.append("\n");
        }

        // 添加文档上下文
        prompt.append("参考文档:\n");
        for (DocumentSegment doc : documents) {
            prompt.append("- ").append(doc.getContent()).append("\n");
        }

        prompt.append("\n当前问题: ").append(question);
        prompt.append("\n请结合对话历史和参考文档回答：");

        return prompt.toString();
    }

    private String generateDetailedAnswer(String question, List<DocumentSegment> documents,
                                          Double temperature, Integer maxTokens) {
        return "详细回答：" + generateAnswer(question, documents, temperature, maxTokens);
    }

    private String generateConciseAnswer(String question, List<DocumentSegment> documents,
                                         Double temperature, Integer maxTokens) {
        return "简洁回答：" + generateAnswer(question, documents, temperature, Math.min(maxTokens, 500));
    }

    private String generateComparativeAnswer(String question, List<DocumentSegment> documents,
                                             Double temperature, Integer maxTokens) {
        return "比较分析：" + generateAnswer(question, documents, temperature, maxTokens);
    }

    private String formatCitation(DocumentSegment doc, String style, Boolean includePage, int index) {
        switch (style.toLowerCase()) {
            case "apa":
                return String.format("[%d] %s. %s", index, doc.getTitle(), doc.getSource());
            case "mla":
                return String.format("[%d] \"%s.\" %s", index, doc.getTitle(), doc.getSource());
            case "chicago":
                return String.format("[%d] %s, %s", index, doc.getTitle(), doc.getSource());
            default:
                return String.format("[%d] %s - %s", index, doc.getTitle(), doc.getSource());
        }
    }
} 