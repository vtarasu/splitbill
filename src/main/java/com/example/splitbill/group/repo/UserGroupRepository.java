package com.example.splitbill.group.repo;

import com.example.splitbill.group.domain.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
    Optional<UserGroup> findByUserIdAndGroupId(Long userId, Long groupId);
}
