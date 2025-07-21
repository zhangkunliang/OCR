package com.twx.ocr.service;

import com.twx.ocr.dto.OcrRequest;
import com.twx.ocr.dto.OcrResponse;

/**
 * OCR服务接口
 */
public interface OcrService {
    
    /**
     * 处理OCR识别请求
     * 
     * @param request OCR请求参数
     * @return OCR处理结果
     */
    OcrResponse processOcr(OcrRequest request);
    
    /**
     * 处理单个图片文件
     * 
     * @param imagePath 图片文件路径
     * @return OCR处理结果
     */
    OcrResponse processSingleImage(String imagePath);
    
    /**
     * 批量处理目录下的图片文件
     * 
     * @param directoryPath 目录路径
     * @return OCR处理结果
     */
    OcrResponse processBatchImages(String directoryPath);
}
