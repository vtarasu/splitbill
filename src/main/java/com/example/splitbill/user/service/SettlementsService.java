package com.example.splitbill.user.service;

import com.example.splitbill.expense.domain.GroupBalances;
import com.example.splitbill.expense.domain.NonGroupBalance;
import com.example.splitbill.expense.dto.AddExpenseRequestDto;
import com.example.splitbill.expense.dto.ExpenseType;
import com.example.splitbill.expense.dto.SplitDetails;
import com.example.splitbill.expense.dto.SplitStrategy;
import com.example.splitbill.expense.repo.GroupBalancesRepo;
import com.example.splitbill.expense.repo.NonGroupBalanceRepo;
import com.example.splitbill.expense.service.ExpenseService;
import com.example.splitbill.user.domain.Settlements;
import com.example.splitbill.user.dto.*;
import com.example.splitbill.user.exception.UserDoesNotExistsException;
import com.example.splitbill.user.repo.SettlementsRepository;
import com.example.splitbill.user.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SettlementsService {
    private final UserBalancesService userBalancesService;
    private final UserRepository userRepository;
    private final GroupBalancesRepo groupBalancesRepo;
    private final NonGroupBalanceRepo nonGroupBalanceRepo;
    private final SettlementsRepository settlementsRepository;
    private final ExpenseService expenseService;

    public SettlementsService(UserBalancesService userBalancesService, UserRepository userRepository, GroupBalancesRepo groupBalancesRepo, NonGroupBalanceRepo nonGroupBalanceRepo, SettlementsRepository settlementsRepository, ExpenseService expenseService) {
        this.userBalancesService = userBalancesService;
        this.userRepository = userRepository;
        this.groupBalancesRepo = groupBalancesRepo;
        this.nonGroupBalanceRepo = nonGroupBalanceRepo;
        this.settlementsRepository = settlementsRepository;
        this.expenseService = expenseService;
    }

    @Transactional
    public List<TotalBalancesDto> recordPaymentForUser(SettleBalanceRequestDto requestDto) {
        var userId = (Long)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var fromUser = userRepository.findUserById(requestDto.getFromUserId())
                .orElseThrow(() -> new UserDoesNotExistsException("Invalid user id"));

        var toUser = userRepository.findUserById(requestDto.getToUserId())
                .orElseThrow(() -> new UserDoesNotExistsException("Invalid user id"));

        var settlement = Settlements.builder()
                .from(fromUser)
                .to(toUser)
                .amount(requestDto.getAmount())
                .build();
        settlementsRepository.save(settlement);
        settleGroupBalances(requestDto, userId);
        settleNonGroupBalances(requestDto, userId);
        return userBalancesService.getAllOpenBalancesForUser(userId);
    }

    private void settleGroupBalances(SettleBalanceRequestDto requestDto, Long userId) {
        var groupBalances = groupBalancesRepo.findByFromIdAndToId(requestDto.getFromUserId(), requestDto.getToUserId());
        groupBalances.addAll(groupBalancesRepo.findByFromIdAndToId(requestDto.getToUserId(), requestDto.getFromUserId()));
        var expenses = convertBalanceToExpense(userId, groupBalances);
        for (var expense : expenses) {
            expenseService.addExpense(expense);
        }
    }

    private void settleNonGroupBalances(SettleBalanceRequestDto requestDto, Long userId) {
        var balance = nonGroupBalanceRepo.findByFromIdAndToId(requestDto.getFromUserId(), requestDto.getToUserId());
        if (balance.isEmpty()) {
            balance = nonGroupBalanceRepo.findByFromIdAndToId(requestDto.getToUserId(), requestDto.getFromUserId());
        }
        if (balance.isEmpty()) {
            return;
        }
        var expense = convertBalanceToExpense(userId, balance.get());
        expenseService.addExpense(expense);
    }

    private List<AddExpenseRequestDto> convertBalanceToExpense(Long userId,
                                                               List<GroupBalances> groupBalances) {
        var result = new ArrayList<AddExpenseRequestDto>();
        for (GroupBalances balances : groupBalances) {
            var split = new ArrayList<SplitDetails>();
            split.add(new SplitDetails(balances.getTo().getId(), balances.getBalance()));
            var expense = AddExpenseRequestDto.builder()
                    .expenseName("Settlement")
                    .groupId(balances.getGroup().getId())
                    .expenseDate(LocalDate.now())
                    .paidBy(balances.getFrom().getId())
                    .addedByUser(userId)
                    .splitStrategy(SplitStrategy.EXACT)
                    .amount(balances.getBalance())
                    .splitDetails(split)
                    .expenseType(ExpenseType.SETTLEMENT)
                    .build();
            result.add(expense);
        }
        return result;
    }

    private AddExpenseRequestDto convertBalanceToExpense(Long userId, NonGroupBalance balance) {

            var split = new ArrayList<SplitDetails>();
            split.add(new SplitDetails(balance.getTo().getId(), balance.getBalance()));
        return AddExpenseRequestDto.builder()
                .expenseName("Settlement")
                .expenseDate(LocalDate.now())
                .paidBy(balance.getFrom().getId())
                .addedByUser(userId)
                .splitStrategy(SplitStrategy.EXACT)
                .expenseType(ExpenseType.SETTLEMENT)
                .amount(balance.getBalance())
                .splitDetails(split)
                .build();
    }

    public boolean recordPaymentForGroup(SettleGroupBalanceRequestDto requestDto) {
        var userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var fromUser = userRepository.findUserByUsername(requestDto.getFrom())
                .orElseThrow(() -> new UserDoesNotExistsException("Invalid user id"));

        var toUser = userRepository.findUserByUsername(requestDto.getTo())
                .orElseThrow(() -> new UserDoesNotExistsException("Invalid user id"));

        var groupBalances = groupBalancesRepo.findByGroupIdAndFromIdAndToId(requestDto.getGroupId(),
                        fromUser.getId(), toUser.getId())
                .orElseThrow(() -> new RuntimeException("Invalid request"));

        var expenses = convertBalanceToExpense((Long) userId, List.of(groupBalances));
        var settlement = Settlements.builder()
                .from(fromUser)
                .to(toUser)
                .amount(requestDto.getAmount())
                .build();
        settlementsRepository.save(settlement);
        for (var expense : expenses) {
            expenseService.addExpense(expense);
        }
        return true;
    }

    public SettlementHistoryResponseDto getSettlements(Long userId, Integer pageNumber, Integer pageSize) {
        var pageable = PageRequest.of(pageNumber, pageSize, Sort.by("settledAt").descending());
        var settlements = settlementsRepository.findByFromIdOrToId(userId, userId, pageable);
        var totalAmount = settlementsRepository.getTotalSettledAmount(userId);
        var settlementDtos = settlements.stream()
                .map(settlement -> SettlementDto.builder()
                        .id(settlement.getId())
                        .from(settlement.getFrom().getUsername())
                        .to(settlement.getTo().getUsername())
                        .amount(settlement.getAmount())
                        .settledAt(settlement.getSettledAt())
                        .build())
                .toList();
        return SettlementHistoryResponseDto.builder()
                .totalPages(settlements.getTotalPages())
                .totalSettlements(settlements.getNumberOfElements())
                .totalAmount(totalAmount)
                .currentPage(pageNumber)
                .settlements(settlementDtos)
                .build();
    }
}
