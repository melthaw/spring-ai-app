#!/bin/bash

# 文档嵌入模块 API 测试脚本
# 基础配置
BASE_URL="http://localhost:8080"
CONTENT_TYPE="Content-Type: application/json"

echo "========================================="
echo "文档嵌入模块 API 测试脚本"
echo "========================================="

# 测试用的文件ID和知识库ID
TEST_FILE_ID="test-file-123"
TEST_KNOWLEDGE_BASE_ID="kb_001"
TEST_PROCESSING_ID=""

# 1. 获取支持的文件类型
echo -e "\n1. 测试获取支持的文件类型"
echo "curl -X GET \"${BASE_URL}/api/rag/embedding/supported-types\""
curl -s -X GET "${BASE_URL}/api/rag/embedding/supported-types" | jq '.'

# 2. 获取支持的嵌入模型
echo -e "\n2. 测试获取支持的嵌入模型"
echo "curl -X GET \"${BASE_URL}/api/rag/embedding/supported-models\""
curl -s -X GET "${BASE_URL}/api/rag/embedding/supported-models" | jq '.'

# 3. 处理文档嵌入（同步模式）
echo -e "\n3. 测试处理文档嵌入（同步模式）"
echo "curl -X POST \"${BASE_URL}/api/rag/embedding/process\" -H \"${CONTENT_TYPE}\""
SYNC_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/rag/embedding/process" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "fileId": "'${TEST_FILE_ID}'",
    "knowledgeBaseId": "'${TEST_KNOWLEDGE_BASE_ID}'",
    "embeddingModel": "text-embedding-ada-002",
    "chunkSize": 500,
    "chunkOverlap": 50,
    "processingMode": "SYNC",
    "readerConfig": {
      "fileType": "PDF",
      "extractTables": true,
      "extractImages": false,
      "language": "zh",
      "maxContentLength": 1048576,
      "enableChunking": true,
      "preserveFormatting": false
    },
    "metadata": {
      "source": "test_document",
      "category": "technical",
      "tags": ["AI", "machine learning"]
    }
  }')

echo "同步处理响应: $SYNC_RESPONSE"

# 4. 处理文档嵌入（异步模式）
echo -e "\n4. 测试处理文档嵌入（异步模式）"
echo "curl -X POST \"${BASE_URL}/api/rag/embedding/process\" -H \"${CONTENT_TYPE}\""
ASYNC_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/rag/embedding/process" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "fileId": "test-file-456",
    "knowledgeBaseId": "'${TEST_KNOWLEDGE_BASE_ID}'",
    "embeddingModel": "text-embedding-ada-002",
    "chunkSize": 1000,
    "chunkOverlap": 100,
    "processingMode": "ASYNC",
    "readerConfig": {
      "fileType": "TEXT",
      "preserveFormatting": true,
      "enableChunking": true,
      "chunkSize": 1000,
      "removeWhitespace": true
    },
    "metadata": {
      "source": "async_test_document",
      "category": "documentation"
    }
  }')

echo "异步处理响应: $ASYNC_RESPONSE"

# 提取处理ID用于后续测试
TEST_PROCESSING_ID=$(echo $ASYNC_RESPONSE | grep -o '"processingId":"[^"]*"' | cut -d'"' -f4)
echo "提取的处理ID: $TEST_PROCESSING_ID"

# 5. 批量处理文档嵌入
echo -e "\n5. 测试批量处理文档嵌入"
echo "curl -X POST \"${BASE_URL}/api/rag/embedding/batch\" -H \"${CONTENT_TYPE}\""
curl -s -X POST "${BASE_URL}/api/rag/embedding/batch" \
  -H "${CONTENT_TYPE}" \
  -d '[
    {
      "fileId": "batch-file-001",
      "knowledgeBaseId": "'${TEST_KNOWLEDGE_BASE_ID}'",
      "embeddingModel": "text-embedding-ada-002",
      "chunkSize": 500,
      "chunkOverlap": 50,
      "processingMode": "ASYNC",
      "readerConfig": {
        "fileType": "PDF",
        "extractTables": true,
        "language": "zh"
      }
    },
    {
      "fileId": "batch-file-002",
      "knowledgeBaseId": "'${TEST_KNOWLEDGE_BASE_ID}'",
      "embeddingModel": "text-embedding-ada-002",
      "chunkSize": 800,
      "chunkOverlap": 80,
      "processingMode": "ASYNC",
      "readerConfig": {
        "fileType": "JSON",
        "jsonPath": "$.content",
        "flattenJson": true
      }
    },
    {
      "fileId": "batch-file-003",
      "knowledgeBaseId": "'${TEST_KNOWLEDGE_BASE_ID}'",
      "embeddingModel": "text-embedding-ada-002",
      "chunkSize": 600,
      "chunkOverlap": 60,
      "processingMode": "ASYNC",
      "readerConfig": {
        "fileType": "OFFICE",
        "extractFormulas": true,
        "extractComments": true
      }
    }
  ]' | jq '.'

# 6. 查询处理状态
echo -e "\n6. 测试查询处理状态"
if [ ! -z "$TEST_PROCESSING_ID" ]; then
    echo "curl -X GET \"${BASE_URL}/api/rag/embedding/status/${TEST_PROCESSING_ID}\""
    curl -s -X GET "${BASE_URL}/api/rag/embedding/status/${TEST_PROCESSING_ID}" | jq '.'
else
    echo "跳过测试 - 未获取到处理ID"
fi

