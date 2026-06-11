package com.example.splitbill.expense.service;

import com.example.splitbill.expense.domain.Expense;
import com.example.splitbill.expense.dto.*;
import com.example.splitbill.expense.exception.ExpenseDoesNotExistsException;
import com.example.splitbill.expense.exception.MaxLimitReachedException;
import com.example.splitbill.expense.repo.ExpenseRepo;
import com.example.splitbill.expense.service.strategy.ExpenseSplitStrategy;
import com.example.splitbill.group.exception.GroupDoesNotExistsException;
import com.example.splitbill.group.repo.GroupRepository;
import com.example.splitbill.user.domain.User;
import com.example.splitbill.user.exception.UserDoesNotExistsException;
import com.example.splitbill.user.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.splitbill.user.domain.UserType.PREMIUM;

@Slf4j
@Service
public class ExpenseService {
    private final Long PER_DAY_LIMIT = 5L;
    private final ExpenseRepo expenseRepo;
    private final GroupBalanceService groupBalanceService;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final NonGroupBalanceService nonGroupBalanceService;

    public ExpenseService(ExpenseRepo expenseRepo, GroupBalanceService groupBalanceService, GroupRepository groupRepository, UserRepository userRepository, NonGroupBalanceService nonGroupBalanceService) {
        this.expenseRepo = expenseRepo;
        this.groupBalanceService = groupBalanceService;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.nonGroupBalanceService = nonGroupBalanceService;
    }

    @Transactional
    public ExpenseResponseDto addExpense(AddExpenseRequestDto addExpenseRequestDto) {
        var addedBy = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var addedByUser = userRepository.findUserById(addedBy)
                .orElseThrow(() -> new UserDoesNotExistsException("Invalid user id received"));

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        Long count = expenseRepo.countByAddedByUser_IdAndDateAddedAtBetween(addedBy, startOfDay, endOfDay);

        if (count >= PER_DAY_LIMIT && !PREMIUM.equals(addedByUser.getUserType()) &&
                !ExpenseType.SETTLEMENT.equals(addExpenseRequestDto.getExpenseType())) {
            throw new MaxLimitReachedException("Daily Limit Reached. Get Subscription to add more expenses");
        }

        var group = Objects.isNull(addExpenseRequestDto.getGroupId()) ? null :
                groupRepository.findGroupById(addExpenseRequestDto.getGroupId())
                .orElseThrow(() -> new GroupDoesNotExistsException("Invalid group id received"));

        var paidByUser = userRepository.findUserById(addExpenseRequestDto.getPaidBy())
                .orElseThrow(() -> new UserDoesNotExistsException("Invalid user id received"));

        Set<Long> userIds = addExpenseRequestDto.getSplitDetails().stream()
                .map(SplitDetails::getUserId)
                .collect(Collectors.toSet());

        var users = userRepository.findAllById(userIds);
        Map<Long, User> usersById = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        usersById.put(paidByUser.getId(), paidByUser);
        usersById.put(addedByUser.getId(), addedByUser);
        var splits = ExpenseSplitStrategy.getExpenseSplitStrategy(addExpenseRequestDto.getSplitStrategy())
                .splitExpense(usersById, addExpenseRequestDto);

        log.info("Expense splits computed successfully");
        var expenseType = addExpenseRequestDto.getExpenseType() != null ?
                addExpenseRequestDto.getExpenseType() : ExpenseType.EXPENSE;
        Expense expense = Expense.builder()
                .expense(addExpenseRequestDto.getExpenseName())
                .addedByUser(addedByUser)
                .paidByUser(paidByUser)
                .group(group)
                .expenseDate(addExpenseRequestDto.getExpenseDate())
                .billAmount(addExpenseRequestDto.getAmount())
                .splitStrategy(addExpenseRequestDto.getSplitStrategy())
                .expenseType(expenseType)
                .split(splits)
                .build();

        splits.forEach(expenseSplit -> expenseSplit.setExpense(expense));
        var savedExpense = expenseRepo.save(expense);
        ExpenseResponseDto expenseResponseDto;
        if (Objects.isNull(group)) {
            var balances = nonGroupBalanceService.updateBalance(addedByUser.getId(), splits);
            expenseResponseDto = ExpenseResponseDto.from(expense.getId(), balances);
        } else {
            var balances = groupBalanceService.updateGroupBalance(group, splits);
            expenseResponseDto = ExpenseResponseDto.from(expense.getId(), group, balances);
        }
        log.info("Expense saved successfully and group balances updated. savedExpense={}", savedExpense.getId());
        return expenseResponseDto;
    }

    @Transactional
    public PaginationResponse<GetAllExpensesResponseDto> getExpensesInGroup(Long groupId, Integer pageNo, Integer pageSize) {
        var pageable = PageRequest.of(pageNo, pageSize, Sort.by("expenseDate", "dateAddedAt").descending());
        var expenses = expenseRepo.findAllByGroupId(groupId, pageable);
        var expensesList = expenses.stream().map(GetAllExpensesResponseDto::from).toList();
        return PaginationResponse.<GetAllExpensesResponseDto>builder()
                .totalElements(expenses.getNumberOfElements())
                .totalPages(expenses.getTotalPages())
                .results(expensesList)
                .build();
    }

    public Boolean deleteExpense(long id) {
        var expense = expenseRepo.findById(id).orElseThrow(() -> new ExpenseDoesNotExistsException("Invalid expense"));
        var splits = expense.getSplit();
        expenseRepo.delete(expense);
        if (Objects.isNull(expense.getGroup())) {
            var userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            nonGroupBalanceService.reverseBalance((Long) userId, splits);
        } else {
            groupBalanceService.reverseBalances(expense.getGroup(), splits);
        }
        log.info("Expense deleted successfully and balances updated. expense={}", expense.getId());
        return true;
    }

    @Transactional
    public ExpenseResponseDto updateExpense(AddExpenseRequestDto expenseRequestDto) {
        var expense = expenseRepo.findById(expenseRequestDto.getId())
                .orElseThrow(() -> new ExpenseDoesNotExistsException("Invalid expense id."));
        deleteExpense(expense.getId());
        log.info("Existing expense deleted successfully. id={}", expense.getId());
        return addExpense(expenseRequestDto);
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
        var groupName = Objects.isNull(expense.getGroup()) ? "" : expense.getGroup().getGroupName();
        return ExpenseDetailsDto.builder()
                .id(id)
                .expenseName(expense.getExpense())
                .paidBy(expense.getPaidByUser().getUsername())
                .addedBy(expense.getAddedByUser().getUsername())
                .expenseDate(expense.getExpenseDate())
                .billAmount(expense.getBillAmount())
                .groupName(groupName)
                .splitStrategy(expense.getSplitStrategy())
                .expenseSplit(splitDetails)
                .build();
    }

    public PaginationResponse<GetAllExpensesResponseDto> getNonGroupExpenses(Long userId,
                                                                             Integer pageNo,
                                                                             Integer pageSize) {
        var pageable = PageRequest.of(pageNo, pageSize, Sort.by("expenseDate", "dateAddedAt").descending());
        var expenses = expenseRepo.findAllNonGroupExpensesForUser(userId, pageable);
        var expensesList = expenses.stream().map(GetAllExpensesResponseDto::from).toList();
        return PaginationResponse.<GetAllExpensesResponseDto>builder()
                .totalElements(expenses.getNumberOfElements())
                .totalPages(expenses.getTotalPages())
                .results(expensesList)
                .build();
    }
}
