package com.itop.core.repository;

import com.itop.core.entity.CodeTableItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeTableItemRepository extends JpaRepository<CodeTableItem, Long> {

    List<CodeTableItem> findByTableCodeOrderBySortOrderAscNameAsc(String tableCode);

    List<CodeTableItem> findByTableCodeAndStatusIgnoreCaseOrderBySortOrderAscNameAsc(String tableCode, String status);

    Optional<CodeTableItem> findByTableCodeAndCodeIgnoreCase(String tableCode, String code);
}
