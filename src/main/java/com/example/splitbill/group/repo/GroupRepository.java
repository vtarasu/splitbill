package com.example.splitbill.group.repo;

import com.example.splitbill.group.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findGroupByGroupName(String groupName);

    Optional<Group> findGroupById(Long id);
}
