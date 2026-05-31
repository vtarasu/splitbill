package com.example.splitbill.user.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
public class SettlementHistoryResponseDto {
    private Integer totalPages;
    private Integer totalSettlements;
    private Integer currentPage;
    private BigDecimal totalAmount;
    private List<SettlementDto> settlements;
}
