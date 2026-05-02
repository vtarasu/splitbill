package com.example.splitbill.expense.service;

import com.example.splitbill.expense.domain.Expense;
import com.example.splitbill.expense.domain.ExpenseSplit;
import com.example.splitbill.expense.domain.GroupBalances;
import com.example.splitbill.expense.dto.AddExpenseRequestDto;
import com.example.splitbill.expense.dto.GetExpensesRequestDto;
import com.example.splitbill.expense.dto.GetExpensesResponseDto;
import com.example.splitbill.expense.dto.SplitStrategy;
import com.example.splitbill.expense.repo.ExpenseRepo;
import com.example.splitbill.expense.service.strategy.ExpenseSplitStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
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

        log.info("Expense splits computed successfully for expenseId={}.", addExpenseRequestDto.getGroupId());
        var splitDetails = addExpenseRequestDto.getUsersSharingExpense().toString();
        if(addExpenseRequestDto.getSplitStrategy().equals(SplitStrategy.EQUAL)) {
            splitDetails = addExpenseRequestDto.getUsersSharingExpense().keySet().toString();
        }

        Expense expense = Expense.builder()
                .expense(addExpenseRequestDto.getExpenseName())
                .userId(addExpenseRequestDto.getAddedByUser())
                .groupId(addExpenseRequestDto.getGroupId())
                .addedAt(addExpenseRequestDto.getExpenseDate())
                .billAmount(addExpenseRequestDto.getAmount())
                .splitStrategy(addExpenseRequestDto.getSplitStrategy())
                .split(splits)
                .splitDetails(splitDetails)
                .build();

        splits.forEach(expenseSplit -> expenseSplit.setExpense(expense));
        var savedExpense = expenseRepo.save(expense);
        var groupBalances = groupBalanceService.updateGroupBalance(addExpenseRequestDto.getGroupId(), splits);
        log.info("Expense saved successfully and group balances updated. savedExpense={}", savedExpense.getId());
        return groupBalances;
    }

    public List<GetExpensesResponseDto> getExpenses(GetExpensesRequestDto getExpensesRequestDto) {
        var pageable = PageRequest.of(getExpensesRequestDto.getPageNo(), getExpensesRequestDto.getPageSize(),
                Sort.by("addedAt").descending());
        var expenses = expenseRepo.findAllByGroupId(getExpensesRequestDto.getGroupId(), pageable);
        return expenses.stream().map(GetExpensesResponseDto::from).toList();
    }
}
