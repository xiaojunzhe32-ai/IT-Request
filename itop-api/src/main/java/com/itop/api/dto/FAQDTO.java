package com.itop.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FAQDTO {

    private Long id;
    private String question;
    private String answer;
    private String category;
    private Long serviceId;
    private String serviceName;
    private String keywords;
    private Integer viewCount;
    private Integer helpfulCount;
    private Integer notHelpfulCount;
    private Integer sortOrder;
    private Boolean isPublished;
    private Long authorId;
    private String authorName;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}