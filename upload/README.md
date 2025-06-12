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

- `POST /api/upload/single` - 单个文件上传
- `POST /api/upload/multiple` - 多个文件上传
- `GET /api/upload/download/{path}` - 文件下载
- `DELETE /api/upload/{path}` - 删除文件
- `GET /api/upload/exists/{path}` - 检查文件存在
- `GET /api/upload/url/{path}` - 获取文件URL

## 使用示例

```java
@Autowired
private FileUploadService fileUploadService;

// 获取文件资源
Resource resource = fileUploadService.getFileResource("path/to/file.jpg");

// 检查文件是否存在
boolean exists = fileUploadService.fileExists("path/to/file.jpg");
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