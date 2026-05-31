package com.example.splitbill.user.repo;

import com.example.splitbill.user.domain.Settlements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SettlementsRepository extends JpaRepository<Settlements, Long> {
    Page<Settlements> findByFromIdOrToId(Long from, Long to, Pageable pageable);

    @Query("""
       SELECT COALESCE(SUM(s.amount), 0)
       FROM Settlements s
       WHERE s.from.id = :userId
          OR s.to.id = :userId
       """)
    BigDecimal getTotalSettledAmount(Long userId);
}
