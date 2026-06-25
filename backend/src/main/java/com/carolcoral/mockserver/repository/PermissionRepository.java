package com.carolcoral.mockserver.repository;

import com.carolcoral.mockserver.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByCode(String code);
    List<Permission> findByGroupNameOrderBySortOrderAsc(String groupName);
    List<Permission> findAllByOrderBySortOrderAsc();
    boolean existsByCode(String code);
}
