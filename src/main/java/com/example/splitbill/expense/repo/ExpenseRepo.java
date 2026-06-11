package com.example.splitbill.expense.repo;

import com.example.splitbill.expense.domain.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ExpenseRepo extends JpaRepository<Expense, Long> {
    Page<Expense> findAllByGroupId(Long groupId, Pageable pageable);

    @Query("""
                select distinct e from Expense e
                left join e.split s
                where e.group is null
                  and ( e.paidByUser.id = :userId or s.owedBy.id = :userId )
            """)
    Page<Expense> findAllNonGroupExpensesForUser(Long userId, Pageable pageable);

    Long countByAddedByUser_IdAndDateAddedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
