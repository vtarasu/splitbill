package com.example.splitbill.expense.service.strategy;

import com.example.splitbill.expense.domain.ExpenseSplit;
import com.example.splitbill.expense.dto.AddExpenseRequestDto;
import com.example.splitbill.expense.service.ExpenseSplitService;

import java.util.ArrayList;
import java.util.List;

public class EqualSplitStrategy implements ExpenseSplitService {

    @Override
    public List<ExpenseSplit> splitExpense(AddExpenseRequestDto addExpenseRequestDto) {
        Double perPersonShare = addExpenseRequestDto.getAmount() / addExpenseRequestDto.getUsersSharingExpense().size();
        List<ExpenseSplit> splits = new ArrayList<>();
        for (Long user : addExpenseRequestDto.getUsersSharingExpense().keySet()) {
            var split = new ExpenseSplit();
            split.setAmount(perPersonShare);
            split.setOwedBy(user);
            split.setPaidBy(addExpenseRequestDto.getPaidByUsers());
            splits.add(split);
        }
        return splits;
    }
}
