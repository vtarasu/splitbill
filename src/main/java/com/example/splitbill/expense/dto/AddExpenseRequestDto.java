package com.example.splitbill.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AddExpenseRequestDto {
    private String expenseName;
    private Map<Long, Double> paidByUsers;
    private Map<Long, Double> usersSharingExpense;
    private Long addedByUser;
    private Double amount;
    private SplitStrategy splitStrategy;
    private LocalDate expenseDate;
}
