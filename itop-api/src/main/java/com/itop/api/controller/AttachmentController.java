package com.itop.api.controller;

import com.itop.api.dto.AttachmentDTO;
import com.itop.api.security.SecurityUtils;
import com.itop.api.service.RequestService;
import com.itop.common.dto.ApiResponse;
import com.itop.core.entity.Attachment;
import com.itop.core.entity.RequestComment;
import com.itop.core.repository.AttachmentRepository;
import com.itop.core.repository.RequestCommentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "Attachment", description = "Attachment management APIs")
@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentRepository attachmentRepository;
    private final RequestCommentRepository commentRepository;
    private final RequestService requestService;
    private final SecurityUtils securityUtils;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Operation(summary = "Upload file", description = "Upload a file and attach to an entity")
    @PostMapping("/upload")
    @PreAuthorize("@securityUtils.hasPermission('request:comment')")
    public ResponseEntity<ApiResponse<AttachmentDTO>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId,
            @RequestParam(value = "description", required = false) String description) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "File is empty"));
        }
        AttachmentTarget target = authorizeEntity(entityType, entityId, true);
        if (target == null) {
            return ResponseEntity.ok(ApiResponse.error(403, "You cannot attach files to this resource"));
        }

        // Create upload directory if not exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalName = file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
                ? "attachment" : file.getOriginalFilename().replaceAll("[\\r\\n]", "_");
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
            if (extension.length() > 12) extension = "";
        }
        String fileName = UUID.randomUUID().toString() + extension;

        // Create entity-specific subdirectory
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path entityPath = uploadPath.resolve(target.entityType().toLowerCase()).resolve(datePath);
        if (!Files.exists(entityPath)) {
            Files.createDirectories(entityPath);
        }

        // Save file
        Path filePath = entityPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // Create attachment record
        Attachment attachment = new Attachment(
                target.entityType(), entityId, fileName, originalName,
                filePath.toString(), file.getSize()
        );
        attachment.setContentType(file.getContentType());
        attachment.setDescription(description);
        attachment.setUploaderId(securityUtils.getCurrentUserId());

        attachment = attachmentRepository.save(attachment);
        requestService.recordAttachmentAdded(target.ticketId(), attachment.getId(), attachment.getOriginalName(), target.internal());

        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", toDTO(attachment)));
    }

    @Operation(summary = "Download file", description = "Download an attachment by ID")
    @GetMapping("/download/{id}")
    @PreAuthorize("@securityUtils.hasPermission('request:read')")
    public ResponseEntity<Resource> download(@PathVariable("id") Long id) throws MalformedURLException {
        Attachment attachment = attachmentRepository.findById(id).orElse(null);
        if (attachment == null) {
            return ResponseEntity.notFound().build();
        }
        if (authorizeEntity(attachment.getEntityType(), attachment.getEntityId(), false) == null) {
            return ResponseEntity.status(403).build();
        }

        Path filePath = Paths.get(attachment.getFilePath());
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        // Increment download count
        attachment.setDownloadCount(attachment.getDownloadCount() + 1);
        attachmentRepository.save(attachment);

        Resource resource = new UrlResource(filePath.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType() != null ?
                        attachment.getContentType() : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(attachment.getOriginalName(), StandardCharsets.UTF_8)
                                .build().toString())
                .body(resource);
    }

    @Operation(summary = "Get attachments by entity", description = "Get all attachments for an entity")
    @GetMapping("/by-entity")
    @PreAuthorize("@securityUtils.hasPermission('request:read')")
    public ResponseEntity<ApiResponse<List<AttachmentDTO>>> getByEntity(
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId) {

        AttachmentTarget target = authorizeEntity(entityType, entityId, false);
        if (target == null) {
            return ResponseEntity.ok(ApiResponse.error(403, "Resource is outside your access scope"));
        }
        List<Attachment> attachments = attachmentRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(target.entityType(), entityId);

        List<AttachmentDTO> dtos = attachments.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @Operation(summary = "Get attachment by ID", description = "Get attachment metadata by ID")
    @GetMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('request:read')")
    public ResponseEntity<ApiResponse<AttachmentDTO>> getById(@PathVariable("id") Long id) {
        Attachment attachment = attachmentRepository.findById(id).orElse(null);
        if (attachment == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "Attachment not found"));
        }
        if (authorizeEntity(attachment.getEntityType(), attachment.getEntityId(), false) == null) {
            return ResponseEntity.ok(ApiResponse.error(403, "Resource is outside your access scope"));
        }
        return ResponseEntity.ok(ApiResponse.success(toDTO(attachment)));
    }

    @Operation(summary = "Delete attachment", description = "Delete an attachment by ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('request:comment')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) throws IOException {
        Attachment attachment = attachmentRepository.findById(id).orElse(null);
        if (attachment == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "Attachment not found"));
        }
        if (authorizeEntity(attachment.getEntityType(), attachment.getEntityId(), true) == null
                || (!securityUtils.isITStaff() && !java.util.Objects.equals(attachment.getUploaderId(), securityUtils.getCurrentUserId()))) {
            return ResponseEntity.ok(ApiResponse.error(403, "You cannot delete this attachment"));
        }

        // Delete file from storage
        Path filePath = Paths.get(attachment.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        // Delete record
        attachmentRepository.deleteById(id);

        return ResponseEntity.ok(ApiResponse.success("Attachment deleted", null));
    }

    @Operation(summary = "Get attachment count", description = "Get attachment count for an entity")
    @GetMapping("/count")
    @PreAuthorize("@securityUtils.hasPermission('request:read')")
    public ResponseEntity<ApiResponse<Long>> count(
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId) {

        AttachmentTarget target = authorizeEntity(entityType, entityId, false);
        if (target == null) {
            return ResponseEntity.ok(ApiResponse.error(403, "Resource is outside your access scope"));
        }
        long count = attachmentRepository.countByEntityTypeAndEntityId(target.entityType(), entityId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    private AttachmentDTO toDTO(Attachment attachment) {
        return AttachmentDTO.builder()
                .id(attachment.getId())
                .entityType(attachment.getEntityType())
                .entityId(attachment.getEntityId())
                .fileName(attachment.getFileName())
                .originalName(attachment.getOriginalName())
                .fileSize(attachment.getFileSize())
                .contentType(attachment.getContentType())
                .description(attachment.getDescription())
                .uploaderId(attachment.getUploaderId())
                .uploaderName(attachment.getUploader() != null ? attachment.getUploader().getName() : null)
                .downloadCount(attachment.getDownloadCount())
                .createdAt(attachment.getCreatedAt())
                .updatedAt(attachment.getUpdatedAt())
                .build();
    }

    private AttachmentTarget authorizeEntity(String entityType, Long entityId, boolean write) {
        if (entityType == null || entityId == null) {
            return null;
        }
        String normalizedType = entityType.trim().toUpperCase();
        if ("REQUEST".equals(normalizedType)) {
            boolean allowed = write ? requestService.canComment(entityId) : requestService.canView(entityId);
            return allowed ? new AttachmentTarget("REQUEST", entityId, false) : null;
        }
        if ("REQUEST_COMMENT".equals(normalizedType)) {
            RequestComment comment = commentRepository.findById(entityId).orElse(null);
            if (comment == null) {
                return null;
            }
            boolean allowed = write ? requestService.canComment(comment.getTicketId())
                    : requestService.canView(comment.getTicketId());
            if (!allowed) {
                return null;
            }
            return new AttachmentTarget("REQUEST_COMMENT", comment.getTicketId(),
                    comment.getLogType() == RequestComment.LogType.INTERNAL);
        }
        return null;
    }

    private record AttachmentTarget(String entityType, Long ticketId, boolean internal) { }
}
