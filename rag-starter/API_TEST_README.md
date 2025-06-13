# Spring AI App API 测试脚本

本目录包含了完整的 API 测试脚本，用于测试 Spring AI App 的各个模块功能。

## 📁 文件结构

```
├── upload_api_test.sh              # 文件上传模块测试脚本
├── rag_query_api_test.sh           # RAG查询模块测试脚本
├── document_embedding_api_test.sh  # 文档嵌入模块测试脚本
├── run_all_api_tests.sh           # 综合测试脚本
└── API_TEST_README.md             # 本说明文档
```

## 🚀 快速开始

### 1. 环境准备

确保已安装以下工具：

```bash
# macOS
brew install curl jq

# Ubuntu/Debian
sudo apt-get install curl jq

# CentOS/RHEL
sudo yum install curl jq
```

### 2. 启动应用

```bash
# 在项目根目录启动应用
mvn spring-boot:run

# 或者分别启动各个模块
cd upload && mvn spring-boot:run
cd rag && mvn spring-boot:run
```

### 3. 运行测试

#### 运行所有测试
```bash
chmod +x run_all_api_tests.sh
./run_all_api_tests.sh
```

#### 运行特定模块测试
```bash
# 只测试文件上传模块
./run_all_api_tests.sh upload

# 只测试RAG查询模块
./run_all_api_tests.sh rag

# 只测试文档嵌入模块
./run_all_api_tests.sh embedding

# 只运行集成测试
./run_all_api_tests.sh integration
```

#### 单独运行模块测试
```bash
# 文件上传模块
chmod +x upload_api_test.sh
./upload_api_test.sh

# RAG查询模块
chmod +x rag_query_api_test.sh
./rag_query_api_test.sh

# 文档嵌入模块
chmod +x document_embedding_api_test.sh
./document_embedding_api_test.sh
```

## 📋 测试内容

### 1. 文件上传模块测试 (`upload_api_test.sh`)

测试以下功能：
- ✅ 单个文件上传
- ✅ 多个文件上传
- ✅ 根据文件ID获取文件信息
- ✅ 根据文件ID检查文件存在性
- ✅ 根据文件ID获取文件URL
- ✅ 根据文件ID下载文件（内联/附件模式）
- ✅ 根据文件ID删除文件
- ✅ 错误处理测试

**认证要求**: HTTP Basic 认证
- 用户名: `admin`
- 密码: `admin123`

### 2. RAG查询模块测试 (`rag_query_api_test.sh`)

测试以下功能：

#### Case by Case 查询接口
- ✅ 简单查询
- ✅ 多知识库查询
- ✅ 语义查询
- ✅ 混合查询
- ✅ 对话式查询
- ✅ 结构化查询
- ✅ 摘要查询
- ✅ 引用查询

#### All in One 查询接口
- ✅ 智能查询
- ✅ 批量查询

#### 辅助查询接口
- ✅ 查询建议
- ✅ 相关查询
- ✅ 查询历史

### 3. 文档嵌入模块测试 (`document_embedding_api_test.sh`)

测试以下功能：
- ✅ 获取支持的文件类型
- ✅ 获取支持的嵌入模型
- ✅ 处理文档嵌入（同步/异步模式）
- ✅ 批量处理文档嵌入
- ✅ 查询处理状态
- ✅ 检查文档嵌入状态
- ✅ 重试处理任务
- ✅ 取消处理任务
- ✅ 删除文档嵌入
- ✅ 高级配置测试（PDF、JSON、Office文档）
- ✅ 错误处理测试

### 4. 集成测试

完整的端到端测试流程：
1. 上传测试文件
2. 处理文档嵌入
3. 执行RAG查询
4. 清理测试数据

## ⚙️ 配置说明

### 基础配置

所有脚本都使用以下默认配置：

```bash
BASE_URL="http://localhost:8080"
USERNAME="admin"
PASSWORD="admin123"
```

如需修改，请编辑对应脚本文件中的配置部分。

### 测试数据

脚本会自动创建和清理测试文件，包括：
- 文本文件 (`test.txt`)
- JSON文件 (`test.json`)
- 集成测试文件 (`integration_test.txt`)

## 📊 测试结果

### 成功响应示例

#### 文件上传成功
```json
{
  "success": true,
  "fileInfo": {
    "fileId": "uuid-string",
    "originalFileName": "test.txt",
    "relativePath": "2025/01/15/abc123.txt",
    "fileSize": 1024,
    "contentType": "text/plain",
    "md5Hash": "...",
    "sha256Hash": "...",
    "storageType": "LOCAL",
    "accessUrl": "/files/2025/01/15/abc123.txt"
  }
}
```

#### RAG查询成功
```json
{
  "answer": "人工智能是...",
  "sources": [...],
  "metadata": {...},
  "processingTime": 1500
}
```

#### 文档嵌入成功
```json
{
  "processingId": "uuid-string",
  "fileId": "file-uuid",
  "knowledgeBaseId": "kb_001",
  "status": "COMPLETED",
  "chunksProcessed": 10,
  "processingTime": 2000
}
```

### 错误响应处理

脚本包含完整的错误处理测试，涵盖：
- 无效参数
- 认证失败
- 资源不存在
- 服务器错误

## 🔧 故障排除

### 常见问题

1. **服务不可用**
   ```
   ✗ 服务不可用，请确保应用已启动
   ```
   解决方案：确保 Spring Boot 应用已启动并监听 8080 端口

2. **认证失败**
   ```
   HTTP 401 Unauthorized
   ```
   解决方案：检查用户名密码是否正确

3. **JSON格式化失败**
   ```
   jq: command not found
   ```
   解决方案：安装 jq 工具或忽略格式化

4. **文件权限错误**
   ```
   Permission denied
   ```
   解决方案：给脚本添加执行权限 `chmod +x *.sh`

### 调试模式

在脚本中添加 `-v` 参数可以看到详细的 curl 输出：

```bash
curl -v -X POST "http://localhost:8080/api/upload/single" ...
```

## 📝 自定义测试

### 添加新的测试用例

1. 在对应的脚本文件中添加新的测试函数
2. 按照现有格式编写 curl 命令
3. 添加适当的错误处理和输出格式化

### 修改测试参数

可以根据需要修改以下参数：
- 文件大小限制
- 分块大小
- 相似度阈值
- 温度参数
- Token限制

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来改进这些测试脚本。

## 📄 许可证

本测试脚本遵循与主项目相同的许可证。 