package com.example.splitbill.expense.repo;

import com.example.splitbill.expense.domain.NonGroupBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NonGroupBalanceRepo extends JpaRepository<NonGroupBalance, Long> {
    Optional<NonGroupBalance> findByFromIdAndToId(Long fromId, Long toId);

    List<NonGroupBalance> findByFromIdOrToId(Long fromId, Long toId);

    List<NonGroupBalance> findByFromId(Long groupId, Long fromId);

    List<NonGroupBalance> findByToId(Long groupId, Long toId);

//    @Query("""
//       SELECT gb FROM GroupBalances gb
//       WHERE gb.group.id = :groupId AND ( gb.from.id IN :userIds OR gb.to.id IN :userIds )
//       """)
//    List<GroupBalances> findBalancesForUsers(Long groupId, List<Long> userIds);
}
