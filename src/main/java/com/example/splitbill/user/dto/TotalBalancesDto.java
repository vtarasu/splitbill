package com.example.splitbill.user.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TotalBalancesDto {
    private Long userId;
    private String userName;
    private BigDecimal amount;
    private Direction direction;
}
