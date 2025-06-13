package cn.mojoup.ai.kb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 知识库模块配置
 *
 * @author matt
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "knowledge-base")
public class KnowledgeBaseConfig {

    /**
     * 基础配置
     */
    private ConfigProperties config = new ConfigProperties();

    /**
     * 权限配置
     */
    private PermissionProperties permission = new PermissionProperties();

    /**
     * 嵌入配置
     */
    private EmbeddingProperties embedding = new EmbeddingProperties();

    /**
     * 审计配置
     */
    private AuditProperties audit = new AuditProperties();

    /**
     * 文件配置
     */
    private FileProperties file = new FileProperties();

    /**
     * 上传集成配置
     */
    private UploadIntegrationProperties uploadIntegration = new UploadIntegrationProperties();

    /**
     * 缓存配置
     */
    private CacheProperties cache = new CacheProperties();

    /**
     * 性能配置
     */
    private PerformanceProperties performance = new PerformanceProperties();

    /**
     * 监控配置
     */
    private MonitoringProperties monitoring = new MonitoringProperties();

    /**
     * 安全配置
     */
    private SecurityProperties security = new SecurityProperties();

    @Data
    public static class ConfigProperties {
        private Integer defaultPageSize = 20;
        private Integer maxPageSize = 100;
        private Integer maxTreeDepth = 10;
        private Integer maxDocumentsPerKb = 10000;
        private Integer maxKbNameLength = 100;
    }

    @Data
    public static class PermissionProperties {
        private Boolean defaultInheritParent = true;
        private Integer permissionCacheTtl = 3600;
        private Boolean enablePermissionCache = true;
        private Integer permissionCheckTimeout = 5000;
    }

    @Data
    public static class EmbeddingProperties {
        private String defaultModel = "text-embedding-ada-002";
        private List<String> supportedModels = List.of(
                "text-embedding-ada-002",
                "text-embedding-3-small",
                "text-embedding-3-large",
                "bge-large-zh",
                "m3e-base"
        );
        private ChunkingProperties chunking = new ChunkingProperties();
        private TaskProperties task = new TaskProperties();
        private VectorStoreProperties vectorStore = new VectorStoreProperties();

        @Data
        public static class ChunkingProperties {
            private Integer defaultSize = 1000;
            private Integer defaultOverlap = 200;
            private Integer maxSize = 4000;
            private Integer minSize = 100;
        }

        @Data
        public static class TaskProperties {
            private Integer maxConcurrentTasks = 5;
            private Integer taskTimeoutMinutes = 30;
            private Integer maxRetryCount = 3;
            private Integer retryIntervalSeconds = 60;
        }

        @Data
        public static class VectorStoreProperties {
            private Integer batchInsertSize = 100;
            private Integer connectionTimeout = 30000;
        }
    }

    @Data
    public static class AuditProperties {
        private Boolean enabled = true;
        private Integer retentionDays = 365;
        private Boolean requireSensitiveAudit = true;
        private Boolean asyncLogging = true;
        private Integer auditQueueSize = 1000;
    }

    @Data
    public static class FileProperties {
        private List<String> supportedTypes = List.of(
                "pdf", "doc", "docx", "txt", "md", "json", "xml", "csv",
                "html", "htm", "ppt", "pptx", "xls", "xlsx"
        );
        private Integer maxFileSize = 50;
        private Integer maxPreviewLength = 5000;
    }

    @Data
    public static class UploadIntegrationProperties {
        private Boolean enabled = true;
        private Boolean autoCreateDocument = true;
        private Boolean filePermissionCheck = true;
        private List<String> supportedSources = List.of("LOCAL", "MINIO");
        private Integer fileAccessTimeout = 10000;
    }

    @Data
    public static class CacheProperties {
        private Integer kbListCacheTtl = 1800;
        private Integer documentTreeCacheTtl = 3600;
        private Integer statsCacheTtl = 900;
        private Integer permissionCacheTtl = 1800;
    }

    @Data
    public static class PerformanceProperties {
        private Integer asyncPoolSize = 10;
        private Integer maxBatchSize = 1000;
        private Integer queryTimeout = 30000;
    }

    @Data
    public static class MonitoringProperties {
        private Boolean enableMetrics = true;
        private Integer slowQueryThreshold = 5000;
        private Boolean detailedLogging = false;
    }

    @Data
    public static class SecurityProperties {
        private DataMaskingProperties dataMasking = new DataMaskingProperties();
        private RateLimitProperties rateLimit = new RateLimitProperties();

        @Data
        public static class DataMaskingProperties {
            private Boolean enabled = true;
            private List<String> maskFields = List.of("password", "token", "secret");
        }

        @Data
        public static class RateLimitProperties {
            private Boolean enabled = true;
            private Integer requestsPerMinute = 1000;
            private Integer requestsPerHour = 10000;
        }
    }
} 