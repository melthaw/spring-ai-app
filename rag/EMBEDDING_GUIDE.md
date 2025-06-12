# 文档嵌入向量化功能指南

## 概述

RAG模块新增了强大的文档嵌入向量化功能，能够接受upload模块中的FileInfo，根据文件类型读取数据，并进行向量嵌入操作，最终存储到PGVector数据库中。该功能采用模块化设计，将文档读取、嵌入处理和向量存储分离为独立服务，同时提供整合服务统一处理流程。

## 系统架构

### 核心组件

#### 1. 文档读取服务 (DocumentReaderService)
- **功能**: 根据FileInfo和文件类型读取文档内容
- **支持格式**: PDF, DOC, DOCX, TXT, MD, JSON, XML, CSV, HTML, PPT, PPTX, XLS, XLSX
- **特性**: 
  - 智能文件类型识别
  - 自动内容清理
  - 元数据保留
  - 多种文档读取器集成

#### 2. 嵌入服务 (EmbeddingService)
- **功能**: 将文档内容转换为向量嵌入
- **支持模型**: 
  - OpenAI: text-embedding-ada-002, text-embedding-3-small, text-embedding-3-large
  - 中文模型: bge-large-zh, m3e-base
- **特性**:
  - 批量嵌入处理
  - 模型动态切换
  - 向量维度自适应

#### 3. 向量存储服务 (VectorStoreService)
- **功能**: 向量数据的存储和检索
- **数据库**: PGVector (PostgreSQL + pgvector扩展)
- **特性**:
  - 知识库隔离
  - 相似性搜索
  - 批量操作
  - 统计信息

#### 4. 文档嵌入整合服务 (DocumentEmbeddingService)
- **功能**: 整合上述三个服务，提供完整的处理流程
- **特性**:
  - 同步/异步处理
  - 批量处理
  - 任务状态追踪
  - 错误重试

## API接口

### 基础嵌入接口

#### 1. 处理文档嵌入
```http
POST /api/rag/embedding/process
Content-Type: application/json

{
  "fileId": "file-uuid-123",
  "knowledgeBaseId": "kb_001",
  "knowledgeBaseName": "技术文档库",
  "embeddingModel": "text-embedding-ada-002",
  "chunkSize": 1000,
  "chunkOverlap": 200,
  "preserveMetadata": true,
  "enableCleaning": true,
  "tags": ["技术", "AI"],
  "processingMode": "SYNC"
}
```

响应:
```json
{
  "processingId": "proc-uuid-456",
  "fileId": "file-uuid-123",
  "knowledgeBaseId": "kb_001",
  "status": "COMPLETED",
  "message": "文档嵌入完成",
  "segmentCount": 15,
  "successCount": 15,
  "failureCount": 0,
  "startTime": "2025-06-12T10:00:00",
  "endTime": "2025-06-12T10:02:30",
  "processingTime": 150000,
  "segments": [
    {
      "segmentId": "seg_001",
      "content": "文档片段内容...",
      "length": 800,
      "vectorDimension": 1536,
      "embedded": true,
      "error": null
    }
  ]
}
```

#### 2. 批量处理文档嵌入
```http
POST /api/rag/embedding/batch
Content-Type: application/json

[
  {
    "fileId": "file-uuid-123",
    "knowledgeBaseId": "kb_001",
    "embeddingModel": "text-embedding-ada-002"
  },
  {
    "fileId": "file-uuid-456", 
    "knowledgeBaseId": "kb_001",
    "embeddingModel": "text-embedding-ada-002"
  }
]
```

#### 3. 查询处理状态
```http
GET /api/rag/embedding/status/{processingId}
```

#### 4. 取消处理任务
```http
DELETE /api/rag/embedding/cancel/{processingId}
```

#### 5. 重试失败任务
```http
POST /api/rag/embedding/retry/{processingId}
```

### 管理接口

#### 1. 删除文档嵌入
```http
DELETE /api/rag/embedding/document/{fileId}?knowledgeBaseId=kb_001
```

#### 2. 检查文档嵌入状态
```http
GET /api/rag/embedding/check/{fileId}?knowledgeBaseId=kb_001
```

#### 3. 获取支持的文件类型
```http
GET /api/rag/embedding/supported-types
```

响应:
```json
{
  "supportedTypes": ["pdf", "doc", "docx", "txt", "md", "json", "xml", "csv", "html", "ppt", "pptx", "xls", "xlsx"],
  "count": 13
}
```

#### 4. 获取支持的嵌入模型
```http
GET /api/rag/embedding/supported-models
```

## 配置说明

### 应用配置 (application.yml)

```yaml
rag:
  spring-ai:
    openai:
      api-key: ${OPENAI_API_KEY:}
      base-url: https://api.openai.com
      embedding-model: text-embedding-ada-002
    vectorstore:
      type: pgvector
      pgvector:
        schema-name: public
        table-name: vector_store
        dimensions: 1536

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/suiyue_rag
    username: postgres
    password: postgres

document:
  embedding:
    supported-types:
      - pdf
      - doc
      - docx
      - txt
      - md
      - json
    chunking:
      default-size: 1000
      default-overlap: 200
    models:
      default: text-embedding-ada-002
    processing:
      max-concurrent-jobs: 5
      timeout-minutes: 30
```

