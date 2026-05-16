package com.example.splitbill.expense.dto;

import com.example.splitbill.expense.domain.Expense;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ExpensesInGroupResponseDto {
    private Long expenseId;
    private String description;
    private Long groupId;
    private String groupName;
    private String paidBy;
    private String addedBy;
    private BigDecimal amount;
    private LocalDate addedAt;

    public static ExpensesInGroupResponseDto from(Expense expense) {
        return ExpensesInGroupResponseDto.builder()
                .expenseId(expense.getId())
                .groupId(expense.getGroup().getId())
                .groupName(expense.getGroup().getGroupName())
                .description(expense.getExpense())
                .paidBy(expense.getPaidByUser().getUsername())
                .addedBy(expense.getAddedByUser().getUsername())
                .amount(expense.getBillAmount())
                .addedAt(expense.getExpenseDate())
                .build();
    }
}
