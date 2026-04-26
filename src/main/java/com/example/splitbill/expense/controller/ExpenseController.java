package com.example.splitbill.expense.controller;

import com.example.splitbill.expense.dto.AddExpenseRequestDto;
import com.example.splitbill.expense.dto.AddExpenseResponseDto;
import com.example.splitbill.expense.service.ExpenseService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/expense")
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }


    @PostMapping("/add")
    public void addExpense(AddExpenseRequestDto addExpenseRequestDto) {
        expenseService.addExpense(addExpenseRequestDto);
    }
}
