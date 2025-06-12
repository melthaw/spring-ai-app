package cn.mojoup.ai.rag.domain;

import lombok.Data;

@Data
public class Citation {
    private String citationText;
    private String sourceTitle;
    private String sourceUrl;
    private String author;
    private String publishDate;
    private String pageNumber;
    private DocumentSegment sourceSegment;
} 