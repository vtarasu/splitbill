package com.example.splitbill.expense.service;

import com.example.splitbill.expense.domain.ExpenseSplit;
import com.example.splitbill.expense.domain.GroupBalances;
import com.example.splitbill.expense.repo.GroupBalancesRepo;
import com.example.splitbill.group.domain.Group;
import com.example.splitbill.user.domain.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GroupBalanceServiceImpl implements GroupBalanceService {
    private final GroupBalancesRepo groupBalancesRepo;

    public GroupBalanceServiceImpl(GroupBalancesRepo groupBalancesRepo) {
        this.groupBalancesRepo = groupBalancesRepo;
    }

    @Override
    public List<GroupBalances> updateGroupBalance(Group group, Map<Long, User> users, List<ExpenseSplit> expenseSplit) {
        expenseSplit.forEach(split -> {
            if (split.getOwedBy().equals(split.getPaidBy())) {
                return;
            }
            var newBalance = new GroupBalances();
            var owedBy = split.getOwedBy();
            var paidBy = split.getPaidBy();
            var existingBalance = groupBalancesRepo.findByGroupIdAndFromIdAndToId(group.getId(), owedBy, paidBy);
            if (existingBalance.isPresent()) {
                newBalance = existingBalance.get();
                newBalance.setBalance(split.getAmount().add(existingBalance.get().getBalance()));
                groupBalancesRepo.save(newBalance);
            } else {
                existingBalance = groupBalancesRepo.findByGroupIdAndFromIdAndToId(group.getId(), paidBy, owedBy);
                if (existingBalance.isPresent()) {
                    newBalance = existingBalance.get();
                    newBalance.setBalance(existingBalance.get().getBalance().subtract(split.getAmount()));
                } else {
                    newBalance.setGroup(group);
                    newBalance.setFrom(users.get(split.getOwedBy()));
                    newBalance.setTo(users.get(split.getPaidBy()));
                    newBalance.setBalance(split.getAmount());
                }
            }
            groupBalancesRepo.save(newBalance);

        });
        return groupBalancesRepo.findByGroupId(group.getId());
    }

    @Transactional
    @Override
    public List<GroupBalances> reverseBalances(Group group, List<ExpenseSplit> expenseSplit) {
        for (ExpenseSplit split : expenseSplit) {
            if (split.getOwedBy().equals(split.getPaidBy())) {
                continue;
            }
            var existingBalance = groupBalancesRepo.findByGroupIdAndFromIdAndToId(group.getId(),
                    split.getOwedBy(), split.getPaidBy());
            if (existingBalance.isPresent()) {
                var balance = existingBalance.get();
                balance.setBalance(balance.getBalance().subtract(split.getAmount()));
            } else {
                existingBalance = groupBalancesRepo.findByGroupIdAndFromIdAndToId(group.getId(),
                        split.getPaidBy(), split.getOwedBy());
                var balance = existingBalance.get();
                balance.setBalance(balance.getBalance().add(split.getAmount()));
            }
        };
        return groupBalancesRepo.findByGroupId(group.getId());
    }

    @Override
    public List<GroupBalances> findBalanceForGroupId(Long groupId) {
        return groupBalancesRepo.findByGroupId(groupId);
    }
}
