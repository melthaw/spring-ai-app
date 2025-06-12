# RAG查询系统

## 概述

RAG查询系统是一个基于Spring AI框架的检索增强生成(RAG)系统，提供了全面的查询功能。系统采用模块化架构，支持多种查询模式和智能策略选择，既可以进行专门化的case-by-case查询，也可以使用all-in-one的智能查询接口。

## 系统架构

### 技术栈

- **Spring Boot 3.4.4**: 应用框架
- **Spring AI 1.0.0**: AI集成框架，提供统一的AI模型抽象层
- **Java 17**: 开发语言
- **Maven**: 项目管理和构建工具
- **Swagger/OpenAPI**: API文档生成

### 核心组件

#### 1. 控制器层 (Controller)
- **RagQueryController**: 统一的REST API入口，提供所有查询接口

#### 2. 服务层 (Service)
- **RagQueryService**: 核心查询服务接口，统一各种查询类型
- **VectorSearchService**: 向量搜索服务，基于语义相似度检索
- **KeywordSearchService**: 关键词搜索服务，基于文本匹配
- **HybridSearchService**: 混合搜索服务，结合向量和关键词搜索
- **StructuredSearchService**: 结构化搜索服务，支持过滤和排序
- **IntelligentSearchService**: 智能搜索服务，自动策略选择
- **AnswerGenerationService**: 答案生成服务，基于检索结果生成回答
- **SummaryGenerationService**: 摘要生成服务，对内容进行摘要
- **DocumentRerankService**: 文档重排服务，优化检索结果排序
- **RagAssistantService**: RAG助手服务，提供辅助功能

#### 3. 领域层 (Domain)
- **BaseQueryRequest/Response**: 查询请求和响应的基础类
- **DocumentSegment**: 文档片段模型，包含内容和元数据
- **Citation**: 引用信息模型
- **ConversationMessage**: 对话消息模型
- 各种专门的请求/响应对象 (SimpleQuery, SemanticQuery, HybridQuery等)

#### 4. 文档读取层 (Reader) 🆕
- **DocumentReader**: 文档读取器统一接口，支持策略模式
- **DocumentReaderFactory**: 读取器工厂，根据文件类型自动选择合适的读取器
- **ReaderConfig**: 文档读取配置类，支持20+参数精细控制读取行为
- **PdfDocumentReader**: PDF文档读取器，支持表格、图片、注释提取
- **TextDocumentReader**: 文本文档读取器，支持txt、md格式和格式保持
- **JsonDocumentReader**: JSON文档读取器，支持JSONPath和数据扁平化
- **TikaDocumentReader**: Office文档读取器，支持doc、docx、ppt、xlsx等多种格式

#### 5. 嵌入向量层 (Embedding) 🆕
- **DocumentEmbeddingService**: 文档嵌入服务，整合文档读取、向量化和存储
- **EmbeddingService**: 向量化服务，支持多种嵌入模型
- **VectorStoreService**: 向量存储服务，基于PGVector的数据库操作
- **DocumentEmbeddingController**: 嵌入功能REST API控制器

#### 6. 配置层 (Config)
- **SpringAiConfig**: Spring AI配置类，集成AI模型和向量数据库

## 功能特性

### 🆕 文档读取与嵌入功能

#### 文档读取器架构重构
采用策略模式将文档读取逻辑模块化，支持多种文件格式和丰富的读取参数：

**支持的文件格式**：
- **PDF文档** (pdf) - 支持表格提取、图片提取、注释提取
- **文本文档** (txt, md, text, markdown) - 支持格式保持、Markdown特殊处理
- **JSON数据** (json, jsonl, ndjson) - 支持JSONPath、JSON扁平化
- **Office文档** (doc, docx, ppt, pptx, xls, xlsx) - 支持公式提取、注释提取
- **网页文档** (html, htm, xml) - 结构化解析
- **其他格式** (csv, rtf, odt, ods, odp) - 通用文档处理

**丰富的配置参数**：
```java
// PDF配置示例
ReaderConfig pdfConfig = ReaderConfig.pdfConfig()
    .setExtractTables(true)        // 提取表格
    .setExtractImages(false)       // 不提取图片
    .setPdfPageLimit(1000)         // 页数限制
    .setLanguage("zh")             // 中文处理
    .setMaxContentLength(1024*1024); // 内容长度限制

// 文本配置示例
ReaderConfig textConfig = ReaderConfig.textConfig()
    .setPreserveFormatting(false)  // 不保持格式
    .setChunkSize(1000)            // 分块大小
    .setEnableChunking(true)       // 启用分块
    .setRemoveWhitespace(true);    // 清理空白字符

// JSON配置示例
ReaderConfig jsonConfig = ReaderConfig.jsonConfig()
    .setJsonPath("$.content")      // JSONPath提取
    .setFlattenJson(true)          // JSON扁平化
    .setJsonDepthLimit(5);         // 深度限制
```

