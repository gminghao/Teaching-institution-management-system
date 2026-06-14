package com.institution.coursemanager.util;

import com.institution.coursemanager.exception.ValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传工具类
 */
public class FileUploadUtil {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private FileUploadUtil() {
    }

    /**
     * 上传图片文件
     *
     * @param file      上传的文件
     * @param uploadDir 上传根目录（如 "uploads"）
     * @param subDir    子目录（如 "courses"）
     * @return 可访问的相对路径，如 "/uploads/courses/xxx.jpg"
     */
    public static String upload(MultipartFile file, String uploadDir, String subDir) {
        validateFile(file);

        String extension = getExtension(file);
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        Path targetDir = Paths.get(uploadDir, subDir);
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new ValidationException("创建上传目录失败");
        }

        Path targetPath = targetDir.resolve(fileName);
        try {
            file.transferTo(targetPath.toFile());
        } catch (IOException e) {
            throw new ValidationException("文件写入失败");
        }

        return "/" + uploadDir + "/" + subDir + "/" + fileName;
    }

    private static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("文件大小不能超过5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ValidationException("仅支持 jpg、jpeg、png、gif、webp 格式的图片");
        }
    }

    private static String getExtension(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            return originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        }
        String contentType = file.getContentType();
        if ("image/png".equals(contentType)) return "png";
        if ("image/gif".equals(contentType)) return "gif";
        if ("image/webp".equals(contentType)) return "webp";
        return "jpg";
    }
}
