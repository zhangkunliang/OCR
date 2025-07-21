package com.twx.ocr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * OCR配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ocr")
public class OcrConfig {
    
    /**
     * Python解释器路径
     */
    private String pythonPath = "python";
    
    /**
     * Python脚本路径
     */
    private String scriptPath = "src/main/resources/python/ocr_classifier.py";
    
    /**
     * 默认输出目录
     */
    private String defaultOutputDir = "output";
    
    /**
     * 脚本执行超时时间（秒）
     */
    private Integer timeoutSeconds = 300;
    
    /**
     * 是否启用调试模式
     */
    private Boolean debugMode = false;
    
    /**
     * 支持的图片格式
     */
    private String[] supportedFormats = {"jpg", "jpeg", "png", "bmp", "tiff", "webp"};
    
    /**
     * 最大文件大小（MB）
     */
    private Integer maxFileSizeMb = 10;
}
