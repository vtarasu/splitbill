package com.example.splitbill.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetExpensesRequestDto {
    private Long groupId;
    private int pageNo;
    private int pageSize;
}
