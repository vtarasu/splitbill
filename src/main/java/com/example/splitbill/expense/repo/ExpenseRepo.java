package com.example.splitbill.expense.repo;

import com.example.splitbill.expense.domain.Expense;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepo extends JpaRepository<Expense, Long> {
    List<Expense> findAllByGroupId(Long groupId, Pageable pageable);

    List<Expense> findAllByGroupId(Long groupId);
}
