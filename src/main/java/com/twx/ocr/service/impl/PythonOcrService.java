package com.twx.ocr.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twx.ocr.config.OcrConfig;
import com.twx.ocr.dto.DocumentClassificationResult;
import com.twx.ocr.dto.OcrRequest;
import com.twx.ocr.dto.OcrResponse;
import com.twx.ocr.exception.OcrException;
import com.twx.ocr.service.OcrService;
import com.twx.ocr.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Python OCR服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PythonOcrService implements OcrService {
    
    private final OcrConfig ocrConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public OcrResponse processOcr(OcrRequest request) {
        try {
            // 验证输入参数
            validateRequest(request);
            
            String imagePath = request.getImagePath();
            
            // 检查路径类型
            if (FileUtils.isFile(imagePath)) {
                return processSingleImage(imagePath);
            } else if (FileUtils.isDirectory(imagePath)) {
                if (request.getBatchProcess()) {
                    return processBatchImages(imagePath);
                } else {
                    throw new OcrException("指定路径是目录，请设置batchProcess=true进行批量处理");
                }
            } else {
                throw new OcrException("指定的路径既不是文件也不是目录: " + imagePath);
            }
            
        } catch (Exception e) {
            log.error("OCR处理失败", e);
            return OcrResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
    
    @Override
    public OcrResponse processSingleImage(String imagePath) {
        try {
            // 验证文件
            validateImageFile(imagePath);
            
            // 执行Python脚本
            String result = executePythonScript(imagePath);
            
            // 解析结果
            DocumentClassificationResult classificationResult = parseResult(result, imagePath);
            
            return OcrResponse.builder()
                    .success(true)
                    .timestamp(LocalDateTime.now())
                    .result(classificationResult)
                    .totalProcessed(1)
                    .successCount(classificationResult.getSuccess() ? 1 : 0)
                    .failureCount(classificationResult.getSuccess() ? 0 : 1)
                    .build();
                    
        } catch (Exception e) {
            log.error("处理单个图片失败: {}", imagePath, e);
            return OcrResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .totalProcessed(1)
                    .successCount(0)
                    .failureCount(1)
                    .build();
        }
    }
    
    @Override
    public OcrResponse processBatchImages(String directoryPath) {
        try {
            // 获取目录下的所有图片文件
            List<String> imageFiles = FileUtils.getImageFiles(directoryPath, ocrConfig.getSupportedFormats());
            
            if (imageFiles.isEmpty()) {
                throw new OcrException("目录中未找到支持的图片文件: " + directoryPath);
            }
            
            List<DocumentClassificationResult> results = new ArrayList<>();
            int successCount = 0;
            int failureCount = 0;
            
            // 处理每个图片文件
            for (String imageFile : imageFiles) {
                try {
                    String result = executePythonScript(imageFile);
                    DocumentClassificationResult classificationResult = parseResult(result, imageFile);
                    results.add(classificationResult);
                    
                    if (classificationResult.getSuccess()) {
                        successCount++;
                    } else {
                        failureCount++;
                    }
                } catch (Exception e) {
                    log.error("处理图片失败: {}", imageFile, e);
                    DocumentClassificationResult errorResult = DocumentClassificationResult.builder()
                            .imagePath(imageFile)
                            .success(false)
                            .error(e.getMessage())
                            .build();
                    results.add(errorResult);
                    failureCount++;
                }
            }
            
            return OcrResponse.builder()
                    .success(true)
                    .timestamp(LocalDateTime.now())
                    .results(results)
                    .totalProcessed(imageFiles.size())
                    .successCount(successCount)
                    .failureCount(failureCount)
                    .build();
                    
        } catch (Exception e) {
            log.error("批量处理图片失败: {}", directoryPath, e);
            return OcrResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * 验证请求参数
     */
    private void validateRequest(OcrRequest request) {
        if (request == null) {
            throw new OcrException("请求参数不能为空");
        }
        if (request.getImagePath() == null || request.getImagePath().trim().isEmpty()) {
            throw new OcrException("图片路径不能为空");
        }
    }

    /**
     * 验证图片文件
     */
    private void validateImageFile(String imagePath) {
        FileUtils.validateFileExists(imagePath);
        FileUtils.validateFileFormat(imagePath, ocrConfig.getSupportedFormats());
        FileUtils.validateFileSize(imagePath, ocrConfig.getMaxFileSizeMb());
    }

    /**
     * 执行Python脚本
     */
    private String executePythonScript(String imagePath) throws IOException, InterruptedException {
        // 构建命令
        String absoluteImagePath = FileUtils.getAbsolutePath(imagePath);
        String absoluteScriptPath = FileUtils.getAbsolutePath(ocrConfig.getScriptPath());

        ProcessBuilder processBuilder = new ProcessBuilder(
                ocrConfig.getPythonPath(),
                absoluteScriptPath,
                absoluteImagePath
        );

        // 设置工作目录
        processBuilder.directory(new File(System.getProperty("user.dir")));

        // 设置环境变量以确保UTF-8编码
        processBuilder.environment().put("PYTHONIOENCODING", "utf-8");

        if (ocrConfig.getDebugMode()) {
            log.info("执行命令: {} {} {}",
                    ocrConfig.getPythonPath(), absoluteScriptPath, absoluteImagePath);
        }

        // 启动进程
        Process process = processBuilder.start();

        // 读取输出
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"))) {

            String line;
            boolean jsonStarted = false;
            while ((line = reader.readLine()) != null) {
                // 只保留JSON部分，忽略Python的print输出
                if (line.trim().startsWith("{") || jsonStarted) {
                    jsonStarted = true;
                    output.append(line).append("\n");
                } else if (line.trim().startsWith("}")) {
                    output.append(line).append("\n");
                    break; // JSON结束
                }
            }

            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
        }

        // 等待进程完成
        boolean finished = process.waitFor(ocrConfig.getTimeoutSeconds(), TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            throw new OcrException("Python脚本执行超时");
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            String errorMsg = errorOutput.toString();
            log.error("Python脚本执行失败，退出码: {}, 错误信息: {}", exitCode, errorMsg);
            throw new OcrException("Python脚本执行失败: " + errorMsg);
        }

        String result = output.toString().trim();
        if (ocrConfig.getDebugMode()) {
            log.info("Python脚本输出: {}", result);
        }

        return result;
    }

    /**
     * 解析Python脚本的输出结果
     */
    private DocumentClassificationResult parseResult(String jsonResult, String imagePath) {
        try {
            // 清理输出，只保留JSON部分
            String cleanJson = extractJsonFromOutput(jsonResult);

            if (ocrConfig.getDebugMode()) {
                log.info("清理后的JSON: {}", cleanJson);
            }

            JsonNode jsonNode = objectMapper.readTree(cleanJson);

            // 检查是否有错误
            if (jsonNode.has("error")) {
                return DocumentClassificationResult.builder()
                        .imagePath(imagePath)
                        .success(false)
                        .error(jsonNode.get("error").asText())
                        .build();
            }

            // 解析正常结果
            String documentType = jsonNode.has("document_type") ?
                    jsonNode.get("document_type").asText() : "未知类型";

            List<String> recTexts = new ArrayList<>();
            if (jsonNode.has("rec_texts") && jsonNode.get("rec_texts").isArray()) {
                for (JsonNode textNode : jsonNode.get("rec_texts")) {
                    recTexts.add(textNode.asText());
                }
            }

            return DocumentClassificationResult.builder()
                    .imagePath(imagePath)
                    .documentType(documentType)
                    .recTexts(recTexts)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("解析Python脚本结果失败: {}", jsonResult, e);
            return DocumentClassificationResult.builder()
                    .imagePath(imagePath)
                    .success(false)
                    .error("解析结果失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 从输出中提取JSON部分
     */
    private String extractJsonFromOutput(String output) {
        if (output == null || output.trim().isEmpty()) {
            return "{}";
        }

        // 查找JSON开始和结束位置
        int jsonStart = output.indexOf("{");
        int jsonEnd = output.lastIndexOf("}");

        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            return output.substring(jsonStart, jsonEnd + 1);
        }

        // 如果没有找到完整的JSON，返回原始输出
        return output.trim();
    }
}
