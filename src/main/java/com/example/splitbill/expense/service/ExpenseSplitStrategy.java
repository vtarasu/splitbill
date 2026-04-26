package com.example.splitbill.expense.service;

import com.example.splitbill.expense.domain.ExpenseSplit;
import com.example.splitbill.expense.dto.SplitStrategy;

public class ExpenseSplitStrategy {
    public static ExpenseSplitService getExpenseSplitStrategy(SplitStrategy splitStrategy) {
        switch (splitStrategy) {
            case EQUAL -> {
                return new EqualSplitStrategy();
            }
            default -> throw new RuntimeException("Invalid strategy");
        }
    }
}
