# 数据库集成指南

## 概述

upload模块默认使用内存实现的文件信息存储服务，生产环境需要实现数据库版本。

## 数据库表设计

```sql
CREATE TABLE file_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id VARCHAR(36) NOT NULL UNIQUE,
    original_file_name VARCHAR(500) NOT NULL,
    stored_file_name VARCHAR(500) NOT NULL,
    relative_path VARCHAR(1000) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(200),
    storage_type VARCHAR(20) NOT NULL,
    upload_time DATETIME NOT NULL,
    upload_user_id VARCHAR(50),
    enabled TINYINT(1) DEFAULT 1,
    INDEX idx_file_id (file_id),
    INDEX idx_upload_user_id (upload_user_id)
);
```

## 实现数据库版本

1. 添加JPA依赖
2. 创建Entity和Repository
3. 实现DatabaseFileInfoStorageService
4. 配置数据库连接
5. 设置条件注解切换实现

详细实现请参考项目文档。 