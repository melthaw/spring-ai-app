# Knowledge Base Module

知识库管理模块，提供企业级的文档管理、权限控制、嵌入操作和审计功能。

## 功能特性

### 核心功能
- **树形文档管理**: 支持文件夹和文档的层级结构管理
- **细粒度权限控制**: 基于组织机构的权限管理体系
- **文档嵌入操作**: 集成RAG服务，支持文档向量化和语义搜索
- **企业级审计**: 完整的操作日志记录和审计追踪

### 与Upload模块集成
- **文件关联**: 通过`fileId`字段关联upload模块的文件信息
- **自动创建**: 支持从上传文件自动创建文档节点
- **文件更新**: 支持更新文档节点关联的文件
- **权限继承**: 文件权限与文档节点权限的统一管理

## 模块架构

```
knowledge-base/
├── src/main/java/cn/mojoup/ai/kb/
│   ├── config/          # 配置类
│   ├── controller/      # REST控制器
│   ├── dto/            # 数据传输对象
│   ├── entity/         # JPA实体类
│   ├── repository/     # 数据访问层
│   └── service/        # 业务逻辑层
└── src/main/resources/
    └── application.yml  # 配置文件
```

## 核心实体

### KnowledgeBase (知识库)
- 知识库基本信息管理
- 支持多种类型：通用、技术、业务、产品、培训
- 访问级别控制：公开、内部、受限、机密
- 嵌入配置：模型、分块大小、重叠等

### DocumentNode (文档节点)
- 树形结构的文档管理
- 支持文件夹和文档两种类型
- 与upload模块FileInfo集成
- 嵌入状态跟踪

### DocumentEmbedding (文档嵌入)
- 嵌入操作的参数和结果记录
- 支持多种嵌入模型
- 任务状态跟踪和重试机制

### 权限管理
- **KnowledgeBasePermission**: 知识库级别权限
- **DocumentNodePermission**: 文档节点级别权限
- 支持用户、部门、组织三种权限主体

### AuditLog (审计日志)
- 完整的操作记录
- 支持敏感操作审计
- 异步日志记录

## API接口

### 知识库管理
```http
POST   /api/kb                    # 创建知识库
GET    /api/kb                    # 查询知识库列表
GET    /api/kb/{kbId}             # 获取知识库详情
PUT    /api/kb/{kbId}             # 更新知识库
DELETE /api/kb/{kbId}             # 删除知识库
```

### 文档节点管理
```http
POST   /api/kb/{kbId}/nodes       # 创建文档节点
GET    /api/kb/{kbId}/nodes       # 获取文档树
GET    /api/kb/{kbId}/nodes/{nodeId} # 获取节点详情
PUT    /api/kb/{kbId}/nodes/{nodeId} # 更新节点
DELETE /api/kb/{kbId}/nodes/{nodeId} # 删除节点
```

### 文档上传集成
```http
POST   /api/kb/{kbId}/documents/upload        # 从上传文件创建文档节点
PUT    /api/kb/{kbId}/documents/{nodeId}/file # 更新文档节点的文件
POST   /api/kb/{kbId}/documents/batch-upload  # 批量创建文档节点
```

### 文档嵌入
```http
POST   /api/kb/{kbId}/embedding/embed         # 嵌入文档
POST   /api/kb/{kbId}/embedding/batch-embed   # 批量嵌入
GET    /api/kb/{kbId}/embedding/status/{taskId} # 查询嵌入状态
```

## 与Upload模块集成

### 集成方式
1. **依赖注入**: 注入`FileInfoStorageService`服务
2. **字段关联**: DocumentNode通过`fileId`字段关联FileInfo
3. **权限检查**: 验证用户对文件的访问权限
4. **元数据同步**: 自动同步文件的基本信息

### 集成配置
```yaml
knowledge-base:
  upload-integration:
    enabled: true                    # 启用文件上传集成
    auto-create-document: true       # 自动创建文档节点
    file-permission-check: true      # 文件权限检查
    supported-sources:               # 支持的文件来源
      - "LOCAL"
      - "MINIO"
    file-access-timeout: 10000       # 文件访问超时时间
```

### 使用示例

#### 从上传文件创建文档节点
```java
@PostMapping("/upload")
public ResponseEntity<DocumentNode> createDocumentFromUpload(
        @PathVariable String kbId,
        @RequestParam(required = false) String parentId,
        @RequestBody CreateDocumentFromUploadRequest request) {
    
    DocumentNode documentNode = documentNodeService
        .createDocumentFromUpload(kbId, parentId, request);
    return ResponseEntity.ok(documentNode);
}
```

#### 更新文档节点的文件
```java
@PutMapping("/{nodeId}/file")
public ResponseEntity<DocumentNode> updateDocumentFile(
        @PathVariable String nodeId,
        @RequestBody UpdateDocumentFileRequest request) {
    
    DocumentNode documentNode = documentNodeService
        .updateDocumentFile(nodeId, request);
    return ResponseEntity.ok(documentNode);
}
```

## 配置说明

### 数据库配置
- 使用PostgreSQL数据库
- 支持Druid连接池
- 自动创建表结构 (`ddl-auto: update`)

### 缓存配置
- 使用Caffeine缓存
- 支持知识库列表、文档树、权限等多级缓存
- 可配置缓存过期时间

### 安全配置
- 集成Spring Security
- 支持方法级权限控制 (`@PreAuthorize`)
- 数据脱敏和API限流

### 性能配置
- 异步处理支持
- 批量操作优化
- 查询超时控制
- 慢查询监控

## 部署说明

### 依赖要求
- Java 17+
- PostgreSQL 12+
- Redis (可选，用于分布式缓存)

### 环境变量
```bash
# 数据库配置
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/spring_ai_app
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password

# 缓存配置
SPRING_CACHE_TYPE=caffeine

# 日志级别
LOGGING_LEVEL_CN_MOJOUP_AI_KB=DEBUG
```

### Docker部署
```dockerfile
FROM openjdk:17-jdk-slim
COPY knowledge-base-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 开发指南

### 添加新的文档类型支持
1. 在`application.yml`中添加文件类型
2. 实现相应的文档解析器
3. 更新嵌入服务的处理逻辑

### 扩展权限模型
1. 在权限实体中添加新的权限类型
2. 更新权限检查服务
3. 添加相应的API接口

### 自定义审计规则
1. 实现`AuditEventListener`接口
2. 在配置中注册监听器
3. 定义审计规则和过滤条件

## 监控和运维

### 健康检查
- `/actuator/health` - 应用健康状态
- `/actuator/metrics` - 性能指标
- `/actuator/info` - 应用信息

### 日志监控
- 支持结构化日志输出
- 集成ELK Stack
- 慢查询和异常监控

### 性能调优
- 数据库连接池优化
- JVM参数调优
- 缓存策略优化

## 故障排除

### 常见问题
1. **文件关联失败**: 检查upload模块服务是否正常
2. **权限检查失败**: 验证Spring Security配置
3. **嵌入操作超时**: 调整任务超时配置
4. **数据库连接问题**: 检查Druid连接池配置

### 日志分析
```bash
# 查看知识库操作日志
grep "KnowledgeBase" application.log

# 查看文档嵌入日志
grep "DocumentEmbedding" application.log

# 查看权限检查日志
grep "Permission" application.log
```

## 版本历史

### v1.0.0
- 基础知识库管理功能
- 文档树形结构支持
- 权限控制体系
- 与upload模块集成
- 文档嵌入功能
- 审计日志系统 