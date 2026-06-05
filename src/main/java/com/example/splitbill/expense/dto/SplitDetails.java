package com.example.splitbill.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SplitDetails {
    private Long userId;
    private BigDecimal amount;
    private Integer shares;
    private String userName;

    public SplitDetails(Long id, BigDecimal balance) {
        this.userId = id;
        this.amount = balance;
    }
}