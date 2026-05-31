package com.example.splitbill.expense.service;

import com.example.splitbill.expense.domain.Expense;
import com.example.splitbill.expense.domain.GroupBalances;
import com.example.splitbill.expense.dto.*;
import com.example.splitbill.expense.exception.ExpenseDoesNotExistsException;
import com.example.splitbill.expense.repo.ExpenseRepo;
import com.example.splitbill.expense.service.strategy.ExpenseSplitStrategy;
import com.example.splitbill.group.exception.GroupDoesNotExistsException;
import com.example.splitbill.group.repo.GroupRepository;
import com.example.splitbill.user.exception.UserDoesNotExistsException;
import com.example.splitbill.user.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExpenseService {
    private final ExpenseRepo expenseRepo;
    private final GroupBalanceService groupBalanceService;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepo expenseRepo, GroupBalanceService groupBalanceService, GroupRepository groupRepository, UserRepository userRepository) {
        this.expenseRepo = expenseRepo;
        this.groupBalanceService = groupBalanceService;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ExpenseResponseDto addExpense(AddExpenseRequestDto addExpenseRequestDto) {
        var group = groupRepository.findGroupById(addExpenseRequestDto.getGroupId())
                .orElseThrow(() -> new GroupDoesNotExistsException("Invalid group id received"));

        var addedByUser = userRepository.findUserById(addExpenseRequestDto.getAddedByUser())
                .orElseThrow(() -> new UserDoesNotExistsException("Invalid user id received"));

        var paidByUser = userRepository.findUserById(addExpenseRequestDto.getPaidByUsers())
                .orElseThrow(() -> new UserDoesNotExistsException("Invalid user id received"));

        var users = addExpenseRequestDto.getUsersSharingExpense().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> userRepository.findUserById(entry.getKey())
                                .orElseThrow(() -> new UserDoesNotExistsException("Invalid user id received"))
                ));

        users.put(paidByUser.getId(), paidByUser);
        users.put(addedByUser.getId(), addedByUser);

        var splits = ExpenseSplitStrategy.getExpenseSplitStrategy(addExpenseRequestDto.getSplitStrategy())
                .splitExpense(users, addExpenseRequestDto);

        log.info("Expense splits computed successfully for expenseId={}.", addExpenseRequestDto.getGroupId());
        var splitDetails = addExpenseRequestDto.getUsersSharingExpense().toString();
        if (addExpenseRequestDto.getSplitStrategy().equals(SplitStrategy.EQUAL)) {
            splitDetails = addExpenseRequestDto.getUsersSharingExpense().keySet().toString();
        }

        Expense expense = Expense.builder()
                .expense(addExpenseRequestDto.getExpenseName())
                .addedByUser(addedByUser)
                .paidByUser(paidByUser)
                .group(group)
                .expenseDate(addExpenseRequestDto.getExpenseDate())
                .billAmount(addExpenseRequestDto.getAmount())
                .splitStrategy(addExpenseRequestDto.getSplitStrategy())
                .split(splits)
                .splitDetails(splitDetails)
                .build();

        splits.forEach(expenseSplit -> expenseSplit.setExpense(expense));
        var savedExpense = expenseRepo.save(expense);
        var groupBalances = groupBalanceService.updateGroupBalance(group, splits);
        log.info("Expense saved successfully and group balances updated. savedExpense={}", savedExpense.getId());
        return ExpenseResponseDto.from(expense.getId(), groupBalances);
    }

    @Transactional
    public List<ExpensesInGroupResponseDto> getExpensesInGroup(ExpensesInGroupRequestDto getExpensesRequestDto) {
        var pageable = PageRequest.of(getExpensesRequestDto.getPageNo(), getExpensesRequestDto.getPageSize(),
                Sort.by("expenseDate").descending());
        var expenses = expenseRepo.findAllByGroupId(getExpensesRequestDto.getGroupId(), pageable);
        return expenses.stream().map(ExpensesInGroupResponseDto::from).toList();
    }

    public ExpenseResponseDto deleteExpense(long id) {
        var expense = expenseRepo.findById(id).orElseThrow(() -> new ExpenseDoesNotExistsException("Invalid expense"));
        var splits = expense.getSplit();
        expenseRepo.delete(expense);
        var groupBalances = groupBalanceService.reverseBalances(expense.getGroup(), splits);
        log.info("Expense deleted successfully and group balances updated. expense={}", expense.getId());
        return ExpenseResponseDto.from(id, groupBalances);
    }

    @Transactional
    public ExpenseResponseDto updateExpense(UpdateExpenseRequestDto expenseRequestDto) {
        var expense = expenseRepo.findById(expenseRequestDto.getId())
                .orElseThrow(() -> new ExpenseDoesNotExistsException("Invalid expense id."));
        List<GroupBalances> groupBalances = new ArrayList<>();
        if (!expenseRequestDto.isAmountUpdateRequire()) {
            updateExpenseMetaData(expense, expenseRequestDto);
            groupBalances = groupBalanceService.findBalanceForGroupId(expense.getGroup().getId());
            return ExpenseResponseDto.from(expense.getId(), groupBalances);
        } else {
            var addExpenseDto = AddExpenseRequestDto.from(expense, expenseRequestDto);
            deleteExpense(expense.getId());
            log.info("Existing expense deleted successfully. id={}", expense.getId());
            return addExpense(addExpenseDto);
        }
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
        expenseRepo.save(expense);
    }

    public ExpenseDetailsDto getExpenseById(Long id) {
        var expense = expenseRepo.findById(id)
                .orElseThrow(() -> new ExpenseDoesNotExistsException("Invalid Expense id."));

        var splitDetails = expense.getSplit().stream()
                .map(expenseSplit -> ExpenseSplitDto.builder()
                        .fromId(expenseSplit.getOwedBy().getId())
                        .fromUserName(expenseSplit.getOwedBy().getUsername())
                        .toId(expenseSplit.getPaidBy().getId())
                        .toUserName(expenseSplit.getPaidBy().getUsername())
                        .amount(expenseSplit.getAmount())
                        .build())
                .toList();

        return ExpenseDetailsDto.builder()
                .id(id)
                .expenseName(expense.getExpense())
                .paidBy(expense.getPaidByUser().getUsername())
                .addedBy(expense.getAddedByUser().getUsername())
                .expenseDate(expense.getExpenseDate())
                .billAmount(expense.getBillAmount())
                .groupName(expense.getGroup().getGroupName())
                .splitStrategy(expense.getSplitStrategy())
                .expenseSplit(splitDetails)
                .build();
    }
}
