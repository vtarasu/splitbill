package com.example.splitbill.expense.dto;

import com.example.splitbill.expense.domain.Expense;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class GetExpensesResponseDto {
    private Long expenseId;
    private String description;
    private String groupId;
    private String paidBy;
    private String addedBy;
    private BigDecimal amount;
    private LocalDate addedAt;

    public static GetExpensesResponseDto from(Expense expense) {
        return GetExpensesResponseDto.builder()
                .expenseId(expense.getId())
                .groupId(expense.getGroup().getGroupName())
                .description(expense.getExpense())
                .paidBy(expense.getPaidByUser().getUsername())
                .addedBy(expense.getAddedByUser().getUsername())
                .amount(expense.getBillAmount())
                .addedAt(expense.getExpenseDate())
                .build();
    }
}
