package com.example.splitbill.expense.service;

import com.example.splitbill.expense.domain.ExpenseSplit;
import com.example.splitbill.expense.domain.NonGroupBalance;

import java.util.List;

public interface NonGroupBalanceService {
    List<NonGroupBalance> updateBalance(Long userId, List<ExpenseSplit> splits);

    List<NonGroupBalance> reverseBalance(Long userId, List<ExpenseSplit> splits);
}
