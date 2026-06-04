package com.example.splitbill.expense.dto;

import com.example.splitbill.expense.domain.Expense;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Data
@Builder
public class GetAllExpensesResponseDto {
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

    public static GetAllExpensesResponseDto from(Expense expense, List<SplitDetails> splitDetails) {
        var groupId = Objects.isNull(expense.getGroup()) ? null : expense.getGroup().getId();
        var groupName = Objects.isNull(expense.getGroup()) ? "" : expense.getGroup().getGroupName();
        return GetAllExpensesResponseDto.builder()
                .expenseId(expense.getId())
                .groupId(groupId)
                .groupName(groupName)
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