#### 嵌入向量功能
集成文档读取与向量化存储的完整流程：

**核心API接口**：
- `/api/embedding/process` - 文档嵌入处理
- `/api/embedding/batch` - 批量文档处理
- `/api/embedding/status/{id}` - 处理状态查询
- `/api/embedding/supported-types` - 支持的文件类型
- `/api/embedding/supported-models` - 支持的嵌入模型

**使用示例**：
```bash
# 处理单个文档
curl -X POST http://localhost:8080/api/embedding/process \
  -H "Content-Type: application/json" \
  -d '{
    "fileId": "file_123",
    "knowledgeBaseId": "kb_001",
    "embeddingModel": "text-embedding-ada-002",
    "chunkSize": 500,
    "chunkOverlap": 50,
    "processingMode": "SYNC"
  }'

# 批量处理文档
curl -X POST http://localhost:8080/api/embedding/batch \
  -H "Content-Type: application/json" \
  -d '{
    "requests": [
      {"fileId": "file_1", "knowledgeBaseId": "kb_001"},
      {"fileId": "file_2", "knowledgeBaseId": "kb_001"}
    ],
    "embeddingModel": "text-embedding-ada-002",
    "processingMode": "ASYNC"
  }'
```

### Case by Case 查询接口

#### 1. 简单查询 (`/api/rag/query/simple`)
- **功能**: 基于单个知识库的基础问答
- **特点**: 支持温度参数和token限制，适合简单的QA场景
- **参数**: 知识库ID、相似度阈值、返回数量限制等

#### 2. 多知识库查询 (`/api/rag/query/multi-knowledge-base`)
- **功能**: 跨多个知识库进行查询
- **特点**: 并行查询多个知识库，提供综合答案
- **参数**: 多个知识库ID列表、权重配置等

#### 3. 语义查询 (`/api/rag/query/semantic`)
- **功能**: 基于向量相似度的文档检索
- **特点**: 纯语义搜索，支持重排序功能
- **参数**: 嵌入模型选择、重排序开关等

#### 4. 混合查询 (`/api/rag/query/hybrid`)
- **功能**: 结合关键词和语义的混合检索
- **特点**: 平衡精确匹配和语义理解
- **参数**: 关键词权重、语义权重、重排序配置等

#### 5. 对话式查询 (`/api/rag/query/conversational`)
- **功能**: 支持上下文的多轮对话
- **特点**: 自动维护对话历史，支持上下文理解
- **参数**: 对话历史、最大历史长度等

#### 6. 结构化查询 (`/api/rag/query/structured`)
- **功能**: 支持过滤条件的结构化检索
- **特点**: 支持字段过滤、排序、分组等
- **参数**: 过滤条件、排序字段、聚合选项等

#### 7. 摘要查询 (`/api/rag/query/summary`)
- **功能**: 对检索结果进行摘要生成
- **特点**: 支持不同摘要类型和长度控制
- **参数**: 摘要类型、摘要长度、关键词提取等

#### 8. 引用查询 (`/api/rag/query/citation`)
- **功能**: 提供详细的引用信息和来源
- **特点**: 支持多种引用格式，追踪信息来源
- **参数**: 引用格式、详细级别等

### All in One 查询接口

#### 1. 智能查询 (`/api/rag/query/intelligent`)
- **功能**: 一体化查询接口，自动选择最佳查询策略
- **特点**: 
  - 自动意图识别
  - 策略自动优化
  - 多种查询方式融合
  - 智能参数调优
- **优势**: 用户无需了解底层复杂性，一个接口解决所有查询需求

#### 2. 批量查询 (`/api/rag/query/batch`)
- **功能**: 支持批量问题的并行处理
- **特点**: 高效的批量处理，支持并行和顺序两种模式
- **参数**: 问题列表、处理模式、并发度等

### 辅助查询接口

#### 1. 查询建议 (`/api/rag/query/suggestions`)
- **功能**: 基于输入提供查询建议
- **特点**: 智能补全和查询优化建议

