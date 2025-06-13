# 文件信息存储服务

本文档介绍基于JPA和Druid的文件信息存储服务，提供强大的文件管理功能和持久化能力。

## 功能特性

### 基础功能
- ✅ 文件信息持久化存储（PostgreSQL数据库）
- ✅ 软删除支持（逻辑删除）
- ✅ 乐观锁并发控制
- ✅ 审计字段（创建时间、更新时间、创建者、更新者）
- ✅ 自动时间戳管理

### 高级功能
- 🔍 **全文搜索**：支持文件名、标签、类型的模糊搜索
- 🔍 **高级搜索**：多条件组合搜索（时间范围、文件大小、用户等）
- 📊 **统计分析**：用户文件数量、总大小统计
- 🔄 **重复检测**：基于MD5/SHA256哈希值的重复文件检测
- 📄 **分页查询**：大数据量的分页处理
- 🗑️ **批量操作**：批量软删除、恢复
- 🧹 **自动清理**：过期文件自动清理

## 配置说明

### 1. 数据库配置

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    url: jdbc:postgresql://localhost:5432/spring_ai_app
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver
    
    # Druid连接池配置
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      filters: stat,wall,slf4j
      connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
      
      # Druid监控配置
      web-stat-filter:
        enabled: true
        url-pattern: "/*"
        exclusions: "*.js,*.gif,*.jpg,*.bmp,*.png,*.css,*.ico,/druid/*"
      stat-view-servlet:
        enabled: true
        url-pattern: "/druid/*"
        allow: 127.0.0.1
        reset-enable: false
        login-username: admin
        login-password: 123456
    
  jpa:
    hibernate:
      ddl-auto: update  # 自动建表和更新表结构
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
```

### 2. Druid监控

访问 `http://localhost:8080/druid` 可以查看数据库连接池监控信息：
- 用户名：admin
- 密码：123456

## 数据库表结构

### file_info 表

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGSERIAL | 主键ID |
| file_id | VARCHAR(36) | 文件ID（UUID） |
| original_file_name | VARCHAR(500) | 原始文件名 |
| stored_file_name | VARCHAR(500) | 存储文件名 |
| file_extension | VARCHAR(20) | 文件扩展名 |
| relative_path | VARCHAR(1000) | 相对存储路径 |
| full_path | VARCHAR(2000) | 完整存储路径 |
| file_size | BIGINT | 文件大小（字节） |
| content_type | VARCHAR(200) | MIME类型 |
| md5_hash | VARCHAR(32) | MD5哈希值 |
| sha256_hash | VARCHAR(64) | SHA256哈希值 |
| storage_type | VARCHAR(20) | 存储类型（LOCAL/MINIO） |
| bucket_name | VARCHAR(100) | 存储桶名称 |
| upload_time | TIMESTAMP | 上传时间 |
| upload_user_id | VARCHAR(50) | 上传用户ID |
| access_url | VARCHAR(2000) | 文件访问URL |
| tags | VARCHAR(1000) | 文件标签 |
| metadata | TEXT | 扩展元数据（JSON格式） |
| enabled | BOOLEAN | 是否启用（软删除标识） |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| created_by | VARCHAR(50) | 创建者 |
| updated_by | VARCHAR(50) | 更新者 |
| version | BIGINT | 版本号（乐观锁） |

### 索引优化

系统自动创建了以下索引以优化查询性能：

- `idx_file_info_file_id`：文件ID索引
- `idx_file_info_upload_user_id`：用户ID索引
- `idx_file_info_relative_path`：路径索引
- `idx_file_info_upload_time`：时间索引
- `idx_file_info_enabled`：启用状态索引
- `idx_file_info_user_enabled`：用户+状态复合索引
- `idx_file_info_enabled_upload_time`：状态+时间复合索引

## API接口

### 基础接口

所有 `/api/files` 下的基础接口都可以正常使用。

### 扩展接口

#### 1. 搜索文件
```http
GET /api/files/management/search?keyword=文档&page=0&size=20
```

#### 2. 高级搜索
```http
GET /api/files/management/advanced-search?fileName=报告&contentType=application/pdf&startTime=2024-01-01T00:00:00&endTime=2024-12-31T23:59:59&minSize=1024&maxSize=10485760&page=0&size=20
```

#### 3. 分页查询用户文件
```http
GET /api/files/management/user/user123?page=0&size=20
```

#### 4. 用户文件统计
```http
GET /api/files/management/statistics/user123
```

响应示例：
```json
{
  "userId": "user123",
  "fileCount": 25,
  "totalSize": 52428800,
  "averageSize": 2097152
}
```

#### 5. 查找重复文件
```http
GET /api/files/management/duplicates/d41d8cd98f00b204e9800998ecf8427e
```

#### 6. 批量软删除
```http
POST /api/files/management/batch-delete
Content-Type: application/json

["file-id-1", "file-id-2", "file-id-3"]
```

#### 7. 恢复文件
```http
POST /api/files/management/restore/file-id-1
```

#### 8. 清理过期文件
```http
POST /api/files/management/cleanup?expireTime=2024-01-01T00:00:00
```

## 使用示例

### Java代码示例

```java
@Service
@RequiredArgsConstructor
public class FileService {
    
    private final JpaFileInfoStorageService fileInfoStorageService;
    
    // 搜索文件
    public Page<FileInfo> searchFiles(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return fileInfoStorageService.searchFiles(keyword, pageable);
    }
    
    // 获取用户统计
    public Map<String, Object> getUserStats(String userId) {
        long count = fileInfoStorageService.countByUploadUserId(userId);
        long totalSize = fileInfoStorageService.sumFileSizeByUploadUserId(userId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("fileCount", count);
        stats.put("totalSize", totalSize);
        return stats;
    }
    
    // 清理过期文件
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupExpiredFiles() {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(30);
        List<FileInfo> cleaned = fileInfoStorageService.cleanupExpiredFiles(expireTime);
        log.info("Cleaned up {} expired files", cleaned.size());
    }
}
```

## 性能优化建议

### 1. 数据库连接池配置
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000
```

### 2. JPA批处理配置
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true
        batch_versioned_data: true
```

### 3. 查询优化
- 使用分页查询避免大结果集
- 合理使用索引
- 避免N+1查询问题
- 使用@Query注解优化复杂查询

## 监控和维护

### 1. 数据库监控
- 监控连接池使用情况
- 监控慢查询
- 监控表大小和索引效率

### 2. 定期维护
- 定期清理过期文件
- 定期分析表统计信息
- 定期检查索引使用情况

### 3. 备份策略
- 定期备份数据库
- 测试恢复流程
- 监控备份任务状态

## 故障排除

### 常见问题

1. **服务启动失败**
   - 检查数据库连接配置
   - 确认数据库服务是否启动
   - 检查Flyway迁移脚本

2. **查询性能慢**
   - 检查是否缺少索引
   - 分析查询执行计划
   - 考虑添加复合索引

3. **内存占用高**
   - 检查JPA二级缓存配置
   - 优化查询结果集大小
   - 调整连接池参数

## 快速开始

1. 确保PostgreSQL数据库已启动
2. 配置数据库连接信息
3. 启动应用程序，JPA会自动创建表结构
4. 访问 `http://localhost:8080/druid` 查看数据库监控
5. 使用文件上传和管理功能

## 技术特性

- **JPA自动建表**：无需手动创建数据库表
- **Druid连接池**：高性能数据库连接池
- **监控面板**：实时查看SQL执行情况
- **事务管理**：自动事务控制
- **乐观锁**：并发控制机制 