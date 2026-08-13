package com.itop.api.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

@Component
public class FileValidationUtil {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024L;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "png", "jpg", "jpeg", "gif", "bmp", "svg",
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
        "txt", "log", "csv", "md",
        "zip", "7z", "gz", "tar",
        "html"
    );

    private static final Map<String, byte[]> DANGEROUS_MAGIC = Map.of(
        "exe/dll", new byte[]{0x4D, 0x5A},
        "elf", new byte[]{0x7F, 0x45, 0x4C, 0x46},
        "class", new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE}
    );

    public void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小超过 20MB 限制");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank() || !originalName.contains(".")) {
            throw new IllegalArgumentException("文件名缺少扩展名");
        }

        String ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("不支持的文件类型: ." + ext);
        }

        try {
            byte[] header = new byte[8];
            try (InputStream is = file.getInputStream()) {
                int read = is.read(header);
                if (read < 2) return;
            }
            for (var entry : DANGEROUS_MAGIC.entrySet()) {
                if (startsWith(header, entry.getValue())) {
                    throw new IllegalArgumentException("检测到危险文件类型: " + entry.getKey());
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("文件读取失败");
        }
    }

    private boolean startsWith(byte[] data, byte[] pattern) {
        if (data.length < pattern.length) return false;
        for (int i = 0; i < pattern.length; i++) {
            if (data[i] != pattern[i]) return false;
        }
        return true;
    }
}
