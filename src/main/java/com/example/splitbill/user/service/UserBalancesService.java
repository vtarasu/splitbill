package com.example.splitbill.user.service;

import com.example.splitbill.expense.repo.GroupBalancesRepo;
import com.example.splitbill.expense.repo.NonGroupBalanceRepo;
import com.example.splitbill.group.domain.UserGroup;
import com.example.splitbill.user.dto.Direction;
import com.example.splitbill.user.dto.GetGroupAndBalances;
import com.example.splitbill.user.dto.OwesDto;
import com.example.splitbill.user.dto.TotalBalancesDto;
import com.example.splitbill.user.exception.UserDoesNotExistsException;
import com.example.splitbill.user.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.splitbill.user.dto.Direction.GET;
import static com.example.splitbill.user.dto.Direction.GIVE;
import static java.math.BigDecimal.ZERO;

@Slf4j
@Service
public class UserBalancesService {
    private final UserRepository userRepository;
    private final GroupBalancesRepo groupBalancesRepo;
    private final NonGroupBalanceRepo nonGroupBalanceRepo;

    public UserBalancesService(UserRepository userRepository,
                               GroupBalancesRepo groupBalancesRepo,
                               NonGroupBalanceRepo nonGroupBalanceRepo) {
        this.userRepository = userRepository;
        this.groupBalancesRepo = groupBalancesRepo;
        this.nonGroupBalanceRepo = nonGroupBalanceRepo;
    }

    public List<GetGroupAndBalances> getUserGroupsAndBalances(Long userId) {
        var user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserDoesNotExistsException("User doesn't exists"));
        log.info("Received request to fetch group and balances for user={}", userId);
        var userGroups = user.getUserGroups();
        var result = new ArrayList<GetGroupAndBalances>();
        for (UserGroup userGroup : userGroups) {
            var balances = findUserBalancesForGroup(user.getId(), userGroup.getGroup().getId());
            var userGroupAndBalance = GetGroupAndBalances.builder()
                    .balances(balances)
                    .groupId(userGroup.getGroup().getId())
                    .groupName(userGroup.getGroup().getGroupName())
                    .memberCount(userGroup.getGroup().getUsers().size())
                    .build();
            log.info("Fetched balances for user{} group={} balancesSize={}", userId,
                    userGroupAndBalance.getGroupName(), userGroupAndBalance.getBalances().size());
            result.add(userGroupAndBalance);
        }
        log.info("Fetched group and balances for user={} groupsCount={}", userId, result.size());
        return result;
    }

    private List<OwesDto> findUserBalancesForGroup(Long userId, Long groupId) {
        var result = new ArrayList<OwesDto>();
        result.addAll(findOwedBalances(userId, groupId));
        result.addAll(findOwesBalances(userId, groupId));
        return result;
    }

    private List<OwesDto> findOwedBalances(Long userId, Long groupId) {
        var balances = groupBalancesRepo.findByGroupIdAndToId(groupId, userId);
        var result = new ArrayList<OwesDto>();
        for (var balance : balances) {
            var owed = OwesDto.builder()
                    .from(balance.getFrom().getUsername())
                    .to(balance.getTo().getUsername())
                    .amount(balance.getBalance())
                    .direction(Direction.GET)
                    .build();
            result.add(owed);
        }
        return result;
    }

    private List<OwesDto> findOwesBalances(Long userId, Long groupId) {
        var balances = groupBalancesRepo.findByGroupIdAndFromId(groupId, userId);
        var result = new ArrayList<OwesDto>();
        for (var balance : balances) {
            var owes = OwesDto.builder()
                    .from(balance.getFrom().getUsername())
                    .to(balance.getTo().getUsername())
                    .amount(balance.getBalance())
                    .direction(Direction.GIVE)
                    .build();
            result.add(owes);
        }
        return result;
    }

    public List<TotalBalancesDto> getAllOpenBalancesForUser(Long userId) {
        var groupBalances = groupBalancesRepo.findByFromIdOrToId(userId, userId);
        var nonGroupBalances = nonGroupBalanceRepo.findByFromIdOrToId(userId, userId);
        var allBalances = new HashMap<Long, BigDecimal>();
        var userCache = new HashMap<Long, String>();
        Long id;

        for (var groupBalance : groupBalances) {
            if (groupBalance.getFrom().getId().equals(userId)) {
                id = groupBalance.getTo().getId();
                allBalances.put(id, allBalances.getOrDefault(id, ZERO).subtract(groupBalance.getBalance()));
                userCache.put(id, groupBalance.getTo().getUsername());
            } else {
                id = groupBalance.getFrom().getId();
                allBalances.put(id, allBalances.getOrDefault(id, ZERO).add(groupBalance.getBalance()));
                userCache.put(id, groupBalance.getFrom().getUsername());
            }
        }

        for (var nonGroupBalance : nonGroupBalances) {
            if (nonGroupBalance.getFrom().getId().equals(userId)) {
                id = nonGroupBalance.getTo().getId();
                allBalances.put(id, allBalances.getOrDefault(id, ZERO).subtract(nonGroupBalance.getBalance()));
                userCache.put(id, nonGroupBalance.getTo().getUsername());
            } else {
                id = nonGroupBalance.getFrom().getId();
                allBalances.put(id, allBalances.getOrDefault(id, ZERO).add(nonGroupBalance.getBalance()));
                userCache.put(id, nonGroupBalance.getFrom().getUsername());
            }
        }

        var results = new ArrayList<TotalBalancesDto>();
        for (Map.Entry<Long, BigDecimal> entry : allBalances.entrySet()) {
            var balance = TotalBalancesDto.builder()
                    .userId(entry.getKey())
                    .amount(entry.getValue().abs())
                    .userName(userCache.get(entry.getKey()))
                    .direction(entry.getValue().compareTo(ZERO) < 0 ? GIVE : GET)
                    .build();
            results.add(balance);
        }
        log.info("Fetched all open balances for user={} balancesCount={}", userId, results.size());
        return results;
    }
}
