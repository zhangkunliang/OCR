package com.twx.ocr.util;

import com.twx.ocr.exception.OcrException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件处理工具类
 */
@Slf4j
public class FileUtils {
    
    /**
     * 验证文件是否存在
     */
    public static void validateFileExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new OcrException("文件不存在: " + filePath);
        }
    }
    
    /**
     * 验证文件格式是否支持
     */
    public static void validateFileFormat(String filePath, String[] supportedFormats) {
        String extension = FilenameUtils.getExtension(filePath).toLowerCase();
        boolean isSupported = Arrays.stream(supportedFormats)
                .anyMatch(format -> format.equalsIgnoreCase(extension));
        
        if (!isSupported) {
            throw new OcrException("不支持的文件格式: " + extension + 
                    "，支持的格式: " + Arrays.toString(supportedFormats));
        }
    }
    
    /**
     * 验证文件大小
     */
    public static void validateFileSize(String filePath, int maxSizeMb) {
        File file = new File(filePath);
        long fileSizeBytes = file.length();
        long maxSizeBytes = maxSizeMb * 1024L * 1024L;
        
        if (fileSizeBytes > maxSizeBytes) {
            throw new OcrException("文件大小超过限制: " + 
                    (fileSizeBytes / 1024 / 1024) + "MB，最大允许: " + maxSizeMb + "MB");
        }
    }
    
    /**
     * 创建目录（如果不存在）
     */
    public static void createDirectoryIfNotExists(String dirPath) {
        try {
            Path path = Paths.get(dirPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("创建目录: {}", dirPath);
            }
        } catch (IOException e) {
            throw new OcrException("创建目录失败: " + dirPath, e);
        }
    }
    
    /**
     * 获取目录下的所有图片文件
     */
    public static List<String> getImageFiles(String dirPath, String[] supportedFormats) {
        try {
            Path path = Paths.get(dirPath);
            if (!Files.isDirectory(path)) {
                throw new OcrException("路径不是目录: " + dirPath);
            }
            
            return Files.list(path)
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(filePath -> {
                        String extension = FilenameUtils.getExtension(filePath).toLowerCase();
                        return Arrays.stream(supportedFormats)
                                .anyMatch(format -> format.equalsIgnoreCase(extension));
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new OcrException("读取目录失败: " + dirPath, e);
        }
    }
    
    /**
     * 获取绝对路径
     */
    public static String getAbsolutePath(String path) {
        return new File(path).getAbsolutePath();
    }
    
    /**
     * 检查路径是否为目录
     */
    public static boolean isDirectory(String path) {
        return new File(path).isDirectory();
    }
    
    /**
     * 检查路径是否为文件
     */
    public static boolean isFile(String path) {
        return new File(path).isFile();
    }
}
