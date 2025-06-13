package cn.mojoup.ai.rag.reader.ocr.model;

import lombok.Data;
import java.util.List;

@Data
public class TableRow {
    /**
     * 行中的单元格列表
     */
    private List<TableCell> cells;
} 