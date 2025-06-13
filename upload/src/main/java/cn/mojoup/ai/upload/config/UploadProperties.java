package cn.mojoup.ai.upload.config;

import cn.mojoup.ai.upload.domain.StorageType;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件上传配置属性
 *
 * @author matt
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    /**
     * 存储类型（local/minio）
     */
    private String storageType = "local";

    /**
     * 最大文件大小（字节）
     */
    private Long maxFileSize = 10 * 1024 * 1024L; // 10MB

    /**
     * 最大请求大小（字节）
     */
    private Long maxRequestSize = 50 * 1024 * 1024L; // 50MB

    /**
     * 允许的文件扩展名
     */
    private List<String> allowedExtensions = List.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", // 图片
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", // 文档
            "txt", "md", "json", "xml", "csv", // 文本
            "zip", "rar", "7z", "tar", "gz" // 压缩包
    );

    /**
     * 本地存储配置
     */
    private Local local = new Local();

    /**
     * MinIO存储配置
     */
    private Minio minio = new Minio();

    /**
     * 本地存储配置
     */
    @Data
    public static class Local {

        /**
         * 存储根目录
         */
        private String rootPath = "./uploads";

        /**
         * URL前缀
         */
        private String urlPrefix = "/files";

        /**
         * 是否按日期分目录
         */
        private Boolean dateFolder = true;
    }

    /**
     * MinIO存储配置
     */
    @Data
    public static class Minio {

        /**
         * 服务端点
         */
        private String endpoint = "http://localhost:9000";

        /**
         * 访问密钥
         */
        private String accessKey = "minioadmin";

        /**
         * 秘密密钥
         */
        private String secretKey = "minioadmin";

        /**
         * 默认存储桶
         */
        private String bucketName = "uploads";

        /**
         * 是否自动创建存储桶
         */
        private Boolean autoCreateBucket = true;

        /**
         * URL过期时间（秒）
         */
        private Integer urlExpiry = 7 * 24 * 3600; // 7天
    }

    /**
     * 获取存储类型枚举
     */
    public StorageType getStorageTypeEnum() {
        return StorageType.fromCode(this.storageType);
    }
} 