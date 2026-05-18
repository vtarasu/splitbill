package com.example.splitbill.user.dto;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SettleBalanceRequestDto {
    Long toUserId;
    BigDecimal amount;
}
