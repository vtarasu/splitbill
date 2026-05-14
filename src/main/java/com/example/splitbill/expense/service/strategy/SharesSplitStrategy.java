package com.example.splitbill.expense.service.strategy;

import com.example.splitbill.expense.domain.ExpenseSplit;
import com.example.splitbill.expense.dto.AddExpenseRequestDto;
import com.example.splitbill.expense.service.ExpenseSplitService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SharesSplitStrategy implements ExpenseSplitService {

    @Override
    public List<ExpenseSplit> splitExpense(AddExpenseRequestDto addExpenseRequestDto) {
        List<ExpenseSplit> splits = new ArrayList<>();
        var userExpenses = addExpenseRequestDto.getUsersSharingExpense();
        var totalShares = userExpenses.values().stream().mapToInt(BigDecimal::intValue).sum();
        var perShare = addExpenseRequestDto.getAmount().divide(BigDecimal.valueOf(totalShares),
                RoundingMode.DOWN);
        for (Long user : addExpenseRequestDto.getUsersSharingExpense().keySet()) {
            var split = new ExpenseSplit();
            split.setAmount(perShare.multiply(userExpenses.get(user)));
            split.setOwedBy(user);
            split.setPaidBy(addExpenseRequestDto.getPaidByUsers());
            splits.add(split);
        }
        return splits;
    }
}
