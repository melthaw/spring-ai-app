package cn.mojoup.ai.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 文件统计信息DTO
 * 
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfoStatsDTO {
    
    private Long totalFiles;
    private Long totalSize;
    private Long averageSize;
    private Map<String, Long> fileTypeStats;
    private Map<String, Long> userStats;
    private Map<String, Long> dailyStats;
} 