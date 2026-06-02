package com.example.splitbill.expense.service.strategy;

import com.example.splitbill.expense.domain.ExpenseSplit;
import com.example.splitbill.expense.dto.AddExpenseRequestDto;
import com.example.splitbill.expense.dto.SplitDetails;
import com.example.splitbill.expense.service.ExpenseSplitService;
import com.example.splitbill.user.domain.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SharesSplitStrategy implements ExpenseSplitService {

    @Override
    public List<ExpenseSplit> splitExpense(Map<Long, User> users, AddExpenseRequestDto addExpenseRequestDto) {
        List<ExpenseSplit> splits = new ArrayList<>();
        var totalShares = BigDecimal.valueOf(addExpenseRequestDto.getSplitDetails().stream()
                .mapToInt(SplitDetails::getShares)
                .sum());
        var perShare = addExpenseRequestDto.getAmount().divide(totalShares, 2, RoundingMode.HALF_UP);
        var reminder = addExpenseRequestDto.getAmount().subtract(perShare.multiply(totalShares));
        var paidByUser = users.get(addExpenseRequestDto.getPaidBy());
        for (SplitDetails splitDetail : addExpenseRequestDto.getSplitDetails()) {
            var split = new ExpenseSplit();
            split.setAmount(perShare.multiply(BigDecimal.valueOf(splitDetail.getShares())));
            split.setOwedBy(users.get(splitDetail.getUserId()));
            split.setPaidBy(paidByUser);
            splits.add(split);
        }
        if (reminder.compareTo(BigDecimal.ZERO) != 0) {
            var split = splits.getFirst();
            split.setAmount(split.getAmount().add(reminder));
        }
        return splits;
    }
}
