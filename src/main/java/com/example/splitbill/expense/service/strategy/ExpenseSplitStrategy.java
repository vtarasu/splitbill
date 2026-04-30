package com.example.splitbill.expense.service.strategy;

import com.example.splitbill.expense.dto.SplitStrategy;
import com.example.splitbill.expense.service.ExpenseSplitService;

public class ExpenseSplitStrategy {
    public static ExpenseSplitService getExpenseSplitStrategy(SplitStrategy splitStrategy) {
        switch (splitStrategy) {
            case EQUAL -> {
                return new EqualSplitStrategy();
            }
            case EXACT -> {
                return new ExactSplitStrategy();
            }
            case SHARES -> {
                return new SharesSplitStrategy();
            }
            default -> throw new RuntimeException("Invalid strategy");
        }
    }
}
