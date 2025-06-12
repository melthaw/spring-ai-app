package cn.mojoup.ai.rag.service;

import cn.mojoup.ai.rag.domain.ConversationMessage;
import cn.mojoup.ai.rag.domain.DocumentSegment;

import java.util.List;

/**
 * 答案生成服务接口
 *
 * @author matt
 */
public interface AnswerGenerationService {

    /**
     * 生成基础答案
     */
    String generateAnswer(String question, List<DocumentSegment> documents,
                          Double temperature, Integer maxTokens);

    /**
     * 生成对话式答案
     */
    String generateConversationalAnswer(String question, List<DocumentSegment> documents,
                                        List<ConversationMessage> history,
                                        Double temperature, Integer maxTokens);

    /**
     * 生成优化答案
     */
    String generateOptimizedAnswer(String question, List<DocumentSegment> documents,
                                   String intent, String strategy,
                                   Double temperature, Integer maxTokens);

    /**
     * 生成带引用的答案
     */
    String generateAnswerWithCitations(String question, List<DocumentSegment> documents,
                                       String citationStyle, Boolean includePage,
                                       Double temperature, Integer maxTokens);

    /**
     * 构建提示词
     */
    String buildPrompt(String question, List<DocumentSegment> documents, String promptType);

    /**
     * 根据意图调整温度参数
     */
    Double adjustTemperatureByIntent(String intent, Double temperature);
} 