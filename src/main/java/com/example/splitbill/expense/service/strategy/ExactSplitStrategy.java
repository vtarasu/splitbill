package com.example.splitbill.expense.service.strategy;

import com.example.splitbill.expense.domain.ExpenseSplit;
import com.example.splitbill.expense.dto.AddExpenseRequestDto;
import com.example.splitbill.expense.service.ExpenseSplitService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExactSplitStrategy implements ExpenseSplitService {

    @Override
    public List<ExpenseSplit> splitExpense(AddExpenseRequestDto addExpenseRequestDto) {
        var splits = new ArrayList<ExpenseSplit>();
        var userExpenses = addExpenseRequestDto.getUsersSharingExpense();
        for (Long user : userExpenses.keySet()) {
            var split = new ExpenseSplit();
            split.setAmount(userExpenses.get(user));
            split.setOwedBy(user);
            split.setPaidBy(addExpenseRequestDto.getPaidByUsers());
            splits.add(split);
        }
        return splits;
    }
}
