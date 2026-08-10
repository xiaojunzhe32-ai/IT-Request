package com.itop.api.controller;

import com.itop.api.dto.KnownErrorDTO;
import com.itop.api.dto.PageResponse;
import com.itop.common.dto.ApiResponse;
import com.itop.core.entity.KnownError;
import com.itop.core.repository.KnownErrorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "KnownError", description = "Known Error management APIs")
@RestController
@RequestMapping("/known-errors")
@RequiredArgsConstructor
public class KnownErrorController {

    private final KnownErrorRepository knownErrorRepository;

    @Operation(summary = "Get all known errors", description = "Retrieve a paginated list of all known errors")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<KnownErrorDTO>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt") String sort,
            @RequestParam(name = "problemId", required = false) Long problemId,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "search", required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        Page<KnownError> errorPage;

        if (search != null && !search.isEmpty()) {
            errorPage = knownErrorRepository.searchByKeyword(search, pageable);
        } else if (problemId != null) {
            errorPage = knownErrorRepository.findByProblemId(problemId, pageable);
        } else if (type != null && !type.isEmpty()) {
            errorPage = knownErrorRepository.findByErrorType(KnownError.ErrorType.valueOf(type), pageable);
        } else {
            errorPage = knownErrorRepository.findAll(pageable);
        }

        List<KnownErrorDTO> dtos = errorPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResponse<KnownErrorDTO> response = PageResponse.of(dtos, page, size, errorPage.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get known error by ID", description = "Retrieve a single known error by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KnownErrorDTO>> getById(@PathVariable("id") Long id) {
        return knownErrorRepository.findById(id)
                .map(error -> ResponseEntity.ok(ApiResponse.success(toDTO(error))))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Known error not found")));
    }

    @Operation(summary = "Create known error", description = "Create a new known error")
    @PostMapping
    public ResponseEntity<ApiResponse<KnownErrorDTO>> create(@RequestBody KnownErrorDTO dto) {
        if (knownErrorRepository.existsByErrorCode(dto.getErrorCode())) {
            return ResponseEntity.ok(ApiResponse.error(400, "Error code already exists"));
        }

        KnownError error = new KnownError(dto.getErrorCode(), dto.getSymptoms());
        error.setDescription(dto.getDescription());

        if (dto.getProblemId() != null) {
            error.setProblemId(dto.getProblemId());
        }
        if (dto.getCause() != null) {
            error.setCause(dto.getCause());
        }
        if (dto.getWorkaround() != null) {
            error.setWorkaround(dto.getWorkaround());
        }
        if (dto.getSolution() != null) {
            error.setSolution(dto.getSolution());
        }
        if (dto.getErrorType() != null) {
            error.setErrorType(KnownError.ErrorType.valueOf(dto.getErrorType()));
        }
        if (dto.getSeverity() != null) {
            error.setSeverity(KnownError.Severity.valueOf(dto.getSeverity()));
        }
        if (dto.getApplyToAll() != null) {
            error.setApplyToAll(dto.getApplyToAll());
        }

        error.setFirstDetected(LocalDateTime.now());
        error = knownErrorRepository.save(error);
        return ResponseEntity.ok(ApiResponse.success("Known error created", toDTO(error)));
    }

    @Operation(summary = "Update known error", description = "Update an existing known error")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KnownErrorDTO>> update(
            @PathVariable("id") Long id,
            @RequestBody KnownErrorDTO dto) {
        return knownErrorRepository.findById(id)
                .map(error -> {
                    if (dto.getSymptoms() != null) {
                        error.setSymptoms(dto.getSymptoms());
                    }
                    if (dto.getCause() != null) {
                        error.setCause(dto.getCause());
                    }
                    if (dto.getWorkaround() != null) {
                        error.setWorkaround(dto.getWorkaround());
                    }
                    if (dto.getSolution() != null) {
                        error.setSolution(dto.getSolution());
                    }
                    if (dto.getErrorType() != null) {
                        error.setErrorType(KnownError.ErrorType.valueOf(dto.getErrorType()));
                    }
                    if (dto.getSeverity() != null) {
                        error.setSeverity(KnownError.Severity.valueOf(dto.getSeverity()));
                    }

                    error.setLastOccurrence(LocalDateTime.now());
                    error.setOccurrenceCount(error.getOccurrenceCount() + 1);
                    KnownError saved = knownErrorRepository.save(error);
                    return ResponseEntity.ok(ApiResponse.success("Known error updated", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Known error not found")));
    }

    @Operation(summary = "Delete known error", description = "Delete a known error by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        if (!knownErrorRepository.existsById(id)) {
            return ResponseEntity.ok(ApiResponse.error(404, "Known error not found"));
        }
        knownErrorRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Known error deleted", null));
    }

    private KnownErrorDTO toDTO(KnownError error) {
        return KnownErrorDTO.builder()
                .id(error.getId())
                .errorCode(error.getErrorCode())
                .problemId(error.getProblemId())
                .problemName(error.getProblem() != null ? error.getProblem().getName() : null)
                .symptoms(error.getSymptoms())
                .cause(error.getCause())
                .workaround(error.getWorkaround())
                .solution(error.getSolution())
                .errorType(error.getErrorType() != null ? error.getErrorType().name() : null)
                .severity(error.getSeverity() != null ? error.getSeverity().name() : null)
                .applyToAll(error.getApplyToAll())
                .firstDetected(error.getFirstDetected())
                .lastOccurrence(error.getLastOccurrence())
                .occurrenceCount(error.getOccurrenceCount())
                .description(error.getDescription())
                .createdAt(error.getCreatedAt())
                .updatedAt(error.getUpdatedAt())
                .build();
    }
}