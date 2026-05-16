package com.example.splitbill.expense.service.strategy;

import com.example.splitbill.expense.domain.ExpenseSplit;
import com.example.splitbill.expense.dto.AddExpenseRequestDto;
import com.example.splitbill.expense.service.ExpenseSplitService;
import com.example.splitbill.user.domain.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExactSplitStrategy implements ExpenseSplitService {

    @Override
    public List<ExpenseSplit> splitExpense(Map<Long, User> users, AddExpenseRequestDto addExpenseRequestDto) {
        var splits = new ArrayList<ExpenseSplit>();
        var userExpenses = addExpenseRequestDto.getUsersSharingExpense();
        var paidByUser = users.get(addExpenseRequestDto.getPaidByUsers());
        for (Long userId : userExpenses.keySet()) {
            var split = new ExpenseSplit();
            split.setAmount(userExpenses.get(userId));
            split.setOwedBy(users.get(userId));
            split.setPaidBy(paidByUser);
            splits.add(split);
        }
        return splits;
    }
}
