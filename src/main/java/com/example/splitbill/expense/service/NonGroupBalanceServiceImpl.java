package com.example.splitbill.expense.service;

import com.example.splitbill.expense.domain.ExpenseSplit;
import com.example.splitbill.expense.domain.NonGroupBalance;
import com.example.splitbill.expense.repo.NonGroupBalanceRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class NonGroupBalanceServiceImpl implements NonGroupBalanceService {
    private final NonGroupBalanceRepo balancesRepo;

    public NonGroupBalanceServiceImpl(NonGroupBalanceRepo nonGroupBalanceRepo) {
        this.balancesRepo = nonGroupBalanceRepo;
    }

    @Override
    public List<NonGroupBalance> updateBalance(Long userId, List<ExpenseSplit> expenseSplit) {
        expenseSplit.forEach(split -> {
            if (split.getOwedBy().equals(split.getPaidBy())) {
                return;
            }
            var newBalance = new NonGroupBalance();
            var owedBy = split.getOwedBy().getId();
            var paidBy = split.getPaidBy().getId();
            var existingBalance = balancesRepo.findByFromIdAndToId(owedBy, paidBy);
            if (existingBalance.isPresent()) {
                newBalance = existingBalance.get();
                newBalance.setBalance(split.getAmount().add(existingBalance.get().getBalance()));
                balancesRepo.save(newBalance);
            } else {
                existingBalance = balancesRepo.findByFromIdAndToId(paidBy, owedBy);
                if (existingBalance.isPresent()) {
                    var netBalance = existingBalance.get().getBalance().subtract(split.getAmount());
                    if (netBalance.compareTo(BigDecimal.ZERO) == 0) {
                        balancesRepo.deleteById(existingBalance.get().getId());
                    } else if (netBalance.compareTo(BigDecimal.ZERO) < 0) {
                        newBalance = new NonGroupBalance();
                        newBalance.setFrom(split.getOwedBy());
                        newBalance.setTo(split.getPaidBy());
                        newBalance.setBalance(netBalance.abs());
                        balancesRepo.deleteById(existingBalance.get().getId());
                        balancesRepo.save(newBalance);
                    } else {
                        newBalance = existingBalance.get();
                        newBalance.setBalance(netBalance);
                        balancesRepo.save(newBalance);
                    }
                } else {
                    newBalance.setFrom(split.getOwedBy());
                    newBalance.setTo(split.getPaidBy());
                    newBalance.setBalance(split.getAmount());
                    balancesRepo.save(newBalance);
                }
            }
        });
        return balancesRepo.findByFromIdOrToId(userId, userId);
    }

    @Transactional
    @Override
    public List<NonGroupBalance> reverseBalance(Long userId, List<ExpenseSplit> expenseSplit) {
        for (ExpenseSplit split : expenseSplit) {
            if (split.getOwedBy().equals(split.getPaidBy())) {
                continue;
            }
            var existingBalance = balancesRepo.findByFromIdAndToId(split.getOwedBy().getId(), split.getPaidBy().getId());
            if (existingBalance.isPresent()) {
                var netBalance = existingBalance.get().getBalance().subtract(split.getAmount());
                if (netBalance.compareTo(BigDecimal.ZERO) < 0) {
                    var balance = new NonGroupBalance();
                    balance.setFrom(split.getPaidBy());
                    balance.setTo(split.getOwedBy());
                    balance.setBalance(netBalance.abs());
                    balancesRepo.deleteById(existingBalance.get().getId());
                    balancesRepo.save(balance);
                } else if(netBalance.compareTo(BigDecimal.ZERO) == 0) {
                    balancesRepo.deleteById(existingBalance.get().getId());
                } else {
                    var newBalance = existingBalance.get();
                    newBalance.setBalance(netBalance);
                    balancesRepo.save(newBalance);
                }
            } else {
                existingBalance = balancesRepo.findByFromIdAndToId(split.getPaidBy().getId(), split.getOwedBy().getId());
                if (existingBalance.isPresent()) {
                    var newGroupBalance = existingBalance.get();
                    newGroupBalance.setBalance(newGroupBalance.getBalance().add(split.getAmount()));
                    balancesRepo.save(newGroupBalance);
                } else {
                    var newBalance = new NonGroupBalance();
                    newBalance.setFrom(split.getPaidBy());
                    newBalance.setTo(split.getOwedBy());
                    newBalance.setBalance(split.getAmount());
                    balancesRepo.save(newBalance);
                }
            }
        }
        return balancesRepo.findByFromIdOrToId(userId, userId);
    }
}
