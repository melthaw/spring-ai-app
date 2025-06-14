#!/bin/bash

# 知识库高级搜索API测试脚本
# 测试新增的高级搜索功能

BASE_URL="http://localhost:8080"
CONTENT_TYPE="Content-Type: application/json"

echo "======================================"
echo "知识库高级搜索API测试"
echo "======================================"

# 1. 测试知识库高级搜索
echo "1. 测试知识库高级搜索..."
curl -X POST "${BASE_URL}/api/knowledge-base/search-advanced" \
     -H "${CONTENT_TYPE}" \
     -d '{
       "keyword": "技术",
       "ownerId": "user123",
       "kbTypes": ["PERSONAL", "TEAM"],
       "accessLevels": ["PUBLIC", "INTERNAL"],
       "statuses": ["ACTIVE"],
       "isPublic": true,
       "sortBy": "createTime",
       "sortDirection": "DESC"
     }' | jq .

echo -e "\n"

# 2. 测试文档高级搜索
echo "2. 测试文档高级搜索..."
curl -X POST "${BASE_URL}/api/knowledge-base/documents/search-advanced" \
     -H "${CONTENT_TYPE}" \
     -d '{
       "keyword": "机器学习",
       "kbId": 1,
       "docTypes": ["PDF", "WORD"],
       "statuses": ["COMPLETED"],
       "accessLevels": ["PUBLIC"],
       "enabled": true,
       "hasEmbedding": true,
       "sortBy": "createTime",
       "sortDirection": "DESC"
     }' | jq .

echo -e "\n"

# 3. 测试知识库高级搜索 - 时间范围筛选
echo "3. 测试知识库时间范围搜索..."
curl -X POST "${BASE_URL}/api/knowledge-base/search-advanced" \
     -H "${CONTENT_TYPE}" \
     -d '{
       "keyword": "AI",
       "createTimeStart": "2024-01-01T00:00:00",
       "createTimeEnd": "2024-12-31T23:59:59",
       "minDocumentCount": 1,
       "maxDocumentCount": 100,
       "sortBy": "documentCount",
       "sortDirection": "DESC"
     }' | jq .

echo -e "\n"

# 4. 测试文档高级搜索 - 多分类查询
echo "4. 测试文档多分类搜索..."
curl -X POST "${BASE_URL}/api/knowledge-base/documents/search-advanced" \
     -H "${CONTENT_TYPE}" \
     -d '{
       "kbId": 1,
       "categoryIds": [1, 2, 3],
       "tags": ["技术", "教程"],
       "minFileSize": 1024,
       "maxFileSize": 10485760,
       "createTimeStart": "2024-01-01T00:00:00",
       "sortBy": "fileSize",
       "sortDirection": "ASC"
     }' | jq .

echo -e "\n"

# 5. 测试文档高级搜索 - 嵌入相关筛选
echo "5. 测试文档嵌入状态搜索..."
curl -X POST "${BASE_URL}/api/knowledge-base/documents/search-advanced" \
     -H "${CONTENT_TYPE}" \
     -d '{
       "kbId": 1,
       "hasEmbedding": true,
       "embeddingModel": "text-embedding-ada-002",
       "embeddingStatus": "COMPLETED",
       "sortBy": "updateTime",
       "sortDirection": "DESC"
     }' | jq .

echo -e "\n"

# 6. 测试知识库高级搜索 - 复杂条件组合
echo "6. 测试知识库复杂条件搜索..."
curl -X POST "${BASE_URL}/api/knowledge-base/search-advanced?page=0&size=10" \
     -H "${CONTENT_TYPE}" \
     -d '{
       "keyword": "深度学习",
       "ownerId": "user123",
       "kbTypes": ["TEAM", "ORGANIZATION"],
       "accessLevels": ["INTERNAL", "PUBLIC"],
       "statuses": ["ACTIVE"],
       "isPublic": false,
       "minTotalSize": 1048576,
       "maxTotalSize": 104857600,
       "createTimeStart": "2024-01-01T00:00:00",
       "sortBy": "totalSize",
       "sortDirection": "DESC"
     }' | jq .

echo -e "\n"

# 7. 测试文档高级搜索 - 空条件（返回所有）
echo "7. 测试文档搜索空条件..."
curl -X POST "${BASE_URL}/api/knowledge-base/documents/search-advanced?page=0&size=5" \
     -H "${CONTENT_TYPE}" \
     -d '{}' | jq .

echo -e "\n"

# 8. 测试分页功能
echo "8. 测试分页功能..."
curl -X POST "${BASE_URL}/api/knowledge-base/search-advanced?page=1&size=5&sort=createTime,desc" \
     -H "${CONTENT_TYPE}" \
     -d '{
       "statuses": ["ACTIVE"],
       "sortBy": "createTime",
       "sortDirection": "DESC"
     }' | jq .

echo -e "\n"

echo "======================================"
echo "高级搜索API测试完成"
echo "======================================"

# 9. 创建一些测试数据用的脚本
echo "9. 创建测试知识库..."
curl -X POST "${BASE_URL}/api/knowledge-base" \
     -H "${CONTENT_TYPE}" \
     -d '{
       "kbCode": "test-advanced-search",
       "kbName": "高级搜索测试库",
       "description": "用于测试高级搜索功能的知识库",
       "ownerId": "test-user",
       "ownerName": "测试用户",
       "kbType": "PERSONAL",
       "isPublic": true,
       "accessLevel": "PUBLIC"
     }' | jq .

echo -e "\n"

# 10. 测试错误情况
echo "10. 测试无效参数..."
curl -X POST "${BASE_URL}/api/knowledge-base/search-advanced" \
     -H "${CONTENT_TYPE}" \
     -d '{
       "invalidField": "test",
       "sortDirection": "INVALID"
     }' | jq .

echo -e "\n"

echo "测试脚本执行完毕！" 