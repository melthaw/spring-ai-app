package cn.mojoup.ai.upload.storage;

import cn.mojoup.ai.upload.config.UploadProperties;
import cn.mojoup.ai.upload.domain.FileInfo;
import cn.mojoup.ai.upload.domain.StorageType;
import cn.mojoup.ai.upload.domain.UploadRequest;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO存储服务实现
 * 
 * @author matt
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.upload.storage-type", havingValue = "minio")
public class MinioFileStorageService implements FileStorageService {
    
    private final UploadProperties uploadProperties;
    private final MinioClient minioClient;
    
    @Override
    public FileInfo uploadFile(MultipartFile file, UploadRequest request) {
        validateFile(file);
        
        try {
            // 确保存储桶存在
            ensureBucketExists();
            
            // 生成对象名
            String objectName = generateObjectName(file, request);
            
            // 上传文件
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(uploadProperties.getMinio().getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();
            
            ObjectWriteResponse response = minioClient.putObject(putObjectArgs);
            
            // 生成文件信息
            return buildFileInfo(file, objectName, request);
            
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to upload file to MinIO", e);
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
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(uploadProperties.getMinio().getBucketName())
                    .object(relativePath)
                    .build();
            
            InputStream inputStream = minioClient.getObject(getObjectArgs);
            return new InputStreamResource(inputStream);
            
        } catch (Exception e) {
            log.error("Failed to get resource from MinIO: {}", relativePath, e);
            throw new RuntimeException("Failed to get resource from MinIO", e);
        }
    }
    
    @Override
    public boolean deleteFile(String relativePath) {
        try {
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(uploadProperties.getMinio().getBucketName())
                    .object(relativePath)
                    .build();
            
            minioClient.removeObject(removeObjectArgs);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", relativePath, e);
            return false;
        }
    }
    
    @Override
    public boolean fileExists(String relativePath) {
        try {
            StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                    .bucket(uploadProperties.getMinio().getBucketName())
                    .object(relativePath)
                    .build();
            
            minioClient.statObject(statObjectArgs);
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String getFileUrl(String relativePath) {
        try {
            GetPresignedObjectUrlArgs getPresignedObjectUrlArgs = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(uploadProperties.getMinio().getBucketName())
                    .object(relativePath)
                    .expiry(uploadProperties.getMinio().getUrlExpiry(), TimeUnit.SECONDS)
                    .build();
            
            return minioClient.getPresignedObjectUrl(getPresignedObjectUrlArgs);
            
        } catch (Exception e) {
            log.error("Failed to get presigned URL: {}", relativePath, e);
            return null;
        }
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
     * 确保存储桶存在
     */
    private void ensureBucketExists() {
        try {
            String bucketName = uploadProperties.getMinio().getBucketName();
            
            BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build();
            
            boolean bucketExists = minioClient.bucketExists(bucketExistsArgs);
            
            if (!bucketExists && uploadProperties.getMinio().getAutoCreateBucket()) {
                MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build();
                
                minioClient.makeBucket(makeBucketArgs);
                log.info("Created MinIO bucket: {}", bucketName);
            }
            
        } catch (Exception e) {
            log.error("Failed to ensure bucket exists", e);
            throw new RuntimeException("Failed to ensure bucket exists", e);
        }
    }
    
    /**
     * 生成对象名
     */
    private String generateObjectName(MultipartFile file, UploadRequest request) {
        StringBuilder pathBuilder = new StringBuilder();
        
        // 自定义路径
        if (StringUtils.hasText(request.getCustomPath())) {
            pathBuilder.append(request.getCustomPath()).append("/");
        }
        
        // 按日期分目录
        String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        pathBuilder.append(dateFolder).append("/");
        
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
    private FileInfo buildFileInfo(MultipartFile file, String objectName, UploadRequest request) {
        try {
            return FileInfo.builder()
                    .fileId(UUID.randomUUID().toString())
                    .originalFileName(file.getOriginalFilename())
                    .storedFileName(objectName.substring(objectName.lastIndexOf("/") + 1))
                    .fileExtension(FilenameUtils.getExtension(file.getOriginalFilename()))
                    .relativePath(objectName)
                    .fullPath(uploadProperties.getMinio().getEndpoint() + "/" + 
                             uploadProperties.getMinio().getBucketName() + "/" + objectName)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .md5Hash(calculateMD5(file))
                    .sha256Hash(calculateSHA256(file))
                    .storageType(StorageType.MINIO)
                    .bucketName(uploadProperties.getMinio().getBucketName())
                    .uploadTime(LocalDateTime.now())
                    .uploadUserId(request.getUploadUserId())
                    .accessUrl(getFileUrl(objectName))
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