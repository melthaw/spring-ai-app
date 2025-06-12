package cn.mojoup.ai.upload.storage;

import cn.mojoup.ai.upload.config.UploadProperties;
import cn.mojoup.ai.upload.domain.FileInfo;
import cn.mojoup.ai.upload.domain.StorageType;
import cn.mojoup.ai.upload.domain.UploadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 本地文件系统存储服务实现
 * 
 * @author matt
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.upload.storage-type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {
    
    private final UploadProperties uploadProperties;
    
    @Override
    public FileInfo uploadFile(MultipartFile file, UploadRequest request) {
        validateFile(file);
        
        try {
            // 生成文件路径
            String relativePath = generateFilePath(file, request);
            Path targetPath = Paths.get(uploadProperties.getLocal().getRootPath()).resolve(relativePath);
            
            // 确保目录存在
            Files.createDirectories(targetPath.getParent());
            
            // 复制文件
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 生成文件信息
            return buildFileInfo(file, relativePath, targetPath, request);
            
        } catch (IOException e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }
    
    @Override
    public List<FileInfo> uploadFiles(List<MultipartFile> files, UploadRequest request) {
        List<FileInfo> fileInfos = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                FileInfo fileInfo = uploadFile(file, request);
                fileInfos.add(fileInfo);
            }
        }
        
        return fileInfos;
    }
    
    @Override
    public Resource getResource(String relativePath) {
        try {
            Path filePath = Paths.get(uploadProperties.getLocal().getRootPath()).resolve(relativePath);
            Resource resource = new FileSystemResource(filePath);
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable: " + relativePath);
            }
        } catch (Exception e) {
            log.error("Failed to get resource: {}", relativePath, e);
            throw new RuntimeException("Failed to get resource", e);
        }
    }
    
    @Override
    public boolean deleteFile(String relativePath) {
        try {
            Path filePath = Paths.get(uploadProperties.getLocal().getRootPath()).resolve(relativePath);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", relativePath, e);
            return false;
        }
    }
    
    @Override
    public boolean fileExists(String relativePath) {
        Path filePath = Paths.get(uploadProperties.getLocal().getRootPath()).resolve(relativePath);
        return Files.exists(filePath);
    }
    
    @Override
    public String getFileUrl(String relativePath) {
        return uploadProperties.getLocal().getUrlPrefix() + "/" + relativePath;
    }
    
    @Override
    public void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // 检查文件大小
        if (file.getSize() > uploadProperties.getMaxFileSize()) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }
        
        // 检查文件扩展名
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!uploadProperties.getAllowedExtensions().contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("File type not allowed: " + extension);
        }
    }
    
    /**
     * 生成文件存储路径
     */
    private String generateFilePath(MultipartFile file, UploadRequest request) {
        StringBuilder pathBuilder = new StringBuilder();
        
        // 自定义路径
        if (StringUtils.hasText(request.getCustomPath())) {
            pathBuilder.append(request.getCustomPath()).append("/");
        }
        
        // 按日期分目录
        if (uploadProperties.getLocal().getDateFolder()) {
            String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            pathBuilder.append(dateFolder).append("/");
        }
        
        // 生成文件名
        String fileName = generateFileName(file, request);
        pathBuilder.append(fileName);
        
        return pathBuilder.toString();
    }
    
    /**
     * 生成文件名
     */
    private String generateFileName(MultipartFile file, UploadRequest request) {
        String originalFileName = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFileName);
        String baseName = FilenameUtils.getBaseName(originalFileName);
        
        if (request.getGenerateUniqueName()) {
            // 生成唯一文件名
            String uniqueId = UUID.randomUUID().toString().replace("-", "");
            return uniqueId + "." + extension;
        } else {
            // 使用原始文件名
            return originalFileName;
        }
    }
    
    /**
     * 构建文件信息
     */
    private FileInfo buildFileInfo(MultipartFile file, String relativePath, Path targetPath, UploadRequest request) {
        try {
            return FileInfo.builder()
                    .fileId(UUID.randomUUID().toString())
                    .originalFileName(file.getOriginalFilename())
                    .storedFileName(targetPath.getFileName().toString())
                    .fileExtension(FilenameUtils.getExtension(file.getOriginalFilename()))
                    .relativePath(relativePath)
                    .fullPath(targetPath.toString())
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .md5Hash(calculateMD5(file))
                    .sha256Hash(calculateSHA256(file))
                    .storageType(StorageType.LOCAL)
                    .uploadTime(LocalDateTime.now())
                    .uploadUserId(request.getUploadUserId())
                    .accessUrl(getFileUrl(relativePath))
                    .tags(request.getTags())
                    .metadata(request.getMetadata())
                    .enabled(true)
                    .build();
        } catch (Exception e) {
            log.error("Failed to build file info", e);
            throw new RuntimeException("Failed to build file info", e);
        }
    }
    
    /**
     * 计算MD5哈希值
     */
    private String calculateMD5(MultipartFile file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(file.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            log.warn("Failed to calculate MD5 hash", e);
            return null;
        }
    }
    
    /**
     * 计算SHA256哈希值
     */
    private String calculateSHA256(MultipartFile file) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(file.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            log.warn("Failed to calculate SHA256 hash", e);
            return null;
        }
    }
} 