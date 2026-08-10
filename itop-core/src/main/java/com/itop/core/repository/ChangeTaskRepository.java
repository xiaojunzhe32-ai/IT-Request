package com.itop.core.repository;

import com.itop.core.entity.ChangeTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeTaskRepository extends JpaRepository<ChangeTask, Long>,
        JpaSpecificationExecutor<ChangeTask> {

    List<ChangeTask> findByChangeId(Long changeId);

    Page<ChangeTask> findByChangeId(Long changeId, Pageable pageable);

    List<ChangeTask> findByAssigneeId(Long assigneeId);

    List<ChangeTask> findByTaskStatus(ChangeTask.TaskStatus taskStatus);

    List<ChangeTask> findByChangeIdOrderBySortOrderAsc(Long changeId);
}