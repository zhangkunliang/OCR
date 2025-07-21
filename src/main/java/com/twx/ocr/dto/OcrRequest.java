package com.twx.ocr.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * OCR请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrRequest {
    
    /**
     * 图片文件路径或目录路径
     */
    @NotBlank(message = "图片路径不能为空")
    private String imagePath;
    
    /**
     * 是否批量处理（当路径为目录时）
     */
    private Boolean batchProcess = false;
    
    /**
     * 是否保存结果到文件
     */
    private Boolean saveToFile = true;
    
    /**
     * 输出目录（可选，默认为output）
     */
    private String outputDir = "output";
}
