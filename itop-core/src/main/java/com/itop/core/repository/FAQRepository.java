package com.itop.core.repository;

import com.itop.core.entity.FAQ;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FAQRepository extends JpaRepository<FAQ, Long>,
        JpaSpecificationExecutor<FAQ> {

    List<FAQ> findByCategory(String category);

    Page<FAQ> findByCategory(String category, Pageable pageable);

    List<FAQ> findByServiceId(Long serviceId);

    Page<FAQ> findByServiceId(Long serviceId, Pageable pageable);

    Page<FAQ> findByIsPublishedTrue(Pageable pageable);

    @Query("SELECT f FROM FAQ f WHERE f.isPublished = true AND " +
           "(LOWER(f.question) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.answer) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.keywords) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<FAQ> searchPublished(@Param("keyword") String keyword, Pageable pageable);

    List<FAQ> findByIsPublishedTrueOrderByViewCountDesc();

    List<FAQ> findByIsPublishedTrueOrderByHelpfulCountDesc(Pageable pageable);
}