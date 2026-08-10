package com.itop.core.repository;

import com.itop.core.entity.UserAccessibleOrg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAccessibleOrgRepository extends JpaRepository<UserAccessibleOrg, UserAccessibleOrg.PK> {

    List<UserAccessibleOrg> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
