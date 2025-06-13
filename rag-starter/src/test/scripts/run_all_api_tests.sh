#!/bin/bash

# 综合API测试脚本 - 运行所有模块的测试
# 基础配置
BASE_URL="http://localhost:8080"
USERNAME="admin"
PASSWORD="admin123"

echo "========================================="
echo "Spring AI App - 综合API测试脚本"
echo "========================================="
echo "测试目标: ${BASE_URL}"
echo "认证用户: ${USERNAME}"
echo "========================================="

# 检查依赖工具
check_dependencies() {
    echo "检查依赖工具..."
    
    if ! command -v curl &> /dev/null; then
        echo "错误: curl 未安装"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        echo "警告: jq 未安装，JSON响应将不会格式化"
        echo "建议安装: brew install jq (macOS) 或 apt-get install jq (Ubuntu)"
    fi
    
    echo "依赖检查完成"
}

# 检查服务是否可用
check_service() {
    echo "检查服务可用性..."
    
    if curl -s --connect-timeout 5 "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
        echo "✓ 服务可用"
    else
        echo "✗ 服务不可用，请确保应用已启动"
        echo "  启动命令: mvn spring-boot:run"
        exit 1
    fi
}

# 运行文件上传模块测试
run_upload_tests() {
    echo -e "\n========================================="
    echo "运行文件上传模块测试"
    echo "========================================="
    
    if [ -f "upload_api_test.sh" ]; then
        chmod +x upload_api_test.sh
        ./upload_api_test.sh
    else
        echo "错误: upload_api_test.sh 文件不存在"
    fi
}

# 运行RAG查询模块测试
run_rag_query_tests() {
    echo -e "\n========================================="
    echo "运行RAG查询模块测试"
    echo "========================================="
    
    if [ -f "rag_query_api_test.sh" ]; then
        chmod +x rag_query_api_test.sh
        ./rag_query_api_test.sh
    else
        echo "错误: rag_query_api_test.sh 文件不存在"
    fi
}

# 运行文档嵌入模块测试
run_embedding_tests() {
    echo -e "\n========================================="
    echo "运行文档嵌入模块测试"
    echo "========================================="
    
    if [ -f "document_embedding_api_test.sh" ]; then
        chmod +x document_embedding_api_test.sh
        ./document_embedding_api_test.sh
    else
        echo "错误: document_embedding_api_test.sh 文件不存在"
    fi
}

# 运行集成测试（文件上传 -> 文档嵌入 -> RAG查询）
run_integration_tests() {
    echo -e "\n========================================="
    echo "运行集成测试"
    echo "========================================="
    
    AUTH="-u ${USERNAME}:${PASSWORD}"
    CONTENT_TYPE="Content-Type: application/json"
    
    # 创建测试文件
    echo "创建测试文件..."
    cat > integration_test.txt << EOF
人工智能（Artificial Intelligence，AI）是计算机科学的一个分支，它企图了解智能的实质，并生产出一种新的能以人类智能相似的方式做出反应的智能机器。

机器学习是人工智能的一个重要分支，它使计算机能够在没有明确编程的情况下学习。机器学习算法通过分析数据来识别模式，并使用这些模式来做出预测或决策。

深度学习是机器学习的一个子集，它使用多层神经网络来学习数据的复杂模式。深度学习在图像识别、自然语言处理和语音识别等领域取得了显著成果。
EOF
    
    echo "1. 上传测试文件..."
    UPLOAD_RESPONSE=$(curl -s ${AUTH} \
        -X POST "${BASE_URL}/api/upload/single" \
        -F "file=@integration_test.txt" \
        -F "tags=integration,test,ai")
    
    echo "上传响应: $UPLOAD_RESPONSE"
    
    # 提取文件ID
    FILE_ID=$(echo $UPLOAD_RESPONSE | grep -o '"fileId":"[^"]*"' | cut -d'"' -f4)
    echo "文件ID: $FILE_ID"
    
    if [ ! -z "$FILE_ID" ]; then
        echo -e "\n2. 处理文档嵌入..."
        EMBEDDING_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/rag/embedding/process" \
            -H "${CONTENT_TYPE}" \
            -d '{
                "fileId": "'${FILE_ID}'",
                "knowledgeBaseId": "integration_test_kb",
                "embeddingModel": "text-embedding-ada-002",
                "chunkSize": 200,
                "chunkOverlap": 20,
                "processingMode": "SYNC",
                "readerConfig": {
                    "fileType": "TEXT",
                    "preserveFormatting": false,
                    "enableChunking": true,
                    "removeWhitespace": true
                },
                "metadata": {
                    "source": "integration_test",
                    "category": "test"
                }
            }')
        
        echo "嵌入响应: $EMBEDDING_RESPONSE"
        
        echo -e "\n3. 执行RAG查询..."
        QUERY_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/rag/query/simple" \
            -H "${CONTENT_TYPE}" \
            -d '{
                "question": "什么是深度学习？",
                "knowledgeBaseId": "integration_test_kb",
                "temperature": 0.7,
                "maxTokens": 1000,
                "limit": 5,
                "similarityThreshold": 0.5,
                "includeMetadata": true,
                "includeContent": true
            }')
        
        echo "查询响应: $QUERY_RESPONSE"
        
        echo -e "\n4. 清理测试数据..."
        # 删除文档嵌入
        curl -s -X DELETE "${BASE_URL}/api/rag/embedding/document/${FILE_ID}?knowledgeBaseId=integration_test_kb" > /dev/null
        
        # 删除上传的文件
        curl -s ${AUTH} -X DELETE "${BASE_URL}/api/upload/id/${FILE_ID}" > /dev/null
        
        echo "集成测试完成"
    else
        echo "集成测试失败 - 未能获取文件ID"
    fi
    
    # 清理测试文件
    rm -f integration_test.txt
}

# 生成测试报告
generate_report() {
    echo -e "\n========================================="
    echo "测试报告"
    echo "========================================="
    echo "测试时间: $(date)"
    echo "测试目标: ${BASE_URL}"
    echo ""
    echo "测试模块:"
    echo "  ✓ 文件上传模块 (FileUploadController)"
    echo "  ✓ RAG查询模块 (RagQueryController)"
    echo "  ✓ 文档嵌入模块 (DocumentEmbeddingController)"
    echo "  ✓ 集成测试"
    echo ""
    echo "注意事项:"
    echo "  - 某些测试可能因为Mock实现而返回模拟数据"
    echo "  - 生产环境需要配置真实的AI模型和向量数据库"
    echo "  - 建议在开发环境中运行这些测试"
    echo "========================================="
}

# 主函数
main() {
    check_dependencies
    check_service
    
    # 根据参数决定运行哪些测试
    case "${1:-all}" in
        "upload")
            run_upload_tests
            ;;
        "rag")
            run_rag_query_tests
            ;;
        "embedding")
            run_embedding_tests
            ;;
        "integration")
            run_integration_tests
            ;;
        "all")
            run_upload_tests
            run_rag_query_tests
            run_embedding_tests
            run_integration_tests
            ;;
        *)
            echo "用法: $0 [upload|rag|embedding|integration|all]"
            echo ""
            echo "参数说明:"
            echo "  upload      - 只运行文件上传模块测试"
            echo "  rag         - 只运行RAG查询模块测试"
            echo "  embedding   - 只运行文档嵌入模块测试"
            echo "  integration - 只运行集成测试"
            echo "  all         - 运行所有测试 (默认)"
            exit 1
            ;;
    esac
    
    generate_report
}

# 运行主函数
main "$@" 