package com.example.splitbill.expense.controller;

import com.example.splitbill.expense.dto.*;
import com.example.splitbill.expense.service.ExpenseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    public ExpenseResponseDto addExpense(@RequestBody AddExpenseRequestDto addExpenseRequestDto) {
        log.info("Received request to add expense for group. groupId={} expensePaidBy={} expenseSplit={}",
                addExpenseRequestDto.getGroupId(), addExpenseRequestDto.getPaidByUsers(),
                addExpenseRequestDto.getUsersSharingExpense());
        var addExpenseResponseDto = expenseService.addExpense(addExpenseRequestDto);
        log.info("Expense added successfully. response={}", addExpenseResponseDto);
        return addExpenseResponseDto;
    }

    @PostMapping("/delete/{id}")
    public ExpenseResponseDto deleteExpense(@PathVariable long id) {
        log.info("Received request to delete expense id. id={}", id);
        var expenseResponseDto = expenseService.deleteExpense(id);
        log.info("Expense deleted successfully. id={}", id);
        return expenseResponseDto;
    }

    @PostMapping("/update")
    public ExpenseResponseDto updateExpense(@RequestBody UpdateExpenseRequestDto expenseRequestDto) {
        log.info("Received request to update expense id. id={}", expenseRequestDto.getId());
        var expenseResponseDto = expenseService.updateExpense(expenseRequestDto);
        log.info("Expense updated successfully. id={}", expenseResponseDto.getId());
        return expenseResponseDto;
    }

    @GetMapping
    public List<GetExpensesResponseDto> getExpenses(@RequestBody GetExpensesRequestDto getExpensesRequestDto) {
        log.info("Received request to get expenses for group. request={}", getExpensesRequestDto);
        var expenses = expenseService.getExpenses(getExpensesRequestDto);
        log.info("Retrieved expenses successfully. expenses={}", expenses.size());
        return expenses;
    }
}
