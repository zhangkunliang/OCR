package com.twx.ocr.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OCR响应结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcrResponse {
    
    /**
     * 处理是否成功
     */
    private Boolean success;
    
    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
    
    /**
     * 处理时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 单个文件处理结果
     */
    private DocumentClassificationResult result;
    
    /**
     * 批量处理结果列表
     */
    private List<DocumentClassificationResult> results;
    
    /**
     * 处理的文件总数
     */
    private Integer totalProcessed;
    
    /**
     * 成功处理的文件数
     */
    private Integer successCount;
    
    /**
     * 失败处理的文件数
     */
    private Integer failureCount;
}