#### 2. 相关查询 (`/api/rag/query/related`)
- **功能**: 基于当前查询推荐相关问题
- **特点**: 扩展用户查询思路

#### 3. 查询历史 (`/api/rag/query/history`)
- **功能**: 获取用户查询历史
- **特点**: 支持历史记录管理和分析

## 配置说明

### 应用配置
目前系统使用Mock实现进行开发和测试，生产环境可配置真实的AI模型：

```yaml
rag:
  spring-ai:
    openai:
      api-key: ${OPENAI_API_KEY:}
      base-url: https://api.openai.com
      embedding-model: text-embedding-ada-002
      chat-model: gpt-3.5-turbo
    vectorstore:
      type: chroma  # 支持 chroma, pinecone, redis
      chroma:
        url: http://localhost:8000
        collection-name: rag_documents

logging:
  level:
    cn.mojoup.ai.rag: DEBUG
```

### 环境变量
- `OPENAI_API_KEY`: OpenAI API密钥（可选，未配置时使用Mock实现）
- `PINECONE_API_KEY`: Pinecone API密钥（使用Pinecone时）
- `REDIS_URL`: Redis连接URL（使用Redis向量存储时）

## API使用示例

### 简单查询
```bash
curl -X POST http://localhost:8080/api/rag/query/simple \
  -H "Content-Type: application/json" \
  -d '{
    "question": "什么是人工智能？",
    "knowledgeBaseId": "kb_001",
    "temperature": 0.7,
    "maxTokens": 2000,
    "limit": 10,
    "similarityThreshold": 0.7,
    "includeMetadata": true,
    "includeContent": true
  }'
```

### 智能查询（推荐使用）
```bash
curl -X POST http://localhost:8080/api/rag/query/intelligent \
  -H "Content-Type: application/json" \
  -d '{
    "question": "比较深度学习和机器学习的区别",
    "knowledgeBaseIds": ["kb_001", "kb_002"],
    "autoOptimize": true,
    "enableMultiModal": true,
    "enableRerank": true,
    "temperature": 0.7,
    "maxTokens": 2000
  }'
```

### 混合查询
```bash
curl -X POST http://localhost:8080/api/rag/query/hybrid \
  -H "Content-Type: application/json" \
  -d '{
    "question": "神经网络架构设计",
    "knowledgeBaseId": "kb_001",
    "keywords": ["CNN", "RNN", "Transformer"],
    "keywordWeight": 0.3,
    "semanticWeight": 0.7,
    "enableRerank": true,
    "limit": 15,
    "similarityThreshold": 0.6
  }'
```

### 对话式查询
```bash
curl -X POST http://localhost:8080/api/rag/query/conversational \
  -H "Content-Type: application/json" \
  -d '{
    "question": "那么神经网络呢？",
    "knowledgeBaseId": "kb_001",
    "conversationHistory": [
      {
        "role": "user",
        "content": "什么是深度学习？",
        "timestamp": "2025-06-12T10:00:00"
      },
      {
        "role": "assistant", 
        "content": "深度学习是机器学习的一个子领域...",
        "timestamp": "2025-06-12T10:00:01"
      }
    ],
    "maxHistoryLength": 10,
    "contextWeight": 0.5
  }'
```

## 响应格式

### 统一响应结构
```json
{
  "queryId": "uuid-string",
  "question": "用户输入的问题",
  "queryTime": "2025-06-12T10:00:00",
  "processingTime": 1500,
  "queryType": "simple|semantic|hybrid|...",
  "success": true,
  "errorMessage": null,
  "metadata": {}
}
```

### 简单查询响应
```json
{
  "queryId": "abc-123",
  "question": "什么是人工智能？",
  "answer": "人工智能是计算机科学的一个分支...",
  "documents": [
    {
      "segmentId": "seg_001",
      "documentId": "doc_001",
      "content": "文档内容片段",
      "score": 0.95,
      "title": "AI基础介绍",
      "source": "textbook_ai.pdf",
      "metadata": {}
    }
  ],
  "knowledgeBaseId": "kb_001",
  "model": "qwen-max",
  "tokensUsed": 150,
  "queryTime": "2025-06-12T10:00:00",
  "processingTime": 1200,
  "success": true
}
```

### 智能查询响应
```json
{
  "queryId": "def-456",
  "question": "比较深度学习和机器学习",
  "answer": "深度学习和机器学习的主要区别在于...",
  "selectedStrategy": "hybrid_multi_kb",
  "strategiesConsidered": ["simple", "semantic", "hybrid"],
  "optimizations": ["rerank", "multi_modal"],
  "knowledgeBaseScores": {
    "kb_001": 0.85,
    "kb_002": 0.72
  },
  "documents": [...],
  "processingTime": 2300,
  "success": true
}
```

