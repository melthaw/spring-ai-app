# MapStruct 使用指南

## 概述

本项目已全面引入MapStruct来解决对象转换的问题，替代了繁琐的手动转换代码。MapStruct是一个代码生成器，在编译时自动生成类型安全、高性能的映射代码。

## 项目架构

### 依赖配置

在父项目`pom.xml`中已配置：

```xml
<properties>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
</properties>

<dependencyManagement>
    <dependencies>
        <!-- MapStruct -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct-processor</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>${mapstruct.version}</version>
                    </path>
                    <!-- Lombok和MapStruct集成 -->
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok-mapstruct-binding</artifactId>
                        <version>0.2.0</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 模块配置

每个模块的`pom.xml`中添加：

```xml
<!-- MapStruct -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <scope>provided</scope>
</dependency>
```

## 模块实现

### 1. Upload模块

#### 配置类
```java
@MapperConfig(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface MapStructConfig {
}
```

#### 转换器
```java
@Mapper(config = MapStructConfig.class)
public interface FileInfoMapper {
    
    @Mapping(target = "originalFilename", source = "originalFileName")
    @Mapping(target = "storedFilename", source = "storedFileName")
    @Mapping(target = "createdAt", source = "uploadTime")
    @Mapping(target = "updatedAt", source = "uploadTime")
    FileInfoDTO toDTO(FileInfo fileInfo);
    
    List<FileInfoDTO> toDTOList(List<FileInfo> fileInfoList);
    
    void updateFromRequest(UpdateFileInfoRequest request, @MappingTarget FileInfo fileInfo);
}
```

#### 使用示例
```java
@RestController
public class FileInfoController {
    
    private final FileInfoMapper fileInfoMapper;
    
    @GetMapping("/{fileId}")
    public ResponseEntity<FileInfoDetailDTO> getFileDetail(@PathVariable String fileId) {
        Optional<FileInfo> fileInfoOpt = fileInfoStorageService.getFileInfo(fileId);
        if (fileInfoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // 使用MapStruct转换
        FileInfoDetailDTO detailDTO = fileInfoMapper.toDetailDTO(fileInfoOpt.get());
        return ResponseEntity.ok(detailDTO);
    }
    
    @PutMapping("/{fileId}")
    public ResponseEntity<FileInfoDTO> updateFileInfo(
            @PathVariable String fileId,
            @RequestBody UpdateFileInfoRequest request) {
        
        FileInfo fileInfo = getFileInfo(fileId);
        
        // 使用MapStruct进行部分更新
        fileInfoMapper.updateFromRequest(request, fileInfo);
        
        FileInfo updatedFileInfo = fileInfoStorageService.saveFileInfo(fileInfo);
        FileInfoDTO dto = fileInfoMapper.toDTO(updatedFileInfo);
        return ResponseEntity.ok(dto);
    }
}
```

### 2. Knowledge-Base模块

#### 转换器
```java
@Mapper(config = MapStructConfig.class)
public interface DocumentNodeMapper {
    
    DocumentNodeDTO toDTO(DocumentNode documentNode);
    List<DocumentNodeDTO> toDTOList(List<DocumentNode> documentNodes);
    DocumentNodeDetailDTO toDetailDTO(DocumentNode documentNode);
    
    @Mapping(target = "children", ignore = true)
    DocumentNodeTreeDTO toTreeDTO(DocumentNode documentNode);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "knowledgeBase", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    DocumentNode fromCreateRequest(CreateDocumentNodeRequest request);
    
    void updateFromRequest(UpdateDocumentNodeRequest request, @MappingTarget DocumentNode documentNode);
}
```

#### 服务层使用
```java
@Service
public class DocumentNodeServiceImpl implements DocumentNodeService {
    
    private final DocumentNodeMapper documentNodeMapper;
    
    @Override
    public DocumentNode createDocumentNode(String kbId, CreateDocumentNodeRequest request) {
        // 使用MapStruct创建实体
        DocumentNode documentNode = documentNodeMapper.fromCreateRequest(request);
        documentNode.setKnowledgeBaseId(kbId);
        documentNode.setCreatedBy(getCurrentUserId());
        
        return documentNodeRepository.save(documentNode);
    }
    
    @Override
    public DocumentNode updateDocumentNode(String nodeId, UpdateDocumentNodeRequest request) {
        DocumentNode documentNode = getDocumentNodeEntity(nodeId);
        
        // 使用MapStruct进行部分更新
        documentNodeMapper.updateFromRequest(request, documentNode);
        documentNode.setUpdatedBy(getCurrentUserId());
        
        return documentNodeRepository.save(documentNode);
    }
    
    private DocumentNodeDetailDTO convertToDetailDTO(DocumentNode node) {
        return documentNodeMapper.toDetailDTO(node);
    }
}
```

### 3. RAG模块

#### 转换器
```java
@Mapper(config = MapStructConfig.class)
public interface QueryMapper {
    
    @Mapping(target = "processingId", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "startTime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "message", constant = "Processing started")
    DocumentEmbeddingResponse toInitialResponse(DocumentEmbeddingRequest request);
    
    @Mapping(target = "batchId", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "totalQuestions", source = "questions", qualifiedByName = "listSize")
    BatchQueryResponse toInitialBatchResponse(BatchQueryRequest request);
    
    @org.mapstruct.Named("listSize")
    default Integer getListSize(List<?> list) {
        return list != null ? list.size() : 0;
    }
    
    default void initializeBaseResponse(BaseQueryResponse response, BaseQueryRequest request) {
        response.setQueryId(java.util.UUID.randomUUID().toString());
        response.setQuestion(request.getQuestion());
        response.setQueryTime(java.time.LocalDateTime.now());
        response.setSuccess(true);
    }
}
```

#### 服务层使用
```java
@Service
public class RagQueryServiceImpl implements RagQueryService {
    
    private final QueryMapper queryMapper;
    
    @Override
    public SimpleQueryResponse simpleQuery(SimpleQueryRequest request) {
        // 执行查询逻辑...
        
        // 使用MapStruct初始化响应
        SimpleQueryResponse response = new SimpleQueryResponse();
        queryMapper.initializeBaseResponse(response, request);
        response.setQueryType("simple");
        response.setAnswer(answer);
        response.setDocuments(documents);
        
        return response;
    }
}
```

## 最佳实践

### 1. 配置统一
- 每个模块都有自己的`MapStructConfig`配置类
- 使用Spring组件模型：`componentModel = SPRING`
- 忽略未映射字段：`unmappedTargetPolicy = IGNORE`

### 2. 命名规范
- Mapper接口以`Mapper`结尾
- 转换方法使用清晰的命名：`toDTO`, `toEntity`, `fromRequest`
- 批量转换方法添加`List`后缀：`toDTOList`

### 3. 映射策略
- 使用`@Mapping`注解处理字段名不匹配
- 使用`@MappingTarget`进行部分更新
- 使用`ignore = true`忽略不需要映射的字段
- 使用`expression`进行复杂映射

### 4. 性能优化
- MapStruct生成的代码是编译时生成，运行时性能优异
- 避免在Mapper中进行复杂业务逻辑
- 使用`@Named`方法进行可重用的转换逻辑

### 5. 集成Lombok
- 项目已配置Lombok和MapStruct的集成
- 可以直接在实体类上使用`@Data`, `@Builder`等注解
- MapStruct会自动识别Lombok生成的getter/setter

## 常见问题

### 1. 编译错误
如果遇到MapStruct相关的编译错误：
```bash
mvn clean compile
```

### 2. IDE支持
- IntelliJ IDEA：安装MapStruct Support插件
- Eclipse：确保启用注解处理器

### 3. 调试生成的代码
生成的Mapper实现类位于：
```
target/generated-sources/annotations/
```

### 4. 字段映射问题
```java
// 字段名不匹配
@Mapping(target = "targetField", source = "sourceField")

// 常量值
@Mapping(target = "status", constant = "ACTIVE")

// 表达式
@Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")

// 忽略字段
@Mapping(target = "internalField", ignore = true)
```

## 扩展指南

### 添加新的转换器

1. 创建Mapper接口：
```java
@Mapper(config = MapStructConfig.class)
public interface YourEntityMapper {
    YourEntityDTO toDTO(YourEntity entity);
    YourEntity fromCreateRequest(CreateYourEntityRequest request);
    void updateFromRequest(UpdateYourEntityRequest request, @MappingTarget YourEntity entity);
}
```

2. 在服务类中注入使用：
```java
@Service
public class YourService {
    private final YourEntityMapper mapper;
    
    // 使用mapper进行转换
}
```

3. 在控制器中使用：
```java
@RestController
public class YourController {
    private final YourEntityMapper mapper;
    
    // 在API方法中使用mapper
}
```

## 总结

通过引入MapStruct，项目实现了：

1. **代码简化**：消除了大量手动转换代码
2. **类型安全**：编译时检查，避免运行时错误
3. **性能优化**：生成的代码性能优于反射方式
4. **维护性**：统一的转换逻辑，易于维护和扩展
5. **可读性**：清晰的映射配置，代码更易理解

MapStruct已成为项目中对象转换的标准解决方案，建议在后续开发中继续使用这种模式。 