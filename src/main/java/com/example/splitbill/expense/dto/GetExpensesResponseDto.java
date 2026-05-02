package com.example.splitbill.expense.dto;

import com.example.splitbill.expense.domain.Expense;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class GetExpensesResponseDto {
    private Long expenseId;
    private String description;
    private Long groupId;
    private Long paidBy;
    private Double amount;
    private LocalDate addedAt;

    public static GetExpensesResponseDto from(Expense expense) {
        return GetExpensesResponseDto.builder()
                .expenseId(expense.getId())
                .groupId(expense.getGroupId())
                .description(expense.getExpense())
                .paidBy(expense.getUserId())
                .amount(expense.getBillAmount())
                .addedAt(expense.getAddedAt())
                .build();
    }
}
