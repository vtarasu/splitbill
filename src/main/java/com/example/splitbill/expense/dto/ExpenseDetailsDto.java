package com.example.splitbill.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ExpenseDetailsDto {
    private Long id;
    private String expenseName;
    private BigDecimal billAmount;
    private LocalDate expenseDate;
    private SplitStrategy splitStrategy;
    private String paidBy;
    private String addedBy;
    private String groupName;
    private List<ExpenseSplitDto> expenseSplit;
}
