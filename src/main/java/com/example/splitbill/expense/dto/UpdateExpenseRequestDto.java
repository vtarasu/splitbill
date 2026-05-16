package com.example.splitbill.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UpdateExpenseRequestDto {
    private Long id;
    private String expenseName;
    private Long groupId;
    private Long paidByUsers;
    private Map<Long, BigDecimal> usersSharingExpense;
    private Long addedByUser;
    private BigDecimal amount;
    private SplitStrategy splitStrategy;
    private LocalDate expenseDate;
    private boolean isAmountUpdateRequire;
}
