package com.itop.api.controller;

import com.itop.api.dto.ChangeTaskDTO;
import com.itop.common.dto.ApiResponse;
import com.itop.core.entity.ChangeTask;
import com.itop.core.repository.ChangeTaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "ChangeTask", description = "Change Task management APIs")
@RestController
@RequestMapping("/change-tasks")
@RequiredArgsConstructor
public class ChangeTaskController {

    private final ChangeTaskRepository changeTaskRepository;

    @Operation(summary = "Get tasks by change ID", description = "Retrieve all tasks for a change request")
    @GetMapping("/by-change/{changeId}")
    public ResponseEntity<ApiResponse<List<ChangeTaskDTO>>> getByChangeId(@PathVariable("changeId") Long changeId) {
        List<ChangeTask> tasks = changeTaskRepository.findByChangeIdOrderBySortOrderAsc(changeId);
        List<ChangeTaskDTO> dtos = tasks.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @Operation(summary = "Get task by ID", description = "Retrieve a single task by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChangeTaskDTO>> getById(@PathVariable("id") Long id) {
        return changeTaskRepository.findById(id)
                .map(task -> ResponseEntity.ok(ApiResponse.success(toDTO(task))))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Change task not found")));
    }

    @Operation(summary = "Create task", description = "Create a new change task")
    @PostMapping
    public ResponseEntity<ApiResponse<ChangeTaskDTO>> create(@RequestBody ChangeTaskDTO dto) {
        ChangeTask task = new ChangeTask(dto.getChangeId(), dto.getName());

        if (dto.getTaskType() != null) {
            task.setTaskType(ChangeTask.TaskType.valueOf(dto.getTaskType()));
        }
        if (dto.getAssigneeId() != null) {
            task.setAssigneeId(dto.getAssigneeId());
        }
        if (dto.getPlannedStartDate() != null) {
            task.setPlannedStartDate(dto.getPlannedStartDate());
        }
        if (dto.getPlannedEndDate() != null) {
            task.setPlannedEndDate(dto.getPlannedEndDate());
        }
        if (dto.getInstructions() != null) {
            task.setInstructions(dto.getInstructions());
        }
        if (dto.getSortOrder() != null) {
            task.setSortOrder(dto.getSortOrder());
        }
        if (dto.getDescription() != null) {
            task.setDescription(dto.getDescription());
        }

        task = changeTaskRepository.save(task);
        return ResponseEntity.ok(ApiResponse.success("Change task created", toDTO(task)));
    }

    @Operation(summary = "Update task", description = "Update an existing change task")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChangeTaskDTO>> update(
            @PathVariable("id") Long id,
            @RequestBody ChangeTaskDTO dto) {
        return changeTaskRepository.findById(id)
                .map(task -> {
                    if (dto.getName() != null) {
                        task.setName(dto.getName());
                    }
                    if (dto.getInstructions() != null) {
                        task.setInstructions(dto.getInstructions());
                    }
                    if (dto.getResult() != null) {
                        task.setResult(dto.getResult());
                    }
                    if (dto.getSortOrder() != null) {
                        task.setSortOrder(dto.getSortOrder());
                    }

                    ChangeTask saved = changeTaskRepository.save(task);
                    return ResponseEntity.ok(ApiResponse.success("Change task updated", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Change task not found")));
    }

    @Operation(summary = "Start task", description = "Mark task as in progress")
    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<ChangeTaskDTO>> start(@PathVariable("id") Long id) {
        return changeTaskRepository.findById(id)
                .map(task -> {
                    task.setTaskStatus(ChangeTask.TaskStatus.IN_PROGRESS);
                    task.setActualStartDate(LocalDateTime.now());
                    ChangeTask saved = changeTaskRepository.save(task);
                    return ResponseEntity.ok(ApiResponse.success("Task started", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Change task not found")));
    }

    @Operation(summary = "Complete task", description = "Mark task as completed")
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<ChangeTaskDTO>> complete(
            @PathVariable("id") Long id,
            @RequestBody(required = false) TaskResultRequest request) {
        return changeTaskRepository.findById(id)
                .map(task -> {
                    task.setTaskStatus(ChangeTask.TaskStatus.COMPLETED);
                    task.setActualEndDate(LocalDateTime.now());
                    if (request != null && request.getResult() != null) {
                        task.setResult(request.getResult());
                    }
                    ChangeTask saved = changeTaskRepository.save(task);
                    return ResponseEntity.ok(ApiResponse.success("Task completed", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Change task not found")));
    }

    @Operation(summary = "Fail task", description = "Mark task as failed")
    @PostMapping("/{id}/fail")
    public ResponseEntity<ApiResponse<ChangeTaskDTO>> fail(
            @PathVariable("id") Long id,
            @RequestBody TaskResultRequest request) {
        return changeTaskRepository.findById(id)
                .map(task -> {
                    task.setTaskStatus(ChangeTask.TaskStatus.FAILED);
                    task.setActualEndDate(LocalDateTime.now());
                    if (request.getResult() != null) {
                        task.setResult(request.getResult());
                    }
                    ChangeTask saved = changeTaskRepository.save(task);
                    return ResponseEntity.ok(ApiResponse.success("Task marked as failed", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Change task not found")));
    }

    @Operation(summary = "Delete task", description = "Delete a change task by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        if (!changeTaskRepository.existsById(id)) {
            return ResponseEntity.ok(ApiResponse.error(404, "Change task not found"));
        }
        changeTaskRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Change task deleted", null));
    }

    private ChangeTaskDTO toDTO(ChangeTask task) {
        return ChangeTaskDTO.builder()
                .id(task.getId())
                .changeId(task.getChangeId())
                .changeNumber(task.getChangeRequest() != null ? task.getChangeRequest().getChangeNumber() : null)
                .taskType(task.getTaskType() != null ? task.getTaskType().name() : null)
                .taskStatus(task.getTaskStatus() != null ? task.getTaskStatus().name() : null)
                .assigneeId(task.getAssigneeId())
                .assigneeName(task.getAssignee() != null ? task.getAssignee().getName() : null)
                .plannedStartDate(task.getPlannedStartDate())
                .plannedEndDate(task.getPlannedEndDate())
                .actualStartDate(task.getActualStartDate())
                .actualEndDate(task.getActualEndDate())
                .instructions(task.getInstructions())
                .result(task.getResult())
                .sortOrder(task.getSortOrder())
                .name(task.getName())
                .description(task.getDescription())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    @lombok.Data
    public static class TaskResultRequest {
        private String result;
    }
}