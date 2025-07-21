package com.twx.ocr.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI配置类
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OCR证件识别系统 API")
                        .description("基于Spring Boot和Python PaddleOCR的证件识别分类系统API文档\n\n" +
                                "## 功能特性\n" +
                                "- 🔍 多证件类型识别：身份证、营业执照、驾驶证、护照\n" +
                                "- 📝 文字提取：提取证件中的所有文字内容\n" +
                                "- 🏷️ 智能分类：基于关键词和正则表达式的智能证件分类\n" +
                                "- 📁 批量处理：支持单个文件和目录批量处理\n\n" +
                                "## 支持的文件格式\n" +
                                "JPG, JPEG, PNG, BMP, TIFF, WebP")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("zkl1005789053@163.com")
                                .url("https://github.com/ocr-system"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
