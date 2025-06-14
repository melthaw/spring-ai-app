![](./cover.jpeg)

# Spring AI RAG 应用

> 声明：
> 本项目的主体代码都是由 AI 生成，主体代码大概消耗了50个cursor fast request。
> 作者只对生成的代码进行重构粘合，并确保代码在Spring AI 1.0.0 上可以正确运行。

## 📚 项目概述

这是一个基于Spring AI 1.0.0的检索增强生成(RAG)应用，提供了完整的文档处理、向量化和智能查询功能。项目采用模块化设计，支持多种文档格式和查询模式，可以灵活应对不同的业务场景。

## 🏗️ 系统架构

### 核心模块

1. **RAG模块** (`/rag`)
   - 文档读取与处理
   - 向量化与存储
   - 智能查询服务
   - 答案生成

2. **上传模块** (`/upload`)
   - 文件上传管理
   - 文件元数据管理
   - 文件存储服务

### 技术栈

- **Spring Boot 3.4.4**: 应用框架
- **Spring AI 1.0.0**: AI集成框架
- **Java 17**: 开发语言
- **PostgreSQL + pgvector**: 向量数据库
- **Docker**: 容器化部署
- **Maven**: 项目管理和构建工具

## 🚀 主要功能

### 1. 文档处理

#### 支持的文档类型
- PDF文档 (支持表格、图片、注释提取)
- Office文档 (Word, Excel, PowerPoint)
- 文本文档 (TXT, MD)
- JSON文档
- 网页文档 (HTML, XML)
- 其他格式 (CSV, RTF等)

#### 文档读取特性
- 智能文件类型识别
- 自动内容清理
- 元数据保留
- 分块处理
- 格式保持

### 2. 向量化处理

#### 支持的嵌入模型
- OpenAI: text-embedding-ada-002
- 中文模型: bge-large-zh, m3e-base

#### 向量化特性
- 批量处理
- 异步处理
- 任务状态追踪
- 错误重试
- 知识库隔离

### 3. 智能查询

#### 查询模式
- 简单查询
- 语义查询
- 混合查询
- 结构化查询
- 对话式查询
- 摘要查询
- 引用查询

#### 查询特性
- 自动意图识别
- 策略自动优化
- 多种查询方式融合
- 智能参数调优

## 🛠️ 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 14+

### 1. 克隆项目
```bash
git clone https://github.com/yourusername/spring-ai-app.git
cd spring-ai-app
```

### 2. 配置环境变量
```bash
# .env
OPENAI_API_KEY=your_api_key
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=suiyue_rag
```

### 3. 启动服务
```bash
# 启动数据库
docker-compose up -d postgres

# 启动应用
mvn spring-boot:run
```

### 4. 访问API
- Swagger UI: http://localhost:8080/swagger-ui.html
- API文档: http://localhost:8080/v3/api-docs

## 📖 详细文档

- [RAG模块文档](rag/README.md)
- [文档读取器架构](rag/READER_ARCHITECTURE.md)
- [向量化功能指南](rag/EMBEDDING_GUIDE.md)
- [数据库集成说明](upload/DATABASE_INTEGRATION.md)

## 🔧 配置说明

### 应用配置
```yaml
rag:
  spring-ai:
    openai:
      api-key: ${OPENAI_API_KEY:}
      base-url: https://api.openai.com
      embedding-model: text-embedding-ada-002
      chat-model: gpt-3.5-turbo
    vectorstore:
      type: pgvector
      pgvector:
        schema-name: public
        table-name: vector_store
        dimensions: 1536

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/suiyue_rag
    username: ${POSTGRES_USER:postgres}
    password: ${POSTGRES_PASSWORD:postgres}
```

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 📝 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 🙏 致谢

- [Spring AI](https://github.com/spring-projects/spring-ai)
- [OpenAI](https://openai.com)
- [pgvector](https://github.com/pgvector/pgvector)

