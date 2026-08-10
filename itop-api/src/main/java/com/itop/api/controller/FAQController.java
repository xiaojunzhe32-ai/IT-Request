package com.itop.api.controller;

import com.itop.api.dto.FAQDTO;
import com.itop.api.dto.PageResponse;
import com.itop.common.dto.ApiResponse;
import com.itop.core.entity.FAQ;
import com.itop.core.repository.FAQRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "FAQ", description = "FAQ management APIs")
@RestController
@RequestMapping("/faqs")
@RequiredArgsConstructor
public class FAQController {

    private final FAQRepository faqRepository;

    @Operation(summary = "Get all FAQs", description = "Retrieve a paginated list of all FAQs")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FAQDTO>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "sortOrder") String sort,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "serviceId", required = false) Long serviceId,
            @RequestParam(name = "search", required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sort));
        Page<FAQ> faqPage;

        if (search != null && !search.isEmpty()) {
            faqPage = faqRepository.searchPublished(search, pageable);
        } else if (category != null && !category.isEmpty()) {
            faqPage = faqRepository.findByCategory(category, pageable);
        } else if (serviceId != null) {
            faqPage = faqRepository.findByServiceId(serviceId, pageable);
        } else {
            faqPage = faqRepository.findByIsPublishedTrue(pageable);
        }

        List<FAQDTO> dtos = faqPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResponse<FAQDTO> response = PageResponse.of(dtos, page, size, faqPage.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get FAQ by ID", description = "Retrieve a single FAQ by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FAQDTO>> getById(@PathVariable("id") Long id) {
        return faqRepository.findById(id)
                .map(faq -> {
                    // Increment view count
                    faq.setViewCount(faq.getViewCount() + 1);
                    faqRepository.save(faq);
                    return ResponseEntity.ok(ApiResponse.success(toDTO(faq)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "FAQ not found")));
    }

    @Operation(summary = "Create FAQ", description = "Create a new FAQ")
    @PostMapping
    public ResponseEntity<ApiResponse<FAQDTO>> create(@RequestBody FAQDTO dto) {
        FAQ faq = new FAQ(dto.getQuestion(), dto.getAnswer());

        if (dto.getCategory() != null) {
            faq.setCategory(dto.getCategory());
        }
        if (dto.getServiceId() != null) {
            faq.setServiceId(dto.getServiceId());
        }
        if (dto.getKeywords() != null) {
            faq.setKeywords(dto.getKeywords());
        }
        if (dto.getSortOrder() != null) {
            faq.setSortOrder(dto.getSortOrder());
        }
        if (dto.getIsPublished() != null) {
            faq.setIsPublished(dto.getIsPublished());
        }
        if (dto.getAuthorId() != null) {
            faq.setAuthorId(dto.getAuthorId());
        }
        if (dto.getDescription() != null) {
            faq.setDescription(dto.getDescription());
        }

        faq = faqRepository.save(faq);
        return ResponseEntity.ok(ApiResponse.success("FAQ created", toDTO(faq)));
    }

    @Operation(summary = "Update FAQ", description = "Update an existing FAQ")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FAQDTO>> update(
            @PathVariable("id") Long id,
            @RequestBody FAQDTO dto) {
        return faqRepository.findById(id)
                .map(faq -> {
                    if (dto.getQuestion() != null) {
                        faq.setQuestion(dto.getQuestion());
                    }
                    if (dto.getAnswer() != null) {
                        faq.setAnswer(dto.getAnswer());
                    }
                    if (dto.getCategory() != null) {
                        faq.setCategory(dto.getCategory());
                    }
                    if (dto.getKeywords() != null) {
                        faq.setKeywords(dto.getKeywords());
                    }
                    if (dto.getSortOrder() != null) {
                        faq.setSortOrder(dto.getSortOrder());
                    }
                    if (dto.getIsPublished() != null) {
                        faq.setIsPublished(dto.getIsPublished());
                    }

                    FAQ saved = faqRepository.save(faq);
                    return ResponseEntity.ok(ApiResponse.success("FAQ updated", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "FAQ not found")));
    }

    @Operation(summary = "Mark FAQ as helpful", description = "Mark a FAQ as helpful")
    @PostMapping("/{id}/helpful")
    public ResponseEntity<ApiResponse<Void>> markHelpful(@PathVariable("id") Long id) {
        return faqRepository.findById(id)
                .map(faq -> {
                    faq.setHelpfulCount(faq.getHelpfulCount() + 1);
                    faqRepository.save(faq);
                    return ResponseEntity.ok(ApiResponse.<Void>success("Marked as helpful", null));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "FAQ not found")));
    }

    @Operation(summary = "Mark FAQ as not helpful", description = "Mark a FAQ as not helpful")
    @PostMapping("/{id}/not-helpful")
    public ResponseEntity<ApiResponse<Void>> markNotHelpful(@PathVariable("id") Long id) {
        return faqRepository.findById(id)
                .map(faq -> {
                    faq.setNotHelpfulCount(faq.getNotHelpfulCount() + 1);
                    faqRepository.save(faq);
                    return ResponseEntity.ok(ApiResponse.<Void>success("Marked as not helpful", null));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "FAQ not found")));
    }

    @Operation(summary = "Delete FAQ", description = "Delete a FAQ by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        if (!faqRepository.existsById(id)) {
            return ResponseEntity.ok(ApiResponse.error(404, "FAQ not found"));
        }
        faqRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("FAQ deleted", null));
    }

    private FAQDTO toDTO(FAQ faq) {
        return FAQDTO.builder()
                .id(faq.getId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .category(faq.getCategory())
                .serviceId(faq.getServiceId())
                .serviceName(faq.getService() != null ? faq.getService().getName() : null)
                .keywords(faq.getKeywords())
                .viewCount(faq.getViewCount())
                .helpfulCount(faq.getHelpfulCount())
                .notHelpfulCount(faq.getNotHelpfulCount())
                .sortOrder(faq.getSortOrder())
                .isPublished(faq.getIsPublished())
                .authorId(faq.getAuthorId())
                .authorName(faq.getAuthor() != null ? faq.getAuthor().getName() : null)
                .description(faq.getDescription())
                .createdAt(faq.getCreatedAt())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }
}