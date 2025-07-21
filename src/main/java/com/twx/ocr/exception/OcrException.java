package com.twx.ocr.exception;

/**
 * OCR处理异常
 */
public class OcrException extends RuntimeException {
    
    public OcrException(String message) {
        super(message);
    }
    
    public OcrException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public OcrException(Throwable cause) {
        super(cause);
    }
}
