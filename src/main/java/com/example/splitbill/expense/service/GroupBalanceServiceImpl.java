package com.example.splitbill.expense.service;

import com.example.splitbill.expense.domain.ExpenseSplit;
import com.example.splitbill.expense.domain.GroupBalances;
import com.example.splitbill.expense.repo.GroupBalancesRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupBalanceServiceImpl implements GroupBalanceService {
    private final GroupBalancesRepo groupBalancesRepo;

    public GroupBalanceServiceImpl(GroupBalancesRepo groupBalancesRepo) {
        this.groupBalancesRepo = groupBalancesRepo;
    }

    @Override
    public List<GroupBalances> updateGroupBalance(Long groupId, List<ExpenseSplit> expenseSplit) {
        expenseSplit.stream().forEach(split -> {
            if (split.getOwedBy().equals(split.getPaidBy())) {
                return;
            }
            var newBalance = new GroupBalances();
            var existingBalance = groupBalancesRepo.findByUserId1AndUserId2(split.getOwedBy(), split.getPaidBy());
            if (existingBalance.isPresent()) {
                newBalance = existingBalance.get();
                newBalance.setBalance(split.getAmount() + existingBalance.get().getBalance());
                groupBalancesRepo.save(newBalance);
            } else {
                existingBalance = groupBalancesRepo.findByUserId1AndUserId2(split.getPaidBy(), split.getOwedBy());
                if (existingBalance.isPresent()) {
                    newBalance = existingBalance.get();
                    newBalance.setBalance(existingBalance.get().getBalance() - split.getAmount());
                } else {
                    newBalance.setGroupId(groupId);
                    newBalance.setUserId1(split.getOwedBy());
                    newBalance.setUserId2(split.getPaidBy());
                    newBalance.setBalance(split.getAmount());
                }
            }
            groupBalancesRepo.save(newBalance);

        });
        return groupBalancesRepo.findByGroupId(groupId);
    }
}
