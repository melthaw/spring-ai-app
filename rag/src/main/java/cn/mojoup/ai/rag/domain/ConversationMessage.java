package cn.mojoup.ai.rag.domain;

import lombok.Data;

@Data
public class ConversationMessage {
    private String role; // user, assistant
    private String content;
    private String timestamp;
} 