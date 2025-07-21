package com.twx.ocr.controller;

import com.twx.ocr.dto.OcrRequest;
import com.twx.ocr.dto.OcrResponse;
import com.twx.ocr.service.OcrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * OCR识别控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
@Validated
@Tag(name = "OCR证件识别", description = "OCR证件识别相关API接口")
public class OcrController {
    
    private final OcrService ocrService;
    
    @Operation(
            summary = "处理OCR识别请求",
            description = "根据请求参数处理单个文件或批量处理目录下的图片文件，支持身份证、营业执照、驾驶证、护照等证件类型识别"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "处理成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/process")
    public ResponseEntity<OcrResponse> processOcr(
            @Parameter(description = "OCR请求参数", required = true)
            @Valid @RequestBody OcrRequest request) {
        log.info("收到OCR处理请求: {}", request);
        
        OcrResponse response = ocrService.processOcr(request);
        
        log.info("OCR处理完成，成功: {}", response.getSuccess());
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "处理单个图片文件",
            description = "处理指定路径的单个图片文件，识别证件类型并提取文字内容"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "处理成功"),
            @ApiResponse(responseCode = "400", description = "文件路径无效或文件不存在"),
            @ApiResponse(responseCode = "500", description = "处理过程中发生错误")
    })
    @PostMapping("/process-single")
    public ResponseEntity<OcrResponse> processSingleImage(
            @Parameter(description = "图片文件的完整路径", required = true, example = "D:/images/idcard.jpg")
            @RequestParam String imagePath) {
        log.info("收到单个图片处理请求: {}", imagePath);
        
        OcrResponse response = ocrService.processSingleImage(imagePath);
        
        log.info("单个图片处理完成，成功: {}", response.getSuccess());
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "批量处理目录下的图片文件",
            description = "批量处理指定目录下的所有支持格式的图片文件，返回每个文件的处理结果"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "批量处理完成"),
            @ApiResponse(responseCode = "400", description = "目录路径无效或目录不存在"),
            @ApiResponse(responseCode = "500", description = "批量处理过程中发生错误")
    })
    @PostMapping("/process-batch")
    public ResponseEntity<OcrResponse> processBatchImages(
            @Parameter(description = "包含图片文件的目录路径", required = true, example = "D:/images/")
            @RequestParam String directoryPath) {
        log.info("收到批量图片处理请求: {}", directoryPath);
        
        OcrResponse response = ocrService.processBatchImages(directoryPath);
        
        log.info("批量图片处理完成，成功: {}, 总数: {}, 成功: {}, 失败: {}", 
                response.getSuccess(), 
                response.getTotalProcessed(),
                response.getSuccessCount(),
                response.getFailureCount());
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "健康检查",
            description = "检查OCR服务是否正常运行"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "服务正常")
    })
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OCR服务运行正常");
    }
}
