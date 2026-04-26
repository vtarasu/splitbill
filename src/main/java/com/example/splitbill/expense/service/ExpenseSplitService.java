package com.example.splitbill.expense.service;

import com.example.splitbill.expense.domain.ExpenseSplit;
import com.example.splitbill.expense.dto.AddExpenseRequestDto;

import java.util.List;

public interface ExpenseSplitService {
    List<ExpenseSplit> splitExpense(AddExpenseRequestDto addExpenseRequestDto);
}
