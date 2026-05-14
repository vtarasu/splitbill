package com.example.splitbill.expense.service;

import com.example.splitbill.expense.domain.ExpenseSplit;
import com.example.splitbill.expense.domain.GroupBalances;
import com.example.splitbill.expense.repo.GroupBalancesRepo;
import com.example.splitbill.group.domain.Group;
import com.example.splitbill.user.domain.User;
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
            var existingBalance = groupBalancesRepo.findByUserId1AndUserId2(split.getOwedBy(), split.getPaidBy());
            if (existingBalance.isPresent()) {
                newBalance = existingBalance.get();
                newBalance.setBalance(split.getAmount().add(existingBalance.get().getBalance()));
                groupBalancesRepo.save(newBalance);
            } else {
                existingBalance = groupBalancesRepo.findByUserId1AndUserId2(split.getPaidBy(), split.getOwedBy());
                if (existingBalance.isPresent()) {
                    newBalance = existingBalance.get();
                    newBalance.setBalance(existingBalance.get().getBalance().subtract(split.getAmount()));
                } else {
                    newBalance.setGroupId(group);
                    newBalance.setUserId1(users.get(split.getOwedBy()));
                    newBalance.setUserId2(users.get(split.getPaidBy()));
                    newBalance.setBalance(split.getAmount());
                }
            }
            groupBalancesRepo.save(newBalance);

        });
        return groupBalancesRepo.findByGroupId(group.getId());
    }
}
