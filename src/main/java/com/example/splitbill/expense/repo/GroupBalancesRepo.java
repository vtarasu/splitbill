package com.example.splitbill.expense.repo;

import com.example.splitbill.expense.domain.GroupBalances;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupBalancesRepo extends JpaRepository<GroupBalances, Long> {
    Optional<GroupBalances> findByGroupIdAndFromIdAndToId(Long groupId, Long fromId, Long toId);

    List<GroupBalances> findByGroupId(Long groupId);

    List<GroupBalances> findByFromIdOrToId(Long fromId, Long toId);

    List<GroupBalances> findByGroupIdAndFromIdOrGroupIdAndToId(Long groupId, Long userId, Long groupId1, Long userId1);

    List<GroupBalances> findByFromIdAndToId(Long fromUserId, Long toUserId);

    Boolean existsByGroupId(Long id);
}
