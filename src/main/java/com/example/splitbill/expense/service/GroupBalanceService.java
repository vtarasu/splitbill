package com.example.splitbill.expense.service;

import com.example.splitbill.expense.domain.ExpenseSplit;
import com.example.splitbill.expense.domain.GroupBalances;
import com.example.splitbill.group.domain.Group;
import com.example.splitbill.user.domain.User;

import java.util.List;
import java.util.Map;

public interface GroupBalanceService {
    List<GroupBalances> updateGroupBalance(Group group, Map<Long, User> users, List<ExpenseSplit> splits);
}
