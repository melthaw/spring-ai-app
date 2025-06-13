from fastapi import FastAPI, Form, HTTPException
from paddleocr import PaddleOCR
import base64
import io
from PIL import Image
import json
import numpy as np

app = FastAPI()

# 初始化PaddleOCR
ocr = PaddleOCR(use_angle_cls=True, lang='ch', use_gpu=True)

@app.post("/ocr")
async def ocr_endpoint(
    image: str = Form(...),
    language: str = Form("ch"),
    enable_table_recognition: bool = Form(False)
):
    try:
        # 解码Base64图片
        image_data = base64.b64decode(image)
        image = Image.open(io.BytesIO(image_data))
        
        # 执行OCR识别
        result = ocr.ocr(np.array(image), cls=True)
        
        # 处理识别结果
        text_blocks = []
        full_text = []
        
        for line in result:
            for item in line:
                text = item[1][0]  # 文本内容
                confidence = float(item[1][1])  # 置信度
                points = item[0]  # 四个角点坐标
                
                # 计算边界框
                x_coords = [p[0] for p in points]
                y_coords = [p[1] for p in points]
                x = min(x_coords)
                y = min(y_coords)
                width = max(x_coords) - x
                height = max(y_coords) - y
                
                # 创建文本块
                text_block = {
                    "text": text,
                    "confidence": confidence,
                    "boundingBox": {
                        "x": float(x),
                        "y": float(y),
                        "width": float(width),
                        "height": float(height)
                    },
                    "orientation": "horizontal"  # PaddleOCR默认水平文本
                }
                
                text_blocks.append(text_block)
                full_text.append(text)
        
        # 构建响应
        response = {
            "text": "\n".join(full_text),
            "language": language,
            "confidence": sum(block["confidence"] for block in text_blocks) / len(text_blocks) if text_blocks else 0.0,
            "textBlocks": text_blocks
        }
        
        return response
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e)) 