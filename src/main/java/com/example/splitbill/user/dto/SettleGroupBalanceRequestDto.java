package com.example.splitbill.user.dto;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SettleGroupBalanceRequestDto {
    Long groupId;
    String from;
    String to;
    BigDecimal amount;
}
