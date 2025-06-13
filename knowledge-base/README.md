# Knowledge Base Module

## 概述

知识库模块是一个企业级的知识管理系统，支持树形结构的知识库组织，整合了文件上传和RAG（检索增强生成）功能。

## 功能特性

### 🏗️ 核心架构
- **三层树形结构**: 知识库 → 分类 → 文档
- **模块化设计**: 整合upload和rag模块的能力
- **企业级功能**: 支持多用户、权限控制、访问级别管理

### 📁 知识库管理
- **知识库创建**: 支持个人、团队、组织三种类型
- **访问控制**: 私有、内部、公开三种访问级别
- **状态管理**: 活跃、归档、删除状态管理
- **统计信息**: 文档数量、存储大小、处理状态统计

### 🗂️ 分类管理（树形结构）
- **无限层级**: 支持任意深度的分类嵌套
- **路径管理**: 自动维护分类路径和层级关系
- **排序支持**: 支持分类的自定义排序
- **递归操作**: 支持递归查询和操作子分类

### 📄 文档管理
- **多格式支持**: PDF、Word、Excel、PPT、图片、音视频等
- **上传集成**: 调用upload模块进行文件存储
- **嵌入处理**: 调用rag模块进行文档嵌入
- **状态跟踪**: 完整的文档处理状态跟踪

### 🧠 嵌入管理
- **多模型支持**: 支持不同的嵌入模型
- **参数配置**: 可配置分块策略、大小、重叠等参数
- **结果存储**: 详细记录嵌入过程和结果
- **重试机制**: 支持失败重试和重新处理

## API接口

### 知识库管理 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/knowledge-base` | 创建知识库 |
| GET | `/api/knowledge-base/{id}` | 获取知识库详情 |
| PUT | `/api/knowledge-base/{id}` | 更新知识库 |
| DELETE | `/api/knowledge-base/{id}` | 删除知识库 |
| GET | `/api/knowledge-base/user/{userId}` | 获取用户知识库列表 |
| GET | `/api/knowledge-base/search` | 搜索知识库 |

### 文档管理 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/knowledge-base/documents/upload` | 上传文档 |
| POST | `/api/knowledge-base/documents/upload/batch` | 批量上传 |
| GET | `/api/knowledge-base/documents/{id}` | 获取文档详情 |
| DELETE | `/api/knowledge-base/documents/{id}` | 删除文档 |
| GET | `/api/knowledge-base/documents/category/{categoryId}` | 获取分类下的文档 |
| POST | `/api/knowledge-base/documents/{id}/reprocess` | 重新处理文档 |
| GET | `/api/knowledge-base/documents/{id}/embeddings` | 获取文档嵌入 |

## 使用示例

### 1. 创建知识库

```bash
curl -X POST http://localhost:8080/api/knowledge-base \
  -H "Content-Type: application/json" \
  -d '{
    "kbCode": "tech-docs",
    "kbName": "技术文档库",
    "description": "技术相关文档的知识库",
    "ownerId": "user123",
    "kbType": "TEAM",
    "accessLevel": "INTERNAL"
  }'
```

### 2. 上传文档

```bash
curl -X POST http://localhost:8080/api/knowledge-base/documents/upload \
  -F "file=@document.pdf" \
  -F "title=技术规范文档" \
  -F "categoryId=1" \
  -F "uploadUserId=user123" \
  -F "immediateEmbedding=true" \
  -F "embeddingModel=text-embedding-ada-002"
```

### 3. 搜索文档

```bash
curl "http://localhost:8080/api/knowledge-base/documents/search?keyword=API&page=0&size=10"
```

## 模块集成

### 与upload模块集成
- 文件上传和存储
- 文件管理和下载
- 文件类型验证

### 与rag模块集成  
- 文档内容解析
- 文本嵌入处理
- 向量数据存储