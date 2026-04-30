package com.example.splitbill.expense.service;

import com.example.splitbill.expense.domain.ExpenseSplit;
import com.example.splitbill.expense.domain.GroupBalances;

import java.util.List;

public interface GroupBalanceService {
    List<GroupBalances> updateGroupBalance(Long groupId, List<ExpenseSplit> expenseSplit);
}
