package com.example.splitbill.expense.repo;

import com.example.splitbill.expense.domain.GroupBalances;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupBalancesRepo extends JpaRepository<GroupBalances, Long> {
    Optional<GroupBalances> findByGroupIdAndFromIdAndToId(Long groupId, Long fromId, Long toId);

    List<GroupBalances> findByGroupId(Long groupId);

    List<GroupBalances> findByGroupIdAndFromId(Long groupId, Long userId);

    List<GroupBalances> findByGroupIdAndToId(Long groupId, Long userId);
}
