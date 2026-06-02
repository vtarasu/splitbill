package com.example.splitbill.user.service;

import com.example.splitbill.expense.repo.GroupBalancesRepo;
import com.example.splitbill.user.domain.Settlements;
import com.example.splitbill.user.dto.SettleBalanceRequestDto;
import com.example.splitbill.user.dto.TotalBalancesDto;
import com.example.splitbill.user.exception.UserDoesNotExistsException;
import com.example.splitbill.user.repo.SettlementsRepository;
import com.example.splitbill.user.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SettlementsService {
    private final UserBalancesService userBalancesService;
    private final UserRepository userRepository;
    private final GroupBalancesRepo groupBalancesRepo;
    private final SettlementsRepository settlementsRepository;

    public SettlementsService(UserBalancesService userBalancesService, UserRepository userRepository, GroupBalancesRepo groupBalancesRepo, SettlementsRepository settlementsRepository) {
        this.userBalancesService = userBalancesService;
        this.userRepository = userRepository;
        this.groupBalancesRepo = groupBalancesRepo;
        this.settlementsRepository = settlementsRepository;
    }

    @Transactional
    public List<TotalBalancesDto> recordPaymentForUser(SettleBalanceRequestDto requestDto) {
        var fromUser = userRepository.findUserById(requestDto.getFromUserId())
                .orElseThrow(() -> new UserDoesNotExistsException("Invalid user id"));

        var toUser = userRepository.findUserById(requestDto.getToUserId())
                .orElseThrow(() -> new UserDoesNotExistsException("Invalid user id"));

        var groupBalances = groupBalancesRepo.findByFromIdAndToId(requestDto.getFromUserId(), requestDto.getToUserId());
        groupBalances.addAll(groupBalancesRepo.findByFromIdAndToId(requestDto.getToUserId(), requestDto.getFromUserId()));

        var settlement = Settlements.builder()
                .from(fromUser)
                .to(toUser)
                .amount(requestDto.getAmount())
                .build();
        settlementsRepository.save(settlement);
        groupBalancesRepo.deleteAll(groupBalances);
        return userBalancesService.getAllOpenBalancesForUser(fromUser.getId());
    }
}
