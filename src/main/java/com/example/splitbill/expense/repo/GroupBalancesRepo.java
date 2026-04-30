package com.example.splitbill.expense.repo;

import com.example.splitbill.expense.domain.GroupBalances;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupBalancesRepo extends JpaRepository<GroupBalances, Long> {
    Optional<GroupBalances> findByUserId1AndUserId2(Long userId1, Long userId2);

    List<GroupBalances> findByGroupId(Long groupId);
}