# 7. 检查文档嵌入状态
echo -e "\n7. 测试检查文档嵌入状态"
echo "curl -X GET \"${BASE_URL}/api/rag/embedding/check/${TEST_FILE_ID}?knowledgeBaseId=${TEST_KNOWLEDGE_BASE_ID}\""
curl -s -X GET "${BASE_URL}/api/rag/embedding/check/${TEST_FILE_ID}?knowledgeBaseId=${TEST_KNOWLEDGE_BASE_ID}" | jq '.'

# 8. 重试处理任务
echo -e "\n8. 测试重试处理任务"
if [ ! -z "$TEST_PROCESSING_ID" ]; then
    echo "curl -X POST \"${BASE_URL}/api/rag/embedding/retry/${TEST_PROCESSING_ID}\""
    curl -s -X POST "${BASE_URL}/api/rag/embedding/retry/${TEST_PROCESSING_ID}" | jq '.'
else
    echo "跳过测试 - 未获取到处理ID"
fi

# 9. 取消处理任务
echo -e "\n9. 测试取消处理任务"
if [ ! -z "$TEST_PROCESSING_ID" ]; then
    echo "curl -X DELETE \"${BASE_URL}/api/rag/embedding/cancel/${TEST_PROCESSING_ID}\""
    curl -s -X DELETE "${BASE_URL}/api/rag/embedding/cancel/${TEST_PROCESSING_ID}" | jq '.'
else
    echo "跳过测试 - 未获取到处理ID"
fi

# 10. 删除文档嵌入
echo -e "\n10. 测试删除文档嵌入"
echo "curl -X DELETE \"${BASE_URL}/api/rag/embedding/document/${TEST_FILE_ID}?knowledgeBaseId=${TEST_KNOWLEDGE_BASE_ID}\""
curl -s -X DELETE "${BASE_URL}/api/rag/embedding/document/${TEST_FILE_ID}?knowledgeBaseId=${TEST_KNOWLEDGE_BASE_ID}" | jq '.'

echo -e "\n========================================="
echo "文档嵌入模块 API 测试完成"
echo "========================================="

# 高级配置测试
echo -e "\n========================================="
echo "高级配置测试"
echo "========================================="

# 测试不同文件类型的配置
echo -e "\n测试PDF文档配置:"
curl -s -X POST "${BASE_URL}/api/rag/embedding/process" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "fileId": "pdf-test-file",
    "knowledgeBaseId": "'${TEST_KNOWLEDGE_BASE_ID}'",
    "embeddingModel": "text-embedding-ada-002",
    "processingMode": "SYNC",
    "readerConfig": {
      "fileType": "PDF",
      "extractTables": true,
      "extractImages": true,
      "extractAnnotations": true,
      "pdfPageLimit": 100,
      "language": "zh",
      "maxContentLength": 2097152,
      "enableChunking": true,
      "chunkSize": 800,
      "chunkOverlap": 80,
      "preserveFormatting": true
    }
  }' | jq '.'

echo -e "\n测试JSON文档配置:"
curl -s -X POST "${BASE_URL}/api/rag/embedding/process" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "fileId": "json-test-file",
    "knowledgeBaseId": "'${TEST_KNOWLEDGE_BASE_ID}'",
    "embeddingModel": "text-embedding-ada-002",
    "processingMode": "SYNC",
    "readerConfig": {
      "fileType": "JSON",
      "jsonPath": "$.documents[*].content",
      "flattenJson": true,
      "jsonDepthLimit": 10,
      "enableChunking": true,
      "chunkSize": 600,
      "removeWhitespace": true
    }
  }' | jq '.'

echo -e "\n测试Office文档配置:"
curl -s -X POST "${BASE_URL}/api/rag/embedding/process" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "fileId": "office-test-file",
    "knowledgeBaseId": "'${TEST_KNOWLEDGE_BASE_ID}'",
    "embeddingModel": "text-embedding-ada-002",
    "processingMode": "SYNC",
    "readerConfig": {
      "fileType": "OFFICE",
      "extractFormulas": true,
      "extractComments": true,
      "extractHiddenText": false,
      "enableChunking": true,
      "chunkSize": 1000,
      "preserveFormatting": false
    }
  }' | jq '.'

# 错误处理示例
echo -e "\n========================================="
echo "错误处理测试"
echo "========================================="

# 测试无效的文件ID
echo -e "\n测试无效的文件ID:"
curl -s -X POST "${BASE_URL}/api/rag/embedding/process" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "fileId": "",
    "knowledgeBaseId": "'${TEST_KNOWLEDGE_BASE_ID}'",
    "embeddingModel": "text-embedding-ada-002",
    "processingMode": "SYNC"
  }' | jq '.'

# 测试无效的嵌入模型
echo -e "\n测试无效的嵌入模型:"
curl -s -X POST "${BASE_URL}/api/rag/embedding/process" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "fileId": "'${TEST_FILE_ID}'",
    "knowledgeBaseId": "'${TEST_KNOWLEDGE_BASE_ID}'",
    "embeddingModel": "invalid-model",
    "processingMode": "SYNC"
  }' | jq '.'

# 测试无效的分块参数
echo -e "\n测试无效的分块参数:"
curl -s -X POST "${BASE_URL}/api/rag/embedding/process" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "fileId": "'${TEST_FILE_ID}'",
    "knowledgeBaseId": "'${TEST_KNOWLEDGE_BASE_ID}'",
    "embeddingModel": "text-embedding-ada-002",
    "chunkSize": -1,
    "chunkOverlap": 200,
    "processingMode": "SYNC"
  }' | jq '.'

# 测试不存在的处理ID
echo -e "\n测试不存在的处理ID:"
FAKE_PROCESSING_ID="non-existent-processing-id"
curl -s -X GET "${BASE_URL}/api/rag/embedding/status/${FAKE_PROCESSING_ID}" | jq '.'

echo -e "\n所有测试完成！" 