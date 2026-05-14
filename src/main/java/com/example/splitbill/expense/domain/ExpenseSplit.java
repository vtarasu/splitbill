package com.example.splitbill.expense.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long paidBy;
    private Long owedBy;
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "expense_id")
    private Expense expense;
}
