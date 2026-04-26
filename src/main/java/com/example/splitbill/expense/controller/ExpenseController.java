package com.example.splitbill.expense.controller;

import com.example.splitbill.expense.domain.Expense;
import com.example.splitbill.expense.dto.AddExpenseRequestDto;
import com.example.splitbill.expense.service.ExpenseService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public Expense addExpense(@RequestBody AddExpenseRequestDto addExpenseRequestDto) {
        return expenseService.addExpense(addExpenseRequestDto);
    }
}
