package com.example.splitbill.expense.dto;

import com.example.splitbill.expense.domain.Expense;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    private LocalDate expenseDate;
    private String expenseStrategy;
    private List<SplitDetails> splitDetails;

    public static ExpensesInGroupResponseDto from(Expense expense, List<SplitDetails> splitDetails) {
        return ExpensesInGroupResponseDto.builder()
                .expenseId(expense.getId())
                .groupId(expense.getGroup().getId())
                .groupName(expense.getGroup().getGroupName())
                .description(expense.getExpense())
                .paidBy(expense.getPaidByUser().getUsername())
                .addedBy(expense.getAddedByUser().getUsername())
                .amount(expense.getBillAmount())
                .expenseDate(expense.getExpenseDate())
                .expenseStrategy(expense.getSplitStrategy().name())
                .splitDetails(splitDetails)
                .build();
    }
}
