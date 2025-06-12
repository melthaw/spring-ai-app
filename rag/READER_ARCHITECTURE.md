# 文档读取器架构重构说明

## 📋 概述

将原有的`DocumentReaderServiceImpl`中的文档读取逻辑拆分为独立的读取器类，采用策略模式设计，便于扩展和维护。

## 🏗️ 架构设计

### 核心组件

```
rag/src/main/java/cn/mojoup/ai/rag/reader/
├── DocumentReader.java          # 文档读取器接口
├── ReaderConfig.java           # 读取配置类
├── DocumentReaderFactory.java  # 读取器工厂
└── impl/
    ├── PdfDocumentReader.java      # PDF文档读取器
    ├── TextDocumentReader.java     # 文本文档读取器
    ├── JsonDocumentReader.java     # JSON文档读取器
    └── TikaDocumentReader.java     # Office文档读取器
```

### 设计模式

- **策略模式**: 不同文件类型使用不同的读取策略
- **工厂模式**: 根据文件扩展名自动选择合适的读取器
- **建造者模式**: ReaderConfig支持链式配置

## 🛠️ 核心接口

### DocumentReader 接口

```java
public interface DocumentReader {
    List<Document> read(Resource resource, ReaderConfig config);
    boolean supports(String extension);
    List<String> getSupportedExtensions();
    String getReaderType();
}
```

### ReaderConfig 配置类

支持的主要配置参数：

#### 通用配置
- `charset`: 字符编码
- `maxContentLength`: 最大内容长度
- `language`: 语言设置
- `extractMetadata`: 是否提取元数据

#### PDF特定配置
- `extractTables`: 是否提取表格
- `extractImages`: 是否提取图片
- `extractAnnotations`: 是否提取注释
- `pdfPageLimit`: 页数限制

#### 文本文件配置
- `preserveFormatting`: 是否保持格式
- `lineEnding`: 行结束符

#### JSON配置
- `jsonPath`: JSON路径
- `flattenJson`: 是否扁平化JSON
- `jsonDepthLimit`: 深度限制

#### Office文档配置
- `extractFormulas`: 是否提取公式
- `extractComments`: 是否提取注释
- `extractHiddenText`: 是否提取隐藏文本
- `parseEmbeddedDocuments`: 是否解析嵌入文档

#### 分块配置
- `chunkSize`: 分块大小
- `chunkOverlap`: 分块重叠
- `enableChunking`: 是否启用分块

#### 清理配置
- `removeWhitespace`: 移除多余空白
- `removeEmptyLines`: 移除空行
- `normalizeUnicode`: Unicode标准化

## 🚀 使用示例

### 基础使用

```java
@Autowired
private DocumentReaderFactory readerFactory;

// 获取读取器
Optional<DocumentReader> reader = readerFactory.getReader("pdf");
if (reader.isPresent()) {
    ReaderConfig config = ReaderConfig.pdfConfig()
        .setExtractTables(true)
        .setExtractImages(false);
    
    List<Document> documents = reader.get().read(resource, config);
}
```

### 在Service中使用

```java
@Autowired
private DocumentReaderServiceImpl documentReaderService;

// 使用默认配置
List<Document> docs1 = documentReaderService.readDocuments(fileInfo);

// 使用自定义配置
ReaderConfig config = ReaderConfig.textConfig()
    .setChunkSize(1000)
    .setEnableChunking(true);
List<Document> docs2 = documentReaderService.readDocuments(fileInfo, config);

// 使用优化配置
List<Document> docs3 = documentReaderService.readDocumentsWithOptimizedConfig(fileInfo);
```

### 配置示例

#### PDF文档配置
```java
ReaderConfig pdfConfig = ReaderConfig.pdfConfig()
    .setExtractTables(true)
    .setExtractImages(false)
    .setPdfPageLimit(1000)
    .setLanguage("zh")
    .setMaxContentLength(1024 * 1024);
```

#### 文本文档配置
```java
ReaderConfig textConfig = ReaderConfig.textConfig()
    .setPreserveFormatting(false)
    .setChunkSize(1000)
    .setChunkOverlap(100)
    .setEnableChunking(true)
    .setRemoveWhitespace(true);
```

#### JSON文档配置
```java
ReaderConfig jsonConfig = ReaderConfig.jsonConfig()
    .setJsonPath("$.content")
    .setFlattenJson(true)
    .setJsonDepthLimit(5);
```

#### Office文档配置
```java
ReaderConfig officeConfig = ReaderConfig.officeConfig()
    .setExtractFormulas(true)
    .setExtractComments(false)
    .setParseEmbeddedDocuments(false);
```

## 📊 支持的文件类型

### PDF文档
- **扩展名**: `pdf`
- **读取器**: `PdfDocumentReader`
- **特性**: 支持表格提取、图片提取、注释提取

### 文本文档
- **扩展名**: `txt`, `md`, `text`, `markdown`
- **读取器**: `TextDocumentReader`
- **特性**: 支持格式保持、Markdown特殊处理

### JSON文档
- **扩展名**: `json`, `jsonl`, `ndjson`
- **读取器**: `JsonDocumentReader`
- **特性**: 支持JSONPath、JSON扁平化

### Office文档
- **扩展名**: `doc`, `docx`, `ppt`, `pptx`, `xls`, `xlsx`, `html`, `htm`, `xml`, `csv`, `rtf`, `odt`, `ods`, `odp`
- **读取器**: `TikaDocumentReader`
- **特性**: 支持公式提取、注释提取、嵌入文档解析

## 🔧 扩展指南

### 添加新的文档读取器

1. **实现DocumentReader接口**
```java
@Component
public class CustomDocumentReader implements DocumentReader {
    @Override
    public List<Document> read(Resource resource, ReaderConfig config) {
        // 实现读取逻辑
    }
    
    @Override
    public boolean supports(String extension) {
        return "custom".equals(extension.toLowerCase());
    }
    
    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList("custom");
    }
    
    @Override
    public String getReaderType() {
        return "CUSTOM";
    }
}
```

2. **Spring会自动注册**
Spring Boot会自动扫描并注册新的DocumentReader实现到DocumentReaderFactory中。

### 添加新的配置参数

1. **在ReaderConfig中添加属性**
```java
private boolean customFeature = false;

public boolean isCustomFeature() { return customFeature; }
public ReaderConfig setCustomFeature(boolean customFeature) { 
    this.customFeature = customFeature; 
    return this; 
}
```

2. **在读取器中使用配置**
```java
if (config.isCustomFeature()) {
    // 执行自定义功能
}
```

## 🎯 优势

1. **可扩展性**: 新增文件类型只需实现DocumentReader接口
2. **可配置性**: 丰富的配置参数控制读取行为
3. **可维护性**: 每种文件类型独立维护
4. **类型安全**: 编译时检查文件类型支持
5. **性能优化**: 针对不同文件类型优化读取策略

## 📈 性能考虑

- 大文件支持内容长度限制
- 分块处理减少内存占用
- 延迟加载避免不必要的处理
- 缓存读取器实例提高性能

## 🔒 错误处理

- 统一的异常处理机制
- 详细的错误日志记录
- 优雅的降级策略
- 文件类型验证

## 📝 更新历史

- **v1.0.0**: 初始架构重构
- 将单一的DocumentReaderServiceImpl拆分为多个专门的读取器
- 添加ReaderConfig配置系统
- 实现DocumentReaderFactory工厂模式 