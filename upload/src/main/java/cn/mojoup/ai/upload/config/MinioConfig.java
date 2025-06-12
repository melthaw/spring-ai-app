package cn.mojoup.ai.upload.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO配置类
 * 
 * @author matt
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.upload.storage-type", havingValue = "minio")
public class MinioConfig {
    
    private final UploadProperties uploadProperties;
    
    /**
     * 配置MinIO客户端
     */
    @Bean
    public MinioClient minioClient() {
        UploadProperties.Minio minioProps = uploadProperties.getMinio();
        
        log.info("Configuring MinIO client with endpoint: {}", minioProps.getEndpoint());
        
        return MinioClient.builder()
                .endpoint(minioProps.getEndpoint())
                .credentials(minioProps.getAccessKey(), minioProps.getSecretKey())
                .build();
    }
} 