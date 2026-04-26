package com.example.splitbill.expense.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Double amount;

    @ManyToOne
    @JoinColumn(name = "expense_id")
    private Expense expense;
}
