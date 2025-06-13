#!/bin/bash

# 文件上传模块 API 测试脚本
# 基础配置
BASE_URL="http://localhost:8080"
USERNAME="admin"
PASSWORD="admin123"
AUTH="-u ${USERNAME}:${PASSWORD}"

echo "========================================="
echo "文件上传模块 API 测试脚本"
echo "========================================="

# 创建测试文件
echo "创建测试文件..."
echo "这是一个测试文件内容" > test.txt
echo '{"name": "test", "content": "json test file"}' > test.json

# 1. 单个文件上传
echo -e "\n1. 测试单个文件上传"
echo "curl ${AUTH} -X POST \"${BASE_URL}/api/upload/single\" -F \"file=@test.txt\" -F \"tags=test,single\""
UPLOAD_RESPONSE=$(curl -s ${AUTH} \
  -X POST "${BASE_URL}/api/upload/single" \
  -F "file=@test.txt" \
  -F "tags=test,single" \
  -F "customPath=test-folder" \
  -F "overwrite=false" \
  -F "generateUniqueName=true")

echo "响应: $UPLOAD_RESPONSE"

# 提取文件ID用于后续测试
FILE_ID=$(echo $UPLOAD_RESPONSE | grep -o '"fileId":"[^"]*"' | cut -d'"' -f4)
echo "提取的文件ID: $FILE_ID"

# 2. 多个文件上传
echo -e "\n2. 测试多个文件上传"
echo "curl ${AUTH} -X POST \"${BASE_URL}/api/upload/multiple\" -F \"files=@test.txt\" -F \"files=@test.json\""
curl -s ${AUTH} \
  -X POST "${BASE_URL}/api/upload/multiple" \
  -F "files=@test.txt" \
  -F "files=@test.json" \
  -F "tags=test,multiple" \
  -F "customPath=batch-folder" \
  -F "overwrite=false" \
  -F "generateUniqueName=true" | jq '.'

# 3. 根据文件ID获取文件信息
echo -e "\n3. 测试根据文件ID获取文件信息"
if [ ! -z "$FILE_ID" ]; then
    echo "curl ${AUTH} -X GET \"${BASE_URL}/api/upload/info/id/${FILE_ID}\""
    curl -s ${AUTH} \
      -X GET "${BASE_URL}/api/upload/info/id/${FILE_ID}" | jq '.'
else
    echo "跳过测试 - 未获取到文件ID"
fi

# 4. 根据文件ID检查文件是否存在
echo -e "\n4. 测试根据文件ID检查文件是否存在"
if [ ! -z "$FILE_ID" ]; then
    echo "curl ${AUTH} -X GET \"${BASE_URL}/api/upload/exists/id/${FILE_ID}\""
    curl -s ${AUTH} \
      -X GET "${BASE_URL}/api/upload/exists/id/${FILE_ID}" | jq '.'
else
    echo "跳过测试 - 未获取到文件ID"
fi

# 5. 根据文件ID获取文件URL
echo -e "\n5. 测试根据文件ID获取文件URL"
if [ ! -z "$FILE_ID" ]; then
    echo "curl ${AUTH} -X GET \"${BASE_URL}/api/upload/url/id/${FILE_ID}\""
    curl -s ${AUTH} \
      -X GET "${BASE_URL}/api/upload/url/id/${FILE_ID}" | jq '.'
else
    echo "跳过测试 - 未获取到文件ID"
fi

# 6. 根据文件ID下载文件（内联方式）
echo -e "\n6. 测试根据文件ID下载文件（内联方式）"
if [ ! -z "$FILE_ID" ]; then
    echo "curl ${AUTH} -X GET \"${BASE_URL}/api/upload/download/id/${FILE_ID}?attachment=false\" -o downloaded_inline.txt"
    curl -s ${AUTH} \
      -X GET "${BASE_URL}/api/upload/download/id/${FILE_ID}?attachment=false" \
      -o downloaded_inline.txt
    echo "下载完成，文件保存为: downloaded_inline.txt"
    echo "文件内容:"
    cat downloaded_inline.txt
else
    echo "跳过测试 - 未获取到文件ID"
fi

# 7. 根据文件ID下载文件（附件方式）
echo -e "\n7. 测试根据文件ID下载文件（附件方式）"
if [ ! -z "$FILE_ID" ]; then
    echo "curl ${AUTH} -X GET \"${BASE_URL}/api/upload/download/id/${FILE_ID}?attachment=true\" -o downloaded_attachment.txt"
    curl -s ${AUTH} \
      -X GET "${BASE_URL}/api/upload/download/id/${FILE_ID}?attachment=true" \
      -o downloaded_attachment.txt
    echo "下载完成，文件保存为: downloaded_attachment.txt"
else
    echo "跳过测试 - 未获取到文件ID"
fi

# 8. 根据文件ID删除文件（最后执行）
echo -e "\n8. 测试根据文件ID删除文件"
if [ ! -z "$FILE_ID" ]; then
    echo "curl ${AUTH} -X DELETE \"${BASE_URL}/api/upload/id/${FILE_ID}\""
    curl -s ${AUTH} \
      -X DELETE "${BASE_URL}/api/upload/id/${FILE_ID}" | jq '.'
else
    echo "跳过测试 - 未获取到文件ID"
fi

# 清理测试文件
echo -e "\n清理测试文件..."
rm -f test.txt test.json downloaded_inline.txt downloaded_attachment.txt

echo -e "\n========================================="
echo "文件上传模块 API 测试完成"
echo "========================================="

# 错误处理示例
echo -e "\n========================================="
echo "错误处理测试"
echo "========================================="

# 测试不存在的文件ID
echo -e "\n测试不存在的文件ID:"
FAKE_FILE_ID="non-existent-file-id"
echo "curl ${AUTH} -X GET \"${BASE_URL}/api/upload/info/id/${FAKE_FILE_ID}\""
curl -s ${AUTH} \
  -X GET "${BASE_URL}/api/upload/info/id/${FAKE_FILE_ID}" | jq '.'

# 测试无效的认证
echo -e "\n测试无效的认证:"
echo "curl -u invalid:invalid -X GET \"${BASE_URL}/api/upload/info/id/${FAKE_FILE_ID}\""
curl -s -u invalid:invalid \
  -X GET "${BASE_URL}/api/upload/info/id/${FAKE_FILE_ID}" | jq '.'

echo -e "\n所有测试完成！" 