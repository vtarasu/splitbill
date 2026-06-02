package com.example.splitbill.expense.controller;

import com.example.splitbill.expense.dto.*;
import com.example.splitbill.expense.service.ExpenseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                addExpenseRequestDto.getGroupId(), addExpenseRequestDto.getPaidBy(),
                addExpenseRequestDto.getSplitDetails());
        var addExpenseResponseDto = expenseService.addExpense(addExpenseRequestDto);
        log.info("Expense added successfully. response={}", addExpenseResponseDto);
        return addExpenseResponseDto;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable long id) {
        log.info("Received request to delete expense id. id={}", id);
        var expenseResponseDto = expenseService.deleteExpense(id);
        log.info("Expense deleted successfully. id={}", id);
        return ResponseEntity.ok("Expense deleted successfully");
    }

    @PostMapping("/update")
    public ExpenseResponseDto updateExpense(@RequestBody AddExpenseRequestDto expenseRequestDto) {
        log.info("Received request to update expense id. id={}", expenseRequestDto.getId());
        var expenseResponseDto = expenseService.updateExpense(expenseRequestDto);
        log.info("Expense updated successfully. id={}", expenseResponseDto.getId());
        return expenseResponseDto;
    }

    @GetMapping("/{id}")
    public ExpenseDetailsDto getExpense(@PathVariable Long id) {
        log.info("Received request to fetch expense id. id={}", id);
        var expenseDetailsDto = expenseService.getExpenseById(id);
        log.info("Expense fetched successfully. id={}", expenseDetailsDto.getId());
        return expenseDetailsDto;
    }

    @GetMapping("/group")
    public ResponseEntity<?> getExpensesInGroup(@RequestParam Long groupId,
                                             @RequestParam Integer pageNo,
                                             @RequestParam Integer pageSize) {
        log.info("Received request to get expenses for group. groupId={}", groupId);
        var expenses = expenseService.getExpensesInGroup(groupId, pageNo, pageSize);
        log.info("Retrieved expenses successfully for group={}.", groupId);
        return ResponseEntity.ok(expenses);
    }
}
