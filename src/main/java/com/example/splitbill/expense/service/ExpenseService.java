package com.example.splitbill.expense.service;

import com.example.splitbill.expense.domain.Expense;
import com.example.splitbill.expense.domain.ExpenseSplit;
import com.example.splitbill.expense.domain.GroupBalances;
import com.example.splitbill.expense.dto.AddExpenseRequestDto;
import com.example.splitbill.expense.repo.ExpenseRepo;
import com.example.splitbill.expense.service.strategy.ExpenseSplitStrategy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {
    private final ExpenseRepo expenseRepo;
    private final GroupBalanceService groupBalanceService;

    public ExpenseService(ExpenseRepo expenseRepo, GroupBalanceService groupBalanceService) {
        this.expenseRepo = expenseRepo;
        this.groupBalanceService = groupBalanceService;
    }

    public List<GroupBalances> addExpense(AddExpenseRequestDto addExpenseRequestDto) {
        List<ExpenseSplit> splits = ExpenseSplitStrategy.getExpenseSplitStrategy(addExpenseRequestDto.getSplitStrategy())
                .splitExpense(addExpenseRequestDto);

        Expense expense = Expense.builder()
                .expense(addExpenseRequestDto.getExpenseName())
                .userId(addExpenseRequestDto.getAddedByUser())
                .groupId(addExpenseRequestDto.getGroupId())
                .addedAt(addExpenseRequestDto.getExpenseDate())
                .split(splits)
                .build();

        splits.forEach(expenseSplit -> expenseSplit.setExpense(expense));
        var savedExpense = expenseRepo.save(expense);
        var groupBalances = groupBalanceService.updateGroupBalance(addExpenseRequestDto.getGroupId(), splits);
        return groupBalances;
    }
}
