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
}
