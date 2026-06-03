package com.example.splitbill.user.service;

import com.example.splitbill.expense.domain.GroupBalances;
import com.example.splitbill.expense.dto.AddExpenseRequestDto;
import com.example.splitbill.expense.dto.SplitDetails;
import com.example.splitbill.expense.dto.SplitStrategy;
import com.example.splitbill.expense.repo.GroupBalancesRepo;
import com.example.splitbill.expense.service.ExpenseService;
import com.example.splitbill.user.domain.Settlements;
import com.example.splitbill.user.dto.SettleBalanceRequestDto;
import com.example.splitbill.user.dto.SettleGroupBalanceRequestDto;
import com.example.splitbill.user.dto.TotalBalancesDto;
import com.example.splitbill.user.exception.UserDoesNotExistsException;
import com.example.splitbill.user.repo.SettlementsRepository;
import com.example.splitbill.user.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
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
    private final SettlementsRepository settlementsRepository;
    private final ExpenseService expenseService;

    public SettlementsService(UserBalancesService userBalancesService, UserRepository userRepository, GroupBalancesRepo groupBalancesRepo, SettlementsRepository settlementsRepository, ExpenseService expenseService) {
        this.userBalancesService = userBalancesService;
        this.userRepository = userRepository;
        this.groupBalancesRepo = groupBalancesRepo;
        this.settlementsRepository = settlementsRepository;
        this.expenseService = expenseService;
    }

    @Transactional
    public List<TotalBalancesDto> recordPaymentForUser(SettleBalanceRequestDto requestDto) {
        var userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var fromUser = userRepository.findUserById(requestDto.getFromUserId())
                .orElseThrow(() -> new UserDoesNotExistsException("Invalid user id"));

        var toUser = userRepository.findUserById(requestDto.getToUserId())
                .orElseThrow(() -> new UserDoesNotExistsException("Invalid user id"));

        var groupBalances = groupBalancesRepo.findByFromIdAndToId(requestDto.getFromUserId(), requestDto.getToUserId());
        groupBalances.addAll(groupBalancesRepo.findByFromIdAndToId(requestDto.getToUserId(), requestDto.getFromUserId()));
        var expenses = convertGroupBalanceToExpense((Long) userId, groupBalances);
        var settlement = Settlements.builder()
                .from(fromUser)
                .to(toUser)
                .amount(requestDto.getAmount())
                .build();
        settlementsRepository.save(settlement);
        for (var expense : expenses) {
            expenseService.addExpense(expense);
        }
        return userBalancesService.getAllOpenBalancesForUser((Long) userId);
    }

    private List<AddExpenseRequestDto> convertGroupBalanceToExpense(Long userId,
                                                                    List<GroupBalances> groupBalances) {
        var result = new ArrayList<AddExpenseRequestDto>();
        for (GroupBalances balances : groupBalances) {
            var split = new ArrayList<SplitDetails>();
            split.add(new SplitDetails(balances.getTo().getId(), balances.getBalance(), null));
            var expense = AddExpenseRequestDto.builder()
                    .expenseName("Settlement")
                    .groupId(balances.getGroup().getId())
                    .expenseDate(LocalDate.now())
                    .paidBy(balances.getFrom().getId())
                    .addedByUser(userId)
                    .splitStrategy(SplitStrategy.EXACT)
                    .amount(balances.getBalance())
                    .splitDetails(split)
                    .build();
            result.add(expense);
        }
        return result;
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

        var expenses = convertGroupBalanceToExpense((Long) userId, List.of(groupBalances));
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
}
