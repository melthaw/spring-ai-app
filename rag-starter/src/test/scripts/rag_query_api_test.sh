#!/bin/bash

# RAG查询模块 API 测试脚本
# 基础配置
BASE_URL="http://localhost:8080"
CONTENT_TYPE="Content-Type: application/json"

echo "========================================="
echo "RAG查询模块 API 测试脚本"
echo "========================================="

# ==================== Case by Case 查询接口 ====================

# 1. 简单查询
echo -e "\n1. 测试简单查询"
echo "curl -X POST \"${BASE_URL}/api/rag/query/simple\" -H \"${CONTENT_TYPE}\""
curl -s -X POST "${BASE_URL}/api/rag/query/simple" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "question": "什么是人工智能？",
    "knowledgeBaseId": "kb_001",
    "temperature": 0.7,
    "maxTokens": 2000,
    "limit": 10,
    "similarityThreshold": 0.7,
    "includeMetadata": true,
    "includeContent": true
  }' | jq '.'

# 2. 多知识库查询
echo -e "\n2. 测试多知识库查询"
echo "curl -X POST \"${BASE_URL}/api/rag/query/multi-knowledge-base\" -H \"${CONTENT_TYPE}\""
curl -s -X POST "${BASE_URL}/api/rag/query/multi-knowledge-base" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "question": "机器学习的基本概念是什么？",
    "knowledgeBaseIds": ["kb_001", "kb_002", "kb_003"],
    "weights": [0.4, 0.3, 0.3],
    "temperature": 0.8,
    "maxTokens": 1500,
    "limit": 15,
    "similarityThreshold": 0.6,
    "mergeStrategy": "WEIGHTED_AVERAGE",
    "includeMetadata": true
  }' | jq '.'

# 3. 语义查询
echo -e "\n3. 测试语义查询"
echo "curl -X POST \"${BASE_URL}/api/rag/query/semantic\" -H \"${CONTENT_TYPE}\""
curl -s -X POST "${BASE_URL}/api/rag/query/semantic" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "question": "深度学习和神经网络的关系",
    "knowledgeBaseId": "kb_001",
    "embeddingModel": "text-embedding-ada-002",
    "limit": 20,
    "similarityThreshold": 0.75,
    "enableReranking": true,
    "rerankingModel": "cross-encoder",
    "includeMetadata": true,
    "includeContent": true
  }' | jq '.'

# 4. 混合查询
echo -e "\n4. 测试混合查询"
echo "curl -X POST \"${BASE_URL}/api/rag/query/hybrid\" -H \"${CONTENT_TYPE}\""
curl -s -X POST "${BASE_URL}/api/rag/query/hybrid" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "question": "自然语言处理技术应用",
    "knowledgeBaseId": "kb_001",
    "keywords": ["NLP", "自然语言", "文本处理"],
    "keywordWeight": 0.3,
    "semanticWeight": 0.7,
    "limit": 12,
    "similarityThreshold": 0.65,
    "enableReranking": true,
    "includeMetadata": true
  }' | jq '.'

# 5. 对话式查询
echo -e "\n5. 测试对话式查询"
echo "curl -X POST \"${BASE_URL}/api/rag/query/conversational\" -H \"${CONTENT_TYPE}\""
curl -s -X POST "${BASE_URL}/api/rag/query/conversational" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "question": "那么深度学习有哪些应用场景？",
    "knowledgeBaseId": "kb_001",
    "conversationHistory": [
      {
        "role": "user",
        "content": "什么是深度学习？"
      },
      {
        "role": "assistant",
        "content": "深度学习是机器学习的一个分支，使用多层神经网络来学习数据的复杂模式。"
      }
    ],
    "maxHistoryLength": 10,
    "temperature": 0.7,
    "maxTokens": 2000,
    "limit": 10,
    "includeMetadata": true
  }' | jq '.'

# 6. 结构化查询
echo -e "\n6. 测试结构化查询"
echo "curl -X POST \"${BASE_URL}/api/rag/query/structured\" -H \"${CONTENT_TYPE}\""
curl -s -X POST "${BASE_URL}/api/rag/query/structured" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "question": "计算机视觉相关论文",
    "knowledgeBaseId": "kb_001",
    "filters": [
      {
        "field": "category",
        "operator": "EQUALS",
        "value": "computer_vision"
      },
      {
        "field": "publishYear",
        "operator": "GREATER_THAN",
        "value": "2020"
      }
    ],
    "sortBy": "relevance",
    "sortOrder": "DESC",
    "limit": 15,
    "includeMetadata": true,
    "includeAggregations": true
  }' | jq '.'

# 7. 摘要查询
echo -e "\n7. 测试摘要查询"
echo "curl -X POST \"${BASE_URL}/api/rag/query/summary\" -H \"${CONTENT_TYPE}\""
curl -s -X POST "${BASE_URL}/api/rag/query/summary" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "question": "人工智能的发展历程",
    "knowledgeBaseId": "kb_001",
    "summaryType": "COMPREHENSIVE",
    "summaryLength": "MEDIUM",
    "extractKeywords": true,
    "maxKeywords": 10,
    "temperature": 0.5,
    "maxTokens": 1000,
    "limit": 20,
    "includeMetadata": true
  }' | jq '.'

