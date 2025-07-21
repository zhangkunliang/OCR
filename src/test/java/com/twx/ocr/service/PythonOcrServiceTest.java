package com.twx.ocr.service;

import com.twx.ocr.config.OcrConfig;
import com.twx.ocr.dto.OcrRequest;
import com.twx.ocr.dto.OcrResponse;
import com.twx.ocr.service.impl.PythonOcrService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Python OCR服务测试类
 */
@ExtendWith(MockitoExtension.class)
class PythonOcrServiceTest {
    
    @Mock
    private OcrConfig ocrConfig;
    
    @InjectMocks
    private PythonOcrService pythonOcrService;
    
    @BeforeEach
    void setUp() {
        // 设置默认配置
        when(ocrConfig.getPythonPath()).thenReturn("python");
        when(ocrConfig.getScriptPath()).thenReturn("src/main/resources/python/ocr_classifier.py");
        when(ocrConfig.getTimeoutSeconds()).thenReturn(300);
        when(ocrConfig.getDebugMode()).thenReturn(false);
        when(ocrConfig.getSupportedFormats()).thenReturn(new String[]{"jpg", "jpeg", "png", "bmp", "tiff", "webp"});
        when(ocrConfig.getMaxFileSizeMb()).thenReturn(10);
    }
    
    @Test
    void testProcessOcrWithNullRequest() {
        // 测试空请求
        OcrResponse response = pythonOcrService.processOcr(null);
        
        assertFalse(response.getSuccess());
        assertNotNull(response.getErrorMessage());
        assertTrue(response.getErrorMessage().contains("请求参数不能为空"));
    }
    
    @Test
    void testProcessOcrWithEmptyImagePath() {
        // 测试空图片路径
        OcrRequest request = new OcrRequest();
        request.setImagePath("");
        
        OcrResponse response = pythonOcrService.processOcr(request);
        
        assertFalse(response.getSuccess());
        assertNotNull(response.getErrorMessage());
        assertTrue(response.getErrorMessage().contains("图片路径不能为空"));
    }
    
    @Test
    void testProcessOcrWithNonExistentFile() {
        // 测试不存在的文件
        OcrRequest request = new OcrRequest();
        request.setImagePath("non_existent_file.jpg");
        
        OcrResponse response = pythonOcrService.processOcr(request);
        
        assertFalse(response.getSuccess());
        assertNotNull(response.getErrorMessage());
    }
}
