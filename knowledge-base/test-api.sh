#!/bin/bash

# Knowledge Base API 测试脚本

BASE_URL="http://localhost:8080"
USER_ID="test-user-001"

echo "=== 知识库模块 API 测试 ==="

# 1. 创建知识库
echo "1. 创建知识库..."
KNOWLEDGE_BASE=$(curl -s -X POST "$BASE_URL/api/knowledge-base" \
  -H "Content-Type: application/json" \
  -d '{
    "kbCode": "test-kb-001",
    "kbName": "测试知识库",
    "description": "这是一个测试用的知识库",
    "ownerId": "'$USER_ID'",
    "ownerName": "测试用户",
    "kbType": "PERSONAL",
    "accessLevel": "PRIVATE"
  }')

echo "创建结果: $KNOWLEDGE_BASE"

# 提取知识库ID
KB_ID=$(echo $KNOWLEDGE_BASE | grep -o '"id":[0-9]*' | cut -d':' -f2)
echo "知识库ID: $KB_ID"

# 2. 获取知识库详情
echo -e "\n2. 获取知识库详情..."
curl -s "$BASE_URL/api/knowledge-base/$KB_ID" | jq .

# 3. 检查知识库编码可用性
echo -e "\n3. 检查知识库编码可用性..."
curl -s "$BASE_URL/api/knowledge-base/code/test-kb-002/available" | jq .

# 4. 搜索知识库
echo -e "\n4. 搜索知识库..."
curl -s "$BASE_URL/api/knowledge-base/search?keyword=测试&page=0&size=10" | jq .

# 5. 获取用户知识库列表
echo -e "\n5. 获取用户知识库列表..."
curl -s "$BASE_URL/api/knowledge-base/user/$USER_ID" | jq .

# 6. 获取知识库统计信息
echo -e "\n6. 获取知识库统计信息..."
curl -s "$BASE_URL/api/knowledge-base/$KB_ID/stats" | jq .

# 7. 上传测试文档（需要有测试文件）
if [ -f "test-document.txt" ]; then
    echo -e "\n7. 上传测试文档..."
    DOCUMENT=$(curl -s -X POST "$BASE_URL/api/knowledge-base/documents/upload" \
      -F "file=@test-document.txt" \
      -F "title=测试文档" \
      -F "description=这是一个测试文档" \
      -F "categoryId=1" \
      -F "uploadUserId=$USER_ID" \
      -F "immediateEmbedding=true" \
      -F "embeddingModel=text-embedding-ada-002")
    
    echo "上传结果: $DOCUMENT"
    
    # 提取文档ID
    DOC_ID=$(echo $DOCUMENT | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "文档ID: $DOC_ID"
    
    # 8. 获取文档详情
    echo -e "\n8. 获取文档详情..."
    curl -s "$BASE_URL/api/knowledge-base/documents/$DOC_ID" | jq .
    
    # 9. 获取文档处理状态
    echo -e "\n9. 获取文档处理状态..."
    curl -s "$BASE_URL/api/knowledge-base/documents/$DOC_ID/status" | jq .
    
    # 10. 获取文档嵌入列表
    echo -e "\n10. 获取文档嵌入列表..."
    curl -s "$BASE_URL/api/knowledge-base/documents/$DOC_ID/embeddings" | jq .
else
    echo -e "\n注意: 测试文档 test-document.txt 不存在，跳过文档上传测试"
fi

# 11. 搜索文档
echo -e "\n11. 搜索文档..."
curl -s "$BASE_URL/api/knowledge-base/documents/search?keyword=测试&page=0&size=10" | jq .

# 12. 获取处理失败的文档
echo -e "\n12. 获取处理失败的文档..."
curl -s "$BASE_URL/api/knowledge-base/documents/failed" | jq .

echo -e "\n=== API 测试完成 ===" 