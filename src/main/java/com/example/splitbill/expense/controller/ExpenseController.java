package com.example.splitbill.expense.controller;

import com.example.splitbill.expense.domain.GroupBalances;
import com.example.splitbill.expense.dto.AddExpenseRequestDto;
import com.example.splitbill.expense.service.ExpenseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/expense")
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }


    @PostMapping("/add")
    public List<GroupBalances> addExpense(@RequestBody AddExpenseRequestDto addExpenseRequestDto) {
        log.info("Received request to add expense for group. groupId={} expensePaidBy={} expenseSplit={}",
                addExpenseRequestDto.getGroupId(), addExpenseRequestDto.getPaidByUsers(),
                addExpenseRequestDto.getUsersSharingExpense());
        var groupBalances = expenseService.addExpense(addExpenseRequestDto);
        log.info("Expense added successfully. newBalances={}", groupBalances);
        return groupBalances;
    }
}
