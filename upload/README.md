# 文件上传模块 (Upload Module)

## 概述

文件上传模块提供了完整的文件上传、存储和管理功能，支持单个文件和多个文件上传，支持本地文件系统和MinIO对象存储两种存储方式。

## 功能特性

### 核心功能
- 单个文件上传
- 多个文件上传  
- 文件下载
- 文件删除
- 文件存在检查
- 文件URL获取

### 存储支持
- 本地文件系统存储
- MinIO对象存储
- 配置化存储方式切换

### 文件管理
- 文件类型验证
- 文件大小限制
- 唯一文件名生成
- 按日期分目录存储
- 文件哈希计算(MD5/SHA256)

## 配置说明

### 本地存储配置
```yaml
app:
  upload:
    storage-type: local
    local:
      root-path: ./uploads
      url-prefix: /files
      date-folder: true
```

### MinIO存储配置
```yaml
app:
  upload:
    storage-type: minio
    minio:
      endpoint: http://localhost:9000
      access-key: minioadmin
      secret-key: minioadmin
      bucket-name: uploads
```

## API接口

### 文件上传
- `POST /api/upload/single` - 单个文件上传
- `POST /api/upload/multiple` - 多个文件上传

### 基于UUID的安全操作
- `GET /api/upload/info/id/{fileId}` - 根据文件ID获取文件信息
- `GET /api/upload/download/id/{fileId}` - 根据文件ID下载文件
- `DELETE /api/upload/id/{fileId}` - 根据文件ID删除文件
- `GET /api/upload/exists/id/{fileId}` - 根据文件ID检查文件存在
- `GET /api/upload/url/id/{fileId}` - 根据文件ID获取文件URL

### 认证要求
- 所有文件上传和操作接口都需要认证
- 默认用户名：`admin`，密码：`admin123`
- 使用HTTP Basic认证

## 使用示例

### 基本使用
```java
@Autowired
private FileUploadService fileUploadService;

// 基于UUID的安全操作
Optional<FileInfo> fileInfo = fileUploadService.getFileInfoById("file-uuid");
Resource resource = fileUploadService.getFileResourceById("file-uuid");
boolean exists = fileUploadService.fileExistsById("file-uuid");
boolean deleted = fileUploadService.deleteFileById("file-uuid");
```

### API调用示例
```bash
# 上传单个文件（需要认证）
curl -u admin:admin123 \
  -X POST "http://localhost:8080/api/upload/single" \
  -F "file=@test.jpg" \
  -F "tags=image,test"

# 获取文件信息
curl -u admin:admin123 \
  -X GET "http://localhost:8080/api/upload/info/id/{fileId}"

# 下载文件
curl -u admin:admin123 \
  -X GET "http://localhost:8080/api/upload/download/id/{fileId}" \
  -o downloaded_file.jpg

# 删除文件
curl -u admin:admin123 \
  -X DELETE "http://localhost:8080/api/upload/id/{fileId}"
```

### 事件监听
```java
@Component
public class CustomFileEventListener {
    
    @EventListener
    @Async
    public void handleFileUpload(FileUploadEvent event) {
        if (event.getEventType() == FileUploadEvent.EventType.FILE_UPLOADED) {
            FileInfo fileInfo = event.getFileInfo();
            // 自定义处理逻辑：发送通知、更新缓存等
            log.info("File uploaded: {}", fileInfo.getOriginalFileName());
        }
    }
}
```

## 文件信息返回格式

```json
{
  "fileId": "uuid",
  "originalFileName": "example.jpg",
  "relativePath": "2025/01/15/abc123.jpg",
  "fileSize": 1024000,
  "contentType": "image/jpeg",
  "md5Hash": "...",
  "sha256Hash": "...",
  "storageType": "LOCAL",
  "accessUrl": "/files/2025/01/15/abc123.jpg"
}
``` 