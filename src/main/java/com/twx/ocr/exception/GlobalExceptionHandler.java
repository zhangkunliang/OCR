package com.twx.ocr.exception;

import com.twx.ocr.dto.OcrResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理OCR异常
     */
    @ExceptionHandler(OcrException.class)
    public ResponseEntity<OcrResponse> handleOcrException(OcrException e) {
        log.error("OCR处理异常: {}", e.getMessage(), e);
        
        OcrResponse response = OcrResponse.builder()
                .success(false)
                .errorMessage(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<OcrResponse> handleValidationException(Exception e) {
        log.error("参数验证异常: {}", e.getMessage(), e);
        
        String errorMessage = "请求参数验证失败";
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            if (ex.getBindingResult().hasFieldErrors()) {
                errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
            }
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            if (ex.getBindingResult().hasFieldErrors()) {
                errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
            }
        }
        
        OcrResponse response = OcrResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<OcrResponse> handleGenericException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        
        OcrResponse response = OcrResponse.builder()
                .success(false)
                .errorMessage("系统内部错误，请稍后重试")
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