# 8. 引用查询
echo -e "\n8. 测试引用查询"
echo "curl -X POST \"${BASE_URL}/api/rag/query/citation\" -H \"${CONTENT_TYPE}\""
curl -s -X POST "${BASE_URL}/api/rag/query/citation" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "question": "机器学习算法分类",
    "knowledgeBaseId": "kb_001",
    "citationFormat": "APA",
    "detailLevel": "DETAILED",
    "includeBibliography": true,
    "maxCitations": 5,
    "temperature": 0.6,
    "maxTokens": 1500,
    "limit": 10,
    "includeMetadata": true
  }' | jq '.'

# ==================== All in One 查询接口 ====================

# 9. 智能查询
echo -e "\n9. 测试智能查询"
echo "curl -X POST \"${BASE_URL}/api/rag/query/intelligent\" -H \"${CONTENT_TYPE}\""
curl -s -X POST "${BASE_URL}/api/rag/query/intelligent" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "question": "请详细介绍一下强化学习的基本原理和应用",
    "knowledgeBaseId": "kb_001",
    "userPreferences": {
      "detailLevel": "HIGH",
      "includeExamples": true,
      "preferredLanguage": "zh-CN"
    },
    "contextHints": ["机器学习", "算法", "人工智能"],
    "enableAutoOptimization": true,
    "maxTokens": 3000,
    "temperature": 0.7
  }' | jq '.'

# 10. 批量查询
echo -e "\n10. 测试批量查询"
echo "curl -X POST \"${BASE_URL}/api/rag/query/batch\" -H \"${CONTENT_TYPE}\""
curl -s -X POST "${BASE_URL}/api/rag/query/batch" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "questions": [
      "什么是监督学习？",
      "什么是无监督学习？",
      "什么是强化学习？"
    ],
    "knowledgeBaseId": "kb_001",
    "processingMode": "PARALLEL",
    "maxConcurrency": 3,
    "temperature": 0.7,
    "maxTokens": 1000,
    "limit": 5,
    "includeMetadata": false
  }' | jq '.'

# ==================== 辅助查询接口 ====================

# 11. 查询建议
echo -e "\n11. 测试查询建议"
echo "curl -X POST \"${BASE_URL}/api/rag/query/suggestions\" -H \"${CONTENT_TYPE}\""
curl -s -X POST "${BASE_URL}/api/rag/query/suggestions" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "partialQuery": "机器学习",
    "knowledgeBaseId": "kb_001",
    "maxSuggestions": 5,
    "includePopular": true,
    "includeRecent": true,
    "userContext": {
      "previousQueries": ["人工智能", "深度学习"],
      "interests": ["技术", "算法"]
    }
  }' | jq '.'

# 12. 相关查询
echo -e "\n12. 测试相关查询"
echo "curl -X POST \"${BASE_URL}/api/rag/query/related\" -H \"${CONTENT_TYPE}\""
curl -s -X POST "${BASE_URL}/api/rag/query/related" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "currentQuery": "深度学习的基本概念",
    "knowledgeBaseId": "kb_001",
    "maxRelatedQueries": 8,
    "similarityThreshold": 0.6,
    "diversityFactor": 0.3,
    "includeMetadata": true
  }' | jq '.'

# 13. 查询历史
echo -e "\n13. 测试查询历史"
echo "curl -X POST \"${BASE_URL}/api/rag/query/history\" -H \"${CONTENT_TYPE}\""
curl -s -X POST "${BASE_URL}/api/rag/query/history" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "userId": "user_123",
    "knowledgeBaseId": "kb_001",
    "limit": 20,
    "offset": 0,
    "startDate": "2024-01-01T00:00:00Z",
    "endDate": "2024-12-31T23:59:59Z",
    "includeResponses": true,
    "sortBy": "timestamp",
    "sortOrder": "DESC"
  }' | jq '.'

echo -e "\n========================================="
echo "RAG查询模块 API 测试完成"
echo "========================================="

# 错误处理示例
echo -e "\n========================================="
echo "错误处理测试"
echo "========================================="

# 测试无效的知识库ID
echo -e "\n测试无效的知识库ID:"
curl -s -X POST "${BASE_URL}/api/rag/query/simple" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "question": "测试问题",
    "knowledgeBaseId": "invalid_kb_id",
    "temperature": 0.7
  }' | jq '.'

# 测试空问题
echo -e "\n测试空问题:"
curl -s -X POST "${BASE_URL}/api/rag/query/simple" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "question": "",
    "knowledgeBaseId": "kb_001",
    "temperature": 0.7
  }' | jq '.'

# 测试无效的参数
echo -e "\n测试无效的参数:"
curl -s -X POST "${BASE_URL}/api/rag/query/simple" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "question": "测试问题",
    "knowledgeBaseId": "kb_001",
    "temperature": 2.0,
    "limit": -1
  }' | jq '.'

echo -e "\n所有测试完成！" 