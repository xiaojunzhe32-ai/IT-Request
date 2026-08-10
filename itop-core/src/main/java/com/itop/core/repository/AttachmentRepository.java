package com.itop.core.repository;

import com.itop.core.entity.Attachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    Page<Attachment> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    List<Attachment> findByUploaderIdOrderByCreatedAtDesc(Long uploaderId);

    long countByEntityTypeAndEntityId(String entityType, Long entityId);
}