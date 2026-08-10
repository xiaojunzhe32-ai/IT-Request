package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * FAQ - Frequently Asked Questions
 * Self-service knowledge base for end users
 */
@Entity
@Table(name = "faq")
@Getter
@Setter
@NoArgsConstructor
public class FAQ extends BaseEntity {

    @Column(name = "question", columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(name = "answer", columnDefinition = "TEXT", nullable = false)
    private String answer;

    @Column(name = "faq_category", length = 100)
    private String category;

    @Column(name = "service_id")
    private Long serviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", insertable = false, updatable = false)
    private Service service;

    @Column(name = "keywords", length = 500)
    private String keywords;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;

    @Column(name = "not_helpful_count")
    private Integer notHelpfulCount = 0;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_published")
    private Boolean isPublished = true;

    @Column(name = "author_id")
    private Long authorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    private Person author;

    public FAQ(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }
}