## 部署说明

### 开发环境
1. 确保Java 17已安装
2. 克隆项目并进入rag目录
3. 运行应用：`mvn spring-boot:run`
4. 访问API文档：http://localhost:8080/swagger-ui.html

### Mock模式（默认）
系统默认使用Mock实现，无需配置外部依赖即可运行和测试所有功能。

### 生产环境配置
1. **配置AI模型**：
   - 设置OpenAI API密钥
   - 或集成其他支持的AI模型

2. **配置向量数据库**：
   - Chroma: 启动Chroma服务
   - Pinecone: 配置Pinecone账户
   - Redis: 部署Redis Stack

3. **性能优化**：
   - 调整JVM参数
   - 配置连接池
   - 启用缓存机制

## 代码结构

```
rag/
├── src/main/java/cn/mojoup/ai/rag/
│   ├── controller/
│   │   ├── RagQueryController.java          # RAG查询REST API控制器
│   │   └── DocumentEmbeddingController.java # 文档嵌入REST API控制器 🆕
│   ├── service/
│   │   ├── RagQueryService.java             # 核心查询服务接口
│   │   ├── VectorSearchService.java         # 向量搜索服务
│   │   ├── KeywordSearchService.java        # 关键词搜索服务
│   │   ├── HybridSearchService.java         # 混合搜索服务
│   │   ├── StructuredSearchService.java     # 结构化搜索服务
│   │   ├── IntelligentSearchService.java    # 智能搜索服务
│   │   ├── AnswerGenerationService.java     # 答案生成服务
│   │   ├── SummaryGenerationService.java    # 摘要生成服务
│   │   ├── DocumentRerankService.java       # 文档重排服务
│   │   ├── RagAssistantService.java         # RAG助手服务
│   │   ├── DocumentReaderService.java       # 文档读取服务接口 🆕
│   │   ├── DocumentEmbeddingService.java    # 文档嵌入服务接口 🆕
│   │   ├── EmbeddingService.java            # 向量化服务接口 🆕
│   │   ├── VectorStoreService.java          # 向量存储服务接口 🆕
│   │   └── impl/                            # 所有服务的实现类
│   ├── reader/                              # 文档读取器包 🆕
│   │   ├── DocumentReader.java              # 文档读取器统一接口
│   │   ├── ReaderConfig.java                # 读取配置类
│   │   ├── DocumentReaderFactory.java       # 读取器工厂
│   │   └── impl/
│   │       ├── PdfDocumentReader.java       # PDF文档读取器
│   │       ├── TextDocumentReader.java      # 文本文档读取器
│   │       ├── JsonDocumentReader.java      # JSON文档读取器
│   │       └── TikaDocumentReader.java      # Office文档读取器
│   ├── domain/
│   │   ├── BaseQueryRequest.java            # 基础请求类
│   │   ├── BaseQueryResponse.java           # 基础响应类
│   │   ├── DocumentSegment.java             # 文档片段模型
│   │   ├── Citation.java                    # 引用信息模型
│   │   ├── DocumentEmbeddingRequest.java    # 文档嵌入请求类 🆕
│   │   ├── DocumentEmbeddingResponse.java   # 文档嵌入响应类 🆕
│   │   └── ...                              # 各种专门的请求/响应对象
│   ├── config/
│   │   └── SpringAiConfig.java              # Spring AI配置
│   └── exception/
│       └── RagException.java                # 异常定义
├── EMBEDDING_GUIDE.md                       # 嵌入功能详细指南 🆕
├── READER_ARCHITECTURE.md                   # 读取器架构说明 🆕
└── pom.xml                                  # Maven配置
```

## 扩展和定制

### 添加新的查询类型
1. 在`domain`包中定义新的请求/响应类
2. 在`RagQueryService`接口中添加新方法
3. 在`RagQueryServiceImpl`中实现具体逻辑
4. 在`RagQueryController`中添加REST端点

### 集成新的AI模型
1. 实现Spring AI的模型接口
2. 在`SpringAiConfig`中注册新模型
3. 更新服务实现以支持新模型

### 添加新的向量数据库
1. 实现Spring AI的VectorStore接口
2. 在配置中添加相应的Bean定义
3. 更新文档和配置说明

## 最佳实践

