package com.itop.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AttachmentDTO {

    private Long id;
    private String entityType;
    private Long entityId;
    private String fileName;
    private String originalName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private String description;
    private Long uploaderId;
    private String uploaderName;
    private Integer downloadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}