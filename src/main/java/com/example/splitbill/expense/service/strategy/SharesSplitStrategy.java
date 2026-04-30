package com.example.splitbill.expense.service.strategy;

import com.example.splitbill.expense.domain.ExpenseSplit;
import com.example.splitbill.expense.dto.AddExpenseRequestDto;
import com.example.splitbill.expense.service.ExpenseSplitService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SharesSplitStrategy implements ExpenseSplitService {

    @Override
    public List<ExpenseSplit> splitExpense(AddExpenseRequestDto addExpenseRequestDto) {
        List<ExpenseSplit> splits = new ArrayList<>();
        Map<Long, Double> userExpenses = addExpenseRequestDto.getUsersSharingExpense();
        var totalShares = userExpenses.values().stream().mapToDouble(Double::doubleValue).sum();
        var perShare = addExpenseRequestDto.getAmount() / totalShares;
        for (Long user : addExpenseRequestDto.getUsersSharingExpense().keySet()) {
            var split = new ExpenseSplit();
            split.setAmount(perShare * userExpenses.get(user));
            split.setOwedBy(user);
            split.setPaidBy(addExpenseRequestDto.getPaidByUsers());
            splits.add(split);
        }
        return splits;
    }
}