### 环境变量

- `OPENAI_API_KEY`: OpenAI API密钥（可选，未配置时使用Mock实现）
- `DATABASE_URL`: PostgreSQL数据库连接URL
- `POSTGRES_USER`: 数据库用户名
- `POSTGRES_PASSWORD`: 数据库密码

## 部署指南

### 1. 数据库准备

#### 安装PostgreSQL和pgvector扩展
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo apt install postgresql-14-pgvector

# 或使用Docker
docker run --name pgvector-db \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=suiyue_rag \
  -p 5432:5432 \
  -d pgvector/pgvector:pg16
```

#### 创建数据库和扩展
```sql
CREATE DATABASE suiyue_rag;
\c suiyue_rag;
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

### 2. 应用部署

#### 开发环境
```bash
# 克隆项目
cd rag
mvn spring-boot:run
```

#### 生产环境
```bash
# 构建项目
mvn clean package -DskipTests

# 运行应用
java -jar target/rag-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --rag.spring-ai.openai.api-key=${OPENAI_API_KEY}
```

## 使用流程

### 1. 完整处理流程

```bash
# 1. 上传文件（使用upload模块）
curl -X POST http://localhost:8080/api/upload/single \
  -F "file=@document.pdf" \
  -F "uploadUserId=user123" \
  -F "tags=技术文档"

# 响应包含fileId: "file-uuid-123"

# 2. 处理文档嵌入
curl -X POST http://localhost:8080/api/rag/embedding/process \
  -H "Content-Type: application/json" \
  -d '{
    "fileId": "file-uuid-123",
    "knowledgeBaseId": "kb_tech_docs",
    "embeddingModel": "text-embedding-ada-002",
    "chunkSize": 1000,
    "chunkOverlap": 200
  }'

# 3. 使用RAG查询（使用现有查询接口）
curl -X POST http://localhost:8080/api/rag/query/simple \
  -H "Content-Type: application/json" \
  -d '{
    "question": "什么是人工智能？",
    "knowledgeBaseId": "kb_tech_docs"
  }'
```

### 2. 异步处理流程

```bash
# 1. 启动异步处理
curl -X POST http://localhost:8080/api/rag/embedding/process \
  -H "Content-Type: application/json" \
  -d '{
    "fileId": "file-uuid-123",
    "knowledgeBaseId": "kb_tech_docs",
    "processingMode": "ASYNC"
  }'

# 响应包含processingId

# 2. 查询处理状态
curl -X GET http://localhost:8080/api/rag/embedding/status/{processingId}

# 3. 如果需要，取消处理
curl -X DELETE http://localhost:8080/api/rag/embedding/cancel/{processingId}
```

## 最佳实践

### 1. 文档分块策略
- **技术文档**: chunkSize=1000, chunkOverlap=200
- **长篇文章**: chunkSize=1500, chunkOverlap=300
- **FAQ文档**: chunkSize=500, chunkOverlap=100
- **代码文档**: chunkSize=800, chunkOverlap=150

### 2. 嵌入模型选择
- **英文内容**: text-embedding-ada-002, text-embedding-3-large
- **中文内容**: bge-large-zh, m3e-base
- **多语言**: text-embedding-3-large

### 3. 知识库管理
- 按业务领域分离知识库
- 使用有意义的知识库ID
- 定期清理无用的嵌入数据
- 监控知识库大小和性能

### 4. 性能优化
- 使用批量处理接口处理大量文档
- 合理设置并发数量和超时时间
- 启用异步处理模式处理大文件
- 定期维护数据库索引

## 扩展开发

### 1. 添加新的文档读取器
```java
@Component
public class CustomDocumentReader implements DocumentReader {
    @Override
    public List<Document> read(Resource resource) {
        // 实现自定义读取逻辑
    }
}
```

### 2. 集成新的嵌入模型
```java
@Configuration
public class CustomEmbeddingConfig {
    @Bean
    @ConditionalOnProperty(name = "embedding.model.type", havingValue = "custom")
    public EmbeddingModel customEmbeddingModel() {
        return new CustomEmbeddingModel();
    }
}
```

### 3. 自定义向量存储
```java
@Service
public class CustomVectorStoreService implements VectorStoreService {
    // 实现自定义向量存储逻辑
}
```

## 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查PostgreSQL服务状态
   - 验证连接参数
   - 确认pgvector扩展已安装

2. **嵌入处理失败**
   - 检查OpenAI API密钥
   - 验证文件格式支持
   - 查看详细错误日志

3. **内存不足**
   - 调整JVM堆大小
   - 减少并发处理数量
   - 使用异步处理模式

4. **处理超时**
   - 增加超时时间配置
   - 减少文档分块大小
   - 检查网络连接

### 日志分析
```bash
# 查看嵌入处理日志
grep "DocumentEmbedding" application.log

# 查看Spring AI日志
grep "org.springframework.ai" application.log

# 查看数据库操作日志
grep "postgresql" application.log
```

## 总结

文档嵌入向量化功能为RAG系统提供了强大的知识库构建能力，通过模块化设计确保了系统的可扩展性和可维护性。该功能与upload模块无缝集成，支持多种文档格式和嵌入模型，能够满足不同场景下的知识库构建需求。 