1. **查询优化**：
   - 优先使用智能查询接口
   - 合理设置相似度阈值
   - 根据场景选择合适的limit参数

2. **性能考虑**：
   - 批量查询适合处理大量问题
   - 合理使用缓存机制
   - 监控查询响应时间

3. **错误处理**：
   - 检查响应中的success字段
   - 处理可能的异常情况
   - 实现重试机制

## 🎉 最新功能更新

### v2.0.0 - 文档读取与嵌入功能重构

#### ✅ 已完成功能

**1. 文档读取器架构重构**
- 📁 采用策略模式将文档读取逻辑模块化
- 🔌 创建统一的DocumentReader接口
- ⚙️ 实现功能丰富的ReaderConfig配置系统
- 🏭 建立DocumentReaderFactory工厂管理模式

**2. 多格式文档读取器**
- 📄 `PdfDocumentReader` - 支持表格/图片/注释提取
- 📝 `TextDocumentReader` - 支持Markdown格式和格式保持
- 📊 `JsonDocumentReader` - 支持JSONPath和数据扁平化
- 🗂️ `TikaDocumentReader` - 支持13+种Office文档格式

**3. 嵌入向量功能**
- 🔗 完整的文档→向量化→存储流程
- 🎛️ 11个REST API接口（同步/异步/批量处理）
- 📈 状态跟踪和进度监控
- 🗄️ PGVector数据库集成
- 🧠 支持5+主流嵌入模型

**4. 丰富的配置参数**
- 🎯 20+配置参数精细控制读取行为
- 🔧 支持链式配置和预设配置模板
- 📏 分块处理、内容清理、格式化控制
- 🌍 多语言和多编码支持

#### 🎯 架构优势

1. **📈 高度可扩展** - 新增文件类型只需实现接口
2. **⚙️ 精细可配** - 20+参数控制读取和处理行为
3. **🔧 易于维护** - 每种文件类型独立维护
4. **🛡️ 类型安全** - 编译时检查文件类型支持
5. **⚡ 性能优化** - 针对性的读取策略和批量处理

#### 📊 支持能力

| 类别 | 支持格式 | 数量 | 特殊功能 |
|------|---------|------|---------|
| 文档 | PDF, DOC, DOCX, PPT, PPTX | 5种 | 表格/公式/注释提取 |
| 文本 | TXT, MD, HTML, XML | 4种 | 格式保持/Markdown解析 |
| 数据 | JSON, CSV, XLS, XLSX | 4种 | JSONPath/数据扁平化 |
| 其他 | RTF, ODT, ODS, ODP | 4种 | 通用文档处理 |
| **总计** | **17种文件格式** | **17种** | **全面覆盖主流格式** |

#### 🚀 使用便利性

```java
// 一行代码读取任意格式文档
List<Document> docs = documentReaderService.readDocumentsWithOptimizedConfig(fileInfo);

// 灵活配置读取参数
ReaderConfig config = ReaderConfig.pdfConfig()
    .setExtractTables(true)
    .setChunkSize(1000)
    .setLanguage("zh");

// 自动选择最佳读取器
DocumentReader reader = readerFactory.getReader("pdf").get();
```

## 开发计划

### 已完成 ✅
- [x] **文档读取器架构重构** - 策略模式，支持17种文件格式
- [x] **嵌入向量功能** - 完整的文档向量化流程
- [x] **PGVector集成** - 向量数据库支持
- [x] **丰富的配置系统** - 20+参数精细控制
- [x] **批量处理支持** - 异步和同步处理模式

### 进行中 🚧
- [ ] 集成更多AI模型（本地模型、其他云服务）
- [ ] 支持更多向量数据库
- [ ] 实现查询缓存机制
- [ ] 添加性能监控和指标

### 计划中 📋
- [ ] 支持流式响应
- [ ] 实现查询结果的可解释性
- [ ] 添加多语言支持
- [ ] 实现查询意图分析
- [ ] 支持知识图谱增强
- [ ] 文档分块策略优化
- [ ] 嵌入模型微调支持

```
export GOOSE_DRIVER=postgres
export GOOSE_DBSTRING=postgres://postgres:xzaGkaLAMw8AyYD232@180.76.111.87:5432/mojoup
export GOOSE_MIGRATION_DIR=db/
```

### 创建db升级sql

goose create check_replication_task sql -dir db/

### 执行升级

goose up -dir db/

### 执行降级

goose down -dir db/
goose down-to 20170506082527 -dir db/