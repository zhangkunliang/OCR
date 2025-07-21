package com.twx.ocr.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

/**
 * 文档分类识别结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentClassificationResult {
    
    /**
     * 图片文件路径
     */
    private String imagePath;
    
    /**
     * 识别的文档类型（身份证、营业执照、驾驶证、护照、未知类型）
     */
    private String documentType;
    
    /**
     * 识别到的文本列表
     */
    private List<String> recTexts;
    
    /**
     * 错误信息（如果处理失败）
     */
    private String error;
    
    /**
     * 置信度（高、中、低）
     */
    private String confidence;
    
    /**
     * 处理是否成功
     */
    private Boolean success;
    
    /**
     * 输出文件路径（如果保存了结果文件）
     */
    private String outputFilePath;
}
