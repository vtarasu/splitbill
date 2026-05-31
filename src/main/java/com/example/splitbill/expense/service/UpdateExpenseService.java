package com.example.splitbill.expense.service;

import com.example.splitbill.expense.domain.Expense;
import com.example.splitbill.expense.domain.GroupBalances;
import com.example.splitbill.expense.dto.*;
import com.example.splitbill.expense.exception.ExpenseDoesNotExistsException;
import com.example.splitbill.expense.repo.ExpenseRepo;
import com.example.splitbill.group.repo.GroupRepository;
import com.example.splitbill.user.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class UpdateExpenseService {
    private final ExpenseRepo expenseRepo;
    private final GroupBalanceService groupBalanceService;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public UpdateExpenseService(ExpenseRepo expenseRepo, GroupBalanceService groupBalanceService, GroupRepository groupRepository, UserRepository userRepository) {
        this.expenseRepo = expenseRepo;
        this.groupBalanceService = groupBalanceService;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public ExpenseResponseDto updateExpense(UpdateExpenseRequestDto expenseRequestDto) {
        var expense = expenseRepo.findById(expenseRequestDto.getId())
                .orElseThrow(() -> new ExpenseDoesNotExistsException("Invalid expense id."));
        List<GroupBalances> groupBalances = new ArrayList<>();
        if (!expenseRequestDto.isAmountUpdateRequire()) {
           updateExpenseMetaData(expense, expenseRequestDto);
           groupBalances = groupBalanceService.findBalanceForGroupId(expenseRequestDto.getGroupId());
        } else {

        }
        return ExpenseResponseDto.from(expense.getId(), groupBalances);
    }

    @Transactional
    private void updateExpenseMetaData(Expense expense, UpdateExpenseRequestDto expenseRequestDto) {
        if (Objects.nonNull(expenseRequestDto.getExpenseDate()) &&
                !expenseRequestDto.getExpenseDate().isEqual(expenseRequestDto.getExpenseDate())) {
            expense.setExpenseDate(expenseRequestDto.getExpenseDate());
        }

        if (Objects.nonNull(expenseRequestDto.getExpenseName()) &&
                !expenseRequestDto.getExpenseName().equals(expense.getExpense())) {
            expense.setExpense(expenseRequestDto.getExpenseName());
        }
    }
}
