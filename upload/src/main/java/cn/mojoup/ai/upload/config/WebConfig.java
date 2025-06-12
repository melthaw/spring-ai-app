package cn.mojoup.ai.upload.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.MultipartConfigElement;
import java.nio.file.Paths;

/**
 * Web配置类
 * 
 * @author matt
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final UploadProperties uploadProperties;
    
    /**
     * 配置文件上传限制
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // 设置单个文件最大大小
        factory.setMaxFileSize(DataSize.ofBytes(uploadProperties.getMaxFileSize()));
        
        // 设置总上传数据最大大小
        factory.setMaxRequestSize(DataSize.ofBytes(uploadProperties.getMaxRequestSize()));
        
        log.info("Configured multipart: maxFileSize={}, maxRequestSize={}", 
                uploadProperties.getMaxFileSize(), uploadProperties.getMaxRequestSize());
        
        return factory.createMultipartConfig();
    }
    
    /**
     * 配置静态资源处理（仅当使用本地存储时）
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if ("local".equalsIgnoreCase(uploadProperties.getStorageType())) {
            String urlPrefix = uploadProperties.getLocal().getUrlPrefix();
            String rootPath = uploadProperties.getLocal().getRootPath();
            
            // 确保URL前缀以/开头和结尾
            if (!urlPrefix.startsWith("/")) {
                urlPrefix = "/" + urlPrefix;
            }
            if (!urlPrefix.endsWith("/")) {
                urlPrefix = urlPrefix + "/";
            }
            
            // 确保根路径是绝对路径
            String absolutePath = Paths.get(rootPath).toAbsolutePath().toString();
            String resourceLocation = "file:" + absolutePath + "/";
            
            registry.addResourceHandler(urlPrefix + "**")
                    .addResourceLocations(resourceLocation)
                    .setCachePeriod(3600) // 缓存1小时
                    .resourceChain(true);
            
            log.info("Configured static resource handler: {} -> {}", urlPrefix + "**", resourceLocation);
        }
    }
} 