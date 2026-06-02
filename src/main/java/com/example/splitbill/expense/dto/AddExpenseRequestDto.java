package com.example.splitbill.expense.dto;

import com.example.splitbill.expense.domain.Expense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AddExpenseRequestDto {
    private Long id;
    private String expenseName;
    private Long paidBy;
    private Long groupId;
    private List<SplitDetails> splitDetails;
    private Long addedByUser;
    private BigDecimal amount;
    private SplitStrategy splitStrategy;
    private LocalDate expenseDate;

    public static AddExpenseRequestDto from(Expense expense, UpdateExpenseRequestDto expenseRequestDto) {
        var expenseDate = Objects.nonNull(expenseRequestDto.getExpenseDate()) ?
                expenseRequestDto.getExpenseDate() : expense.getExpenseDate();
        var expenseName = Objects.nonNull(expenseRequestDto.getExpenseName()) ?
                expenseRequestDto.getExpenseName() : expense.getExpense();
        return AddExpenseRequestDto.builder()
                .expenseDate(expenseDate)
                .expenseName(expenseName)
                .addedByUser(expenseRequestDto.getAddedByUser())
                .groupId(expenseRequestDto.getGroupId())
                .splitDetails(expenseRequestDto.getSplitDetails())
                .paidBy(expenseRequestDto.getPaidByUsers())
                .splitStrategy(expenseRequestDto.getSplitStrategy())
                .amount(expenseRequestDto.getAmount())
                .build();
    }
}
