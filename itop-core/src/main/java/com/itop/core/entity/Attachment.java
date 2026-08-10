package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "attachment")
@Getter
@Setter
@NoArgsConstructor
public class Attachment extends BaseEntity {

    @Column(name = "entity_type", length = 50, nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "original_name", length = 255, nullable = false)
    private String originalName;

    @Column(name = "file_path", length = 500, nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "uploader_id")
    private Long uploaderId;

    @Column(name = "download_count")
    private Integer downloadCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", insertable = false, updatable = false)
    private User uploader;

    public Attachment(String entityType, Long entityId, String fileName, String originalName,
                      String filePath, Long fileSize) {
        this.setName(originalName);
        this.entityType = entityType;
        this.entityId = entityId;
        this.fileName = fileName;
        this.originalName = originalName;
        this.filePath = filePath;
        this.fileSize = fileSize;
    }
}
