package com.example.splitbill.expense.service;

import com.example.splitbill.expense.domain.Expense;
import com.example.splitbill.expense.domain.ExpenseSplit;
import com.example.splitbill.expense.dto.AddExpenseRequestDto;
import com.example.splitbill.expense.repo.ExpenseRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {
    private final ExpenseRepo expenseRepo;

    public ExpenseService(ExpenseRepo expenseRepo) {
        this.expenseRepo = expenseRepo;
    }

    public Expense addExpense(AddExpenseRequestDto addExpenseRequestDto) {
        List<ExpenseSplit> splits = ExpenseSplitStrategy.getExpenseSplitStrategy(addExpenseRequestDto.getSplitStrategy())
                .splitExpense(addExpenseRequestDto);

        Expense expense = Expense.builder()
                .expense(addExpenseRequestDto.getExpenseName())
                .userId(addExpenseRequestDto.getAddedByUser())
                .groupId(addExpenseRequestDto.getGroupId())
                .addedAt(addExpenseRequestDto.getExpenseDate())
                .split(splits)
                .build();

        var savedexpense = expenseRepo.save(expense);
        return savedexpense;
    }
}
