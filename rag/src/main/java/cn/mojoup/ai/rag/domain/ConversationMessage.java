package cn.mojoup.ai.rag.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMessage {
    private String role; // user, assistant
    private String content;
    private String timestamp;
} 